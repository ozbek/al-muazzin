package uz.efir.muazzin;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Notifier;
import islam.adhanalarm.Preferences;
import islam.adhanalarm.Schedule;
import islam.adhanalarm.dialog.CalculationSettingsDialog;
import islam.adhanalarm.dialog.SettingsDialog;
import islam.adhanalarm.receiver.StartNotificationReceiver;
import islam.adhanalarm.util.LocaleManager;
import islam.adhanalarm.view.QiblaCompassView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import net.sourceforge.jitl.Jitl;
import net.sourceforge.jitl.astro.Dms;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.sriramramani.droid.inspector.server.ViewServer;

public class Muazzin extends SherlockFragmentActivity implements ActionBar.TabListener {
    private static LocaleManager sLocaleManager;
    private static Preferences sPreferences;
    private MyFragmentStatePagerAdapter myFragmentStatePagerAdapter;
    private ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        sPreferences = Preferences.getInstance(this);
        super.onCreate(savedInstanceState);
        ViewServer.get(this).addWindow(this);
        sLocaleManager = new LocaleManager(this);

        setContentView(R.layout.activity_muazzin);

        myFragmentStatePagerAdapter = new MyFragmentStatePagerAdapter(this, getSupportFragmentManager());

        // Set up action bar
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled(false);

        // Set up the ViewPager, attaching the adapter
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(myFragmentStatePagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar
        for (int i = 0; i < myFragmentStatePagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setText(myFragmentStatePagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (Schedule.settingsAreDirty() || sLocaleManager.isDirty()) {
                // Restart the app to apply new settings changes
                Preferences.updateWidgets(this);
                AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 1000/*one second*/,
                        PendingIntent.getActivity(this, 0, getIntent(), PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT));
                finish();
                return;
            }

            if (sPreferences.isLocationSet()) {
                ((TextView)findViewById(R.id.notes)).setText(null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.activity_muazzin, menu);
        if (!BuildConfig.DEBUG) {
            menu.removeItem(R.id.menu_previous);
            menu.removeItem(R.id.menu_next);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        short time = Schedule.today(this).nextTimeIndex();
        switch(item.getItemId()) {
        case R.id.menu_location_calculation:
            new CalculationSettingsDialog(this).show();
            break;
        case R.id.menu_previous:
            time--;
            if (time < CONSTANT.FAJR) {
                time = CONSTANT.ISHAA;
            }
            if (CONSTANT.SUNRISE == time && sPreferences.dontNotifySunrise()) {
                time = CONSTANT.FAJR;
            }
            Notifier.start(this, time, Schedule.today(this).getTimes()[time].getTimeInMillis());
            break;
        case R.id.menu_next:
            if (CONSTANT.SUNRISE == time && sPreferences.dontNotifySunrise()) {
                time = CONSTANT.DHUHR;
            }
            Notifier.start(this, time, Schedule.today(this).getTimes()[time].getTimeInMillis());
            break;
        case R.id.menu_stop:
            Notifier.stop();
            break;
        case R.id.menu_settings:
            new SettingsDialog(this, sLocaleManager).show();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewServer.get(this).setFocusedWindow(this);
        sPreferences.setIsForeground(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        sPreferences.setIsForeground(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }

    public static class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
        private Context mContext;

        public MyFragmentStatePagerAdapter(Context context, FragmentManager fm) {
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
            // We only have a time table + a compass view for now
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

    /**
     * Prayer times fragment that displays time table for the day's prayer times.
     * In the future, we may add some extra days...
     */
    public static class PrayerTimesFragment extends Fragment {
        private ArrayList<HashMap<String, String>> mTimeTable = new ArrayList<HashMap<String, String>>(7);
        private SimpleAdapter mTimetableView;
        private TextView mNotes;
        private TextView mTodaysDate;

        @Override
        public void onCreate (Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            for (short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("time_name", getString(CONSTANT.TIME_NAMES[i]));
                mTimeTable.add(i, map);
            }
            mTimetableView = new SimpleAdapter(getActivity(), mTimeTable, R.layout.timetable_row,
                    new String[]{"time_name", "time"},
                    new int[]{R.id.time_name, R.id.time});
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.tab_today, container, false);
            mNotes = (TextView)rootView.findViewById(R.id.notes);
            try {
                sPreferences.initCalculationDefaults(getActivity());
            } catch (NullPointerException npe) {
                mNotes.setText(getString(R.string.location_not_set));
            }
            mTodaysDate = (TextView)rootView.findViewById(R.id.today);

            ListView lv = (ListView)rootView.findViewById(R.id.timetable);
            lv.setAdapter(mTimetableView);
            lv.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                // Set zebra stripes
                private int numChildren = 0;
                public void onChildViewAdded(View parent, View child) {
                    child.setBackgroundResource(++numChildren % 2 == 0 ? R.color.semi_transparent_white : android.R.color.transparent);
                    if (numChildren > CONSTANT.NEXT_FAJR) {
                        // Reached the last row, reset for next time
                        numChildren = 0;
                    }
                }
                public void onChildViewRemoved(View parent, View child) {}
            });

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            updateTodaysTimetable();
        }

        private void updateTodaysTimetable() {
            Context context = getActivity();
            StartNotificationReceiver.setNext(context);
            Schedule today = Schedule.today(context);
            mTodaysDate.setText(today.hijriDateToString(context));
            GregorianCalendar[] schedule = today.getTimes();
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", sLocaleManager.getLocale(context));
            if (DateFormat.is24HourFormat(context)) {
                timeFormat = new SimpleDateFormat("HH:mm ", sLocaleManager.getLocale(context));
            }

            for (short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) {
                String fullTime = timeFormat.format(schedule[i].getTime());
                mTimeTable.get(i).put("time", today.isExtreme(i) ? fullTime.concat(" *") : fullTime);
                if (today.isExtreme(i)) {
                    // FIXME: this is getting cleared if Preferences.isLocationSet() is true
                    mNotes.setText("* " + getString(R.string.extreme));
                }
            }

            final short next = today.nextTimeIndex();
            mTimeTable.get(next).put("time_name", getString(R.string.next_time_marker).concat(getString(CONSTANT.TIME_NAMES[next])));
            mTimetableView.notifyDataSetChanged();
        }
    }

    /**
     * Qibla compass fragment.
     */
    @SuppressWarnings("deprecation") // for SensorListener and SensorManager APIs
    public static class QiblaCompassFragment extends Fragment {
        private static final DecimalFormat DF = new DecimalFormat("#.###");
        private static SensorManager sSensorManager;
        private static float sQiblaDirection = 0f;
        private static android.hardware.SensorListener sOrientationListener;
        private static boolean isTrackingOrientation = false;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            sSensorManager = (SensorManager)getActivity().getSystemService(SENSOR_SERVICE);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.tab_qibla, container, false);
            final QiblaCompassView qiblaCompassView = (QiblaCompassView)rootView.findViewById(R.id.qibla_compass);
            qiblaCompassView.setConstants(((TextView)rootView.findViewById(R.id.bearing_north)), getText(R.string.bearing_north),
                    ((TextView)rootView.findViewById(R.id.bearing_qibla)), getText(R.string.bearing_qibla));
            sOrientationListener = new android.hardware.SensorListener() {
                public void onSensorChanged(int s, float v[]) {
                    float northDirection = v[SensorManager.DATA_X];
                    qiblaCompassView.setDirections(northDirection, sQiblaDirection);

                }
                public void onAccuracyChanged(int s, int a) {
                }
            };

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            updateDms();
            if (!isTrackingOrientation) {
                isTrackingOrientation = sSensorManager.registerListener(sOrientationListener, SensorManager.SENSOR_ORIENTATION);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            if (isTrackingOrientation) {
                sSensorManager.unregisterListener(sOrientationListener);
                isTrackingOrientation = false;
            }
        }

        /**
         * Add Latitude, Longitude and Qibla DMS location
         */
        private void updateDms() {
            net.sourceforge.jitl.astro.Location location = sPreferences.getJitlLocation();
            Dms latitude = new Dms(location.getDegreeLat());
            Dms longitude = new Dms(location.getDegreeLong());
            Dms qibla = Jitl.getNorthQibla(location);
            sQiblaDirection = (float)qibla.getDecimalValue(net.sourceforge.jitl.astro.Direction.NORTH);

            View rootView = getView();
            TextView tv = (TextView)rootView.findViewById(R.id.current_latitude);
            tv.setText(getString(R.string.degree_minute_second,
                    latitude.getDegree(),
                    latitude.getMinute(),
                    DF.format(latitude.getSecond())));

            tv = (TextView)rootView.findViewById(R.id.current_longitude);
            tv.setText(getString(R.string.degree_minute_second,
                    longitude.getDegree(),
                    longitude.getMinute(),
                    DF.format(longitude.getSecond())));

            tv = (TextView)rootView.findViewById(R.id.current_qibla);
            tv.setText(getString(R.string.degree_minute_second,
                    qibla.getDegree(),
                    qibla.getMinute(),
                    DF.format(qibla.getSecond())));
        }
    }
}
