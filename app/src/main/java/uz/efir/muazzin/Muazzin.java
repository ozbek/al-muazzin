package uz.efir.muazzin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import java.util.Arrays;
import java.util.Locale;

public class Muazzin extends AppCompatActivity implements CalculationSettingsDialog.LocationProvider {
    private static final int REQUEST_ACCESS_LOCATION = 1001;
    private static final int REQUEST_POST_NOTIFICATIONS = 1002;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muazzin);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_LOCATION);
        } else {
            // Location permission is already granted
            initCalculationDefaults();
            setupViewPager();
            requestNotificationPermission();
        }
    }

    private void setupViewPager() {
        // set up the ViewPager, attaching the adapter
        MuazzinAdapter muazzinAdapter = new MuazzinAdapter(getApplicationContext(), getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(muazzinAdapter);
        final SlidingTabLayout indicator = findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_LOCATION) {
            initCalculationDefaults();
            setupViewPager();
            // After the location permission request has been answered,
            // request the notification permission with a delay to prevent screen flickering.
            new Handler(Looper.getMainLooper()).postDelayed(this::requestNotificationPermission, 1000);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
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
        if (BuildConfig.DEBUG) {
            menu.setGroupVisible(R.id.menu_group_controller, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Preferences preferences = Preferences.getInstance(this);
        short time = Schedule.today(this).nextTimeIndex();
        int itemId = item.getItemId();
        if (itemId == R.id.menu_location_calculation) {
            new CalculationSettingsDialog(this, this).show();
        } else if (itemId == R.id.menu_previous) {
            time--;
            if (time < CONSTANT.FAJR) {
                time = CONSTANT.ISHAA;
            }
            if (CONSTANT.SUNRISE == time && preferences.dontNotifySunrise()) {
                time = CONSTANT.FAJR;
            }
            NotificationService.notify(this, time, Schedule.today(this).getTimes()[time].getTimeInMillis());
        } else if (itemId == R.id.menu_next) {
            if (CONSTANT.SUNRISE == time && preferences.dontNotifySunrise()) {
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

    private void initCalculationDefaults() {
        Preferences preferences = Preferences.getInstance(this);
        if (!preferences.isCalculationMethodSet()) {
            String country = Locale.getDefault().getISO3Country().toUpperCase(Locale.US);
            for (int i = 0; i < CONSTANT.CALCULATION_METHOD_COUNTRY_CODES.length; i++) {
                if (Arrays.asList(CONSTANT.CALCULATION_METHOD_COUNTRY_CODES[i]).contains(country)) {
                    preferences.setCalculationMethodIndex(i);
                    break;
                }
            }
        }

        if (!preferences.isLocationSet()) {
            Location currentLocation = getCurrentLocation(this);
            if (currentLocation != null) {
                preferences.setLocation(currentLocation);
            }
        }
    }

    @Nullable
    private Location getCurrentLocation(@NonNull Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location currentLocation = null;
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (currentLocation == null && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        return currentLocation;
    }

    @Override
    public Location getLatestLocation() {
        return getCurrentLocation(this);
    }
}
