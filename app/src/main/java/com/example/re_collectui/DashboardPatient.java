package com.example.re_collectui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardPatient extends AppCompatActivity {

    Button btnLogout, btnEvents;

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

        btnLogout = findViewById(R.id.btnLogout);
        btnEvents = findViewById(R.id.btnEvents);

        btnLogout.setOnClickListener(v ->{
            Intent intent = new Intent(DashboardPatient.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnEvents.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}