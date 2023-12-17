import java.io.File

data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

data class Statistics(
    val learned: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4,
) {

    internal var question: Question? = null
    private val wordsFile = File("words.txt")
    val dictionary = loadDictionary(wordsFile)

    fun getStatistics(): Statistics {
        val learned =
            dictionary.count { it.correctAnswersCount >= learnedAnswerCount }
        val total = dictionary.map { it.questionWord }.count()
        val percent =
            (Math.round((learned.toDouble() / total) * PERCENTAGE_OF_THE_NUMBER)).toInt()
        return Statistics(learned, total, percent)
    }

    fun getNextQuestion(): Question? {
        val notLearnedList: List<Word> =
            dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (notLearnedList.isEmpty()) return null
        var questionWords: List<Word> = notLearnedList.shuffled().take(countOfQuestionWords)
        val learningWord = questionWords.random()
        if (questionWords.size < countOfQuestionWords) {
            questionWords = (questionWords + dictionary
                .filter { (it.correctAnswersCount >= learnedAnswerCount) }
                .shuffled().take(countOfQuestionWords - questionWords.size)).shuffled()
        }
        question = Question(
            variants = questionWords,
            correctAnswer = learningWord
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(wordsFile, dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(file: File): List<Word> {
        try {
            val newDictionary = file.readLines()
                .map {
                    val line = it.split("|")
                    val correctAnswers: Int = line.getOrNull(2)?.toIntOrNull() ?: 0
                    Word(questionWord = line[0], translate = line[1], correctAnswersCount = correctAnswers)
                }
            return newDictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл")
        }
    }

    private fun saveDictionary(file: File, dictionary: List<Word>) {
        file.writeText("")
        dictionary.forEach {
            val line = "${it.questionWord}|${it.translate}|${it.correctAnswersCount}\n"
            file.appendText(line)
        }
    }

}