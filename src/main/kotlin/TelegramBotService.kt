import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyBoard>>,
)

@Serializable
data class InlineKeyBoard(
    @SerialName("text")
    val text: String,
    @SerialName("callback_data")
    val callbackData: String,
)

class TelegramBotService(
    private val botToken: String,
    private var updateId: Long,
) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun getUpdates(): Response {
        val urlGetUpdates = "$QUERIES_TO_THE_TELEGRAM_BOT_API$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        println(response.body())
        val responseDeserialization: Response = json.decodeFromString(response.body())
        return responseDeserialization
    }

    private fun sendMessage(chatId: Long, message: String): String {
        val urlSendMessage = "$QUERIES_TO_THE_TELEGRAM_BOT_API$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendMenu(chatId: Long): String {
        val urlSendMessage = "$QUERIES_TO_THE_TELEGRAM_BOT_API$botToken/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyBoard(text = "Изучать слова", callbackData = USER_CHOOSE_LEARN_WORDS),
                        InlineKeyBoard(text = "Статистика", callbackData = USER_CHOOSE_STATISTICS),
                    ),
                    listOf(
                        InlineKeyBoard(text = "Cбросить прогресс", callbackData = RESET_CLICKED),
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendQuestions(chatId: Long, question: Question): String {
        val urlSendMessage = "$QUERIES_TO_THE_TELEGRAM_BOT_API$botToken/sendMessage"

        val backToMenuButton = listOf(
            InlineKeyBoard(
                text = "Вернуться в меню", callbackData = "$CALLBACK_DATA_ANSWER_PREFIX${CALLBACK_DATA_ANSWER_TO_MENU}"
            )
        )
        val learnWordsSectionButtons = question.variants.mapIndexed { index, word ->
            InlineKeyBoard(
                text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
            )
        }.plus(backToMenuButton).chunked(1)

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.questionWord,
            replyMarkup = ReplyMarkup(learnWordsSectionButtons)
        )

        val requestBodyString = json.encodeToString(requestBody)

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Long) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(chatId, "Вы выучили все слова в базе")
        } else {
            sendQuestions(chatId, question)
        }

    }

    fun handleUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>) {

        val userMessage = update.message?.text
        val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
        val data = update.callbackQuery?.data

        val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

        if (userMessage?.lowercase() == "/start"
            || userMessage?.lowercase() == "start"
            || userMessage?.lowercase() == "menu"
        ) {
            sendMenu(chatId)
        }

        if (data?.lowercase() == USER_CHOOSE_STATISTICS) {
            val statistics = trainer.getStatistics()
            val stringWithStatistics =
                "Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%"
            sendMessage(chatId, stringWithStatistics)
        }

        if (data?.lowercase() == USER_CHOOSE_LEARN_WORDS) {
            checkNextQuestionAndSend(trainer, chatId)
        }

        if (data?.lowercase()?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val responseNumber = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (responseNumber == CALLBACK_DATA_ANSWER_TO_MENU) {
                sendMenu(chatId)
                return
            }
            if (trainer.checkAnswer(responseNumber)) {
                sendMessage(chatId, "Правильно")
            } else {
                sendMessage(
                    chatId,
                    "Не правильно: ${trainer.question?.correctAnswer?.questionWord} - ${
                        trainer.question?.correctAnswer?.translate
                    }"
                )
            }
            checkNextQuestionAndSend(trainer, chatId)
        }

        if (data?.lowercase() == RESET_CLICKED) {
            trainer.resetProgress()
            sendMessage(chatId, "Прогресс сброшен")
        }

    }

}
