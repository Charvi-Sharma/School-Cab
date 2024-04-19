package com.example.schoolcab;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RequestBusActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    public static final String SHARED_PREFS = "shared_prefs";

    // key for schoolId
    public static final String sId = "sId";
    // variable for shared preferences.
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_bus);

        //        Getting the school id saved in local preferences
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String schoolID = sharedpreferences.getString("sId", null);
        db = FirebaseFirestore.getInstance();

        EditText edtBus = findViewById(R.id.edtTitle);
        Button btnSend = findViewById(R.id.edtSend);

        btnSend.setOnClickListener(v -> {
            DocumentReference doc = db.collection("schools").document(schoolID);
            doc.update("requestedBus", Integer.parseInt(edtBus.getText().toString()));
            Toast.makeText(RequestBusActivity.this, "Requested Successfully", Toast.LENGTH_LONG).show();
        });


    }

}