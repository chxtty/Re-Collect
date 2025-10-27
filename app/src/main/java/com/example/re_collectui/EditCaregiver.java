package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
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

public class EditCaregiver extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPassword, etContactNumber, etEmail, etWorkNumber, etEmployerType;
    private DatePicker dpDob;
    private Button btnSaveChanges;
    private FloatingActionButton btnAddPhoto;
    private ImageButton btnBack;
    private ImageView ivProfileImage;
    private String userImageBase64 = null;
    private Care_giver caregiverToEdit;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        Glide.with(this).load(imageUri).into(ivProfileImage);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_caregiver);

        caregiverToEdit = (Care_giver) getIntent().getSerializableExtra("CAREGIVER_DATA");
        if (caregiverToEdit == null) {
            Toast.makeText(this, "Error: No caregiver data found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bindViews();
        dpDob.setMaxDate(System.currentTimeMillis());
        populateFields();

        btnBack.setOnClickListener(v -> onBackPressed());
        btnAddPhoto.setOnClickListener(v -> openGallery());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void bindViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        dpDob = findViewById(R.id.dpDob);
        etPassword = findViewById(R.id.editPassword);
        etContactNumber = findViewById(R.id.editTextPhone);
        etEmail = findViewById(R.id.etEmail);
        etWorkNumber = findViewById(R.id.etWorkNumber);
        etEmployerType = findViewById(R.id.etEmployerType);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnAddPhoto = findViewById(R.id.fabAddPhoto);
        btnBack = findViewById(R.id.btnExit);
    }

    private void populateFields() {
        setText(etFirstName, caregiverToEdit.getFirstName());
        setText(etLastName, caregiverToEdit.getLastName());
        setText(etPassword, caregiverToEdit.getCaregiverPassword());
        setText(etContactNumber, caregiverToEdit.getContactNumber());
        setText(etEmail, caregiverToEdit.getEmail());
        setText(etWorkNumber, caregiverToEdit.getWorkNumber());
        setText(etEmployerType, caregiverToEdit.getEmployerType());

        if (caregiverToEdit.getDoB() != null && !caregiverToEdit.getDoB().isEmpty()) {
            String[] dobParts = caregiverToEdit.getDoB().split("-");
            if (dobParts.length == 3) {
                try {
                    int year = Integer.parseInt(dobParts[0]);
                    int month = Integer.parseInt(dobParts[1]) - 1;
                    int day = Integer.parseInt(dobParts[2]);
                    dpDob.updateDate(year, month, day);
                } catch (NumberFormatException e) {
                }
            }
        }

        if (caregiverToEdit.getUserImage() != null && !caregiverToEdit.getUserImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(caregiverToEdit.getUserImage(), Base64.DEFAULT);
                Glide.with(this)
                        .load(decodedString)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(ivProfileImage);
            } catch (IllegalArgumentException e) {
                ivProfileImage.setImageResource(R.drawable.default_avatar);
            }
        } else {
            ivProfileImage.setImageResource(R.drawable.default_avatar);
        }
    }

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
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        this.userImageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void saveChanges() {
        if (!validate()) return;
        String urlpath= GlobalVars.apiPath;
        String url = urlpath + "edit_caregiver";
        RequestQueue queue = Volley.newRequestQueue(this);

        final String newFirstName = etFirstName.getText().toString().trim();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if ("success".equals(res.getString("status"))) {
                            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("caregiverFirstName", newFirstName);
                            if (userImageBase64 != null) {
                                editor.putString("caregiverImage", userImageBase64);
                            }
                            editor.apply();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + res.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("careGiverID", String.valueOf(caregiverToEdit.getCareGiverID()));
                params.put("firstName", etFirstName.getText().toString().trim());
                params.put("lastName", etLastName.getText().toString().trim());
                String dob = String.format("%04d-%02d-%02d", dpDob.getYear(), dpDob.getMonth() + 1, dpDob.getDayOfMonth());
                params.put("DoB", dob);
                params.put("contactNumber", etContactNumber.getText().toString().replaceAll("\\D+", ""));
                params.put("workNumber", etWorkNumber.getText().toString().replaceAll("\\D+", ""));
                params.put("employerType", etEmployerType.getText().toString().trim());
                params.put("email", etEmail.getText().toString().trim());
                params.put("caregiverPassword", etPassword.getText().toString());

                if (userImageBase64 != null && !userImageBase64.isEmpty()) {
                    params.put("userImage", userImageBase64);
                }
                return params;
            }
        };
        queue.add(request);
    }

    private boolean validate() {
        if (etFirstName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "First name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etPassword.getText().toString().length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}