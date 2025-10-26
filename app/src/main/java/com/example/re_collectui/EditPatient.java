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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditPatient extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPassword, etPhone, etEmail, etEmergencyPhone, etDiagnosis;
    private DatePicker dpDob;
    private Button btnSaveChanges;
    private FloatingActionButton btnAddPhoto;
    private ImageButton btnBack;
    private ImageView ivProfileImage;

    private String userImageBase64 = null;
    private Patient patientToEdit;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        Glide.with(this).load(imageUri).into(ivProfileImage);
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
        dpDob.setMaxDate(System.currentTimeMillis());
        populateFields();

        btnAddPhoto.setOnClickListener(v -> openGallery());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnBack.setOnClickListener(v -> onBackPressed());
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
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnAddPhoto = findViewById(R.id.fabAddPhoto);
        btnBack = findViewById(R.id.btnExit);
    }

    private void populateFields() {
        // --- THIS IS THE FIX ---
        // Use the helper method to safely set text fields
        setText(etFirstName, patientToEdit.getFirstName());
        setText(etLastName, patientToEdit.getLastName());
        setText(etPassword, patientToEdit.getPatientPassword());
        setText(etPhone, patientToEdit.getContactNumber());
        setText(etEmail, patientToEdit.getEmail());
        setText(etEmergencyPhone, patientToEdit.getEmergencyContact());
        setText(etDiagnosis, patientToEdit.getDiagnosis());

        // Safely set the date
        if (patientToEdit.getDoB() != null && !patientToEdit.getDoB().isEmpty()) {
            String[] dobParts = patientToEdit.getDoB().split("-");
            if (dobParts.length == 3) {
                try {
                    int year = Integer.parseInt(dobParts[0]);
                    int month = Integer.parseInt(dobParts[1]) - 1;
                    int day = Integer.parseInt(dobParts[2]);
                    dpDob.updateDate(year, month, day);
                } catch (NumberFormatException e) {
                    // Date was in a wrong format, do nothing
                }
            }
        }

        // Safely load the image, catching any errors from bad data
        if (patientToEdit.getImage() != null && !patientToEdit.getImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(patientToEdit.getImage(), Base64.DEFAULT);
                Glide.with(this)
                        .load(decodedString)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(ivProfileImage);
            } catch (IllegalArgumentException e) {
                // If decoding fails, load the default avatar
                ivProfileImage.setImageResource(R.drawable.default_avatar);
            }
        } else {
            ivProfileImage.setImageResource(R.drawable.default_avatar);
        }
        // --- END FIX ---
    }

    // --- ADD THIS HELPER METHOD ---
    // Safely sets text on an EditText, preventing crashes from null values.
    private void setText(EditText editText, String text) {
        if (text != null) {
            editText.setText(text);
        } else {
            editText.setText("");
        }
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
        // ... (This method remains unchanged)
        String urlpath= GlobalVars.apiPath;
        String url = urlpath + "edit_patient";
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