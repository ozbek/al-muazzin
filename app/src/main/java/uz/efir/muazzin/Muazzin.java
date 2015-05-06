package uz.efir.muazzin;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import java.util.Calendar;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Notifier;
import islam.adhanalarm.Preferences;
import islam.adhanalarm.Schedule;
import islam.adhanalarm.dialog.CalculationSettingsDialog;
import islam.adhanalarm.util.LocaleManager;

public class Muazzin extends SherlockFragmentActivity implements ActionBar.TabListener {
    // private static final String TAG = Muazzin.class.getSimpleName();
    private Preferences mPreferences;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(this);
        LocaleManager.getInstance(this, true);

        setContentView(R.layout.activity_muazzin);

        MuazzinAdapter muazzinAdapter = new MuazzinAdapter(this, getSupportFragmentManager());

        // Set up action bar
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled(false);

        // Set up the ViewPager, attaching the adapter
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(muazzinAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the
                // corresponding tab
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar
        for (int i = 0; i < muazzinAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter.
            // Also specify this Activity object, which implements the
            // TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(actionBar.newTab().setText(muazzinAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (Utils.isRestartNeeded) {
                restartSelf();
                return;
            }

            if (mPreferences.isLocationSet()) {
                ((TextView) findViewById(R.id.notes)).setText(null);
            }
        }
    }

    /**
     * Restarts the app to apply new settings changes
     */
    private void restartSelf() {
//        Utils.updateWidgets(this);
        Utils.isRestartNeeded = false;

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().getTimeInMillis() + 300,
                PendingIntent.getActivity(this, 0, getIntent(), PendingIntent.FLAG_ONE_SHOT
                        | PendingIntent.FLAG_CANCEL_CURRENT));

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.activity_muazzin, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!BuildConfig.DEBUG) {
            menu.removeGroup(R.id.menu_group_controller);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        short time = Schedule.today(this).nextTimeIndex();
        switch (item.getItemId()) {
            case R.id.menu_location_calculation:
                new CalculationSettingsDialog(this).show();
                break;
            case R.id.menu_previous:
                time--;
                if (time < CONSTANT.FAJR) {
                    time = CONSTANT.ISHAA;
                }
                if (CONSTANT.SUNRISE == time && mPreferences.dontNotifySunrise()) {
                    time = CONSTANT.FAJR;
                }
                Notifier.start(this, time, Schedule.today(this).getTimes()[time].getTimeInMillis());
                break;
            case R.id.menu_next:
                if (CONSTANT.SUNRISE == time && mPreferences.dontNotifySunrise()) {
                    time = CONSTANT.DHUHR;
                }
                Notifier.start(this, time, Schedule.today(this).getTimes()[time].getTimeInMillis());
                break;
            case R.id.menu_stop:
                Notifier.stop(this);
                break;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isRestartNeeded) {
            restartSelf();
            return;
        }
        Utils.setIsForeground(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.setIsForeground(false);
    }
}
