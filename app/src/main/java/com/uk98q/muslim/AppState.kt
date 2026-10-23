package com.uk98q.muslim

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import org.json.JSONObject
import java.time.LocalDate

/** Keeps all state on the device only: SharedPreferences for settings, and today's counts for adhkar. */
class AppState(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("muslim", Context.MODE_PRIVATE)
    private val storageKey = "athkar.progress.v2"

    var textSize by mutableIntStateOf(prefs.getInt("textSize", 1))
        private set
    var haptics by mutableStateOf(prefs.getBoolean("haptics", true))
        private set
    var tasbihCount by mutableIntStateOf(prefs.getInt("tasbih.count", 0))
        private set
    var tasbihTotal by mutableIntStateOf(prefs.getInt("tasbih.total", 0))
        private set
    var tasbihTarget by mutableIntStateOf(prefs.getInt("tasbih.target", 33))
        private set
    var tasbihPhrase by mutableIntStateOf(prefs.getInt("tasbih.phrase", 0))
        private set
    var counterValue by mutableIntStateOf(prefs.getInt("counter.value", 0))
        private set
    var counterLabel by mutableStateOf(prefs.getString("counter.label", "عداد") ?: "عداد")
        private set
    var latitude by mutableStateOf<Double?>(null)
        private set
    var longitude by mutableStateOf<Double?>(null)
        private set

    val counts = mutableStateMapOf<String, Int>()

    init {
        loadProgress()
    }

    fun setTextSize(value: Int) {
        textSize = value
        prefs.edit().putInt("textSize", value).apply()
    }

    fun setHaptics(value: Boolean) {
        haptics = value
        prefs.edit().putBoolean("haptics", value).apply()
    }

    fun setTasbih(count: Int, total: Int) {
        tasbihCount = count
        tasbihTotal = total
        prefs.edit().putInt("tasbih.count", count).putInt("tasbih.total", total).apply()
    }

    fun setTasbihTarget(value: Int) {
        tasbihTarget = value
        prefs.edit().putInt("tasbih.target", value).apply()
    }

    fun setTasbihPhrase(value: Int) {
        tasbihPhrase = value
        prefs.edit().putInt("tasbih.phrase", value).apply()
    }

    fun setCounterValue(value: Int) {
        counterValue = value
        prefs.edit().putInt("counter.value", value).apply()
    }

    fun setCounterLabel(value: String) {
        counterLabel = value
        prefs.edit().putString("counter.label", value).apply()
    }

    fun setLocation(lat: Double, lon: Double) {
        latitude = lat
        longitude = lon
    }

    private fun today(): String = LocalDate.now().toString()

    private fun key(category: Category, id: String): String = today() + "|" + category.id + "|" + id

    fun count(category: Category, id: String): Int = counts[key(category, id)] ?: 0

    fun tap(category: Category, dhikr: Dhikr) {
        val k = key(category, dhikr.id)
        val current = counts[k] ?: 0
        if (current >= dhikr.target) return
        counts[k] = current + 1
        saveProgress()
    }

    fun resetItem(category: Category, dhikr: Dhikr) {
        counts.remove(key(category, dhikr.id))
        saveProgress()
    }

    fun resetCategory(category: Category) {
        val prefix = today() + "|" + category.id + "|"
        counts.keys.filter { it.startsWith(prefix) }.forEach { counts.remove(it) }
        saveProgress()
    }

    fun resetToday() {
        val prefix = today() + "|"
        counts.keys.filter { it.startsWith(prefix) }.forEach { counts.remove(it) }
        saveProgress()
    }

    /** Fraction of the category completed, weighted by each item's target. */
    fun progress(category: Category): Double {
        val total = category.items.sumOf { it.target }
        if (total == 0) return 0.0
        val done = category.items.sumOf { minOf(count(category, it.id), it.target) }
        return done.toDouble() / total
    }

    private fun loadProgress() {
        val raw = prefs.getString(storageKey, null) ?: return
        val obj = try {
            JSONObject(raw)
        } catch (e: Exception) {
            return
        }
        val prefix = today() + "|"
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next().toString()
            if (k.startsWith(prefix)) counts[k] = obj.optInt(k)
        }
    }

    private fun saveProgress() {
        val obj = JSONObject()
        for ((k, v) in counts) obj.put(k, v)
        prefs.edit().putString(storageKey, obj.toString()).apply()
    }
}
