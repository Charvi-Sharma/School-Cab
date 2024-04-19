package com.example.schoolcab;

import android.content.Intent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class AdminHomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        RelativeLayout verifySchool = findViewById(R.id.verifySchool);
        RelativeLayout approveBus = findViewById(R.id.approveBus);
        RelativeLayout logoutButton = findViewById(R.id.logout_button);

        verifySchool.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
        });

        approveBus.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, ApproveBusActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {

        });

    }
}



