package com.example.dictionary

import android.os.Bundle
import com.example.dictionary.data.createAppStorage
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val storage = createAppStorage(applicationContext)
        setContent {
            App(storage)
        }
    }
}
