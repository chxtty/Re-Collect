package com.example.re_collectui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;

import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventsView extends AppCompatActivity {

    RecyclerView parentRecyclerView;

    CustomToast toast;
    private EventAdapter adapter;
    private ArrayList<Event> eventList = new ArrayList<>();
    private ArrayList<Event> searchList = new ArrayList<>();
    private String currentQuery = "";
    boolean filterByTitle = true;
    boolean filterByLocation ;
    boolean sortAsc = true;
    boolean completed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        int patientID = sharedPref.getInt("patientID", -1);
        Log.d("EventsView", "Patient ID: " + patientID);

        if (patientID != -1){
            getEvents(patientID);
        }

        parentRecyclerView = findViewById(R.id.parentRecyclerView);
        parentRecyclerView.setHasFixedSize(true);
        parentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this, eventList, searchList);
        parentRecyclerView.setAdapter(adapter);
        parentRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(30));
        toast = new CustomToast(this);

        ConstraintLayout consCreate = findViewById(R.id.crdCreateEvent); // I changed from cardview to constraint layout to fit with the new button and changed name appropriately

        consCreate.setOnClickListener(v -> showCreateEventDialog());

        SearchView searchView = findViewById(R.id.edtSearchEvents);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                applyEventFilterDialog(currentQuery, filterByTitle, filterByLocation, sortAsc, completed);
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                applyEventFilterDialog(currentQuery, filterByTitle, filterByLocation, sortAsc, completed);
                return false;
            }
        });

    }

    private void getEvents(int patientID) {
        String url = GlobalVars.apiPath + "view_events&patientId=" + patientID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("EventsView", "API response: " + response.toString()); // ADD THIS

                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray events = response.getJSONArray("events");
                            eventList.clear();

                            for (int i = 0; i < events.length(); i++) {
                                JSONObject event = events.getJSONObject(i);

                                String title = event.getString("eventTitle");
                                String startDate = event.getString("eventStartDate").split(" ")[0];
                                String endDate = event.getString("eventEndDate").split(" ")[0];
                                String description = event.getString("eventDescription");
                                String location = event.getString("eventLocation");
                                int id = event.getInt("eventID");
                                boolean allDay = event.optInt("allDay", 0) == 1;

                                eventList.add(new Event(id, title, startDate, endDate, description, location, allDay));
                            }

                            applyEventFilterDialog(currentQuery, filterByTitle, filterByLocation, sortAsc, completed);

                        } else {
                            Log.e("EventsView", "API returned error: " + response.getString("message"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("EventsView", "JSON parsing error");
                    }

                },
                error -> {
                    Log.e("Volley", "Error: " + error.toString());
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void showCreateEventDialog() {

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.event_create_dialog, null);
        CustomToast toast1 = new CustomToast(this);


        EditText edtTitle = dialogView.findViewById(R.id.editTitle);
        EditText edtStartDate = dialogView.findViewById(R.id.editStartDate);
        EditText edtEndDate = dialogView.findViewById(R.id.editEndDate);
        CheckBox checkAllDay = dialogView.findViewById(R.id.checkBox);
        EditText edtLocation = dialogView.findViewById(R.id.editLocation);
        EditText edtDescription = dialogView.findViewById(R.id.editDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);


        edtStartDate.setFocusable(false);
        edtStartDate.setClickable(true);
        edtStartDate.setOnClickListener(v -> showDatePicker(edtStartDate));

        edtEndDate.setFocusable(false);
        edtEndDate.setClickable(true);
        edtEndDate.setOnClickListener(v -> showDatePicker(edtEndDate));

        checkAllDay.setOnCheckedChangeListener((v,b) -> {
            if (b){
                edtEndDate.setEnabled(false);
                edtEndDate.setAlpha(0.5f);
                if (!edtStartDate.getText().toString().isEmpty())
                {
                    edtEndDate.setText(edtStartDate.getText().toString());
                }
            } else {
                edtEndDate.setEnabled(true);
                edtEndDate.setAlpha(1.0f);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();


        btnCancel.setOnClickListener(v ->
                {   edtTitle.setText("");
                    edtStartDate.setText("");
                    edtEndDate.setText("");
                    edtLocation.setText("");
                    edtDescription.setText("");
                    checkAllDay.setChecked(false);
                    dialog.dismiss();
                });

        btnCreate.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String startDate = edtStartDate.getText().toString().trim();
            String endDate = "";
            boolean allDay = checkAllDay.isChecked();
            String location = edtLocation.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();

            if (!allDay) {
                endDate = edtEndDate.getText().toString().trim();
            } else {
                endDate = startDate;
            }

            if (title.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                toast1.GetErrorToast("Please fill in required fields").show();
                return;
            }

            if (startDate.compareTo(endDate) > 0) {
                toast1.GetErrorToast( "Start date must be before the End date").show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startD = null;
            try {
                if (startDate != null) {
                    startD = sdf.parse(startDate);
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date today = cal.getTime(); //resets to midnight in case event wants to start today

            if (startD.before(today)) {
                toast1.GetErrorToast("Event cannot start in the past").show();
                return;
            }

            createEvent(title, startDate, endDate, allDay, location, description);


            dialog.dismiss();
        });

        dialog.show();
    }



    private void createEvent(String title, String startDate, String endDate, boolean allDay, String location, String description) {
        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        int patientID = sharedPref.getInt("patientID", -1);

        String url = GlobalVars.apiPath + "create_event";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.getString("status").equals("success")) {
                            toast.GetInfoToast( "Event created!").show();
                            int newId = res.getInt("event_id");
                            Event event = new Event(newId, title, startDate, endDate, description, location, allDay);
                            eventList.add(event);
                            applyEventFilterDialog(currentQuery,filterByTitle,filterByLocation,sortAsc,completed);
                        } else {
                           toast.GetErrorToast(res.getString("message")).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                       Log.e("JSON", "Invalid response from server");
                    }
                },
                error -> {
                    error.printStackTrace();
                    Log.e("Server Connection:", "Failed to connect to server");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("start_date", startDate);
                params.put("end_date", endDate);
                params.put("all_day", allDay ? "1" : "0");
                params.put("location", location);
                params.put("description", description);
                params.put("patient_id", String.valueOf(patientID));
                return params;
            }
        };

        queue.add(request);
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    targetEditText.setText(date);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private String getCurrDate(){
        Calendar calendar = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }


    public void showFilterDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view1 = getLayoutInflater().inflate(R.layout.event_filter_dialog,null);
        builder.setView(view1);
        AlertDialog dialog = builder.create();

        RadioButton rgbTitle = view1.findViewById(R.id.rgbTitle);
        RadioButton rgbLocation = view1.findViewById(R.id.rgbLocation);
        RadioButton rgbAsc = view1.findViewById(R.id.rgbAsc);
        RadioButton rgbDesc = view1.findViewById(R.id.rgbDesc);

        Button btnCancel = view1.findViewById(R.id.btnCancelFilter);
        Button btnSave = view1.findViewById(R.id.btnSaveFilter);
        CheckBox checkCompleted = view1.findViewById(R.id.checkDeclined);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        //default values
        rgbTitle.setChecked(filterByTitle);
        rgbLocation.setChecked(filterByLocation);
        rgbAsc.setChecked(sortAsc);
        rgbDesc.setChecked(!sortAsc);
        checkCompleted.setChecked(completed);

        btnSave.setOnClickListener(v -> {
           filterByTitle = rgbTitle.isChecked();
           filterByLocation = rgbLocation.isChecked();
           sortAsc = rgbAsc.isChecked();
           completed = checkCompleted.isChecked();
            applyEventFilterDialog(currentQuery, filterByTitle, filterByLocation, sortAsc, completed);

           dialog.dismiss();
        });

        dialog.show();

    }

    private void applyEventFilterDialog(String searchQuery, boolean filterByTitle, boolean filterByLocation,
                                    boolean ascending, boolean showCompleted) {
    List<Event> filtered = new ArrayList<>();
    String query = searchQuery.toLowerCase(Locale.getDefault());

    for (Event event : eventList) {
        boolean matchesSearch = false;

        if(!query.isEmpty()) {
            if (filterByTitle && startsWithWord(event.getTitle(), query)) {
                matchesSearch = true;
            } else if (filterByLocation && startsWithWord(event.getLocation(), query)) {
                matchesSearch = true;
            }
        } else {
            matchesSearch = true;
        }

        boolean isCompleted = event.getEndDate().compareTo(getCurrDate()) < 0;

        if (matchesSearch && (showCompleted || !isCompleted)) {
            filtered.add(event);
        }
    }

    filtered.sort((e1, e2) -> ascending ?
            e1.getStartDate().compareTo(e2.getStartDate()) :
            e2.getStartDate().compareTo(e1.getStartDate()));

    searchList.clear();
    searchList.addAll(filtered);
    adapter.notifyDataSetChanged();
    }

    //since startWith only works on the first work
    private boolean startsWithWord(String text, String query) {
        String[] words = text.toLowerCase(Locale.getDefault()).split("\\s+");
        for (String word : words) {
            if (word.startsWith(query)) {
                return true;
            }
        }
        return false;
    }
}