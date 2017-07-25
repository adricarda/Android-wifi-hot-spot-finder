package com.example.adriano.hotspotfinder;


import android.app.Fragment;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class listWifi extends Fragment  {

    MapView mMapView;
    private GoogleMap mMap;
    private DBClass db;
    private List<Marker> open = new ArrayList<>();
    private List<Marker> encrypted = new ArrayList<>();
    public listWifi() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_wifi, container, false);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        db = MainActivity.getDB();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.clear();
                Cursor cursor = db.getData();
                cursor.moveToFirst();
                LatLng item = new LatLng(0,0);
                while (!cursor.isAfterLast()) {
                    String ssid;
                    ssid = cursor.getString(cursor.getColumnIndex(DBClass.COLUMN_SSID));
                    String cap = cursor.getString(cursor.getColumnIndex(DBClass.COLUMN_CAP));
                    int level = cursor.getInt(cursor.getColumnIndex(DBClass.COLUMN_LEVEL));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(DBClass.COLUMN_LONGITUDE));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(DBClass.COLUMN_LATITUDE));

                    item = new LatLng(latitude, longitude);
                    Marker marker;
                    if (cap == "open"){
                        marker = mMap.addMarker(new MarkerOptions().position(item).title(ssid).snippet("strength: " +level+"/5").visible(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        open.add(marker);
                    }else {
                        marker = mMap.addMarker(new MarkerOptions().position(item).title(ssid).snippet("strength: " +level+"/5").visible(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        encrypted.add(marker);
                    }
                    cursor.moveToNext();
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item, 18));

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                String prova = sharedPrefs.getString("networksType", "NULL");
                switch(prova) {
                    case "1":
                        setVisibility(true,true);
                        break;
                    case "2":
                        setVisibility(true,false);
                        break;
                    case "3":
                        setVisibility(false,true);
                        break;
                }

                cursor.close();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String prova = sharedPrefs.getString("networksType", "NULL");
        switch(prova) {
            case "1":
                setVisibility(true,true);
                break;
            case "2":
                setVisibility(true,false);
                break;
            case "3":
                setVisibility(false,true);
                break;
        }
    }


    private void setVisibility(Boolean visibilityForOpen, Boolean visibilityForEncrypted){
        for(Marker item : open){
            item.setVisible(visibilityForOpen);
        }
        for(Marker item : encrypted){
            item.setVisible(visibilityForEncrypted);
        }
    }

}
