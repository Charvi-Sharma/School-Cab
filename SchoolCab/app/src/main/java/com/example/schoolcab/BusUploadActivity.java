package com.example.schoolcab;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class BusUploadActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    SharedPreferences sharedPreferences;
    String schoolId;



    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_upload);

        //        Getting the school id saved in local preferences
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String ID = mAuth.getCurrentUser().getUid().toString();

        RelativeLayout chooseFileButton = findViewById(R.id.uploadButton);

        // Initialize the ActivityResultLauncher
        filePickerLauncher = registerForActivityResult(

                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri fileUri = data.getData();
                            // Read the selected Excel file

                            readExcelFile(fileUri);
                            // Process and store the data as needed

                        }
                    }
                });

        chooseFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a file picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/vnd.ms-excel"); // for .xls files
                filePickerLauncher.launch(intent);
            }
        });
    }

    // Define the readExcelFile method here or in a separate class
    private void readExcelFile(Uri fileUri) {
         List<Bus> buses = new ArrayList<>();
        sharedPreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        schoolId = sharedPreferences.getString("sId", null);


        try {
            // Open the Excel file using JExcelApi
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            Workbook workbook = Workbook.getWorkbook(inputStream);
            // Assuming data is in the first sheet
            Sheet sheet = workbook.getSheet(0);
            for (int row = 0; row < 3 ; row++) {

                Cell[] cells = sheet.getRow(row);
//               parsing the rows one by one
                String number = cells[0].getContents();
                String id = cells[1].getContents();
                String capacity = cells[2].getContents();
                String userId = cells[3].getContents();


                Bus bus = new Bus();
                bus.setBusNo(Integer.parseInt(number));
                bus.setBusId(id);
                bus.setBusCapacity(Integer.parseInt(capacity));
                bus.setBusUserId(userId);
                bus.setPassword("1234567890");
                bus.setSchoolId(schoolId);

                buses.add(bus);

            }

            HashMap<Bus, String> mapper = new HashMap<>();
            createUser(0 ,buses , mapper);
            workbook.close();
        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
    }



    void createUser(int index ,List<Bus> buses , HashMap<Bus , String> mapper ){
        if(index == buses.size()) {
            addBusesToDb(mapper);
            return;
        }
        Bus bus = buses.get(index);
        mAuth.createUserWithEmailAndPassword(bus.getBusUserId(), "1234567890")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User signup successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName("bus")
                                    .build();
                            user.updateProfile(profileUpdates);

                            mapper.put(bus , userId);
                            createUser(index+1 , buses , mapper);
                            return;
                        } else {
                            // Handle signup failure
                            Toast.makeText(BusUploadActivity.this, "Signup failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void addBusesToDb(HashMap<Bus , String> mapper ){


        for (Map.Entry<Bus, String> entry : mapper.entrySet()) {
            Bus busx = entry.getKey();
            String uId = entry.getValue();


            DocumentReference userRef = db.collection("bus").document(uId);

            //Saving Additional information of user in fireStore with same id
            userRef.set(busx)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Hello here" , "task Accomplished");
                            } else {
                                Toast.makeText(BusUploadActivity.this, "Error saving user data to Firestore.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        SharedPreferences sharedpreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);
        String id = sharedpreferences.getString("email", null);
        String password = sharedpreferences.getString("password", null);


        mAuth.signInWithEmailAndPassword(id, password);
        Intent intent = new Intent(BusUploadActivity.this, BusAddRemoveUploadActivity.class);
        startActivity(intent);
//        finish();
        return;
    }
}




































