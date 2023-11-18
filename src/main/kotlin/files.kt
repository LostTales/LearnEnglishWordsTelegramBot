import java.io.File

fun main() {

    val wordsFile = File("words.txt")

    val dictionary = mutableListOf<Word>()

    wordsFile.readLines().forEach {
        val line = it.split("|")
        val correctAnswers = line[2]?.toInt() ?: 0
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = correctAnswers)
        dictionary.add(word)
    }

    dictionary.forEach { println(it) }
}

fun File.createDictionary(text: String){

    createNewFile()
    appendText(text)
}