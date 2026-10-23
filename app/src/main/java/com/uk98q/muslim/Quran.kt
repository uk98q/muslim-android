package com.uk98q.muslim

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Hafs 'an 'Asim, Uthmani script. Text from the Tanzil Project (CC BY 3.0), bundled unchanged. */
data class Surah(
    val number: Int,
    val name: String,
    val revelation: String,
    val ayahs: List<String>
) {
    val ayahCount: Int get() = ayahs.size
    val revelationLabel: String get() = if (revelation == "makkah") "مكية" else "مدنية"
}

object QuranRepo {
    @Volatile
    private var cache: List<Surah>? = null

    fun surahs(context: Context): List<Surah> {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val raw = context.assets.open("quran-hafs-uthmani.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            val arr = JSONObject(raw).getJSONArray("surahs")
            val list = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val ay = o.getJSONArray("ayahs")
                Surah(
                    number = o.getInt("number"),
                    name = o.getString("name"),
                    revelation = o.getString("revelation"),
                    ayahs = (0 until ay.length()).map { ay.getString(it) }
                )
            }
            cache = list
            return list
        }
    }

    fun surah(context: Context, number: Int): Surah? = surahs(context).firstOrNull { it.number == number }
}

/** Reciters streamed from everyayah.com (folders verified: each returns an MP3). */
enum class Reciter(val arName: String, val folder: String) {
    ALAFASY("مشاري راشد العفاسي", "Alafasy_128kbps"),
    BASIT("عبد الباسط عبد الصمد (مرتل)", "Abdul_Basit_Murattal_192kbps"),
    SUDAIS("عبد الرحمن السديس", "Abdurrahmaan_As-Sudais_192kbps"),
    GHAMIDI("سعد الغامدي", "Ghamadi_40kbps"),
    MAHER("ماهر المعيقلي", "MaherAlMuaiqly128kbps");

    fun url(surah: Int, ayah: Int): String =
        "https://everyayah.com/data/$folder/%03d%03d.mp3".format(surah, ayah)

    companion object {
        fun fromIndex(index: Int): Reciter = values().getOrElse(index) { ALAFASY }
    }
}

/** Ibn Kathir (Arabic) tafsir from the quran.com API v4, resource 14. Author died 774 AH. */
object Tafsir {
    const val ATTRIBUTION = "التفسير: تفسير ابن كثير (الحافظ ابن كثير، ت 774هـ) عبر واجهة quran.com"

    private val cache = HashMap<String, String>()

    /** Blocking network call: run it on a background thread. */
    fun fetch(surah: Int, ayah: Int): String {
        val key = "$surah:$ayah"
        synchronized(cache) {
            cache[key]?.let { return it }
        }
        val conn = URL("https://api.quran.com/api/v4/tafsirs/14/by_ayah/$key").openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 30000
        try {
            if (conn.responseCode != 200) throw IllegalStateException("HTTP ${conn.responseCode}")
            val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val text = plainText(JSONObject(body).getJSONObject("tafsir").getString("text"))
            synchronized(cache) { cache[key] = text }
            return text
        } finally {
            conn.disconnect()
        }
    }

    fun plainText(html: String): String {
        var s = html.replace(Regex("(?i)<br\\s*/?>"), "\n").replace("</p>", "\n\n")
        s = s.replace(Regex("<[^>]+>"), "")
        s = s.replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
        return s.trim()
    }
}

/** Eastern Arabic-Indic digits for ayah numbers. */
fun arabicDigits(value: Int): String = value.toString().map { '٠' + (it - '0') }.joinToString("")
