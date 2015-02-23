package islam.adhanalarm.dialog;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Preferences;
import islam.adhanalarm.Schedule;
import uz.efir.muazzin.R;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class CalculationSettingsDialog extends Dialog {
    private Context mContext;
    private EditText mLatitudeText;
    private EditText mLongitudeText;
    private Spinner mCalculationMethods;

    public CalculationSettingsDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings_calculation);
        setTitle(R.string.calculation);

        final Preferences preferences = Preferences.getInstance(mContext);
        final float[] latLong = preferences.getLocation();
        final int calculationMethod = preferences.getCalculationMethodIndex();

        mLatitudeText = (EditText)findViewById(R.id.latitude);
        mLatitudeText.setText(Float.toString(latLong[0]));

        mLongitudeText = (EditText)findViewById(R.id.longitude);
        mLongitudeText.setText(Float.toString(latLong[1]));

        mCalculationMethods = (Spinner)findViewById(R.id.calculation_methods);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.calculation_methods,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCalculationMethods.setAdapter(adapter);
        mCalculationMethods.setSelection(calculationMethod);

        ((ImageButton)findViewById(R.id.lookup_gps)).setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                Location currentLocation = preferences.getCurrentLocation(mContext);
                if (currentLocation != null) {
                    mLatitudeText.setText(Double.toString(currentLocation.getLatitude()));
                    mLongitudeText.setText(Double.toString(currentLocation.getLongitude()));
                } else {
                    mLatitudeText.setText(null);
                    mLongitudeText.setText(null);
                }
            }
        });

        ((Button)findViewById(R.id.save_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                float newLatitude = parseFloat(mLatitudeText, latLong[0]);
                float newLongitude = parseFloat(mLongitudeText, latLong[1]);
                int newCalculationMethod = mCalculationMethods.getSelectedItemPosition();
                // Check if any of the values has changed
                if (newLatitude != latLong[0] || newLongitude != latLong[1]
                        || newCalculationMethod != calculationMethod) {
                    preferences.setLocation(newLatitude, newLongitude);
                    preferences.setCalculationMethodIndex(newCalculationMethod);
                    Schedule.setSettingsDirty();
                }
                dismiss();
            }
        });

        ((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mCalculationMethods.setSelection(CONSTANT.DEFAULT_CALCULATION_METHOD);
            }
        });
    }

    private static float parseFloat(EditText et, float defaultValue) {
        try {
            return Float.parseFloat(et.getText().toString());
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }
}
