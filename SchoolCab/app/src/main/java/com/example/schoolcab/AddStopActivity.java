package com.example.schoolcab;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class AddStopActivity extends AppCompatActivity {

    private EditText stopNameEditText, latitudeEditText, longitudeEditText;
    private Button saveButton;
    private FirebaseFirestore db;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_stop_activity);

        stopNameEditText = findViewById(R.id.stop_name);
        latitudeEditText = findViewById(R.id.latitude);
        longitudeEditText = findViewById(R.id.longitude);
        saveButton = findViewById(R.id.save_button);

        db = FirebaseFirestore.getInstance();

        sharedpreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStop();
            }
        });
    }

    private void addStop() {
        String stopName = stopNameEditText.getText().toString().trim();
        String latitudeStr = latitudeEditText.getText().toString().trim();
        String longitudeStr = longitudeEditText.getText().toString().trim();

        if (stopName.isEmpty() || latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = Double.parseDouble(latitudeStr);
        double longitude = Double.parseDouble(longitudeStr);

        // Create a GeoPoint
        GeoPoint location = new GeoPoint(latitude, longitude);

        // Retrieve school ID from SharedPreferences
        String schoolID = sharedpreferences.getString("sId", null);

        // Generate unique bus ID
        String stopID = generateStopID();

        // Create a new stop object
        Map<String, Object> stop = new HashMap<>();
        stop.put("stopName", stopName);
        stop.put("location", location); // GeoPoint
        stop.put("stopID", stopID);
        stop.put("schoolID", schoolID);
        // Add more fields if needed

        // Get a reference to the stops collection
        CollectionReference stopsCollection = db.collection("stops");

        // Add the stop to Firestore
        stopsCollection.add(stop)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddStopActivity.this, "Stop added successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after adding the stop
                        } else {
                            Toast.makeText(AddStopActivity.this, "Failed to add stop", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Method to generate a unique stop ID
    private String generateStopID() {
        // Implement your logic to generate a unique ID
        // Here, you can use a combination of timestamp and random numbers, for example
        return "STOP_" + System.currentTimeMillis();
    }
}
