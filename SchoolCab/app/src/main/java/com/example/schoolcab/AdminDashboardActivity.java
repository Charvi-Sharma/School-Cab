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

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    SharedPreferences sharedPreferences;
    private List<Map<String,String>> schoolData = new ArrayList<>();
    private List<String> schoolNames = new ArrayList<>();

    private List<String> checkedSchools = new ArrayList<>();

    private String TAG = "AdminDashboardActivity";

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        db.collection("schools")
                .whereEqualTo("verifiedStatus", false)
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
                                school.put("BusCap",String.valueOf((Long) document.getData().get("busCapacity")));
                                schoolNames.add((String) document.getData().get("name"));
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
                            SimpleAdapter simpleAdapter=new SimpleAdapter(AdminDashboardActivity.this,schoolData,R.layout.row_layout,from,to);
//                            ArrayAdapter<String> arr;
//                            arr
//                                    = new ArrayAdapter<String>(
//                                    AdminDashboardActivity.this,
//                                    android.R.layout.simple_list_item_1,
//                                    schoolNames);
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
                doc.update("verifiedStatus", true);

            }
            Toast.makeText(AdminDashboardActivity.this, "Verified Succesfully", Toast.LENGTH_LONG).show();
            finish();
            startActivity(getIntent());
        });

        deleteBtn.setOnClickListener(v -> {
                    Log.d("List: ", checkedSchools.toString());
                    deleteUser(0);
                    Toast.makeText(AdminDashboardActivity.this, "Deleted Succesfully", Toast.LENGTH_LONG).show();
                }

        );

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

    void deleteUser(int index ){
        if(index == checkedSchools.size()) {
            return;
        }
        String sch = checkedSchools.get(index);
        DocumentReference doc = db.collection("schools").document(sch);
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Map<String,Object> m = task.getResult().getData();
                    mAuth.signInWithEmailAndPassword(m.get("email").toString(), m.get("password").toString()).
                            addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        AuthCredential credential = EmailAuthProvider
                                                .getCredential(m.get("email").toString(), m.get("password").toString());
                                        Log.d("AdminDashboard : User", String.valueOf(user));
                                        if (user != null) {
                                            user.reauthenticate(credential).addOnCompleteListener(task2 -> user.delete().addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    Log.d("Tag", "User account deleted.");
                                                    doc.delete();
                                                    deleteUser(index+1);
                                                }
                                                else {
                                                    Log.d("Tag", "User deletion failed.");
                                                }
                                            }));
                                        }
                                    }
                                    else{
                                        Log.d("Tag", "Authentication failed.");
                                    }
                                }
                            });
                }
            }
        });
    }
}
