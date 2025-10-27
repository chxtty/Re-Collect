package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardCaregiver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_caregiver);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        String caregiverName = sharedPref.getString("name", "");
        TextView caregiverNameTextView = findViewById(R.id.txtWelcome);
        caregiverNameTextView.setText("Welcome, " + caregiverName + " :)");

        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v ->{
            Intent intent = new Intent(DashboardCaregiver.this, LoginActivity.class);
            sharedPref.edit().clear().apply();
            startActivity(intent);
            finish();
        });

    }

    public void on_Caregiver_profile_Click(View view) {

    }

    public void on_Caregiver_patients_Click(View view) {
    }

    public void on_Caregiver_requests_Click(View view) {
        Intent intent = new Intent(DashboardCaregiver.this, RequestsView.class);
        startActivity(intent);
    }


}
/*
package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_caregiver);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        int patientID = sharedPref.getInt("patientID", -1); // for patientID for session
        int careGiverID = sharedPref.getInt("careGiverID", -1);
        String caregiverFirstName = sharedPref.getString("caregiverFirstName", "Caregiver");
        btnLogout = findViewById(R.id.btnLogout);
        caregiver_profile = findViewById(R.id.caregiver_profile);
        caregiver_patients = findViewById(R.id.caregiver_patients);
        caregiver_requests = findViewById(R.id.caregiver_requests);
        txtWelcome = findViewById(R.id.txtWelcome); // ## ADD THIS ##
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



    public void on_Caregiver_requests_Click(View view) {
        Intent intent = new Intent(DashboardCaregiver.this, RequestsView.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This method is called every time the user returns to this screen.
        updateWelcomeMessage();
    }

    private void updateWelcomeMessage() {
        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        String caregiverFirstName = sharedPref.getString("caregiverFirstName", "Caregiver");
        txtWelcome.setText("Welcome back,\n" + caregiverFirstName);
    }


}
* */