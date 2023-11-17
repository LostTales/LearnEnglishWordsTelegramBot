import java.io.File

fun main() {

    val wordsFile = File("words.txt")
    wordsFile.createNewFile()
    wordsFile.writeText("hello|привет|25\n")
    wordsFile.appendText("dog|собака|10\n")
    wordsFile.appendText("cat|кошка|5\n")

    val dictionary = mutableListOf<Word>()

    wordsFile.readLines().forEach {
        val line = it.split("|")
        val answersCount = line[2]
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = answersCount ?: "0")
        dictionary.add(word)
    }

    dictionary.forEach { println(it) }
}