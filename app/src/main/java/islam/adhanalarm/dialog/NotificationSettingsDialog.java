package islam.adhanalarm.dialog;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialog;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Preferences;
import uz.efir.muazzin.R;

public class NotificationSettingsDialog extends AppCompatDialog {
    private static Preferences sPreferences;

    public NotificationSettingsDialog(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Context context = getContext();
        setContentView(R.layout.settings_notification);
        setTitle(R.string.notification);
        sPreferences = Preferences.getInstance(context);

        final int[] notificationIds = new int[]{
                R.id.notification_fajr,
                R.id.notification_sunrise,
                R.id.notification_dhuhr,
                R.id.notification_asr,
                R.id.notification_maghrib,
                R.id.notification_ishaa
        };

        for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
            ArrayList<String> notificationMethods = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.notification_methods)));

            if (i == CONSTANT.SUNRISE) {
                notificationMethods.remove(context.getString(R.string.adhan));
            } else {
                notificationMethods.remove(context.getString(R.string.beep));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, notificationMethods);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner notification = findViewById(notificationIds[i]);
            notification.setAdapter(adapter);

            // Validate the saved preference to prevent crash from stale data
            int savedPosition = sPreferences.getNotificationMethod(i);
            if (savedPosition >= adapter.getCount()) {
                // The saved index is out of bounds for the new list, reset to a safe default.
                savedPosition = (i == CONSTANT.SUNRISE ? 0 : 1); // 0="None", 1="Default"
                sPreferences.setNotificationMethod(i, savedPosition);
            }
            notification.setSelection(savedPosition);
        }

        Button saveSettings = findViewById(R.id.save_settings);
        saveSettings.setOnClickListener(v -> {
            Spinner spinner;
            for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
                spinner = findViewById(notificationIds[i]);
                sPreferences.setNotificationMethod(i, spinner.getSelectedItemPosition());
            }
            dismiss();
        });

        Button resetSettings = findViewById(R.id.reset_settings);
        resetSettings.setOnClickListener(v -> {
            Spinner spinner;
            for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
                spinner = findViewById(notificationIds[i]);
                spinner.setSelection(i == CONSTANT.SUNRISE ? 0 : 1); // 0="None", 1="Default"
            }
        });
    }
}
