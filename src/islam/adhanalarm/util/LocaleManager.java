package islam.adhanalarm.util;

import islam.adhanalarm.Preferences;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;
import java.util.MissingResourceException;

public class LocaleManager {
    public static final String[] LOCALES = new String[] { "default", // represents
                                                                     // the
                                                                     // default
                                                                     // system
                                                                     // language;
                                                                     // i.e.,
                                                                     // not
                                                                     // necessarily
                                                                     // English
            "uz", "ar", "de", "en", "es", "fr", "in", "it", "tr", "ru" };

    private boolean languageDirty = false;
    private int languageIndex = 0;
    private Locale mLocale;
    private static LocaleManager sLocaleManager;

    /**
     * This class should be instantiated after an activity's super.onCreate()
     * call but before setContentView()
     * 
     * @param context
     *            The activity which will get set to the language specified in
     *            settings
     * @param forced
     *            Whether LocaleManager should be force re-instantiated
     */
    public static LocaleManager getInstance(Context context, boolean forced) {
        return (forced || sLocaleManager == null) ? (sLocaleManager = new LocaleManager(context))
                : sLocaleManager;
    }

    private LocaleManager(Context context) {
        // Set the language based on settings
        Preferences preferences = Preferences.getInstance(context);
        String languageKey = preferences.getLocale();
        Locale defaultLocale = Locale.getDefault();
        if (LOCALES[0].equals(languageKey)) {
            languageKey = defaultLocale.getCountry();
        }
        Locale locale;
        try {
            locale = new Locale(languageKey, defaultLocale.getISO3Country().toUpperCase(
                    defaultLocale));
        } catch (MissingResourceException mre) {
            locale = new Locale(languageKey);
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;

        context.getResources().updateConfiguration(config, null);

        // Set the language index into the local LANGUAGE_KEYS array
        for (int i = 0; i < LOCALES.length; i++) {
            if (languageKey.equals(LOCALES[i])) {
                languageIndex = i;
                break;
            }
        }

        setLocale(locale);
    }

    public int getLanguageIndex() {
        return languageIndex;
    }

    public void setDirty(boolean isDirty) {
        languageDirty = isDirty;
    }

    public boolean isDirty() {
        return languageDirty;
    }

    private void setLocale(Locale locale) {
        mLocale = locale;
    }

    public Locale getLocale(Context context) {
        if (mLocale == null) {
            new LocaleManager(context);
        }

        return mLocale;
    }
}
