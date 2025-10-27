package com.example.re_collectui;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewCommunity extends AppCompatActivity {

    private ImageButton fabAddMember;

    CustomToast toast;
    private Uri selectedImageUri = null;
    private ImageButton btnRequestComm;
    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private ArrayList<Community_Member> memberList = new ArrayList<>();
    private int patientID = -1;
    private int careGiverID = -1;
    private static final int IMAGE_REQUEST = 101;


    // In ViewCommunity.java

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_community);

        // Remove or keep this EdgeToEdge code as you see fit
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fabAddMember = findViewById(R.id.btnAddMember);
        btnRequestComm = findViewById(R.id.btnRequestComm);

        // 2. Determine the user's role from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        // If patientID in session is -1, it's a Caregiver. Otherwise, it's a Patient.
        boolean isCaregiver = sharedPref.getInt("patientID", -1) == -1;

        // 3. Set button visibility based on the user's role
        if (isCaregiver) {
            // CARECAREGIVER: Show the "Add Member" button, hide the "Request" button
            fabAddMember.setVisibility(View.VISIBLE);
            btnRequestComm.setVisibility(View.GONE);
        } else {
            // PATIENT: Show the "Request" button, hide the "Add Member" button
            fabAddMember.setVisibility(View.GONE);
            btnRequestComm.setVisibility(View.VISIBLE);
        }

        patientID = getIntent().getIntExtra("patientID", -1);
        careGiverID = sharedPref.getInt("careGiverID", -1);


        if (patientID == -1) {
            Toast.makeText(this, "Error: Could not identify the patient's community.", Toast.LENGTH_LONG).show();
            finish(); // Exit if no valid ID was passed to this screen
            return;
        }

        // 2. NOW, set up your button's click listener.
        // It will now have the correct patientID to pass along.
       fabAddMember.setOnClickListener(view -> {
            Intent intent = new Intent(ViewCommunity.this, AddCommMem.class);
            intent.putExtra("patientID", patientID);
            startActivity(intent);
        });


        ImageView imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(e -> onBackPressed());


        recyclerView = findViewById(R.id.rvCommunity);

        // ✅ THIS IS THE FIX
        // 1. Determine the user's role by checking the session.
        boolean isCaregiverViewing = sharedPref.getInt("patientID", -1) == -1;

         // 2. You MUST call the constructor with THREE arguments, passing the role.
        // The old two-argument call `new CommunityAdapter(this, memberList)` is wrong.
        adapter = new CommunityAdapter(this, memberList, isCaregiverViewing);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        SearchView svCommunity = findViewById(R.id.svCommunity);
        svCommunity.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void fetchCommunityMembers() {
        String urlpath= GlobalVars.apiPath;
        String url = urlpath + "view_community_by_patient&patientId=" + patientID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray membersJson = response.getJSONArray("members");
                            ArrayList<Community_Member> newMembers = new ArrayList<>();
                            for (int i = 0; i < membersJson.length(); i++) {
                                JSONObject memberObject = membersJson.getJSONObject(i);
                                newMembers.add(Community_Member.fromJson(memberObject));
                            }
                            adapter.replaceData(newMembers);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error: " + error.getMessage());
                    Toast.makeText(this, "Could not fetch community list", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (patientID != -1) {
            fetchCommunityMembers();
        }
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