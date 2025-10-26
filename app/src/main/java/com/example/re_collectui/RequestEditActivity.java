package com.example.re_collectui;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
        ImageView imgPreview = findViewById(R.id.imgPreview);


        Intent intent = getIntent();

        if (intent!=null){
        Bundle extras = intent.getExtras();
        if (extras != null){
            currentItem = (RequestItem) extras.get("requestItem");
            edtType.setText(currentItem.getActType());
            edtDescription.setText(currentItem.getActDescription());
            txtTitle.setText("Request #" + currentItem.getId());
            txtSubTitle.setText("By " + currentItem.getAuthor());

            if (currentItem.getActIcon() != null && !currentItem.getActIcon().isEmpty()) {
                setImageFromBase64(currentItem.getActIcon());
            }
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

            ImageView imgPreviewAct = findViewById(R.id.imgPreviewAct);
            imgPreviewAct.setVisibility(View.VISIBLE);
            imgPreviewAct.setScaleType(ImageView.ScaleType.CENTER_CROP);

            int sizePx = (int) (50 * getResources().getDisplayMetrics().density + 0.5f);
            ViewGroup.LayoutParams lp = imgPreviewAct.getLayoutParams();
            lp.width = sizePx;
            lp.height = sizePx;
            imgPreviewAct.setLayoutParams(lp);

            imgPreviewAct.setImageURI(selectedImageUri);
            toast.GetInfoToast("Image selected").show();
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
                            toast.GetInfoToast("Request declined").show();
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

    private void setImageFromBase64(String base64String) {
        if (base64String != null && !base64String.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                ImageView imgPreviewAct = findViewById(R.id.imgPreviewAct);
                imgPreviewAct.setVisibility(View.VISIBLE);
                imgPreviewAct.setScaleType(ImageView.ScaleType.CENTER_CROP);

                int sizePx = (int) (50 * getResources().getDisplayMetrics().density + 0.5f);
                ViewGroup.LayoutParams lp = imgPreviewAct.getLayoutParams();
                lp.width = sizePx;
                lp.height = sizePx;
                imgPreviewAct.setLayoutParams(lp);

                imgPreviewAct.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onSaveRequest(View view) {
        String url = GlobalVars.apiPath + "save_Actrequest";

        String actType = edtType.getText().toString().trim();
        String actDescription = edtDescription.getText().toString().trim();
        String imagePath = selectedImageUri != null
                ? encodeImageToBase64(selectedImageUri)
                : (currentItem != null ? currentItem.getActIcon() : "");

        if (actType.isEmpty() || actDescription.isEmpty()) {
            toast.GetErrorToast("Please fill in all fields").show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            toast.GetInfoToast("Request saved").show();
                        } else {
                            toast.GetErrorToast("Error: " + obj.getString("message")).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast.GetErrorToast("Unexpected error occurred").show();
                    }
                },
                error -> {
                    Log.e("Network error", Objects.requireNonNull(error.getMessage()));
                    toast.GetErrorToast("Network error occurred").show();
                }
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
                                toast.GetDeleteToast( "Request deleted").show();
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

        getSupportFragmentManager().executePendingTransactions();
        if (dialog.getDialog() != null) {
            Button btnPositive = dialog.getDialog().findViewById(R.id.btnPositive);
            if (btnPositive != null) {
                btnPositive.setBackgroundResource(R.drawable.dashboard_icon_caregiver);
            }
        }
    }

    private void acceptRequest(View view) {
        String url = GlobalVars.apiPath + "accept_Actrequest";

        String actType = edtType.getText().toString().trim();
        String actDescription = edtDescription.getText().toString().trim();
        String imagePath = selectedImageUri != null
                ? encodeImageToBase64(selectedImageUri)
                : (currentItem != null ? currentItem.getActIcon() : "");

        if (actType.isEmpty() || actDescription.isEmpty() || imagePath.isEmpty()) {
            toast.GetErrorToast("Please fill in all fields").show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            toast.GetInfoToast("Activity accepted").show();
                            finish();
                        } else {
                            toast.GetErrorToast("Error: " + obj.getString("message")).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast.GetErrorToast("Unexpected error occurred").show();
                    }
                },
                error -> {
                    Log.e("Network error", Objects.requireNonNull(error.getMessage()));
                    toast.GetErrorToast("Network error occurred").show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("requestID", String.valueOf(currentItem.getId()));
                params.put("patientID", String.valueOf(currentItem.getPatientID()));
                params.put("actType", actType);
                params.put("actDescription", actDescription);
                params.put("imagePath", imagePath);
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