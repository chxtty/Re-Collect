package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    Button btnSignIn;

    private CustomToast toast;

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

        btnSignIn.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            login(email,password);
        });
    }

    private void login(String email, String password) {
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
                            int patientID = user.getInt("patientID");
                            toast.GetGreatingToast("Welcome " + name).show();

                            SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("patientID", patientID);
                            editor.putString("role", role);
                            editor.putString("name", name);

                            if (role.equals("patient")){
                                int caregiverID = user.getInt("careGiverID");
                                editor.putInt("patientID", patientID);
                                editor.putInt("caregiverID",caregiverID);
                                editor.apply();

                                //Toast.makeText(this, "Welcome, " + name, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(LoginActivity.this, DashboardPatient.class);
                                startActivity(intent);
                            } else if(role.equals("admin")){
                                int adminID = user.getInt("caregiverID");
                                editor.putInt("caregiverID", adminID);
                                editor.apply();

                                //Toast.makeText(this, "Welcome, " + name, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(LoginActivity.this, DashboardCaregiver.class);
                                startActivity(intent);
                            }


                            finish();
                        } else {
                            String message = jsonResponse.getString("message");
                            toast.GetErrorToast(message);
                        }
                    } catch (JSONException e) {
                        Log.e("Error",  e.getMessage());
                    }
                },
                error -> Log.e( "Network error",error.toString())
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
