package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CreatePatient extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPassword, etPhone, etEmail, etEmergencyPhone, etDiagnosis;
    private DatePicker dpDob;
    private Button btnSignUp;
    private FloatingActionButton btnAddPhoto; // Changed from Button
    private ImageButton btnBack;
    private ImageView ivProfileImage;

    private String userImageBase64 = null;
    private boolean navigateToDashboardOnSuccess = false;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        ivProfileImage.setImageURI(imageUri);
                        try {
                            encodeImage(imageUri);
                            Toast.makeText(this, "Photo selected successfully", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to encode image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );
    private static final String API_URL = GlobalVars.apiPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_patient);
        navigateToDashboardOnSuccess = getIntent().getBooleanExtra("NAVIGATE_TO_DASHBOARD", false);
        bindViews();

        dpDob.setMaxDate(System.currentTimeMillis());

        btnBack.setOnClickListener(v -> onBackPressed());

        btnAddPhoto.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        btnSignUp.setOnClickListener(v -> {
            if (!validate()) return;

            String first = etFirstName.getText().toString().trim();
            String last = etLastName.getText().toString().trim();
            String password = etPassword.getText().toString();
            String phone = digitsOnly(etPhone.getText().toString());
            String email = etEmail.getText().toString().trim();
            String emPhone = digitsOnly(etEmergencyPhone.getText().toString());
            String diagnosis = etDiagnosis.getText().toString().trim();

            int y = dpDob.getYear();
            int m = dpDob.getMonth() + 1;
            int d = dpDob.getDayOfMonth();
            String dob = String.format("%04d-%02d-%02d", y, m, d);

            createPatient(first, last, dob, phone, diagnosis, emPhone, userImageBase64, email, password);
        });
    }

    private void bindViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        dpDob = findViewById(R.id.dpDob);
        etPassword = findViewById(R.id.editTextTextPassword);
        etPhone = findViewById(R.id.editTextPhone);
        etEmail = findViewById(R.id.editTextTextEmailAddress);
        etEmergencyPhone = findViewById(R.id.editTextEmPhone);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        ivProfileImage = findViewById(R.id.ivProfileImage);

        btnSignUp = findViewById(R.id.btnFormSignUp);
        // --- THIS IS THE FIX ---
        btnAddPhoto = findViewById(R.id.fabAddPhoto); // Changed from R.id.button2
        // --- END FIX ---
        btnBack = findViewById(R.id.btnExit);
    }

    private void encodeImage(Uri imageUri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        this.userImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d("Base64_Image", "Encoded string length: " + userImageBase64.length());
    }

    private boolean validate() {
        String first = get(etFirstName);
        String last = get(etLastName);
        String password = get(etPassword);
        String phone = digitsOnly(get(etPhone));
        String email = get(etEmail);
        String emPhone = digitsOnly(get(etEmergencyPhone));

        if (first.isEmpty()) { toast("First name required"); return false; }
        if (last.isEmpty()) { toast("Last name required"); return false; }
        if (password.length() < 8) { toast("Use at least 8 characters for password"); return false; }
        if (phone.length() != 10) { toast("Enter 10-digit contact number"); return false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Invalid email"); return false; }
        if (emPhone.length() != 10) { toast("Enter 10-digit emergency number"); return false; }
        return true;
    }

    private static String get(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private static String digitsOnly(String s) {
        return s.replaceAll("\\D+", "");
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private void createPatient( String firstName, String lastName, String DoB, String contactNumber, String diagnosis, String emergencyContact, String userImage, String email, String patientPassword) {
        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        int careGiverID = sharedPref.getInt("careGiverID", -1);
        if (careGiverID == -1) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = API_URL + "create_patient";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.getString("status").equals("success")) {
                            Toast.makeText(this, "Patient Account created!", Toast.LENGTH_SHORT).show();

                            if (navigateToDashboardOnSuccess) {
                                Intent intent = new Intent(CreatePatient.this, DashboardCaregiver.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + res.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        int code = error.networkResponse.statusCode;
                        String body = "";
                        try { body = new String(error.networkResponse.data, StandardCharsets.UTF_8); } catch (Exception ignored) {}
                        Toast.makeText(this, "HTTP "+code+": "+body, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "No response: "+error.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
                    }
                    error.printStackTrace();}
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("careGiverID", String.valueOf(careGiverID));
                params.put("firstName", firstName);
                params.put("lastName", lastName);
                params.put("DoB", DoB);
                params.put("contactNumber", contactNumber);
                params.put("diagnosis", diagnosis);
                params.put("emergencyContact", emergencyContact);
                if (userImage != null && !userImage.isEmpty()) {
                    params.put("userImage", userImage);
                }
                params.put("email", email);
                params.put("patientPassword", patientPassword);
                return params;
            }
        };

        queue.add(request);
    }
}