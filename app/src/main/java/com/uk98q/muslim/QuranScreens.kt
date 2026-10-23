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
