package islam.adhanalarm;

import android.content.Context;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sourceforge.jitl.Jitl;
import net.sourceforge.jitl.Method;
import net.sourceforge.jitl.Prayer;
import uz.efir.muazzin.R;
import uz.efir.muazzin.Utils;

public class Schedule {

    private final GregorianCalendar[] schedule = new GregorianCalendar[7];
    private final boolean[] extremes = new boolean[7];
    private final fi.joensuu.joyds1.calendar.Calendar hijriDate;

    private static Schedule today;

    public Schedule(Context context, GregorianCalendar day) {
        Preferences preferences = Preferences.getInstance(context);
        Method method = CONSTANT.CALCULATION_METHODS[preferences.getCalculationMethodIndex()].copy();
        method.setRound(CONSTANT.ROUNDING_METHODS[preferences.getRoundingMethodIndex()]);

        net.sourceforge.jitl.astro.Location location = preferences.getJitlLocation();
        Jitl jitl = new Jitl(location, method);
        Prayer[] dayPrayers = jitl.getPrayerTimes(day).getPrayers();
        Prayer[] allTimes = new Prayer[]{dayPrayers[0], dayPrayers[1], dayPrayers[2], dayPrayers[3], dayPrayers[4], dayPrayers[5], jitl.getNextDayFajr(day)};

        for (short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) {
            // Set the times on the schedule
            schedule[i] = new GregorianCalendar(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH), allTimes[i].getHour(), allTimes[i].getMinute(), allTimes[i].getSecond());
            schedule[i].add(Calendar.MINUTE, preferences.getOffsetMinutes());
            extremes[i] = allTimes[i].isExtreme();
        }
        schedule[CONSTANT.NEXT_FAJR].add(Calendar.DAY_OF_MONTH, 1/* next fajr is tomorrow */);

        hijriDate = new fi.joensuu.joyds1.calendar.IslamicCalendar();
    }

    public GregorianCalendar[] getTimes() {
        return schedule;
    }

    public boolean isExtreme(int i) {
        return extremes[i];
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

    public String hijriDateToString(Context context) {
        boolean addedDay = false;
        if (currentlyAfterSunset()) {
            addedDay = true;
            hijriDate.addDays(1);
        }
        String day = String.valueOf(hijriDate.getDay());
        String month = context.getResources().getStringArray(R.array.hijri_months)[hijriDate.getMonth() - 1];
        String year = String.valueOf(hijriDate.getYear());
        if (addedDay) {
            hijriDate.addDays(-1); // Revert to the day independent of sunset
        }
        return day + " " + month + ", " + year + " " + context.getResources().getString(R.string.anno_hegirae);
    }

    public static Schedule today(Context context) {
        GregorianCalendar now = new GregorianCalendar();
        if (today == null) {
            today = new Schedule(context, now);
        } else {
            GregorianCalendar fajr = today.getTimes()[CONSTANT.FAJR];
            if (fajr.get(Calendar.YEAR) != now.get(Calendar.YEAR)
                    || fajr.get(Calendar.MONTH) != now.get(Calendar.MONTH)
                    || fajr.get(Calendar.DAY_OF_MONTH) != now.get(Calendar.DAY_OF_MONTH)) {
                today = new Schedule(context, now);
            }
        }
        return today;
    }

    public static void setSettingsDirty() {
        // Force re-instantiation of new today
        today = null;
        Utils.isRestartNeeded = true;
    }

    public static double getGMTOffset() {
        Calendar now = new GregorianCalendar();
        int gmtOffset = now.getTimeZone().getOffset(now.getTimeInMillis());
        return gmtOffset / 3600000;
    }

    public static boolean isDaylightSavings() {
        Calendar now = new GregorianCalendar();
        return now.getTimeZone().inDaylightTime(now.getTime());
    }
}
