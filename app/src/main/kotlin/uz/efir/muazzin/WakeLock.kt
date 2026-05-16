package uz.efir.muazzin

import android.content.Context
import android.os.PowerManager

object WakeLock {
    private val lock = Any()
    private var wakeLock: PowerManager.WakeLock? = null
    private const val WAKELOCK_TIMEOUT_MS = 3 * 60 * 1000L // 3 minutes

    fun acquire(context: Context) = synchronized(lock) {
        val wl = wakeLock ?: run {
            val app = context.applicationContext
            val pm = app.getSystemService(PowerManager::class.java)
            pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${app.packageName}:WakeLock")
                .also { wakeLock = it }
        }
        wl.acquire(WAKELOCK_TIMEOUT_MS)
    }

    fun release() = synchronized(lock) {
        val wl = wakeLock
        if (wl != null && wl.isHeld) {
            wl.release()
        }
    }
}
