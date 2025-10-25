package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView; // Import TextView

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class DashboardCaregiver extends AppCompatActivity {

    Button btnLogout;
    ConstraintLayout caregiver_profile;
    ConstraintLayout caregiver_patients;
    ConstraintLayout caregiver_requests;
    TextView txtWelcome; // ## ADD THIS ##

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_caregiver);

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        int careGiverID = sharedPref.getInt("careGiverID", -1);

        // ## ADD THIS: Retrieve the first name saved during login ##
        String caregiverFirstName = sharedPref.getString("caregiverFirstName", "Caregiver");

        // Bind views
        btnLogout = findViewById(R.id.btnLogout);
        caregiver_profile = findViewById(R.id.caregiver_profile);
        caregiver_patients = findViewById(R.id.caregiver_patients);
        caregiver_requests = findViewById(R.id.caregiver_requests);
        txtWelcome = findViewById(R.id.txtWelcome); // ## ADD THIS ##

        // ## ADD THIS: Set the welcome text ##
        // Using "\n" forces the name onto a new line and the `gravity="center"` in XML handles alignment
        txtWelcome.setText("Welcome back,\n" + caregiverFirstName);

        // --- Listeners ---

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardCaregiver.this, LoginActivity.class);
            sharedPref.edit().clear().apply();
            startActivity(intent);
            finish();
        });

        caregiver_profile.setOnClickListener(v -> {
            if (careGiverID != -1) {
                Intent intent = new Intent(DashboardCaregiver.this, ViewCaregiver.class);
                intent.putExtra("careGiverID", careGiverID);
                startActivity(intent);
            }
        });

        caregiver_patients.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardCaregiver.this, ViewPatientList.class);
            startActivity(intent);
        });

        caregiver_requests.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardCaregiver.this, ViewPatientList.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // This method is called every time the user returns to this screen.
        updateWelcomeMessage();
    }

    // ## ADD THIS HELPER METHOD FOR CLEAN CODE ##
    private void updateWelcomeMessage() {
        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        String caregiverFirstName = sharedPref.getString("caregiverFirstName", "Caregiver");
        txtWelcome.setText("Welcome back,\n" + caregiverFirstName);
    }
}