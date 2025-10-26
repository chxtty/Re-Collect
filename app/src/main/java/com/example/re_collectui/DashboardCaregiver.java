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