package com.uk98q.muslim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state = remember { AppState(applicationContext) }
            MuslimTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalTextScale provides textScaleFor(state.textSize)
                ) {
                    MuslimApp(state)
                }
            }
        }
    }
}

@Composable
fun MuslimApp(state: AppState) {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                state = state,
                onNavigate = { route -> nav.navigate(route) },
                onSearch = { nav.navigate("search") }
            )
        }
        composable(
            "category/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: "morning"
            CategoryScreen(state = state, category = Category.fromId(id), onBack = back)
        }
        composable("names") { NamesScreen(onBack = back) }
        composable("tasbih") { TasbihScreen(state = state, onBack = back) }
        composable("counter") { CounterScreen(state = state, onBack = back) }
        composable("settings") { SettingsScreen(state = state, onBack = back) }
        composable("search") { SearchScreen(onBack = back) }
    }
}
