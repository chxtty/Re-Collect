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

// The class has been renamed to ViewPatientList
public class ViewPatientList extends AppCompatActivity implements PatientAdapter.OnPatientDeleteListener{

    // The RecyclerView and Adapter now use the Patient class
    private RecyclerView recyclerView;
    private PatientAdapter adapter;
    private ArrayList<Patient> patientList = new ArrayList<>();
    private RequestQueue requestQueue;

    // Assuming you get the careGiverID from SharedPreferences to fetch their assigned patients
    private int careGiverID = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // **ASSUMPTION**: Your layout file is now named "activity_view_patient_list.xml"
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


        // Fetching the careGiverID from the session
        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        // **ASSUMPTION**: The key for the caregiver's ID in SharedPreferences is "careGiverID"
        careGiverID = sharedPref.getInt("careGiverID", -1);
        if (careGiverID == -1) {
            Toast.makeText(this, "Caregiver ID not found in session", Toast.LENGTH_SHORT).show();
            // You might want to finish the activity or redirect to login here
        }

        // **ASSUMPTION**: The RecyclerView in your XML is now named "rvPatients"
        recyclerView = findViewById(R.id.rvPatients);
        adapter = new PatientAdapter(this, patientList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Setup the SearchView
        // **ASSUMPTION**: The SearchView in your XML is now named "svPatients"
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

        // The "Create New Entry" button and its logic have been removed.
    }

    private void fetchPatientList() {
        patientList.clear();
        // **IMPORTANT**: Replace with your actual API endpoint to fetch patients for a specific caregiver.
        String url = "http://100.104.224.68/android/api.php?action=view_patients_by_caregiver&careGiverId=" + careGiverID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("ViewPatientList", "Response: " + response.toString());
                        if (response.getString("status").equals("success")) {
                            // **ASSUMPTION**: The JSON array key is "patients"
                            JSONArray patients = response.getJSONArray("patients");

                            for (int i = 0; i < patients.length(); i++) {
                                JSONObject patientJson = patients.getJSONObject(i);

                                // Parsing all the patient details from the JSON object
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
                                // The password is omitted for security

                                // **ASSUMPTION**: You have a Patient model class with a constructor that accepts these fields.
                                patientList.add(new Patient(patientID, cgID, firstName, lastName, dob, contactNumber, diagnosis, emergencyContact, userimage, email,password));
                            }

                            // Use the adapter's replaceData method to update the list and UI
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
        // Fetch the patient list every time the activity is resumed to see updates.
        if (careGiverID != -1) {
            fetchPatientList();
        }
    }

    @Override
    public void onDeleteClick(Patient patient, int position) {
        // Call a method to perform the network request
        deletePatientOnServer(patient, position);
    }

    private void deletePatientOnServer(final Patient patient, final int position) {
        // IMPORTANT: Replace with your server's IP address or domain
        String url = "http://100.104.224.68/android/api.php?action=delete_patient";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // This block is executed when the server responds successfully
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");

                        if ("success".equals(status)) {
                            // The patient was deleted on the server, now remove it from the UI
                            adapter.removeItem(position);
                            Toast.makeText(ViewPatientList.this, "Patient deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            // The server responded with an error (e.g., patient not found)
                            String message = jsonObject.getString("message");
                            Toast.makeText(ViewPatientList.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // This error occurs if the server's response is not valid JSON
                        Toast.makeText(ViewPatientList.this, "JSON parsing error!", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    // This block is executed if there's a network error (e.g., no connection)
                    Toast.makeText(ViewPatientList.this, "Volley Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // This is where you send the data to the PHP script
                Map<String, String> params = new HashMap<>();
                params.put("patientID", String.valueOf(patient.getPatientID()));
                return params;
            }
        };

        // Add the request to the queue to be executed
        requestQueue.add(stringRequest);
    }
}