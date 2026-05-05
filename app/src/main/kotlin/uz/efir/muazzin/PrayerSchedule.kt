package uz.efir.muazzin

import android.content.Context
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import java.text.NumberFormat
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PrayerSchedule(preferences: Preferences) {
    val times: Array<GregorianCalendar> = Array(7) { GregorianCalendar() }

    init {
        val method = CalculationMethodCatalog.method(preferences.calculationMethodIndex)

        val location = preferences.location
        val coordinates = Coordinates(location.latitude, location.longitude)

        val today = GregorianCalendar()
        val dateComponents = DateComponents(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH) + 1,
            today.get(Calendar.DAY_OF_MONTH)
        )

        val params = method.parameters
        var prayerTimes = PrayerTimes(coordinates, dateComponents, params)
        val allTimes = arrayOf(
            prayerTimes.fajr,
            prayerTimes.sunrise,
            prayerTimes.dhuhr,
            prayerTimes.asr,
            prayerTimes.maghrib,
            prayerTimes.isha
        )

        for (i in PrayerTime.FAJR.ordinal..<PrayerTime.NEXT_FAJR.ordinal) {
            // Set the times on the schedule
            this.times[i].timeInMillis = allTimes[i].toEpochMilliseconds()
            this.times[i].add(Calendar.MINUTE, preferences.offsetMinutes)
        }

        // Calculate next Fajr
        val nextDay = today.clone() as GregorianCalendar
        nextDay.add(Calendar.DAY_OF_MONTH, 1)
        val nextDateComponents = DateComponents(
            nextDay.get(Calendar.YEAR),
            nextDay.get(Calendar.MONTH) + 1,
            nextDay.get(Calendar.DAY_OF_MONTH)
        )
        prayerTimes = PrayerTimes(coordinates, nextDateComponents, params)
        this.times[PrayerTime.NEXT_FAJR.ordinal].timeInMillis =
            prayerTimes.fajr.toEpochMilliseconds()
        this.times[PrayerTime.NEXT_FAJR.ordinal].add(Calendar.MINUTE, preferences.offsetMinutes)
    }

    fun nextTimeIndex(): Int {
        val now: Calendar = GregorianCalendar()
        if (now.before(this.times[PrayerTime.FAJR.ordinal])) return PrayerTime.FAJR.ordinal
        var i = PrayerTime.FAJR.ordinal
        while (i < PrayerTime.NEXT_FAJR.ordinal) {
            if (now.after(this.times[i]) && now.before(this.times[i + 1])) {
                return ++i
            }
            i++
        }
        return PrayerTime.NEXT_FAJR.ordinal
    }

    fun hijriDateToString(context: Context): String {
        var hijrahDate = HijrahDate.now()
        if (currentlyAfterSunset()) {
            hijrahDate = hijrahDate.plus(1, ChronoUnit.DAYS)
        }
        val numberFormat = NumberFormat.getInstance(Locale.getDefault()).apply {
            isGroupingUsed = false
        }
        val day = numberFormat.format(hijrahDate.get(ChronoField.DAY_OF_MONTH).toLong())
        val month =
            context.resources.getStringArray(R.array.hijri_months)[hijrahDate.get(ChronoField.MONTH_OF_YEAR) - 1]
        val year = numberFormat.format(hijrahDate.get(ChronoField.YEAR).toLong())
        return context.getString(R.string.anno_hegirae, day, month, year)
    }

    private fun currentlyAfterSunset(): Boolean {
        val now: Calendar = GregorianCalendar()
        return now.after(this.times[PrayerTime.MAGHRIB.ordinal])
    }
}
