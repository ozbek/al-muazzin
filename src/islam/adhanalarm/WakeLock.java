package islam.adhanalarm;

import android.content.Context;
import android.os.PowerManager;

public class WakeLock {

    private static PowerManager.WakeLock sWakeLock;

    public static void acquire(Context context) {
        if (sWakeLock == null) {
            final PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getPackageName());
        } else if (/*sWakeLock != null && */sWakeLock.isHeld()) {
            return;
        }

        sWakeLock.acquire();
    }

    public static void release() {
        if (sWakeLock != null) {
            if (sWakeLock.isHeld()) {
                sWakeLock.release();
            }
            sWakeLock = null;
        }
    }
}
