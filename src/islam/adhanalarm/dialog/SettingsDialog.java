package islam.adhanalarm.dialog;

import islam.adhanalarm.Schedule;
import islam.adhanalarm.VARIABLE;
import islam.adhanalarm.util.LocaleManager;

import java.util.GregorianCalendar;

import uz.efir.muazzin.R;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class SettingsDialog extends Dialog {

    private LocaleManager localeManager;
    private static MediaPlayer mediaPlayer;

    public SettingsDialog(Context context, LocaleManager localeManager) {
        super(context);
        this.localeManager = localeManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings);
        setTitle(R.string.settings);

        double gmtOffset = Schedule.getGMTOffset();
        String plusMinusGMT = gmtOffset < 0 ? "" + gmtOffset : "+" + gmtOffset;
        String daylightTime = Schedule.isDaylightSavings() ? " " + getContext().getString(R.string.daylight_savings) : "";
        ((TextView)findViewById(R.id.display_time_zone)).setText(getContext().getString(R.string.system_time_zone) + ": " + getContext().getString(R.string.gmt) + plusMinusGMT + " (" + new GregorianCalendar().getTimeZone().getDisplayName() + daylightTime + ")");

        ((Button)findViewById(R.id.set_notification)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new NotificationSettingsDialog(v.getContext()).show();
            }
        });
        ((Button)findViewById(R.id.set_interface)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new InterfaceSettingsDialog(v.getContext(), localeManager).show();
            }
        });
        ((Button)findViewById(R.id.set_advanced)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new AdvancedSettingsDialog(v.getContext()).show();
            }
        });
        ((CheckBox)findViewById(R.id.bismillah_on_boot_up)).setChecked(VARIABLE.settings.getBoolean("bismillahOnBootUp", false));
        ((CheckBox)findViewById(R.id.bismillah_on_boot_up)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.bismillah);
                    mediaPlayer.setScreenOnWhilePlaying(true);
                    mediaPlayer.start();
                } else {
                    if(mediaPlayer != null) mediaPlayer.stop();
                }
                SharedPreferences.Editor editor = VARIABLE.settings.edit();
                editor.putBoolean("bismillahOnBootUp", isChecked);
                editor.commit();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && localeManager.isDirty()) {
            dismiss();
        } else if (hasFocus) {
            Schedule.setSettingsDirty(); // Technically we should do it only when they have changed i.e. if Calculation or Advanced settings changed but this is easier
        }
    }
}