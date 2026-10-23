package com.uk98q.muslim

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CategoryScreen(state: AppState, category: Category, onBack: () -> Unit) {
    AmbientBackground(tint = category.accent) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeader(category.title, onBack) {
                GlassIconButton(Icons.Filled.Refresh, "تصفير القسم", { state.resetCategory(category) })
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SummaryCard(state, category) }
                items(category.items, key = { it.id }) { dhikr ->
                    DhikrCard(state, category, dhikr)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(state: AppState, category: Category) {
    val progress = state.progress(category)
    GlassPanel(modifier = Modifier.fillMaxWidth(), radius = 26.dp, padding = 20.dp) {
        Txt(category.subtitle, 17f, weight = FontWeight.Medium)
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = category.accent,
            trackColor = Color.White.copy(alpha = 0.12f)
        )
        Spacer(Modifier.height(10.dp))
        Txt(progressLabel(progress), 13f, color = SecondaryText)
    }
}

@Composable
private fun DhikrCard(state: AppState, category: Category, dhikr: Dhikr) {
    val haptic = LocalHapticFeedback.current
    val count = state.count(category, dhikr.id)
    val done = count >= dhikr.target
    val tint = if (done) Mint else category.accent

    GlassPanel(
        modifier = Modifier.fillMaxWidth().alpha(if (done) 0.6f else 1f),
        radius = 26.dp,
        padding = 22.dp
    ) {
        Txt(dhikr.text, 20f, weight = FontWeight.Medium)
        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(tint.copy(alpha = 0.16f))
                    .clickable(enabled = !done) {
                        if (state.haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        state.tap(category, dhikr)
                    }
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (done) Icons.Filled.Check else Icons.Filled.Add,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Txt("$count / ${dhikr.target}", 16f, color = tint, weight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            Txt("التكرار ${dhikr.target}", 14f, color = SecondaryText)
        }
        if (count > 0) {
            Spacer(Modifier.height(10.dp))
            Txt(
                "إعادة هذا الذكر",
                13f,
                color = Mint,
                modifier = Modifier.clickable { state.resetItem(category, dhikr) }
            )
        }
    }
}
