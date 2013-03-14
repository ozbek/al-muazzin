package islam.adhanalarm.dialog;

import uz.efir.muazzin.R;
import islam.adhanalarm.VARIABLE;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class FilePathDialog extends Dialog {

    private int timeIndex = -1;

    public FilePathDialog(Context context, int timeIndex) {
        super(context);
        this.timeIndex = timeIndex;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.file_path);
        setTitle(R.string.file_path_example);

        ((EditText)findViewById(R.id.file_path)).setText(VARIABLE.settings.getString("notificationCustomFile" + timeIndex, ""));

        ((Button)findViewById(R.id.save_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = VARIABLE.settings.edit();
                editor.putString("notificationCustomFile" + timeIndex, ((EditText)findViewById(R.id.file_path)).getText().toString());
                editor.commit();
                dismiss();
            }
        });
        ((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ((EditText)findViewById(R.id.file_path)).setText("");
            }
        });
    }
}