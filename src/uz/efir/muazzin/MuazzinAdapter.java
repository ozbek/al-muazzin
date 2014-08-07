package uz.efir.muazzin;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MuazzinAdapter extends FragmentStatePagerAdapter {
    private final Context mContext;

    public MuazzinAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
        case 0:
            return new PrayerTimesFragment();
        case 1:
            return new QiblaCompassFragment();
        default:
            return null;
        }
    }

    @Override
    public int getCount() {
        // time table + compass
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
        case 0:
            return mContext.getString(R.string.today);
        case 1:
            return mContext.getString(R.string.qibla);
        default:
            return null;
        }
    }
}
