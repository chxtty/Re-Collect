package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardPatient extends AppCompatActivity {

    Button btnLogout;
    ConstraintLayout eventsOption;
    ConstraintLayout diaryOption;
    ConstraintLayout activityOption;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboardpatient);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        int patientID = sharedPref.getInt("patientID", -1); // for patientID for session



        btnLogout = findViewById(R.id.btnLogout);
        eventsOption = findViewById(R.id.eventsOption);
        diaryOption = findViewById(R.id.dairyOption);
        activityOption = findViewById(R.id.activityOption);

        btnLogout.setOnClickListener(v ->{
            Intent intent = new Intent(DashboardPatient.this, LoginActivity.class);
            sharedPref.edit().clear().apply();
            startActivity(intent);
        });

        eventsOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, EventsView.class);
            startActivity(intent);
        });

        diaryOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, ViewDiaryEntries.class);
            startActivity(intent);
        });

        activityOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, ViewActivities.class);
            startActivity(intent);
        });

    }
}