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

interface DictionaryRepository {
    suspend fun fetchDefinitions(word: String): Pair<List<DictionaryEntry>, List<DictionaryEntry>>
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

    override suspend fun fetchDefinitions(word: String): Pair<List<DictionaryEntry>, List<DictionaryEntry>> {
        val sanitized = word.trim().lowercase()

        // 1. Fetch from Urban Dictionary
        val urbanEntries = try {
            val urbanUrl = "https://api.urbandictionary.com/v0/define?term=$sanitized"
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

        // 2. Fetch from Merriam-Webster (Replace MW_API_KEY with your key)
        val mwApiKey = "2233bca2-414f-4402-b28c-69f7aef9d02e"
        val formalEntries = try {
            val mwUrl = "https://dictionaryapi.com/api/v3/references/collegiate/json/$sanitized?key=$mwApiKey"
            val responseText: String = client.get(mwUrl).body()
            val jsonArray = Json.parseToJsonElement(responseText).jsonArray

            jsonArray.mapNotNull { element ->
                if (element is JsonObject && element.containsKey("shortdef")) {
                    val fl = element["fl"]?.jsonPrimitive?.contentOrNull ?: "definition"
                    val defs = element["shortdef"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                    defs.firstOrNull()?.let { def ->
                        DictionaryEntry(
                            word = sanitized,
                            source = "Merriam-Webster",
                            partOfSpeech = fl,
                            definition = def
                        )
                    }
                } else null
            }.take(3)
        } catch (e: Exception) {
            emptyList()
        }

        return Pair(formalEntries, urbanEntries)
    }
}