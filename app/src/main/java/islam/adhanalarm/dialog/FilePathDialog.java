package islam.adhanalarm.dialog;

import islam.adhanalarm.Preferences;
import uz.efir.muazzin.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class FilePathDialog extends Dialog {

    private short mTimeIndex = -1;
    private EditText mFilePathText;

    public FilePathDialog(Context context, short timeIndex) {
        super(context);
        mTimeIndex = timeIndex;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.file_path);
        setTitle(R.string.file_path_example);

        final Preferences preferences = Preferences.getInstance(getContext());

        mFilePathText = (EditText)findViewById(R.id.file_path);
        mFilePathText.setText(preferences.getCustomFilePath(mTimeIndex));

        ((Button)findViewById(R.id.save_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                preferences.setCustomFilePath(mFilePathText.getText().toString());
                dismiss();
            }
        });

        ((Button)findViewById(R.id.reset_settings)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mFilePathText.setText("");
            }
        });
    }
}
