package uz.efir.muazzin

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.core.content.edit

class Preferences private constructor(context: Context) {
    private val mSharedPreferences: SharedPreferences = context.getSharedPreferences(
        context.packageName + PREFERENCE_FILENAME_SUFFIX, Context.MODE_PRIVATE
    )

    init {
        migrate()
    }

    /**
     * Migrate persisted preferences forward when their schema changes.
     * Runs once per upgrade, gated by KEY_PREFS_VERSION.
     */
    private fun migrate() {
        val current = mSharedPreferences.getInt(KEY_PREFS_VERSION, 0)
        if (current >= PREFS_VERSION) return

        Log.i("Preferences", "Migrating preferences from v$current to v$PREFS_VERSION")

        @Suppress("KotlinConstantConditions")
        if (current < 1 && isCalculationMethodSet) {
            // v1: collapse Karachi Shafi/Hanafi duplicate. Old order:
            //   0=EGYPTIAN, 1=KARACHI Shafi, 2=KARACHI Hanafi, 3=N. AMERICA, 4=MWL, 5=UMM_AL_QURA
            // New order:
            //   0=EGYPTIAN, 1=KARACHI, 2=N. AMERICA, 3=MWL, 4=UMM_AL_QURA
            // Old indices 0 and 1 stay; old indices >= 2 shift down by 1.
            val old = mSharedPreferences.getInt(KEY_CALCULATION_METHOD_INDEX, -1)
            if (old >= 2) {
                calculationMethodIndex = old - 1
            }
        }

        mSharedPreferences.edit { putInt(KEY_PREFS_VERSION, PREFS_VERSION) }
    }

    fun getNotificationMethod(time: Int): Int {
        val default =
            if (time == PrayerTime.SUNRISE.ordinal) NotificationMethod.NONE else NotificationMethod.DEFAULT
        return mSharedPreferences.getInt(KEY_NOTIFICATION_METHOD + time.toString(), default.ordinal)
    }

    fun setNotificationMethod(time: Int, method: Int) {
        mSharedPreferences.edit {
            putInt(KEY_NOTIFICATION_METHOD + time.toString(), method)
        }
    }

    var calculationMethodIndex: Int
        get() = mSharedPreferences.getInt(
            KEY_CALCULATION_METHOD_INDEX, CalculationMethodCatalog.DEFAULT_INDEX
        )
        set(index) {
            mSharedPreferences.edit {
                putInt(
                    KEY_CALCULATION_METHOD_INDEX, index
                )
            }
        }

    var location: Location
        get() {
            val location = Location("")
            // Kaaba coordinates: 21.4225° N, 39.8261° E
            location.latitude = mSharedPreferences.getFloat(
                KEY_LATITUDE, 21.4225f
            ).toDouble()
            location.longitude = mSharedPreferences.getFloat(
                KEY_LONGITUDE, 39.8261f
            ).toDouble()
            return location
        }
        set(location) {
            mSharedPreferences.edit {
                putFloat(
                    KEY_LATITUDE, location.latitude.toFloat()
                )
                putFloat(
                    KEY_LONGITUDE, location.longitude.toFloat()
                )
            }
        }

    val isLocationSet: Boolean
        get() = mSharedPreferences.contains(KEY_LATITUDE) &&
                mSharedPreferences.contains(KEY_LONGITUDE)

    val isCalculationMethodSet: Boolean
        get() = mSharedPreferences.contains(KEY_CALCULATION_METHOD_INDEX)

    var offsetMinutes: Int
        get() = mSharedPreferences.getInt(KEY_OFFSET_MINUTES, 0)
        set(minutes) {
            mSharedPreferences.edit {
                putInt(KEY_OFFSET_MINUTES, minutes)
            }
        }

    companion object {
        private const val PREFERENCE_FILENAME_SUFFIX = "_preferences"
        private const val KEY_CALCULATION_METHOD_INDEX = "calculation_method_index"
        private const val KEY_NOTIFICATION_METHOD = "notification_method_"
        private const val KEY_LATITUDE = "location_latitude"
        private const val KEY_LONGITUDE = "location_longitude"
        private const val KEY_OFFSET_MINUTES = "offset_minutes"
        private const val KEY_PREFS_VERSION = "prefs_version"

        // Bump and add a migrate() branch when the persistence schema changes.
        private const val PREFS_VERSION = 1

        @Volatile
        private var sPreferences: Preferences? = null
        fun getInstance(context: Context): Preferences {
            return sPreferences ?: synchronized(this) {
                sPreferences ?: Preferences(context.applicationContext).also { sPreferences = it }
            }
        }
    }
}
