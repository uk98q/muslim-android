package com.uk98q.muslim

import android.Manifest
import android.icu.text.SimpleDateFormat
import android.icu.util.ULocale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

data class HomeSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val route: String
)

@Composable
fun HomeScreen(state: AppState, onNavigate: (String) -> Unit, onSearch: () -> Unit) {
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) LocationHelper.fetch(context, state)
    }
    LaunchedEffect(Unit) {
        if (LocationHelper.hasPermission(context)) {
            LocationHelper.fetch(context, state)
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val zone = ZoneId.systemDefault()
    val lat = state.latitude ?: PrayerCalc.MAKKAH_LAT
    val lon = state.longitude ?: PrayerCalc.MAKKAH_LON
    val nowZ: ZonedDateTime = Instant.ofEpochMilli(nowMs).atZone(zone)
    val prayers = PrayerCalc.times(nowZ.toLocalDate(), lat, lon)
    val next = PrayerCalc.next(nowMs, lat, lon, zone)
    val remaining = if (next != null) next.epochMillis - nowMs else 0L

    val dayOfYear = LocalDate.now().dayOfYear
    val featuredDua = AthkarData.duas[dayOfYear % AthkarData.duas.size].text
    val featuredName = AllahNames.all[dayOfYear % AllahNames.all.size]

    val sections = listOf(
        HomeSection("أذكار الصباح", progressLabel(state.progress(Category.MORNING)), Icons.Filled.Star, Dawn, "category/morning"),
        HomeSection("أذكار المساء", progressLabel(state.progress(Category.EVENING)), Icons.Filled.Favorite, Dusk, "category/evening"),
        HomeSection("بعد الصلاة", progressLabel(state.progress(Category.AFTER_PRAYER)), Icons.Filled.Check, Mint, "category/afterPrayer"),
        HomeSection("أذكار النوم", progressLabel(state.progress(Category.SLEEP)), Icons.Filled.Lock, Dusk, "category/sleep"),
        HomeSection("عند الاستيقاظ", progressLabel(state.progress(Category.WAKE_UP)), Icons.Filled.Notifications, Dawn, "category/wakeUp"),
        HomeSection("أسماء الله الحسنى", "${AllahNames.all.size} اسمًا", Icons.Filled.Info, Mint, "names"),
        HomeSection("المسبحة", "سبحان الله والحمد لله", Icons.Filled.Refresh, Mint, "tasbih"),
        HomeSection("العداد", "عداد حر لأي ذكر", Icons.Filled.Add, Dusk, "counter"),
        HomeSection("الإعدادات", "الخط والاهتزاز والموقع", Icons.Filled.Settings, Dawn, "settings")
    )

    AmbientBackground(tint = Dusk) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Top bar: logo and name, then settings, search and menu buttons.
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.sunrise_logo),
                    contentDescription = "شعار مسلم",
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.width(10.dp))
                Txt("مسلم", 20f, weight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                GlassIconButton(Icons.Filled.Settings, "الإعدادات", { onNavigate("settings") })
                Spacer(Modifier.width(10.dp))
                GlassIconButton(Icons.Filled.Search, "بحث", onSearch)
                Spacer(Modifier.width(10.dp))
                Box {
                    GlassIconButton(Icons.Filled.MoreVert, "القائمة", { menuOpen = true })
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Txt("دعاء السفر", 15f) },
                            onClick = { menuOpen = false; onNavigate("category/travel") }
                        )
                        DropdownMenuItem(
                            text = { Txt("أدعية متنوعة", 15f) },
                            onClick = { menuOpen = false; onNavigate("category/duas") }
                        )
                    }
                }
            }

            // Header: Hijri and Gregorian date, countdown to the next prayer, and prayer row.
            Column(modifier = Modifier.fillMaxWidth()) {
                Txt(hijriDate(nowMs), 20f, weight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Txt(gregorianDate(nowZ), 15f, color = SecondaryText)
            }

            GlassPanel(modifier = Modifier.fillMaxWidth(), radius = 28.dp, padding = 20.dp) {
                Txt("الوقت المتبقي على ${next?.name ?: ""}", 14f, color = Mint, weight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Txt(countdown(remaining), 34f, weight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    prayers.filter { it.id != "sunrise" }.forEach { p ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Txt(p.name, 11f, color = SecondaryText)
                            Txt(
                                clock(p.epochMillis, zone),
                                13f,
                                color = if (p.id == next?.id) Mint else Color.White,
                                weight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Txt(
                    if (state.latitude != null) "أوقات الصلاة محسوبة على موقعك، ومحليًا على جهازك فقط."
                    else "أوقات الصلاة تقريبية لمكة المكرمة حتى تسمح بالموقع.",
                    12f,
                    color = SecondaryText
                )
            }

            // Wide cards: horizontal scroll.
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureCard("الصباح", "أذكار الصباح", progressLabel(state.progress(Category.MORNING)), Icons.Filled.Star, Dawn) {
                    onNavigate("category/morning")
                }
                FeatureCard("المساء", "أذكار المساء", progressLabel(state.progress(Category.EVENING)), Icons.Filled.Favorite, Dusk) {
                    onNavigate("category/evening")
                }
                FeatureCard("دعاء اليوم", "من الأدعية المأثورة", featuredDua, Icons.Filled.Check, Mint) {
                    onNavigate("category/duas")
                }
                FeatureCard("اسم اليوم", featuredName, "من أسماء الله الحسنى", Icons.Filled.Info, Mint) {
                    onNavigate("names")
                }
            }

            Txt("الأقسام", 18f, weight = FontWeight.Bold)
            sections.chunked(2).forEach { pair ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    pair.forEach { section ->
                        SectionTile(section, Modifier.weight(1f)) { onNavigate(section.route) }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    eyebrow: String,
    title: String,
    detail: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    GlassPanel(
        modifier = Modifier.width(280.dp).height(176.dp),
        radius = 30.dp,
        padding = 20.dp,
        tint = accent,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Txt(eyebrow, 13f, color = accent, weight = FontWeight.Medium)
        }
        Spacer(Modifier.weight(1f))
        Txt(title, 20f, weight = FontWeight.Bold, maxLines = 2)
        Spacer(Modifier.height(6.dp))
        Txt(detail, 14f, color = SecondaryText, maxLines = 3)
    }
}

@Composable
fun SectionTile(section: HomeSection, modifier: Modifier, onClick: () -> Unit) {
    GlassPanel(
        modifier = modifier.height(150.dp),
        radius = 26.dp,
        padding = 18.dp,
        onClick = onClick
    ) {
        Icon(section.icon, null, tint = section.accent, modifier = Modifier.size(24.dp))
        Spacer(Modifier.weight(1f))
        Txt(section.title, 16f, weight = FontWeight.Bold, maxLines = 2)
        Spacer(Modifier.height(4.dp))
        Txt(section.subtitle, 12f, color = SecondaryText, maxLines = 2)
    }
}

private fun hijriDate(ms: Long): String {
    val formatter = SimpleDateFormat("EEEE d MMMM y", ULocale("ar@calendar=islamic-umalqura"))
    return formatter.format(Date(ms))
}

private fun gregorianDate(z: ZonedDateTime): String =
    DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale("ar")).format(z)

private fun clock(ms: Long, zone: ZoneId): String =
    DateTimeFormatter.ofPattern("HH:mm").format(Instant.ofEpochMilli(ms).atZone(zone))

fun countdown(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0L)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}
