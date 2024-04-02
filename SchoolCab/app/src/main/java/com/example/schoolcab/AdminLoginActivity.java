package com.example.schoolcab;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminLoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        db = FirebaseFirestore.getInstance();


        TextView forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(AdminLoginActivity.this, ForgotPassword.class);
            startActivity(intent);

        });

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            EditText idEditText = findViewById(R.id.edtEmail);
            EditText passwordEditText = findViewById(R.id.edtPassWord);

            String id = idEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            Log.d("AdminLogin", "Id: " + id + ", Password: " + password);

            db.collection("admins")
                    .whereEqualTo("id", id)
                    .whereEqualTo("password", password)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("Login","Task Success");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("doc", document.toString());
                                    // Redirect to AdminDashboardActivity

                                    Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                                    startActivity(intent);
                                    finish(); // Close the current activity
                                }
                            } else {
                                Log.d("AdminLoginActivity", "Error while login: ", task.getException());
                                Toast.makeText(AdminLoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }
}


