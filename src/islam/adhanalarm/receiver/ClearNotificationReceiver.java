package islam.adhanalarm.receiver;

import islam.adhanalarm.Notifier;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClearNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Notifier.stop();
    }
}