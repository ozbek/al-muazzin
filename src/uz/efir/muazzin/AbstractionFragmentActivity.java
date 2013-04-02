package uz.efir.muazzin;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AbstractionFragmentActivity extends SherlockFragmentActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public void onResume() {
        // apply language, theme, etc here
        super.onResume();
    }
}