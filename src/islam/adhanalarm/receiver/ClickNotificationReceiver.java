package islam.adhanalarm.receiver;

import islam.adhanalarm.AdhanAlarm;
import islam.adhanalarm.Notifier;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClickNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Notifier.stop();

        Intent i = new Intent(context, AdhanAlarm.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}