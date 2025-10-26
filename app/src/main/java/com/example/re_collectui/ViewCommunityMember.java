package com.example.re_collectui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

public class ViewCommunityMember extends AppCompatActivity {

    private static final String BASE_URL = "http://100.104.224.68/android/api.php";

    private TextView quote, details, name, relationship;
    private Button editButton, deleteButton;
    private ImageButton backButton;
    private ImageView profileImage;

    private Community_Member currentMember;
    private int memberId;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "Member details refreshed.", Toast.LENGTH_SHORT).show();
                    if (memberId != -1) {
                        fetchCommunityMember(memberId);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_community_member);

        bindViews();
        backButton.setOnClickListener(v -> onBackPressed());

        memberId = getIntent().getIntExtra("commID", -1);
        if (memberId != -1) {
            fetchCommunityMember(memberId);
        } else {
            Toast.makeText(this, "Error: Member ID not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindViews() {
        quote = findViewById(R.id.quote_text);
        details = findViewById(R.id.details_text);
        name = findViewById(R.id.name_text);
        relationship = findViewById(R.id.relationship_text);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);
        backButton = findViewById(R.id.back_button);
        profileImage = findViewById(R.id.profile_image);
    }

    private void fetchCommunityMember(int commId) {
        String url = BASE_URL + "?action=view_community_member&commId=" + commId;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if ("success".equals(json.optString("status"))) {
                            JSONArray arr = json.optJSONArray("members");
                            if (arr != null && arr.length() > 0) {
                                JSONObject obj = arr.getJSONObject(0);
                                currentMember = Community_Member.fromJson(obj);
                                updateUI(currentMember);
                                setupUserPermissions();
                            } else {
                                Toast.makeText(this, "Community member not found", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "Error: " + json.optString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show()
        );
        queue.add(req);
    }

    private void setupUserPermissions() {
        boolean isCaregiver = getIntent().getBooleanExtra("isCaregiver", false);
        if (isCaregiver) {
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);

            editButton.setOnClickListener(v -> {
                if (currentMember != null) {
                    Intent intent = new Intent(ViewCommunityMember.this, EditCommMem.class);
                    intent.putExtra("MEMBER_DATA", currentMember);
                    editLauncher.launch(intent);
                } else {
                    Toast.makeText(this, "Cannot edit. Member data is missing.", Toast.LENGTH_SHORT).show();
                }
            });

            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        } else {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Member")
                .setMessage("Are you sure you want to delete this community member? This action cannot be undone.")
                .setPositiveButton("Yes, Delete", (dialog, which) -> deleteCommunityMember())
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteCommunityMember() {
        String url = BASE_URL + "?action=delete_community_member";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest deleteRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if ("success".equals(res.getString("status"))) {
                            Toast.makeText(this, "Member deleted successfully!", Toast.LENGTH_SHORT).show();
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
                params.put("commID", String.valueOf(memberId));
                return params;
            }
        };
        queue.add(deleteRequest);
    }

    private void updateUI(Community_Member member) {
        name.setText(member.getCommFirstName() + " " + member.getCommLastName());
        quote.setText(member.getCommCuteMessage());
        details.setText(member.getCommDescription());
        relationship.setText(member.getCommType());

        // --- THIS IS THE CORRECTED CODE ---
        String base64Image = member.getCommImage();

        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                Glide.with(this)
                        .load(decodedString)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(profileImage);
            } catch (IllegalArgumentException e) {
                profileImage.setImageResource(R.drawable.default_avatar);
            }
        } else {
            profileImage.setImageResource(R.drawable.default_avatar);
        }
    }
}