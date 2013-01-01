package net.sourceforge.jitl;

import java.util.GregorianCalendar;

import net.sourceforge.jitl.astro.Astro;
import net.sourceforge.jitl.astro.AstroLib;
import net.sourceforge.jitl.astro.SimpleDate;
import net.sourceforge.jitl.astro.Dms;
import net.sourceforge.jitl.astro.Location;
import net.sourceforge.jitl.astro.Utils;

/**
 * This the main class of the JITL library. You can use static methods
 * to do qibla calculation but you will need to create a Jitl instance
 * to calculate prayer times for a specific location, using a specified method.
 *
 */
public class Jitl {

    /**
     * minor version of jitl
     */
    static final int VERSION_MINOR = 0;

    /**
     * major version of jitl
     */
    static final int VERSION_MAJOR = 1;


    /* This is Used for storing some formulae results between
     * multiple getPrayerTimes calls*/
    private Astro astroCache = new Astro();


    private Location loc;

    private Method method;


    /**
     * Creates the jitl main class
     * @param loc the location
     * @param method the method used in the calculation. You can use
     *  predefined methods for example <code>Method.MUSLIM_LEAGUE</code> or creates
     *  your own personalized method.
     */
    public Jitl(Location loc, Method method) {
        this.loc = loc;
        this.method = method;
    }

    /**
     * changes the location
     * @param loc the new location
     */
    public void setLocation(Location loc) {
        this.loc = loc;
    }

    /**
     * changes the method
     * @param method the new method
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * generates prayer times
     * @param date the date of prayers
     * @return a DayPrayers object containing time of different
     * prayers
     */
    public DayPrayers getPrayerTimes(final GregorianCalendar date) {
        return getPrayerTimes(new SimpleDate(date));
    }

    /**
     * Generate prayer times from a GregorianCalendar date
     * @param date the date of prayers
     * @param pt instance of a DayPrayers object
     */
    public void getPrayerTimes(final GregorianCalendar date, DayPrayers pt) {
        getPrayerTimes(new SimpleDate(date), pt);
    }

    /**
     * Create a DayPrayers instance and fill it with prayer times
     * @param date SimpleDate object
     * @return a DayPrayers instance containing prayer times
     */
    public DayPrayers getPrayerTimes(final SimpleDate date) {
        DayPrayers dp = new DayPrayers();
        getPrayerTimes(date, dp);

        return dp;
    }

    /**
     * Generate prayer times from a SimpleDate date
     * @param date SimpleDate object
     * @param pt instance of a DayPrayers object
     */
    public void getPrayerTimes(final SimpleDate date, DayPrayers pt) {
        DayCouple dc;

        dc = getDayInfo(date, loc.getGmtDiff());
        getPrayerTimesByDay(dc, pt, PrayerTime.FAJR);
    }

    void getPrayerTimesByDay(DayCouple dc, DayPrayers pt, PrayerTime type) {
        getPrayerTimesByDay(method, dc, pt, type);
    }

    void getPrayerTimesByDay(Method method, DayCouple dc, DayPrayers pt,
            PrayerTime type) {
        int i, invalid;
        double th, sh, mg, fj, is, ar;
        double lat, lon, dec;
        double tempPrayer[] = new double[6];
        Astro tAstro = new Astro();

        lat = loc.getDegreeLat();
        lon = loc.getDegreeLong();
        invalid = 0;

        /* Start by filling the tAstro structure with the appropriate astronomical
         * values for this day. We also pass the cache structure to update and check
         * if the actual values are already available. */
        AstroLib
        .getAstroValuesByDay(dc.getJulianDay(), loc, astroCache, tAstro);
        dec = Utils.DEG_TO_RAD(tAstro.getDec()[1]);

        /* Get Prayer Times formulae results for this day of year and this
         * location. The results are NOT the actual prayer times */
        fj = getFajIsh(lat, dec, method.getFajrAng());
        sh = getShoMag(loc, tAstro, PrayerTime.SHUROOQ);
        th = getThuhr(lon, tAstro);
        ar = getAssr(lat, dec, method.getMathhab());
        mg = getShoMag(loc, tAstro, PrayerTime.MAGHRIB);
        is = getFajIsh(lat, dec, method.getIshaaAng());

        /* Calculate all prayer times as Base-10 numbers in Normal circumstances */
        /* Fajr */
        if (fj == 99) {
            tempPrayer[0] = 99;
            invalid = 1;
        } else {
            tempPrayer[0] = th - fj;
        }

        if (sh == 99) {
            invalid = 1;
        }

        tempPrayer[1] = sh;
        tempPrayer[2] = th;
        tempPrayer[3] = th + ar;
        tempPrayer[4] = mg;

        if (mg == 99) {
            invalid = 1;
        }

        /* Ishaa */
        if (is == 99) {
            tempPrayer[5] = 99;
            invalid = 1;
        } else {
            tempPrayer[5] = th + is;
        }

        /* Calculate all prayer times as Base-10 numbers in Extreme Latitudes (if
         * needed) */

        /* Reset status of extreme switches */
        pt.setAllExtreme(false);

        if ((method.getExtremeLatitude() != ExtremeLatitude.NONE_EX)
                && !((method.getExtremeLatitude() == ExtremeLatitude.GOOD_INVALID
                        || method.getExtremeLatitude() == ExtremeLatitude.LAT_INVALID
                        || method.getExtremeLatitude() == ExtremeLatitude.SEVEN_NIGHT_INVALID
                        || method.getExtremeLatitude() == ExtremeLatitude.SEVEN_DAY_INVALID || method
                        .getExtremeLatitude() == ExtremeLatitude.HALF_INVALID) && (invalid == 0))) {
            double exdecPrev, exdecNext;
            double exTh = 99, exFj = 99, exIs = 99, exAr = 99, exSh = 99, exMg = 99;
            //exIm=99

            double portion = 0;
            double nGoodDay = 0;
            int exinterval = 0;
            Location exLoc = loc.copy();
            Astro exAstroPrev;
            Astro exAstroNext;
            ExtremeLatitude ext = method.getExtremeLatitude();

            /* Nearest Latitude (Method.nearestLat) */
            if(ext == ExtremeLatitude.LAT_ALL || ext == ExtremeLatitude.LAT_ALWAYS || ext == ExtremeLatitude.LAT_INVALID) {
            /*
            case LAT_ALL:
            case LAT_ALWAYS:
            case LAT_INVALID:
            */

                /* xxxthamer: we cannot compute this when interval is set because
                 * angle==0 . Only the if-invalid methods would work */
                exLoc.setDegreeLat(method.getNearestLat());
                exFj = getFajIsh(method.getNearestLat(), dec, method
                        .getFajrAng());
                //exIm = getFajIsh(method.getNearestLat(), dec, method.getImsaakAng());
                exIs = getFajIsh(method.getNearestLat(), dec, method
                        .getIshaaAng());
                exAr = getAssr(method.getNearestLat(), dec, method.getMathhab());
                exSh = getShoMag(exLoc, tAstro, PrayerTime.SHUROOQ);
                exMg = getShoMag(exLoc, tAstro, PrayerTime.MAGHRIB);

                //switch (ext) {
                if(ext == ExtremeLatitude.LAT_ALL) {
                    //case LAT_ALL:
                    tempPrayer[0] = th - exFj;
                    tempPrayer[1] = exSh;
                    tempPrayer[3] = th + exAr;
                    tempPrayer[4] = exMg;
                    tempPrayer[5] = th + exIs;
                    pt.setAllExtreme(true);
                } else if (ext == ExtremeLatitude.LAT_ALWAYS) {
                    tempPrayer[0] = th - exFj;
                    tempPrayer[5] = th + exIs;
                    pt.fajr().setExtreme(true);
                    pt.ishaa().setExtreme(true);
                } else if (ext == ExtremeLatitude.LAT_INVALID) {
                    if (tempPrayer[0] == 99) {
                        tempPrayer[0] = th - exFj;
                        pt.fajr().setExtreme(true);
                    }
                    if (tempPrayer[5] == 99) {
                        tempPrayer[5] = th + exIs;
                        pt.ishaa().setExtreme(true);
                    }
                }

            } else if (ext == ExtremeLatitude.GOOD_ALL || ext == ExtremeLatitude.GOOD_INVALID
                    || ext == ExtremeLatitude.GOOD_DIF) {
                /* Nearest Good Day */
                exAstroPrev = astroCache;
                exAstroNext = astroCache;

                /* Start by getting last or next nearest Good Day */
                for (i = 0; i <= dc.getLastDay(); i++) {

                    /* last closest day */
                    nGoodDay = dc.getJulianDay() - i;
                    AstroLib.getAstroValuesByDay(nGoodDay, loc, exAstroPrev,
                            tAstro);
                    exdecPrev = Utils.DEG_TO_RAD(tAstro.getDec()[1]);
                    exFj = getFajIsh(lat, exdecPrev, method.getFajrAng());

                    if (exFj != 99) {
                        exIs = getFajIsh(lat, exdecPrev, method.getIshaaAng());
                        if (exIs != 99) {
                            exTh = getThuhr(lon, tAstro);
                            exSh = getShoMag(loc, tAstro, PrayerTime.SHUROOQ);
                            exMg = getShoMag(loc, tAstro, PrayerTime.MAGHRIB);
                            exAr = getAssr(lat, exdecPrev, method.getMathhab());
                            break;
                        }
                    }

                    /* Next closest day */
                    nGoodDay = dc.getJulianDay() + i;
                    AstroLib.getAstroValuesByDay(nGoodDay, loc, exAstroNext,
                            tAstro);
                    exdecNext = Utils.DEG_TO_RAD(tAstro.getDec()[1]);
                    exFj = getFajIsh(lat, exdecNext, method.getFajrAng());
                    if (exFj != 99) {
                        exIs = getFajIsh(lat, exdecNext, method.getIshaaAng());
                        if (exIs != 99) {
                            exTh = getThuhr(lon, tAstro);
                            exSh = getShoMag(loc, tAstro, PrayerTime.SHUROOQ);
                            exMg = getShoMag(loc, tAstro, PrayerTime.MAGHRIB);
                            exAr = getAssr(lat, exdecNext, method.getMathhab());
                            break;
                        }
                    }
                }

                if(ext == ExtremeLatitude.GOOD_ALL) {
                    tempPrayer[0] = exTh - exFj;
                    tempPrayer[1] = exSh;
                    tempPrayer[2] = exTh;
                    tempPrayer[3] = exTh + exAr;
                    tempPrayer[4] = exMg;
                    tempPrayer[5] = exTh + exIs;
                    pt.setAllExtreme(true);

                } else if (ext == ExtremeLatitude.GOOD_INVALID) {
                    if (tempPrayer[0] == 99) {
                        tempPrayer[0] = exTh - exFj;
                        pt.fajr().setExtreme(true);
                    }
                    if (tempPrayer[5] == 99) {
                        tempPrayer[5] = exTh + exIs;
                        pt.ishaa().setExtreme(true);
                    }
                } else if (ext == ExtremeLatitude.GOOD_DIF) {
                    /* Nearest Good Day: Different good days for Fajr and Ishaa (Not
                     * implemented) */
                }
            } else if (ext == ExtremeLatitude.SEVEN_NIGHT_ALWAYS || ext == ExtremeLatitude.SEVEN_NIGHT_INVALID
                    || ext == ExtremeLatitude.SEVEN_DAY_ALWAYS || ext == ExtremeLatitude.SEVEN_DAY_INVALID
                    || ext == ExtremeLatitude.HALF_ALWAYS || ext == ExtremeLatitude.HALF_INVALID) {


                /* xxxthamer: For clarity, we may need to move the HALF_* methods
                 * into their own separate case statement. */

                if(ext == ExtremeLatitude.SEVEN_NIGHT_ALWAYS || ext == ExtremeLatitude.SEVEN_NIGHT_INVALID) {
                    portion = (24 - (tempPrayer[4] - tempPrayer[1]))
                    * (1 / 7.0);
                } else if (ext == ExtremeLatitude.SEVEN_DAY_ALWAYS || ext == ExtremeLatitude.SEVEN_DAY_INVALID) {
                    portion = (tempPrayer[4] - tempPrayer[1]) * (1 / 7.0);
                } else if (ext == ExtremeLatitude.HALF_ALWAYS || ext == ExtremeLatitude.HALF_INVALID) {
                    portion = (24 - tempPrayer[4] - tempPrayer[1]) * (1 / 2.0);
                }

                if (method.getExtremeLatitude() == ExtremeLatitude.SEVEN_NIGHT_INVALID
                        || method.getExtremeLatitude() == ExtremeLatitude.SEVEN_DAY_INVALID
                        || method.getExtremeLatitude() == ExtremeLatitude.HALF_INVALID) {
                    if (tempPrayer[0] == 99) {
                        if (method.getExtremeLatitude() == ExtremeLatitude.HALF_INVALID)
                            tempPrayer[0] = portion
                            - (method.getFajrInv() / 60.0);
                        else
                            tempPrayer[0] = tempPrayer[1] - portion;
                        pt.fajr().setExtreme(true);
                    }
                    if (tempPrayer[5] == 99) {
                        if (method.getExtremeLatitude() == ExtremeLatitude.HALF_INVALID)
                            tempPrayer[5] = portion
                            + (method.getIshaaInv() / 60.0);
                        else
                            tempPrayer[5] = tempPrayer[4] + portion;
                        pt.ishaa().setExtreme(true);
                    }
                } else { /* for the always methods */

                    if (method.getExtremeLatitude() == ExtremeLatitude.HALF_ALWAYS) {
                        tempPrayer[0] = portion - (method.getFajrInv() / 60.0);
                        tempPrayer[5] = portion + (method.getIshaaInv() / 60.0);
                    }

                    else {
                        tempPrayer[0] = tempPrayer[1] - portion;
                        tempPrayer[5] = tempPrayer[4] + portion;
                    }
                    pt.fajr().setExtreme(true);
                    pt.ishaa().setExtreme(true);
                }
            } else if (ext == ExtremeLatitude.MIN_ALWAYS) {
                /* Do nothing here because this is implemented through fajrInv and
                 * ishaaInv structure members */
                tempPrayer[0] = tempPrayer[1];
                tempPrayer[5] = tempPrayer[4];
                pt.fajr().setExtreme(true);
                pt.ishaa().setExtreme(true);
            } else if (ext == ExtremeLatitude.MIN_INVALID) {
                if (tempPrayer[0] == 99) {
                    exinterval = (int) ((double) method.getFajrInv() / 60.0);
                    tempPrayer[0] = tempPrayer[1] - exinterval;
                    pt.fajr().setExtreme(true);
                }
                if (tempPrayer[5] == 99) {
                    exinterval = (int) ((double) method.getIshaaInv() / 60.0);
                    tempPrayer[5] = tempPrayer[4] + exinterval;
                    pt.ishaa().setExtreme(true);
                }
            } /* end switch */
        } /* end extreme */

        /* Apply intervals if set */
        if (method.getExtremeLatitude() != ExtremeLatitude.MIN_INVALID
                && method.getExtremeLatitude() != ExtremeLatitude.HALF_INVALID
                && method.getExtremeLatitude() != ExtremeLatitude.HALF_ALWAYS) {
            if (method.getFajrInv() != 0)
                tempPrayer[0] = tempPrayer[1] - (method.getFajrInv() / 60.0);
            if (method.getIshaaInv() != 0)
                tempPrayer[5] = tempPrayer[4] + (method.getIshaaInv() / 60.0);
        }

        /* Final Step: Fill the Prayer array by doing decimal degree to
         * Prayer structure conversion*/
        if (type == PrayerTime.IMSAAK || type == PrayerTime.NEXTFAJR) {
            base6hm(tempPrayer[0], method, pt.fajr(), type);
        } else {
            Prayer[] pArray = pt.getPrayers();
            PrayerTime[] timeArray = { PrayerTime.FAJR, PrayerTime.SHUROOQ,
                    PrayerTime.THUHR, PrayerTime.ASSR, PrayerTime.MAGHRIB,
                    PrayerTime.ISHAA };

            for (i = 0; i < 6; i++) {
                base6hm(tempPrayer[i], method, pArray[i], timeArray[i]);
            }
        }

    }

    void base6hm(double bs, Method method, Prayer pt, PrayerTime type) {
        double min, sec;

        if (bs == 99) {
            pt.setHour(99);
            pt.setMinute(99);
            pt.setSecond(0);
            return;
        }

        /* Add offsets */
        if (method.getOffset()) {
            if (type == PrayerTime.IMSAAK || type == PrayerTime.NEXTFAJR)
                bs += (method.getFajrOffset() / 60.0);
            else
                bs += (method.getOffset(type) / 60.0);
        }

        /* Fix after minus offsets before midnight */
        if (bs < 0) {
            while (bs < 0)
                bs = 24 + bs;
        }

        min = (bs - Math.floor(bs)) * 60;
        sec = (min - Math.floor(min)) * 60;

        /* Add rounding minutes */
        if (method.getRound() == Rounding.NORMAL) {
            if (sec >= Utils.DEFAULT_ROUND_SEC)
                bs += 1 / 60.0;
            /* compute again */
            min = (bs - Math.floor(bs)) * 60;
            sec = 0;

        } else if (method.getRound() == Rounding.SPECIAL || method.getRound() == Rounding.AGRESSIVE) {
            if (type == PrayerTime.FAJR || type == PrayerTime.THUHR
                    || type == PrayerTime.ASSR || type == PrayerTime.MAGHRIB
                    || type == PrayerTime.ISHAA || type == PrayerTime.NEXTFAJR) {
                if (method.getRound() == Rounding.SPECIAL) {
                    if (sec >= Utils.DEFAULT_ROUND_SEC) {
                        bs += 1 / 60.0;
                        min = (bs - Math.floor(bs)) * 60;
                    }
                } else if (method.getRound() == Rounding.AGRESSIVE) {
                    if (sec >= Utils.AGGRESSIVE_ROUND_SEC) {
                        bs += 1 / 60.0;
                        min = (bs - Math.floor(bs)) * 60;
                    }
                }
                sec = 0;
            } else {
                //case Prayer.SHUROOQ:
                //case Prayer.IMSAAK:
                sec = 0;
            }
        }

        /* Add daylight saving time and fix after midnight times */
        bs += loc.getDst();
        if (bs >= 24) {
            bs = Math.IEEEremainder(bs, 24);
        }

        pt.setHour((int) bs);
        pt.setMinute((int) min);
        pt.setSecond((int) sec);

    }

    /**
     * Generate imsaak time
     * @param date GregorianCalendar date
     * @return imsaak time
     */
    public Prayer getImsaak(GregorianCalendar date) {
        return getImsaak(new SimpleDate(date));
    }

    /**
     * Generate imsaak time
     * @param date SimpleDate date
     * @return imsaak time
     */
    public Prayer getImsaak(SimpleDate date) {

        Method tmpConf;
        DayCouple dc;
        DayPrayers temp = new DayPrayers();

        tmpConf = method.copy();

        if (method.getFajrInv() != 0) {
            if (method.getImsaakInv() == 0)
                tmpConf
                .setFajrInv((int) (tmpConf.getFajrInv() + Utils.DEF_IMSAAK_INTERVAL));
            else
                tmpConf.setFajrInv((int) (tmpConf.getFajrInv() + method
                        .getImsaakInv()));

        } else if (method.getImsaakInv() != 0) {
            /* use an inv even if al-Fajr is computed (Indonesia?) */
            tmpConf.setFajrOffset(tmpConf.getFajrOffset()
                    + (method.getImsaakInv() * -1));
            tmpConf.setOffset(true);
        } else {
            tmpConf.setFajrAng(tmpConf.getFajrAng() + method.getImsaakAng());
        }

        dc = getDayInfo(date, loc.getGmtDiff());
        getPrayerTimesByDay(tmpConf, dc, temp, PrayerTime.IMSAAK);

        /* xxxthamer: We probably need to check whether it's possible to compute
         * Imsaak normally for some extreme methods first */
        /* In case of an extreme Fajr time calculation use intervals for Imsaak and
         * compute again */
        if (temp.fajr().isExtreme()) {
            tmpConf = method.copy();
            if (method.getImsaakInv() == 0) {
                tmpConf.setFajrOffset(tmpConf.getFajrOffset()
                        - Utils.DEF_IMSAAK_INTERVAL);
                tmpConf.setOffset(true);
            } else {
                tmpConf.setFajrOffset(tmpConf.getFajrOffset()
                        - method.getImsaakInv());
                tmpConf.setOffset(true);
            }
            getPrayerTimesByDay(tmpConf, dc, temp, PrayerTime.IMSAAK);
        }

        return temp.fajr();
    }

    /**
     * Generate next day imsaak time
     * @param date GregorianCalendar date
     * @return next day imsaak time
     */
    public Prayer getNextDayImsaak(GregorianCalendar date) {
        return getNextDayImsaak(new SimpleDate(date));
    }

    /**
     * Generate next day imsaak time
     * @param date SimpleDate date
     * @return next day imsaak time
     */
    public Prayer getNextDayImsaak(SimpleDate date) {
        /* Copy the date structure and increment for next day.*/
        SimpleDate tempd = date.copy();
        tempd.setDay(tempd.getDay() + 1);

        return getImsaak(tempd);
    }

    /**
     * Generate next day fajr time
     * @param date GregorianCalendar date
     * @return next day fajr time
     */
    public Prayer getNextDayFajr(GregorianCalendar date) {
        return getNextDayFajr(new SimpleDate(date));
    }

    /**
     * Generate next day fajr time
     * @param date SimpleDate date
     * @return next day fajr time
     */
    public Prayer getNextDayFajr(SimpleDate date) {

        DayPrayers temp = new DayPrayers();
        DayCouple dc;

        dc = getDayInfo(date, loc.getGmtDiff());
        dc.setJulianDay(dc.getJulianDay() + 1);
        getPrayerTimesByDay(dc, temp, PrayerTime.NEXTFAJR);
        return temp.fajr().copy();
    }

    static double getFajIsh(double Lat, double dec, double Ang) {

        double part1 = Math.cos(Utils.DEG_TO_RAD(Lat)) * Math.cos(dec);
        double part2 = -Math.sin(Utils.DEG_TO_RAD(Ang))
        - Math.sin(Utils.DEG_TO_RAD(Lat)) * Math.sin(dec);

        double part3 = part2 / part1;
        if (part3 <= Utils.INVALID_TRIGGER) {
            return 99;
        }

        return Utils.DEG_TO_10_BASE * Utils.RAD_TO_DEG(Math.acos(part3));

    }

    static double getShoMag(Location loc, Astro astro, PrayerTime type) {
        double lhour, M, sidG, ra0 = astro.getRa()[0], ra2 = astro.getRa()[2];
        double A, B, H, sunAlt, R, tH;

        double part1 = Math.sin(Utils.DEG_TO_RAD(loc.getDegreeLat()))
        * Math.sin(Utils.DEG_TO_RAD(astro.getDec()[1]));
        double part2a = Utils.CENTER_OF_SUN_ANGLE;
        double part2 = Math.sin(Utils.DEG_TO_RAD(part2a)) - part1;
        double part3 = Math.cos(Utils.DEG_TO_RAD(loc.getDegreeLat()))
        * Math.cos(Utils.DEG_TO_RAD(astro.getDec()[1]));

        double part4 = part2 / part3;

        if (part4 <= -1 || part4 >= 1)
            return 99;

        lhour = AstroLib.limitAngle180((Utils.RAD_TO_DEG(Math.acos(part4))));
        M = ((astro.getRa()[1] - loc.getDegreeLong() - astro.getSid()[1]) / 360.0);

        if (type == PrayerTime.SHUROOQ)
            M = M - (lhour / 360.0);
        if (type == PrayerTime.MAGHRIB)
            M = M + (lhour / 360.0);

        M = AstroLib.limitAngle111(M);

        sidG = AstroLib.limitAngle(astro.getSid()[1] + 360.985647 * M);

        ra0 = astro.getRa()[0];
        ra2 = astro.getRa()[2];

        if (astro.getRa()[1] > 350 && astro.getRa()[2] < 10)
            ra2 += 360;
        if (astro.getRa()[0] > 350 && astro.getRa()[1] < 10)
            ra0 = 0;

        A = astro.getRa()[1]
                          + (M
                                  * ((astro.getRa()[1] - ra0) + (ra2 - astro.getRa()[1]) + ((ra2 - astro
                                          .getRa()[1]) - (astro.getRa()[1] - ra0))
                                          * M) / 2.0);

        B = astro.getDec()[1]
                           + (M
                                   * ((astro.getDec()[1] - astro.getDec()[0])
                                           + (astro.getDec()[2] - astro.getDec()[1]) + ((astro
                                                   .getDec()[2] - astro.getDec()[1]) - (astro
                                                           .getDec()[1] - astro.getDec()[0]))
                                                           * M) / 2.0);

        H = AstroLib.limitAngle180between(sidG + loc.getDegreeLong() - A);

        tH = H - Utils.RAD_TO_DEG(astro.getDra()[1]);

        sunAlt = Utils.RAD_TO_DEG(Math.asin(Math.sin(Utils.DEG_TO_RAD(loc
                .getDegreeLat()))
                * Math.sin(Utils.DEG_TO_RAD(B))
                + Math.cos(Utils.DEG_TO_RAD(loc.getDegreeLat()))
                * Math.cos(Utils.DEG_TO_RAD(B))
                * Math.cos(Utils.DEG_TO_RAD(tH))));

        sunAlt += AstroLib.getRefraction(loc, sunAlt);

        R = (M + ((sunAlt - Utils.CENTER_OF_SUN_ANGLE + (Utils.ALTITUDE_REFRACTION * Math
                .pow(loc.getSeaLevel(), 0.5))) / (360.0
                        * Math.cos(Utils.DEG_TO_RAD(B))
                        * Math.cos(Utils.DEG_TO_RAD(loc.getDegreeLat())) * Math
                        .sin(Utils.DEG_TO_RAD(tH)))));

        return (R * 24.0);

    }

    static double getThuhr(double lon, Astro astro) {

        double M, sidG;
        double ra0 = astro.getRa()[0], ra2 = astro.getRa()[2];
        double A, H;

        M = ((astro.getRa()[1] - lon - astro.getSid()[1]) / 360.0);
        M = AstroLib.limitAngle111(M);
        sidG = astro.getSid()[1] + 360.985647 * M;

        if (astro.getRa()[1] > 350 && astro.getRa()[2] < 10)
            ra2 += 360;
        if (astro.getRa()[0] > 350 && astro.getRa()[1] < 10)
            ra0 = 0;

        A = astro.getRa()[1]
                          + (M
                                  * ((astro.getRa()[1] - ra0) + (ra2 - astro.getRa()[1]) + ((ra2 - astro
                                          .getRa()[1]) - (astro.getRa()[1] - ra0))
                                          * M) / 2.0);

        H = AstroLib.limitAngle180between(sidG + lon - A);

        return 24.0 * (M - H / 360.0);
    }

    static double getAssr(double Lat, double dec, Mathhab mathhab) {
        double part1, part2, part3, part4, ndec;
        int mathhabValue = (mathhab == Mathhab.SHAAFI ? 1 : 2);
        double rlat = Utils.DEG_TO_RAD(Lat);

        /* Reverse if at or near the southern hemisphere - Modified by Imran, copied from libitl-0.7.0 */
        ndec = dec;
        if (Lat < 0.0) {
            ndec = -dec;
        }
        part1 = mathhabValue + Math.tan(rlat - ndec);
        if (part1 < 1.0) {
            part1 = mathhabValue - Math.tan(rlat - ndec);
        }

        part2 = (Utils.PI / 2.0) - Math.atan(part1);
        /* Compute the hour angle - Modified by Imran, copied from libitl-0.7.0 */
        part3 = Math.sin(part2) - (Math.sin(rlat) * Math.sin(ndec));
        part4 = (part3 / (Math.cos(rlat) * Math.cos(ndec)));

        /*  if (part4 > 1) */
        /*      return 99; */

        return Utils.DEG_TO_10_BASE * Utils.RAD_TO_DEG(Math.acos(part4));
    }

    static int getDayofYear(int year, int month, int day) {
        int i;
        int isLeap = (((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0)) ? 1
                : 0;

        char dayList[][] = {
                { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 },
                { 0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 } };

        for (i = 1; i < month; i++)
            day += dayList[isLeap][i];

        return day;
    }

    static DayCouple getDayInfo(SimpleDate date, double gmt) {
        int ld;
        double jd;
        ld = getDayofYear(date.getYear(), 12, 31);
        jd = AstroLib.getJulianDay(date, gmt);
        return new DayCouple(ld, jd);
    }

    /* Obtaining the direction of the shortest distance towards Qibla by uMath.sing the
     * great circle formula */

    /**
     * generate qibla direction
     * @param loc location where to calculate
     * @return a Dms object containg qibla direction
     */
    static public Dms getNorthQibla(Location loc) {
        /* xxxthamer: reduce Utils.DEG_TO_RAD usage */
        double num, denom;
        num = Math.sin(Utils.DEG_TO_RAD(loc.getDegreeLong())
                - Utils.DEG_TO_RAD(Utils.KAABA_LONG));
        denom = (Math.cos(Utils.DEG_TO_RAD(loc.getDegreeLat())) * Math
                .tan(Utils.DEG_TO_RAD(Utils.KAABA_LAT)))
                - (Math.sin(Utils.DEG_TO_RAD(loc.getDegreeLat())) * ((Math
                        .cos((Utils.DEG_TO_RAD(loc.getDegreeLong()) - Utils
                                .DEG_TO_RAD(Utils.KAABA_LONG))))));
        return new Dms(Utils.RAD_TO_DEG(Math.atan2(num, denom)));

    }

    /**
     * generate qibla direction
     * @return a Dms object containing qibla direction for the current location
     */
    public Dms getNorthQibla() {
        return getNorthQibla(loc);
    }

    /**
     *  Major version of Jitl
     * @return Jitl major version
     */
    public static int getMajorVersion() {
        return VERSION_MAJOR;
    }

    /**
     *  Minor version of Jitl
     * @return Jitl minor version
     */
    public static int getMinorVersion() {
        return VERSION_MINOR;
    }

}
