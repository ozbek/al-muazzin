package uz.efir.muazzin;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.annotation.NonNull;

/**
 * Helper class to store and retrieve user settings to/from shared preferences
 */
public class Preferences {

    private static final String PREFERENCE_FILENAME_SUFFIX = "_preferences";
    private static final String KEY_BASMALA = "key_bismillah_on_boot_up";
    private static final String KEY_CALCULATION_METHOD_INDEX = "calculation_method_index";
    private static final String KEY_NOTIFICATION_METHOD = "notification_method_";
    private static final String KEY_LATITUDE = "location_latitude";
    private static final String KEY_LONGITUDE = "location_longitude";
    private static final String KEY_OFFSET_MINUTES = "offset_minutes";
    private static Preferences sPreferences;
    private final SharedPreferences mSharedPreferences;

    private Preferences(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(context.getPackageName().concat(PREFERENCE_FILENAME_SUFFIX), Context.MODE_PRIVATE);
    }

    public static Preferences getInstance(Context context) {
        return sPreferences == null ? (sPreferences = new Preferences(context)) : sPreferences;
    }

    public boolean dontNotifySunrise() {
        return CONSTANT.NOTIFICATION_NONE == getNotificationMethod(CONSTANT.SUNRISE);
    }

    public boolean getBasmalaEnabled() {
        return mSharedPreferences.getBoolean(KEY_BASMALA, false);
    }

    public void setBasmalaEnabled(boolean enabled) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(KEY_BASMALA, enabled);
        editor.apply();
    }

    public int getNotificationMethod(int time) {
        return mSharedPreferences.getInt(KEY_NOTIFICATION_METHOD.concat(Integer.toString(time)), CONSTANT.SUNRISE == time ? CONSTANT.NOTIFICATION_NONE : CONSTANT.NOTIFICATION_DEFAULT);
    }

    public void setNotificationMethod(short time, int method) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_NOTIFICATION_METHOD.concat(Short.toString(time)), method);
        editor.apply();
    }

    public int getCalculationMethodIndex() {
        return mSharedPreferences.getInt(KEY_CALCULATION_METHOD_INDEX, CONSTANT.DEFAULT_CALCULATION_METHOD);
    }

    public void setCalculationMethodIndex(int index) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_CALCULATION_METHOD_INDEX, index);
        editor.apply();
    }

    public Location getLocation() {
        Location location = new Location("");
        // Kaaba coordinates: 21.4225° N, 39.8261° E
        location.setLatitude(mSharedPreferences.getFloat(KEY_LATITUDE, 21.4225f));
        location.setLongitude(mSharedPreferences.getFloat(KEY_LONGITUDE, 39.8261f));
        return location;
    }

    public void setLocation(@NonNull Location location) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putFloat(KEY_LATITUDE, (float) location.getLatitude());
        editor.putFloat(KEY_LONGITUDE, (float) location.getLongitude());
        editor.apply();
    }

    public boolean isLocationSet() {
        return mSharedPreferences.contains(KEY_LATITUDE) && mSharedPreferences.contains(KEY_LONGITUDE);
    }

    public boolean isCalculationMethodSet() {
        return mSharedPreferences.contains(KEY_CALCULATION_METHOD_INDEX);
    }

    public int getOffsetMinutes() {
        return mSharedPreferences.getInt(KEY_OFFSET_MINUTES, 0);
    }

    public void setOffsetMinutes(int minutes) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_OFFSET_MINUTES, minutes);
        editor.apply();
    }
}
