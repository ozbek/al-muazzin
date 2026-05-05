package uz.efir.muazzin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Locale

class Muazzin : AppCompatActivity(), CalculationSettingsDialog.LocationProvider {
    public override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muazzin)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.activity_muazzin)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_location_calculation -> {
                    CalculationSettingsDialog(this, this).show()
                    true
                }

                R.id.menu_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pager)) { v, insets ->
            v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_ACCESS_LOCATION
            )
        } else {
            // Location permission is already granted
            initCalculationDefaults()
            setupViewPager()
            requestNotificationPermission()
        }
    }

    private fun setupViewPager() {
        val viewPager = findViewById<ViewPager2>(R.id.pager)
        viewPager.adapter = MuazzinAdapter(this)
        val indicator = findViewById<TabLayout>(R.id.indicator)
        TabLayoutMediator(indicator, viewPager) { tab, position ->
            tab.setText(if (position == 1) R.string.qibla else R.string.today)
        }.attach()
        val appBar = findViewById<AppBarLayout>(R.id.appbar)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                appBar.setExpanded(true, true)
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == REQUEST_ACCESS_LOCATION) {
            initCalculationDefaults()
            setupViewPager()
            // After the location permission request has been answered,
            // request the notification permission with a delay to prevent screen flickering.
            Handler(Looper.getMainLooper()).postDelayed(
                { this.requestNotificationPermission() }, 1000
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun initCalculationDefaults() {
        val preferences: Preferences = Preferences.getInstance(this)
        if (!preferences.isCalculationMethodSet) {
            val country = Locale.getDefault().isO3Country.uppercase()
            CalculationMethodCatalog.indexForCountry(country)?.let {
                preferences.calculationMethodIndex = it
            }
        }

        if (!preferences.isLocationSet) {
            val currentLocation = getCurrentLocation(this)
            if (currentLocation != null) {
                preferences.location = currentLocation
            }
        }
    }

    private fun getCurrentLocation(context: Context): Location? {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        var currentLocation: Location? = null
        try {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (currentLocation == null && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                currentLocation =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
        } catch (_: IllegalArgumentException) {
            return null
        }
        return currentLocation
    }

    override val latestLocation: Location?
        get() = getCurrentLocation(this)

    companion object {
        private const val REQUEST_ACCESS_LOCATION = 1001
        private const val REQUEST_POST_NOTIFICATIONS = 1002
    }
}
