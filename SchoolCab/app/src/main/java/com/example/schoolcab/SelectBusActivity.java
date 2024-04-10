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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SelectBusActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    SharedPreferences sharedPreferences;
    private List<Map<String,String>> busData = new ArrayList<>();
    private List<String> busNames = new ArrayList<>();

    private String jsonString;

    private String TAG = "SelectBusActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bus);

        busNames.add("Bus No 15");

        sharedPreferences=getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        String school=sharedPreferences.getString("sId",NULL);
        db = FirebaseFirestore.getInstance();

        getRoute("Ed8b6yjYDIQYgPxUCFFfHwgeEkw2");

//        db.collection("bus")
//                .whereEqualTo("schoolId", school)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "Task Successful");
//                            Log.d(TAG, String.valueOf(task.getResult().size()));
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d("Doc", String.valueOf(document));
//                                Map<String,String> bus = new HashMap<>();
//                                bus.put("BusId", document.getId());
//                                bus.put("BusName", document.getData().get("busNo").toString());
//                                Log.d("Bus", String.valueOf(bus));
//                                if(bus!=null){
//                                    busData.add(bus);
//                                }
//                                else {
//                                    Log.d(TAG,"bus is NULL");
//                                }
//                                Log.d(TAG, document.getId() + " => " + document.getData());
//                                busData.add(bus);
//                                busNames.add(bus.get(" BusName"));
//                            }
//
//                        } else {
//                            Log.d(TAG, "Error while fetching bus data: ", task.getException());
//                        }
//                    }
//                });
                        Spinner dynamicSpinner = (Spinner) findViewById(R.id.dynamic_spinner);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectBusActivity.this, android.R.layout.simple_spinner_item, busNames);
        dynamicSpinner.setAdapter(adapter);
        setupListViewListener();

        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(SelectBusActivity.this, ParentsMaps.class);
            intent.putExtra("data", jsonString);
            startActivity(intent);


        });

    }

    private void setupListViewListener() {
        Spinner dynamicSpinner = findViewById(R.id.dynamic_spinner);
        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("bus No", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void getRoute(String busId)
    {
        Log.d(TAG, "starting to get Route ");
        CollectionReference busCollection = db.collection("bus");
        busCollection.document(busId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // The document exists, you can access its data
                                Log.d(TAG, String.format(document.getId() + " onComplete: Bus Data " + document.getData().get("Route").getClass().getName()));

                                // If you want to convert the data to JSON
                                jsonString = new Gson().toJson(document.getData());

                            } else {
                                // The document does not exist
                                Log.d(TAG, "Document does not exist.");
                                Toast.makeText(SelectBusActivity.this, "Document not found", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d(TAG, "Error getting document: " + task.getException());
                        }
                    }
                });

    }
}