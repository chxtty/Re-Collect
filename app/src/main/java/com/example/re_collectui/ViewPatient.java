package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.Period;

public class ViewPatient extends AppCompatActivity {

    private static final String BASE_URL = GlobalVars.apiPath;

    private TextView tvName;
    private ImageView ivUser;
    private ImageButton exitBtn;
    private EditText pwValue;
    private Button btnEditPatient;
    private ConstraintLayout commOption;
    private View emailTile, diagnosisTile, contactTile, emergencyTile, pillPassword, pillAge;
    private Patient currentPatient;
    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && currentPatient != null) {
                    fetchPatient(currentPatient.getPatientID());
                    Toast.makeText(this, "Profile refreshed", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_patient);

        bindViews();
        setupStaticUI();
        exitBtn.setOnClickListener(v-> onBackPressed());

        int patientId = getIntent().getIntExtra("patientID", -1);
        if (patientId != -1) {
            fetchPatient(patientId);
        } else {
            Toast.makeText(this, "Error: Patient ID not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindViews() {
        emailTile = findViewById(R.id.fieldEmail);
        diagnosisTile = findViewById(R.id.fieldDiagnosis);
        contactTile = findViewById(R.id.fieldContact);
        emergencyTile = findViewById(R.id.fieldEmergency);
        exitBtn = findViewById(R.id.btnExit);
        ivUser = findViewById(R.id.profileImage);
        btnEditPatient = findViewById(R.id.button3);
        tvName = findViewById(R.id.tvName);
        pillPassword = findViewById(R.id.pillPassword);
        pillAge = findViewById(R.id.pillAge);
        commOption = findViewById(R.id.commOption);
    }

    private void setupCaregiverFeatures(Patient patient) {
        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        int loggedInPatientId = sharedPref.getInt("patientID", -1);

        Log.d("VISIBILITY_CHECK", "------------------------------------");
        Log.d("VISIBILITY_CHECK", "Logged-in User's patientID: " + loggedInPatientId);
        Log.d("VISIBILITY_CHECK", "Viewing Profile of patientID: " + patient.getPatientID());
        if (loggedInPatientId == patient.getPatientID()) {
            Log.d("VISIBILITY_CHECK", "RESULT: IDs match. Hiding community button.");
            commOption.setVisibility(View.GONE);
        } else {
            Log.d("VISIBILITY_CHECK", "RESULT: IDs DO NOT match. Showing community button.");
            commOption.setVisibility(View.VISIBLE);
            commOption.setOnClickListener(v -> {
                Intent intent = new Intent(ViewPatient.this, ViewCommunity.class);
                intent.putExtra("patientID", patient.getPatientID());
                startActivity(intent);
            });
        }
    }

    private void setupStaticUI() {
        ((TextView) emailTile.findViewById(R.id.tvLabel)).setText("EMAIL:");
        ((TextView) diagnosisTile.findViewById(R.id.tvLabel)).setText("DIAGNOSIS:");
        ((TextView) contactTile.findViewById(R.id.tvLabel)).setText("CONTACT NO.:");
        ((TextView) emergencyTile.findViewById(R.id.tvLabel)).setText("EMERGENCY NO.:");
        ((TextView) pillAge.findViewById(R.id.tvLabel)).setText("Age:");
        ((TextView) pillPassword.findViewById(R.id.tvLabel)).setText("PASSWORD:");
        pwValue = pillPassword.findViewById(R.id.tvValue);
        ImageButton pwToggle = pillPassword.findViewById(R.id.btnToggle);
        pwToggle.setVisibility(View.VISIBLE);
        pwValue.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
        pwToggle.setImageResource(R.drawable.eye_24);
        pwToggle.setOnClickListener(v -> {
            boolean isMasked = pwValue.getTransformationMethod() instanceof android.text.method.PasswordTransformationMethod;
            if (isMasked) {
                pwValue.setTransformationMethod(null);
                pwToggle.setImageResource(R.drawable.crossed_eye_24);
            } else {
                pwValue.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
                pwToggle.setImageResource(R.drawable.eye_24);
            }
            pwValue.setTypeface(pwValue.getTypeface());
        });
    }

    private void checkIfCanEdit(Patient patient) {
        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        int loggedInPatientId = sharedPref.getInt("patientID", -1);
        int loggedInCaregiverId = sharedPref.getInt("careGiverID", -1);
        boolean canEdit = (loggedInPatientId == patient.getPatientID()) || (loggedInCaregiverId == patient.getCareGiverID());
        if (canEdit) {
            btnEditPatient.setVisibility(View.VISIBLE);
            btnEditPatient.setOnClickListener(v -> {
                if (currentPatient != null) {
                    Intent intent = new Intent(ViewPatient.this, EditPatient.class);
                    intent.putExtra("PATIENT_DATA", currentPatient);
                    editLauncher.launch(intent);
                }
            });
        } else {
            btnEditPatient.setVisibility(View.GONE);
        }
    }

    private void fetchPatient(int patientId) {
        String url = BASE_URL + "view_patient&patientId=" + patientId;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest req = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if ("success".equals(json.optString("status"))) {
                    JSONArray arr = json.optJSONArray("patients");
                    if (arr != null && arr.length() > 0) {
                        this.currentPatient = Patient.fromJson(arr.getJSONObject(0));
                        updateUI(this.currentPatient);
                        checkIfCanEdit(this.currentPatient);
                        setupCaregiverFeatures(this.currentPatient);
                    } else {
                        Toast.makeText(this, "Patient not found", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Error: " + json.optString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show());
        queue.add(req);
    }

    private void updateUI(Patient p) {
        tvName.setText(p.getFirstName() + " " + p.getLastName());
        ((TextView) emailTile.findViewById(R.id.tvValue)).setText(p.getEmail());
        ((TextView) contactTile.findViewById(R.id.tvValue)).setText(p.getContactNumber());
        ((TextView) diagnosisTile.findViewById(R.id.tvValue)).setText(p.getDiagnosis());
        ((TextView) emergencyTile.findViewById(R.id.tvValue)).setText(p.getEmergencyContact());
        ((EditText) pillPassword.findViewById(R.id.tvValue)).setText(p.getPatientPassword());
        ((TextView) pillAge.findViewById(R.id.tvValue)).setText(String.valueOf(getAgeFromIso(p.getDoB())));

        String base64Image = p.getImage();

        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);

                Glide.with(this)
                        .load(decodedString)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(ivUser);
            } catch (IllegalArgumentException e) {
                ivUser.setImageResource(R.drawable.default_avatar);
            }
        } else {
            ivUser.setImageResource(R.drawable.default_avatar);
        }
    }

    public static int getAgeFromIso(String dobIso) {
        if (dobIso == null || dobIso.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return 0;
        try {
            return Period.between(LocalDate.parse(dobIso), LocalDate.now()).getYears();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}