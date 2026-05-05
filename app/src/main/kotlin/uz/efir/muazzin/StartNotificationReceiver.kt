package uz.efir.muazzin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StartNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            WakeLock.acquire(context)
            PrayerAlarmScheduler.setNext(context)
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                WakeLock.release()
            } else {
                val serviceIntent =
                    Intent(context, StartNotificationService::class.java).putExtras(intent)
                context.startForegroundService(serviceIntent)
            }
        } catch (t: Throwable) {
            Log.e("StartNotificationReceiver", "Failed to handle ${intent.action}", t)
            WakeLock.release()
        }
    }
}
