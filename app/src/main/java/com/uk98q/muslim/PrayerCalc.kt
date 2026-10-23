package com.uk98q.muslim

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.tan

data class PrayerTime(val id: String, val name: String, val epochMillis: Long)

/**
 * Prayer times computed on the device with no network access.
 * Muslim World League angles (Fajr 18, Isha 17) and the standard Asr shadow factor (1).
 * Same formula as the iOS app.
 */
object PrayerCalc {
    const val MAKKAH_LAT = 21.4225
    const val MAKKAH_LON = 39.8262

    private val RAD = PI / 180.0

    /** All prayers for the given local calendar day, in chronological order. */
    fun times(day: LocalDate, lat: Double, lon: Double): List<PrayerTime> {
        val midnightUtc = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val jd0 = midnightUtc / 86400000.0 + 2440587.5
        val sun = solar(jd0 + 0.5)
        val declination = sun.first
        val eqTime = sun.second
        // Solar noon in minutes after 00:00 UTC.
        val noon = 720.0 - 4.0 * lon - eqTime
        val asrAltitude = atan(1.0 / (1.0 + tan(abs(lat * RAD - declination)))) / RAD

        fun at(minutes: Double): Long = midnightUtc + minutes.roundToLong() * 60000L

        val out = mutableListOf<PrayerTime>()
        offset(-18.0, lat, declination)?.let { out.add(PrayerTime("fajr", "الفجر", at(noon - it))) }
        offset(-0.833, lat, declination)?.let { out.add(PrayerTime("sunrise", "الشروق", at(noon - it))) }
        out.add(PrayerTime("dhuhr", "الظهر", at(noon)))
        offset(asrAltitude, lat, declination)?.let { out.add(PrayerTime("asr", "العصر", at(noon + it))) }
        offset(-0.833, lat, declination)?.let { out.add(PrayerTime("maghrib", "المغرب", at(noon + it))) }
        offset(-17.0, lat, declination)?.let { out.add(PrayerTime("isha", "العشاء", at(noon + it))) }
        return out
    }

    /** The next prayer after nowMs, skipping sunrise. Falls back to tomorrow's Fajr. */
    fun next(nowMs: Long, lat: Double, lon: Double, zone: ZoneId = ZoneId.systemDefault()): PrayerTime? {
        val day = Instant.ofEpochMilli(nowMs).atZone(zone).toLocalDate()
        val upcoming = times(day, lat, lon).firstOrNull { it.id != "sunrise" && it.epochMillis > nowMs }
        if (upcoming != null) return upcoming
        return times(day.plusDays(1), lat, lon).firstOrNull { it.id == "fajr" }
    }

    /** Hour angle in minutes for a given sun altitude, or null if the sun never reaches it that day. */
    private fun offset(altitude: Double, lat: Double, decl: Double): Double? {
        val latR = lat * RAD
        val c = (sin(altitude * RAD) - sin(latR) * sin(decl)) / (cos(latR) * cos(decl))
        if (abs(c) > 1.0) return null
        return acos(c) / RAD * 4.0
    }

    /** Solar declination (radians) and equation of time (minutes), NOAA style approximation. */
    private fun solar(jd: Double): Pair<Double, Double> {
        val t = (jd - 2451545.0) / 36525.0
        val l0 = (280.46646 + t * (36000.76983 + 0.0003032 * t)) % 360.0
        val m = 357.52911 + t * (35999.05029 - 0.0001537 * t)
        val e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
        val mr = m * RAD
        val c1 = sin(mr) * (1.914602 - t * (0.004817 + 0.000014 * t))
        val c2 = sin(2 * mr) * (0.019993 - 0.000101 * t)
        val c3 = sin(3 * mr) * 0.000289
        val trueLong = l0 + c1 + c2 + c3
        val omega = (125.04 - 1934.136 * t) * RAD
        val appLong = (trueLong - 0.00569 - 0.00478 * sin(omega)) * RAD
        val meanObl = 23.0 + (26.0 + (21.448 - t * (46.815 + t * (0.00059 - t * 0.001813))) / 60.0) / 60.0
        val obl = (meanObl + 0.00256 * cos(omega)) * RAD
        val decl = asin(sin(obl) * sin(appLong))
        val y = tan(obl / 2.0).pow(2)
        val l0r = l0 * RAD
        val part1 = y * sin(2 * l0r) - 2 * e * sin(mr)
        val part2 = 4 * e * y * sin(mr) * cos(2 * l0r) - 0.5 * y * y * sin(4 * l0r)
        val part3 = 1.25 * e * e * sin(2 * mr)
        val eq = 4 * (part1 + part2 - part3) / RAD
        return Pair(decl, eq)
    }
}
