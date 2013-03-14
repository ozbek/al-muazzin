package islam.adhanalarm.util;

import islam.adhanalarm.VARIABLE;

import java.util.Locale;

import android.content.res.Configuration;

public class LocaleManager {

    private boolean languageDirty = false;
    private int languageIndex = 0;
    private Locale mLocale;

    public static final String[] LANGUAGE_KEYS = new String[] {
            "default", // represents the default system language; i.e., not necessarily English
            "uz", "ar", "de", "en", "es",
            "fr", "in", "it", "tr", "ru"
            };

    /**
     * This class should be instantiated after an activity's super.onCreate() call but before setContentView()
     * @param a The activity which will get set to the language specified in settings
     */
    public LocaleManager() {
        // Set the language based on settings
        String languageKey = VARIABLE.settings.getString("locale", LANGUAGE_KEYS[0]/*"default"*/);
        Locale defaultLocale = Locale.getDefault();
        if (languageKey.equals("default")) {
            languageKey = defaultLocale.getCountry();
        }
        String country = defaultLocale.getISO3Country().toUpperCase(defaultLocale);
        Locale locale = new Locale(languageKey, country);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;

        VARIABLE.context.getResources().updateConfiguration(config, VARIABLE.context.getResources().getDisplayMetrics());

        // Set the language index into the local LANGUAGE_KEYS array
        for (int i = 0; i < LANGUAGE_KEYS.length; i++) {
            if(languageKey.equals(LANGUAGE_KEYS[i])) {
                languageIndex = i;
                break;
            }
        }

        setLocale(locale);
    }

    public int getLanguageIndex() {
        return languageIndex;
    }

    public void setDirty() {
        languageDirty = true;
    }

    public boolean isDirty() {
        return languageDirty;
    }

    private void setLocale(Locale locale) {
        mLocale = locale;
    }

    public Locale getLocale() {
        if (mLocale == null) {
            new LocaleManager();
        }

        return mLocale;
    }
}