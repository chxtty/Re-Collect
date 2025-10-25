package com.example.re_collectui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequestEditActivity extends AppCompatActivity {

    EditText edtType, edtDescription;
    TextView txtTitle, txtSubTitle;
    Button btnImage;
    RequestItem currentItem;
    CustomToast toast;

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

        toast = new CustomToast(this);
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
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ImageView imgPreview = findViewById(R.id.imgPreview);
            imgPreview.setVisibility(View.VISIBLE);
            imgPreview.setImageURI(selectedImageUri);
            toast.GetInfoToast( "Image added");
        }

    }

    public void onDeclineRequest(View view) {
        CustomPopupDialogFragment dialog = CustomPopupDialogFragment.newInstance(
                "Are you sure you want to decline this request?",
                "Yes",
                "Cancel"
        );

        dialog.setOnPositiveClickListener(() -> declineRequest(view));
        dialog.show(getSupportFragmentManager(), "DeclineConfirmationDialog");
    }

    private void declineRequest(View view){
        String url = GlobalVars.apiPath + "decline_Actrequest";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            Toast.makeText(view.getContext(), "Request declined", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            //Toast.makeText(view.getContext(), "Error: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Network error", Objects.requireNonNull(error.getMessage()))
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
            String url = GlobalVars.apiPath +  "save_Actrequest";

            String actType = edtType.getText().toString();
            String actDescription = edtDescription.getText().toString();
        String imagePath = selectedImageUri != null ? encodeImageToBase64(selectedImageUri) : "";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("status").equals("success")) {
                               toast.GetInfoToast("Request saved");
                                //Toast.makeText(this, "Request saved", Toast.LENGTH_SHORT).show();
                                finish(); // Go back to list
                            } else {
                                //Toast.makeText(this, "Error: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.e("Network error", Objects.requireNonNull(error.getMessage()))
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
        CustomPopupDialogFragment dialog = CustomPopupDialogFragment.newInstance(
                "Are you sure you want to delete this request?",
                "Yes",
                "Cancel"
        );

        dialog.setOnPositiveClickListener(() -> {
            deleteRequest(view);
        });

        dialog.show(getSupportFragmentManager(), "DeleteConfirmationDialog");
    }

    private void deleteRequest(View view){
            String url = GlobalVars.apiPath + "delete_Actrequest";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("status").equals("success")) {
                                toast.GetInfoToast( "Request deleted");
                                finish();
                            } else {
                                //Toast.makeText(this, "Error: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.e("Network error", Objects.requireNonNull(error.getMessage()))
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
        CustomPopupDialogFragment dialog = CustomPopupDialogFragment.newInstance(
                "Are you sure you want to accept this request?",
                "Yes",
                "Cancel"
        );

        dialog.setOnPositiveClickListener(() -> acceptRequest(view));
        dialog.show(getSupportFragmentManager(), "AcceptConfirmationDialog");
    }

    private void acceptRequest(View view) {
        String url = GlobalVars.apiPath +  "accept_Actrequest";

        String actType = edtType.getText().toString().trim();
        String actDescription = edtDescription.getText().toString().trim();
        String imagePath = selectedImageUri != null ? encodeImageToBase64(selectedImageUri) : "";

        if (actType.isEmpty() || actDescription.isEmpty() || imagePath.isEmpty()){
            toast.GetErrorToast( "Please enter all fields");
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            toast.GetInfoToast( "Activity accepted");
                        } else {
                            //Toast.makeText(this, "Error: " + obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Network error", Objects.requireNonNull(error.getMessage()))
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

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}