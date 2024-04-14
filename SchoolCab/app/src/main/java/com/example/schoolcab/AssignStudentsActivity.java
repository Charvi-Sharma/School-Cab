

package com.example.schoolcab;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AssignStudentsActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteRollNo, autoCompleteBusNo;
    private TextView textViewStopName;
    private Button updateButton;

    private FirebaseFirestore db;
    public static final String SHARED_PREFS = "shared_prefs";

    // key for schoolId
    public static final String sId = "sId";
    // variable for shared preferences.
    SharedPreferences sharedpreferences;

    private List<String> studentRollNos, busNos;
    private String selectedStudentRollNo, selectedStopName;
    String schoolID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_students);

        autoCompleteRollNo = findViewById(R.id.autoCompleteRollNo);
        autoCompleteBusNo = findViewById(R.id.autoCompleteBusNo);
        textViewStopName = findViewById(R.id.textViewStopName);
        updateButton = findViewById(R.id.updateButton);

        db = FirebaseFirestore.getInstance();
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        schoolID = sharedpreferences.getString("sId", null);

        // Initialize lists
        studentRollNos = new ArrayList<>();
        busNos = new ArrayList<>();

        // Load student roll numbers
        loadStudentRollNumbers(schoolID);

        autoCompleteRollNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedStudentRollNo = parent.getItemAtPosition(position).toString();
                loadStopNameForStudent(selectedStudentRollNo);
            }
        });

        autoCompleteBusNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // No need to implement anything here for now
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStudentBus();
            }
        });
    }

    private void loadStudentRollNumbers(String schoolId) {

        db.collection("students")
                .whereEqualTo("schoolId", schoolId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            studentRollNos.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String rollNo = document.getString("rollNo");
                                if (rollNo != null) {
                                    studentRollNos.add(rollNo);
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AssignStudentsActivity.this, android.R.layout.simple_dropdown_item_1line, studentRollNos);
                            autoCompleteRollNo.setAdapter(adapter);
                        } else {
                            Toast.makeText(AssignStudentsActivity.this, "Error loading student roll numbers", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadStopNameForStudent(String rollNo) {
        db.collection("students")
                .whereEqualTo("rollNo", rollNo)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                selectedStopName = document.getString("stopName");
                                if (selectedStopName != null) {
                                    textViewStopName.setText("Stop Name: " + selectedStopName);
                                    textViewStopName.setVisibility(View.VISIBLE);
                                    loadBusNumbersForStop(selectedStopName);
                                } else {
                                    textViewStopName.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            Toast.makeText(AssignStudentsActivity.this, "Error loading stop name for student", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadBusNumbersForStop(String stopName) {
        db.collection("bus")
                .whereEqualTo("schoolId", schoolID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            busNos.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String jsonString = new Gson().toJson(document.getData());
                                Log.d("chuchu", "onComplete: " + jsonString);
                                Gson gson = new Gson();
                                Type type = new TypeToken<Map<String, Object>>() {}.getType();
                                Map<String, Object> jsonMap = gson.fromJson(jsonString, type);
                                Map<String, Object> routeMap = (Map<String, Object>) jsonMap.get("Route");
                                if(routeMap != null) {
                                    for (Map.Entry<String, Object> entry : routeMap.entrySet()) {
                                        String locationName = entry.getKey();
                                        if (Objects.equals(locationName, stopName)) {
//                                    Map<String, Object> stopMap = routeMap.get(stopName);
                                            Log.d("chuchu", "haaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                                            Long busNo = document.getLong("busNo");

                                            if (busNo != null) {
                                                busNos.add(String.valueOf(busNo));}
                                        }
                                    }
                                }
                                }
                                if (!busNos.isEmpty()) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AssignStudentsActivity.this, android.R.layout.simple_dropdown_item_1line, busNos);
                                    autoCompleteBusNo.setAdapter(adapter);
                                    autoCompleteBusNo.setEnabled(true);
                                    updateButton.setEnabled(true);
                                } else {
                                    Toast.makeText(AssignStudentsActivity.this, "No bus numbers found for stop", Toast.LENGTH_SHORT).show();
                                }
//                                Map<String, Map<String, Object>> routeMap = (Map<String, Map<String, Object>>) document.get("Route");

                            }

                        else {
                            Toast.makeText(AssignStudentsActivity.this, "Error loading bus numbers for stop", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void updateStudentBus() {
        // Get the selected bus number
        String selectedBusNo = autoCompleteBusNo.getText().toString();
        String selectedRollNo = autoCompleteRollNo.getText().toString();
        db.collection("bus")
                .whereEqualTo("busNo", Integer.parseInt(selectedBusNo))
                .whereEqualTo("schoolId", schoolID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String busId = document.getId();
                                Toast.makeText(AssignStudentsActivity.this, "Student's bus fetched", Toast.LENGTH_SHORT).show();
                                db.collection("students")
                                        .whereEqualTo("rollNo", selectedRollNo)
                                        .whereEqualTo("schoolId", schoolID)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot studentDocument : task.getResult()) {
                                                        // Update the student document with the retrieved busId
                                                        studentDocument.getReference().update("busId", busId)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        // Student's bus updated successfully
                                                                        Toast.makeText(AssignStudentsActivity.this, "Student's bus updated successfully", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Handle any errors while updating the student's bus
                                                                        Toast.makeText(AssignStudentsActivity.this, "Error updating bus for student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                } else {
                                                    // Handle errors while fetching the student document
                                                    Toast.makeText(AssignStudentsActivity.this, "Error updating bus for student", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }


                        else {
                            Toast.makeText(AssignStudentsActivity.this, "Error updating bus for student", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
