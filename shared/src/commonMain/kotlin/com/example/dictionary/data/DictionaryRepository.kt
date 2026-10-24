package com.example.dictionary.data

import com.example.dictionary.domain.DictionaryEntry
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
private data class UrbanResponse(val list: List<UrbanDefinition>)

@Serializable
private data class UrbanDefinition(
    val definition: String,
    val word: String,
    val example: String = ""
)

// Updated to include the suggested correction string
interface DictionaryRepository {
    suspend fun fetchDefinitions(word: String): Triple<List<DictionaryEntry>, List<DictionaryEntry>, String?>
}

class DictionaryRepositoryImpl : DictionaryRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 10000
        }
        defaultRequest {
            header(HttpHeaders.UserAgent, "Mozilla/5.0")
        }
    }

    override suspend fun fetchDefinitions(word: String): Triple<List<DictionaryEntry>, List<DictionaryEntry>, String?> {
        val sanitizedInput = word.trim().lowercase()
        val encodedQuery = sanitizedInput.replace(" ", "%20")

        val urbanEntries = try {
            val urbanUrl = "https://api.urbandictionary.com/v0/define?term=$encodedQuery"
            val response: UrbanResponse = client.get(urbanUrl).body()
            response.list.take(3).map {
                DictionaryEntry(
                    word = it.word,
                    source = "Urban Dictionary",
                    definition = it.definition.replace("[", "").replace("]", ""),
                    example = it.example.replace("[", "").replace("]", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }

        val mwApiKey = "2233bca2-414f-4402-b28c-69f7aef9d02e"
        var mwDidYouMean: String? = null

        val formalEntries = try {
            val mwUrl = "https://dictionaryapi.com/api/v3/references/collegiate/json/$sanitizedInput?key=$mwApiKey"
            val responseText: String = client.get(mwUrl).body()
            val jsonArray = Json.parseToJsonElement(responseText).jsonArray

            // Merriam-Webster returns an array of strings (e.g. ["receive"]) if misspelled
            if (jsonArray.isNotEmpty() && jsonArray[0] is JsonPrimitive) {
                mwDidYouMean = jsonArray.firstOrNull()?.jsonPrimitive?.contentOrNull
                emptyList()
            } else {
                jsonArray.mapNotNull { element ->
                    if (element is JsonObject && element.containsKey("shortdef")) {
                        val fl = element["fl"]?.jsonPrimitive?.contentOrNull ?: "definition"
                        val defs = element["shortdef"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                        defs.firstOrNull()?.let { def ->
                            DictionaryEntry(
                                word = sanitizedInput,
                                source = "Merriam-Webster",
                                partOfSpeech = fl,
                                definition = def
                            )
                        }
                    } else null
                }.take(3)
            }
        } catch (e: Exception) {
            emptyList()
        }

        return Triple(formalEntries, urbanEntries, mwDidYouMean)
    }
}