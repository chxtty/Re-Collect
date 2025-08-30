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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewDiaryEntries extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EntryAdapter adapter;
    private ArrayList<Entry> entryList = new ArrayList<>();

    private int patientID = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activty_veiw_entries);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        patientID = sharedPref.getInt("patientID", -1);
        if (patientID == -1) {
            Toast.makeText(this, "Patient ID not found in session", Toast.LENGTH_SHORT).show();
        }

        recyclerView = findViewById(R.id.rvEntries);
        adapter = new EntryAdapter(this, entryList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SetUpEntries();
    }

    private void SetUpEntries(){
        entryList.clear();
        String url = "http://100.79.152.109/android/api.php?action=view_diary_entries_p&patientId=" + patientID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("ViewDiaryEntries", "Response: " + response.toString());
                        if (response.getString("status").equals("success")) {
                            JSONArray entries = response.getJSONArray("diary_entries");

                            for (int i = 0; i < entries.length(); i++) {
                                JSONObject entry = entries.getJSONObject(i);
                                String date = entry.getString("diaryDate");
                                String title = entry.getString("diaryTitle");
                                int entryId = entry.getInt("entryID");
                                int author = entry.getInt("author");
                                String content = entry.getString("content");

                                entryList.add(new Entry(title, date, entryId, author, content));
                            }

                            adapter.notifyDataSetChanged(); //refresh adapter
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

    @Override
    protected void onResume() {
        super.onResume();
        SetUpEntries();
    }
}
