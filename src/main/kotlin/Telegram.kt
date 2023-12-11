import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    private val botToken: String,
    private var updateId: Int,
) {
    fun getUpdates(): String {
        val urlGetUpdates = "$QUERIES_TO_THE_TELEGRAM_BOT_API$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chat_id: String, text: String): String {
        val urlSendMessage = "$QUERIES_TO_THE_TELEGRAM_BOT_API$botToken/sendMessage?chat_id=$chat_id&text=$text"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

}

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

    val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val userMessageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val idMessageTextRegex: Regex = "\"id\":(.+?),".toRegex()

    while (true) {
        Thread.sleep(2000)
        val bot = TelegramBotService(botToken, updateId)
        val updates: String = bot.getUpdates()
        println(updates)

        val update_id = getSubstring(updateIdRegex, updates)
        println(update_id)

        updateId = update_id?.toInt()?.plus(1) ?: continue

        val userText = getSubstring(userMessageTextRegex, updates)
        println(userText)

        val chatId = getSubstring(idMessageTextRegex, updates)
        println(chatId)
        if (chatId != null && userText == "Hello") {
            bot.sendMessage(chatId, "Hello")
        }
    }
}

fun getSubstring(regex: Regex, updates: String): String? {
    val matchResult: MatchResult? = regex.find(updates)
    val groups = matchResult?.groups
    val text = groups?.get(1)?.value
    return text
}

const val QUERIES_TO_THE_TELEGRAM_BOT_API = "https://api.telegram.org/bot"