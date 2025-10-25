package com.example.re_collectui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog; // ✅ Import AlertDialog
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap; // ✅ Import for Volley POST params
import java.util.Map;     // ✅ Import for Volley POST params

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
        Toast.makeText(this, "4. ViewMember Received: Role isCaregiver = " + isCaregiver, Toast.LENGTH_LONG).show();
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

            // ✅ --- START OF DELETE LOGIC ---
            // Replace the old Toast with a call to show the confirmation dialog
            deleteButton.setOnClickListener(v -> {
                showDeleteConfirmationDialog();
            });
            // ✅ --- END OF DELETE LOGIC ---

        } else {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
    }

    // ✅ New method to show the confirmation dialog
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Member")
                .setMessage("Are you sure you want to delete this community member? This action cannot be undone.")
                .setPositiveButton("Yes, Delete", (dialog, which) -> {
                    // If the user clicks "Yes", call the method to perform the deletion
                    deleteCommunityMember();
                })
                .setNegativeButton("No", null) // Do nothing if "No" is clicked
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // ✅ New method to handle the API call for deletion
    private void deleteCommunityMember() {
        String url = BASE_URL + "?action=delete_community_member";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest deleteRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if ("success".equals(res.getString("status"))) {
                            Toast.makeText(this, "Member deleted successfully!", Toast.LENGTH_SHORT).show();
                            // Close this activity and go back to the community list
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
                // Pass the ID of the member to be deleted
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

        String imagePath = member.getCommImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            String fullImageUrl = "http://100.104.224.68/android/" + imagePath;
            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.default_avatar);
        }
    }
}