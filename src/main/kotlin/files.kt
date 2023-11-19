import java.io.File

fun main() {

    val wordsFile = File("words.txt")

    val dictionary = wordsFile.createDictionary("hello|привет|25")

    calculateStatistics(dictionary)

    showStartScreen(calculateStatistics(dictionary))
}

fun File.createDictionary(text: String): MutableList<Word> {

    createNewFile()
    appendText(text)

    val dictionary = mutableListOf<Word>()

    this.readLines().forEach {
        val line = it.split("|")
        val correctAnswers: Int = line.getOrNull(2)?.toIntOrNull() ?: 0
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = correctAnswers)
        dictionary.add(word)
    }
    return dictionary
}

fun showStartScreen(statistics: String) {

    do {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        val userSelection = readln().toIntOrNull()
        when (userSelection) {
            NUMBER_1 -> println("Учить слова")
            NUMBER_2 -> println(statistics)
            ZERO -> {
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
    val percentageOfCorrectAnswers = (numberOfWordsLearned.toDouble() / numberOfWords) * NUMBER_100
    return "Выучено $numberOfWordsLearned из $numberOfWords слов | $percentageOfCorrectAnswers%"
}

const val ZERO = 0
const val NUMBER_1 = 1
const val NUMBER_2 = 2
const val NUMBER_3 = 3
const val NUMBER_100 = 100
