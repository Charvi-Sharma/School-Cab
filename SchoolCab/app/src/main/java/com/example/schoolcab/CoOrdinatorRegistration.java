package com.example.schoolcab;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CoOrdinatorRegistration extends AppCompatActivity {

    private FirebaseFirestore db;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_co_ordinator_registration);

        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedpreferences=getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        String school_id=sharedpreferences.getString("sId",NULL);

         String id = sharedpreferences.getString("email", null);
        String pass = sharedpreferences.getString("password", null);


        mAuth = FirebaseAuth.getInstance();
        String ID = mAuth.getCurrentUser().getUid().toString();

        Button registerButton = findViewById(R.id.co_register);
        registerButton.setOnClickListener(v -> {
            // Collect coordinator details from EditText fields
            EditText nameEditText = findViewById(R.id.EditTextName);
            EditText lastNameEditText = findViewById(R.id.EditTextLastName);
            EditText coordinator=findViewById(R.id.EditTextSchoolId);
            EditText phoneNoEditText=findViewById(R.id.editTextPhoneNo);
            EditText passwordEditText=findViewById(R.id.password);

            String name = nameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();
            String phoneNo=phoneNoEditText.getText().toString();
            String coordinator_id=coordinator.getText().toString();
            String password=passwordEditText.getText().toString();


            CoOrdinator c = new CoOrdinator();
            c.setName(name);
            c.setLastName(lastName);
            c.setSchoolId(school_id);
            c.setphoneNo(phoneNo);
            c.setCoordinatorId(coordinator_id);
            c.setPassword(password);




            //            Creating authentication for user in firebase
            mAuth.createUserWithEmailAndPassword(coordinator_id,g password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // User signup successful
                                FirebaseUser user = mAuth.getCurrentUser();
                                String userId = user.getUid();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName("coordinator")
                                        .build();

                                user.updateProfile(profileUpdates);

                                // Save additional user information to Firestore
                                DocumentReference userRef = db.collection("coordinators").document(userId);

//                                Saving Additional information of user in fireStore with same id
                                userRef.set(c)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // User information saved to Firestore successfully
                                                    Toast.makeText(CoOrdinatorRegistration.this, "Coordinator registered successfully!", Toast.LENGTH_SHORT).show();
                                                    mAuth.signOut();

                                                    mAuth.signInWithEmailAndPassword(id, pass);
                                                    Intent intent = new Intent(CoOrdinatorRegistration.this, CoordinatorDashboardActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    // Handle Firestore document creation failure
                                                    Toast.makeText(CoOrdinatorRegistration.this, "Error saving user data to Firestore.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                            } else {
                                // Handle signup failure
                                Toast.makeText(CoOrdinatorRegistration.this, "Signup failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });



            // Add student to Firestore
//            CollectionReference coordinatorCollection = db.collection("coordinators");
//
//            coordinatorCollection.add(c)
//                    .addOnSuccessListener(documentReference -> {
//                        Toast.makeText(this, "Coordinator registered successfully!", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e("coordinatorRegistration", "Error registering co-ordinator", e);
//                        Toast.makeText(this, "Error registering co-ordinator", Toast.LENGTH_SHORT).show();
//                    });
        });
    }
}