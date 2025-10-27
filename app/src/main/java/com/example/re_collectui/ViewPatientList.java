package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewPatientList extends AppCompatActivity implements PatientAdapter.OnPatientDeleteListener,ConfirmDeletePatientDialog.DeletionAuthListener{

    private RecyclerView recyclerView;
    private PatientAdapter adapter;
    private ArrayList<Patient> patientList = new ArrayList<>();
    private RequestQueue requestQueue;

    private int careGiverID = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_patient_list);
        requestQueue = Volley.newRequestQueue(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(e -> {
            onBackPressed();
        });

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        careGiverID = sharedPref.getInt("careGiverID", -1);
        if (careGiverID == -1) {
            Toast.makeText(this, "Caregiver ID not found in session", Toast.LENGTH_SHORT).show();
        }

        recyclerView = findViewById(R.id.rvPatients);
        adapter = new PatientAdapter(this, patientList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SearchView svPatients = findViewById(R.id.svPatients);
        svPatients.setIconifiedByDefault(false);
        svPatients.clearFocus();
        svPatients.setQueryHint("Search by name, ID, diagnosis...");

        svPatients.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void fetchPatientList() {
        patientList.clear();
        String urlpath= GlobalVars.apiPath;
        String url = urlpath + "view_patients_by_caregiver&careGiverId=" + careGiverID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("ViewPatientList", "Response: " + response.toString());
                        if (response.getString("status").equals("success")) {
                            JSONArray patients = response.getJSONArray("patients");

                            for (int i = 0; i < patients.length(); i++) {
                                JSONObject patientJson = patients.getJSONObject(i);

                                int patientID = patientJson.getInt("patientID");
                                int cgID = patientJson.getInt("careGiverID");
                                String firstName = patientJson.getString("firstName");
                                String lastName = patientJson.getString("lastName");
                                String dob = patientJson.getString("DoB");
                                String contactNumber = patientJson.getString("contactNumber");
                                String diagnosis = patientJson.getString("diagnosis");
                                String emergencyContact = patientJson.getString("emergencyContact");
                                String email = patientJson.getString("email");
                                String userimage = patientJson.getString("userImage");
                                String password = patientJson.getString("patientPassword");

                                patientList.add(new Patient(patientID, cgID, firstName, lastName, dob, contactNumber, diagnosis, emergencyContact, userimage, email,password));
                            }

                            adapter.replaceData(new ArrayList<>(patientList));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error: " + error.getMessage());
                    Toast.makeText(this, "Could not fetch patient list", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (careGiverID != -1) {
            fetchPatientList();
        }
    }

    @Override
    public void onDeleteClick(Patient patient, int position) {
        ConfirmDeletePatientDialog dialog = ConfirmDeletePatientDialog.newInstance(
                patient.getPatientID(),
                position
        );
        dialog.show(getSupportFragmentManager(), "ConfirmDelete");
    }

    private void deletePatientOnServer(final int patientId, final int position) {
        String urlpath= GlobalVars.apiPath;
        String url = urlpath + "delete_patient";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");

                        if ("success".equals(status)) {
                            adapter.removeItem(position);
                            Toast.makeText(ViewPatientList.this, "Patient deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = jsonObject.getString("message");
                            Toast.makeText(ViewPatientList.this, "Deletion Error: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("VolleyError", "Delete JSON Error: " + response, e);
                        Toast.makeText(ViewPatientList.this, "Deletion failed (server response invalid).", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(ViewPatientList.this, "Volley Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("patientID", String.valueOf(patientId));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    @Override
    public void onAuthenticationSuccess(int patientId, String email, String password, int position) {
        verifyCaregiverAndThenDelete(patientId, email, password, position);
    }

    private void verifyCaregiverAndThenDelete(int patientId, String email, String password, int position) {
        String urlpath = GlobalVars.apiPath;
        String verifyUrl = urlpath + "login_caregiver";

        final int loggedInCareGiverID = this.careGiverID;

        StringRequest verifyRequest = new StringRequest(Request.Method.POST, verifyUrl,
                verifyResponse -> {
                    try {
                        JSONObject jsonObject = new JSONObject(verifyResponse);
                        String status = jsonObject.getString("status");

                        if ("success".equals(status)) {
                            JSONObject user = jsonObject.getJSONObject("user");
                            int authenticatedCareGiverID = user.getInt("careGiverID");

                            if (authenticatedCareGiverID == loggedInCareGiverID) {
                                deletePatientOnServer(patientId, position);
                            } else {
                                Toast.makeText(ViewPatientList.this, "Security Alert: Credentials belong to a different user.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(ViewPatientList.this, "Authentication Failed: Invalid email or password.", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(ViewPatientList.this, "Verification JSON error.", Toast.LENGTH_LONG).show();
                        Log.e("VolleyError", "Verification JSON Error: " + verifyResponse, e);
                    }
                },
                verifyError -> {
                    Toast.makeText(ViewPatientList.this, "Verification Network Error: " + verifyError.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };
        requestQueue.add(verifyRequest);
    }
}