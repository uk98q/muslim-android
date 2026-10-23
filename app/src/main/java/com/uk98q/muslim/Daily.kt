package com.uk98q.muslim

import android.content.Context
import java.time.LocalDate

/** A dua from the authentic Prophetic supplications, with the hadith collection it is reported in. */
data class DailyDua(val text: String, val source: String)

/** One verse of the Hafs text, with its surah and ayah numbers. */
data class DailyVerse(val surah: Int, val surahName: String, val ayah: Int, val text: String)

/**
 * Picks the verse and dua of the day deterministically from the local calendar date,
 * so every day shows the same content on every launch, and consecutive days differ.
 */
object DailyContent {
    val duas: List<DailyDua> = listOf(
        DailyDua("اللهم إني أسألك علما نافعا، ورزقا طيبا، وعملا متقبلا", "رواه ابن ماجه، وصححه الألباني"),
        DailyDua("اللهم اهدني وسددني", "رواه مسلم"),
        DailyDua("اللهم إني أعوذ بك من الهم والحزن، والعجز والكسل، والبخل والجبن، وضلع الدين وغلبة الرجال", "رواه البخاري"),
        DailyDua("اللهم أصلح لي ديني الذي هو عصمة أمري، وأصلح لي دنياي التي فيها معاشي، وأصلح لي آخرتي التي فيها معادي، واجعل الحياة زيادة لي في كل خير، واجعل الموت راحة لي من كل شر", "رواه مسلم"),
        DailyDua("اللهم إني أسألك الهدى والتقى والعفاف والغنى", "رواه مسلم"),
        DailyDua("اللهم إني أعوذ بك من علم لا ينفع، ومن قلب لا يخشع، ومن نفس لا تشبع، ومن دعوة لا يستجاب لها", "رواه مسلم"),
        DailyDua("اللهم اغفر لي خطيئتي وجهلي وإسرافي في أمري، وما أنت أعلم به مني", "متفق عليه"),
        DailyDua("اللهم إني أعوذ بك من الجبن والبخل، وأعوذ بك من أن أرد إلى أرذل العمر، وأعوذ بك من فتنة الدنيا وعذاب القبر", "رواه البخاري"),
        DailyDua("اللهم إنك عفو تحب العفو فاعف عني", "رواه الترمذي وابن ماجه"),
        DailyDua("اللهم أعني على ذكرك وشكرك وحسن عبادتك", "رواه أبو داود والنسائي")
    )

    fun dua(day: LocalDate): DailyDua {
        val count = duas.size
        val index = (((day.toEpochDay() % count) + count) % count).toInt()
        return duas[index]
    }

    fun verse(context: Context, day: LocalDate): DailyVerse {
        val flat = QuranRepo.surahs(context).flatMap { surah ->
            surah.ayahs.mapIndexed { index, text ->
                DailyVerse(surah.number, surah.name, index + 1, text.trim())
            }
        }
        if (flat.isEmpty()) return DailyVerse(1, "الفاتحة", 1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ")
        val count = flat.size
        val index = (((day.toEpochDay() % count) + count) % count).toInt()
        return flat[index]
    }
}
