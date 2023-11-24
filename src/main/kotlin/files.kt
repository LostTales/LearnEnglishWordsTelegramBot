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
            val fourRandomWords = unlearnedWords.shuffled().take(NUMBER_OF_WORDS_TO_CHOOSE_CORRECT_ANSWER)
            val randomWord = fourRandomWords.random()
            println(
                """
                |Переведите слово ${randomWord.original}
                |варианты ответов:
                |${
                    fourRandomWords.mapIndexed { index, word ->
                        "${index + 1}.${word.translate}"
                    }.joinToString("\n")
                }
                |0.Вернуться в меню 
            """.trimMargin()
            )
            val userSelection = readln().toIntOrNull()

            if (userSelection == (fourRandomWords.indexOf(randomWord) + ADD_VALUE_FOR_COUNTING_FROM_ONE)) {
                println("Ваш ответ правильный")
                randomWord.correctAnswersCount++
                saveDictionary(dictionary)
            } else if (userSelection == MENU_ITEM_EXIT) {
                println("Выход в меню")
                break
            } else {
                println("Неверный ответ")
            }
        }
    } while (true)
}

fun File.newCreateDictionary(text: String){
    createNewFile()
    appendText(text)
    appendText("\n")
}

fun saveDictionary(dictionary: MutableList<Word>) {


    val userWords = File("userWords.txt")
    dictionary.forEach {
        val line = "${it.original}|${it.translate}|${it.correctAnswersCount}"
        userWords.newCreateDictionary(line)
    }
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
const val NUMBER_OF_WORDS_TO_CHOOSE_CORRECT_ANSWER = 4
const val ADD_VALUE_FOR_COUNTING_FROM_ONE = 1