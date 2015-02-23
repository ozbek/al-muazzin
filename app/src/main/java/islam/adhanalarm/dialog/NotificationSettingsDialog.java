package islam.adhanalarm.dialog;

import islam.adhanalarm.CONSTANT;
import islam.adhanalarm.Preferences;

import java.util.ArrayList;
import java.util.Arrays;

import uz.efir.muazzin.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class NotificationSettingsDialog extends Dialog {
    private static Preferences sPrefereces;

    public NotificationSettingsDialog(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Context context = getContext();
        setContentView(R.layout.settings_notification);
        setTitle(R.string.notification);
        sPrefereces = Preferences.getInstance(context);

        final int[] notificationIds = new int[]{R.id.notification_fajr, R.id.notification_sunrise, R.id.notification_dhuhr, R.id.notification_asr, R.id.notification_maghrib, R.id.notification_ishaa};
        for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
            ArrayList<String> notificationMethods = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.notification_methods)));
            notificationMethods.remove(i == CONSTANT.SUNRISE ? 3 : 2);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, notificationMethods);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner notification = (Spinner)findViewById(notificationIds[i]);
            notification.setAdapter(adapter);
            notification.setSelection(sPrefereces.getNotificationMethod(i));
            notification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                private boolean passedOnceOnLayout = false;
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (passedOnceOnLayout && position == 3) {
                        short timeIndex = CONSTANT.FAJR;
                        for(; timeIndex < CONSTANT.NEXT_FAJR && notificationIds[timeIndex] != parent.getId(); timeIndex++);
                        new FilePathDialog(context, timeIndex).show();
                    } else {
                        passedOnceOnLayout = true;
                    }
                }
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        ((Button)findViewById(R.id.save_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                for (short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
                    sPrefereces.setNotificationMethod(i, ((Spinner)findViewById(notificationIds[i])).getSelectedItemPosition());
                }
                dismiss();
            }
        });
        ((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                for(short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
                    ((Spinner)findViewById(notificationIds[i])).setSelection(i == CONSTANT.SUNRISE ? 0 : 1);
                }
            }
        });
    }
}