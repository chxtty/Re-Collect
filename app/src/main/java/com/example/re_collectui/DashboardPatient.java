package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class DashboardPatient extends AppCompatActivity {

    Button btnLogout;
    ConstraintLayout eventsOption, diaryOption, communityOption, caregiverOption, activityOption, myselfOption;
    int patientID, careGiverID;
    private Uri selectedImageUri = null;
    CustomToast toast;
    private static final int IMAGE_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboardpatient);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        patientID = sharedPref.getInt("patientID", -1);
        careGiverID = sharedPref.getInt("caregiverID", -1);
        String name = sharedPref.getString("name", "");
        toast = new CustomToast(this);

        TextView txtWelcome = findViewById(R.id.txtWelcome);
        txtWelcome.setText("Welcome, " + name + " :)");

        btnLogout = findViewById(R.id.btnLogout);
        eventsOption = findViewById(R.id.eventsOption);
        diaryOption = findViewById(R.id.dairyOption);
        activityOption = findViewById(R.id.activityOption);
        myselfOption = findViewById(R.id.myselfOption);
        communityOption = findViewById(R.id.commOption);
        caregiverOption = findViewById(R.id.CaregiverOption);

        btnLogout.setOnClickListener(v ->{
            sharedPref.edit().clear().apply();
            Intent intent = new Intent(DashboardPatient.this, goodbye_splash.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        eventsOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, EventsView.class);
            startActivity(intent);
        });

        diaryOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, ViewDiaryEntries.class);
            startActivity(intent);
        });

        myselfOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, ViewPatient.class);
            intent.putExtra("patientID", patientID);
            startActivity(intent);
        });

        activityOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, ViewActivities.class);
            startActivity(intent);
        });

        communityOption.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardPatient.this, ViewCommunity.class);
            intent.putExtra("patientID", patientID);
            startActivity(intent);
        });

        caregiverOption.setOnClickListener(v -> {
            if (careGiverID != -1) {
                Intent intent = new Intent(DashboardPatient.this, ViewCaregiver.class);
                intent.putExtra("careGiverID", careGiverID);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No caregiver is assigned to this profile.", Toast.LENGTH_SHORT).show();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                toast.GetInfoToast( "Please log out to exit").show();
            }
        });
    }

    public void showRequestDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View currView = getLayoutInflater().inflate(R.layout.request_community_dialog,null);

        EditText edtFName = currView.findViewById(R.id.edtFNameCommReq);
        EditText edtLName = currView.findViewById(R.id.edtLNameCommReq);
        Spinner spnType = currView.findViewById(R.id.spnType);
        EditText edtDesc = currView.findViewById(R.id.edtDescCommReq);
        EditText edtCute = currView.findViewById(R.id.edtCuteMessageCommReq);
        Button btnImage = currView.findViewById(R.id.btnAddImage);
        Button btnSave = currView.findViewById(R.id.btnSendCommReq);
        Button btnCancel = currView.findViewById(R.id.btnCancelCommReq);

        builder.setView(currView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

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

        spnType.setSelection(0);

        btnImage.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String firstName = edtFName.getText().toString().trim();
            String lastName = edtLName.getText().toString().trim();
            String commType = spnType.getSelectedItemPosition() == 0 ? "" : spnType.getSelectedItem().toString();
            String desc = edtDesc.getText().toString().trim();
            String cuteMsg = edtCute.getText().toString().trim();
            String imgBase64 = "";
            if (selectedImageUri != null) {
                imgBase64 = encodeImageToBase64(selectedImageUri);
            }

            if (commType.isEmpty()) {
                toast.GetErrorToast("Please select a valid type").show();
                return;
            }

            if (firstName.isEmpty()) {
                toast.GetErrorToast("Please at least fill First Name").show();
                return;
            }

            submitCommRequest(patientID, careGiverID, commType, firstName, lastName, desc, cuteMsg, imgBase64);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void submitCommRequest(int patientID, int careGiverID, String commType,
                                   String firstName, String lastName, String desc, String cuteMsg, String imgBase64) {

        String url = GlobalVars.apiPath + "create_community_request";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        String status = res.getString("status");

                        if (status.equals("success")) {
                            toast.GetInfoToast( "Community Member request submitted!").show();
                        } else {
                            toast.GetErrorToast(res.getString("message")).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("Parsing error", e.getMessage());
                    }
                },
                error -> toast.GetErrorToast("Network error: " + error.getMessage()).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("patientID", String.valueOf(patientID));
                params.put("careGiverID", String.valueOf(careGiverID));
                params.put("commType", commType);
                params.put("commFirstName", firstName);
                params.put("commLastName", lastName);
                params.put("commDescription", desc);
                params.put("commCuteMessage", cuteMsg);
                params.put("commImage", imgBase64);
                return params;
            }
        };
        queue.add(request);

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            selectedImageUri = data.getData();
            toast.GetInfoToast("Image added").show();
        }
    }

}