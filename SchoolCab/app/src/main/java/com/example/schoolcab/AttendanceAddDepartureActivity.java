package com.example.schoolcab;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceAddDepartureActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    SharedPreferences sharedPreferences;
    private List<Map<String,String>> studentData = new ArrayList<>();
    private List<String> studentNames = new ArrayList<>();

    private List<String> checkedStudents = new ArrayList<>();

    private String TAG = "AttendanceAddActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_add);

        sharedPreferences=getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        String school=sharedPreferences.getString("sId",NULL);
        String busId = sharedPreferences.getString("busId",NULL);
        db = FirebaseFirestore.getInstance();

        db.collection("students")
                .whereEqualTo("schoolId", school)
                .whereEqualTo("busId",busId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Task Successful");
                            Log.d(TAG, String.valueOf(task.getResult().size()));
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Doc", String.valueOf(document));
                                Map<String,String> stud = new HashMap<>();
                                stud.put("StudentId", document.getId());
                                stud.put("StudentName", (String) document.getData().get("name"));
                                studentNames.add((String) document.getData().get("name"));
                                Log.d("Student", String.valueOf(stud));
                                if(stud!=null){
                                    studentData.add(stud);
                                }
                                else {
                                    Log.d(TAG,"stud is NULL");
                                }
                                Log.d(TAG, document.getId() + " => " + document.getData());

                            }
                            ListView l = findViewById(R.id.list);
                            ArrayAdapter<String> arr;
                            arr
                                    = new ArrayAdapter<String>(
                                    AttendanceAddDepartureActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    studentNames);
                            l.setAdapter(arr);
                            setupListViewListener();
                        } else {
                            Log.d(TAG, "Error while fetching student data: ", task.getException());
                        }
                    }
                });


        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(v -> {
            Log.d("List: ", checkedStudents.toString());
            for(String stud : checkedStudents){
//                Attendance record = new Attendance(stud,id,"bus-1",formattedDate);
//                db.collection("attendance").add(record)
//                        .addOnSuccessListener(documentReference -> {
//                            Log.d(TAG, "Added Successfully");
//                        })
//                        .addOnFailureListener(e -> {
//                            Log.e(TAG, "Error adding attendance", e);
//                        });

                DocumentReference doc = db.collection("students").document(stud);
                doc.update("departureAttendance", FieldValue.arrayUnion(Timestamp.now()));

            }
            Toast.makeText(AttendanceAddDepartureActivity.this, "Attendance Marked Succesfully", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AttendanceAddDepartureActivity.this, BusDashboard.class);
            startActivity(intent);


        });

    }

    private void setupListViewListener() {
        ListView l = findViewById(R.id.list);
        l.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                        String studId = studentData.get(pos).get("StudentId");
                        if(checkedStudents.contains(studId)){
                            checkedStudents.remove(studId);
                            view.setBackgroundColor(Color.WHITE);
                        }
                        else{
                            checkedStudents.add(studId);
                            view.setBackgroundColor(Color.GREEN);
                        }
                    }

                });
    }
}