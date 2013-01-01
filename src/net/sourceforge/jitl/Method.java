package net.sourceforge.jitl;

import net.sourceforge.jitl.astro.Utils;

public class Method {
    private double fajrAng;

    private double ishaaAng;

    private double imsaakAng;

    private int fajrInv;

    private int ishaaInv;

    private int imsaakInv;

    private Rounding round;

    private Mathhab mathhab;

    private double nearestLat;

    private ExtremeLatitude extremeLatitude;

    private boolean offset;

    private double fajrOffset;

    private double shurooqOffset;

    private double thuhrOffset;

    private double assrOffset;

    private double maghribOffset;

    private double ishaaOffset;

    public Method() {
    }

    /**
     * copy constructor
     * @param orig Method to copy from
     */
    public Method(Method orig) {
        this.fajrAng = orig.getFajrAng();
        this.ishaaAng = orig.getIshaaAng();
        this.imsaakAng = orig.getImsaakAng();
        this.fajrInv = orig.getFajrInv();
        this.ishaaInv = orig.getIshaaInv();
        this.imsaakInv = orig.getImsaakInv();
        this.round = orig.getRound();
        this.mathhab = orig.getMathhab();
        this.nearestLat = orig.getNearestLat();
        this.extremeLatitude = orig.getExtremeLatitude();
        this.offset = orig.getOffset();

        this.fajrOffset = orig.getFajrOffset();
        this.shurooqOffset = orig.getShurooqOffset();
        this.thuhrOffset = orig.getThuhrOffset();
        this.assrOffset = orig.getAssrOffset();
        this.maghribOffset = orig.getMaghribOffset();
        this.ishaaOffset = orig.getIshaaOffset();
    }

    /**
     * Build a method object containing all the information needed to compute
     * prayer time.
     *
     * @param fajrAng Fajr angle
     * @param ishaaAng Ishaa angle
     * @param imsaakAng The angle difference between Imsaak and Fajr (default
     *  is 1.5)
     * @param fajrInv Fajr Interval is the amount of minutes between Fajr and
     *  Shurooq (0 if not used)
     * @param ishaaInv Ishaa Interval is the amount if minutes between Ishaa and
     *  Maghrib (0 if not used)
     * @param imsaakInv Imsaak Interval is the amount of minutes between Imsaak
     *  and Fajr. The default is 10 minutes before Fajr if Fajr Interval is set
     * @param round Method used for rounding seconds
     * @param mathhab mathhab for calculating assr prayer shadow ratio
     * @param nearestLat Latitude Used for the 'Nearest Latitude' extreme
     *  methods. The default is 48.5
     * @param extreme Extreme latitude calculation method (@see ExtremeLatitude)
     * @param offset Enable Offsets switch (set this to true to activate). This
     *  option allows you to add or subtract any amount of minutes to the daily
     *  computed prayer times based on values (in minutes) for each prayer in
     *  the next xxxOffset parameters
     *  For Example: If you want to add 30 seconds to Maghrib and subtract 2
     *  minutes from Ishaa:<br />
     *   <code>method.setOffset(true);
     *   method.setMaghribOffset(0.5);
     *   method.setIshaaOffset(-2); </code>
     * @param fajrOffset fajr offset
     * @param shurooqOffset shurooq offset
     * @param thuhrOffset thuhr offset
     * @param assrOffset assr offset
     * @param maghribOffset maghrib offset
     * @param ishaaOffset ishaa offset
     */
    public Method(double fajrAng, double ishaaAng, double imsaakAng,
            int fajrInv, int ishaaInv, int imsaakInv, Rounding round, Mathhab mathhab,
            double nearestLat, ExtremeLatitude extreme, boolean offset, double fajrOffset,
            double shurooqOffset, double thuhrOffset, double assrOffset,
            double maghribOffset, double ishaaOffset) {
        this.fajrAng = fajrAng;
        this.ishaaAng = ishaaAng;
        this.imsaakAng = imsaakAng;
        this.fajrInv = fajrInv;
        this.ishaaInv = ishaaInv;
        this.imsaakInv = imsaakInv;
        this.round = round;
        this.mathhab = mathhab;
        this.nearestLat = nearestLat;
        this.extremeLatitude = extreme;
        this.offset = offset;

        this.fajrOffset = fajrOffset;
        this.shurooqOffset = shurooqOffset;
        this.thuhrOffset = thuhrOffset;
        this.assrOffset = assrOffset;
        this.maghribOffset = maghribOffset;
        this.ishaaOffset = ishaaOffset;

    }

    /**
     * copy constructor
     * @return a new instance of Method containing a clone of
     * the current instance
     */
    public Method copy() {
        return new Method(fajrAng, ishaaAng, imsaakAng, fajrInv, ishaaInv,
                imsaakInv, round, mathhab, nearestLat, extremeLatitude, offset,
                fajrOffset, shurooqOffset, thuhrOffset, assrOffset,
                maghribOffset, ishaaOffset);
    }

    public ExtremeLatitude getExtremeLatitude() {
        return extremeLatitude;
    }

    /**
     *
     * @param extreme Extreme latitude calculation method
     * @see ExtremeLatitude
     */
    public void setExtremeLatitude(ExtremeLatitude extreme) {
        this.extremeLatitude = extreme;
    }

    public double getFajrAng() {
        return fajrAng;
    }

    public void setFajrAng(double fajrAng) {
        this.fajrAng = fajrAng;
    }

    public int getFajrInv() {
        return fajrInv;
    }

    /**
     *
     * @param fajrInv Fajr Interval is the amount of minutes between Fajr and
     *  Shurooq (0 if not used)
     */
    public void setFajrInv(int fajrInv) {
        this.fajrInv = fajrInv;
    }

    public double getImsaakAng() {
        return imsaakAng;
    }

    /**
     *
     * @param imsaakAng The angle difference between Imsaak and Fajr (default
     *  is 1.5)
     */
    public void setImsaakAng(double imsaakAng) {
        this.imsaakAng = imsaakAng;
    }

    public int getImsaakInv() {
        return imsaakInv;
    }

    /**
     *
     * @param imsaakInv Imsaak Interval is the amount of minutes between Imsaak
     *  and Fajr. The default is 10 minutes before Fajr if Fajr Interval is set
     */
    public void setImsaakInv(int imsaakInv) {
        this.imsaakInv = imsaakInv;
    }

    public double getIshaaAng() {
        return ishaaAng;
    }

    public void setIshaaAng(double ishaaAng) {
        this.ishaaAng = ishaaAng;
    }

    public int getIshaaInv() {
        return ishaaInv;
    }

    /**
     *
     * @param ishaaInv Ishaa Interval is the amount if minutes between Ishaa and
     *  Maghrib (0 if not used)
     */
    public void setIshaaInv(int ishaaInv) {
        this.ishaaInv = ishaaInv;
    }

    public Mathhab getMathhab() {
        return mathhab;
    }

    /**
     *
     * @param mathhab mathhab for calculating assr prayer shadow ratio
     * @see Mathhab
     */
    public void setMathhab(Mathhab mathhab) {
        this.mathhab = mathhab;
    }

    public double getNearestLat() {
        return nearestLat;
    }

    /**
     *
     * @param nearestLat Latitude Used for the 'Nearest Latitude' extreme
     *  methods. The default is 48.5
     */
    public void setNearestLat(double nearestLat) {
        this.nearestLat = nearestLat;
    }

    public boolean getOffset() {
        return offset;
    }

    /**
     *
     * @param offset Enable Offsets switch (set this to true to activate). This
     *  option allows you to add or subtract any amount of minutes to the daily
     *  computed prayer times based on values (in minutes) for each prayer in
     *  the next xxxOffset parameters
     *  For Example: If you want to add 30 seconds to Maghrib and subtract 2
     *  minutes from Ishaa:
     *   <code>method.setOffset(true);
     *   method.setMaghribOffset(0.5);
     *   method.setIshaaOffset(-2); </code>
     */
    public void setOffset(boolean offset) {
        this.offset = offset;
    }

    public Rounding getRound() {
        return round;
    }

    /**
     *
     * @param round Method used for rounding seconds
     * @see Rounding
     */
    public void setRound(Rounding round) {
        this.round = round;
    }

    /*
     fajrAng, ishaaAng, imsaakAng, fajrInv = 0, ishaaInv = 0, imsaakInv = 0, round = 2, mathhab = 1,
     nearestLat =  Utils.DEF_NEAREST_LATITUDE , extreme = 5, offset = 0, offList = null
     */

    public static final Method NONE = new Method(0.0, 0.0, Utils.DEF_IMSAAK_ANGLE, 0, 0, 0, Rounding.SPECIAL, Mathhab.SHAAFI,
            Utils.DEF_NEAREST_LATITUDE, ExtremeLatitude.GOOD_INVALID, false, 0, 0, 0, 0, 0, 0);

    /**
     * Egyptian General Authority of Survey<br />
     * <ul>
     * <li>Fajr Angle      = 20</li>
     * <li>Ishaa Angle     = 18</li>
     * <li>Used in:
     *  Indonesia, Iraq, Jordan, Lebanon, Malaysia, Singapore, Syria, parts of Africa,
     *  parts of United States</li>
     *  </ul>
     */
    public static final Method EGYPT_SURVEY = new Method(20, 18, Utils.DEF_IMSAAK_ANGLE, 0, 0, 0, Rounding.SPECIAL,
            Mathhab.SHAAFI, Utils.DEF_NEAREST_LATITUDE, ExtremeLatitude.GOOD_INVALID, false, 0, 0, 0, 0, 0, 0);

    /**
     * University of Islamic Sciences, Karachi (Shaf'i)<br />
     * <ul><li>      Fajr Angle      = 18</li>
     *       <li>Ishaa Angle     = 18</li>
     *       <li>Used in:        Iran, Kuwait, parts of Europe
     *       </li></ul>
     */
    public static final Method KARACHI_SHAF = new Method(18, 18, Utils.DEF_IMSAAK_ANGLE, 0, 0, 0, Rounding.SPECIAL,
            Mathhab.SHAAFI, Utils.DEF_NEAREST_LATITUDE, ExtremeLatitude.GOOD_INVALID, false, 0, 0, 0, 0, 0, 0);

    /**
     *   University of Islamic Sciences, Karachi (Hanafi)<br />
     *   <ul><li>Fajr Angle      = 18</li>
     *   <li>Ishaa Angle     = 18</li>
     *   <li>Used in:        Afghanistan, Bangladesh, India</li>
     *   </ul>
     */
    public static final Method KARACHI_HANAF = new Method(18, 18, Utils.DEF_IMSAAK_ANGLE, 0, 0, 0,
            Rounding.SPECIAL, Mathhab.HANAFI, Utils.DEF_NEAREST_LATITUDE, ExtremeLatitude.GOOD_INVALID, false, 0, 0, 0, 0, 0, 0);

    /**
     *  Islamic Society of North America<br />
     *   <ul><li>Fajr Angle      = 15</li>
     *   <li>Ishaa Angle     = 15</li>
     *   <li>Used in:        Canada, Parts of UK, parts of United States</li>
     *   </ul>
     */
    public static final Method NORTH_AMERICA = new Method(15, 15, Utils.DEF_IMSAAK_ANGLE, 0, 0, 0,
            Rounding.SPECIAL, Mathhab.SHAAFI, Utils.DEF_NEAREST_LATITUDE, ExtremeLatitude.GOOD_INVALID, false, 0, 0, 0, 0, 0, 0);

    /**
     *   Muslim World League (MWL)<br />
     *   <ul><li>Fajr Angle      = 18</li>
     *   <li>Ishaa Angle     = 17</li>
     *   <li>Used in:        parts of Europe, Far East, parts of United States</li>
     *   </ul>
     *
     */
    public static final Method MUSLIM_LEAGUE = new Method(18, 17, Utils.DEF_IMSAAK_ANGLE, 0, 0, 0,
            Rounding.SPECIAL, Mathhab.SHAAFI, Utils.DEF_NEAREST_LATITUDE, ExtremeLatitude.GOOD_INVALID, false, 0, 0, 0, 0, 0, 0);

    /**
     *  Om Al-Qurra University<br />
     *   <ul><li>Fajr Angle      = 19</li>
     *   <li>Ishaa Angle     = 0 (not used)</li>
     *   <li>Ishaa Interval  = 90 minutes from Al-Maghrib prayer
     *                     but set to 120 during Ramadan.</li>
     *   <li>Used in:        Saudi Arabia</li>
     *   </ul>
     */
    public static final Method UMM_ALQURRA = new Method(19, 0, Utils.DEF_IMSAAK_ANGLE, 0, 90, 0, Rounding.SPECIAL,
            Mathhab.SHAAFI, Utils.DEF_NEAREST_LATITUDE, ExtremeLatitude.GOOD_INVALID, false, 0, 0, 0, 0, 0, 0);

    /**
     *   Fixed Ishaa Angle Interval (always 90)<br />
     *   <ul><li>Fajr Angle      = 19.5</li>
     *   <li>Ishaa Angle     = 0 (not used)</li>
     *   <li>Ishaa Interval  = 90 minutes from Al-Maghrib prayer.</li>
     *   <li>Used in:          Bahrain, Oman, Qatar, United Arab Emirates</li>
     *   </ul>
     */
    public static final Method FIXED_ISHAA = new Method(19.5, 0, Utils.DEF_IMSAAK_ANGLE, 0, 90, 0,
            Rounding.SPECIAL, Mathhab.SHAAFI, Utils.DEF_NEAREST_LATITUDE, ExtremeLatitude.GOOD_INVALID, false, 0, 0, 0, 0, 0, 0);

    public double getAssrOffset() {
        return assrOffset;
    }

    public void setAssrOffset(double assrOffset) {
        this.assrOffset = assrOffset;
    }

    public double getFajrOffset() {
        return fajrOffset;
    }

    public void setFajrOffset(double fajrOffset) {
        this.fajrOffset = fajrOffset;
    }

    public double getIshaaOffset() {
        return ishaaOffset;
    }

    public void setIshaaOffset(double ishaaOffset) {
        this.ishaaOffset = ishaaOffset;
    }

    public double getMaghribOffset() {
        return maghribOffset;
    }

    public void setMaghribOffset(double maghribOffset) {
        this.maghribOffset = maghribOffset;
    }

    public double getShurooqOffset() {
        return shurooqOffset;
    }

    public void setShurooqOffset(double shurooqOffset) {
        this.shurooqOffset = shurooqOffset;
    }

    public double getThuhrOffset() {
        return thuhrOffset;
    }

    public void setThuhrOffset(double thuhrOffset) {
        this.thuhrOffset = thuhrOffset;
    }

    public double getOffset(PrayerTime prayer) {
        if (prayer == PrayerTime.FAJR)
            return getFajrOffset();
        if (prayer == PrayerTime.SHUROOQ)
            return getShurooqOffset();
        if (prayer == PrayerTime.THUHR)
            return getThuhrOffset();
        if (prayer == PrayerTime.ASSR)
            return getAssrOffset();
        if (prayer == PrayerTime.MAGHRIB)
            return getMaghribOffset();
        if (prayer == PrayerTime.ISHAA)
            return getIshaaOffset();
        return 0;
    }

}
