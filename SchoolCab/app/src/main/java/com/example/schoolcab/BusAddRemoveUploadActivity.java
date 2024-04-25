package com.example.schoolcab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class BusAddRemoveUploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_add_remove_upload);

        RelativeLayout addBus = findViewById(R.id.addBus);
        RelativeLayout deleteBus = findViewById(R.id.deleteBus);
        RelativeLayout updateBus = findViewById(R.id.updateBus);
        RelativeLayout uploadBus = findViewById(R.id.uploadBus);

        addBus.setOnClickListener(v -> {
            Intent intent = new Intent(BusAddRemoveUploadActivity.this, AddBusActivity.class);
            startActivity(intent);
        });

        updateBus.setOnClickListener(v -> {

            Toast.makeText(BusAddRemoveUploadActivity.this, "Need To be Implemented.",
                    Toast.LENGTH_SHORT).show();
        });

        deleteBus.setOnClickListener(v -> {
            Toast.makeText(BusAddRemoveUploadActivity.this, "Need To be Implemented.",
                    Toast.LENGTH_SHORT).show();
        });


        uploadBus.setOnClickListener(v -> {
            Intent intent = new Intent(BusAddRemoveUploadActivity.this, BusUploadActivity.class);
            startActivity(intent);
        });


    }
}