package uz.efir.azon.receiver;

import uz.efir.azon.Azon;
import uz.efir.azon.Notifier;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClickNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Notifier.stop();

        Intent i = new Intent(context, Azon.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}