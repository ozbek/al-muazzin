package net.sourceforge.jitl;

/**
 * This class is used to round prayer times (seconds).
 */
public class Rounding {

    private Rounding(){

    }

    /**
     *  No Rounding. second is set to the amount of computed seconds.
     */
    public static final Rounding NONE = new Rounding();

    /**
     * Normal Rounding. If seconds are equal to 30 or above, add 1 minute. Sets "Prayer.seconds" to zero.
     */
    public static final Rounding NORMAL = new Rounding();

    /**
     * Special Rounding. Similar to normal rounding but we always round down for Shurooq and Imsaak times. (default)
     */
    public static final Rounding SPECIAL = new Rounding();

    /**
     * Aggressive Rounding. Similar to Special Rounding but we add 1 minute if the seconds value are equal to 1 second or more.
     */
    public static final Rounding AGRESSIVE = new Rounding();

}
