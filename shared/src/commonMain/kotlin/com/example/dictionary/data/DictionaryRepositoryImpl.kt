package com.example.dictionary.data

import com.example.dictionary.domain.DictionaryEntry
import com.example.dictionary.domain.DictionaryResult
import com.example.dictionary.domain.DictionaryRepository
import com.example.dictionary.domain.HistoryItem
import com.example.dictionary.data.local.HistoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.CancellationException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

// Module 6: App supplies the client and API key through the constructor.
class DictionaryRepositoryImpl(
    private val dao: HistoryDao,
    private val client: HttpClient,
    private val apiKey: String
) : DictionaryRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchDefinitions(word: String, forceRefresh: Boolean): DictionaryResult? {
        val sanitizedInput = word.trim().lowercase()
        // Module 9: read the local database first, unless explicitly refreshing.
        if (!forceRefresh) {
            val cached = dao.findWord(sanitizedInput)
            if (cached != null) {
                val result = json.decodeFromString<DictionaryResult>(cached.resultJson)
                if (result.formalEntries.isNotEmpty()) return result.copy(isCached = true)
            }
        }
        var mwDidYouMean: String? = null

        val formalEntries = try {
            val httpResponse = client.get(
                "https://dictionaryapi.com/api/v3/references/collegiate/json/${sanitizedInput.encodeURLPathPart()}"
            ) {
                parameter("key", apiKey)
            }
            if (httpResponse.status != HttpStatusCode.OK) {
                error("Merriam-Webster is unavailable.")
            }
            // This API returns either spelling strings OR definition objects.
            // Only inspect the first item; serialization handles the definition fields.
            val response: JsonArray = httpResponse.body()
            if (response.firstOrNull() is JsonPrimitive) {
                mwDidYouMean = response.first().jsonPrimitive.content
                emptyList()
            } else {
                val definitions = json.decodeFromJsonElement<List<FormalDefinition>>(response)
                val entries = mutableListOf<DictionaryEntry>()
                for (item in definitions.take(3)) {
                    if (item.shortdef.isNotEmpty()) {
                        entries.add(DictionaryEntry(
                            word = sanitizedInput,
                            source = "Merriam-Webster",
                            partOfSpeech = item.fl,
                            definition = item.shortdef.first()
                        ))
                    }
                }
                entries
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }

        val result = DictionaryResult(formalEntries, mwDidYouMean)
        if (formalEntries.isNotEmpty()) {
            dao.saveWord(sanitizedInput, formalEntries.first().definition, json.encodeToString(result))
        }
        return result
    }

    // Modules 7 and 9: Room emits history again whenever saved data changes.
    override fun observeHistory(): Flow<List<HistoryItem>> {
        return dao.observeHistory().map { savedWords ->
            savedWords.mapNotNull { saved ->
                val result = json.decodeFromString<DictionaryResult>(saved.resultJson)
                if (result.formalEntries.isEmpty()) null
                else HistoryItem(saved.word, result.formalEntries.first().definition, result.formalEntries, saved.isFavorite)
            }
        }
    }

    override suspend fun toggleFavorite(word: String) {
        dao.toggleFavorite(word.trim().lowercase())
    }

}

@Serializable
private data class FormalDefinition(
    val fl: String = "",
    val shortdef: List<String> = emptyList()
)
