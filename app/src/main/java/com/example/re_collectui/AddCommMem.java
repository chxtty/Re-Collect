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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
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

public class AddCommMem extends AppCompatActivity {

    private EditText etFirstName, etLastName, etType, etDescription, etCuteMessage;
    private Button btnSaveMember;
    private ImageButton backbtn;
    private FloatingActionButton btnAddPhoto;
    private ImageView ivProfileImage;

    private int patientID = -1;
    private String userImageBase64 = null;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comm_mem);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etType = findViewById(R.id.etType);
        etDescription = findViewById(R.id.etDescription);
        etCuteMessage = findViewById(R.id.etCuteMessage);
        btnSaveMember = findViewById(R.id.btnSaveMember);
        backbtn = findViewById(R.id.imgBack);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnAddPhoto = findViewById(R.id.fabAddPhoto);

        patientID = getIntent().getIntExtra("patientID", -1);
        if (patientID == -1) {
            Toast.makeText(this, "Error: Patient link not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        backbtn.setOnClickListener(view -> finish());
        btnSaveMember.setOnClickListener(view -> saveMember());
        btnAddPhoto.setOnClickListener(v -> openGallery());
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

    private void saveMember() {
        final String firstName = etFirstName.getText().toString().trim();
        final String lastName = etLastName.getText().toString().trim();
        final String type = etType.getText().toString().trim();
        final String description = etDescription.getText().toString().trim();
        final String cuteMessage = etCuteMessage.getText().toString().trim();

        if (firstName.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "First Name and Relationship are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiUrl =  GlobalVars.apiPath;

        String url = apiUrl + "add_community_member";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Member added successfully!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Response parsing error.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("patientID", String.valueOf(patientID));
                params.put("commFirstName", firstName);
                params.put("commLastName", lastName);
                params.put("commType", type);
                params.put("commDescription", description);
                params.put("commCuteMessage", cuteMessage);

                // --- THIS IS THE FIX ---
                // If an image was selected, add it to the request.
                if (userImageBase64 != null && !userImageBase64.isEmpty()) {
                    params.put("commImage", userImageBase64);
                }
                // --- END FIX ---

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}