package com.example.re_collectui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EventsView extends AppCompatActivity {

    RecyclerView parentRecyclerView;
    private EventAdapter adapter;
    private ArrayList<Event> eventList = new ArrayList<>();

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
        adapter = new EventAdapter(this, eventList);
        parentRecyclerView.setAdapter(adapter);

    }

    private void getEvents(int patientID) {
        String url = "http://10.0.2.2/recollect/api.php?action=view_events&patientId=" + patientID;

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

                                eventList.add(new Event(title, startDate, endDate, description, location));
                            }

                            adapter.notifyDataSetChanged(); // Just refresh the adapter
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
}