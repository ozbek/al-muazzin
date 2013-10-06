package islam.adhanalarm.dialog;

import islam.adhanalarm.Preferences;
import islam.adhanalarm.util.LocaleManager;
import uz.efir.muazzin.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class InterfaceSettingsDialog extends Dialog {
    private Context mContext;
    private static LocaleManager sLocaleManager;
    private Spinner mLocalesSpinner;

    public InterfaceSettingsDialog(Context context, LocaleManager lm) {
        super(context);
        mContext = context;
        sLocaleManager = lm;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings_interface);
        setTitle(R.string.sinterface);

        mLocalesSpinner = (Spinner) findViewById(R.id.languages);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLocalesSpinner.setAdapter(adapter);
        mLocalesSpinner.setSelection(sLocaleManager.getLanguageIndex());

        ((Button) findViewById(R.id.save_settings))
                .setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        int newLocaleIndex = mLocalesSpinner.getSelectedItemPosition();
                        if (newLocaleIndex != sLocaleManager.getLanguageIndex()) {
                            Preferences preferences = Preferences.getInstance(mContext);
                            preferences.setLocale(LocaleManager.LOCALES[newLocaleIndex]);
                            sLocaleManager.setDirty(true);
                        }

                        dismiss();
                    }
                });

        ((Button) findViewById(R.id.reset_settings))
                .setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        mLocalesSpinner.setSelection(0);
                    }
                });
    }
}
