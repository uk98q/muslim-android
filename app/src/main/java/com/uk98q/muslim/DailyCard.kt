package com.uk98q.muslim

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

private sealed class DailyTafsir {
    object Idle : DailyTafsir()
    object Loading : DailyTafsir()
    class Done(val text: String) : DailyTafsir()
    object Failed : DailyTafsir()
}

/**
 * Home card for "آية اليوم" and "دعاء اليوم". The tafsir opens inside the card and
 * pushes the content below it down, so it never covers other content.
 */
@Composable
fun DailyCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val verse = remember(today) { DailyContent.verse(context, today) }
    val dua = remember(today) { DailyContent.dua(today) }
    val scope = rememberCoroutineScope()
    var showTafsir by remember { mutableStateOf(false) }
    var tafsir by remember { mutableStateOf<DailyTafsir>(DailyTafsir.Idle) }
    val scale = LocalTextScale.current

    GlassPanel(modifier = modifier.fillMaxWidth(), radius = 28.dp, padding = 20.dp) {
        Txt("اليوم", 13f, color = Mint, weight = FontWeight.Medium)
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Txt("آية اليوم", 17f, weight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Txt("${verse.surahName} · الآية ${arabicDigits(verse.ayah)}", 12f, color = SecondaryText)
        }
        Spacer(Modifier.height(12.dp))

        Text(
            text = verse.text,
            modifier = Modifier.fillMaxWidth(),
            fontFamily = AmiriQuran,
            fontSize = (24f * scale).sp,
            lineHeight = (24f * scale * 1.9f).sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                    showTafsir = !showTafsir
                    val needsLoad = tafsir is DailyTafsir.Idle || tafsir is DailyTafsir.Failed
                    if (showTafsir && needsLoad) {
                        tafsir = DailyTafsir.Loading
                        scope.launch {
                            tafsir = try {
                                DailyTafsir.Done(withContext(Dispatchers.IO) {
                                    Tafsir.fetch(verse.surah, verse.ayah)
                                })
                            } catch (e: Exception) {
                                DailyTafsir.Failed
                            }
                        }
                    }
                }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Txt(if (showTafsir) "إخفاء التفسير" else "عرض التفسير", 14f, color = Mint, weight = FontWeight.Medium)
        }

        if (showTafsir) {
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(14.dp)
            ) {
                when (val state = tafsir) {
                    is DailyTafsir.Done -> Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Txt(state.text, 15f)
                    }
                    is DailyTafsir.Failed -> Txt(
                        "تعذّر تحميل التفسير. تحقق من الاتصال بالإنترنت ثم حاول مرة أخرى.",
                        14f,
                        color = SecondaryText
                    )
                    else -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Mint, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Txt("جارٍ تحميل التفسير", 14f, color = SecondaryText)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Txt(Tafsir.ATTRIBUTION, 11f, color = SecondaryText)
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
        Spacer(Modifier.height(16.dp))

        Txt("دعاء اليوم", 17f, weight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Txt(dua.text, 17f)
        Spacer(Modifier.height(8.dp))
        Txt("المصدر: ${dua.source}", 12f, color = SecondaryText)
    }
}
