package com.example.re_collectui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditPatient extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPassword, etPhone, etEmail, etEmergencyPhone, etDiagnosis;
    private DatePicker dpDob;
    private Button btnSaveChanges, btnAddPhoto;
    private ImageButton btnBack;

    private String userImageBase64 = null; // Holds new image if selected
    private Patient patientToEdit;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            encodeImage(imageUri);
                            Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(this, "Failed to encode image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_patient);

        patientToEdit = (Patient) getIntent().getSerializableExtra("PATIENT_DATA");
        if (patientToEdit == null) {
            Toast.makeText(this, "Error: No patient data found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bindViews();
        populateFields();

        btnAddPhoto.setOnClickListener(v -> openGallery());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void bindViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        dpDob = findViewById(R.id.dpDob);
        etPassword = findViewById(R.id.editTextTextPassword); // Using ID from your create XML
        etPhone = findViewById(R.id.editTextPhone);
        etEmail = findViewById(R.id.editTextTextEmailAddress);
        etEmergencyPhone = findViewById(R.id.editTextEmPhone);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnAddPhoto = findViewById(R.id.button2);
        btnBack = findViewById(R.id.btnExit);
    }

    private void populateFields() {
        etFirstName.setText(patientToEdit.getFirstName());
        etLastName.setText(patientToEdit.getLastName());
        etPassword.setText(patientToEdit.getPatientPassword());
        etPhone.setText(patientToEdit.getContactNumber());
        etEmail.setText(patientToEdit.getEmail());
        etEmergencyPhone.setText(patientToEdit.getEmergencyContact());
        etDiagnosis.setText(patientToEdit.getDiagnosis());

        String[] dobParts = patientToEdit.getDoB().split("-");
        dpDob.updateDate(Integer.parseInt(dobParts[0]), Integer.parseInt(dobParts[1]) - 1, Integer.parseInt(dobParts[2]));
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void encodeImage(Uri imageUri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        this.userImageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void saveChanges() {
        String url = "http://100.104.224.68/android/api.php?action=edit_patient";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if ("success".equals(res.getString("status"))) {
                            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK); // Signal to ViewPatient to refresh
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + res.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("patientID", String.valueOf(patientToEdit.getPatientID()));
                params.put("firstName", etFirstName.getText().toString().trim());
                params.put("lastName", etLastName.getText().toString().trim());
                params.put("DoB", String.format("%04d-%02d-%02d", dpDob.getYear(), dpDob.getMonth() + 1, dpDob.getDayOfMonth()));
                params.put("contactNumber", etPhone.getText().toString().trim());
                params.put("diagnosis", etDiagnosis.getText().toString().trim());
                params.put("emergencyContact", etEmergencyPhone.getText().toString().trim());
                params.put("email", etEmail.getText().toString().trim());
                params.put("patientPassword", etPassword.getText().toString());

                if (userImageBase64 != null && !userImageBase64.isEmpty()) {
                    params.put("userImage", userImageBase64);
                }
                return params;
            }
        };
        queue.add(request);
    }
}