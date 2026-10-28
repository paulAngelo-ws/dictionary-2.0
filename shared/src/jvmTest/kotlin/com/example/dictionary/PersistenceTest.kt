package com.example.dictionary

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.dictionary.data.DictionaryRepositoryImpl
import com.example.dictionary.data.configureDictionaryClient
import com.example.dictionary.data.local.DictionaryDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PersistenceTest {
    private val directory = Files.createTempDirectory("dictionary-test-").toFile()
    private var database: DictionaryDatabase? = null
    private val clients = mutableListOf<HttpClient>()

    private fun openDatabase(): DictionaryDatabase {
        return Room.databaseBuilder<DictionaryDatabase>(
            name = File(directory, "test.db").absolutePath
        ).setDriver(BundledSQLiteDriver()).build().also { database = it }
    }

    private fun client(
        formal: String = """[{"fl":"noun","shortdef":["a greeting"],"extra":"ignored"}]""",
        status: HttpStatusCode = HttpStatusCode.OK,
        onRequest: () -> Unit = {}
    ): HttpClient {
        return HttpClient(MockEngine { request ->
            assertEquals("dictionaryapi.com", request.url.host)
            onRequest()
            respond(formal, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }) { configureDictionaryClient() }.also { clients.add(it) }
    }

    @AfterTest
    fun cleanUp() {
        clients.forEach { it.close() }
        database?.close()
        directory.deleteRecursively()
    }

    @Test
    fun definitionsAndFavoritesSurviveReopeningWithoutNetwork() = runBlocking {
        withTimeout(20000) {
            val db = openDatabase()
            val repository = DictionaryRepositoryImpl(db.historyDao(), client(), "test-key")
            val result = assertNotNull(repository.fetchDefinitions(" Hello "))
            assertEquals("a greeting", result.formalEntries.first().definition)
            repository.toggleFavorite("hello")
            db.close()

            val reopened = openDatabase()
            var requests = 0
            val offline = client(status = HttpStatusCode.ServiceUnavailable) { requests++ }
            val restored = DictionaryRepositoryImpl(reopened.historyDao(), offline, "test-key")
            assertTrue(assertNotNull(restored.fetchDefinitions("hello")).isCached)
            assertEquals(0, requests)
            val history = restored.observeHistory().first()
            assertEquals(1, history.size)
            assertTrue(history.first().isFavorite)
            assertEquals("hello", history.first().word)
        }
    }

    @Test
    fun olderSavedResultsKeepFormalDefinitionsAndHideRemovedContent() = runBlocking {
        withTimeout(20000) {
            val db = openDatabase()
            val oldEntries = "\"urbanEntries\":[{\"word\":\"hello\",\"source\":\"Urban Dictionary\",\"definition\":\"old slang\"}]"
            db.historyDao().saveWord("hello", "old slang", """{"formalEntries":[{"word":"hello","source":"Merriam-Webster","definition":"a greeting"}],$oldEntries}""")
            db.historyDao().saveWord("removed", "old slang", """{"formalEntries":[],$oldEntries}""")
            val repository = DictionaryRepositoryImpl(db.historyDao(), client(), "test-key")
            val result = assertNotNull(repository.fetchDefinitions("hello"))
            assertTrue(result.isCached)
            assertEquals("a greeting", result.formalEntries.first().definition)
            val history = repository.observeHistory().first()
            assertEquals(listOf("hello"), history.map { it.word })
            assertEquals("a greeting", history.first().snippet)
        }
    }

    @Test
    fun failedRefreshKeepsTheSavedWordAndFavorite() = runBlocking {
        withTimeout(20000) {
            val db = openDatabase()
            val original = DictionaryRepositoryImpl(db.historyDao(), client(), "test-key")
            original.fetchDefinitions("hello")
            original.toggleFavorite("hello")
            val offline = DictionaryRepositoryImpl(
                db.historyDao(), client(status = HttpStatusCode.ServiceUnavailable), "test-key"
            )
            assertNull(offline.fetchDefinitions("hello", forceRefresh = true))
            assertEquals("a greeting", assertNotNull(offline.fetchDefinitions("hello")).formalEntries.first().definition)
            assertTrue(offline.observeHistory().first().first().isFavorite)
        }
    }

    @Test
    fun successfulRefreshUpdatesDefinitionsWithoutClearingFavorites() = runBlocking {
        withTimeout(20000) {
            val db = openDatabase()
            val original = DictionaryRepositoryImpl(db.historyDao(), client(), "test-key")
            original.fetchDefinitions("hello")
            original.toggleFavorite("hello")
            val updated = DictionaryRepositoryImpl(db.historyDao(), client(
                formal = """[{"fl":"noun","shortdef":["updated greeting"]}]"""
            ), "test-key")
            val result = assertNotNull(updated.fetchDefinitions("hello", forceRefresh = true))
            assertFalse(result.isCached)
            assertEquals("updated greeting", result.formalEntries.first().definition)
            assertTrue(updated.observeHistory().first().first().isFavorite)
        }
    }

    @Test
    fun spellingOnlyAndMalformedResponsesAreNotSavedAsDefinitions() = runBlocking {
        withTimeout(20000) {
            val db = openDatabase()
            val spelling = DictionaryRepositoryImpl(db.historyDao(), client(
                formal = """["hello"]"""
            ), "test-key")
            assertEquals("hello", assertNotNull(spelling.fetchDefinitions("helo")).didYouMeanSuggestion)
            assertTrue(spelling.observeHistory().first().isEmpty())
            val broken = DictionaryRepositoryImpl(db.historyDao(), client(
                formal = "invalid JSON"
            ), "test-key")
            assertNull(broken.fetchDefinitions("hello"))
            assertTrue(broken.observeHistory().first().isEmpty())
        }
    }

}
