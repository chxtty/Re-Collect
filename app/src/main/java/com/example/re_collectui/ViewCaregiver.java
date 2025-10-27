package com.example.re_collectui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.HashMap;
import java.util.Map;

public class ViewCaregiver extends AppCompatActivity {

    private static final String BASE_URL = GlobalVars.apiPath;
    private TextView tvName;
    private ImageView ivUser;
    private ImageButton exitBtn;
    private Button btnEditProfile, btnDeleteAccount; // ✅ Add delete button

    private View emailTile, contactTile, workNumberTile, employerTypeTile, pillPassword, pillAge;
    private Care_giver currentCaregiver;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && currentCaregiver != null) {
                    fetchCaregiver(currentCaregiver.getCareGiverID());
                    Toast.makeText(this, "Profile refreshed", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_caregiver);

        bindViews();
        exitBtn.setOnClickListener(v -> onBackPressed());
        setupTiles();

        int careGiverIdToView = getIntent().getIntExtra("careGiverID", -1);

        if (careGiverIdToView != -1) {
            fetchCaregiver(careGiverIdToView);
            checkIfOwner(careGiverIdToView);
        } else {
            Toast.makeText(this, "Error: Caregiver ID not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindViews() {
        tvName = findViewById(R.id.tvName);
        ivUser = findViewById(R.id.profileImage);
        exitBtn = findViewById(R.id.btnExit);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount); // ✅ Bind new button
        emailTile = findViewById(R.id.fieldEmail);
        contactTile = findViewById(R.id.fieldContact);
        workNumberTile = findViewById(R.id.fieldWorkNumber);
        employerTypeTile = findViewById(R.id.fieldEmployerType);
        pillPassword = findViewById(R.id.pillPassword);
        pillAge = findViewById(R.id.pillAge);
    }

    private void checkIfOwner(int profileId) {
        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        int loggedInCaregiverId = sharedPref.getInt("careGiverID", -1);
        boolean isCaregiverLoggedIn = sharedPref.getInt("patientID", -1) == -1;

        if (isCaregiverLoggedIn && profileId == loggedInCaregiverId) {
            btnEditProfile.setVisibility(View.VISIBLE);
            btnDeleteAccount.setVisibility(View.VISIBLE); // ✅ Show delete button

            btnEditProfile.setOnClickListener(v -> {
                if (currentCaregiver != null) {
                    Intent intent = new Intent(ViewCaregiver.this, EditCaregiver.class);
                    intent.putExtra("CAREGIVER_DATA", currentCaregiver);
                    editLauncher.launch(intent);
                }
            });

            // ✅ Add OnClickListener for the delete button
            btnDeleteAccount.setOnClickListener(v -> {
                showDeleteOptionsDialog();
            });

        } else {
            btnEditProfile.setVisibility(View.GONE);
            btnDeleteAccount.setVisibility(View.GONE); // ✅ Hide delete button
        }
    }

    // ✅ New method to show the first dialog with delete options
    private void showDeleteOptionsDialog() {
        final String[] options = {"Transfer my patients", "Delete my patients and account"};
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Transfer my patients
                        showTransferCredentialsDialog();
                    } else { // Delete my patients
                        // Ask for one final confirmation before deleting everything
                        new AlertDialog.Builder(this)
                                .setTitle("Confirm Deletion")
                                .setMessage("Are you sure? This will permanently delete your account and all associated patient profiles.")
                                .setPositiveButton("Yes, Delete All", (d, w) -> performDeleteRequest("delete", null, null))
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ✅ New method to show the dialog for entering new caregiver credentials
    private void showTransferCredentialsDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogue_caregiver_login, null); // Re-use your existing layout
        final EditText etEmail = dialogView.findViewById(R.id.caregiverEmail);
        final EditText etPassword = dialogView.findViewById(R.id.caregiverPassword);

        new AlertDialog.Builder(this)
                .setTitle("Enter New Caregiver's Credentials")
                .setView(dialogView)
                .setPositiveButton("Confirm Transfer", (dialog, which) -> {
                    String email = etEmail.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show();
                    } else {
                        performDeleteRequest("transfer", email, password);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ✅ New method to handle the actual network request
    private void performDeleteRequest(final String actionType, final String newEmail, final String newPassword) {
        String url = BASE_URL + "delete_caregiver_account";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if ("success".equals(jsonResponse.getString("status"))) {
                            Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_LONG).show();
                            // Clear session and navigate to login screen
                            getSharedPreferences("userSession", MODE_PRIVATE).edit().clear().apply();
                            Intent intent = new Intent(ViewCaregiver.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid response from server.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network Error: " + error.getMessage(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("careGiverID", String.valueOf(currentCaregiver.getCareGiverID()));
                params.put("actionType", actionType);
                if (actionType.equals("transfer")) {
                    params.put("newCaregiverEmail", newEmail);
                    params.put("newCaregiverPassword", newPassword);
                }
                return params;
            }
        };
        queue.add(request);
    }

    // ... (fetchCaregiver, updateUI, setupTiles, getAgeFromIso methods remain the same) ...
    private void fetchCaregiver(int careGiverId) {
        String url = BASE_URL + "view_caregiver&careGiverId=" + careGiverId;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest req = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if ("success".equals(json.optString("status"))) {
                    JSONArray arr = json.optJSONArray("caregivers");
                    if (arr != null && arr.length() > 0) {
                        JSONObject obj = arr.getJSONObject(0);
                        currentCaregiver = Care_giver.fromJson(obj); // Assuming Caregiver class
                        updateUI(currentCaregiver);
                    } else {
                        Toast.makeText(this, "Caregiver not found", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, json.optString("message", "Unknown error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show());
        queue.add(req);
    }

    // In your ViewCaregiver.java file...

// (Assume other methods like onCreate, bindViews, etc., are present)

    private void updateUI(Care_giver caregiver) {
        // Update other text views as you normally would...
        tvName.setText(caregiver.getFirstName() + " " + caregiver.getLastName());
        ((TextView) emailTile.findViewById(R.id.tvValue)).setText(caregiver.getEmail());
        // ... and so on for contact number, work number, etc.

        // --- THIS IS THE FIX ---
        // The 'getUserImage()' method now returns a Base64 string.
        String base64Image = caregiver.getUserImage();

        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                // Decode the Base64 string into a byte array
                byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);

                // Load the byte array directly with Glide
                Glide.with(this)
                        .load(decodedString)
                        .placeholder(R.drawable.default_avatar) // Default image while loading
                        .error(R.drawable.default_avatar)       // Default image if loading fails
                        .into(ivUser); // Your ImageView for the caregiver's profile
            } catch (IllegalArgumentException e) {
                // If the Base64 string is corrupted, show the default avatar
                ivUser.setImageResource(R.drawable.default_avatar);
            }
        } else {
            // If there is no image string, show the default avatar
            ivUser.setImageResource(R.drawable.default_avatar);
        }
        // --- END FIX ---
    }

    // setupTiles() and getAgeFromIso() remain the same
    private void setupTiles() {
        ((TextView) emailTile.findViewById(R.id.tvLabel)).setText("EMAIL:");
        ((TextView) contactTile.findViewById(R.id.tvLabel)).setText("CONTACT NO.:");
        ((TextView) workNumberTile.findViewById(R.id.tvLabel)).setText("WORK NO.:");
        ((TextView) employerTypeTile.findViewById(R.id.tvLabel)).setText("EMPLOYER:");
        ((TextView) pillAge.findViewById(R.id.tvLabel)).setText("Age:");
        ((TextView) pillPassword.findViewById(R.id.tvLabel)).setText("PASSWORD:");
        EditText pwValue = pillPassword.findViewById(R.id.tvValue);
        ImageButton pwToggle = pillPassword.findViewById(R.id.btnToggle);
        pwValue.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
        pwToggle.setImageResource(R.drawable.eye_24);
        pwToggle.setVisibility(View.VISIBLE);
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

    public static int getAgeFromIso(String dobIso) {
        if (dobIso == null || dobIso.isEmpty()) return 0;
        try {
            return Period.between(LocalDate.parse(dobIso), LocalDate.now()).getYears();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}