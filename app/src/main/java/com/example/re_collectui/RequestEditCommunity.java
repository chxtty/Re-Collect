package com.example.re_collectui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequestEditCommunity extends AppCompatActivity {

    TextView txtTitle, txtSubTitle;
    EditText edtName, edtSurname, edtDescription, edtCuteMsg;
    Spinner spnType;
    Button btnImage;
    ImageView imgPreview;
    private static final int IMAGE_REQUEST = 101;
    private Uri selectedImageUri = null;
    RequestItem currentItem;
    CustomToast toast;

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
        toast = new CustomToast(this);

        btnImage = findViewById(R.id.btnImageCR);
        txtTitle = findViewById(R.id.txtTitleReq);
        txtSubTitle = findViewById(R.id.txtSubTitleReq);
        imgPreview = findViewById(R.id.imgPreview);

        Intent intent = getIntent();

        if (intent!=null){
            Bundle extras = intent.getExtras();
            if (extras != null){
                currentItem = (RequestItem) extras.get("requestItem");
                edtName.setText(currentItem.getCommFirstName());
                edtSurname.setText(currentItem.getCommLastName());
                edtDescription.setText(currentItem.getCommDescription());
                edtCuteMsg.setText(currentItem.getCommCuteMessage());

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        getResources().getStringArray(R.array.type_options)
                ) {
                    @Override
                    public boolean isEnabled(int position) {
                        return position != 0;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;
                        tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                        return view;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnType.setAdapter(adapter);

                int position = adapter.getPosition(currentItem.getCommType());
                spnType.setSelection(position > 0 ? position : 1);


                if (currentItem.getCommImage() != null && !currentItem.getCommImage().isEmpty()) {
                    setImageFromBase64(currentItem.getCommImage());
                    selectedImageUri = null;
                }

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
        showCustomDialog(
                "Are you sure you want to delete this request?",
                "Yes",
                "Cancel",
                this::deleteRequest,
                "DeleteRequestDialog"
        );
    }

    private void deleteRequest() {
        String url = GlobalVars.apiPath + "delete_Commrequest";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            toast.GetDeleteToast("Community request deleted").show();
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
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void onSaveRequest(View view) {
        String url = GlobalVars.apiPath + "save_Commrequest";

        String commType = spnType.getSelectedItemPosition() == 0 ? "" : spnType.getSelectedItem().toString().trim();
        String firstName = edtName.getText().toString().trim();
        String lastName = edtSurname.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String cuteMsg = edtCuteMsg.getText().toString().trim();

        if (commType.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            toast.GetErrorToast("Please fill in all required fields").show();
            return;
        }

        String imageBase64 = selectedImageUri != null ? encodeImageToBase64(selectedImageUri)
                : (currentItem != null ? currentItem.getCommImage() : null);

        String finalImageBase64 = imageBase64;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            toast.GetInfoToast("Community request saved").show();
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
                params.put("commType", commType);
                params.put("commFirstName", firstName);
                params.put("commLastName", lastName);
                params.put("commDescription", desc);
                params.put("commCuteMessage", cuteMsg);

                if (finalImageBase64 != null && !finalImageBase64.isEmpty()) {
                    params.put("commImage", finalImageBase64);
                }

                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    public void onDeclineRequest(View view) {
        showCustomDialog(
                "Are you sure you want to decline this request?",
                "Yes",
                "Cancel",
                this::declineRequest,
                "DeclineRequestDialog"
        );
    }

    private void declineRequest() {
        String url = GlobalVars.apiPath + "decline_Commrequest";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            toast.GetInfoToast("Community request declined").show();
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
        dialog.show(getSupportFragmentManager(), "AcceptRequestDialog");

        getSupportFragmentManager().executePendingTransactions();
        if (dialog.getDialog() != null) {
            Button btnPositive = dialog.getDialog().findViewById(R.id.btnPositive);
            if (btnPositive != null) {
                btnPositive.setBackgroundResource(R.drawable.dashboard_icon_caregiver);
            }
        }
    }

    private void acceptRequest(View view) {
        String url = GlobalVars.apiPath + "accept_Commrequest";

        String commType = spnType.getSelectedItemPosition() == 0 ? "" : spnType.getSelectedItem().toString().trim();
        String firstName = edtName.getText().toString().trim();
        String lastName = edtSurname.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String cuteMsg = edtCuteMsg.getText().toString().trim();

        if (commType.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || desc.isEmpty() || cuteMsg.isEmpty()) {
            toast.GetErrorToast("Please fill in all fields").show();
            return;
        }

        String imageBase64 = selectedImageUri != null ? encodeImageToBase64(selectedImageUri)
                : (currentItem != null ? currentItem.getCommImage() : null);

        String finalImageBase64 = imageBase64;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            toast.GetInfoToast("Community request accepted").show();
                            finish();
                        } else {
                            toast.GetErrorToast("Error: " + obj.getString("message")).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("Server Response", response);
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
                params.put("commType", commType);
                params.put("commFirstName", firstName);
                params.put("commLastName", lastName);
                params.put("commDescription", desc);
                params.put("commCuteMessage", cuteMsg);

                if (finalImageBase64 != null && !finalImageBase64.isEmpty()) {
                    params.put("commImage", finalImageBase64);
                }

                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void showCustomDialog(String message, String positiveText, String negativeText, Runnable onPositiveAction, String tag) {
        CustomPopupDialogFragment dialog = CustomPopupDialogFragment.newInstance(
                message,
                positiveText,
                negativeText
        );
        dialog.setOnPositiveClickListener(() -> {
            if (onPositiveAction != null) {
                onPositiveAction.run();
            }
        });
        dialog.show(getSupportFragmentManager(), tag);
    }

    private void setImageFromBase64(String base64String) {
        if (base64String != null && !base64String.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                imgPreview.setVisibility(View.VISIBLE);
                imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);

                int sizePx = (int) (50 * getResources().getDisplayMetrics().density + 0.5f);
                ViewGroup.LayoutParams lp = imgPreview.getLayoutParams();
                lp.width = sizePx;
                lp.height = sizePx;
                imgPreview.setLayoutParams(lp);

                imgPreview.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            imgPreview.setVisibility(View.VISIBLE);
            imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);

            int sizePx = (int) (50 * getResources().getDisplayMetrics().density + 0.5f);
            ViewGroup.LayoutParams lp = imgPreview.getLayoutParams();
            lp.width = sizePx;
            lp.height = sizePx;
            imgPreview.setLayoutParams(lp);

            imgPreview.setImageURI(selectedImageUri);
            toast.GetInfoToast("Image selected").show();
        }
    }
}