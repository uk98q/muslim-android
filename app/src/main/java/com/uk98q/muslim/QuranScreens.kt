package com.uk98q.muslim

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val QURAN_TAG = "MuslimQuran"

private sealed class TafsirLoad {
    object Loading : TafsirLoad()
    class Done(val text: String) : TafsirLoad()
    object Failed : TafsirLoad()
}

/** Streams one ayah at a time and reports when it ends. */
class AyahPlayer {
    private var media: MediaPlayer? = null
    private var prepared = false

    var isPlaying by mutableStateOf(false)
        private set
    var ayahEnded: (() -> Unit)? = null

    fun load(url: String, label: String, autoplay: Boolean) {
        release()
        Log.i(QURAN_TAG, "loading $label $url")
        val mp = MediaPlayer()
        media = mp
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            mp.setDataSource(url)
            mp.setOnPreparedListener {
                prepared = true
                if (autoplay) {
                    it.start()
                    isPlaying = true
                    Log.i(QURAN_TAG, "started $label")
                } else {
                    Log.i(QURAN_TAG, "prepared $label")
                }
            }
            mp.setOnCompletionListener {
                isPlaying = false
                ayahEnded?.invoke()
            }
            mp.setOnErrorListener { _, what, extra ->
                Log.w(QURAN_TAG, "error $label what=$what extra=$extra")
                isPlaying = false
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            Log.w(QURAN_TAG, "load failed $label: ${e.message}")
        }
    }

    fun toggle() {
        val mp = media ?: return
        if (!prepared) return
        try {
            if (mp.isPlaying) {
                mp.pause()
                isPlaying = false
            } else {
                mp.start()
                isPlaying = true
            }
        } catch (e: IllegalStateException) {
            Log.w(QURAN_TAG, "toggle failed: ${e.message}")
        }
    }

    fun release() {
        media?.let { m ->
            try {
                m.reset()
                m.release()
            } catch (_: Exception) {
            }
        }
        media = null
        prepared = false
        isPlaying = false
    }
}

@Composable
fun QuranIndexScreen(onBack: () -> Unit, onOpen: (Int) -> Unit) {
    val context = LocalContext.current
    val all = remember { QuranRepo.surahs(context) }
    var query by remember { mutableStateOf("") }
    val shown = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) all else all.filter { it.name.contains(q) || it.number.toString() == q }
    }

    AmbientBackground(tint = Mint) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeader("القرآن الكريم", onBack)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .glass(22.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontFamily = ReadexPro, fontSize = 16.sp),
                    cursorBrush = SolidColor(Mint),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Txt("ابحث باسم السورة أو رقمها", 16f, color = SecondaryText)
                        }
                        inner()
                    }
                )
            }
            Spacer(Modifier.height(14.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(shown, key = { it.number }) { surah ->
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        radius = 22.dp,
                        padding = 16.dp,
                        onClick = { onOpen(surah.number) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(CircleShape).background(Mint.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Txt(arabicDigits(surah.number), 15f, color = Mint, weight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Txt("سورة ${surah.name}", 17f, weight = FontWeight.Bold)
                                Spacer(Modifier.height(3.dp))
                                Txt("${arabicDigits(surah.ayahCount)} آية · ${surah.revelationLabel}", 13f, color = SecondaryText)
                            }
                        }
                    }
                }
                item {
                    Txt(
                        "نص القرآن: مشروع تنزيل Tanzil.net، رواية حفص عن عاصم، برخصة CC BY 3.0",
                        12f,
                        color = SecondaryText,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        align = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun QuranSurahScreen(state: AppState, surahNumber: Int, autoplay: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val surah = remember(surahNumber) { QuranRepo.surah(context, surahNumber) }
    val player = remember { AyahPlayer() }
    val listState = rememberLazyListState()
    val tafsir = remember { mutableStateMapOf<Int, TafsirLoad>() }
    val expanded = remember { mutableStateMapOf<Int, Boolean>() }
    val main = remember { Handler(Looper.getMainLooper()) }
    var index by remember { mutableIntStateOf(0) }
    var started by remember { mutableStateOf(false) }

    fun playAyah(i: Int, startNow: Boolean) {
        val s = surah ?: return
        index = i.coerceIn(0, s.ayahCount - 1)
        started = true
        player.load(state.reciter.url(s.number, index + 1), "${s.number}:${index + 1}", startNow)
    }

    fun requestTafsir(ayahNo: Int) {
        val s = surah ?: return
        tafsir[ayahNo] = TafsirLoad.Loading
        Thread {
            val result = try {
                TafsirLoad.Done(Tafsir.fetch(s.number, ayahNo))
            } catch (e: Exception) {
                Log.w(QURAN_TAG, "tafsir ${s.number}:$ayahNo failed: ${e.message}")
                TafsirLoad.Failed
            }
            main.post { tafsir[ayahNo] = result }
        }.start()
    }

    fun toggleTafsir(ayahNo: Int) {
        if (expanded[ayahNo] == true) {
            expanded[ayahNo] = false
            return
        }
        expanded[ayahNo] = true
        if (tafsir[ayahNo] !is TafsirLoad.Done) requestTafsir(ayahNo)
    }

    DisposableEffect(surahNumber) {
        player.ayahEnded = {
            val s = surah
            if (s != null && index + 1 < s.ayahCount) {
                playAyah(index + 1, true)
            } else {
                player.release()
                started = false
            }
        }
        onDispose { player.release() }
    }

    LaunchedEffect(surahNumber) {
        if (autoplay && surah != null) playAyah(0, true)
    }

    LaunchedEffect(index) {
        listState.animateScrollToItem(index)
    }

    AmbientBackground(tint = Dusk) {
        if (surah == null) {
            Txt("تعذّر تحميل السورة", 16f, modifier = Modifier.align(Alignment.Center))
            return@AmbientBackground
        }
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeader("سورة ${surah.name}", onBack)
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 130.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(surah.ayahCount) { i ->
                    val ayahNo = i + 1
                    val current = started && index == i
                    AyahCard(
                        number = ayahNo,
                        text = surah.ayahs[i],
                        current = current,
                        playing = current && player.isPlaying,
                        tafsirOn = state.tafsirOn,
                        expandedNow = expanded[ayahNo] == true,
                        tafsirState = tafsir[ayahNo],
                        onPlay = {
                            if (current) {
                                player.toggle()
                            } else {
                                playAyah(i, true)
                            }
                        },
                        onTafsir = { toggleTafsir(ayahNo) },
                        onRetry = { requestTafsir(ayahNo) }
                    )
                }
            }
        }

        GlassPanel(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            radius = 28.dp,
            padding = 14.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Txt("الآية ${arabicDigits(index + 1)} من ${arabicDigits(surah.ayahCount)}", 15f, weight = FontWeight.Bold, maxLines = 1)
                    Txt(state.reciter.arName, 12f, color = SecondaryText, maxLines = 1)
                }
                Txt(
                    "السابقة",
                    14f,
                    color = if (started && index > 0) Color.White else SecondaryText,
                    weight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable(enabled = started && index > 0) { playAyah(index - 1, true) }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                )
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Mint)
                        .clickable {
                            if (started) player.toggle() else playAyah(index, true)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (started && player.isPlaying) {
                        PauseGlyph(Color.Black)
                    } else {
                        Icon(Icons.Filled.PlayArrow, "تشغيل", tint = Color.Black, modifier = Modifier.size(26.dp))
                    }
                }
                Txt(
                    "التالية",
                    14f,
                    color = if (started && index + 1 < surah.ayahCount) Color.White else SecondaryText,
                    weight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable(enabled = started && index + 1 < surah.ayahCount) { playAyah(index + 1, true) }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun AyahCard(
    number: Int,
    text: String,
    current: Boolean,
    playing: Boolean,
    tafsirOn: Boolean,
    expandedNow: Boolean,
    tafsirState: TafsirLoad?,
    onPlay: () -> Unit,
    onTafsir: () -> Unit,
    onRetry: () -> Unit
) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        radius = 24.dp,
        padding = 18.dp,
        tint = if (current) Mint else null
    ) {
        Txt(text, 24f, modifier = Modifier.fillMaxWidth(), align = TextAlign.Start)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Mint.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Txt(arabicDigits(number), 13f, color = Mint, weight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Mint)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                if (playing) {
                    PauseGlyph(Color.Black)
                } else {
                    Icon(Icons.Filled.PlayArrow, "تشغيل الآية $number", tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }
            if (tafsirOn) {
                Spacer(Modifier.width(12.dp))
                Txt(
                    if (expandedNow) "إخفاء التفسير" else "التفسير",
                    13f,
                    color = Dusk,
                    weight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onTafsir).padding(6.dp)
                )
            }
        }
        if (tafsirOn && expandedNow) {
            Spacer(Modifier.height(12.dp))
            when (tafsirState) {
                is TafsirLoad.Done -> {
                    Txt(tafsirState.text, 15f, color = Color.White.copy(alpha = 0.88f))
                    Spacer(Modifier.height(8.dp))
                    Txt(Tafsir.ATTRIBUTION, 11f, color = SecondaryText)
                }
                TafsirLoad.Failed -> {
                    Txt("تعذّر تحميل التفسير الآن.", 14f, color = SecondaryText)
                    Spacer(Modifier.height(6.dp))
                    Txt(
                        "إعادة المحاولة",
                        14f,
                        color = Mint,
                        weight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onRetry).padding(4.dp)
                    )
                }
                else -> CircularProgressIndicator(color = Mint, modifier = Modifier.size(22.dp))
            }
        }
    }
}

/** Two drawn bars, so the pause symbol does not depend on an extended icon set. */
@Composable
private fun PauseGlyph(tint: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(width = 5.dp, height = 16.dp).background(tint, RoundedCornerShape(2.dp)))
        Box(modifier = Modifier.size(width = 5.dp, height = 16.dp).background(tint, RoundedCornerShape(2.dp)))
    }
}
    Log.i(QURAN_TAG, "reader n=$surahNumber autoplay=$autoplay found=${surah != null}")
