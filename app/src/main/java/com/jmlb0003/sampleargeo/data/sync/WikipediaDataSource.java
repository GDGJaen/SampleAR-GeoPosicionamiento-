package com.jmlb0003.sampleargeo.data.sync;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.jmlb0003.sampleargeo.IconMarker;
import com.jmlb0003.sampleargeo.Marker;
import com.jmlb0003.sampleargeo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WikipediaDataSource extends NetworkDataSource {
    private static final String BASE_URL = "http://api.geonames.org/findNearbyWikipediaJSON";

    private static Bitmap icon = null;

    public WikipediaDataSource(Resources res) {
        if (res == null) throw new NullPointerException();

        createIcon(res);
    }

    protected void createIcon(Resources res) {
        if (res == null) throw new NullPointerException();

        icon = BitmapFactory.decodeResource(res, R.drawable.wikipedia);
    }

    @Override
    public String createRequestURL(double lat, double lon, double alt, float radius, String locale) {
        //http://api.geonames.org/findNearbyWikipediaJSON?lat=37.6759861&lng=-3.5661972&radius=15&maxRows=500&lang=es&username=jmlb0003
        return BASE_URL +
                "?lat=" + lat +
                "&lng=" + lon +
                //La opción gratuita de esta API no permite consultas con radius mayor que 20
                "&radius=" + radius +
                "&maxRows=500" +
                "&lang=" + locale +
                "&username=" + "jmlb0003";
    }

    @Override
    public List<Marker> parse(JSONObject root) {
        if (root == null) return null;

        JSONObject jo;
        JSONArray dataArray = null;
        List<Marker> markers = new ArrayList<>();

        try {
            if (root.has("geonames")) dataArray = root.getJSONArray("geonames");
            if (dataArray == null) return markers;
            int top = Math.min(MAX, dataArray.length());
            for (int i = 0; i < top; i++) {
                jo = dataArray.getJSONObject(i);
                Marker ma = processJSONObject(jo);
                if (ma != null) markers.add(ma);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return markers;
    }

    private Marker processJSONObject(JSONObject jo) {
        if (jo == null) return null;

        Marker ma = null;
        if (jo.has("title") &&
                jo.has("lat") &&
                jo.has("lng") &&
                jo.has("elevation")
                ) {
            try {
                ma = new IconMarker(
                        jo.getString("title"),
                        jo.getDouble("lat"),
                        jo.getDouble("lng"),
                        jo.getDouble("elevation"),
                        Color.WHITE,
                        icon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ma;
    }
}