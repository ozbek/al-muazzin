package uz.efir.muazzin;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;

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

public class SettingsActivity extends SherlockPreferenceActivity implements
        OnPreferenceChangeListener {
    private static LocaleManager sLocaleManager;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sLocaleManager = LocaleManager.getInstance(this, true);
        // show action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.settings);

        addPreferencesFromResource(R.xml.settings);

        PreferenceScreen root = getPreferenceScreen();
        // System time zone
        Preference timezonePref = root.findPreference("key_time_zone");
        timezonePref.setSummary(getGmtOffSet(this));

        // Language settings

        ListPreference languagePref = (ListPreference) root.findPreference("key_locale");
        languagePref.setEntryValues(LocaleManager.LOCALES);
        languagePref.setValueIndex(sLocaleManager.getLanguageIndex());
        languagePref.setSummary(languagePref.getEntry());
        languagePref.setOnPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();
        if ("key_notification".equals(key)) {
            new NotificationSettingsDialog(this).show();
            return false;
        } else if ("key_advanced".equals(key)) {
            new AdvancedSettingsDialog(this).show();
            return false;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = new Intent(this, Muazzin.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("key_locale".equals(preference.getKey())) {
            Preferences preferences = Preferences.getInstance(this);
            preferences.setLocale(newValue.toString());
            sLocaleManager.setDirty(true);
            finish();
        }

        return false;
    }

    private final String getGmtOffSet(Context context) {
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
