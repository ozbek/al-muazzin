package uz.efir.azon;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.os.Bundle;

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