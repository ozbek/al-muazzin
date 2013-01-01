package uz.efir.azon;

import uz.efir.azon.widget.NextNotificationWidgetProvider;
import uz.efir.azon.widget.TimetableWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

public class VARIABLE {

    public static Context context;
    public static SharedPreferences settings;
    public static boolean mainActivityIsRunning = false;

    public static float qiblaDirection = 0;

    public static Location getCurrentLocation(Context context) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(true);

        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        Location currentLocation = null;
        try {
            currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
            if(currentLocation == null) {
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
            }
        } catch(Exception ex) {
            // GPS and wireless networks are disabled
        }
        return currentLocation;
    }

    private VARIABLE() {
        // Private constructor to enforce un-instantiability.
    }

    public static boolean alertSunrise() {
        if(settings == null) return false;
        return settings.getInt("notificationMethod" + CONSTANT.SUNRISE, CONSTANT.NOTIFICATION_NONE) != CONSTANT.NOTIFICATION_NONE;
    }

    public static void updateWidgets(Context context) {
        TimetableWidgetProvider.setLatestTimetable(context);
        NextNotificationWidgetProvider.setNextTime(context);
    }
}