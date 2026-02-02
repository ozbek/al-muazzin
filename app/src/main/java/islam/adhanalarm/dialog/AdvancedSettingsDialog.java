package islam.adhanalarm.dialog;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import islam.adhanalarm.Preferences;
import uz.efir.muazzin.R;

public class AdvancedSettingsDialog extends AppCompatDialog {
    private Context mContext;
    private EditText mOffsetMinutesText;

    public AdvancedSettingsDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings_advanced);
        setTitle(R.string.advanced);

        final Preferences preferences = Preferences.getInstance(mContext);

        mOffsetMinutesText = (EditText) findViewById(R.id.offset_minutes);
        mOffsetMinutesText.setText(Integer.toString(preferences.getOffsetMinutes()));

        Button saveSettings = (Button) findViewById(R.id.save_settings);
        assert saveSettings != null;
        saveSettings.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    preferences.setOffsetMinutes(
                            Integer.parseInt(mOffsetMinutesText.getText().toString())
                    );
                } catch (NumberFormatException nfe) {
                    // Do nothing, will keep the previous values
                }

                dismiss();
            }
        });

        Button resetSettings = (Button) findViewById(R.id.reset_settings);
        assert resetSettings != null;
        resetSettings.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mOffsetMinutesText.setText("0");
            }
        });
    }
}
