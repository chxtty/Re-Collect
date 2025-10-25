package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class DashboardPatient extends AppCompatActivity {

    Button btnLogout;
    ConstraintLayout eventsOption, diaryOption, communityOption, caregiverOption, activityOption, myselfOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboardpatient);

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        // Get IDs from the user's session
        int patientID = sharedPref.getInt("patientID", -1);
        // IMPORTANT: Assumes careGiverID is saved to session on login
        int careGiverID = sharedPref.getInt("careGiverID", -1);

        btnLogout = findViewById(R.id.btnLogout);
        eventsOption = findViewById(R.id.eventsOption);
        diaryOption = findViewById(R.id.dairyOption);
        activityOption = findViewById(R.id.activityOption);
        myselfOption = findViewById(R.id.myselfOption);
        communityOption = findViewById(R.id.commOption);
        caregiverOption = findViewById(R.id.CaregiverOption);

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, LoginActivity.class);
            sharedPref.edit().clear().apply();
            startActivity(intent);
            finish();
        });

        eventsOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, EventsView.class);
            startActivity(intent);
        });

        diaryOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, ViewDiaryEntries.class);
            startActivity(intent);
        });

        myselfOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, ViewPatient.class);
            intent.putExtra("patientID", patientID);
            startActivity(intent);
        });

        activityOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, ViewActivities.class);
            startActivity(intent);
        });

        communityOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, ViewCommunity.class);
            // You were missing this line. It adds the patient's ID to the intent
            // so the next screen knows which community to load.
            intent.putExtra("patientID", patientID);
            startActivity(intent);
        });

        // ADDED: OnClickListener for the caregiver option
        caregiverOption.setOnClickListener(v -> {
            if (careGiverID != -1) {
                Intent intent = new Intent(DashboardPatient.this, ViewCaregiver.class);
                // Pass the assigned caregiver's ID to the ViewCaregiver activity
                intent.putExtra("careGiverID", careGiverID);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No caregiver is assigned to this profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}