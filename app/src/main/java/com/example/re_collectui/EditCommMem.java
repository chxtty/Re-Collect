package com.example.re_collectui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
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

public class EditCommMem extends AppCompatActivity {

    private EditText etFirstName, etLastName, etType, etDescription, etCuteMessage;
    private Button btnSaveChanges, btnAddPhoto;
    private ImageButton btnBack;

    private String userImageBase64 = null;
    private Community_Member memberToEdit;

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
        setContentView(R.layout.activity_edit_comm_mem);

        memberToEdit = (Community_Member) getIntent().getSerializableExtra("MEMBER_DATA");
        if (memberToEdit == null) {
            Toast.makeText(this, "Error: Member data not found.", Toast.LENGTH_LONG).show();
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
        etType = findViewById(R.id.etType);
        etDescription = findViewById(R.id.etDescription);
        etCuteMessage = findViewById(R.id.etCuteMessage);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnAddPhoto = findViewById(R.id.button2);
        btnBack = findViewById(R.id.imgBack);
    }

    private void populateFields() {
        etFirstName.setText(memberToEdit.getCommFirstName());
        etLastName.setText(memberToEdit.getCommLastName());
        etType.setText(memberToEdit.getCommType());
        etDescription.setText(memberToEdit.getCommDescription());
        etCuteMessage.setText(memberToEdit.getCommCuteMessage());
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
        String url = "http://100.104.224.68/android/api.php?action=edit_community_member";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if ("success".equals(res.getString("status"))) {
                            Toast.makeText(this, "Member updated successfully!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
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
                params.put("commID", String.valueOf(memberToEdit.getCommID()));
                params.put("commFirstName", etFirstName.getText().toString().trim());
                params.put("commLastName", etLastName.getText().toString().trim());
                params.put("commType", etType.getText().toString().trim());
                params.put("commDescription", etDescription.getText().toString().trim());
                params.put("commCuteMessage", etCuteMessage.getText().toString().trim());

                if (userImageBase64 != null && !userImageBase64.isEmpty()) {
                    params.put("commImage", userImageBase64);
                }
                return params;
            }
        };
        queue.add(request);
    }
}