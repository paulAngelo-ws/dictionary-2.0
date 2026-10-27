package com.example.dictionary.browser

import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

actual fun createBrowserDriver(): WebWorkerSQLiteDriver {
    return WebWorkerSQLiteDriver(Worker(js("""new URL("dictionary-sqlite-worker/worker.js", import.meta.url)""")))
}
