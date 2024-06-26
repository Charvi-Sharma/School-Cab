package com.example.schoolcab;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import jxl.read.biff.BiffException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadStudents extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public static final String SHARED_PREFS = "shared_prefs";


    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_students);

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
        String ID = mAuth.getCurrentUser().getUid().toString();
        List<NewStudent> students = new ArrayList<>();
        try {
            // Open the Excel file using JExcelApi
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            Workbook workbook = Workbook.getWorkbook(inputStream);
            // Assuming data is in the first sheet
            Sheet sheet = workbook.getSheet(0);
            for (int row = 0; row < 3 ; row++) {

                Cell[] cells = sheet.getRow(row);
//               parsing the rows one by one
                String name = cells[0].getContents();
                String rollNo = cells[1].getContents();
                String guardian = cells[2].getContents();
                String phoneNo = cells[3].getContents();
                String email = cells[4].getContents();
                String address = cells[5].getContents();
                int standard = Integer.parseInt(cells[6].getContents());
                String section = cells[7].getContents();
                int age = Integer.parseInt(cells[8].getContents());
                int weight = Integer.parseInt(cells[9].getContents());
                String defaultAddress = cells[10].getContents();
                String sex = cells[11].getContents();

                NewStudent student = new NewStudent(); // Create a Student object
                student.setName(name);
                student.setRollNo(rollNo);
                student.setGuardian(guardian);
                student.setPhoneNo(phoneNo);
                student.setAddress(address);
                student.setDefaultAddress(defaultAddress);
                student.setStandard(standard);
                student.setSection(section);
                student.setSex(sex);
                student.setAge(age);
                student.setWeight(weight);
                student.setEmail(email);
                student.setSchoolId(ID);
//                student.setStopName(selectedStopName);

                students.add(student);

            }

            HashMap<NewStudent , String> mapper = new HashMap<>();
            createUser(0 ,students , mapper);
            workbook.close();
        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
    }



    void createUser(int index ,List<NewStudent> students , HashMap<NewStudent , String> mapper ){
        if(index == students.size()) {
            addStudentsToDb(mapper);
            return;
        }
         NewStudent student = students.get(index);
        mAuth.createUserWithEmailAndPassword(student.getEmail(), "1234567890")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User signup successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName("student")
                                    .build();
                            user.updateProfile(profileUpdates);

                              mapper.put(student , userId);
                            createUser(index+1 , students , mapper);
                            return;
                        } else {
                            // Handle signup failure
                            Toast.makeText(UploadStudents.this, "Signup failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void addStudentsToDb(HashMap<NewStudent , String> mapper ){


        for (Map.Entry<NewStudent, String> entry : mapper.entrySet()) {
            NewStudent studentx = entry.getKey();
            String uId = entry.getValue();


            DocumentReference userRef = db.collection("students").document(uId);

        //Saving Additional information of user in fireStore with same id
                                    userRef.set(studentx)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
                Log.d("Hello here" , "task Accomplished");
            } else {
                     Toast.makeText(UploadStudents.this, "Error saving user data to Firestore.", Toast.LENGTH_SHORT).show();
             }
        }
    });
        }

        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String id = sharedpreferences.getString("email", null);
        String password = sharedpreferences.getString("password", null);


        mAuth.signInWithEmailAndPassword(id, password);
        Intent intent = new Intent(UploadStudents.this, StudentAddUpdatePage.class);
        startActivity(intent);
//        finish();
        return;
    }
}


