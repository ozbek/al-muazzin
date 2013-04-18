package islam.adhanalarm.receiver;

import islam.adhanalarm.Schedule;
import islam.adhanalarm.WakeLock;
import islam.adhanalarm.service.StartNotificationService;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WakeLock.acquire(context);
        context.startService(new Intent(context, StartNotificationService.class).putExtras(intent));
    }

    public static void setNext(Context context) {
        Schedule today = Schedule.today(context);
        short nextTimeIndex = today.nextTimeIndex();
        set(context, nextTimeIndex, today.getTimes()[nextTimeIndex]);
    }
    private static void set(Context context, short timeIndex, Calendar actualTime) {
        if(Calendar.getInstance().after(actualTime)) return; // Somehow current time is greater than the prayer time

        Intent intent = new Intent(context, StartNotificationReceiver.class);
        intent.putExtra("timeIndex", timeIndex);
        intent.putExtra("actualTime", actualTime.getTimeInMillis());

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, actualTime.getTimeInMillis(), PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
    }
}