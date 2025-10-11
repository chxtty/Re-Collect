package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewActivities extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<Activity> activityList;

    private int caregiverID, patientID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_activities);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.view_activities), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        patientID = sharedPref.getInt("patientID", -1);
        caregiverID = sharedPref.getInt("caregiverID", -1);
        String name = sharedPref.getString("name", "");

        activityList = new ArrayList<>();
        activityList.add(new Activity("2025-06-15",1));
        activityList.add(new Activity("2025-06-16",2));
        activityList.add(new Activity("2025-06-17",3));
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ActivityAdapter adapter = new ActivityAdapter(activityList);
        recyclerView.setAdapter(adapter);

    }

    public void showRequestDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View currView = getLayoutInflater().inflate(R.layout.request_activity_dialog,null);

        EditText edtNameAct = currView.findViewById(R.id.edtActName);
        EditText edtDesc = currView.findViewById(R.id.edtActDescr);

        Button btnCancel = currView.findViewById(R.id.btnCancelActReq);
        Button btnSubmit = currView.findViewById(R.id.btnSubmitActReq);

        builder.setView(currView);
        AlertDialog dialog = builder.create();



        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String type = edtNameAct.getText().toString().trim();
            String descr = edtDesc.getText().toString().trim();

            if (type.isEmpty() || descr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            submitActivityRequest(patientID,caregiverID,type,descr);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void submitActivityRequest(int patientID, int caregiverID, String type, String description) {

        String url = "http://10.0.2.2/recollect/api.php?action=create_activity_request";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
             response -> {
                try {
                    JSONObject res = new JSONObject(response);
                    String status = res.getString("status");

                    if (status.equals("success")) {
                        Toast.makeText(this, "Activity request submitted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, res.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            },
            error -> Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show()
    ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("patientID", String.valueOf(patientID));
                params.put("careGiverID", String.valueOf(caregiverID));
                params.put("status", "Pending");
                params.put("actType", type);
                params.put("actDescription", description);
                return params;
            }
        };
        queue.add(request);

    }
}
