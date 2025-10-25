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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CreateCaregiver extends AppCompatActivity {

    // Fields specific to the Caregiver model
    private EditText etFirstName, etLastName, etPassword, etContactNumber, etEmail, etWorkNumber, etEmployerType;
    private DatePicker dpDob;
    private Button btnSignUp, btnAddPhoto;
    private ImageButton btnBack;

    private String userImageBase64 = null;

    // The ActivityResultLauncher for picking an image from the gallery
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // **ASSUMPTION**: Your layout file is named 'activity_create_caregiver.xml'
        setContentView(R.layout.activity_create_caregiver);

        bindViews();

        btnBack.setOnClickListener(v -> onBackPressed());

        btnAddPhoto.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        btnSignUp.setOnClickListener(v -> {
            if (!validate()) return;

            // Get values from the form
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String password = etPassword.getText().toString();
            String contactNumber = digitsOnly(etContactNumber.getText().toString());
            String email = etEmail.getText().toString().trim();
            String workNumber = digitsOnly(etWorkNumber.getText().toString()); // Optional field
            String employerType = etEmployerType.getText().toString().trim(); // Optional field

            int y = dpDob.getYear();
            int m = dpDob.getMonth() + 1;
            int d = dpDob.getDayOfMonth();
            String dob = String.format("%04d-%02d-%02d", y, m, d);

            // Call the method to send data to the server
            createCaregiver(firstName, lastName, dob, contactNumber, workNumber, employerType, email, password, userImageBase64);
        });
    }

    private void bindViews() {
        // **ASSUMPTION**: These are the IDs in your 'activity_create_caregiver.xml'
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        dpDob = findViewById(R.id.dpDob);
        etPassword = findViewById(R.id.editPassword);
        etContactNumber = findViewById(R.id.editTextPhone);
        etEmail = findViewById(R.id.etEmail);
        etWorkNumber = findViewById(R.id.etWorkNumber);
        etEmployerType = findViewById(R.id.etEmployerType);

        btnSignUp = findViewById(R.id.btnSignUp);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnBack = findViewById(R.id.btnExit);
    }

    private void encodeImage(Uri imageUri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        this.userImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private boolean validate() {
        String first = get(etFirstName);
        String last = get(etLastName);
        String password = get(etPassword);
        String phone = digitsOnly(get(etContactNumber));
        String email = get(etEmail);

        if (first.isEmpty()) { toast("First name is required"); return false; }
        if (last.isEmpty()) { toast("Last name is required"); return false; }
        if (password.length() < 8) { toast("Password must be at least 8 characters"); return false; }
        if (phone.length() != 10) { toast("Enter a valid 10-digit contact number"); return false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Please enter a valid email address"); return false; }
        // Work number and employer type are optional, so no validation is needed unless specified
        return true;
    }

    // Helper methods (can be kept the same)
    private static String get(EditText et) { return et.getText() == null ? "" : et.getText().toString().trim(); }
    private static String digitsOnly(String s) { return s.replaceAll("\\D+", ""); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private void createCaregiver(String firstName, String lastName, String dob, String contactNumber, String workNumber, String employerType, String email, String password, String userImage) {
        // The URL for the caregiver creation API endpoint
        String url = "http://100.104.224.68/android/api.php?action=create_caregiver";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.getString("status").equals("success")) {
                            Toast.makeText(this, "Caregiver Account Created!", Toast.LENGTH_SHORT).show();
                            // On success, navigate to the Login screen
                            SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            int careGiverID= Integer.parseInt(res.getString("caregiverID"));
                            editor.putInt("careGiverID", careGiverID);
                            editor.apply();
                            Intent intent = new Intent(CreateCaregiver.this, DashboardCaregiver.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Prevent user from going back to the sign-up form
                        } else {
                            Toast.makeText(this, "Error: " + res.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Same robust error handling as in CreatePatient
                    if (error.networkResponse != null) {
                        String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Toast.makeText(this, "Error " + error.networkResponse.statusCode + ": " + body, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Network error: " + error.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
                    }
                    error.printStackTrace();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Map the form data to the expected POST parameters for the caregiver table
                params.put("firstName", firstName);
                params.put("lastName", lastName);
                params.put("DoB", dob);
                params.put("contactNumber", contactNumber);
                params.put("workNumber", workNumber);
                params.put("employerType", employerType);
                params.put("email", email);
                params.put("caregiverPassword", password);
                if (userImage != null && !userImage.isEmpty()) {
                    params.put("userImage", userImage);
                }
                return params;
            }
        };

        queue.add(request);
    }
}