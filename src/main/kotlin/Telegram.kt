import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

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

    fun sendMessage(chat_id: Int, message: String): String {
        val encoded = URLEncoder.encode(
            message,
            StandardCharsets.UTF_8
        )
        println(encoded)
        val urlSendMessage = "$QUERIES_TO_THE_TELEGRAM_BOT_API$botToken/sendMessage?chat_id=$chat_id&text=$encoded"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chat_id: Int): String {
        val urlSendMessage = "$QUERIES_TO_THE_TELEGRAM_BOT_API$botToken/sendMessage"
        val sendMenuBody = """
            {
            	"chat_id": $chat_id,
            	"text": "Основное меню",
            	"reply_markup": {
            		"inline_keyboard": [
            			[
            				{
            				   "text": "Изучить слова",
            				   "callback_data": "$USER_CHOOSE_LEARN_WORDS"
            				},
            				{
            					"text": "Статистика",
            					"callback_data": "$USER_CHOOSE_STATISTICS"
            				}
            			]
            		]
            	}
            }
        """.trimIndent()

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestions(chat_id: Int, question: Question): String {
        val urlSendMessage = "$QUERIES_TO_THE_TELEGRAM_BOT_API$botToken/sendMessage"
        val newQuestionWord = question.correctAnswer.questionWord
        val listOfWordVariants = question.variants.map { it.translate }
        val sendMenuBody = """
            {
            	"chat_id": $chat_id,
            	"text": "$newQuestionWord",
            	"reply_markup": {
            		"inline_keyboard": [
            			[
            				{
            					"text": "${question.variants[0].translate}",
            					"callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${
            listOfWordVariants.indexOf(question.variants[0].translate)
        }"
            				}
            			],
            			[
            				{
            					"text": "${question.variants[1].translate}",
            					"callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${
            listOfWordVariants.indexOf(question.variants[1].translate)
        }"
            				}
            			],
            			[
            				{
            					"text": "${question.variants[2].translate}",
            					"callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${
            listOfWordVariants.indexOf(question.variants[2].translate)
        }"
            				}
            			],
            			[
            				{
            					"text": "${question.variants[3].translate}",
            					"callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${
            listOfWordVariants.indexOf(question.variants[3].translate)
        }"
            				}
            			],
            			[
            				{
            					"text": "Вернуться в меню",
            					"callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${4}"
            				}
            			]
            		]
            	}
            }
        """.trimIndent()

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chat_id: Int?) {
        val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()
        val data = getSubstring(dataRegex, getUpdates())
        if (chat_id != null && data?.lowercase() == USER_CHOOSE_LEARN_WORDS) {
            if (trainer.getNextQuestion() == null) {
                sendMessage(chat_id, "Вы выучили все слова в базе")
            } else {
                sendQuestions(chat_id, trainer.getNextQuestion()!!)
            }
        }
    }

}

fun main(args: Array<String>) {

    val trainer = try {
        LearnWordsTrainer(3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }
    val statistics = trainer.getStatistics()
    val stringWithStatistics = "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%"

    val botToken = args[0]
    var updateId = 0

    val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val userMessageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val idMessageTextRegex: Regex = "\"chat\":\\{\"id\":(.+?),".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val bot = TelegramBotService(botToken, updateId)
        val updates: String = bot.getUpdates()
        println(updates)

        val update_id = getSubstring(updateIdRegex, updates)
        println(update_id)

        updateId = update_id?.toIntOrNull()?.plus(1) ?: continue

        val userText = getSubstring(userMessageTextRegex, updates)
        println(userText)

        val chatId = getSubstring(idMessageTextRegex, updates)?.toInt()
        println(chatId)

        val data = getSubstring(dataRegex, updates)
        println(data)

        if (chatId != null && (
                    userText?.lowercase() == "/start"
                            || userText?.lowercase() == "start"
                            || userText?.lowercase() == "menu")
        ) {
            bot.sendMenu(chatId)
        }

        if (chatId != null && data?.lowercase() == USER_CHOOSE_STATISTICS) {
            bot.sendMessage(chatId, stringWithStatistics)
        }

        bot.checkNextQuestionAndSend(trainer, chatId)

        if (chatId != null && data?.lowercase()?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val responseNumber = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (responseNumber == 4) {
                bot.sendMenu(chatId)
                continue
            }
            if (trainer.checkAnswer(responseNumber)) {
                bot.sendMessage(chatId, "Правильно")
            } else {
                bot.sendMessage(
                    chatId,
                    "Не правильно: ${trainer.question?.correctAnswer?.questionWord} - ${
                        trainer.question?.correctAnswer?.translate
                    }"
                )
            }
            trainer.getNextQuestion()?.let { bot.sendQuestions(chatId, it) }
            bot.checkNextQuestionAndSend(trainer, chatId)
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
const val USER_CHOOSE_LEARN_WORDS = "learn_words_clicked"
const val USER_CHOOSE_STATISTICS = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"