package com.example.schoolcab;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AssignBusActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private AutoCompleteTextView busNoAutoComplete;
    private List<String> busNumbers;

    public static final String SHARED_PREFS = "shared_prefs";
    public static final String sId = "sId";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_bus);

        db = FirebaseFirestore.getInstance();
        busNoAutoComplete = findViewById(R.id.autoCompleteBusNo);

        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String schoolID = sharedpreferences.getString("sId", null);

        populateBusNumbers(schoolID);

        findViewById(R.id.search_button).setOnClickListener(v -> {
            String selectedBusNo = busNoAutoComplete.getText().toString().trim();
            if (!selectedBusNo.isEmpty()) {
                searchBus(selectedBusNo, schoolID);
            } else {
                Toast.makeText(this, "Please select a bus number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateBusNumbers(String schoolID) {
        db.collection("bus")
                .whereEqualTo("schoolId", schoolID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        busNumbers = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Long busNo = document.getLong("busNo");
                            if (busNo != null) {
                                String busNoString = String.valueOf(busNo);
                                if (!busNumbers.contains(busNoString)) {
                                    busNumbers.add(busNoString);
                                }
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, busNumbers);
                        busNoAutoComplete.setAdapter(adapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void searchBus(String selectedBusNo, String schoolID) {
        int busNoInt;
        try {
            busNoInt = Integer.parseInt(selectedBusNo);
        } catch (NumberFormatException e) {
            // Handle invalid input gracefully
            Toast.makeText(this, "Invalid bus number", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("bus")
                .whereEqualTo("busNo", busNoInt)
                .whereEqualTo("schoolId", schoolID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(AssignBusActivity.this, "BUS NOT FOUND WITH GIVEN BUS NO.", Toast.LENGTH_LONG).show();
                        } else {
                            for (DocumentSnapshot document : task.getResult()) {
//                                String jsonString = document.getData().toString();
                                String jsonString = new Gson().toJson(document.getData());
                                Intent intent = new Intent(AssignBusActivity.this, UpdateRouteActivity.class);
                                intent.putExtra("data", jsonString);
                                intent.putExtra("id", document.getId());
                                startActivity(intent);
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

}
