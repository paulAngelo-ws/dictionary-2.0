package com.example.dictionary

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationCompatibilityTest {
    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun navigationCanRestoreEntriesAndReadWordArguments() {
        SwingUtilities.invokeAndWait {
            val owner = object : LifecycleOwner, ViewModelStoreOwner {
                override val lifecycle = LifecycleRegistry.createUnsafe(this)
                override val viewModelStore = ViewModelStore()
            }
            owner.lifecycle.currentState = Lifecycle.State.RESUMED
            var controller: NavHostController? = null
            val scene = ImageComposeScene(width = 320, height = 240) {
                CompositionLocalProvider(
                    LocalLifecycleOwner provides owner,
                    LocalViewModelStoreOwner provides owner
                ) {
                    val navController = rememberNavController()
                    controller = navController
                    NavHost(navController, startDestination = "input") {
                        composable("input") {}
                        composable("result/{word}") {}
                    }
                }
            }
            try {
                scene.render().close()
                val navController = requireNotNull(controller)
                navController.navigate("result/hello")
                scene.render().close()
                assertEquals("result/{word}", navController.currentDestination?.route)
                assertEquals("hello", navController.currentBackStackEntry?.arguments?.read { getString("word") })
                navController.popBackStack()
                scene.render().close()
                assertEquals("input", navController.currentDestination?.route)
            } finally {
                scene.close()
                owner.lifecycle.currentState = Lifecycle.State.DESTROYED
                owner.viewModelStore.clear()
            }
        }
    }
}
