package islam.adhanalarm.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.AdapterView;
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
            notificationMethods.remove(i == CONSTANT.SUNRISE ? 3 : 2);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, notificationMethods);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner notification = (Spinner) findViewById(notificationIds[i]);
            assert notification != null;
            notification.setAdapter(adapter);
            notification.setSelection(sPreferences.getNotificationMethod(i));
            notification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private boolean passedOnceOnLayout = false;

                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (passedOnceOnLayout && position == 3) {
                        short timeIndex = CONSTANT.FAJR;
                        for (; timeIndex < CONSTANT.NEXT_FAJR && notificationIds[timeIndex] != parent.getId(); timeIndex++)
                            ;
                        new FilePathDialog(context, timeIndex).show();
                    } else {
                        passedOnceOnLayout = true;
                    }
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        Button saveSettings = (Button) findViewById(R.id.save_settings);
        assert saveSettings != null;
        saveSettings.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Spinner spinner;
                for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
                    spinner = (Spinner) findViewById(notificationIds[i]);
                    assert spinner != null;
                    sPreferences.setNotificationMethod(i, spinner.getSelectedItemPosition());
                }
                dismiss();
            }
        });
        Button resetSettings = (Button) findViewById(R.id.reset_settings);
        assert resetSettings != null;
        resetSettings.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Spinner spinner;
                for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
                    spinner = (Spinner) findViewById(notificationIds[i]);
                    assert spinner != null;
                    spinner.setSelection(i == CONSTANT.SUNRISE ? 0 : 1);
                }
            }
        });
    }
}
