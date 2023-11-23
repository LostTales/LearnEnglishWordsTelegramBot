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
            MENU_ITEM_LEARN_WORDS -> {
                println("Учить слова")
                learnWords(dictionary)
            }

            MENU_ITEM_STATISTICS -> {
                println("Статистика")
                println(calculateStatistics(dictionary))
            }

            MENU_ITEM_EXIT -> {
                println("Выход")
                break
            }

            else -> {
                println("Неверные данные.\nПовторите ввод.")
            }
        }

    } while (true)
}

fun learnWords(dictionary: MutableList<Word>) {

    do {
        val unlearnedWords = dictionary.filter { it.correctAnswersCount < MIN_NUMBER_OF_CORRECT_ANSWERS_TO_STUDY_WORD }
        if (unlearnedWords.isEmpty()) {
            println("Вы выучили все слова")
            break
        } else {
            val fourRandomWords = unlearnedWords.shuffled().take(4)
            val randomWord = fourRandomWords.map { it.original }.random()
            println(
                """
                Переведите слово $randomWord
                варианты ответов:
                ${
                    fourRandomWords.mapIndexed { index, word ->
                        "${index + 1}.${word.translate}"
                    }.joinToString(" ")
                }
                0.Вернуться в меню 
            """.trimIndent()
            )

            val originalWords = fourRandomWords.map { it.original }
            val userSelection = readln().toIntOrNull()

            if (userSelection == ((originalWords.indexOf(randomWord) + 1))) {
                println("Ваш ответ правильный")
                dictionary.filter { it == fourRandomWords[originalWords.indexOf(randomWord)] }
                    .map { it.correctAnswersCount++ }
            } else if (userSelection == 0) {
                println("Выход в меню")
                break
            } else {
                println("Неверный ответ")
            }
        }
    } while (true)
}

fun calculateStatistics(dictionary: MutableList<Word>): String {

    val numberOfWordsLearned =
        dictionary.filter { it.correctAnswersCount >= MIN_NUMBER_OF_CORRECT_ANSWERS_TO_STUDY_WORD }.count()
    val numberOfWords = dictionary.map { it.original }.count()
    val percentageOfCorrectAnswers =
        Math.round((numberOfWordsLearned.toDouble() / numberOfWords) * PERCENTAGE_OF_THE_NUMBER)
    return "Выучено $numberOfWordsLearned из $numberOfWords слов | $percentageOfCorrectAnswers%"
}

const val MENU_ITEM_EXIT = 0
const val MENU_ITEM_LEARN_WORDS = 1
const val MENU_ITEM_STATISTICS = 2
const val MIN_NUMBER_OF_CORRECT_ANSWERS_TO_STUDY_WORD = 3
const val PERCENTAGE_OF_THE_NUMBER = 100