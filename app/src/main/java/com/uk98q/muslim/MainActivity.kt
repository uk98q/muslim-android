package com.uk98q.muslim

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
        val startSurah = intent.getIntExtra("surah", 0)
        val startAutoplay = intent.getBooleanExtra("autoplay", false)
        setContent {
            val state = remember { AppState(applicationContext) }
            MuslimTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalTextScale provides textScaleFor(state.textSize)
                ) {
                    MuslimApp(state, startSurah, startAutoplay)
                }
            }
        }
    }
}

@Composable
fun MuslimApp(state: AppState, startSurah: Int = 0, startAutoplay: Boolean = false) {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }
    // True once the requested surah has been opened, so returning home does not reopen it.
    val opened = rememberSaveable { mutableStateOf(false) }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            // The graph is ready here, so this navigation is never dropped.
            LaunchedEffect(Unit) {
                if (!opened.value && startSurah in 1..114) {
                    opened.value = true
                    Log.i("MuslimQuran", "opening surah $startSurah autoplay=$startAutoplay")
                    nav.navigate("quran/$startSurah")
                }
            }
            HomeScreen(
                state = state,
                onNavigate = { route -> nav.navigate(route) },
                onSearch = { nav.navigate("search") }
            )
        }
        composable("quran") {
            QuranIndexScreen(onBack = back, onOpen = { n -> nav.navigate("quran/$n") })
        }
        composable(
            "quran/{n}",
            arguments = listOf(navArgument("n") { type = NavType.IntType })
        ) { entry ->
            val n = entry.arguments?.getInt("n") ?: 1
            QuranSurahScreen(
                state = state,
                surahNumber = n,
                autoplay = startAutoplay && n == startSurah,
                onBack = back
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
