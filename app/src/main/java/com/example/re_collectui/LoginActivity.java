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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {


    EditText editEmail, editPassword;
    Button btnSignIn;

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

    // You'll need this variable at the class level (if you don't have it already)
    // to store the role once it's fetched.
    /**
     * Fetches the user role from the API using just the email.
     * This runs on a background thread and calls handleRoleResponse on the main thread.
     *
     * @param email The email to query.
     */
    private void fetchUserRole(String email) {
        // You could show a loading indicator here

        executor.execute(() -> {
            // This block runs on a background thread

            // !!! IMPORTANT: Update this to your correct API endpoint !!!
            String urlString = GlobalVars.apiPath + "getUserRoleByEmail";
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();

            try {
                // 1. Create POST data
                String postData = "email=" + URLEncoder.encode(email, StandardCharsets.UTF_8.name());
                byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);

                // 2. Setup Connection
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST"); // Use "POST" or "GET" as your API requires
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                urlConnection.setDoOutput(true);
                urlConnection.setReadTimeout(10000); // 10 seconds
                urlConnection.setConnectTimeout(10000);

                // 3. Write data to connection
                try (OutputStream os = urlConnection.getOutputStream()) {
                    os.write(postDataBytes);
                }

                // 4. Read response
                int responseCode = urlConnection.getResponseCode();
                BufferedReader br;

                if (responseCode >= 200 && responseCode < 300) {
                    // Success
                    br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                } else {
                    // Error
                    br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                }

                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }
                br.close();

                // 5. Post result to Main Thread
                String responseString = result.toString();
                // This schedules handleRoleResponse to run on the main UI thread
                runOnUiThread(() -> handleRoleResponse(responseString, responseCode));

            } catch (Exception e) {
                Log.e("Network_Exception", "Error during role fetch", e);
                runOnUiThread(() -> {
                    // Hide loading indicator
                    toast.GetErrorToast("Network error: ".concat(e.getMessage())).show();
                });
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }

    /**
     * Handles the JSON response from the role fetch API on the main thread.
     *
     * @param jsonResponseString The JSON string from the server.
     * @param responseCode       The HTTP response code.
     */
    private void handleRoleResponse(String jsonResponseString, int responseCode) {
        // Hide loading indicator here

        try {
            JSONObject jsonResponse = new JSONObject(jsonResponseString);

            // Check for both HTTP success and API-level success
            if (responseCode >= 200 && responseCode < 300 && jsonResponse.getString("status").equals("success")) {

                // --- This is the part you care about ---

                // Assuming the JSON response is like:
                // {"status":"success", "user": {"role":"patient", ...}}
                JSONObject user = jsonResponse.getJSONObject("user");
                String fetchedRole = user.getString("role");

                Log.i("UserRoleFetch", "Successfully fetched role: " + fetchedRole);

                // 1. Store the role in your variable
                //this.role = fetchedRole;

                // 2. Now call your use case method
                // runMyUseCase(this.role);

                // Or just show a toast for testing
                //toast.GetGreatingToast("Role found: " + this.role).show();

            } else {
                // Handle API error message (e.g., "user not found")
                String message = jsonResponse.optString("message", "Could not find user.");
                toast.GetErrorToast(message).show();
            }
        } catch (JSONException e) {
            Log.e("JSON_Parse_Error", "Error parsing role response: " + jsonResponseString, e);
            toast.GetErrorToast("Invalid response from server.").show();
        }
    }


}
