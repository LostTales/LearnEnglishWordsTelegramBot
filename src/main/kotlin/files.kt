import java.io.File

fun main() {

    val wordsFile = File("words.txt")

    val dictionary = mutableListOf<Word>()

    wordsFile.readLines().forEach {
        val line = it.split("|")
        val correctAnswers: Int = if (line.size >= 3) line[2]?.toIntOrNull() ?: 0 else 0
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = correctAnswers)
        dictionary.add(word)
    }

    dictionary.forEach { println(it) }
}

fun File.createDictionary(text: String) {

    createNewFile()
    appendText(text)
}