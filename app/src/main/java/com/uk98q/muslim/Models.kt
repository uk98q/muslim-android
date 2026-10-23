package com.uk98q.muslim

import androidx.compose.ui.graphics.Color

data class Dhikr(val id: String, val text: String, val target: Int)

val Dawn = Color(1.0f, 0.74f, 0.52f)
val Dusk = Color(0.70f, 0.64f, 1.0f)
val Mint = Color(0.52f, 0.94f, 0.82f)
val SecondaryText = Color.White.copy(alpha = 0.62f)

enum class Category(
    val id: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val items: List<Dhikr>
) {
    MORNING("morning", "أذكار الصباح", "ابدأ يومك بذكر الله", Dawn, AthkarData.morning),
    EVENING("evening", "أذكار المساء", "اختم يومك بذكر الله", Dusk, AthkarData.evening),
    AFTER_PRAYER("afterPrayer", "بعد الصلاة", "تسبيح وأذكار بعد السلام", Mint, AthkarData.afterPrayer),
    SLEEP("sleep", "أذكار النوم", "قبل أن تنام", Dusk, AthkarData.sleep),
    WAKE_UP("wakeUp", "عند الاستيقاظ", "أول ما تستيقظ", Dawn, AthkarData.wakeUp),
    TRAVEL("travel", "دعاء السفر", "للمسافر وفي الطريق", Mint, AthkarData.travel),
    DUAS("duas", "أدعية متنوعة", "أدعية جامعة مأثورة", Dawn, AthkarData.duas);

    companion object {
        fun fromId(id: String): Category = entries.firstOrNull { it.id == id } ?: MORNING
    }
}

fun progressLabel(progress: Double): String {
    if (progress >= 1.0) return "تم إنجاز القسم"
    return "${(progress * 100).toInt()}% مكتمل"
}

val TASBIH_PHRASES = listOf("سبحان الله", "الحمد لله", "الله أكبر", "لا إله إلا الله", "أستغفر الله")
val TASBIH_TARGETS = listOf(33, 99, 100, 0)
