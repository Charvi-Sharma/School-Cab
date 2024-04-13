package com.example.schoolcab;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.MotionEffect;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateRouteActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private AutoCompleteTextView stopNameAutoComplete;
    private List<String> stopNames;

    public static final String SHARED_PREFS = "shared_prefs";
    public static final String sId = "sId";
    Map<String, Object> jsonMap;
    Map<String, Object> routeMap;
    Map<String, Object> finalRouteMap;
    SharedPreferences sharedpreferences;
    String id;
    String jsonString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_route);

        db = FirebaseFirestore.getInstance();
        stopNameAutoComplete = findViewById(R.id.stopNameAutoComplete);

        jsonString = getIntent().getStringExtra("data");
        id = getIntent().getStringExtra("id");
        // Log the JSON string
        Log.d(MotionEffect.TAG, "Received JSON data: " + jsonString);

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        jsonMap = gson.fromJson(jsonString, type);
        routeMap = (Map<String, Object>) jsonMap.get("Route");
        // Check if "Route" field is null
        if (routeMap == null) {
            // If "Route" field is null, create an empty Map for it
            routeMap = new HashMap<>();
        }
        finalRouteMap = routeMap;

        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String schoolID = sharedpreferences.getString("sId", null);
        populateStopNames(schoolID);

        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            String selectedStopName = stopNameAutoComplete.getText().toString().trim();
            if (!selectedStopName.isEmpty()) {
                searchStop(selectedStopName, schoolID);
            } else {
                Toast.makeText(this, "Please select a stop name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateStopNames(String schoolID) {
        db.collection("stops")
                .whereEqualTo("schoolID", schoolID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        stopNames = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String stopName = document.getString("stopName");
                            if (stopName != null) {
                                stopNames.add(stopName);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, stopNames);
                        stopNameAutoComplete.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void searchStop(String selectedStopName, String schoolID) {
        db.collection("stops")
                .whereEqualTo("stopName", selectedStopName)
                .whereEqualTo("schoolID", schoolID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(UpdateRouteActivity.this, "BUS STOP NOT FOUND", Toast.LENGTH_LONG).show();
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
                                    finalRouteMap.put(selectedStopName, locationMap);

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
                });
    }
}
