package com.example.re_collectui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewActivities extends AppCompatActivity implements ActivityDialog.OnActivityDialogListener {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private int patientId;
    private List<Activity> activityList;

    private CustomToast toast;

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

        toast = new CustomToast(this);

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        patientId = sharedPref.getInt("patientID", -1);
        if (patientId == -1) {
            toast.GetErrorToast("Error: Patient ID not found").show();
        }

        activityList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActivityAdapter(activityList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnRecordActivity).setOnClickListener(v -> {
            ActivityDialog dialog = new ActivityDialog();
            dialog.show(getSupportFragmentManager(), "NewActivityDialog");
        });


    }

    private void SetUpActivities() {
        activityList.clear();
        String url = "http://100.79.152.109/android/api.php?action=view_activity_details_p&patientId=" + patientId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("ViewActivities", "Response: " + response.toString());
                        if (response.getString("status").equals("success")) {
                            JSONArray activities = response.getJSONArray("activity_details");

                            for (int i = 0; i < activities.length(); i++) {
                                JSONObject obj = activities.getJSONObject(i);

                                int detailId = obj.getInt("detailId");
                                int activityId = obj.getInt("activityId");
                                int patientId = obj.getInt("patientId");
                                String startTime = obj.getString("actStartTime");
                                String endTime = obj.getString("actEndTime");
                                String date = obj.getString("actDate");
                                String actIconBase64 = obj.getString("actIcon");

                                activityList.add(new Activity(
                                        detailId,
                                        activityId,
                                        patientId,
                                        startTime,
                                        endTime,
                                        date,
                                        actIconBase64
                                ));
                            }

                            // refresh adapter with new data
                            adapter.replaceData(new ArrayList<>(activityList));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast.GetErrorToast("JSON parse error").show();
                    }
                },
                error -> {
                    Log.e("Volley", "Error: " + error.getMessage());
                    toast.GetErrorToast("Network error: " + error.getMessage()).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetUpActivities();
    }

    @Override
    public void onSave(String activityType, String date, String startTime, String endTime,String message) {
        saveActivityToServer(activityType, String.valueOf(patientId), startTime, endTime, date);
        toast.GetInfoToast("Saved: " + activityType + " on " + date);
        // TODO: send new activity to your API here, then refresh list
    }

    @Override
    public void onCancel() {
        toast.GetErrorToast("Cancelled").show();
    }

    private void saveActivityToServer(String activityId, String patientId, String startTime, String endTime, String actDate) {
        String url = "http://100.79.152.109/android/api.php?action=create_activity";

        Map<String, String> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("patientId", patientId);
        params.put("actStartTime", startTime);
        params.put("actEndTime", endTime);
        params.put("actDate", actDate);

        Log.d("ActivityDialog", "Sending: activityId=" + activityId +
                ", patientId=" + patientId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", date=" + actDate);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    Log.d("ActivityDialog", "Cannot show toast: " + response);
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            int detailId = response.getInt("detailId");
                            toast.GetInfoToast("Activity saved!");
                            // Optionally refresh RecyclerView or call listener
                        } else {
                            String message = response.getString("message");
                            toast.GetErrorToast("Error: " + message).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast.GetErrorToast("JSON parse error").show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    toast.GetErrorToast("Network error: " + error.getMessage()).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

}
