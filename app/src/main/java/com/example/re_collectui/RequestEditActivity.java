package com.example.re_collectui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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

public class RequestEditActivity extends AppCompatActivity {

    EditText edtType, edtDescription;
    TextView txtTitle, txtSubTitle;
    Button btnImage;
    RequestItem currentItem;

    private static final int IMAGE_REQUEST = 101;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_edit2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtType = findViewById(R.id.edtActType);
        edtDescription = findViewById(R.id.edtDescription);
        txtTitle = findViewById(R.id.txtTitleReq);
        txtSubTitle = findViewById(R.id.txtSubTitleReq);
        btnImage = findViewById(R.id.btnIconPicker);


        Intent intent = getIntent();

        if (intent!=null){
        Bundle extras = intent.getExtras();
        if (extras != null){
            currentItem = (RequestItem) extras.get("requestItem");
            edtType.setText(currentItem.getActType());
            edtDescription.setText(currentItem.getActDescription());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            selectedImageUri = data.getData();
            Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show();
        }

    }

    public void onDeclineRequest(View view) {
            new AlertDialog.Builder(this)
        .setTitle("Decline Request")
        .setMessage("Are you sure you want to decline this request?")
        .setPositiveButton("Yes", (dialog, which) -> {
            declineRequest(view);
        })
        .setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        })
        .show();
    }

    private void declineRequest(View view){
        String url = "http://10.0.2.2/recollect/api.php?action=decline_Actrequest";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            Toast.makeText(view.getContext(), "Request declined", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(view.getContext(), "Error: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(view.getContext(), "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("requestID", String.valueOf(currentItem.getId()));
                return params;
            }
        };

        Volley.newRequestQueue(view.getContext()).add(stringRequest);
    }

    public void onSaveRequest(View view) {
            String url = "http://10.0.2.2/recollect/api.php?action=save_Actrequest";

            String actType = edtType.getText().toString();
            String actDescription = edtDescription.getText().toString();
            String imagePath = selectedImageUri != null ? selectedImageUri.toString() : "";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("status").equals("success")) {
                                Toast.makeText(this, "Request saved", Toast.LENGTH_SHORT).show();
                                finish(); // Go back to list
                            } else {
                                Toast.makeText(this, "Error: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("requestID", String.valueOf(currentItem.getId()));
                    params.put("actType", actType);
                    params.put("actDescription", actDescription);
                    params.put("imagePath", imagePath);
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(stringRequest);
    }

    public void onDeleteRequest(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Request")
                .setMessage("Are you sure you want to delete this request?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteRequest(view);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();

    }

    private void deleteRequest(View view){
            String url = "http://10.0.2.2/recollect/api.php?action=delete_Actrequest";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("status").equals("success")) {
                                Toast.makeText(this, "Request deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Error: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("requestID", String.valueOf(currentItem.getId()));
                    return params;
                }
            };

            Volley.newRequestQueue(this).add(stringRequest);
    }


    public void onAcceptRequest(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Accept Request")
                .setMessage("Are you sure you want to accept this request?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    acceptRequest(view);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void acceptRequest(View view) {
        String url = "http://10.0.2.2/recollect/api.php?action=delete_Actrequest";

        String actType = edtType.getText().toString().trim();
        String actDescription = edtDescription.getText().toString().trim();
        String imagePath = selectedImageUri != null ? selectedImageUri.toString() : ""; 

        if (actType.isEmpty() || actDescription.isEmpty() || imagePath.isEmpty()){
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            Toast.makeText(this, "Activity accepted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("requestID", String.valueOf(currentItem.getId()));
                params.put("patientID", String.valueOf(currentItem.getPatientID()));
                params.put("actType", actType);
                params.put("actDescription", actDescription);
                params.put("actIcon", imagePath);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}