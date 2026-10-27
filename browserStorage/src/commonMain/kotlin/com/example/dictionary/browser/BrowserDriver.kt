package com.example.dictionary.browser

import androidx.sqlite.driver.web.WebWorkerSQLiteDriver

// Browser-only infrastructure. App/domain/presentation code does not depend on JS APIs.
expect fun createBrowserDriver(): WebWorkerSQLiteDriver
