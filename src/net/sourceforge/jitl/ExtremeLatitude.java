package net.sourceforge.jitl;

/**
 *
 * At certain locations and times of year, some prayer times do not occur
 * or otherwise are impossible to precisely calculate using conventional
 * means. These methods generally apply to locations with High latitudes
 * (near or above 49 degrees) or locations of Extreme proportion (near or
 * above 66 degrees).
 *
 * Method Category Information:<ul>
 *
 *         <li> Nearest Latitude (Aqrab Al-Bilaad): Calculate a prayer time
 *           using a safe latitude value. The recommended latitude by
 *           many schools of Fiqh is 48.5 degrees, but you can customize
 *           this by setting the "Method.setNearestLat()" method.
 *         </li>
 *         <li> Nearest Good Day (Aqrab Al-Ayyam): The library determines
 *           the closest previous or next day that the Fajr and Ishaa
 *           times occur and are both valid.
 *         </li>
 *         <li> An [amount] of Night and Day: Unlike the above mentioned
 *           methods, the multiple methods in this category have no proof
 *           in traditional Shari'a (Fiqh) resources. These methods were
 *           introduced by modern day Muslim scholars and scientists for
 *           practical reasons only.
 *         </li>
 *         <li> Minutes from Shurooq/Maghrib: Use an interval time to
 *           calculate Fajr and Ishaa. This will set the values of Fajr
 *           and Ishaa to the same as the computed Shurooq and Maghrib
 *           respectively, then add or subtract the amount of minutes
 *           found in the "Method.getFajrInv" and "Method.getIshaaInv"
 *           methods.
 *          </li>
 *          </ul>
 */
public class ExtremeLatitude {

    private ExtremeLatitude() {

    }

    /**
     * none. If unable to calculate, leave only the invalid prayer
     *           time as 99:99.
     */
    public static final ExtremeLatitude NONE_EX = new ExtremeLatitude();

    /**
     * Nearest Latitude: Apply to all prayer times always.
     */
    public static final ExtremeLatitude LAT_ALL = new ExtremeLatitude();

    /**
     * Nearest Latitude: Apply to Fajr and Ishaa times always.
     */
    public static final ExtremeLatitude LAT_ALWAYS = new ExtremeLatitude();

    /**
     * Nearest Latitude: Apply to Fajr and Ishaa times but only if
     *                       the library has detected that the current
     *                       Fajr or Ishaa time is invalid.
     */
    public static final ExtremeLatitude LAT_INVALID  = new ExtremeLatitude();

    /**
     * Nearest Good Day: Apply to all prayer times always.
     */
    public static final ExtremeLatitude GOOD_ALL = new ExtremeLatitude();

    /**
     * Nearest Good Day: Apply to Fajr and Ishaa times but only if
     *                       the library has detected that the current
     *                       Fajr or Ishaa time is invalid. This is the
     *                       default method. (Default)
     */
    public static final ExtremeLatitude GOOD_INVALID = new ExtremeLatitude();

    /**
     * 1/7th of Night: Apply to Fajr and Ishaa times always.
     */
    public static final ExtremeLatitude SEVEN_NIGHT_ALWAYS = new ExtremeLatitude();

    /**
     * 1/7th of Night: Apply to Fajr and Ishaa times but only if
     *                       the library has detected that the current
     *                       Fajr or Ishaa time is invalid.
     *
     */
    public static final ExtremeLatitude SEVEN_NIGHT_INVALID = new ExtremeLatitude();

    /**
     * 1/7th of Day: Apply to Fajr and Ishaa times always.
     */
    public static final ExtremeLatitude SEVEN_DAY_ALWAYS = new ExtremeLatitude();

    /**
     * 1/7th of Day: Apply to Fajr and Ishaa times but only if the
     *                       library has detected that the current Fajr
     *                       or Ishaa time is invalid.
     */
    public static final ExtremeLatitude SEVEN_DAY_INVALID = new ExtremeLatitude();

    /**
     * Half of the Night: Apply to Fajr and Ishaa times always.
     */
    public static final ExtremeLatitude HALF_ALWAYS = new ExtremeLatitude();

    /**
     * Half of the Night: Apply to Fajr and Ishaa times but only
     *                         if the library has detected that the
     *                         current Fajr or Ishaa time is
     *                         invalid.
     */
    public static final ExtremeLatitude HALF_INVALID = new ExtremeLatitude();

    /**
     * Minutes from Shorooq/Maghrib: Apply to Fajr and Ishaa times always.
     */
    public static final ExtremeLatitude MIN_ALWAYS = new ExtremeLatitude();

    /**
     * Minutes from Shorooq/Maghrib: Apply to Fajr and Ishaa times but only if
     *                       the library has detected that the
     *                       current Fajr or Ishaa time is invalid.
     *
     */
    public static final ExtremeLatitude MIN_INVALID = new ExtremeLatitude();

    /**
     * Nearest Good Day: Different good days for Fajr and Ishaa (Not
     * implemented)
     */
    public static final ExtremeLatitude GOOD_DIF = new ExtremeLatitude();


}
