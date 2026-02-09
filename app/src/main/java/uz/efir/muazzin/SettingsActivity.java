package uz.efir.muazzin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsActivity extends AppCompatActivity {
    private CheckBox mBismillahOnBootUp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.notification);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Preferences preferences = Preferences.getInstance(this);
        final int[] notificationIds = new int[]{
                R.id.notification_fajr,
                R.id.notification_sunrise,
                R.id.notification_dhuhr,
                R.id.notification_asr,
                R.id.notification_maghrib,
                R.id.notification_ishaa
        };
        for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
            ArrayList<String> notificationMethods = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.notification_methods)));

            if (i == CONSTANT.SUNRISE) {
                notificationMethods.remove(getString(R.string.adhan));
            } else {
                notificationMethods.remove(getString(R.string.beep));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, notificationMethods);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner notification = findViewById(notificationIds[i]);
            notification.setAdapter(adapter);

            int savedPosition = preferences.getNotificationMethod(i);
            if (savedPosition >= adapter.getCount()) {
                savedPosition = (i == CONSTANT.SUNRISE ? 0 : 1);
                preferences.setNotificationMethod(i, savedPosition);
            }
            notification.setSelection(savedPosition);
        }

        mBismillahOnBootUp = findViewById(R.id.bismillah_on_boot_up);
        mBismillahOnBootUp.setChecked(preferences.getBasmalaEnabled());

        Button saveSettings = findViewById(R.id.save_settings);
        saveSettings.setOnClickListener(v -> {
            Spinner spinner;
            for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
                spinner = findViewById(notificationIds[i]);
                preferences.setNotificationMethod(i, spinner.getSelectedItemPosition());
            }
            preferences.setBasmalaEnabled(mBismillahOnBootUp.isChecked());
            finish();
        });

        Button resetSettings = findViewById(R.id.reset_settings);
        resetSettings.setOnClickListener(v -> {
            Spinner spinner;
            for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
                spinner = findViewById(notificationIds[i]);
                spinner.setSelection(i == CONSTANT.SUNRISE ? 0 : 1);
            }
            mBismillahOnBootUp.setChecked(false);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
