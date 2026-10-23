package com.uk98q.muslim

import android.content.Context
import org.json.JSONObject

/** One run of words of a verse on a single mushaf line. Words in [text] are separated by single spaces. */
data class MItem(val text: String, val surah: Int, val ayah: Int, val end: Boolean)

/** kind: 't' text line, 's' surah header, 'b' basmala, 'x' blank line. */
data class MLine(val kind: Char, val surah: Int = 0, val items: List<MItem> = emptyList())

data class MPage(val number: Int, val juz: Int, val hizb: Int, val quarter: Int, val lines: List<MLine>)

/**
 * 604-page Madani layout. Layout from quran.com (word line numbers); text from the Tanzil
 * Hafs Uthmani file (CC BY 3.0). The JSON was built and checked by build_mushaf.py.
 */
object MushafRepo {
    @Volatile private var pageList: List<MPage>? = null
    @Volatile private var surahStart: IntArray = IntArray(0)
    @Volatile private var ayahPage: HashMap<Int, Int> = HashMap()

    val JUZ_NAMES = listOf(
        "الجزء الأول", "الجزء الثاني", "الجزء الثالث", "الجزء الرابع", "الجزء الخامس",
        "الجزء السادس", "الجزء السابع", "الجزء الثامن", "الجزء التاسع", "الجزء العاشر",
        "الجزء الحادي عشر", "الجزء الثاني عشر", "الجزء الثالث عشر", "الجزء الرابع عشر",
        "الجزء الخامس عشر", "الجزء السادس عشر", "الجزء السابع عشر", "الجزء الثامن عشر",
        "الجزء التاسع عشر", "الجزء العشرون", "الجزء الحادي والعشرون", "الجزء الثاني والعشرون",
        "الجزء الثالث والعشرون", "الجزء الرابع والعشرون", "الجزء الخامس والعشرون",
        "الجزء السادس والعشرون", "الجزء السابع والعشرون", "الجزء الثامن والعشرون",
        "الجزء التاسع والعشرون", "الجزء الثلاثون"
    )

    fun pages(context: Context): List<MPage> {
        pageList?.let { return it }
        synchronized(this) {
            pageList?.let { return it }
            val raw = context.assets.open("mushaf-pages.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            val arr = JSONObject(raw).getJSONArray("pages")
            val list = ArrayList<MPage>(arr.length())
            val starts = IntArray(115)
            val map = HashMap<Int, Int>()
            for (i in 0 until arr.length()) {
                val p = arr.getJSONObject(i)
                val la = p.getJSONArray("lines")
                val lines = ArrayList<MLine>(la.length())
                for (j in 0 until la.length()) {
                    val l = la.getJSONObject(j)
                    when (l.getString("k")) {
                        "t" -> {
                            val ia = l.getJSONArray("items")
                            val items = ArrayList<MItem>(ia.length())
                            for (k in 0 until ia.length()) {
                                val it = ia.getJSONObject(k)
                                val item = MItem(it.getString("t"), it.getInt("s"), it.getInt("a"), it.getInt("e") == 1)
                                items.add(item)
                                val key = item.surah * 1000 + item.ayah
                                if (!map.containsKey(key)) map[key] = p.getInt("n")
                            }
                            lines.add(MLine('t', 0, items))
                        }
                        "s" -> {
                            val s = l.getInt("surah")
                            if (starts[s] == 0) starts[s] = p.getInt("n")
                            lines.add(MLine('s', s))
                        }
                        "b" -> lines.add(MLine('b'))
                        else -> lines.add(MLine('x'))
                    }
                }
                list.add(MPage(p.getInt("n"), p.getInt("juz"), p.getInt("hizb"), p.getInt("q"), lines))
            }
            surahStart = starts
            ayahPage = map
            pageList = list
            return list
        }
    }

    /** First page of a surah (1-based). */
    fun startPage(context: Context, surah: Int): Int {
        pages(context)
        return surahStart.getOrNull(surah)?.takeIf { it > 0 } ?: 1
    }

    /** Page on which an ayah starts (1-based). */
    fun pageOf(context: Context, surah: Int, ayah: Int): Int {
        pages(context)
        return ayahPage[surah * 1000 + ayah] ?: startPage(context, surah)
    }
}
