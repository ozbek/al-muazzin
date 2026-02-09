package uz.efir.muazzin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {
    private static final String TAG = NotificationService.class.getSimpleName();

    private static final String ACTION_DONE = "uz.efir.muazzin.ACTION_DONE";
    private static final String ACTION_STOP = "uz.efir.muazzin.ACTION_STOP";
    private static final String ACTION_DELETE = "uz.efir.muazzin.ACTION_DELETE";
    private static final String ACTION_NOTIFY = "uz.efir.muazzin.ACTION_NOTIFY";
    private static final String ACTION_SNOOZE = "uz.efir.muazzin.ACTION_SNOOZE";

    private static final String EXTRA_NOTIFICATION_ID = "uz.efir.muazzin.EXTRA_NOTIFICATION_ID";
    private static final String CHANNEL_ID = "uz.efir.muazzin.NOTIFICATION_CHANNEL";

    private static MediaPlayer sMediaPlayer;
    private static boolean sIsPlaying = false;

    public static void notify(Context context, int timeIndex, long actualTime) {
        if (timeIndex == CONSTANT.NEXT_FAJR) {
            timeIndex = CONSTANT.FAJR;
        }
        Preferences preferences = Preferences.getInstance(context);
        final int notificationMethod = preferences.getNotificationMethod(timeIndex);
        if (CONSTANT.NOTIFICATION_NONE == notificationMethod) {
            WakeLock.release();
            return;
        }

        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_NOTIFY);
        intent.putExtra(CONSTANT.EXTRA_TIME_INDEX, timeIndex);
        intent.putExtra(CONSTANT.EXTRA_ACTUAL_TIME, actualTime);
        context.startForegroundService(intent);
    }

    public static void cancelAll(Context context) {
        if (sMediaPlayer != null) {
            if (sMediaPlayer.isPlaying()) {
                sMediaPlayer.stop();
            }
            sMediaPlayer.release();
            sMediaPlayer = null;
        }
        sIsPlaying = false;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        WakeLock.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            int timeIndex = intent.getIntExtra(CONSTANT.EXTRA_TIME_INDEX, -1);
            long actualTime = intent.getLongExtra(CONSTANT.EXTRA_ACTUAL_TIME, -1);
            createNotificationChannel();

            if (ACTION_NOTIFY.equals(action)) {
                playAdhan(timeIndex, actualTime);
            } else if (ACTION_SNOOZE.equals(action)) {
                cancelNotification(intent);
            } else if (ACTION_DONE.equals(action)) {
                cancelNotification(intent);
            } else if (ACTION_STOP.equals(action)) {
                stopAdhan();
                issueNotification(timeIndex, actualTime);
            } else if (ACTION_DELETE.equals(action)) {
                stopAdhan();
            }
        }
        return START_NOT_STICKY;
    }

    private void stopAdhan() {
        if (sMediaPlayer != null) {
            if (sMediaPlayer.isPlaying()) {
                sMediaPlayer.stop();
            }
            sMediaPlayer.release();
            sMediaPlayer = null;
        }
        sIsPlaying = false;
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.app_name);
        String description = getString(R.string.notification);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void playAdhan(int timeIndex, long actualTime) {
        Preferences preferences = Preferences.getInstance(this);
        final int notificationMethod = preferences.getNotificationMethod(timeIndex);
        if (notificationMethod < CONSTANT.NOTIFICATION_PLAY) {
            sIsPlaying = false;
            issueNotification(timeIndex, actualTime);
            return;
        }

        sIsPlaying = true;
        issueNotification(timeIndex, actualTime);

        int alarm = R.raw.beep;
        if (timeIndex <= CONSTANT.ISHAA && timeIndex >= CONSTANT.DHUHR) {
            alarm = R.raw.adhan;
        } else if (timeIndex == CONSTANT.FAJR) {
            alarm = R.raw.adhan_fajr;
        }
        sMediaPlayer = MediaPlayer.create(this, alarm);
        sMediaPlayer.setScreenOnWhilePlaying(true);
        sMediaPlayer.setOnCompletionListener(mp -> {
            sIsPlaying = false;
            sMediaPlayer.release();
            sMediaPlayer = null;
            issueNotification(timeIndex, actualTime);
            stopForeground(false);
            WakeLock.release();
        });
        try {
            sMediaPlayer.start();
        } catch (IllegalStateException ise) {
            sIsPlaying = false;
            issueNotification(timeIndex, actualTime);
        }
    }

    private void cancelNotification(Intent intent) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        if (notificationId < 0) {
            notificationManager.cancelAll();
        } else {
            notificationManager.cancel(notificationId);
        }
        stopForeground(true);
        WakeLock.release();
    }

    private void issueNotification(int timeIndex, long actualTime) {
        if (timeIndex < 0) {
            Log.wtf(TAG, "Got negative time index");
            return;
        }

        String question = getString(R.string.did_you_pray, getString(CONSTANT.TIME_NAMES[timeIndex]));
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID).setLocalOnly(true).setPriority(NotificationCompat.PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_ALARM).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setSmallIcon(R.drawable.ic_launcher).setWhen(actualTime).setContentTitle(getString(R.string.time_for, getString(CONSTANT.TIME_NAMES[timeIndex])));

        if (sIsPlaying) {
            Intent stopIntent = new Intent(this, NotificationService.class).setAction(ACTION_STOP).putExtra(EXTRA_NOTIFICATION_ID, timeIndex).putExtra(CONSTANT.EXTRA_TIME_INDEX, timeIndex).putExtra(CONSTANT.EXTRA_ACTUAL_TIME, actualTime);
            PendingIntent piStop = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

            notificationBuilder.addAction(R.drawable.ic_stat_action_stop, getString(R.string.stop), piStop).setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        } else {
            Intent doneIntent = new Intent(this, NotificationService.class).setAction(ACTION_DONE).putExtra(EXTRA_NOTIFICATION_ID, timeIndex);
            PendingIntent piDone = PendingIntent.getService(this, timeIndex, doneIntent, PendingIntent.FLAG_IMMUTABLE);

            Intent snoozeIntent = new Intent(this, NotificationService.class).setAction(ACTION_SNOOZE).putExtra(EXTRA_NOTIFICATION_ID, timeIndex);
            PendingIntent piSnooze = PendingIntent.getService(this, timeIndex, snoozeIntent, PendingIntent.FLAG_IMMUTABLE);

            notificationBuilder.addAction(R.drawable.ic_stat_action_done, getString(R.string.yes), piDone).addAction(R.drawable.ic_stat_action_snooze, getString(R.string.later), piSnooze).setDefaults(Notification.DEFAULT_ALL);
            if (timeIndex != CONSTANT.SUNRISE) {
                // There is no sunrise prayer
                notificationBuilder.setContentText(question);
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(question));
            }
            WakeLock.release();
        }

        Intent deleteIntent = new Intent(this, NotificationService.class).setAction(ACTION_DELETE).putExtra(EXTRA_NOTIFICATION_ID, timeIndex);
        PendingIntent piDelete = PendingIntent.getService(this, timeIndex, deleteIntent, PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder.setDeleteIntent(piDelete);

        Intent resultIntent = new Intent(this, Muazzin.class);
        resultIntent.putExtra(EXTRA_NOTIFICATION_ID, timeIndex);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder.setContentIntent(resultPendingIntent);

        Notification notification = notificationBuilder.build();
        startForeground(timeIndex, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
