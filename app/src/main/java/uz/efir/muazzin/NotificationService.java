package uz.efir.muazzin;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Preferences;
import islam.adhanalarm.WakeLock;
import islam.adhanalarm.util.LocaleManager;

public class NotificationService extends IntentService {
    private static final String TAG = NotificationService.class.getSimpleName();

    private static final String ACTION_DONE = "uz.efir.muazzin.ACTION_DONE";
    private static final String ACTION_STOP = "uz.efir.muazzin.ACTION_STOP";
    private static final String ACTION_DELETE = "uz.efir.muazzin.ACTION_DELETE";
    private static final String ACTION_NOTIFY = "uz.efir.muazzin.ACTION_NOTIFY";
    private static final String ACTION_SNOOZE = "uz.efir.muazzin.ACTION_SNOOZE";

    private static final String EXTRA_NOTIFICATION_ID = "uz.efir.muazzin.EXTRA_NOTIFICATION_ID";

    private static MediaPlayer sMediaPlayer;

    public NotificationService() {
        super("NotificationService");
    }

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
        LocaleManager.getInstance(context, true);

        if (notificationMethod >= CONSTANT.NOTIFICATION_PLAY) {
            int alarm = R.raw.beep;
            if (timeIndex <= CONSTANT.ISHAA && timeIndex >= CONSTANT.DHUHR) {
                alarm = R.raw.adhan;
            } else if (timeIndex == CONSTANT.FAJR) {
                alarm = R.raw.adhan_fajr;
            }
            sMediaPlayer = MediaPlayer.create(context, alarm);
            sMediaPlayer.setScreenOnWhilePlaying(true);
            sMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    WakeLock.release();
                }
            });
            try {
                sMediaPlayer.start();
            } catch (IllegalStateException ise) {
                // Nothing to do here
            }
        }

        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_NOTIFY);
        intent.putExtra(CONSTANT.EXTRA_TIME_INDEX, timeIndex);
        intent.putExtra(CONSTANT.EXTRA_ACTUAL_TIME, actualTime);
        context.startService(intent);
    }

    public static void cancelAll(Context context) {
        if (sMediaPlayer != null && sMediaPlayer.isPlaying()) {
            sMediaPlayer.stop();
        }
        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        WakeLock.release();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NOTIFY.equals(action)) {
                issueNotification(intent.getIntExtra(CONSTANT.EXTRA_TIME_INDEX, -1),
                        intent.getLongExtra(CONSTANT.EXTRA_ACTUAL_TIME, -1));
            } else if (ACTION_SNOOZE.equals(action)) {
                // TODO: add snooze logic
                cancelNotification(intent);
            } else if (ACTION_DONE.equals(action)) {
                cancelNotification(intent);
            } else if (ACTION_STOP.equals(action)) {
                if (sMediaPlayer != null && sMediaPlayer.isPlaying()) {
                    sMediaPlayer.stop();
                }
                issueNotification(intent.getIntExtra(CONSTANT.EXTRA_TIME_INDEX, -1),
                        intent.getLongExtra(CONSTANT.EXTRA_ACTUAL_TIME, -1));
            } else if (ACTION_DELETE.equals(action)) {
                if (sMediaPlayer != null && sMediaPlayer.isPlaying()) {
                    sMediaPlayer.stop();
                }
            }
        }
    }

    private void cancelNotification(Intent intent) {
        NotificationManager notificationManager
                = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        if (notificationId < 0) {
            notificationManager.cancelAll();
        } else {
            notificationManager.cancel(notificationId);
        }
        WakeLock.release();
    }

    private void issueNotification(int timeIndex, long actualTime) {
        if (timeIndex < 0) {
            Log.wtf(TAG, "Got negative time index");
            return;
        }

        String question = getString(R.string.did_you_pray, getString(CONSTANT.TIME_NAMES[timeIndex]));
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLocalOnly(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(actualTime)
                .setContentTitle(
                        getString(R.string.time_for, getString(CONSTANT.TIME_NAMES[timeIndex]))
                );

        if (sMediaPlayer != null && sMediaPlayer.isPlaying()) {
            Intent stopIntent = new Intent(this, NotificationService.class)
                    .setAction(ACTION_STOP)
                    .putExtra(EXTRA_NOTIFICATION_ID, timeIndex)
                    .putExtra(CONSTANT.EXTRA_TIME_INDEX, timeIndex)
                    .putExtra(CONSTANT.EXTRA_ACTUAL_TIME, actualTime);
            PendingIntent piStop = PendingIntent.getService(this, 0, stopIntent, 0);

            notificationBuilder
                    .addAction(R.drawable.ic_stat_action_stop, getString(R.string.stop), piStop)
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        } else {
            Intent doneIntent = new Intent(this, NotificationService.class)
                    .setAction(ACTION_DONE)
                    .putExtra(EXTRA_NOTIFICATION_ID, timeIndex);
            PendingIntent piDone = PendingIntent.getService(this, timeIndex, doneIntent, 0);

            Intent snoozeIntent = new Intent(this, NotificationService.class)
                    .setAction(ACTION_SNOOZE)
                    .putExtra(EXTRA_NOTIFICATION_ID, timeIndex);
            PendingIntent piSnooze = PendingIntent.getService(this, timeIndex, snoozeIntent, 0);

            notificationBuilder
                    .addAction(R.drawable.ic_stat_action_done, getString(R.string.yes), piDone)
                    .addAction(R.drawable.ic_stat_action_snooze, getString(R.string.snooze), piSnooze)
                    .setDefaults(Notification.DEFAULT_ALL);
            if (timeIndex != CONSTANT.SUNRISE) {
                // There is no sunrise prayer
                notificationBuilder.setContentText(question);
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(question));
            }
            WakeLock.release();
        }

        Intent deleteIntent = new Intent(this, NotificationService.class)
                .setAction(ACTION_DELETE)
                .putExtra(EXTRA_NOTIFICATION_ID, timeIndex);
        PendingIntent piDelete = PendingIntent.getService(this, timeIndex, deleteIntent, 0);
        notificationBuilder.setDeleteIntent(piDelete);

        Intent resultIntent = new Intent(this, Muazzin.class);
        resultIntent.putExtra(EXTRA_NOTIFICATION_ID, timeIndex);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(timeIndex, notificationBuilder.build());
    }
}
