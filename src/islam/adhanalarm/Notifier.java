package islam.adhanalarm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;

import islam.adhanalarm.receiver.ClearNotificationReceiver;
import islam.adhanalarm.receiver.ClickNotificationReceiver;
import uz.efir.muazzin.Muazzin;
import uz.efir.muazzin.R;

public class Notifier {

    private static MediaPlayer mediaPlayer;
    private static Context context;
    private static Notification notification;

    public static void start(Context context, short timeIndex, long actualTime) {
        Notifier.context = context;

        if (timeIndex == CONSTANT.NEXT_FAJR) {
            timeIndex = CONSTANT.FAJR;
        }
        Preferences preferences = Preferences.getInstance(context);
        int notificationMethod = preferences.getNotificationMethod(timeIndex);
        if (notificationMethod == CONSTANT.NOTIFICATION_NONE) {
            WakeLock.release();
            return;
        }

        buildNotification(timeIndex, actualTime);
        // We call this here since we don't want to
        // clear previous notifications unless we have to
        stopNotification();

        final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        final TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        if (notificationMethod >= CONSTANT.NOTIFICATION_PLAY
                && am.getRingerMode() > AudioManager.RINGER_MODE_VIBRATE
                && tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
            notification.tickerText = notification.tickerText + " ("
                    + context.getString(R.string.stop) + ")";
            int alarm = R.raw.beep;
            if (timeIndex <= CONSTANT.ISHAA && timeIndex >= CONSTANT.DHUHR) {
                alarm = R.raw.adhan;
            } else if (timeIndex == CONSTANT.FAJR) {
                alarm = R.raw.adhan_fajr;
            }
            if (notificationMethod == CONSTANT.NOTIFICATION_CUSTOM) {
                mediaPlayer = MediaPlayer.create(context,
                        Uri.parse(preferences.getCustomFilePath(timeIndex)));
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(context, alarm);
                    notification.tickerText = notification.tickerText + " - "
                            + context.getString(R.string.error_playing_custom_file);
                }
            } else {
                mediaPlayer = MediaPlayer.create(context, alarm);
            }
            final short finalTimeIndex = timeIndex;
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    notification.tickerText = notification.tickerText.toString().replace(
                            " (" + Notifier.context.getString(R.string.stop) + ")", "");
                    notification.defaults = 0;
                    // Since we are playing
                    // the new notification won't have the "(Stop)" at the end of it
                    startNotification(finalTimeIndex);
                }
            });
            try {
                mediaPlayer.start();
            } catch(IllegalStateException ise) {
                notification.tickerText = notification.tickerText + " - "
                        + context.getString(R.string.error_playing_alert);
            }
            notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
        } else {
            notification.defaults = Notification.DEFAULT_ALL;
        }
        startNotification(timeIndex);
    }

    public static void stop() {
        stopNotification();
        WakeLock.release();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private static void buildNotification(short timeIndex, long actualTime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            notification = new Notification(R.drawable.ic_launcher, "", actualTime);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            notification = new Notification.Builder(context).setSmallIcon(R.drawable.ic_launcher)
                    .setWhen(actualTime).getNotification();
        } else {
            notification = new Notification.Builder(context).setSmallIcon(R.drawable.ic_launcher)
                    .setWhen(actualTime).build();
        }
        notification.tickerText = context.getString(R.string.time_for,
                context.getString(CONSTANT.TIME_NAMES[timeIndex]));
    }

    @SuppressWarnings("deprecation")
    private static void startNotification(short timeIndex) {
        Intent intent = new Intent(context, Muazzin.class);
        notification.setLatestEventInfo(
                context,
                context.getString(CONSTANT.TIME_NAMES[timeIndex]),
                notification.tickerText,
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT
                        | PendingIntent.FLAG_ONE_SHOT));
        notification.contentIntent = PendingIntent.getBroadcast(context, 0, new Intent(context,
                ClickNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT
                | PendingIntent.FLAG_ONE_SHOT);
        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, new Intent(context,
                ClearNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT
                | PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            WakeLock.release();
        }
    }

    private static void stopNotification() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        if (context != null) {
            NotificationManager notificationManager = (NotificationManager)context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }
}
