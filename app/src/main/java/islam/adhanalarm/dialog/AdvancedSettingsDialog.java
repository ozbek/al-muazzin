package islam.adhanalarm.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Preferences;
import uz.efir.muazzin.R;

public class AdvancedSettingsDialog extends AppCompatDialog {
    private Context mContext;
    private EditText mAltitudeText;
    private EditText mPressureText;
    private EditText mTemperatureText;
    private EditText mOffsetMinutesText;
    private Spinner mRoundingTypes;

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
        float[] apt = preferences.getApt();

        mAltitudeText = (EditText) findViewById(R.id.altitude);
        mAltitudeText.setText(Float.toString(apt[0]));

        mPressureText = (EditText) findViewById(R.id.pressure);
        mPressureText.setText(Float.toString(apt[1]));

        mTemperatureText = (EditText) findViewById(R.id.temperature);
        mTemperatureText.setText(Float.toString(apt[2]));

        mRoundingTypes = (Spinner) findViewById(R.id.rounding_types);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.rounding_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRoundingTypes.setAdapter(adapter);
        mRoundingTypes.setSelection(preferences.getRoundingMethodIndex());

        mOffsetMinutesText = (EditText) findViewById(R.id.offset_minutes);
        mOffsetMinutesText.setText(Integer.toString(preferences.getOffsetMinutes()));

        Button saveSettings = (Button) findViewById(R.id.save_settings);
        assert saveSettings != null;
        saveSettings.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try {
                    preferences.setApt(
                            Float.parseFloat(mAltitudeText.getText().toString()),
                            Float.parseFloat(mPressureText.getText().toString()),
                            Float.parseFloat(mTemperatureText.getText().toString())
                    );
                    preferences.setOffsetMinutes(
                            Integer.parseInt(mOffsetMinutesText.getText().toString())
                    );
                } catch (NumberFormatException nfe) {
                    // Do nothing, will keep the previous values
                }
                preferences.setRoundingMethodIndex(mRoundingTypes.getSelectedItemPosition());

                dismiss();
            }
        });

        Button resetSettings = (Button) findViewById(R.id.reset_settings);
        assert resetSettings != null;
        resetSettings.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mAltitudeText.setText("0.0");
                mPressureText.setText("1010.0");
                mTemperatureText.setText("10.0");
                mRoundingTypes.setSelection(CONSTANT.DEFAULT_ROUNDING_INDEX);
                mOffsetMinutesText.setText("0");
            }
        });
    }
}
