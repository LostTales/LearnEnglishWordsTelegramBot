import java.io.File

data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {

    val trainer = try {
        LearnWordsTrainer(3, 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    showStartScreen(trainer.dictionary)
}

fun showStartScreen(dictionary: List<Word>) {
    do {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        val userSelection = readln().toIntOrNull()
        when (userSelection) {
            MENU_ITEM_LEARN_WORDS -> {
                println("Учить слова")
                learnWords(dictionary)
            }

            MENU_ITEM_STATISTICS -> {
                println("Статистика")
                val statistics = LearnWordsTrainer(3, 4).getStatistics()
                println("Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}%")
            }

            MENU_ITEM_EXIT -> {
                println("Выход")
                break
            }

            else -> {
                println("Неверные данные.\nВведите 1, 2 или 0")
            }
        }

    } while (true)
}

fun Question.asConsoleString(): String {
    return this.variants
        .mapIndexed { index, word -> "${index + 1} – ${word.translate}" }
        .joinToString(
            prefix = "Переведите слово ${this.correctAnswer.questionWord}, варианты ответов:\n",
            separator = "\n",
            postfix = "\n0 – Вернуться в меню",
        )
}

fun learnWords(dictionary: List<Word>) {
    do {
        val question = LearnWordsTrainer(3, 4).getNextQuestion()
        if (question == null) {
            println("Вы выучили все слова")
            break
        } else {
            println(question.asConsoleString())

            val userAnswerInput = readln().toIntOrNull()

            if (userAnswerInput == MENU_ITEM_EXIT) {
                println("Выход в меню")
                break
            } else if (LearnWordsTrainer().checkAnswer(userAnswerInput?.minus(CONVERSION_TO_THE_INDEX_VALUE))) { // TODO checkAnswer всегда false
                println("Ваш ответ правильный")
            } else {
                println("Неправильно! ${question.correctAnswer.questionWord} – это ${question.correctAnswer.translate}")
            }

        }
    } while (true)
}

fun createDictionary(file: File, text: String): List<Word> {

    file.createNewFile()
    file.appendText(text)
    file.appendText("\n")

    val dictionary = mutableListOf<Word>()

    file.readLines().forEach {
        val line = it.split("|")
        val correctAnswers: Int = line.getOrNull(2)?.toIntOrNull() ?: 0
        val word = Word(questionWord = line[0], translate = line[1], correctAnswersCount = correctAnswers)
        dictionary.add(word)
    }
    return dictionary
}

const val MENU_ITEM_EXIT = 0
const val MENU_ITEM_LEARN_WORDS = 1
const val MENU_ITEM_STATISTICS = 2
const val PERCENTAGE_OF_THE_NUMBER = 100
const val CONVERSION_TO_THE_INDEX_VALUE = 1