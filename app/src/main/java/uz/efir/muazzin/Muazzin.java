package uz.efir.muazzin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Preferences;
import islam.adhanalarm.Schedule;

public class Muazzin extends AppCompatActivity {
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1001;
    private Preferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        }

        setContentView(R.layout.activity_muazzin);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        // Set up the ViewPager, attaching the adapter
        MuazzinAdapter muazzinAdapter = new MuazzinAdapter(getApplicationContext(), getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(muazzinAdapter);
        final SlidingTabLayout indicator = findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (mPreferences.isLocationSet()) {
                TextView notes = findViewById(R.id.notes);
                assert notes != null;
                notes.setText(null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_muazzin, menu);
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
        int itemId = item.getItemId();
        if (itemId == R.id.menu_location_calculation) {
            new CalculationSettingsDialog(this).show();
        } else if (itemId == R.id.menu_previous) {
            time--;
            if (time < CONSTANT.FAJR) {
                time = CONSTANT.ISHAA;
            }
            if (CONSTANT.SUNRISE == time && mPreferences.dontNotifySunrise()) {
                time = CONSTANT.FAJR;
            }
            NotificationService.notify(this, time, Schedule.today(this).getTimes()[time].getTimeInMillis());
        } else if (itemId == R.id.menu_next) {
            if (CONSTANT.SUNRISE == time && mPreferences.dontNotifySunrise()) {
                time = CONSTANT.DHUHR;
            }
            NotificationService.notify(this, time, Schedule.today(this).getTimes()[time].getTimeInMillis());
        } else if (itemId == R.id.menu_stop) {
            NotificationService.cancelAll(this);
        } else if (itemId == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
