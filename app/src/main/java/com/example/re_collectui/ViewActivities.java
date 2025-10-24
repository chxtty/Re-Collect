package com.example.re_collectui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
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

    private List<Activity> masterActivityList; // This will hold the original, unfiltered list
    private List<String> filterableActivityNames;
    private List<Integer> filterableActivityIds;
    private int currentFilterIndex = -1; // -1 means no filter (show all)
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
        ImageView imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(e -> {
            onBackPressed();
        });
        toast = new CustomToast(this);

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        patientId = sharedPref.getInt("patientID", -1);
        if (patientId == -1) {
            toast.GetErrorToast("Error: Patient ID not found").show();
        }

        // Initialize all lists
        masterActivityList = new ArrayList<>();
        filterableActivityNames = new ArrayList<>();
        filterableActivityIds = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Pass the master list to the adapter initially
        adapter = new ActivityAdapter(new ArrayList<>(masterActivityList));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnRecordActivity).setOnClickListener(v -> {
            ActivityDialog dialog = new ActivityDialog();
            dialog.show(getSupportFragmentManager(), "NewActivityDialog");
        });

        // --- UPDATED: Filter button listener ---
        findViewById(R.id.btnFiter).setOnClickListener(e -> {
            if (filterableActivityNames.isEmpty()) {
                toast.GetInfoToast("Filter types not loaded yet.").show();
                return;
            }

            // Cycle to the next filter index
            currentFilterIndex++;
            if (currentFilterIndex >= filterableActivityNames.size()) {
                currentFilterIndex = -1; // Loop back to "All Activities"
            }

            applyFilter();
        });

        fetchFilterableActivityTypes(); // Fetch the types to filter by
    }

    private void fetchFilterableActivityTypes() {
        String url = "http://100.79.152.109/android/api.php?action=view_activity_types";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray activities = response.getJSONArray("activities");
                            filterableActivityNames.clear();
                            filterableActivityIds.clear();
                            for (int i = 0; i < activities.length(); i++) {
                                JSONObject obj = activities.getJSONObject(i);
                                filterableActivityNames.add(obj.getString("actType"));
                                filterableActivityIds.add(obj.getInt("activityId"));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                });
        Volley.newRequestQueue(this).add(request);
    }

    private void applyFilter() {
        List<Activity> filteredList = new ArrayList<>();

        if (currentFilterIndex == -1) {
            // No filter, show all activities
            filteredList.addAll(masterActivityList);
            toast.GetInfoToast("Showing All Activities").show();
        } else {
            // Apply the selected filter
            int filterId = filterableActivityIds.get(currentFilterIndex);
            String filterName = filterableActivityNames.get(currentFilterIndex);

            for (Activity activity : masterActivityList) {
                if (activity.getActivityId() == filterId) {
                    filteredList.add(activity);
                }
            }
            toast.GetInfoToast("Filtering by: " + filterName).show();
        }

        adapter.replaceData(filteredList);
    }
    // --- UPDATED: SetUpActivities now populates the master list ---
    private void SetUpActivities() {
        String url = "http://100.79.152.109/android/api.php?action=view_activity_details_p&patientId=" + patientId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("ViewActivities", "Response: " + response.toString());
                        if (response.getString("status").equals("success")) {
                            masterActivityList.clear(); // Clear the master list before populating
                            JSONArray activities = response.getJSONArray("activity_details");

                            for (int i = 0; i < activities.length(); i++) {
                                // ... (JSON parsing code is the same)
                                JSONObject obj = activities.getJSONObject(i);
                                int detailId = obj.getInt("detailId");
                                int activityId = obj.getInt("activityId");
                                int patientId = obj.getInt("patientId");
                                String startTime = obj.getString("actStartTime");
                                String endTime = obj.getString("actEndTime");
                                String date = obj.getString("actDate");
                                String actIconBase64 = obj.getString("actIcon");

                                masterActivityList.add(new Activity(detailId, activityId, patientId, startTime, endTime, date, actIconBase64));
                            }

                            // Apply the current filter to the newly fetched data
                            applyFilter();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast.GetErrorToast("JSON parse error").show();
                    }
                },
                error -> {
                    // ... (error handling is the same)
                });

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetUpActivities();
    }

    @Override
    public void onSave(int detailId, String activityId, String date, String startTime, String endTime) {
        if (detailId == -1) {
            // This is a new activity
            saveActivityToServer(activityId, String.valueOf(patientId), startTime, endTime, date);
            toast.GetInfoToast("Saving new activity...").show();
        } else {
            // This is an existing activity to update
            updateActivityToServer(detailId, activityId, startTime, endTime, date);
            toast.GetInfoToast("Updating activity...").show();
        }
        // Refresh the list after a short delay to allow the server to process
        new Handler().postDelayed(this::SetUpActivities, 1000);
    }

    @Override
    public void onCancel() {
        toast.GetErrorToast("Cancelled").show();
    }

    private void updateActivityToServer(int detailId, String activityId, String startTime, String endTime, String actDate) {
        String url = "http://100.79.152.109/android/api.php?action=update_activity"; // Assuming this is your update endpoint

        Map<String, String> params = new HashMap<>();
        params.put("detailId", String.valueOf(detailId)); // IMPORTANT: Include the ID of the entry to update
        params.put("activityId", activityId);
        params.put("actStartTime", startTime);
        params.put("actEndTime", endTime);
        params.put("actDate", actDate);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            toast.GetInfoToast("Activity updated successfully!").show();
                        } else {
                            String message = response.getString("message");
                            toast.GetErrorToast("Update failed: " + message).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast.GetErrorToast("JSON parse error on update").show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    toast.GetErrorToast("Network error on update").show();
                });

        Volley.newRequestQueue(this).add(request);
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
