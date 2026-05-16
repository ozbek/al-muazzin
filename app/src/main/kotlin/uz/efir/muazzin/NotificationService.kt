package uz.efir.muazzin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class NotificationService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val timeIndex = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_TIME_INDEX, -1)
            val actualTime = intent.getLongExtra(PrayerAlarmScheduler.EXTRA_ACTUAL_TIME, -1)
            when (intent.action) {
                ACTION_NOTIFY -> {
                    if (timeIndex !in PrayerTime.entries.indices) {
                        Log.wtf(TAG, "ACTION_NOTIFY with invalid time index: $timeIndex")
                    } else {
                        playAdhanOrIssueNotification(timeIndex, actualTime)
                    }
                }

                ACTION_SNOOZE, ACTION_DONE -> dismissNotification(intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1))

                ACTION_STOP, ACTION_DELETE -> {
                    if (timeIndex !in PrayerTime.entries.indices) {
                        Log.wtf(TAG, "${intent.action} with invalid time index: $timeIndex")
                    } else if (stopAdhan()) {
                        if (timeIndex == PrayerTime.SUNRISE.ordinal) {
                            // No follow-up prayer prompt for sunrise
                            dismissNotification(timeIndex + 1)
                        } else {
                            issueNotification(timeIndex, actualTime)
                        }
                    }
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun stopAdhan(): Boolean {
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            return true
        }
        return false
    }

    private fun playAdhanOrIssueNotification(timeIndex: Int, actualTime: Long) {
        val preferences: Preferences = Preferences.getInstance(this)
        val notificationMethod = preferences.getNotificationMethod(timeIndex)
        if (notificationMethod < NotificationMethod.PLAY.ordinal) {
            issueNotification(timeIndex, actualTime)
            return
        }

        val mediaId = when (PrayerTime.entries[timeIndex]) {
            PrayerTime.FAJR -> R.raw.adhan_fajr

            PrayerTime.DHUHR,
            PrayerTime.ASR,
            PrayerTime.MAGHRIB,
            PrayerTime.ISHAA -> R.raw.adhan

            else -> R.raw.beep
        }
        mediaPlayer = MediaPlayer.create(this, mediaId)
        mediaPlayer?.setScreenOnWhilePlaying(true)
        mediaPlayer?.setOnCompletionListener { _: MediaPlayer? ->
            mediaPlayer?.release()
            mediaPlayer = null
            if (timeIndex == PrayerTime.SUNRISE.ordinal) {
                // No follow-up prayer prompt for sunrise
                dismissNotification(timeIndex + 1)
            } else {
                issueNotification(timeIndex, actualTime)
                stopForeground(STOP_FOREGROUND_DETACH)
                WakeLock.release()
            }
        }
        try {
            mediaPlayer?.start()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Could not start media playback", e)
            mediaPlayer?.release()
            mediaPlayer = null
        }

        issueNotification(timeIndex, actualTime)
    }

    private fun dismissNotification(id: Int = -1) {
        val nm = getSystemService(NotificationManager::class.java)
        if (id > 0) nm.cancel(id) else nm.cancelAll()
        stopForeground(STOP_FOREGROUND_REMOVE)
        WakeLock.release()
    }

    private fun issueNotification(timeIndex: Int, actualTime: Long) {
        val name: CharSequence = getString(R.string.app_name)
        val description = getString(R.string.notification)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notificationId = timeIndex + 1 // Make sure a non-zero ID for notification
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, CHANNEL_ID).setLocalOnly(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_stat_muazzin)
                .setWhen(actualTime)
                .setContentTitle(
                    getString(R.string.time_for, getString(PrayerTime.entries[timeIndex].labelRes))
                )

        if (mediaPlayer != null) {
            val stopIntent = Intent(this, NotificationService::class.java).setAction(ACTION_STOP)
                .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                .putExtra(PrayerAlarmScheduler.EXTRA_TIME_INDEX, timeIndex)
                .putExtra(PrayerAlarmScheduler.EXTRA_ACTUAL_TIME, actualTime)
            val piStop =
                PendingIntent.getService(this, timeIndex, stopIntent, PendingIntent.FLAG_IMMUTABLE)
            notificationBuilder.addAction(
                R.drawable.ic_stat_action_stop,
                getString(R.string.stop),
                piStop
            ).setDefaults(Notification.DEFAULT_VIBRATE or Notification.DEFAULT_LIGHTS)
        } else {
            val doneIntent = Intent(this, NotificationService::class.java).setAction(ACTION_DONE)
                .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            val piDone =
                PendingIntent.getService(this, timeIndex, doneIntent, PendingIntent.FLAG_IMMUTABLE)

            val snoozeIntent =
                Intent(this, NotificationService::class.java).setAction(ACTION_SNOOZE)
                    .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            val piSnooze = PendingIntent.getService(
                this, timeIndex, snoozeIntent, PendingIntent.FLAG_IMMUTABLE
            )

            notificationBuilder.addAction(
                R.drawable.ic_stat_action_done,
                getString(R.string.yes),
                piDone
            ).addAction(R.drawable.ic_stat_action_snooze, getString(R.string.later), piSnooze)
                .setDefaults(Notification.DEFAULT_ALL)
            if (timeIndex != PrayerTime.SUNRISE.ordinal) { // There is no sunrise prayer
                val question = getString(
                    R.string.would_you_pray, getString(PrayerTime.entries[timeIndex].labelRes)
                )
                notificationBuilder.setContentText(question)
                notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(question))
            }
            WakeLock.release()
        }

        val deleteIntent = Intent(this, NotificationService::class.java).setAction(ACTION_DELETE)
            .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            .putExtra(PrayerAlarmScheduler.EXTRA_TIME_INDEX, timeIndex)
            .putExtra(PrayerAlarmScheduler.EXTRA_ACTUAL_TIME, actualTime)
        val piDelete =
            PendingIntent.getService(this, timeIndex, deleteIntent, PendingIntent.FLAG_IMMUTABLE)
        notificationBuilder.setDeleteIntent(piDelete)

        val resultIntent = Intent(this, Muazzin::class.java)
        resultIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val resultPendingIntent = PendingIntent.getActivity(
            this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder.setContentIntent(resultPendingIntent)

        val notification = notificationBuilder.build()
        startForeground(notificationId, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private val TAG: String = NotificationService::class.java.simpleName

        private const val ACTION_DONE = "uz.efir.muazzin.ACTION_DONE"
        private const val ACTION_STOP = "uz.efir.muazzin.ACTION_STOP"
        private const val ACTION_DELETE = "uz.efir.muazzin.ACTION_DELETE"
        private const val ACTION_SNOOZE = "uz.efir.muazzin.ACTION_SNOOZE"

        // To fire a test notification in debug builds:
        //   adb shell am start-foreground-service \
        //     -n uz.efir.muazzin/.NotificationService \
        //     -a uz.efir.muazzin.ACTION_NOTIFY \
        //     --ei uz.efir.muazzin.EXTRA_TIME_INDEX 0 \ # can be 0..5
        //     --el uz.efir.muazzin.EXTRA_ACTUAL_TIME $(date +%s%3N)
        internal const val ACTION_NOTIFY = "uz.efir.muazzin.ACTION_NOTIFY"

        private const val EXTRA_NOTIFICATION_ID = "uz.efir.muazzin.EXTRA_NOTIFICATION_ID"
        private const val CHANNEL_ID = "uz.efir.muazzin.NOTIFICATION_CHANNEL"

        private var mediaPlayer: MediaPlayer? = null

        fun notify(context: Context, timeIndex: Int, actualTime: Long) {
            var timeIndex = timeIndex
            if (timeIndex == PrayerTime.NEXT_FAJR.ordinal) {
                timeIndex = PrayerTime.FAJR.ordinal
            }
            val preferences: Preferences = Preferences.getInstance(context)
            val notificationMethod = preferences.getNotificationMethod(timeIndex)
            if (NotificationMethod.NONE.ordinal == notificationMethod) {
                WakeLock.release()
                return
            }

            val intent = Intent(context, NotificationService::class.java)
            intent.action = ACTION_NOTIFY
            intent.putExtra(PrayerAlarmScheduler.EXTRA_TIME_INDEX, timeIndex)
            intent.putExtra(PrayerAlarmScheduler.EXTRA_ACTUAL_TIME, actualTime)
            context.startForegroundService(intent)
        }
    }
}
