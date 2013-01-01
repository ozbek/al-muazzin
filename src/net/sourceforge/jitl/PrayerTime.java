package net.sourceforge.jitl;

/**
 * This class is an enumeration of possible prayer and other
 * useful events.
 *
 */
public class PrayerTime {
    private PrayerTime() {
    }
    /**
     * Fajr time
     */
    public final static PrayerTime FAJR = new PrayerTime();

    /**
     * Shurooq time
     */
    public final static PrayerTime SHUROOQ = new PrayerTime();

    /**
     * Thuhr time
     */
    public final static PrayerTime THUHR = new PrayerTime();

    /**
     * Assr time
     */
    public final static PrayerTime ASSR = new PrayerTime();

    /**
     * Maghrib time
     */
    public final static PrayerTime MAGHRIB = new PrayerTime();

    /**
     * Ishaa time
     */
    public final static PrayerTime ISHAA = new PrayerTime();

    /**
     * Imsaak time
     */
    public final static PrayerTime IMSAAK = new PrayerTime();

    /**
     * Next fajr time
     */
    public final static PrayerTime NEXTFAJR = new PrayerTime();

}
