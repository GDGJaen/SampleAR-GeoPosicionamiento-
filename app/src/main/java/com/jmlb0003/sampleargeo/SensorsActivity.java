package com.jmlb0003.sampleargeo;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.jmlb0003.sampleargeo.Utilities.LowPassFilter;
import com.jmlb0003.sampleargeo.Utilities.Matrix;
import com.jmlb0003.sampleargeo.Utilities.Utilities;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * This activity hasn't user interface and gets sensor information from acelerometer and compass.
 * Furthermore, it gets location information from GPS and network provider.
 */
public class SensorsActivity extends Activity
        implements SensorEventListener, LocationListener {
    private static final String TAG = "SensorsActivity";
    private static final AtomicBoolean computing = new AtomicBoolean(false);

    /**
     * MIN_TIME and MIN_DISTANCE specify the minimum time (in milliseconds and distance (in meters)
     * between location updates
     */
    private static final int MIN_TIME = 30 * 1000;    //30 seconds
    private static final int MIN_DISTANCE = 10; //10 meters

    private static final float temp[] = new float[9];
    //Final rotation matrix
    private static final float ROTATION_MATRIX[] = new float[9];
    //Acelerometer values
    private static final float GRAVITY_ORIENTATION[] = new float[3];
    //Compass values
    private static final float MAGNETIC_ORIENTATION[] = new float[3];

    /**
     * WORLD_COORDINATES stores the location of the device on the world
     */
    private static final Matrix WORLD_COORDINATES = new Matrix();
    /**
     * MAGNETIC_COMPENSATED_COORDINATES and MAGNETIC_NORTH_COMPENSATION are used when compensating for the
     * difference in between the geographical north pole and the magnetic North Pole
     */
    private static final Matrix MAGNETIC_COMPENSATED_COORDINATES = new Matrix();
    /**
     * MAGNETIC_NORTH_COMPENSATION and MAGNETIC_COMPENSATED_COORDINATES are used when compensating for the
     * difference in between the geographical north pole and the magnetic North Pole
     */
    private static final Matrix MAGNETIC_NORTH_COMPENSATION = new Matrix();
    /**
     * X_AXIS_ROTATION is used to store the matrix after it has been rotated by 90 degrees along
     * the X-axis.
     */
    private static final Matrix X_AXIS_ROTATION = new Matrix();

    private static GeomagneticField gmf = null;
    private static SensorManager sensorMgr = null;
    private static Sensor sensorGrav = null;
    private static Sensor sensorMag = null;
    private static LocationManager locationMgr = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();


        //Initialize rotation matrix
        X_AXIS_ROTATION.set(
                1f, 0f, 0f,
                0f, (float) Math.cos(Math.toRadians(-90)), (float) -Math.sin(Math.toRadians(-90)),
                0f, (float) Math.sin(Math.toRadians(-90)), (float) Math.cos(Math.toRadians(-90))
        );

        try {
            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

            //Get Acelerometer
            List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);

            if (sensors.size() > 0) {
                sensorGrav = sensors.get(0);
            }

            //Get compass
            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensors.size() > 0) {
                sensorMag = sensors.get(0);
            }
            sensorMgr.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_NORMAL);
            sensorMgr.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);

            //Get Location with GPS
            locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

            try {

                //Get best last known location
                try {
                    Location gps = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location network = locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (gps != null) {
                        onLocationChanged(gps);
                    } else if (network != null) {
                        onLocationChanged(network);
                    } else {
                        onLocationChanged(ARData.hardFix);
                    }
                } catch (Exception ex2) {
                    onLocationChanged(ARData.hardFix);
                }

                calculateMagneticNorthCompensation();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex1) {
            try {
                if (sensorMgr != null) {
                    sensorMgr.unregisterListener(this, sensorGrav);
                    sensorMgr.unregisterListener(this, sensorMag);
                    sensorMgr = null;
                }
                if (locationMgr != null) {
                    locationMgr.removeUpdates(this);
                    locationMgr = null;
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }


    /**
     * We assign a value to gmf and reassign the value of angleY to the negative declination ,
     * which is the difference between true north (North Pole) and magnetic north (moving toward
     * Siberia at roughly 40 miles/year) of gmf in radians.
     */
    private void calculateMagneticNorthCompensation() {
        double angleY;

        gmf = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(),
                (float) ARData.getCurrentLocation().getLongitude(),
                (float) ARData.getCurrentLocation().getAltitude(),
                System.currentTimeMillis());
        angleY = Math.toRadians(-gmf.getDeclination());

        /**
         * This code is used to first set the value of magneticNorthCompensation and then multiply
         * it with X_AXIS_ROTATION.
         */
        synchronized (MAGNETIC_NORTH_COMPENSATION) {

            MAGNETIC_NORTH_COMPENSATION.toIdentity();

            MAGNETIC_NORTH_COMPENSATION.set((float) Math.cos(angleY),
                    0f,
                    (float) Math.sin(angleY),
                    0f,
                    1f,
                    0f,
                    (float) -Math.sin(angleY),
                    0f,
                    (float) Math.cos(angleY));

            MAGNETIC_NORTH_COMPENSATION.prod(X_AXIS_ROTATION);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            try {
                sensorMgr.unregisterListener(this, sensorGrav);
                sensorMgr.unregisterListener(this, sensorMag);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sensorMgr = null;

            try {
                locationMgr.removeUpdates(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            locationMgr = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onSensorChanged(SensorEvent evt) {
        if (!computing.compareAndSet(false, true)) return;

        float[] smooth = new float[3];
        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            smooth = LowPassFilter.filter(0.5f, 1.0f, evt.values, GRAVITY_ORIENTATION);
            GRAVITY_ORIENTATION[0] = smooth[0];
            GRAVITY_ORIENTATION[1] = smooth[1];
            GRAVITY_ORIENTATION[2] = smooth[2];
        } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smooth = LowPassFilter.filter(2.0f, 4.0f, evt.values, MAGNETIC_ORIENTATION);
            MAGNETIC_ORIENTATION[0] = smooth[0];
            MAGNETIC_ORIENTATION[1] = smooth[1];
            MAGNETIC_ORIENTATION[2] = smooth[2];
        }

        SensorManager.getRotationMatrix(temp, null, GRAVITY_ORIENTATION, MAGNETIC_ORIENTATION);

        /**
         * Here we transform device coordinates saved in temp, to other coordinate system.
         * As a result, we get a rotation matrix for positioning objects on screen
         * @see http://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])
         */
        SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, ROTATION_MATRIX);

        WORLD_COORDINATES.set(ROTATION_MATRIX[0], ROTATION_MATRIX[1], ROTATION_MATRIX[2],
                ROTATION_MATRIX[3], ROTATION_MATRIX[4], ROTATION_MATRIX[5],
                ROTATION_MATRIX[6], ROTATION_MATRIX[7], ROTATION_MATRIX[8]);

        MAGNETIC_COMPENSATED_COORDINATES.toIdentity();

        synchronized (MAGNETIC_NORTH_COMPENSATION) {
            MAGNETIC_COMPENSATED_COORDINATES.prod(MAGNETIC_NORTH_COMPENSATION);
        }

        MAGNETIC_COMPENSATED_COORDINATES.prod(WORLD_COORDINATES);

        MAGNETIC_COMPENSATED_COORDINATES.invert();

        ARData.setRotationMatrix(MAGNETIC_COMPENSATED_COORDINATES);

        computing.set(false);
    }

    public void onProviderDisabled(String provider) {
        //Not Used
    }

    public void onProviderEnabled(String provider) {
        //Not Used
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Not Used
    }

    public void onLocationChanged(Location location) {
        ARData.setCurrentLocation(location);
        gmf = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(),
                (float) ARData.getCurrentLocation().getLongitude(),
                (float) ARData.getCurrentLocation().getAltitude(),
                System.currentTimeMillis());

        double angleY = Math.toRadians(-gmf.getDeclination());

        synchronized (MAGNETIC_NORTH_COMPENSATION) {
            MAGNETIC_NORTH_COMPENSATION.toIdentity();

            MAGNETIC_NORTH_COMPENSATION.set((float) Math.cos(angleY),
                    0f,
                    (float) Math.sin(angleY),
                    0f,
                    1f,
                    0f,
                    (float) -Math.sin(angleY),
                    0f,
                    (float) Math.cos(angleY));

            MAGNETIC_NORTH_COMPENSATION.prod(X_AXIS_ROTATION);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor == null) throw new NullPointerException();

        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e(TAG, "Compass data unreliable");
        }
    }
}