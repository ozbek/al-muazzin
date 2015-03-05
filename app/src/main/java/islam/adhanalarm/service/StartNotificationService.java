package islam.adhanalarm.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Notifier;
import islam.adhanalarm.Preferences;
import islam.adhanalarm.Schedule;
import islam.adhanalarm.WakeLock;
import islam.adhanalarm.receiver.StartNotificationReceiver;
import uz.efir.muazzin.Muazzin;
import uz.efir.muazzin.R;
import uz.efir.muazzin.Utils;

public class StartNotificationService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            Log.d("NotificationService", "The intent is null, recreate and shot a new one");
            Schedule today = Schedule.today(this);
            short nextTimeIndex = today.nextTimeIndex();
            Calendar actualTime = today.getTimes()[nextTimeIndex];
            intent = new Intent();
            intent.putExtra(CONSTANT.EXTRA_ACTUAL_TIME, actualTime.getTimeInMillis());
            intent.putExtra(CONSTANT.EXTRA_TIME_INDEX, nextTimeIndex);
        }
        new Thread(new StartNotificationTask(this, intent)).start();

        return START_REDELIVER_INTENT;
    }

    /**
     * We do the actual work in a separate thread since a Service has a limited life and we want to guarantee completion
     */
    final class StartNotificationTask implements Runnable {

        private final Context context;
        private final Intent intent;

        public StartNotificationTask(Context c, Intent i) {
            context = c;
            intent = i;
        }

        @Override
        public void run() {
            if (Utils.getIsForeground()) {
                // Update the UI marker and set the notification for the next prayer
                Intent i = new Intent(context, Muazzin.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } else {
                StartNotificationReceiver.setNext(context);
            }

//                Utils.updateWidgets(context);

            short timeIndex = intent.getShortExtra(CONSTANT.EXTRA_TIME_INDEX, (short) -1);
            if (timeIndex == -1) { // Got here from boot
                Preferences preferences = Preferences.getInstance(context);
                if (preferences.getBasmalaEnabled()) {
                    MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.bismillah);
                    mediaPlayer.setScreenOnWhilePlaying(true);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            WakeLock.release();
                        }
                    });
                    mediaPlayer.start();
                } else {
                    WakeLock.release();
                }
            } else {
                // Notify the user for the current time, need to do this last since it releases the WakeLock
                long actualTime = intent.getLongExtra(CONSTANT.EXTRA_ACTUAL_TIME, 0);
                Notifier.start(context, timeIndex, actualTime);
            }
        }
    }
}
