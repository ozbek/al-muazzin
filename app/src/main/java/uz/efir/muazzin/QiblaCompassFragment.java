package uz.efir.muazzin;

import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Qibla;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import islam.adhanalarm.Preferences;
import islam.adhanalarm.view.QiblaCompassView;

public class QiblaCompassFragment extends Fragment {
    private static final String TAG = QiblaCompassFragment.class.getSimpleName();
    private static final String PATTERN = "#.###";
    private static DecimalFormat sDecimalFormat;
    private static SensorManager sSensorManager;
    private static float sQiblaDirection = 0f;
    private static SensorListener sOrientationListener;
    private static boolean isTrackingOrientation = false;

    // Helper class to convert decimal degrees to DMS
    private static class Dms {
        private final int degree;
        private final int minute;
        private final double second;

        Dms(double decimalValue) {
            this.degree = (int) decimalValue;
            double absDecimal = Math.abs(decimalValue);
            double absDegree = Math.abs(this.degree);
            this.minute = (int) ((absDecimal - absDegree) * 60);
            this.second = (absDecimal * 3600) - (absDegree * 3600) - (this.minute * 60);
        }

        int getDegree() {
            return degree;
        }

        int getMinute() {
            return minute;
        }

        double getSecond() {
            return second;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        try {
            sDecimalFormat = new DecimalFormat(PATTERN);
        } catch (AssertionError ae) {
            Log.wtf(TAG, "Could not construct DecimalFormat", ae);
            Log.d(TAG, "Will try with Locale.US");
            NumberFormat format = NumberFormat.getInstance(Locale.US);
            if (format instanceof DecimalFormat) {
                sDecimalFormat = (DecimalFormat) format;
                sDecimalFormat.applyPattern(PATTERN);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.tab_qibla, container, false);
        final QiblaCompassView qiblaCompassView = rootView
                .findViewById(R.id.qibla_compass);
        qiblaCompassView.setConstants(rootView.findViewById(R.id.bearing_north),
                getText(R.string.bearing_north),
                rootView.findViewById(R.id.bearing_qibla),
                getText(R.string.bearing_qibla));
        sOrientationListener = new android.hardware.SensorListener() {
            @Override
            public void onSensorChanged(int s, float[] v) {
                float northDirection = v[SensorManager.DATA_X];
                qiblaCompassView.setDirections(northDirection, sQiblaDirection);
            }

            @Override
            public void onAccuracyChanged(int s, int a) {
            }
        };

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDms();
        if (!isTrackingOrientation) {
            isTrackingOrientation = sSensorManager.registerListener(sOrientationListener,
                    SensorManager.SENSOR_ORIENTATION);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isTrackingOrientation) {
            sSensorManager.unregisterListener(sOrientationListener);
            isTrackingOrientation = false;
        }
    }

    /**
     * Add Latitude, Longitude and Qibla DMS location
     */
    private void updateDms() {
        float[] latLon = Preferences.getInstance(getActivity()).getLocation();
        Dms latitude = new Dms(latLon[0]);
        Dms longitude = new Dms(latLon[1]);

        Coordinates coordinates = new Coordinates(latLon[0], latLon[1]);
        Qibla qibla = new Qibla(coordinates);
        sQiblaDirection = (float) qibla.direction;
        Dms qiblaDms = new Dms(qibla.direction);

        View rootView = getView();
        TextView tv = rootView.findViewById(R.id.current_latitude);
        tv.setText(getString(R.string.degree_minute_second, latitude.getDegree(),
                latitude.getMinute(), sDecimalFormat.format(latitude.getSecond())));

        tv = rootView.findViewById(R.id.current_longitude);
        tv.setText(getString(R.string.degree_minute_second, longitude.getDegree(),
                longitude.getMinute(), sDecimalFormat.format(longitude.getSecond())));

        tv = rootView.findViewById(R.id.current_qibla);
        tv.setText(getString(R.string.degree_minute_second, qiblaDms.getDegree(),
                qiblaDms.getMinute(), sDecimalFormat.format(qiblaDms.getSecond())));
    }
}
