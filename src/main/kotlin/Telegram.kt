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