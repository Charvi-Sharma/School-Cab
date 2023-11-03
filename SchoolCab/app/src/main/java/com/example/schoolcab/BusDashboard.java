package com.example.schoolcab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import android.Manifest;

import java.util.HashMap;
import java.util.Map;

public class BusDashboard extends AppCompatActivity implements LocationListener {

    public static final String SHARED_PREFS = "shared_prefs";

    // key for schoolId
    public static final String sId = "sId";
    // variable for shared preferences.
    SharedPreferences sharedpreferences;


    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(BusDashboard.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_dashboard2);

        {// Check and request location permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                // Initialize location manager
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            }
        }

        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

//        Getting the school id saved in local preferences
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String schoolID = sharedpreferences.getString("sId", null);

        RelativeLayout attendance = findViewById(R.id.attendance_button);
        RelativeLayout busTracking = findViewById(R.id.bus_button);
        RelativeLayout logoutButton = findViewById(R.id.logout_button);
        RelativeLayout notification = findViewById(R.id.notification_button);

        attendance.setOnClickListener(v -> {
            Intent intent = new Intent(BusDashboard.this, AttendanceTypeActivity.class);
            startActivity(intent);

        });
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();

//            removing the schooldid from shared preferences on logout
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove(sId);
            editor.remove("busId");
            editor.apply();

            Intent intent = new Intent(BusDashboard.this, MainActivity.class);
            startActivity(intent);
        });

        busTracking.setOnClickListener(v -> {
            String busId = getIntent().getStringExtra("busid");
            String schoolId = getIntent().getStringExtra("schoolid");

            Intent intent = new Intent(BusDashboard.this, MapsActivity.class);
            startActivity(intent);
        });
        notification.setOnClickListener(v -> {
            Intent intent = new Intent(BusDashboard.this, NotificationSend.class);

            startActivity(intent);
        });


    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onLocationChanged(Location location) {




        Log.d("here i am ", "here");


            // Get the current location
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            Log.d("location", "onLocationChanged: " + latLng);


            CollectionReference busCollection = db.collection("bus");
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("location", latLng);
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String busId = sharedpreferences.getString("busId", null);


        busCollection.document(busId).update(updateData);


        }


    // Implement other LocationListener methods as needed

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Initialize location manager
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            }
        }
    }
}