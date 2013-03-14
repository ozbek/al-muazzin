package islam.adhanalarm.dialog;

import java.util.Arrays;
import java.util.ArrayList;
import islam.adhanalarm.CONSTANT;
import uz.efir.muazzin.R;
import islam.adhanalarm.VARIABLE;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class NotificationSettingsDialog extends Dialog {

    public NotificationSettingsDialog(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings_notification);
        setTitle(R.string.notification);

        final int[] notificationIds = new int[]{R.id.notification_fajr, R.id.notification_sunrise, R.id.notification_dhuhr, R.id.notification_asr, R.id.notification_maghrib, R.id.notification_ishaa};
        for(short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
            ArrayList<String> notificationMethods = new ArrayList<String>(Arrays.asList(getContext().getResources().getStringArray(R.array.notification_methods)));
            notificationMethods.remove(i == CONSTANT.SUNRISE ? 3 : 2);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, notificationMethods);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner notification = (Spinner)findViewById(notificationIds[i]);
            notification.setAdapter(adapter);
            notification.setSelection(VARIABLE.settings.getInt("notificationMethod" + i, i == CONSTANT.SUNRISE ? CONSTANT.NOTIFICATION_NONE : CONSTANT.NOTIFICATION_DEFAULT));
            notification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                private boolean passedOnceOnLayout = false;
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(passedOnceOnLayout && position == 3) {
                        int timeIndex = CONSTANT.FAJR;
                        for(; timeIndex < CONSTANT.NEXT_FAJR && notificationIds[timeIndex] != parent.getId(); timeIndex++);
                        new FilePathDialog(parent.getContext(), timeIndex).show();
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
                for(short i = CONSTANT.FAJR; i < CONSTANT.NEXT_FAJR; i++) {
                    SharedPreferences.Editor editor = VARIABLE.settings.edit();
                    editor.putInt("notificationMethod" + i, ((Spinner)findViewById(notificationIds[i])).getSelectedItemPosition());
                    editor.commit();
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