package uz.efir.azon;

import uz.efir.azon.receiver.ClickNotificationReceiver;
import uz.efir.azon.receiver.ClearNotificationReceiver;
import uz.efir.azon.util.LocaleManager;

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

public class Notifier {

    private static MediaPlayer mediaPlayer;
    private static Context context;
    private static Notification notification;

    @SuppressLint("NewApi")
    public static void start(Context context, short timeIndex, long actualTime, LocaleManager localeManager) {
        Notifier.context = context;

        if(timeIndex == CONSTANT.NEXT_FAJR) timeIndex = CONSTANT.FAJR;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            notification = new Notification(R.drawable.icon, "", actualTime);
        } else {
            notification = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.icon)
                    .setWhen(actualTime)
                    .build();
        }
        String tickerText = CONSTANT.SUNRISE == timeIndex ? "" : context.getString(R.string.allahu_akbar);
        notification.tickerText = tickerText.concat(context.getString(R.string.time_for, context.getString(CONSTANT.TIME_NAMES[timeIndex])).toLowerCase(localeManager.getLocale()));

        int notificationMethod = VARIABLE.settings.getInt("notificationMethod" + timeIndex, timeIndex == CONSTANT.SUNRISE ? CONSTANT.NOTIFICATION_NONE : CONSTANT.NOTIFICATION_DEFAULT);
        if(notificationMethod == CONSTANT.NOTIFICATION_NONE || (timeIndex == CONSTANT.SUNRISE && !VARIABLE.alertSunrise())) {
            WakeLock.release();
            return;
        }
        stopNotification(); // We put this after since we don't want to clear previous notifications unless we have to

        int ringerMode = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
        int callState = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
        if((notificationMethod == CONSTANT.NOTIFICATION_PLAY || notificationMethod == CONSTANT.NOTIFICATION_CUSTOM) && ringerMode != AudioManager.RINGER_MODE_SILENT && ringerMode != AudioManager.RINGER_MODE_VIBRATE && callState == TelephonyManager.CALL_STATE_IDLE) {
            notification.tickerText = notification.tickerText + " (" + context.getString(R.string.stop) + ")";
            int alarm = R.raw.beep;
            if(timeIndex == CONSTANT.DHUHR || timeIndex == CONSTANT.ASR || timeIndex == CONSTANT.MAGHRIB || timeIndex == CONSTANT.ISHAA) {
                alarm = R.raw.adhan;
            } else if(timeIndex == CONSTANT.FAJR) {
                alarm = R.raw.adhan_fajr;
            }
            if(notificationMethod == CONSTANT.NOTIFICATION_CUSTOM) {
                mediaPlayer = MediaPlayer.create(context, Uri.parse(VARIABLE.settings.getString("notificationCustomFile" + timeIndex, "")));
                try {
                    mediaPlayer.getDuration();
                } catch(Exception ex) {
                    mediaPlayer = MediaPlayer.create(context, alarm);
                    notification.tickerText = notification.tickerText + " - " + context.getString(R.string.error_playing_custom_file);
                }
            } else {
                mediaPlayer = MediaPlayer.create(context, alarm);
            }
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    notification.tickerText = notification.tickerText.toString().replace(" (" + Notifier.context.getString(R.string.stop) + ")", "");
                    notification.defaults = 0;
                    startNotification(); // New notification won't have the "(Stop)" at the end of it since we are done playing
                }
            });
            try {
                mediaPlayer.start();
            } catch(Exception ex) {
                notification.tickerText = notification.tickerText + " - " + context.getString(R.string.error_playing_alert);
            }
            notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
        } else {
            notification.defaults = Notification.DEFAULT_ALL;
        }
        startNotification();
    }
    public static void stop() {
        stopNotification();
        WakeLock.release();
    }

    private static void stopNotification() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
        if(context != null) ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }
    private static void startNotification() {
        Intent i = new Intent(context, Azon.class);
        notification.setLatestEventInfo(context, context.getString(R.string.app_name), notification.tickerText, PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
        notification.contentIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, ClickNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, ClearNotificationReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, notification);
        if(mediaPlayer == null || !mediaPlayer.isPlaying()) {
            try {
                Thread.sleep(CONSTANT.POST_NOTIFICATION_DELAY);
            } catch(Exception ex) {
                // Just trying to make sure the notification completes before we fall asleep again
            }
            WakeLock.release();
        }
    }
}