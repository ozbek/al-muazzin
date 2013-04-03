package islam.adhanalarm.dialog;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.VARIABLE;
import uz.efir.muazzin.R;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class CalculationSettingsDialog extends Dialog {

    public CalculationSettingsDialog(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings_calculation);
        setTitle(R.string.calculation);

        float latitude = VARIABLE.settings.getFloat("latitude", -999f);
        float longitude = VARIABLE.settings.getFloat("longitude", -999f);
        ((EditText)findViewById(R.id.latitude)).setText(latitude == -999f ? "" : Float.toString(latitude));
        ((EditText)findViewById(R.id.longitude)).setText(longitude == -999f ? "" : Float.toString(longitude));

        Spinner calculation_methods = (Spinner)findViewById(R.id.calculation_methods);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.calculation_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calculation_methods.setAdapter(adapter);
        calculation_methods.setSelection(VARIABLE.settings.getInt("calculationMethodsIndex", CONSTANT.DEFAULT_CALCULATION_METHOD));

        ((ImageButton)findViewById(R.id.lookup_gps)).setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                Location currentLocation = VARIABLE.getCurrentLocation(getContext());
                if (currentLocation != null) {
                    ((EditText)findViewById(R.id.latitude)).setText(Double.toString(currentLocation.getLatitude()));
                    ((EditText)findViewById(R.id.longitude)).setText(Double.toString(currentLocation.getLongitude()));
                } else {
                    ((EditText)findViewById(R.id.latitude)).setText(null);
                    ((EditText)findViewById(R.id.longitude)).setText(null);
                }
            }
        });

        ((Button)findViewById(R.id.save_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = VARIABLE.settings.edit();
                try {
                    editor.putFloat("latitude", Float.parseFloat(((EditText)findViewById(R.id.latitude)).getText().toString()));
                } catch(Exception ex) {
                    // Invalid latitude
                }
                try {
                    editor.putFloat("longitude", Float.parseFloat(((EditText)findViewById(R.id.longitude)).getText().toString()));
                } catch(Exception ex) {
                    // Invalid longitude
                }
                editor.putInt("calculationMethodsIndex", ((Spinner)findViewById(R.id.calculation_methods)).getSelectedItemPosition());
                editor.commit();
                dismiss();
            }
        });
        ((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ((Spinner)findViewById(R.id.calculation_methods)).setSelection(CONSTANT.DEFAULT_CALCULATION_METHOD);
            }
        });
    }
}
