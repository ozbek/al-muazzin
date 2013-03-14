package islam.adhanalarm;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sourceforge.jitl.Jitl;
import net.sourceforge.jitl.Method;
import net.sourceforge.jitl.Prayer;
import uz.efir.muazzin.R;
import android.content.Context;

public class Schedule {

    private GregorianCalendar[] schedule = new GregorianCalendar[7];
    private boolean[] extremes = new boolean[7];
    private fi.joensuu.joyds1.calendar.Calendar hijriDate;

    private static Schedule today;

    public Schedule(GregorianCalendar day) {
        Method method = CONSTANT.CALCULATION_METHODS[VARIABLE.settings.getInt("calculationMethodsIndex", CONSTANT.DEFAULT_CALCULATION_METHOD)].copy();
        method.setRound(CONSTANT.ROUNDING_TYPES[VARIABLE.settings.getInt("roundingTypesIndex", 2)]);

        net.sourceforge.jitl.astro.Location location = new net.sourceforge.jitl.astro.Location(VARIABLE.settings.getFloat("latitude", 43.67f), VARIABLE.settings.getFloat("longitude", -79.417f), getGMTOffset(), 0);
        location.setSeaLevel(VARIABLE.settings.getFloat("altitude", 0) < 0 ? 0 : VARIABLE.settings.getFloat("altitude", 0));
        location.setPressure(VARIABLE.settings.getFloat("pressure", 1010));
        location.setTemperature(VARIABLE.settings.getFloat("temperature", 10));

        Jitl itl = CONSTANT.DEBUG ? new DummyJitl(location, method) : new Jitl(location, method);
        Prayer[] dayPrayers = itl.getPrayerTimes(day).getPrayers();
        Prayer[] allTimes = new Prayer[]{dayPrayers[0], dayPrayers[1], dayPrayers[2], dayPrayers[3], dayPrayers[4], dayPrayers[5], itl.getNextDayFajr(day)};

        for(short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) { // Set the times on the schedule
            schedule[i] = new GregorianCalendar(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH), allTimes[i].getHour(), allTimes[i].getMinute(), allTimes[i].getSecond());
            schedule[i].add(Calendar.MINUTE, VARIABLE.settings.getInt("offsetMinutes", 0));
            extremes[i] = allTimes[i].isExtreme();
        }
        schedule[CONSTANT.NEXT_FAJR].add(Calendar.DAY_OF_MONTH, 1); // Next fajr is tomorrow

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
        if(now.before(schedule[CONSTANT.FAJR])) return CONSTANT.FAJR;
        for(short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
            if(now.after(schedule[i]) && now.before(schedule[i + 1])) {
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
        if(currentlyAfterSunset()) {
            addedDay = true;
            hijriDate.addDays(1);
        }
        String day = String.valueOf(hijriDate.getDay());
        String month = context.getResources().getStringArray(R.array.hijri_months)[hijriDate.getMonth() - 1];
        String year = String.valueOf(hijriDate.getYear());
        if(addedDay) {
            hijriDate.addDays(-1); // Revert to the day independent of sunset
        }
        return day + " " + month + ", " + year + " " + context.getResources().getString(R.string.anno_hegirae);
    }

    public static Schedule today() {
        GregorianCalendar now = new GregorianCalendar();
        if(today == null) {
            today = new Schedule(now);
        } else {
            GregorianCalendar fajr = today.getTimes()[CONSTANT.FAJR];
            if(fajr.get(Calendar.YEAR) != now.get(Calendar.YEAR) || fajr.get(Calendar.MONTH) != now.get(Calendar.MONTH) || fajr.get(Calendar.DAY_OF_MONTH) != now.get(Calendar.DAY_OF_MONTH)) {
                today = new Schedule(now);
            }
        }
        return today;
    }
    public static void setSettingsDirty() {
        today = null; // Nullifying causes a new today to be created with new settings when today() is called
    }
    public static boolean settingsAreDirty() {
        return today == null;
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