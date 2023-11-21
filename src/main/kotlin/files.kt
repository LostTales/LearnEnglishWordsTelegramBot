import java.io.File

fun main() {

    val wordsFile = File("words.txt")

    showStartScreen(wordsFile.createDictionary("hello|привет|25"))
}

fun File.createDictionary(text: String): MutableList<Word> {

    createNewFile()
    appendText(text)
    appendText("\n")

    val dictionary = mutableListOf<Word>()

    this.readLines().forEach {
        val line = it.split("|")
        val correctAnswers: Int = line.getOrNull(2)?.toIntOrNull() ?: 0
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = correctAnswers)
        dictionary.add(word)
    }
    return dictionary
}

fun showStartScreen(dictionary: MutableList<Word>) {

    do {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        val userSelection = readln().toIntOrNull()
        when (userSelection) {
            NUMBER_1_ON_THE_MENU -> {
                println("Учить слова")
            }

            NUMBER_2_ON_THE_MENU -> {
                println("Статистика")
                println(calculateStatistics(dictionary))
            }

            NUMBER_0_ON_THE_MENU -> {
                println("Выход")
                break
            }

            else -> {
                println("Неверные данные.\nПовторите ввод.")
            }
        }

    } while (true)
}

fun calculateStatistics(dictionary: MutableList<Word>): String {

    val numberOfWordsLearned = dictionary.filter { it.correctAnswersCount >= NUMBER_3 }.count()
    val numberOfWords = dictionary.map { it.original }.count()
    val percentageOfCorrectAnswers = Math.round((numberOfWordsLearned.toDouble() / numberOfWords) * NUMBER_100)
    return "Выучено $numberOfWordsLearned из $numberOfWords слов | $percentageOfCorrectAnswers%"
}

const val NUMBER_0_ON_THE_MENU = 0
const val NUMBER_1_ON_THE_MENU = 1
const val NUMBER_2_ON_THE_MENU = 2
const val NUMBER_3 = 3
const val NUMBER_100 = 100
