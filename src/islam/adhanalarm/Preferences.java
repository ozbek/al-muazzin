package islam.adhanalarm;

import static net.sourceforge.jitl.astro.Location.DEFAULT_PRESSURE;
import static net.sourceforge.jitl.astro.Location.DEFAULT_SEA_LEVEL;
import static net.sourceforge.jitl.astro.Location.DEFAULT_TEMPERATURE;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import java.util.Arrays;
import java.util.Locale;

import islam.adhanalarm.util.LocaleManager;
import uz.efir.muazzin.Utils;

/**
 * Helper class to store and retrieve user settings to/from shared preferences
 * file TODO: add API documentation
 */
public class Preferences {

    private static final String PREFERENCE_FILENAME_SUFFIX = "_preferences";
    private static final String KEY_BASMALA = "key_bismillah_on_boot_up";
    private static final String KEY_CALCULATION_METHOD_INDEX = "calculation_method_index";
    private static final String KEY_NOTIFICATION_METHOD = "notification_method_";
    private static final String KEY_NOTIFICATION_CUSTOM_FILE = "notification_custom_file_";
    private static final String KEY_LATITUDE = "location_latitude";
    private static final String KEY_LONGITUDE = "location_longitude";
    private static final String KEY_ALTITUDE = "location_altitude";
    private static final String KEY_PRESSURE = "location_pressure";
    private static final String KEY_TEMPERATURE = "location_temperature";
    private static final String KEY_OFFSET_MINUTES = "offset_minutes";
    private static final String KEY_ROUNDING_METHOD_INDEX = "rounding_method_index";
    private static final String KEY_LOCALE = "key_locale";

    private final SharedPreferences mSharedPreferences;
    private static Preferences sPreferences;

    public static Preferences getInstance(Context context) {
        return sPreferences == null ? (sPreferences = new Preferences(context)) : sPreferences;
    }

    private Preferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(
                context.getPackageName().concat(PREFERENCE_FILENAME_SUFFIX), Context.MODE_PRIVATE);
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

    public String getLocale() {
        return mSharedPreferences.getString(KEY_LOCALE, LocaleManager.LOCALES[0]);
    }

    public void setLocale(String newLocale) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_LOCALE, newLocale);
        editor.apply();
    }

    public int getNotificationMethod(short time) {
        return mSharedPreferences.getInt(KEY_NOTIFICATION_METHOD.concat(Short.toString(time)),
                CONSTANT.SUNRISE == time ? CONSTANT.NOTIFICATION_NONE
                        : CONSTANT.NOTIFICATION_DEFAULT);
    }

    public void setNotificationMethod(short time, int method) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_NOTIFICATION_METHOD.concat(Short.toString(time)), method);
        editor.apply();
    }

    public String getCustomFilePath(short time) {
        return mSharedPreferences.getString(
                KEY_NOTIFICATION_CUSTOM_FILE.concat(Short.toString(time)), "");
    }

    public void setCustomFilePath(String newPath) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_NOTIFICATION_CUSTOM_FILE, newPath);
        editor.apply();
    }

    public int getCalculationMethodIndex() {
        return mSharedPreferences.getInt(KEY_CALCULATION_METHOD_INDEX,
                CONSTANT.DEFAULT_CALCULATION_METHOD);
    }

    public void setCalculationMethodIndex(int index) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_CALCULATION_METHOD_INDEX, index);
        editor.apply();
    }

    public float[] getLocation() {
        float[] location = new float[2];
        // Kaaba coordinates: 21.4225° N, 39.8261° E
        location[0] = mSharedPreferences.getFloat(KEY_LATITUDE, 21.4225f);
        location[1] = mSharedPreferences.getFloat(KEY_LONGITUDE, 39.8261f);
        return location;
    }

    public boolean isLocationSet() {
        return mSharedPreferences.contains(KEY_LATITUDE)
                && mSharedPreferences.contains(KEY_LONGITUDE);
    }

    public void setLocation(float latitude, float longitude) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putFloat(KEY_LATITUDE, latitude);
        editor.putFloat(KEY_LONGITUDE, longitude);
        editor.apply();
    }

    /**
     * @return float[0] = altitude; float[1] = pressure; float[2] = temperature;
     */
    public float[] getApt() {
        float[] apt = new float[3];
        apt[0] = mSharedPreferences.getFloat(KEY_ALTITUDE, (float) DEFAULT_SEA_LEVEL);
        apt[1] = mSharedPreferences.getFloat(KEY_PRESSURE, (float) DEFAULT_PRESSURE);
        apt[2] = mSharedPreferences.getFloat(KEY_TEMPERATURE, (float) DEFAULT_TEMPERATURE);
        return apt;
    }

    public void setApt(float altitude, float pressure, float temperature) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putFloat(KEY_ALTITUDE, altitude);
        editor.putFloat(KEY_PRESSURE, pressure);
        editor.putFloat(KEY_TEMPERATURE, temperature);
        editor.apply();
    }

    public int getRoundingMethodIndex() {
        return mSharedPreferences
                .getInt(KEY_ROUNDING_METHOD_INDEX, CONSTANT.DEFAULT_ROUNDING_INDEX);
    }

    public void setRoundingMethodIndex(int index) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_ROUNDING_METHOD_INDEX, index);
        editor.apply();
    }

    public int getOffsetMinutes() {
        return mSharedPreferences.getInt(KEY_OFFSET_MINUTES, 0);
    }

    public void setOffsetMinutes(int minutes) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(KEY_OFFSET_MINUTES, minutes);
        editor.apply();
    }

    public void initCalculationDefaults(Context context) {
        if (!mSharedPreferences.contains(KEY_CALCULATION_METHOD_INDEX)) {
            String country = Locale.getDefault().getISO3Country().toUpperCase(Locale.US);
            for (int i = 0; i < CONSTANT.CALCULATION_METHOD_COUNTRY_CODES.length; i++) {
                if (Arrays.asList(CONSTANT.CALCULATION_METHOD_COUNTRY_CODES[i]).contains(country)) {
                    setCalculationMethodIndex(i);
                    Utils.updateWidgets(context);
                    break;
                }
            }
        }

        if (!mSharedPreferences.contains(KEY_LATITUDE)
                || !mSharedPreferences.contains(KEY_LONGITUDE)) {
            Location currentLocation = getCurrentLocation(context);
            if (currentLocation == null) {
                throw new NullPointerException();
            }

            setLocation((float) currentLocation.getLatitude(),
                    (float) currentLocation.getLongitude());
            Utils.updateWidgets(context);
        }
    }

    public Location getCurrentLocation(Context context) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(true);

        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        try {
            Location currentLocation = locationManager.getLastKnownLocation(locationManager
                    .getBestProvider(criteria, true));
            if (currentLocation == null) {
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                currentLocation = locationManager.getLastKnownLocation(locationManager
                        .getBestProvider(criteria, true));
            }
            return currentLocation;
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public net.sourceforge.jitl.astro.Location getJitlLocation() {
        final float[] latLong = getLocation();
        net.sourceforge.jitl.astro.Location location = new net.sourceforge.jitl.astro.Location(
                latLong[0], latLong[1], Schedule.getGMTOffset(), 0);
        final float[] apt = getApt();
        location.setSeaLevel(apt[0]);
        location.setPressure(apt[1]);
        location.setTemperature(apt[2]);

        return location;
    }
}
