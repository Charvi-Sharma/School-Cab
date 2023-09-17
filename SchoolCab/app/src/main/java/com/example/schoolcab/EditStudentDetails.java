package com.example.schoolcab;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import org.json.JSONObject;

public class EditStudentDetails extends AppCompatActivity {
    private FirebaseFirestore db;

    public static final String SHARED_PREFS = "shared_prefs";

    // key for schoolId
    public static final String sId = "sId";
    // variable for shared preferences.
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_details);
        db = FirebaseFirestore.getInstance();
        //        Getting the school id saved in local preferences
        sharedpreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String schoolID = sharedpreferences.getString("sId", null);

        String jsonString = getIntent().getStringExtra("data");
        String id = getIntent().getStringExtra("id");
        // Log the JSON string
        Log.d(TAG, "Received JSON data: " + jsonString);

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> jsonMap = gson.fromJson(jsonString, type);

//        {"standard":9,"address":"Samoil","sex":"M","name":"samoil","weight":56,
//        "rollNo":"0801CS201079","section":"A","guardian":"Shabbir","age":22,"phoneNo":9993,"defaultAddress":"Samoil "}

        String name = (String) jsonMap.get("name");
        String rollNo = (String) jsonMap.get("rollNo");
        String address = (String) jsonMap.get("address");
        String sex = (String) jsonMap.get("sex");
        String email = (String) jsonMap.get("email");
        String section = (String) jsonMap.get("section");
        String guardian = (String) jsonMap.get("guardian");
        String defaultAddress = (String) jsonMap.get("defaultAddress");
        int age = ((Double) jsonMap.get("age")).intValue();
        int weight = ((Double) jsonMap.get("weight")).intValue();
        int standard = ((Double) jsonMap.get("standard")).intValue();
        String phoneNo = (String) jsonMap.get("phoneNo");


//        Log.d(TAG , "hello "+age + "   " + weight + " " + phoneNo + " "+ standard);



        EditText edtName = findViewById(R.id.edtName);
        edtName.setText( name);

        TextView edtRollNo = findViewById(R.id.edtRollNo);
        edtRollNo.setText( rollNo);

        EditText edtGuardian = findViewById(R.id.edtGuardian);
        edtGuardian.setText( guardian);
//
        EditText edtPhoneNo = findViewById(R.id.edtPhoneNo);
        edtPhoneNo.setText(""+phoneNo);
//
        EditText edtAddress = findViewById(R.id.edtAddress);
        edtAddress.setText( address);
//
        EditText edtEmail = findViewById(R.id.edtEmail);
        edtEmail.setText( email);
//
        EditText edtDefaultAddress = findViewById(R.id.edtDefaultAddress);
        edtDefaultAddress.setText( defaultAddress);
//
        EditText edtClass = findViewById(R.id.edtClass);
        edtClass.setText("" +standard);
//
        EditText edtSection = findViewById(R.id.edtSection);
        edtSection.setText( section);
//
        EditText edtSex = findViewById(R.id.edtSex);
        edtSex.setText( sex);
//
        EditText edtAge = findViewById(R.id.edtAge);
        edtAge.setText(""+age);
//
        EditText edtWeight = findViewById(R.id.edtWeight);
        edtWeight.setText(""+weight);



        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            // Collect student details from EditText fields

            String name1 = edtName.getText().toString();
            String rollNo1 = edtRollNo.getText().toString();
            String guardian1 = edtGuardian.getText().toString();
            String phoneNo1 = edtPhoneNo.getText().toString();
            String address1 = edtAddress.getText().toString();
            String  defaultAddress1 = edtDefaultAddress.getText().toString();
            int standard1 = Integer.parseInt(edtClass.getText().toString());
            String section1  = edtSection.getText().toString();
            String sex1 = edtSex.getText().toString();
            String email1 = edtEmail.getText().toString();
            int age1 = Integer.parseInt(edtAge.getText().toString());
            int weight1 = Integer.parseInt(edtWeight.getText().toString());



            Log.d("StudentRegistration", "Name: " + name + ", Password: " + rollNo);

//            NewStudent student = new NewStudent(name, password);
            NewStudent student = new NewStudent();
            student.setName(name1);
            student.setRollNo(rollNo1);
            student.setGuardian(guardian1);
            student.setPhoneNo(phoneNo1);
            student.setAddress(address1);
            student.setDefaultAddress(defaultAddress1);
            student.setStandard(standard1);
            student.setSection(section1);
            student.setSex(sex1);
            student.setAge(age1);
            student.setWeight(weight1);
            student.setEmail(email1);
            student.setSchoolId(schoolID);



            CollectionReference studentsCollection = db.collection("students");


            studentsCollection.document(id).set(student)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                          Log.d("SUCCESS" , "data written succesfully");

                            Intent intent = new Intent(EditStudentDetails.this, StudentAddUpdatePage.class);
                            startActivity(intent);

                            Toast.makeText(EditStudentDetails.this, "Student Updated successfully!", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Error" , "FOUND ERROR IN WRITING");
                            Toast.makeText(EditStudentDetails.this, "Unable to Update Found Some Error!", Toast.LENGTH_SHORT).show();

                            // An error occurred while writing the data to the document
                            // You can handle errors here
                        }
                    });








//            studentsCollection.add(student)
//                    .addOnSuccessListener(documentReference -> {
//                        Toast.makeText(this, "Student registered successfully!", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.e("StudentRegistration", "Error registering student", e);
//                        Toast.makeText(this, "Error registering student", Toast.LENGTH_SHORT).show();
//                    });


        });

    }


}