package islam.adhanalarm.dialog;

import islam.adhanalarm.VARIABLE;
import islam.adhanalarm.util.LocaleManager;
import uz.efir.muazzin.R;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class InterfaceSettingsDialog extends Dialog {
    private static LocaleManager localeManager;

    public InterfaceSettingsDialog(Context context, LocaleManager lm) {
        super(context);
        localeManager = lm;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings_interface);
        setTitle(R.string.sinterface);

        Spinner languages = (Spinner)findViewById(R.id.languages);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languages.setAdapter(adapter);
        languages.setSelection(localeManager.getLanguageIndex());

        ((Button)findViewById(R.id.save_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = VARIABLE.settings.edit();
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
                ((Spinner)findViewById(R.id.languages)).setSelection(0);
            }
        });
    }
}