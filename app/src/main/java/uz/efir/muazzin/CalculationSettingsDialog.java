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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import java.util.Locale;

public class CalculationSettingsDialog extends AppCompatDialog {

    public interface LocationProvider {
        Location getLatestLocation();
    }

    private final Context mContext;
    private final LocationProvider mLocationProvider;
    private EditText mLatitudeText;
    private EditText mLongitudeText;
    private Spinner mCalculationMethods;
    private EditText mOffsetMinutesText;

    public CalculationSettingsDialog(Context context, LocationProvider locationProvider) {
        super(context);
        mContext = context;
        mLocationProvider = locationProvider;
    }

    private double parseDouble(@NonNull EditText et, double defaultValue) {
        try {
            return Double.parseDouble(et.getText().toString());
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    private int parseInt(@NonNull EditText et, int defaultValue) {
        try {
            return Integer.parseInt(et.getText().toString());
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    @NonNull
    private String formatDouble(double value) {
        return String.format(Locale.getDefault(), "%.4f", value);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_calculation);
        setTitle(R.string.calculation);

        final Preferences preferences = Preferences.getInstance(mContext);
        final Location location = preferences.getLocation();
        final int calculationMethod = preferences.getCalculationMethodIndex();
        final int offsetMinutes = preferences.getOffsetMinutes();

        mLatitudeText = findViewById(R.id.latitude);
        mLatitudeText.setText(formatDouble(location.getLatitude()));

        mLongitudeText = findViewById(R.id.longitude);
        mLongitudeText.setText(formatDouble(location.getLongitude()));

        mCalculationMethods = findViewById(R.id.calculation_methods);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.calculation_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCalculationMethods.setAdapter(adapter);
        mCalculationMethods.setSelection(calculationMethod);

        mOffsetMinutesText = findViewById(R.id.offset_minutes);
        mOffsetMinutesText.setText(String.format(Locale.getDefault(), "%d", offsetMinutes));

        ImageButton lookupGps = findViewById(R.id.lookup_gps);
        lookupGps.setOnClickListener(v -> {
            Location newLocation = mLocationProvider.getLatestLocation();
            if (newLocation != null) {
                mLatitudeText.setText(formatDouble(newLocation.getLatitude()));
                mLongitudeText.setText(formatDouble(newLocation.getLongitude()));
            }
        });

        Button saveSettings = findViewById(R.id.save_settings);
        saveSettings.setOnClickListener(v -> {
            double newLatitude = parseDouble(mLatitudeText, location.getLatitude());
            double newLongitude = parseDouble(mLongitudeText, location.getLongitude());
            int newCalculationMethod = mCalculationMethods.getSelectedItemPosition();
            int newOffsetMinutes = parseInt(mOffsetMinutesText, offsetMinutes);

            // Check if any of the values has changed
            if (newLatitude != location.getLatitude() || newLongitude != location.getLongitude() || newCalculationMethod != calculationMethod || newOffsetMinutes != offsetMinutes) {
                Location newLocation = new Location("");
                newLocation.setLatitude(newLatitude);
                newLocation.setLongitude(newLongitude);
                preferences.setLocation(newLocation);
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
