package net.sourceforge.jitl;
class DayCouple {
    private int lastDay;

    private double julianDay;

    public DayCouple(int lastDay, double julianDay) {
        this.lastDay = lastDay;
        this.julianDay = julianDay;
    }

    public double getJulianDay() {
        return julianDay;
    }

    public void setJulianDay(double julianDay) {
        this.julianDay = julianDay;
    }

    public int getLastDay() {
        return lastDay;
    }

    public void setLastDay(int lastDay) {
        this.lastDay = lastDay;
    }

}
