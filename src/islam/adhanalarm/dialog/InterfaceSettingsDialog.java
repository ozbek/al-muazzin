package islam.adhanalarm.dialog;

import uz.efir.muazzin.R;
import islam.adhanalarm.VARIABLE;
import islam.adhanalarm.util.LocaleManager;
import islam.adhanalarm.util.ThemeManager;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class InterfaceSettingsDialog extends Dialog {

    private static ThemeManager themeManager;
    private static LocaleManager localeManager;

    public InterfaceSettingsDialog(Context context, ThemeManager tm, LocaleManager lm) {
        super(context);
        themeManager = tm;
        localeManager = lm;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings_interface);
        setTitle(R.string.sinterface);

        Spinner themes = (Spinner)findViewById(R.id.themes);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, themeManager.getAllThemeNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themes.setAdapter(adapter);
        themes.setSelection(themeManager.getThemeIndex());

        Spinner languages = (Spinner)findViewById(R.id.languages);
        adapter = ArrayAdapter.createFromResource(getContext(), R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languages.setAdapter(adapter);
        languages.setSelection(localeManager.getLanguageIndex());

        ((Button)findViewById(R.id.save_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = VARIABLE.settings.edit();
                int newThemeIndex = ((Spinner)findViewById(R.id.themes)).getSelectedItemPosition();
                if(themeManager.getThemeIndex() != newThemeIndex) {
                    editor.putInt("themeIndex", newThemeIndex);
                    themeManager.setDirty();
                }
                int newLanguageIndex = ((Spinner)findViewById(R.id.languages)).getSelectedItemPosition();
                if(newLanguageIndex != localeManager.getLanguageIndex()) {
                    editor.putString("locale", LocaleManager.LANGUAGE_KEYS[newLanguageIndex]);
                    localeManager.setDirty();
                }
                editor.commit();
                dismiss();
            }
        });
        ((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ((Spinner)findViewById(R.id.themes)).setSelection(ThemeManager.DEFAULT_THEME);
                ((Spinner)findViewById(R.id.languages)).setSelection(0);
            }
        });
    }
}