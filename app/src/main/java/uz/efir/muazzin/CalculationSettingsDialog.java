package uz.efir.muazzin;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatDialog;

import java.util.Locale;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Preferences;

public class CalculationSettingsDialog extends AppCompatDialog {
    private final Context mContext;
    private EditText mLatitudeText;
    private EditText mLongitudeText;
    private Spinner mCalculationMethods;
    private EditText mOffsetMinutesText;

    public CalculationSettingsDialog(Context context) {
        super(context);
        mContext = context;
    }

    private static float parseFloat(EditText et, float defaultValue) {
        try {
            return Float.parseFloat(et.getText().toString());
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_calculation);
        setTitle(R.string.calculation);

        final Preferences preferences = Preferences.getInstance(mContext);
        final float[] latLong = preferences.getLocation();
        final int calculationMethod = preferences.getCalculationMethodIndex();
        final int offsetMinutes = preferences.getOffsetMinutes();

        mLatitudeText = findViewById(R.id.latitude);
        mLatitudeText.setText(Float.toString(latLong[0]));

        mLongitudeText = findViewById(R.id.longitude);
        mLongitudeText.setText(Float.toString(latLong[1]));

        mCalculationMethods = findViewById(R.id.calculation_methods);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.calculation_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCalculationMethods.setAdapter(adapter);
        mCalculationMethods.setSelection(calculationMethod);

        mOffsetMinutesText = findViewById(R.id.offset_minutes);
        mOffsetMinutesText.setText(String.format(Locale.getDefault(), "%d", offsetMinutes));

        ImageButton lookupGps = findViewById(R.id.lookup_gps);
        lookupGps.setOnClickListener(v -> {
            Location currentLocation = preferences.getCurrentLocation(mContext);
            if (currentLocation != null) {
                mLatitudeText.setText(String.format(Locale.getDefault(), "%.4f", currentLocation.getLatitude()));
                mLongitudeText.setText(String.format(Locale.getDefault(), "%.4f", currentLocation.getLongitude()));
            } else {
                mLatitudeText.setText(null);
                mLongitudeText.setText(null);
            }
        });

        Button saveSettings = findViewById(R.id.save_settings);
        saveSettings.setOnClickListener(v -> {
            float newLatitude = parseFloat(mLatitudeText, latLong[0]);
            float newLongitude = parseFloat(mLongitudeText, latLong[1]);
            int newCalculationMethod = mCalculationMethods.getSelectedItemPosition();
            int newOffsetMinutes;
            try {
                newOffsetMinutes = Integer.parseInt(mOffsetMinutesText.getText().toString());
            } catch (NumberFormatException nfe) {
                newOffsetMinutes = offsetMinutes;
            }

            // Check if any of the values has changed
            if (newLatitude != latLong[0] || newLongitude != latLong[1] || newCalculationMethod != calculationMethod || newOffsetMinutes != offsetMinutes) {
                preferences.setLocation(newLatitude, newLongitude);
                preferences.setCalculationMethodIndex(newCalculationMethod);
                preferences.setOffsetMinutes(newOffsetMinutes);

                Intent i = new Intent(Utils.ACTION_UPDATE_UI);
                i.setPackage(BuildConfig.APPLICATION_ID);
                mContext.sendBroadcast(i);
            }
            dismiss();
        });

        Button resetSettings = findViewById(R.id.reset_settings);
        resetSettings.setOnClickListener(v -> {
            mCalculationMethods.setSelection(CONSTANT.DEFAULT_CALCULATION_METHOD);
            mOffsetMinutesText.setText("0");
        });
    }
}
