import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json



fun main() {

    val json = Json {
        ignoreUnknownKeys = true
    }

    val responseString = """
        {
        	"ok": true,
        	"result": [
        		{
        			"update_id": 756087051,
        			"message": {
        				"message_id": 498,
        				"from": {
        					"id": 1000015840,
        					"is_bot": false,
        					"first_name": "Vadim",
        					"username": "s_va9im",
        					"language_code": "ru"
        				},
        				"chat": {
        					"id": 1000015840,
        					"first_name": "Vadim",
        					"username": "s_va9im",
        					"type": "private"
        				},
        				"date": 1702919125,
        				"text": "/start",
        				"entities": [
        					{
        						"offset": 0,
        						"length": 6,
        						"type": "bot_command"
        					}
        				]
        			}
        		}
        	]
        }
    """.trimIndent()

//    val word = Json.encodeToString(
//        Word(
//            questionWord = "Hello",
//            translate = "Привет",
//            correctAnswersCount = 0,
//        )
//    )
//    println(word)
//
//    val wordObject = Json.decodeFromString<Word>(
//        """{"questionWord":"Hello","translate":"Привет"}"""
//    )
//    println(wordObject)

    val response = json.decodeFromString<Response>(responseString)
    println(response)

}