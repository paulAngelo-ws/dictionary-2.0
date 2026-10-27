package com.example.dictionary.browser

import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

actual fun createBrowserDriver(): WebWorkerSQLiteDriver = WebWorkerSQLiteDriver(createWorker())

@OptIn(ExperimentalWasmJsInterop::class)
private fun createWorker(): Worker =
    js("""new Worker(new URL("dictionary-sqlite-worker/worker.js", import.meta.url))""")
