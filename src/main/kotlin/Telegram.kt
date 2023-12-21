fun main(args: Array<String>) {

    val trainers = HashMap<Long, LearnWordsTrainer>()

    val botToken: String = args[0]
    var lastUpdateId = 0L

    while (true) {
        Thread.sleep(2000)
        val bot = TelegramBotService(botToken, lastUpdateId)

        if (bot.getUpdates().result.isEmpty()) continue
        val sortedUpdates = bot.getUpdates().result.sortedBy { it.updateId }
        sortedUpdates.forEach { bot.handleUpdate(it, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

const val QUERIES_TO_THE_TELEGRAM_BOT_API = "https://api.telegram.org/bot"
const val USER_CHOOSE_LEARN_WORDS = "learn_words_clicked"
const val USER_CHOOSE_STATISTICS = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val CALLBACK_DATA_ANSWER_TO_MENU = 9
const val RESET_CLICKED = "reset_clicked"