package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {


    EditText editEmail, editPassword;
    Button btnSignIn, btnSignUp, btnSignInCaregiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //fetchDataFromAPI();

        editEmail = findViewById(R.id.edtEmail);
        editPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignInCaregiver=findViewById(R.id.btnSignInCare);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> showSignUpDialog());

        btnSignIn.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            login(email,password);
        });

        btnSignInCaregiver.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            login_caregiver1(email,password);
        });


    }

    private void login(String email, String password) {
        String url = "http://100.104.224.68/android/api.php?action=login";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if (status.equals("success")) {
                            JSONObject user = jsonResponse.getJSONObject("user");
                            String name = user.getString("firstName");
                            int patientID = user.getInt("patientID");
                            int caregiverID= user.getInt("careGiverID");
                            //toast.GetGreatingToast("Welcome " + name).show();
                            Toast.makeText(this,"Welcome " + name , Toast.LENGTH_LONG).show();

                            SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("patientID", patientID);
                            editor.putInt("careGiverID", caregiverID);
                            editor.putString("name", name);
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, DashboardPatient.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        queue.add(stringRequest);


    }

    private void showSignUpDialog() {
        // Options for sign up
        String[] options = {"Caregiver", "Patient"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign up:")
                .setSingleChoiceItems(options, -1, (dialog, which) -> {
                    // Store choice temporarily (caregiver or patient)
                    if (options[which].equals("Caregiver")) {
                        // Open caregiver sign up activity
                        Intent intent = new Intent(LoginActivity.this, CreateCaregiver.class);
                        startActivity(intent);
                        dialog.dismiss();
                    } else {
                        // If patient → show caregiver login dialog
                        dialog.dismiss();
                        showCaregiverLoginDialog();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showCaregiverLoginDialog() {
        // Inflate custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogue_caregiver_login, null);

        EditText caregiverEmail = dialogView.findViewById(R.id.caregiverEmail);
        EditText caregiverPassword = dialogView.findViewById(R.id.caregiverPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Caregiver Credentials")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String email = caregiverEmail.getText().toString().trim();
                    String password = caregiverPassword.getText().toString().trim();

                    // 🔑 TODO: Replace with real caregiver verification (e.g. Firebase DB check)
                    // if (email.equals("caregiver@example.com") && password.equals("1234")) {
                    //   // If verified → go to patient signup
                    // Intent intent = new Intent(LoginActivity.this, CreatePatient.class);
                    //startActivity(intent);
                    //} else {
                    //   Toast.makeText(LoginActivity.this, "Invalid caregiver credentials", Toast.LENGTH_SHORT).show();
                    //}
                    login_caregiver2(email,password);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void login_caregiver2(String email, String password) {
        String url = "http://100.104.224.68/android/api.php?action=login_caregiver";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if (status.equals("success")) {
                            JSONObject user = jsonResponse.getJSONObject("user");
                            int careGiverID = user.getInt("careGiverID");
                            String firstName = user.getString("firstName");

                            SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("careGiverID", careGiverID);
                            editor.putString("caregiverFirstName", firstName);
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, CreatePatient.class);
                            // This flag tells CreatePatient where to go next
                            intent.putExtra("NAVIGATE_TO_DASHBOARD", true);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        queue.add(stringRequest);


    }

    private void login_caregiver1(String email, String password) {
        String url = "http://100.104.224.68/android/api.php?action=login_caregiver";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if (status.equals("success")) {
                            JSONObject user = jsonResponse.getJSONObject("user");
                            String name = user.getString("firstName");
                            int careGiverID = user.getInt("careGiverID");


                            SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("careGiverID", careGiverID);
                            editor.putString("caregiverFirstName", name);
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, DashboardCaregiver.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        queue.add(stringRequest);


    }
}