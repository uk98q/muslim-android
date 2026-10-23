package com.uk98q.muslim

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Amiri Quran (SIL OFL 1.1) for the mushaf text. */
val AmiriQuran: FontFamily = FontFamily(Font(R.font.amiri_quran))

/** Full mushaf reader: 604 pages, 15 lines per page, streamed audio and optional tafsir. */
@Composable
fun MushafScreen(
    state: AppState,
    startPage: Int,
    autoplay: Boolean,
    onAutoplayUsed: () -> Unit,
    onBack: () -> Unit,
    initialPanelOpen: Boolean = true
) {
    val context = LocalContext.current
    val pages = remember { MushafRepo.pages(context) }
    val surahs = remember { QuranRepo.surahs(context) }
    val allAyahs = remember { surahs.flatMap { s -> (1..s.ayahCount).map { s.number to it } } }
    val basmala = remember { surahs.firstOrNull()?.ayahs?.firstOrNull() ?: "" }
    val pagerState = rememberPagerState(
        initialPage = (startPage - 1).coerceIn(0, pages.lastIndex),
        pageCount = { pages.size }
    )
    val player = remember { AyahPlayer() }
    DisposableEffect(Unit) { onDispose { player.release() } }

    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var loaded by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var tafsir by remember { mutableStateOf<String?>(null) }
    var panelOpen by remember { mutableStateOf(initialPanelOpen) }

    fun playAyah(s: Int, a: Int, startNow: Boolean) {
        selected = s to a
        loaded = s to a
        player.ayahEnded = {
            val idx = allAyahs.indexOf(s to a)
            if (idx >= 0 && idx + 1 < allAyahs.size) {
                val next = allAyahs[idx + 1]
                playAyah(next.first, next.second, true)
            }
        }
        player.load(state.reciter.url(s, a), "$s:$a", startNow)
    }

    fun stepAyah(delta: Int) {
        val cur = selected ?: return
        val idx = allAyahs.indexOf(cur) + delta
        if (idx in allAyahs.indices) {
            playAyah(allAyahs[idx].first, allAyahs[idx].second, true)
        }
    }

    fun togglePlay() {
        val cur = selected ?: return
        if (loaded != cur) playAyah(cur.first, cur.second, true) else player.toggle()
    }

    LaunchedEffect(Unit) {
        if (autoplay) {
            val first = pages[pagerState.currentPage].lines.firstOrNull { it.kind == 't' }?.items?.firstOrNull()
            if (first != null) playAyah(first.surah, first.ayah, true)
            onAutoplayUsed()
        }
    }

    // Follow the audio: turn the page when the playing ayah is on another page.
    LaunchedEffect(loaded) {
        val cur = loaded ?: return@LaunchedEffect
        val target = MushafRepo.pageOf(context, cur.first, cur.second) - 1
        if (target in pages.indices && target != pagerState.currentPage) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(selected, state.tafsirOn) {
        tafsir = null
        val sel = selected ?: return@LaunchedEffect
        if (!state.tafsirOn) return@LaunchedEffect
        tafsir = withContext(Dispatchers.IO) {
            try {
                Tafsir.fetch(sel.first, sel.second)
            } catch (e: Exception) {
                "تعذّر تحميل التفسير الآن. تحقق من الاتصال ثم أعد المحاولة."
            }
        }
    }

    fun surahName(n: Int): String = surahs.getOrNull(n - 1)?.name ?: ""

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleGlassButton(Icons.Filled.ArrowForward, Mint, "رجوع", onBack)
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { index ->
                val page = pages[index]
                MushafPageView(
                    page = page,
                    juzName = MushafRepo.JUZ_NAMES[page.juz - 1],
                    nameOf = ::surahName,
                    basmala = basmala,
                    selected = selected,
                    onTap = { s, a -> selected = s to a }
                )
            }

        }

        if (selected != null && state.tafsirOn) {
            TafsirPanel(text = tafsir, open = panelOpen, onToggle = { panelOpen = !panelOpen })
        }

        // Reserved bar area: the page size stays the same whether or not audio is selected.
        Box(Modifier.fillMaxWidth().height(84.dp).padding(horizontal = 12.dp, vertical = 8.dp)) {
            val sel = selected
            if (sel != null) {
                Row(
                    Modifier.fillMaxSize().glass(radius = 22.dp).padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Txt(state.reciter.arName, 12f, color = Mint)
                        Txt("سورة ${surahName(sel.first)} · الآية ${arabicDigits(sel.second)}", 12f, color = SecondaryText)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircleGlassButton(Icons.Filled.KeyboardArrowRight, Mint, "الآية السابقة") { stepAyah(-1) }
                        Spacer(Modifier.width(10.dp))
                        PlayPauseButton(playing = player.isPlaying) { togglePlay() }
                        Spacer(Modifier.width(10.dp))
                        CircleGlassButton(Icons.Filled.KeyboardArrowLeft, Mint, "الآية التالية") { stepAyah(1) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MushafPageView(
    page: MPage,
    juzName: String,
    nameOf: (Int) -> String,
    basmala: String,
    selected: Pair<Int, Int>?,
    onTap: (Int, Int) -> Unit
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 4.dp)) {
        // One font size per page: the widest line fills the width, so every line is justified.
        val fontSp = remember(page.number, maxWidth, maxHeight) {
            var widest = 1f
            page.lines.forEach { line ->
                if (line.kind == 't') {
                    val text = line.items.joinToString(" ") { it.text }
                    val ends = line.items.count { it.end }
                    val px = measurer.measure(text, TextStyle(fontFamily = AmiriQuran, fontSize = 20.sp)).size.width.toFloat()
                    val medallions = ends * 1.25f * 20f * density.density
                    widest = maxOf(widest, px + medallions)
                }
            }
            val widestDp = widest / density.density
            val byWidth = 0.9f * 20f * maxWidth.value / widestDp
            val byHeight = (maxHeight.value - 36f) / 15f / 1.6f
            minOf(byWidth, byHeight).coerceIn(12f, 36f)
        }
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().height(26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Txt(juzName, 12f, color = SecondaryText)
                Txt(nameOf(page.lines.firstOrNull { it.kind == 't' }?.items?.firstOrNull()?.surah ?: 1), 13f, weight = FontWeight.Medium)
            }
            page.lines.forEach { line ->
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    when (line.kind) {
                        't' -> MushafTextLine(line, fontSp, selected, onTap)
                        's' -> Box(Modifier.glass(radius = 14.dp).padding(horizontal = 18.dp, vertical = 2.dp)) {
                            Txt("سورة ${nameOf(line.surah)}", fontSp * 0.7f, color = Mint, weight = FontWeight.Medium)
                        }
                        'b' -> Text(
                            text = basmala,
                            color = Color.White,
                            fontFamily = AmiriQuran,
                            fontSize = (fontSp * 0.9f).sp,
                            overflow = TextOverflow.Visible,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                        else -> Spacer(Modifier.height(1.dp))
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().height(26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Txt("${arabicDigits(page.quarter)}/٤ الحزب ${arabicDigits(page.hizb)}", 12f, color = SecondaryText)
                Txt(arabicDigits(page.number), 12f, color = SecondaryText)
            }
        }
    }
}

/** One word, plus the ayah medallion when the word ends its verse. */
private data class MUnit(val word: String, val surah: Int, val ayah: Int, val end: Boolean)

private fun buildUnits(line: MLine): List<MUnit> = line.items.flatMap { item ->
    val words = item.text.split(' ').filter { it.isNotEmpty() }
    words.mapIndexed { i, w -> MUnit(w, item.surah, item.ayah, item.end && i == words.lastIndex) }
}

/**
 * One justified mushaf line. Gaps are placed only between words, all equal on the line,
 * so the medallions do not stretch the spacing.
 */
@Composable
private fun MushafTextLine(line: MLine, fontSp: Float, selected: Pair<Int, Int>?, onTap: (Int, Int) -> Unit) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val units = remember(line) { buildUnits(line) }
    val style = TextStyle(fontFamily = AmiriQuran, fontSize = fontSp.sp)
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gapDp = remember(units, fontSp, maxWidth) {
            val natural = units.sumOf { u ->
                val px = measurer.measure(u.word, style).size.width.toDouble()
                (px / density.density + if (u.end) fontSp * 1.25f + 4f else 0f).toDouble()
            }
            if (units.size > 1) ((maxWidth.value - natural) / (units.size - 1) - 0.5).coerceAtLeast(0.0).toFloat() else 0f
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gapDp.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            units.forEach { u ->
                val isSelected = selected != null && selected.first == u.surah && selected.second == u.ayah
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = u.word,
                        color = if (isSelected) Mint else Color.White,
                        fontFamily = AmiriQuran,
                        fontSize = fontSp.sp,
                        overflow = TextOverflow.Visible,
                        softWrap = false,
                        modifier = Modifier.clickable { onTap(u.surah, u.ayah) }
                    )
                    if (u.end) {
                        Spacer(Modifier.width(4.dp))
                        AyahMedallion(arabicDigits(u.ayah), fontSp)
                    }
                }
            }
        }
    }
}

/** Tafsir panel: sits in the page column, so it pushes the lines up instead of covering them. */
@Composable
private fun TafsirPanel(text: String?, open: Boolean, onToggle: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .glass(radius = 20.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onToggle),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Txt("التفسير · ابن كثير", 13f, color = Mint, weight = FontWeight.Medium)
            Txt(if (open) "إخفاء" else "إظهار", 13f, color = SecondaryText)
        }
        if (open) {
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth().heightIn(max = 170.dp).verticalScroll(rememberScrollState())) {
                Txt(text ?: "جارٍ تحميل التفسير…", 15f, color = Color.White.copy(alpha = 0.9f))
            }
            Spacer(Modifier.height(6.dp))
            Txt(Tafsir.ATTRIBUTION, 11f, color = SecondaryText)
        }
    }
}

@Composable
private fun AyahMedallion(number: String, fontSp: Float) {
    Box(
        Modifier
            .size((fontSp * 1.25f).dp)
            .border(0.9.dp, Color.White.copy(alpha = 0.55f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            color = Color.White.copy(alpha = 0.85f),
            fontFamily = ReadexPro,
            fontSize = (fontSp * 0.42f).sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun PlayPauseButton(playing: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(56.dp)
            .glass(radius = 28.dp, tint = Mint.copy(alpha = 0.22f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(22.dp)) {
            if (playing) {
                val bar = size.width * 0.3f
                drawRect(Color.White, Offset(0f, 0f), Size(bar, size.height))
                drawRect(Color.White, Offset(size.width - bar, 0f), Size(bar, size.height))
            } else {
                val path = Path().apply {
                    moveTo(size.width * 0.15f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(size.width * 0.15f, size.height)
                    close()
                }
                drawPath(path, Color.White)
            }
        }
    }
}
