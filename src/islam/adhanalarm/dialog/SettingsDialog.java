package islam.adhanalarm.dialog;

import islam.adhanalarm.Preferences;
import islam.adhanalarm.Schedule;
import islam.adhanalarm.util.LocaleManager;

import java.util.GregorianCalendar;

import uz.efir.muazzin.R;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class SettingsDialog extends Dialog {

    private CheckBox mBasmalaCheckBox;
    private Context mContext;
    private LocaleManager mLocaleManager;
    private static MediaPlayer sMediaPlayer;

    public SettingsDialog(Context context, LocaleManager localeManager) {
        super(context);
        mContext = context;
        mLocaleManager = localeManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings);
        setTitle(R.string.settings);

        final Preferences preferences = Preferences.getInstance(mContext);

        double gmtOffset = Schedule.getGMTOffset();
        String plusMinusGMT = gmtOffset < 0 ? "" + gmtOffset : "+" + gmtOffset;
        String daylightTime = Schedule.isDaylightSavings() ? " " + mContext.getString(R.string.daylight_savings) : "";
        ((TextView)findViewById(R.id.display_time_zone)).setText(mContext.getString(R.string.system_time_zone) + ": " + mContext.getString(R.string.gmt) + plusMinusGMT + " (" + new GregorianCalendar().getTimeZone().getDisplayName() + daylightTime + ")");

        ((Button)findViewById(R.id.set_notification)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new NotificationSettingsDialog(mContext).show();
            }
        });

        ((Button)findViewById(R.id.set_interface)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new InterfaceSettingsDialog(mContext, mLocaleManager).show();
            }
        });

        ((Button)findViewById(R.id.set_advanced)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new AdvancedSettingsDialog(mContext).show();
            }
        });

        mBasmalaCheckBox = (CheckBox)findViewById(R.id.bismillah_on_boot_up);
        mBasmalaCheckBox.setChecked(preferences.getBasmalaEnabled());
        mBasmalaCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.setBasmalaEnabled(isChecked);
                if (isChecked) {
                    sMediaPlayer = MediaPlayer.create(mContext, R.raw.bismillah);
                    sMediaPlayer.setScreenOnWhilePlaying(true);
                    sMediaPlayer.start();
                } else {
                    if (sMediaPlayer != null) sMediaPlayer.stop();
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mLocaleManager.isDirty()) {
            dismiss();
        } else if (hasFocus) {
            // Technically we should do it only when Calculation or Advanced settings have changed,
            // but this is easier
            Schedule.setSettingsDirty();
        }
    }
}
