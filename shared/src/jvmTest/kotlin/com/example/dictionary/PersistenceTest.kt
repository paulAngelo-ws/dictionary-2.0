package com.example.dictionary

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.dictionary.data.DictionaryPreferences
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
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
        urban: String = """{"list":[{"word":"hello","definition":"a [greeting]","example":"hello there"}]}""",
        status: HttpStatusCode = HttpStatusCode.OK,
        onRequest: () -> Unit = {}
    ): HttpClient {
        return HttpClient(MockEngine { request ->
            onRequest()
            val body = if (request.url.host == "api.urbandictionary.com") urban else formal
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
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
            assertEquals("a greeting", result.urbanEntries.first().definition)
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
                formal = """["hello"]""", urban = """{"list":[]}"""
            ), "test-key")
            assertEquals("hello", assertNotNull(spelling.fetchDefinitions("helo")).didYouMeanSuggestion)
            assertTrue(spelling.observeHistory().first().isEmpty())
            val broken = DictionaryRepositoryImpl(db.historyDao(), client(
                formal = "invalid JSON", urban = "invalid JSON"
            ), "test-key")
            assertNull(broken.fetchDefinitions("hello"))
            assertTrue(broken.observeHistory().first().isEmpty())
        }
    }

    @Test
    fun preferenceSurvivesRecreatingDataStore() = runBlocking {
        withTimeout(20000) {
            val file = File(directory, "test.preferences_pb")
            val firstJob = SupervisorJob()
            val firstStore = DataStoreFactory.create(
                storage = FileStorage(PreferencesFileSerializer) { file },
                scope = CoroutineScope(firstJob + Dispatchers.IO)
            )
            val firstPreferences = DictionaryPreferences(firstStore)
            try {
                assertFalse(firstPreferences.observeShowSlang().first())
                firstPreferences.saveShowSlang(true)
            } finally {
                firstJob.cancelAndJoin()
            }
            val secondJob = SupervisorJob()
            val secondStore = DataStoreFactory.create(
                storage = FileStorage(PreferencesFileSerializer) { file },
                scope = CoroutineScope(secondJob + Dispatchers.IO)
            )
            try {
                assertTrue(DictionaryPreferences(secondStore).observeShowSlang().first())
            } finally {
                secondJob.cancelAndJoin()
            }
        }
    }
}
