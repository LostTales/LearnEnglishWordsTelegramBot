import java.io.File

fun main() {

    val wordsFile = File("words.txt")
    val dictionary = loadDictionary(wordsFile)
    showStartScreen(dictionary)
}

fun createDictionary(file: File, text: String): List<Word> {

    file.createNewFile()
    file.appendText(text)
    file.appendText("\n")

    val dictionary = mutableListOf<Word>()

    file.readLines().forEach {
        val line = it.split("|")
        val correctAnswers: Int = line.getOrNull(2)?.toIntOrNull() ?: 0
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = correctAnswers)
        dictionary.add(word)
    }
    return dictionary
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

fun learnWords(dictionary: List<Word>) {

    do {
        val unlearnedWords: List<Word> =
            dictionary.filter { it.correctAnswersCount < MIN_NUMBER_OF_CORRECT_ANSWERS_TO_STUDY_WORD }
        if (unlearnedWords.isEmpty()) {
            println("Вы выучили все слова")
            break
        } else {
            var questionWords: List<Word> = unlearnedWords.shuffled().take(NUMBER_OF_WORDS_TO_CHOOSE_CORRECT_ANSWER)
            val learningWord = questionWords.random()
            if (questionWords.size < NUMBER_OF_WORDS_TO_CHOOSE_CORRECT_ANSWER) {
                questionWords = questionWords + dictionary
                    .filter { (it.correctAnswersCount >= MIN_NUMBER_OF_CORRECT_ANSWERS_TO_STUDY_WORD) }
                    .shuffled()
            }

            println(
                questionWords
                    .mapIndexed { index, word -> "${index + 1}.${word.translate}" }
                    .joinToString(
                        prefix = "Переведите слово ${learningWord.original}, варианты ответов:\n",
                        separator = "\n",
                        postfix = "\n0. Вернуться в меню",
                    )
            )
            val userSelection = readln().toIntOrNull()

            if (userSelection == (questionWords.indexOf(learningWord) + ADD_VALUE_FOR_COUNTING_FROM_ONE)) {
                println("Ваш ответ правильный")
                learningWord.correctAnswersCount++
                saveDictionary(File("words.txt"), dictionary)
            } else if (userSelection == MENU_ITEM_EXIT) {
                println("Выход в меню")
                break
            } else {
                println("Неверный ответ")
            }
        }
    } while (true)
}

fun saveDictionary(file: File, dictionary: List<Word>) {

    file.writeText("")
    dictionary.forEach {
        val line = "${it.original}|${it.translate}|${it.correctAnswersCount}"
        file.appendText(line)
        file.appendText("\n")
    }
}

fun loadDictionary(file: File): List<Word> {

    val newDictionary = file.readLines()
        .map {
            val line = it.split("|")
            val correctAnswers: Int = line.getOrNull(2)?.toIntOrNull() ?: 0
            Word(original = line[0], translate = line[1], correctAnswersCount = correctAnswers)
        }
    return newDictionary
}

fun calculateStatistics(dictionary: List<Word>): String {

    val numberOfWordsLearned =
        dictionary.count { it.correctAnswersCount >= MIN_NUMBER_OF_CORRECT_ANSWERS_TO_STUDY_WORD }
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