package net.sourceforge.jitl;

import java.util.Iterator;

/**
 * Encapsulates the list of prayers time and shuruq time
 */
public class DayPrayers {
    private Prayer[] prayers = new Prayer[6];

    public DayPrayers() {
        for (int i = 0; i < 6; i++) {
            prayers[i] = new Prayer();
        }
    }

    /**
     * set all prayer calculation to extreme
     *
     * @param extreme extreme boolean value
     */
    void setAllExtreme(boolean extreme) {
        for (int i = 0; i < 6; i++) {
            prayers[i].setExtreme(extreme);
        }
    }

    /**
     * Fajr prayer time
     * @return fajr prayer time
     */
    public Prayer fajr() {
        return prayers[0];
    }

    /**
     * Shuruq time
     * @return shuruq time
     */
    public Prayer shuruq() {
        return prayers[1];
    }

    /**
     * Thuhr time
     * @return thuhr prayer time
     */
    public Prayer thuhr() {
        return prayers[2];
    }

    /**
     * Assr time
     * @return assr prayer time
     */
    public Prayer assr() {
        return prayers[3];
    }

    /**
     * Maghrib time
     * @return maghrib time
     */
    public Prayer maghrib() {
        return prayers[4];
    }

    /**
     * Ishaa time
     * @return ishaa time
     */
    public Prayer ishaa() {
        return prayers[5];
    }

    /**
     * Get prayer list as an array
     * @return an array containing the list of prayer times including the
     *         shuruq. The size of the array is 6.
     */
    public Prayer[] getPrayers() {
        return prayers;
    }

    /**
     * convert prayer times to a string.
     * @return prayer times as a string. It contains 6 lines
     */
    public String toString() {
        String ret = "";
        for (int i = 0; i < 6; i++) {
            ret += prayers[i].toString() + "\n";
        }
        return ret;
    }

    /**
     * Creates an iterator on the prayers
     * @return an iterator over the prayers
     * @see #getPrayers()
     */
    public Iterator<Prayer> iterator() {
        return new Iterator<Prayer>() {
            private int i = 0;

            public boolean hasNext() {
                if (i < 6)
                    return true;
                return false;
            }

            public Prayer next() {
                return prayers[i++];
            }

            public void remove() {
                if (i > 0) {
                    i--;
                }
            }

        };
    }
}
