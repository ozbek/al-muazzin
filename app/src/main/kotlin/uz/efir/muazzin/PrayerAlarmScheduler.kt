package uz.efir.muazzin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar
import java.util.GregorianCalendar

object PrayerAlarmScheduler {
    const val EXTRA_ACTUAL_TIME: String = "uz.efir.muazzin.EXTRA_ACTUAL_TIME"
    const val EXTRA_TIME_INDEX: String = "uz.efir.muazzin.EXTRA_TIME_INDEX"

    fun setNext(context: Context) {
        val preferences = Preferences.getInstance(context)
        val today = PrayerSchedule(preferences)
        val nextTimeIndex = today.nextTimeIndex()
        set(context, nextTimeIndex, today.times[nextTimeIndex])
    }

    private fun set(context: Context, timeIndex: Int, actualTime: GregorianCalendar) {
        if (Calendar.getInstance().after(actualTime)) {
            // Somehow current time is greater than the prayer time
            return
        }

        val intent = Intent(context, StartNotificationReceiver::class.java)
        intent.putExtra(EXTRA_ACTUAL_TIME, actualTime.timeInMillis)
        intent.putExtra(EXTRA_TIME_INDEX, timeIndex)

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            actualTime.timeInMillis,
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}
