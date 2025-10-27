package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
// REMOVED: import android.widget.EditText; // No longer needed
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
// This is the only text field import you need
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {


    EditText editEmail, editPassword;
    Button btnSignIn, btnSignUp;

    private CustomToast toast;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
        toast = new CustomToast(this);
        editEmail = findViewById(R.id.edtEmail);
        editPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(v -> showSignUpDialog());

        btnSignIn.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            login(email,password, "onCreate");
        });

    }


    private void login(String email, String password, String MethodCall) {
        String url = GlobalVars.apiPath + "login";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if (status.equals("success")) {
                            JSONObject user = jsonResponse.getJSONObject("user");
                            String name = user.getString("firstName");
                            String role = user.optString("role");

                            SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("role", role);
                            editor.putString("name", name);
                            toast.GetGreatingToast("This role:" + role).show();

                            if (role.equals("patient")){
                                toast.GetGreatingToast("Welcome " + name).show();
                                int caregiverID = user.getInt("careGiverID");
                                int patientID = user.getInt("patientID");
                                editor.putInt("patientID", patientID);
                                editor.putInt("caregiverID",caregiverID);
                                editor.apply();

                                //Toast.makeText(this, "Welcome, " + name, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(LoginActivity.this, DashboardPatient.class);
                                startActivity(intent);
                            } else if(role.equals("admin")){
                                toast.GetGreatingToast("Welcome " + name).show();
                                int adminID = user.getInt("careGiverID");
                                editor.putInt("careGiverID", adminID);
                                editor.apply();
                                Intent intent;
                                //Toast.makeText(this, "Welcome, " + name, Toast.LENGTH_LONG).show();
                                if(MethodCall.equals("showCaregiverLoginDialog")){
                                    intent = new Intent(LoginActivity.this, CreatePatient.class);
                                    intent.putExtra("NAVIGATE_TO_DASHBOARD", true);
                                } else{
                                    intent = new Intent(LoginActivity.this, DashboardCaregiver.class);}
                                startActivity(intent);
                            }


                            finish();
                        } else {
                            String message = jsonResponse.getString("message");
                            toast.GetErrorToast(message).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Error",  e.getMessage());
                        toast.GetErrorToast("JSON Error: " + e.getMessage()).show();
                    }
                },
                error -> {
            Log.e( "Network error",error.toString());
            toast.GetErrorToast("Network Error: " + error.getMessage()).show();
        }

        )
        {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_signup_options, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        TextView optionCaregiver = dialogView.findViewById(R.id.optionCaregiver);
        TextView optionPatient = dialogView.findViewById(R.id.optionPatient);
        Button optionCancel = dialogView.findViewById(R.id.optionCancel);
        optionCaregiver.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateCaregiver.class);
            startActivity(intent);
            dialog.dismiss();
        });
        optionPatient.setOnClickListener(v -> {
            showCaregiverLoginDialog();
            dialog.dismiss();
        });
        optionCancel.setOnClickListener(v -> dialog.dismiss());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void showCaregiverLoginDialog() {
        // 1. Create the builder. No special theme is needed now that your main app theme is correct.
        AlertDialog.Builder builder = new AlertDialog.Builder(this, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_Dialog_Alert);
        // 2. Inflate your custom layout file.
        View dialogView = getLayoutInflater().inflate(R.layout.dialogue_caregiver_login, null);
        builder.setView(dialogView);

        // 3. Create the dialog from the builder.
        AlertDialog dialog = builder.create();

        // 4. Find all the views inside your custom layout.
        TextInputEditText caregiverEmail = dialogView.findViewById(R.id.caregiverEmail);
        TextInputEditText caregiverPassword = dialogView.findViewById(R.id.caregiverPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        // 5. Set the click listener for the "Cancel" button.
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss(); // Just closes the dialog.
        });

        // 6. Set the click listener for the "Confirm" button.
        btnConfirm.setOnClickListener(v -> {
            // Safely get the text from the fields to prevent crashes.
            String email = (caregiverEmail.getText() != null) ? caregiverEmail.getText().toString().trim() : "";
            String password = (caregiverPassword.getText() != null) ? caregiverPassword.getText().toString().trim() : "";

            // Call your existing login logic.
            login(email, password, "showCaregiverLoginDialog");

            // Close the dialog after the action is initiated.
            dialog.dismiss();
        });

        // 7. Make the dialog's window transparent to show your custom rounded background.
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 8. Show the dialog.
        dialog.show();
    }
}