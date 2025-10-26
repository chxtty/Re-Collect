package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory; // Import for default image
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
import android.widget.ImageView; // Import ImageView
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import FAB

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar; // Import Calendar
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView; // Import CircleImageView

public class CreateCaregiver extends AppCompatActivity {

    private EditText etFirstName, etLastName, etPassword, etContactNumber, etEmail, etWorkNumber, etEmployerType;
    private DatePicker dpDob;
    private Button btnSignUp;
    private ImageButton btnBack;
    private FloatingActionButton fabAddPhoto; // Changed to FloatingActionButton
    private CircleImageView ivProfilePicture; // ImageView to display profile picture

    private String userImageBase64 = null;
    private Bitmap selectedImageBitmap = null; // To hold the selected image bitmap

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            selectedImageBitmap = bitmap; // Store the bitmap
                            ivProfilePicture.setImageBitmap(bitmap); // Display the selected image
                            encodeImage(bitmap); // Encode the selected bitmap
                            Toast.makeText(this, "Photo selected successfully", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_caregiver);

        bindViews();
        setupDatePicker(); // New method to set up date picker restrictions
        setInitialProfileImage(); // Set the default profile image

        btnBack.setOnClickListener(v -> onBackPressed());

        fabAddPhoto.setOnClickListener(v -> { // Changed to fabAddPhoto
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        btnSignUp.setOnClickListener(v -> {
            if (!validate()) return;

            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String password = etPassword.getText().toString();
            String contactNumber = digitsOnly(etContactNumber.getText().toString());
            String email = etEmail.getText().toString().trim();
            String workNumber = digitsOnly(etWorkNumber.getText().toString());
            String employerType = etEmployerType.getText().toString().trim();

            int y = dpDob.getYear();
            int m = dpDob.getMonth() + 1;
            int d = dpDob.getDayOfMonth();
            String dob = String.format("%04d-%02d-%02d", y, m, d);

            createCaregiver(firstName, lastName, dob, contactNumber, workNumber, employerType, email, password, userImageBase64);
        });
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

        btnSignUp = findViewById(R.id.btnSignUp);
        fabAddPhoto = findViewById(R.id.fabAddPhoto); // Bind the FloatingActionButton
        ivProfilePicture = findViewById(R.id.ivProfilePicture); // Bind the ImageView
        btnBack = findViewById(R.id.btnExit);
    }

    private void setupDatePicker() {
        Calendar today = Calendar.getInstance();
        dpDob.setMaxDate(today.getTimeInMillis()); // Prevent future dates

        // Set initial date to something sensible, e.g., 20 years ago for typical adult
        Calendar defaultDate = Calendar.getInstance();
        defaultDate.add(Calendar.YEAR, -20);
        dpDob.updateDate(defaultDate.get(Calendar.YEAR), defaultDate.get(Calendar.MONTH), defaultDate.get(Calendar.DAY_OF_MONTH));
    }

    private void setInitialProfileImage() {
        // Set a default profile image (e.g., from drawable resources)
        ivProfilePicture.setImageResource(R.drawable.default_avatar);

        // Optionally, encode the default image as Base64 if you want to send it
        // even if the user doesn't pick a new one.
        // This is important if your API requires an image for *all* users.
        try {
            Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
            encodeImage(defaultBitmap); // Encode default image initially
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("CreateCaregiver", "Failed to encode default profile picture.");
        }
    }


    private void encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream); // Compress to JPEG, 50% quality
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        this.userImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private boolean validate() {
        String first = get(etFirstName);
        String last = get(etLastName);
        String password = get(etPassword);
        String phone = digitsOnly(get(etContactNumber));
        String email = get(etEmail);

        // Date picker validation: Ensure selected date is not in the future (already handled by setMaxDate, but good to double check)
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(dpDob.getYear(), dpDob.getMonth(), dpDob.getDayOfMonth());
        if (selectedDate.after(Calendar.getInstance())) {
            toast("Date of Birth cannot be in the future");
            return false;
        }

        if (first.isEmpty()) { toast("First name is required"); return false; }
        if (last.isEmpty()) { toast("Last name is required"); return false; }
        if (password.length() < 8) { toast("Password must be at least 8 characters"); return false; }
        if (phone.length() != 10) { toast("Enter a valid 10-digit contact number"); return false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Please enter a valid email address"); return false; }

        return true;
    }

    // Helper methods
    private static String get(EditText et) { return et.getText() == null ? "" : et.getText().toString().trim(); }
    private static String digitsOnly(String s) { return s.replaceAll("\\D+", ""); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private void createCaregiver(String firstName, String lastName, String dob, String contactNumber, String workNumber, String employerType, String email, String password, String userImage) {
        String url = "http://100.104.224.68/android/api.php?action=create_caregiver";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.getString("status").equals("success")) {
                            Toast.makeText(this, "Caregiver Account Created!", Toast.LENGTH_SHORT).show();
                            SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            int careGiverID= Integer.parseInt(res.getString("caregiverID"));
                            editor.putInt("careGiverID", careGiverID);
                            editor.apply();
                            Intent intent = new Intent(CreateCaregiver.this, DashboardCaregiver.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + res.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
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