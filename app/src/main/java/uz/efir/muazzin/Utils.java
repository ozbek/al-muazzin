package uz.efir.muazzin;

public class Utils {
    public static boolean isRestartNeeded = false;
    private static boolean mIsForeground = false;

    public static boolean getIsForeground() {
        return mIsForeground;
    }

    public static void setIsForeground(boolean isForeground) {
        mIsForeground = isForeground;
    }
}
