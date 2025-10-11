package com.example.re_collectui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestEditCommunity extends AppCompatActivity {

    TextView txtTitle, txtSubTitle;
    EditText edtName, edtSurname, edtDescription, edtCuteMsg;
    Spinner spnType;
    Button btnImage;
    private static final int IMAGE_REQUEST = 101;
    private Uri selectedImageUri = null;
    RequestItem currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtName = findViewById(R.id.edtFirstNameCR);
        edtDescription = findViewById(R.id.edtDescCR);
        edtSurname = findViewById(R.id.edtLastNameCR);
        edtCuteMsg = findViewById(R.id.edtCuteMsgCR);
        spnType = findViewById(R.id.spnTypeCR);

        btnImage = findViewById(R.id.btnImageCR);
        txtTitle = findViewById(R.id.txtTitleReq);
        txtSubTitle = findViewById(R.id.txtSubTitleReq);

        Intent intent = getIntent();

        if (intent!=null){
            Bundle extras = intent.getExtras();
            if (extras != null){
                currentItem = (RequestItem) extras.get("requestItem");
                edtName.setText(currentItem.getCommFirstName());
                edtSurname.setText(currentItem.getCommLastName());
                edtDescription.setText(currentItem.getCommDescription());
                edtCuteMsg.setText(currentItem.getCommCuteMessage());

                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spnType.getAdapter();
                int position = adapter.getPosition(currentItem.getCommType());
                spnType.setSelection(position);

                txtTitle.setText("Request #" + currentItem.getId());
                txtSubTitle.setText("By " + currentItem.getAuthor());
            }
        }


        btnImage.setOnClickListener(v ->{
            Intent intentImage = new Intent(Intent.ACTION_PICK);
            intentImage.setType("image/*");
            intentImage.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intentImage, "Select Picture"), IMAGE_REQUEST);
        });

    }

    public void onDeleteRequest(View view) {
                new AlertDialog.Builder(this)
                .setTitle("Delete Request")
                .setMessage("Are you sure you want to delete this request?")
                .setPositiveButton("Yes", (dialog, which) -> deleteRequest())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteRequest() {
        String url = "http://10.0.2.2/recollect/api.php?action=delete_Commrequest";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> handleResponse(response, "Request deleted"),
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("requestID", String.valueOf(currentItem.getId()));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    public void onSaveRequest(View view) {
              String url = "http://10.0.2.2/recollect/api.php?action=save_Commrequest";

        String commType = spnType.getSelectedItem().toString().trim();
        String firstName = edtName.getText().toString().trim();
        String lastName = edtSurname.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String cuteMsg = edtCuteMsg.getText().toString().trim();
        String imagePath = selectedImageUri != null ? selectedImageUri.toString() : currentItem.getCommImage();

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> handleResponse(response, "Request saved"),
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("requestID", String.valueOf(currentItem.getId()));
                params.put("commType", commType);
                params.put("commFirstName", firstName);
                params.put("commLastName", lastName);
                params.put("commDescription", desc);
                params.put("commCuteMessage", cuteMsg);
                params.put("commImage", imagePath);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    public void onDeclineRequest(View view) {
                String url = "http://10.0.2.2/recollect/api.php?action=decline_Commrequest";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> handleResponse(response, "Request declined"),
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("requestID", String.valueOf(currentItem.getId()));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    public void onAcceptRequest(View view) {
                String url = "http://10.0.2.2/recollect/api.php?action=accept_Commrequest";

        String commType = spnType.getSelectedItem().toString().trim();
        String firstName = edtName.getText().toString().trim();
        String lastName = edtSurname.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String cuteMsg = edtCuteMsg.getText().toString().trim();
        String imagePath = selectedImageUri != null ? selectedImageUri.toString() : currentItem.getCommImage();

        if (commType.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || desc.isEmpty() || cuteMsg.isEmpty() || imagePath.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> handleResponse(response, "Community request accepted"),
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("requestID", String.valueOf(currentItem.getId()));
                params.put("patientID", String.valueOf(currentItem.getPatientID()));
                params.put("commType", commType);
                params.put("commFirstName", firstName);
                params.put("commLastName", lastName);
                params.put("commDescription", desc);
                params.put("commCuteMessage", cuteMsg);
                params.put("commImage", imagePath);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }


    private void handleResponse(String response, String successMessage) {
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.getString("status").equals("success")) {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}