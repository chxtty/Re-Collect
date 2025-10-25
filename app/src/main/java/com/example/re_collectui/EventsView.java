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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventsView extends AppCompatActivity {

    // TODO filter and back button
    // TODO location sync with google maps
    RecyclerView parentRecyclerView;
    private EventAdapter adapter;
    private ArrayList<Event> eventList = new ArrayList<>();
    private ArrayList<Event> searchList = new ArrayList<>();
    private String currentQuery = "";

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

        if (patientID != -1) {
            getEvents(patientID);
        } else {
            Toast.makeText(this, "Patient ID not found in session", Toast.LENGTH_SHORT).show();
        }

        parentRecyclerView = findViewById(R.id.parentRecyclerView);
        parentRecyclerView.setHasFixedSize(true);
        parentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this, eventList, searchList);
        parentRecyclerView.setAdapter(adapter);
        parentRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(30));

        CardView crdCreate = findViewById(R.id.crdCreateEvent);

        crdCreate.setOnClickListener(v -> showCreateEventDialog());

        SearchView searchView = findViewById(R.id.edtSearchEvents);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                adapter.getFilter().filter(newText);
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                adapter.getFilter().filter(query);
                return false;
            }
        });

    }

    private void getEvents(int patientID) {
        String url = "http://100.104.224.68/android/api.php?action=view_events&patientId=" + patientID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray events = response.getJSONArray("events");

                            for (int i = 0; i < events.length(); i++) {
                                JSONObject event = events.getJSONObject(i);
                                String title = event.getString("eventTitle");
                                String startDate = event.getString("eventStartDate");
                                String endDate = event.getString("eventEndDate");
                                String description = event.getString("eventDescription");
                                String location = event.getString("eventLocation");
                                int id = event.getInt("eventID");
                                boolean allDay = event.getInt("allDay") ==1;

                                eventList.add(new Event(id, title, startDate, endDate, description, location, allDay));
                            }
                            searchList.addAll(eventList);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Volley", "Error: " + error.getMessage());
                    Toast.makeText(this, "Volley error", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void showCreateEventDialog() {

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.event_create_dialog, null);


        EditText editTitle = dialogView.findViewById(R.id.editTitle);
        EditText editStartDate = dialogView.findViewById(R.id.editStartDate);
        EditText editEndDate = dialogView.findViewById(R.id.editEndDate);
        CheckBox checkAllDay = dialogView.findViewById(R.id.checkBox);
        EditText editLocation = dialogView.findViewById(R.id.editLocation);
        EditText editDescription = dialogView.findViewById(R.id.editDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);


        editStartDate.setFocusable(false);
        editStartDate.setClickable(true);
        editStartDate.setOnClickListener(v -> showDatePicker(editStartDate));

        editEndDate.setFocusable(false);
        editEndDate.setClickable(true);
        editEndDate.setOnClickListener(v -> showDatePicker(editEndDate));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();


        btnCancel.setOnClickListener(v ->
                {   editTitle.setText("");
                    editStartDate.setText("");
                    editEndDate.setText("");
                    editLocation.setText("");
                    editDescription.setText("");
                    checkAllDay.setChecked(false);
                    dialog.dismiss();
                });

        btnCreate.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String startDate = editStartDate.getText().toString().trim();
            String endDate = editEndDate.getText().toString().trim();
            boolean allDay = checkAllDay.isChecked();
            String location = editLocation.getText().toString().trim();
            String description = editDescription.getText().toString().trim();

            if (title.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDate.compareTo(endDate) > 0) {
                Toast.makeText(this, "Start date must be before or equal to End date", Toast.LENGTH_SHORT).show();
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
        if (patientID == -1) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://100.104.224.68/android/api.php?action=create_event";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject res = new JSONObject(response);
                        if (res.getString("status").equals("success")) {
                            Toast.makeText(this, "Event created!", Toast.LENGTH_SHORT).show();
                            int newId = res.getInt("event_id");
                            Event event = new Event(newId, title, startDate, endDate, description, location, allDay);
                            eventList.add(event);
                            adapter.addEvent(event, currentQuery);
                        } else {
                            Toast.makeText(this, "Error: " + res.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
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
        final Calendar calendar = Calendar.getInstance();
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


}