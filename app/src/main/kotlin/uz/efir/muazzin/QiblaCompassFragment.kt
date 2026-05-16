package uz.efir.muazzin

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Qibla
import kotlin.math.abs
import kotlin.math.roundToInt

class QiblaCompassFragment : Fragment(), SensorEventListener {
    private class Dms(decimalValue: Double) {
        val degree: Int = decimalValue.toInt()
        val minute: Int
        val second: Int

        init {
            val absDecimal = abs(decimalValue)
            val absDegree = abs(this.degree).toDouble()
            this.minute = ((absDecimal - absDegree) * 60).toInt()
            this.second = ((absDecimal * 3600) - (absDegree * 3600) - (this.minute * 60)).toInt()
        }
    }

    private var sensorManager: SensorManager? = null
    private var rotationVectorSensor: Sensor? = null
    private var qiblaCompassView: QiblaCompassView? = null
    private var bearingHeadingView: TextView? = null
    private var bearingQiblaView: TextView? = null
    private var qiblaDirection = 0f
    private var declinationDegrees = 0f
    private var smoothedAzimuth = Float.NaN

    private val rotationMatrix = FloatArray(9)
    private val remappedRotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.tab_qibla, container, false)
        qiblaCompassView = rootView.findViewById(R.id.qibla_compass)
        bearingHeadingView = rootView.findViewById(R.id.bearing_heading)
        bearingQiblaView = rootView.findViewById(R.id.bearing_qibla)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        updateDms()
        smoothedAzimuth = Float.NaN
        rotationVectorSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        val (axisX, axisY) = when (currentDisplayRotation()) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        SensorManager.remapCoordinateSystem(
            rotationMatrix, axisX, axisY, remappedRotationMatrix
        )
        SensorManager.getOrientation(remappedRotationMatrix, orientationAngles)

        // ROTATION_VECTOR's azimuth is measured from magnetic north; add declination
        // to obtain heading relative to true north (the frame Qibla.direction uses).
        var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        azimuth = normalizeDegrees(azimuth + declinationDegrees)

        smoothedAzimuth = if (smoothedAzimuth.isNaN()) {
            azimuth
        } else {
            lowPassAzimuth(smoothedAzimuth, azimuth)
        }

        qiblaCompassView?.setDirections(smoothedAzimuth, qiblaDirection)
        updateBearingText(smoothedAzimuth)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun updateBearingText(azimuth: Float) {
        val ctx = context ?: return

        bearingHeadingView?.text =
            ctx.getString(R.string.bearing_heading, azimuth.roundToInt().toString())

        // Signed shortest-path delta in (-180, 180]:
        // - means qibla is to the left of the device's heading,
        // + means qibla is to the right of the device's heading.
        val delta = ((qiblaDirection - azimuth + 540f) % 360f) - 180f
        val turn = when {
            abs(delta) < ALIGNMENT_THRESHOLD -> ctx.getString(R.string.qibla_aligned)
            delta > 0 -> ctx.getString(R.string.qibla_turn_right, delta.roundToInt().toString())

            else -> ctx.getString(R.string.qibla_turn_left, abs(delta).roundToInt().toString())
        }
        bearingQiblaView?.text =
            ctx.getString(R.string.bearing_qibla, qiblaDirection.roundToInt().toString(), turn)
    }

    /**
     * Compute the qibla bearing and magnetic declination for the saved location,
     * and write the latitude/longitude DMS values to the header rows.
     */
    private fun updateDms() {
        val location: Location = Preferences.getInstance(requireContext()).location
        val latitude = Dms(location.latitude)
        val longitude = Dms(location.longitude)

        val coordinates = Coordinates(location.latitude, location.longitude)
        qiblaDirection = Qibla(coordinates).direction.toFloat()

        declinationDegrees = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        ).declination

        val rootView = view
        var tv = rootView?.findViewById<TextView>(R.id.current_latitude)
        tv?.text = getString(
            R.string.degree_minute_second,
            latitude.degree,
            latitude.minute,
            latitude.second.toString()
        )

        tv = rootView?.findViewById(R.id.current_longitude)
        tv?.text = getString(
            R.string.degree_minute_second,
            longitude.degree,
            longitude.minute,
            longitude.second.toString()
        )
    }

    private fun currentDisplayRotation(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireContext().display.rotation
        } else {
            @Suppress("DEPRECATION")
            requireActivity().windowManager.defaultDisplay.rotation
        }
    }

    private fun lowPassAzimuth(prev: Float, current: Float): Float {
        var diff = current - prev
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return normalizeDegrees(prev + ALPHA * diff)
    }

    private fun normalizeDegrees(value: Float): Float {
        return ((value % 360f) + 360f) % 360f
    }

    companion object {
        private const val ALPHA = 0.15f
        private const val ALIGNMENT_THRESHOLD = 3f
    }
}
