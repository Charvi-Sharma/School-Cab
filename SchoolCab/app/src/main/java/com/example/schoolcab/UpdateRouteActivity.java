package com.example.schoolcab;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.MotionEffect;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateRouteActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    public static final String SHARED_PREFS = "shared_prefs";

    // key for schoolId
    public static final String sId = "sId";
    // variable for shared preferences.
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_route);

        //        Getting the school id saved in local preferences
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String schoolID = sharedpreferences.getString("sId", null);

        String jsonString = getIntent().getStringExtra("data");
        String id = getIntent().getStringExtra("id");
        // Log the JSON string
        Log.d(MotionEffect.TAG, "Received JSON data: " + jsonString);

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> jsonMap = gson.fromJson(jsonString, type);
        Map<String, Object> routeMap = (Map<String, Object>) jsonMap.get("Route");
        // Check if "Route" field is null
        if (routeMap == null) {
            // If "Route" field is null, create an empty Map for it
            routeMap = new HashMap<>();
        }


        db = FirebaseFirestore.getInstance();

        Button searchButton = findViewById(R.id.search_button);
        Map<String, Object> finalRouteMap = routeMap;
        searchButton.setOnClickListener(v -> {
            EditText editStopName = findViewById(R.id.stopName);

            String stopName = editStopName.getText().toString();

            Log.d("Stop", "Name: " + stopName + schoolID);


            db.collection("stops")
                    .whereEqualTo("stopName", stopName)
                    .whereEqualTo("schoolID",schoolID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) {
                                    Log.d(TAG, "Stop Not Found ");

                                    Toast.makeText(UpdateRouteActivity.this, "BUS STOP NOT FOUND", Toast.LENGTH_LONG).show();
//                                    TextView textMessage = findViewById(R.id.textMessage);
//                                    textMessage.setText("No Student Found with Given Enrollment No. Please Enter Again");


                                } else {
                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        GeoPoint geoPoint = document.getGeoPoint("location");
                                        Double latitude = geoPoint.getLatitude();
                                        Double longitude = geoPoint.getLongitude();

                                        if (latitude != null && longitude != null) {
                                            // Create a Map to represent location
                                            Map<String, Object> locationMap = new HashMap<>();
                                            locationMap.put("latitude", latitude);
                                            locationMap.put("longitude", longitude);

                                            // Add the stop to the "Route" map
                                            finalRouteMap.put(stopName, locationMap);

                                            // Update the "Route" field in the jsonMap
                                            jsonMap.put("Route", finalRouteMap);

                                            // Update the Bus document in Firestore
                                            db.collection("bus").document(id)
                                                    .set(jsonMap, SetOptions.merge())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "Route updated successfully");
                                                            Toast.makeText(UpdateRouteActivity.this, "Route updated successfully", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG, "Error updating route", e);
                                                            Toast.makeText(UpdateRouteActivity.this, "Error updating route", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Log.e(TAG, "Latitude or longitude is null");
                                        }
                                    }
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });


        });

    }
}