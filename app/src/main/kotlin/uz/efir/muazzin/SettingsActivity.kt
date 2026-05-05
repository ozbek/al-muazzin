package uz.efir.muazzin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scroll)) { v, insets ->
            v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }

        val preferences: Preferences = Preferences.getInstance(this)
        val notificationIds = intArrayOf(
            R.id.notification_fajr,
            R.id.notification_sunrise,
            R.id.notification_dhuhr,
            R.id.notification_asr,
            R.id.notification_maghrib,
            R.id.notification_ishaa
        )
        for (i in PrayerTime.FAJR.ordinal..<PrayerTime.NEXT_FAJR.ordinal) {
            val notificationMethods =
                ArrayList<String?>(listOf(*getResources().getStringArray(R.array.notification_methods)))

            if (i == PrayerTime.SUNRISE.ordinal) {
                notificationMethods.remove(getString(R.string.adhan))
            } else {
                notificationMethods.remove(getString(R.string.beep))
            }

            val adapter = ArrayAdapter<String?>(
                this, android.R.layout.simple_spinner_item, notificationMethods
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val notification = findViewById<Spinner>(notificationIds[i])
            notification.adapter = adapter

            var savedPosition = preferences.getNotificationMethod(i)
            if (savedPosition >= adapter.count) {
                savedPosition = (if (i == PrayerTime.SUNRISE.ordinal) 0 else 1)
                preferences.setNotificationMethod(i, savedPosition)
            }
            notification.setSelection(savedPosition)
        }

        val saveSettings = findViewById<Button>(R.id.save_settings)
        saveSettings.setOnClickListener { _: View? ->
            var spinner: Spinner
            for (i in PrayerTime.FAJR.ordinal..<PrayerTime.NEXT_FAJR.ordinal) {
                spinner = findViewById(notificationIds[i])
                preferences.setNotificationMethod(i, spinner.selectedItemPosition)
            }
            finish()
        }

        val resetSettings = findViewById<Button>(R.id.reset_settings)
        resetSettings.setOnClickListener { _: View? ->
            var spinner: Spinner
            for (i in PrayerTime.FAJR.ordinal..<PrayerTime.NEXT_FAJR.ordinal) {
                spinner = findViewById(notificationIds[i])
                spinner.setSelection(if (i == PrayerTime.SUNRISE.ordinal) 0 else 1)
            }
        }
    }
}
