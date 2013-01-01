package uz.efir.azon.util;

import uz.efir.azon.VARIABLE;

import java.util.Locale;

import android.content.res.Configuration;

public class LocaleManager {

    private boolean languageDirty = false;
    private int languageIndex = DEFAULT_LANGUAGE;

    private static final short DEFAULT_LANGUAGE = 0; // LANGUAGE_KEYS[0] == "default" (represents the default system language (i.e. not necessarily English))

    public static final String[] LANGUAGE_KEYS = new String[]{"default", "ar", "de", "en", "es", "fr", "in", "it", "ru", "tr"};

    /**
     * This class should be instantiated after an activity's super.onCreate() call but before setContentView()
     * @param a The activity which will get set to the language specified in settings
     */
    public LocaleManager() {
        // Set the language based on settings
        String languageKey = VARIABLE.settings.getString("locale", LANGUAGE_KEYS[DEFAULT_LANGUAGE]);
        if(languageKey.equals("default")) {
            languageKey = Locale.getDefault().getCountry();
        }
        String country = Locale.getDefault().getISO3Country().toUpperCase();
        Locale locale = new Locale(languageKey, country);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;

        VARIABLE.context.getResources().updateConfiguration(config, VARIABLE.context.getResources().getDisplayMetrics());

        // Set the language index into the local LANGUAGE_KEYS array
        for(int i = 0; i < LANGUAGE_KEYS.length; i++) {
            if(languageKey.equals(LANGUAGE_KEYS[i])) {
                languageIndex = i;
                break;
            }
        }
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
}