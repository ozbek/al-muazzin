package uz.efir.muazzin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class StartNotificationService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startInForeground()
        Thread(StartNotificationTask(this, intent)).start()
        return START_NOT_STICKY
    }

    private fun startInForeground() {
        val channel = NotificationChannel(
            CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_MIN
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setLocalOnly(true)
                .setOngoing(true)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                FOREGROUND_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(FOREGROUND_ID, notification)
        }
    }

    /**
     * We do the actual work in a separate thread because a Service has a limited lifetime.
     */
    internal inner class StartNotificationTask(
        private val context: Context, private val intent: Intent?
    ) : Runnable {
        override fun run() {
            try {
                val updateIntent =
                    Intent(PrayerTimesFragment.ACTION_UPDATE_UI).setPackage(BuildConfig.APPLICATION_ID)
                sendBroadcast(updateIntent)

                val timeIndex: Int =
                    intent?.getIntExtra(PrayerAlarmScheduler.EXTRA_TIME_INDEX, -1) ?: -1
                val actualTime: Long =
                    intent?.getLongExtra(PrayerAlarmScheduler.EXTRA_ACTUAL_TIME, 0L) ?: 0L
                if (timeIndex >= 0) {
                    NotificationService.notify(context, timeIndex, actualTime)
                } else {
                    WakeLock.release()
                }
            } catch (t: Throwable) {
                Log.e("StartNotificationService", "Failed to dispatch alarm", t)
                WakeLock.release()
            } finally {
                stopSelf()
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "uz.efir.muazzin.PREPARING_CHANNEL"
        private const val FOREGROUND_ID = 9999
    }
}
