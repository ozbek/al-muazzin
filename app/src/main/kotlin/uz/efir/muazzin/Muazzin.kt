package uz.efir.muazzin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        when {
            isLocationPermissionGranted() -> {
                initCalculationDefaults()
                setupViewPager()
                requestNotificationPermission()
            }

            canShowLocationRationale() -> {
                initCalculationDefaults()
                setupViewPager()
                showLocationPermissionDialog()
            }

            else -> ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_LOCATION_PERMISSION
            )
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
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                initCalculationDefaults()
                setupViewPager()
                requestNotificationPermission()
            }

            REQUEST_LOCATION_PERMISSION_REGRANT -> {
                if (isLocationPermissionGranted()) {
                    initCalculationDefaults()
                }
            }
        }
    }

    override fun requestLocationPermission() {
        if (canShowLocationRationale()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_LOCATION_PERMISSION_REGRANT
            )
        } else {
            showLocationPermissionDialog()
        }
    }

    override val latestLocation: Location?
        get() = getCurrentLocation()

    private fun showLocationPermissionDialog() {
        var requestNotificationPermission = true
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.location_permission_missing_title)
            .setMessage(
                getString(
                    R.string.location_permission_missing_message,
                    getString(R.string.calculation)
                )
            )
            .setPositiveButton(R.string.grant_access) { _, _ ->
                requestNotificationPermission = false
                grantOrOpenSettings()
            }
            .setNegativeButton(R.string.dismiss, null)
            .setOnDismissListener {
                if (requestNotificationPermission) requestNotificationPermission()
            }
            .show()
    }

    private fun grantOrOpenSettings() {
        if (canShowLocationRationale()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_LOCATION_PERMISSION_REGRANT
            )
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun canShowLocationRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Handler(Looper.getMainLooper()).postDelayed({
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATIONS_PERMISSION
                )
            }, 2000)
        }
    }

    private fun initCalculationDefaults() {
        val preferences: Preferences = Preferences.getInstance(this)

        val location: Location? = when {
            preferences.isLocationSet -> preferences.location
            else -> getCurrentLocation()?.also { preferences.location = it }
        }

        if (!preferences.isCalculationMethodSet) {
            autoPickCalculationMethod(preferences, location)
        }
    }

    /**
     * Derive country from the device's current location, fall back to the locale's country
     */
    private fun autoPickCalculationMethod(preferences: Preferences, location: Location?) {
        val applyCountry: (String?) -> Unit = { alpha2 ->
            // Late re-check: the user may have picked a method during the in-flight geocode.
            if (!preferences.isCalculationMethodSet) {
                val alpha3 = alpha2?.takeIf { it.isNotBlank() }?.let { code ->
                    @Suppress("DEPRECATION")
                    runCatching { Locale("", code).isO3Country }.getOrNull()
                }
                val country = alpha3?.takeIf { it.isNotBlank() }
                    ?: Locale.getDefault().isO3Country
                val idx = CalculationMethodCatalog.indexForCountry(country.uppercase())
                idx?.let { preferences.calculationMethodIndex = it }
            }
        }

        if (location == null || !Geocoder.isPresent()) {
            applyCountry(null)
            return
        }
        val geocoder = Geocoder(this, Locale.US)
        val lat = location.latitude
        val lng = location.longitude
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    applyCountry(addresses.firstOrNull()?.countryCode)
                }

                override fun onError(errorMessage: String?) {
                    applyCountry(null)
                }
            })
        } else {
            Thread {
                val countryCode = runCatching {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lng, 1)
                }.getOrNull()?.firstOrNull()?.countryCode
                runOnUiThread { applyCountry(countryCode) }
            }.start()
        }
    }

    private fun getCurrentLocation(): Location? {
        val locationManager = getSystemService(LocationManager::class.java)
        var currentLocation: Location? = null
        try {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (currentLocation == null && ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
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

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
        private const val REQUEST_LOCATION_PERMISSION_REGRANT = 1002
        private const val REQUEST_NOTIFICATIONS_PERMISSION = 1003
    }
}
