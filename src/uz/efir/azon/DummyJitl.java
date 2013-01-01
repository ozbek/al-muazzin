package uz.efir.azon;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sourceforge.jitl.DayPrayers;
import net.sourceforge.jitl.Jitl;
import net.sourceforge.jitl.Method;
import net.sourceforge.jitl.Prayer;
import net.sourceforge.jitl.astro.Location;

public class DummyJitl extends Jitl {

    private final short START_PRAYER = CONSTANT.FAJR;
    private final int INC_HOUR = 0;
    private final int INC_MIN = 0;

    private DayPrayers dp = new DayPrayers();
    private Prayer[] times = new Prayer[7];

    public DummyJitl(Location loc, Method method) {
        super(loc, method);
        Calendar currentTime = Calendar.getInstance();

        for(short i = START_PRAYER; i <= CONSTANT.NEXT_FAJR; i++) {
            currentTime.add(Calendar.MINUTE, 1);
            currentTime.getTime().getMinutes();
            times[i] = new Prayer(currentTime.get(Calendar.HOUR_OF_DAY) + INC_HOUR, currentTime.get(Calendar.MINUTE) + INC_MIN, 0, false);
        }
        currentTime.add(Calendar.MINUTE, -(CONSTANT.NEXT_FAJR - START_PRAYER));
        for(short i = (short)(START_PRAYER - 1); i >= CONSTANT.FAJR; i--) {
            currentTime.add(Calendar.MINUTE, -1);
            times[i] = new Prayer(currentTime.get(Calendar.HOUR_OF_DAY) + INC_HOUR, currentTime.get(Calendar.MINUTE) + INC_MIN, 0, false);
        }
    }
    public DayPrayers getPrayerTimes(final GregorianCalendar date) {
        dp.fajr().setHour(times[0].getHour()); dp.fajr().setMinute(times[0].getMinute()); dp.fajr().setSecond(0);
        dp.shuruq().setHour(times[1].getHour()); dp.shuruq().setMinute(times[1].getMinute()); dp.shuruq().setSecond(0);
        dp.thuhr().setHour(times[2].getHour()); dp.thuhr().setMinute(times[2].getMinute()); dp.thuhr().setSecond(0);
        dp.assr().setHour(times[3].getHour()); dp.assr().setMinute(times[3].getMinute()); dp.assr().setSecond(0);
        dp.maghrib().setHour(times[4].getHour()); dp.maghrib().setMinute(times[4].getMinute()); dp.maghrib().setSecond(0);
        dp.ishaa().setHour(times[5].getHour()); dp.ishaa().setMinute(times[5].getMinute()); dp.ishaa().setSecond(0);
        return dp;
    }
    public Prayer getImsaak (final GregorianCalendar date) {
        return new Prayer(12, 12, 12, false);
    }

    public Prayer getNextDayImsaak (final GregorianCalendar date) {
        return new Prayer(12, 12, 12, false);
    }

    public Prayer getNextDayFajr (final GregorianCalendar date) {
        return times[6];
    }
}