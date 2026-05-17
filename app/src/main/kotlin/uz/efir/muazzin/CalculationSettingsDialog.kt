package uz.efir.muazzin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import java.util.Locale

class CalculationSettingsDialog(
    private val mContext: Context, private val mLocationProvider: LocationProvider
) : AppCompatDialog(mContext, R.style.Theme_Muazzin_Dialog) {
    interface LocationProvider {
        fun requestLocationPermission()
        val latestLocation: Location?
    }

    private var mLatitudeText: EditText? = null
    private var mLongitudeText: EditText? = null
    private var mCalculationMethods: Spinner? = null
    private var mOffsetMinutesText: EditText? = null

    private fun parseDouble(et: EditText?, defaultValue: Double): Double {
        return try {
            et?.text.toString().toDouble()
        } catch (_: NumberFormatException) {
            defaultValue
        }
    }

    private fun parseInt(et: EditText?, defaultValue: Int): Int {
        return try {
            et?.text.toString().toInt()
        } catch (_: NumberFormatException) {
            defaultValue
        }
    }

    private fun formatDouble(value: Double): String {
        return String.format(Locale.getDefault(), "%.4f", value)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_calculation)
        setTitle(R.string.calculation)

        val preferences: Preferences = Preferences.getInstance(mContext)
        val location = preferences.location
        val calculationMethod = preferences.calculationMethodIndex
        val offsetMinutes = preferences.offsetMinutes

        mLatitudeText = findViewById(R.id.latitude)
        mLatitudeText?.setText(formatDouble(location.latitude))

        mLongitudeText = findViewById(R.id.longitude)
        mLongitudeText?.setText(formatDouble(location.longitude))

        mCalculationMethods = findViewById(R.id.calculation_methods)
        val adapter = ArrayAdapter.createFromResource(
            mContext, R.array.calculation_methods, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mCalculationMethods?.adapter = adapter
        mCalculationMethods?.setSelection(calculationMethod)

        mOffsetMinutesText = findViewById(R.id.offset_minutes)
        mOffsetMinutesText?.setText(String.format(Locale.getDefault(), "%d", offsetMinutes))

        val lookupGps = findViewById<Button?>(R.id.lookup_gps)
        lookupGps?.setOnClickListener { _: View? ->
            val locationGranted = ContextCompat.checkSelfPermission(
                mContext, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                mContext, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!locationGranted) {
                mLocationProvider.requestLocationPermission()
                return@setOnClickListener
            }
            val newLocation = mLocationProvider.latestLocation
            if (newLocation != null) {
                mLatitudeText?.setText(formatDouble(newLocation.latitude))
                mLongitudeText?.setText(formatDouble(newLocation.longitude))
            }
        }

        val saveButton = findViewById<Button?>(R.id.save)
        saveButton?.setOnClickListener { _: View? ->
            val newLatitude = parseDouble(mLatitudeText, location.latitude)
            val newLongitude = parseDouble(mLongitudeText, location.longitude)
            val newCalculationMethod =
                mCalculationMethods?.selectedItemPosition ?: calculationMethod
            val newOffsetMinutes = parseInt(mOffsetMinutesText, offsetMinutes)

            // Check if any of the values has changed
            if (newLatitude != location.latitude || newLongitude != location.longitude || newCalculationMethod != calculationMethod || newOffsetMinutes != offsetMinutes) {
                val newLocation = Location("")
                newLocation.latitude = newLatitude
                newLocation.longitude = newLongitude
                preferences.location = newLocation
                preferences.calculationMethodIndex = newCalculationMethod
                preferences.offsetMinutes = newOffsetMinutes

                val i = Intent(PrayerTimesFragment.ACTION_UPDATE_UI)
                i.setPackage(BuildConfig.APPLICATION_ID)
                mContext.sendBroadcast(i)
            }
            dismiss()
        }

        val dismissButton = findViewById<Button?>(R.id.dismiss)
        dismissButton?.setOnClickListener { _: View? -> dismiss() }
    }

    override fun onStart() {
        super.onStart()
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
