package com.jmlb0003.sampleargeo.Utilities;

import android.location.Location;

public class PhysicalLocationUtility {
    private static float[] x = new float[1];
    private static double y = 0.0d;
    private static float[] z = new float[1];
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double altitude = 0.0;

    public PhysicalLocationUtility() {
    }

    public PhysicalLocationUtility(PhysicalLocationUtility pl) {
        if (pl == null) throw new NullPointerException();

        set(pl.latitude, pl.longitude, pl.altitude);
    }

    public static synchronized void convLocationToVector(Location org, PhysicalLocationUtility gp, Vector v) {
        if (org == null || gp == null || v == null)
            throw new NullPointerException("Location, PhysicalLocationUtility, and Vector cannot be NULL.");

        Location.distanceBetween(org.getLatitude(), org.getLongitude(),
                gp.getLatitude(), org.getLongitude(),
                z);

        Location.distanceBetween(org.getLatitude(), org.getLongitude(),
                org.getLatitude(), gp.getLongitude(),
                x);
        y = gp.getAltitude() - org.getAltitude();
        if (org.getLatitude() < gp.getLatitude())
            z[0] *= -1;
        if (org.getLongitude() > gp.getLongitude())
            x[0] *= -1;

        v.set(x[0], (float) y, z[0]);
    }

    public void set(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public String toString() {
        return "(lat=" + latitude + ", lng=" + longitude + ", alt=" + altitude + ")";
    }
}