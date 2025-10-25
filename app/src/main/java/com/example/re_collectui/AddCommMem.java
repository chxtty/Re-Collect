package com.example.re_collectui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class AddCommMem extends AppCompatActivity {

    private EditText etFirstName, etLastName, etType, etDescription, etCuteMessage;
    private Button btnSaveMember;

    private ImageButton backbtn;
    private int patientID = -1;

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
        backbtn=findViewById(R.id.imgBack);
        // Retrieve the patientID passed from the previous activity
        patientID = getIntent().getIntExtra("patientID", -1);
        if (patientID == -1) {
            Toast.makeText(this, "Error: Patient link not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        backbtn.setOnClickListener(view -> finish());

        btnSaveMember.setOnClickListener(view -> saveMember());
    }

    private void saveMember() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String type = etType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String cuteMessage = etCuteMessage.getText().toString().trim();

        if (firstName.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "First Name and Relationship are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://100.104.224.68/android/api.php?action=add_community_member";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Member added successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Close this activity and return to the list
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
                // Image handling would be added here in a more complex request
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}