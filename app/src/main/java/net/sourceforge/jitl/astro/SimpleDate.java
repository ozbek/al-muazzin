package net.sourceforge.jitl.astro;

import java.util.GregorianCalendar;

public class SimpleDate {
    int day;

    int month;

    int year;

    /**
     * TODO
     * @param day
     * @param month
     * @param year
     */
    public SimpleDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    /**
     * TODO
     * @param gCalendar
     */
    public SimpleDate(GregorianCalendar gCalendar) {
        this.day = gCalendar.get(GregorianCalendar.DATE);
        this.month = gCalendar.get(GregorianCalendar.MONTH) + 1;
        this.year = gCalendar.get(GregorianCalendar.YEAR);
    }

    public SimpleDate copy() {
        return new SimpleDate(day, month, year);
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}
