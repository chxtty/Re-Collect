package com.example.re_collectui;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityDialog extends DialogFragment {

    private Spinner spnActivity;
    private EditText etStartTime, etEndTime;
    private TextView tvDate;
    private Button btnSave, btnCancel;
    private List<Integer> activityIds = new ArrayList<>();
    private String message  = "nothing";
    private OnActivityDialogListener listener;

    // Callback interface
    public interface OnActivityDialogListener {
        void onSave(String activityType, String date, String startTime, String endTime, String message);
        void onCancel();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnActivityDialogListener) {
            listener = (OnActivityDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnActivityDialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dialog_new_activity, container, false);

        // Bind views
        spnActivity = view.findViewById(R.id.spnActivity);
        tvDate = view.findViewById(R.id.tvDate);
        etStartTime = view.findViewById(R.id.etStartTime);
        etEndTime = view.findViewById(R.id.etEndTime);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        SharedPreferences sharedPref = requireContext().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        String patientId = String.valueOf(sharedPref.getInt("patientID", -1));
        fetchActivityTypes();


        // Save
        btnSave.setOnClickListener(v -> {
            int position = spnActivity.getSelectedItemPosition();
            String activityType = spnActivity.getSelectedItem().toString();
            String date = tvDate.getText().toString().trim();
            String startTime = etStartTime.getText().toString().trim();
            String endTime = etEndTime.getText().toString().trim();


          //  saveActivityToServer(String.valueOf(activityIds.get(position)), patientId, startTime, endTime, date);

            if (listener != null) {
                listener.onSave(String.valueOf(activityIds.get(position)), date, startTime, endTime,message);
            }
            dismiss();
        });

        // Cancel
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismiss();
        });

        tvDate.setOnClickListener(v -> showDatePicker());

        return view;
    }


    private void fetchActivityTypes() {
        String url = "http://100.79.152.109/android/api.php?action=view_activity_types";
        List<String> activityTypes = new ArrayList<>();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray activities = response.getJSONArray("activities");
                            activityIds.clear(); // clear old IDs
                            activityTypes.clear();

                            for (int i = 0; i < activities.length(); i++) {
                                JSONObject obj = activities.getJSONObject(i);
                                activityTypes.add(obj.getString("actType"));
                                activityIds.add(obj.getInt("activityId"));
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    requireContext(),
                                    android.R.layout.simple_spinner_item,
                                    activityTypes
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spnActivity.setAdapter(adapter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        safeToast( "Failed to load activity types");
                    }
                },
                error -> {
                    error.printStackTrace();
                    safeToast("Volley error");
                });

        Volley.newRequestQueue(requireContext()).add(request);
    }


    private void showDatePicker() {
        // Get today’s date as default
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        // Create the DatePickerDialog
        new android.app.DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Months are zero-based, so add +1
                    String date = selectedYear + "-" +
                            String.format("%02d", (selectedMonth + 1)) + "-" +
                            String.format("%02d", selectedDay);
                    tvDate.setText(date);
                },
                year, month, day
        ).show();
    }

    private void saveActivityToServer(String activityId, String patientId, String startTime, String endTime, String actDate) {
        String url = "http://100.79.152.109/android/api.php?action=create_activity";

        Map<String, String> params = new HashMap<>();
        params.put("activityId", activityId);
        params.put("patientId", patientId);
        params.put("actStartTime", startTime);
        params.put("actEndTime", endTime);
        params.put("actDate", actDate);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            int detailId = response.getInt("detailId");
                            safeToast("Activity saved! ID: " + detailId);
                            // Optionally refresh RecyclerView or call listener
                        } else {
                            String message = response.getString("message");
                            safeToast("Error: " + message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        safeToast("JSON parse error");
                    }
                },
                error -> {
                    error.printStackTrace();
                    safeToast("Network error: ");
                });

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void safeToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            // Fragment not attached, fallback
            Log.w("ActivityDialog", "Cannot show toast: " + message);
            this.message = "Cannot show toast: " + message;
        }
    }




    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Optional: remove default title
        dialog.requestWindowFeature(STYLE_NO_TITLE);
        return dialog;
    }
}
