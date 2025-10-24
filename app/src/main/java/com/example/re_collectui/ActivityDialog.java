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
    private int detailIdToUpdate = -1;
    private int activityIdToSelect = -1;
    private static final String ARG_DETAIL_ID = "detail_id";
    private static final String ARG_ACTIVITY_ID = "activity_id";
    private static final String ARG_DATE = "date";
    private static final String ARG_START_TIME = "start_time";
    private static final String ARG_END_TIME = "end_time";

    // Callback interface
    public interface OnActivityDialogListener {
        void onSave(int detailId, String activityId, String date, String startTime, String endTime); // Pass detailId back
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

        // Check for arguments to see if we are in "edit mode"
        if (getArguments() != null) {
            detailIdToUpdate = getArguments().getInt(ARG_DETAIL_ID, -1);
            activityIdToSelect = getArguments().getInt(ARG_ACTIVITY_ID, -1);
            tvDate.setText(getArguments().getString(ARG_DATE));
            etStartTime.setText(getArguments().getString(ARG_START_TIME));
            etEndTime.setText(getArguments().getString(ARG_END_TIME));
        }

        SharedPreferences sharedPref = requireContext().getSharedPreferences("userSession", Context.MODE_PRIVATE);
        String patientId = String.valueOf(sharedPref.getInt("patientID", -1));
        fetchActivityTypes();

        // Save button listener is now updated
        btnSave.setOnClickListener(v -> {
            int position = spnActivity.getSelectedItemPosition();
            if (position < 0) return; // Avoid crash if spinner is not ready

            String activityId = String.valueOf(activityIds.get(position));
            String date = tvDate.getText().toString().trim();
            String startTime = etStartTime.getText().toString().trim();
            String endTime = etEndTime.getText().toString().trim();

            if (listener != null) {
                // Pass the detailId back. It will be -1 for new activities.
                listener.onSave(detailIdToUpdate, activityId, date, startTime, endTime);
            }
            dismiss();
        });

        // Cancel button listener remains the same
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
                            // If we are editing, find and select the correct activity
                            if (activityIdToSelect != -1) {
                                for (int i = 0; i < activityIds.size(); i++) {
                                    if (activityIds.get(i) == activityIdToSelect) {
                                        spnActivity.setSelection(i);
                                        break;
                                    }
                                }
                            }
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

    public static ActivityDialog newInstance(int detailId, int activityId, String date, String startTime, String endTime) {
        ActivityDialog dialog = new ActivityDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_DETAIL_ID, detailId);
        args.putInt(ARG_ACTIVITY_ID, activityId);
        args.putString(ARG_DATE, date);
        args.putString(ARG_START_TIME, startTime);
        args.putString(ARG_END_TIME, endTime);
        dialog.setArguments(args);
        return dialog;
    }

    /*private void saveActivityToServer(String activityId, String patientId, String startTime, String endTime, String actDate) {
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
    }*/

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
