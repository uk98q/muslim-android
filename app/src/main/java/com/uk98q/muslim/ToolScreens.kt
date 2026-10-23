package com.uk98q.muslim

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box as LayoutBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NamesScreen(onBack: () -> Unit) {
    AmbientBackground(tint = Mint) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeader("أسماء الله الحسنى", onBack)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(AllahNames.all) { index, name ->
                    GlassPanel(modifier = Modifier.fillMaxWidth().height(96.dp), radius = 22.dp, padding = 0.dp) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Txt("${index + 1}", 13f, color = SecondaryText)
                            Txt(name, 22f, weight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TasbihScreen(state: AppState, onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val phrase = TASBIH_PHRASES[state.tasbihPhrase.coerceIn(0, TASBIH_PHRASES.lastIndex)]
    val target = state.tasbihTarget

    AmbientBackground(tint = Mint) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeader("المسبحة", onBack)
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TASBIH_PHRASES.forEachIndexed { index, text ->
                        val selected = index == state.tasbihPhrase
                        GlassChip(text, selected, Mint) {
                            if (!selected) {
                                state.setTasbihPhrase(index)
                                state.setTasbih(0, state.tasbihTotal)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TASBIH_TARGETS.forEach { t ->
                        GlassChip(
                            if (t == 0) "بدون هدف" else "$t",
                            t == target,
                            Dawn,
                            Modifier.weight(1f)
                        ) {
                            state.setTasbihTarget(t)
                            state.setTasbih(0, state.tasbihTotal)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .glass(120.dp)
                        .clickable {
                            var c = state.tasbihCount
                            if (target > 0 && c >= target) c = 0
                            c += 1
                            if (state.haptics) {
                                val reached = target > 0 && c == target
                                haptic.performHapticFeedback(
                                    if (reached) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                                )
                            }
                            state.setTasbih(c, state.tasbihTotal + 1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Txt("${state.tasbihCount}", 64f, weight = FontWeight.Bold)
                        Txt(phrase, 17f, color = Mint, weight = FontWeight.Medium)
                        Txt(if (target == 0) "بدون هدف" else "من $target", 14f, color = SecondaryText)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .glass(22.dp)
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Txt("الإجمالي: ${state.tasbihTotal}", 15f, weight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .glass(22.dp)
                            .clickable { state.setTasbih(0, state.tasbihTotal) }
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Txt("تصفير العداد", 15f, color = Mint, weight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun CounterScreen(state: AppState, onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    fun tick() {
        if (state.haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    AmbientBackground(tint = Dusk) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeader("العداد", onBack)
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                BasicTextField(
                    value = state.counterLabel,
                    onValueChange = { state.setCounterLabel(it) },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = (18f * LocalTextScale.current).sp,
                        fontFamily = ReadexPro,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(Mint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .glass(22.dp)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                )
                Txt("${state.counterValue}", 80f, weight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircleGlassButton(Icons.Filled.Remove, SecondaryText, "إنقاص") {
                        if (state.counterValue > 0) state.setCounterValue(state.counterValue - 1)
                        tick()
                    }
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(Dusk)
                            .clickable {
                                state.setCounterValue(state.counterValue + 1)
                                tick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, "زيادة", tint = Color.Black, modifier = Modifier.size(34.dp))
                    }
                    CircleGlassButton(Icons.Filled.Refresh, Dawn, "تصفير") { state.setCounterValue(0) }
                }
                Txt("اضغط الزائد للعد، والناقص للتراجع", 14f, color = SecondaryText)
            }
        }
    }
}

@Composable
fun SettingsScreen(state: AppState, onBack: () -> Unit) {
    val context = LocalContext.current
    var confirmReset by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) LocationHelper.fetch(context, state)
    }

    AmbientBackground(tint = Dusk) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeader("الإعدادات", onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SettingsSection("حجم الخط") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("صغير", "متوسط", "كبير", "أكبر").forEachIndexed { index, label ->
                            GlassChip(label, state.textSize == index, Mint, Modifier.weight(1f)) {
                                state.setTextSize(index)
                            }
                        }
                    }
                }

                SettingsSection("التفاعل") {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Txt("الاهتزاز عند العد", 16f, modifier = Modifier.weight(1f))
                        Switch(
                            checked = state.haptics,
                            onCheckedChange = { state.setHaptics(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Mint,
                                checkedThumbColor = Color.Black
                            )
                        )
                    }
                }

                SettingsSection("الموقع وأوقات الصلاة") {
                    Txt(
                        if (state.latitude != null) {
                            "الأوقات محسوبة على موقعك الحالي، ولا يُرسل الموقع إلى أي خادم."
                        } else {
                            "لم يُسمح بالموقع بعد، لذلك الأوقات محسوبة لمكة المكرمة."
                        },
                        14f,
                        color = SecondaryText
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .glass(18.dp)
                            .clickable {
                                if (LocationHelper.hasPermission(context)) {
                                    LocationHelper.fetch(context, state)
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                                    )
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Txt("تحديث الموقع", 15f, color = Mint, weight = FontWeight.Medium)
                    }
                }

                SettingsSection("البيانات") {
                    Box(
                        modifier = Modifier
                            .glass(18.dp)
                            .clickable { confirmReset = true }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Txt("تصفير تقدم اليوم", 15f, color = Dawn, weight = FontWeight.Medium)
                    }
                }

                SettingsSection("عن التطبيق") {
                    Txt("مسلم: الإصدار 1.3", 16f, weight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Txt("كل المزايا مجانية، ولا توجد اشتراكات أو مشتريات داخل التطبيق.", 14f, color = SecondaryText)
                    Spacer(Modifier.height(8.dp))
                    Txt("الخط: Readex Pro بترخيص SIL Open Font License 1.1.", 13f, color = SecondaryText)
                }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = Color(0xFF111111),
            title = { Txt("تصفير تقدم اليوم؟", 18f, weight = FontWeight.Bold) },
            confirmButton = {
                TextButton(onClick = {
                    state.resetToday()
                    confirmReset = false
                }) {
                    Txt("تصفير", 15f, color = Dawn)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Txt("إلغاء", 15f)
                }
            }
        )
    }
}

private data class SearchResult(val id: String, val text: String, val source: String)

private fun buildResults(query: String): List<SearchResult> {
    if (query.isEmpty()) return emptyList()
    val out = mutableListOf<SearchResult>()
    for (category in Category.entries) {
        for (dhikr in category.items) {
            if (dhikr.text.contains(query)) out.add(SearchResult(category.id + dhikr.id, dhikr.text, category.title))
        }
    }
    for (name in AllahNames.all) {
        if (name.contains(query)) out.add(SearchResult("name-$name", name, "أسماء الله الحسنى"))
    }
    return out
}

@Composable
fun SearchScreen(onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val trimmed = query.trim()
    val results = remember(trimmed) { buildResults(trimmed) }

    AmbientBackground(tint = Mint) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeader("بحث", onBack)
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = (16f * LocalTextScale.current).sp,
                        fontFamily = ReadexPro,
                        textAlign = TextAlign.Start
                    ),
                    cursorBrush = SolidColor(Mint),
                    modifier = Modifier.fillMaxWidth().glass(22.dp).padding(horizontal = 18.dp, vertical = 14.dp),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) Txt("ابحث في الأذكار والأسماء", 16f, color = SecondaryText)
                            inner()
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    if (trimmed.isEmpty()) {
                        item { Txt("اكتب كلمة مثل: الاستغفار، الصباح، الرزق", 15f, color = SecondaryText) }
                    } else if (results.isEmpty()) {
                        item { Txt("لا توجد نتائج", 15f, color = SecondaryText) }
                    } else {
                        items(results, key = { it.id }) { r ->
                            GlassPanel(modifier = Modifier.fillMaxWidth(), radius = 22.dp, padding = 18.dp) {
                                Txt(r.text, 16f, weight = FontWeight.Medium)
                                Spacer(Modifier.height(8.dp))
                                Txt(r.source, 12f, color = Mint)
                            }
                        }
                    }
                }
            }
        }
    }
}
