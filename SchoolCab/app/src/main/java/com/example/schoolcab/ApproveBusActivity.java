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
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class ApproveBusActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private List<Map<String,String>> schoolData = new ArrayList<>();
    private List<String> schoolNames = new ArrayList<>();

    private List<String> checkedSchools = new ArrayList<>();

    private Map<String,Long> idToBus = new HashMap<>();
    private String TAG = "ApproveBusActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_bus);

        db = FirebaseFirestore.getInstance();

        db.collection("schools")
                .orderBy("requestedBus")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Task Successful");
                            Log.d(TAG, String.valueOf(task.getResult().size()));
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Doc", String.valueOf(document));
                                Map<String,String> school = new HashMap<>();
                                school.put("SchoolId", document.getId());
                                school.put("SchoolName", (String) document.getData().get("name"));
                                school.put("BusCap",String.valueOf((Long) document.getData().get("requestedBus")));
                                schoolNames.add((String) document.getData().get("name"));
                                idToBus.put(document.getId(), (Long) document.getData().get("requestedBus"));
                                if(school!=null){
                                    schoolData.add(school);
                                }
                                else {
                                    Log.d(TAG,"school is NULL");
                                }
                                Log.d(TAG, document.getId() + " => " + document.getData());

                            }
                            ListView l = findViewById(R.id.list);
                            String[] from={"SchoolName","BusCap"};
                            int[] to={R.id.textView1,R.id.textView2};//int array of views id's
                            SimpleAdapter simpleAdapter=new SimpleAdapter(ApproveBusActivity.this,schoolData,R.layout.row_layout,from,to);
                            l.setAdapter(simpleAdapter);
                            setupListViewListener();
                        } else {
                            Log.d(TAG, "Error while fetching school data: ", task.getException());
                        }
                    }
                });


        Button btn = findViewById(R.id.button);
        Button deleteBtn = findViewById(R.id.delete_button);
        btn.setOnClickListener(v -> {
            Log.d("List: ", checkedSchools.toString());
            for(String sch : checkedSchools){

                DocumentReference doc = db.collection("schools").document(sch);
                doc.update("busCapacity", idToBus.get(sch));
                doc.update("requestedBus", FieldValue.delete());

            }
            Toast.makeText(ApproveBusActivity.this, "Approved Successfully", Toast.LENGTH_LONG).show();
            finish();
            startActivity(getIntent());
        });

        deleteBtn.setOnClickListener(v -> {
                    Log.d("List: ", checkedSchools.toString());
                    for(String sch : checkedSchools){
                        DocumentReference doc = db.collection("schools").document(sch);
                        doc.update("requestedBus", FieldValue.delete());
                    }
            Toast.makeText(ApproveBusActivity.this, "Rejected Successfully", Toast.LENGTH_LONG).show();
            finish();
            startActivity(getIntent());
        });

    }

    private void setupListViewListener() {
        ListView l = findViewById(R.id.list);
        l.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                        String scId = schoolData.get(pos).get("SchoolId");
                        if(checkedSchools.contains(scId)){
                            checkedSchools.remove(scId);
                            view.setBackgroundColor(Color.WHITE);
                        }
                        else{
                            checkedSchools.add(scId);
                            view.setBackgroundColor(Color.GREEN);
                        }
                    }

                });
    }
}

