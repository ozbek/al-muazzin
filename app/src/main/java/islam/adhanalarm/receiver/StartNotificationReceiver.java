package islam.adhanalarm.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Schedule;
import islam.adhanalarm.WakeLock;
import islam.adhanalarm.service.StartNotificationService;

public class StartNotificationReceiver extends BroadcastReceiver {
    public static void setNext(Context context) {
        Schedule today = Schedule.today(context);
        short nextTimeIndex = today.nextTimeIndex();
        set(context, nextTimeIndex, today.getTimes()[nextTimeIndex]);
    }

    private static void set(Context context, short timeIndex, Calendar actualTime) {
        if (Calendar.getInstance().after(actualTime)) {
            // Somehow current time is greater than the prayer time
            return;
        }

        Intent intent = new Intent(context, StartNotificationReceiver.class);
        intent.putExtra(CONSTANT.EXTRA_ACTUAL_TIME, actualTime.getTimeInMillis());
        intent.putExtra(CONSTANT.EXTRA_TIME_INDEX, timeIndex);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            am.setExact(AlarmManager.RTC_WAKEUP, actualTime.getTimeInMillis(),
                    PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        } else {
            am.set(AlarmManager.RTC_WAKEUP, actualTime.getTimeInMillis(),
                    PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        WakeLock.acquire(context);
        context.startService(new Intent(context, StartNotificationService.class).putExtras(intent));
    }
}
