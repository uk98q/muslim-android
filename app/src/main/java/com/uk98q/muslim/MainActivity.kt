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
import androidx.compose.ui.platform.LocalContext
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
        val startPage = intent.getIntExtra("page", 0)
        val startAutoplay = intent.getBooleanExtra("autoplay", false)
        val startPanelOpen = intent.getBooleanExtra("tafsirOpen", true)
        setContent {
            val state = remember {
                AppState(applicationContext).also { s ->
                    val r = intent.getIntExtra("reciter", -1)
                    if (r >= 0) s.applyReciter(r)
                    if (intent.hasExtra("tafsir")) s.applyTafsir(intent.getBooleanExtra("tafsir", false))
                }
            }
            MuslimTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalTextScale provides textScaleFor(state.textSize)
                ) {
                    MuslimApp(state, startPage, startAutoplay, startPanelOpen)
                }
            }
        }
    }
}

@Composable
fun MuslimApp(state: AppState, startPage: Int = 0, startAutoplay: Boolean = false, startPanelOpen: Boolean = true) {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }
    val context = LocalContext.current
    val autoplayUsed = rememberSaveable { mutableStateOf(false) }
    // True once the requested surah has been opened, so returning home does not reopen it.
    val opened = rememberSaveable { mutableStateOf(false) }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            // The graph is ready here, so this navigation is never dropped.
            LaunchedEffect(Unit) {
                if (!opened.value && startPage in 1..604) {
                    opened.value = true
                    Log.i("MuslimQuran", "opening page $startPage autoplay=$startAutoplay")
                    nav.navigate("mushaf/$startPage")
                }
            }
            HomeScreen(
                state = state,
                onNavigate = { route -> nav.navigate(route) },
                onSearch = { nav.navigate("search") }
            )
        }
        composable("quran") {
            QuranIndexScreen(onBack = back, onOpen = { n -> nav.navigate("mushaf/${MushafRepo.startPage(context, n)}") })
        }
        composable(
            "mushaf/{page}",
            arguments = listOf(navArgument("page") { type = NavType.IntType })
        ) { entry ->
            val page = entry.arguments?.getInt("page") ?: 1
            MushafScreen(
                state = state,
                startPage = page,
                autoplay = startAutoplay && page == startPage && !autoplayUsed.value,
                onAutoplayUsed = { autoplayUsed.value = true },
                onBack = back,
                initialPanelOpen = startPanelOpen
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
