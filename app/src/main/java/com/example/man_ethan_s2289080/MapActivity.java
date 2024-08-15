//
// Name                 Ethan Man
// Student ID           S2289080
// Programme of Study   BSc Computing
//

package com.example.man_ethan_s2289080;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private String circuitName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get data from the intent
        latitude = getIntent().getDoubleExtra("latitude", 0);
        longitude = getIntent().getDoubleExtra("longitude", 0);
        circuitName = getIntent().getStringExtra("circuitName");

        Log.d("MapActivity", "Received Latitude: " + latitude + " Longitude: " + longitude);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker at the circuit location and move the camera
        LatLng circuitLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(circuitLocation).title(circuitName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(circuitLocation, 15));
    }
}
