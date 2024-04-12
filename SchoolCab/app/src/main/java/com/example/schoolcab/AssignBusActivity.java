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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignBusActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    public static final String SHARED_PREFS = "shared_prefs";

    // key for schoolId
    public static final String sId = "sId";
    // variable for shared preferences.
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_bus);

        //        Getting the school id saved in local preferences
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String schoolID = sharedpreferences.getString("sId", null);


        db = FirebaseFirestore.getInstance();

        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            EditText editBusNo = findViewById(R.id.busNo);

            int busNo = Integer.parseInt(editBusNo.getText().toString());

            Log.d("Bus", "Name: " + busNo + schoolID);


            db.collection("bus")
                    .whereEqualTo("busNo", busNo)
                    .whereEqualTo("schoolId",schoolID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) {
                                    Log.d(TAG, "Bus Not Found with Given Bus No. ");

                                    Toast.makeText(AssignBusActivity.this, "BUS NOT FOUND WITH GIVEN BUS NO.", Toast.LENGTH_LONG).show();
//                                    TextView textMessage = findViewById(R.id.textMessage);
//                                    textMessage.setText("No Student Found with Given Enrollment No. Please Enter Again");


                                } else {
                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                        Log.d(TAG, document.getId() + " => " + document.getData());

                                        String jsonString = new Gson().toJson(document.getData());
//                                        Toast.makeText(AssignBusActivity.this, "BUS FOUND", Toast.LENGTH_LONG).show();

                                        Intent intent = new Intent(AssignBusActivity.this, UpdateRouteActivity.class);
                                        intent.putExtra("data", jsonString);
                                        intent.putExtra("id" ,document.getId());
                                        startActivity(intent);

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