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
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String id = sharedpreferences.getString("email", null);
        String password = sharedpreferences.getString("password", null);




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

                            List<NewStudent> student = readExcelFile(fileUri);
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
    private List<NewStudent> readExcelFile(Uri fileUri) {
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String id = sharedpreferences.getString("email", null);
        String password = sharedpreferences.getString("password", null);
        String ID = mAuth.getCurrentUser().getUid().toString();


        List<NewStudent> students = new ArrayList<>();

        try {
            Log.d("here" , String.valueOf(fileUri));


            // Open the Excel file using JExcelApi
            InputStream inputStream = getContentResolver().openInputStream(fileUri);

            Workbook workbook = Workbook.getWorkbook(inputStream);

            Log.d("here" , "here8");

            // Assuming data is in the first sheet
            Sheet sheet = workbook.getSheet(0);

            Log.d("here" , "here3");

            for (int row = 2; row >=0 ; row--) {
                Cell[] cells = sheet.getRow(row);
                Log.d("here" , "here4");

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

                Log.d("StudentRegistration", row+"        Name: " + name + ", Password: " + rollNo);


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

//                Log.d("hello " , student);
                System.out.println(student.getName()+" " + student.getAddress()+"   Hello");

                students.add(student);



            }

            HashMap<NewStudent , String> mapper = new HashMap<>();
            final int[] completedTasksCount = {0};
            int totalTasks = students.size();

            for (int i=0 ; i<students.size() ; i++){

                NewStudent student = students.get(i);
                mAuth.createUserWithEmailAndPassword(student.getEmail(), "1234567890")
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // User signup successful
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String userId = user.getUid();
//                                    see this line in log uid is getting printed in same for every loop
                                    Log.d("userId" , userId);



                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName("student")
                                            .build();

                                    Log.d("userId --" , String.valueOf(profileUpdates));

                                    user.updateProfile(profileUpdates);

                                    // Save additional user information to Firestore
//                                    DocumentReference userRef = db.collection("students").document(userId);

//                                    Log.d("userId ---" , String.valueOf(userRef));

                                    mapper.put(student , userId);

                                    completedTasksCount[0]++; // Increment completed tasks count

                                    // Check if all tasks are completed
                                    if (completedTasksCount[0] == totalTasks) {
                                        // All tasks are completed, execute the second loop
                                        for (Map.Entry<NewStudent, String> entry : mapper.entrySet()) {
                                            NewStudent studentx = entry.getKey();
                                            String uId = entry.getValue();
                                            System.out.println("Key: " + studentx.getName() + ", Value: " + uId);
                                        }
                                    }


                                } else {
                                    // Handle signup failure
                                    Toast.makeText(UploadStudents.this, "Signup failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }

//            this loop not working because thread comes here before the for loop for authentication is completed so implemented same at line 225 - 232
            for (Map.Entry<NewStudent, String> entry : mapper.entrySet()) {
                NewStudent studentx = entry.getKey();
                String uId = entry.getValue();
                System.out.println("Key: " + studentx.getName() + ", Value: " + uId);
            }

            workbook.close();
        } catch (IOException | BiffException e) {


            e.printStackTrace();
        }

        return students;
    }

}


//
////                                Saving Additional information of user in fireStore with same id
//                                    userRef.set(student)
//                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//@Override
//public void onComplete(@NonNull Task<Void> task) {
//        if (task.isSuccessful()) {
//        Log.d("userId --- "  , "taskSuccessfull");
//        // User information saved to Firestore successfully
////                                                        Toast.makeText(AddStudent.this, "Student registered successfully!", Toast.LENGTH_SHORT).show();
//        mAuth.signOut();
//
//        //                                                        Intent intent = new Intent(AddStudent.this, StudentAddUpdatePage.class);
////                                                        startActivity(intent);
////                                                        finish();
//
//
//        } else {
//        // Handle Firestore document creation failure
//        Log.d("userId --- "  , "taskSuccessfull not pOsiibe");
//        Toast.makeText(UploadStudents.this, "Error saving user data to Firestore.", Toast.LENGTH_SHORT).show();
//        }
//        }
//        });


