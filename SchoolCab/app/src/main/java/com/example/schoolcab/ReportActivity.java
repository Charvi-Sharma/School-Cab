package com.example.schoolcab;


import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    SharedPreferences sharedpreferences;
    private String TAG = "Report Activity";
    int rowNum=1;

    Workbook wb = new HSSFWorkbook();
    Sheet sheet = wb.createSheet("sheet1");

    String startDat="";

    Map<String,String> busMap = new HashMap<>();



    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("Report Activity", "Permission Granted");
                    fetchStudentData();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    Log.d(TAG, "Permission Not Granted: ");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Report Activity", "Started");

        setContentView(R.layout.activity_attendance_report);

        sharedpreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE);

        Button startDate = findViewById(R.id.idBtnPickStartDate);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        ReportActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our text view.
                                startDat = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                                DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                                try {
                                    Date parsedDate = df.parse(startDat);
                                    startDat = df.format(parsedDate);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }

                                startDate.setText(startDat);
                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);
                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });

        Button btn = findViewById(R.id.edtGenerate);
        btn.setOnClickListener(v ->{
            if(ContextCompat.checkSelfPermission(getApplicationContext(),"android.permission.WRITE_EXTERNAL_STORAGE")== PackageManager.PERMISSION_DENIED){
                Log.d("Report Activity", "Permission Denied");
                requestPermissionLauncher.launch("android.permission.WRITE_EXTERNAL_STORAGE");
            }
            else {
                Log.d("Report Activity", "Permission Already Granted");
                fetchStudentData();


            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void fetchStudentData(){
        Row headerRow = sheet.createRow(0);

        Cell tempcell = headerRow.createCell(1);
        tempcell.setCellValue("Bus No");
        Cell tempcell1 = headerRow.createCell(2);
        tempcell1.setCellValue("Arrival Time");
        Cell tempcell2 = headerRow.createCell(3);
        tempcell2.setCellValue("Departure Time");

        String school = sharedpreferences.getString("sId",NULL);

        db = FirebaseFirestore.getInstance();

        db.collection("bus")
                .whereEqualTo("schoolId",school)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String busId = document.getId();
                                String  busNo = String.valueOf(document.getData().get("busNo"));
                                Log.d(busId+": ", busNo);
                                busMap.put(busId, busNo);
                            }
                            db.collection("students")
                                    .whereEqualTo("schoolId",school)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Task Successful");
                                                Log.d(TAG, String.valueOf(task.getResult().size()));
                                                DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                                                DateFormat tf = new SimpleDateFormat("HH:mm:ss");
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    //Log.d("Doc", String.valueOf(document));
                                                    Row row = sheet.createRow(rowNum);
                                                    Cell namecell = row.createCell(0);
                                                    namecell.setCellValue((String) document.getData().get("name"));
                                                    Cell cell = row.createCell(1);
                                                    cell.setCellValue(busMap.get((String) document.getData().get("busId")));
                                                    Cell arrivalCell = row.createCell(2);
                                                    List<Timestamp> arrivalAtten = (List<Timestamp>) document.getData().get("arrivalAttendance");
                                                    if(arrivalAtten!=null){
                                                        for(Timestamp t : arrivalAtten){
                                                            String date = df.format(t.toDate());
                                                            if(startDat.equals(date)){
                                                                arrivalCell.setCellValue(tf.format(t.toDate()));
                                                            }
                                                        }
                                                    }
                                                    Cell departCell = row.createCell(3);
                                                    List<Timestamp> departAtten = (List<Timestamp>) document.getData().get("departureAttendance");
                                                    if(departAtten!=null){
                                                        for(Timestamp t : departAtten){
                                                            String date = df.format(t.toDate());
                                                            Log.d("date: ", date);
                                                            if(startDat.equals(date)){
                                                                departCell.setCellValue(tf.format(t.toDate()));
                                                            }
                                                        }
                                                    }
                                                    //Log.d(TAG, document.getId() + " => " + document.getData());
                                                    rowNum++;
                                                }

                                                File path = Environment.getExternalStoragePublicDirectory(
                                                        Environment.DIRECTORY_DOWNLOADS);
                                                File file = new File(path, "AttendanceReport"+startDat+".xls");

                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    try {
                                                        boolean result = Files.deleteIfExists(file.toPath());
                                                        Log.d(TAG, String.valueOf(result));
                                                    } catch (IOException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                                try (OutputStream fileOut = new FileOutputStream(file)) {
                                                    wb.write(fileOut);
                                                    Toast.makeText(ReportActivity.this, "Attendance Report Generated Succesfully", Toast.LENGTH_LONG).show();
                                                } catch (FileNotFoundException e) {
                                                    throw new RuntimeException(e);
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            } else {
                                                Log.d(TAG, "Error while fetching student data: ", task.getException());
                                            }
                                        }
                                    });
                        }
                    }
                });


    }

}


