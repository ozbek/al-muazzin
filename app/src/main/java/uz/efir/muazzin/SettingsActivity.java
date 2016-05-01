package uz.efir.muazzin;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import islam.adhanalarm.Preferences;
import islam.adhanalarm.Schedule;
import islam.adhanalarm.dialog.AdvancedSettingsDialog;
import islam.adhanalarm.dialog.NotificationSettingsDialog;
import islam.adhanalarm.util.LocaleManager;

public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {
        private static LocaleManager sLocaleManager;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Activity activity = getActivity();
            sLocaleManager = LocaleManager.getInstance(activity, false);

            addPreferencesFromResource(R.xml.settings);
            PreferenceScreen root = getPreferenceScreen();
            // System time zone
            Preference timezonePref = root.findPreference("key_time_zone");
            timezonePref.setSummary(getGmtOffSet(activity));

            // Language settings
            ListPreference languagePref = (ListPreference) root.findPreference("key_locale");
            languagePref.setEntryValues(LocaleManager.LOCALES);
            languagePref.setValueIndex(sLocaleManager.getLanguageIndex());
            languagePref.setSummary(languagePref.getEntry());
            languagePref.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            final String key = preference.getKey();
            if ("key_notification".equals(key)) {
                new NotificationSettingsDialog(getActivity()).show();
                return false;
            } else if ("key_advanced".equals(key)) {
                new AdvancedSettingsDialog(getActivity()).show();
                return false;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if ("key_locale".equals(preference.getKey())) {
                Activity activity = getActivity();
                Preferences preferences = Preferences.getInstance(activity);
                preferences.setLocale(newValue.toString());
                Utils.isRestartNeeded = true;
                activity.finish();
            }

            return false;
        }

        private String getGmtOffSet(Context context) {
            DateFormat dateFormat = new SimpleDateFormat("Z", sLocaleManager.getLocale(context));
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"),
                    sLocaleManager.getLocale(context));

            StringBuilder timeZone = new StringBuilder(getString(R.string.gmt));
            timeZone.append(dateFormat.format(calendar.getTime()));
            timeZone.append(" (");
            timeZone.append(new GregorianCalendar().getTimeZone().getDisplayName());
            if (Schedule.isDaylightSavings()) {
                timeZone.append(getString(R.string.daylight_savings));
            }
            timeZone.append(")");
            return timeZone.toString();
        }
    }
}
