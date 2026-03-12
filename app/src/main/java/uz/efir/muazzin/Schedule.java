package uz.efir.muazzin;

import android.content.Context;
import android.location.Location;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Schedule {

    private final GregorianCalendar[] schedule = new GregorianCalendar[7];

    public Schedule(Context context, GregorianCalendar day) {
        Preferences preferences = Preferences.getInstance(context);
        CalculationMethod method = CONSTANT.CALCULATION_METHODS[preferences.getCalculationMethodIndex()];

        Location location = preferences.getLocation();
        Coordinates coordinates = new Coordinates(location.getLatitude(), location.getLongitude());

        DateComponents dateComponents = new DateComponents(day.get(Calendar.YEAR),
                day.get(Calendar.MONTH) + 1, day.get(Calendar.DAY_OF_MONTH));

        PrayerTimes prayerTimes = new PrayerTimes(coordinates, dateComponents, method.getParameters());

        Date[] allTimes = new Date[]{prayerTimes.fajr, prayerTimes.sunrise, prayerTimes.dhuhr, prayerTimes.asr, prayerTimes.maghrib, prayerTimes.isha};

        for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
            // Set the times on the schedule
            schedule[i] = new GregorianCalendar();
            schedule[i].setTime(allTimes[i]);
            schedule[i].add(Calendar.MINUTE, preferences.getOffsetMinutes());
        }

        // Calculate next Fajr
        GregorianCalendar nextDay = (GregorianCalendar) day.clone();
        nextDay.add(Calendar.DAY_OF_MONTH, 1);
        DateComponents nextDateComponents = new DateComponents(nextDay.get(Calendar.YEAR),
                nextDay.get(Calendar.MONTH) + 1, nextDay.get(Calendar.DAY_OF_MONTH));
        prayerTimes = new PrayerTimes(coordinates, nextDateComponents, method.getParameters());
        schedule[CONSTANT.NEXT_FAJR] = new GregorianCalendar();
        schedule[CONSTANT.NEXT_FAJR].setTime(prayerTimes.fajr);
        schedule[CONSTANT.NEXT_FAJR].add(Calendar.MINUTE, preferences.getOffsetMinutes());
    }

    public GregorianCalendar[] getTimes() {
        return schedule;
    }

    public short nextTimeIndex() {
        Calendar now = new GregorianCalendar();
        if (now.before(schedule[CONSTANT.FAJR])) return CONSTANT.FAJR;
        for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
            if (now.after(schedule[i]) && now.before(schedule[i + 1])) {
                return ++i;
            }
        }
        return CONSTANT.NEXT_FAJR;
    }

    private boolean currentlyAfterSunset() {
        Calendar now = new GregorianCalendar();
        return now.after(schedule[CONSTANT.MAGHRIB]);
    }

//    public String hijriDateToString(Context context) {
//        boolean addedDay = false;
//        if (currentlyAfterSunset()) {
//            addedDay = true;
//            hijriDate.addDays(1);
//        }
//        String day = String.valueOf(hijriDate.getDay());
//        String month = context.getResources().getStringArray(R.array.hijri_months)[hijriDate.getMonth() - 1];
//        String year = String.valueOf(hijriDate.getYear());
//        if (addedDay) {
//            hijriDate.addDays(-1); // Revert to the day independent of sunset
//        }
//        return context.getResources().getString(R.string.anno_hegirae, day, month, year);
//    }

    public static Schedule today(Context context) {
        return new Schedule(context, new GregorianCalendar());
    }

    public static double getGMTOffset() {
        Calendar now = new GregorianCalendar();
        int gmtOffset = now.getTimeZone().getOffset(now.getTimeInMillis());
        return (double) gmtOffset / 3600000.0;
    }

    public static boolean isDaylightSavings() {
        Calendar now = new GregorianCalendar();
        return now.getTimeZone().inDaylightTime(now.getTime());
    }
}
