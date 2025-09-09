package com.example.re_collectui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;


import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewDiaryEntries extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EntryAdapter adapter;
    private ArrayList<Entry> entryList = new ArrayList<>();
    private CustomToast toast;

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

        ImageView imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(e -> {
            onBackPressed();
        });
        toast = new CustomToast(this);

        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        patientID = sharedPref.getInt("patientID", -1);
        if (patientID == -1) {
            toast.GetErrorToast("Patient ID not found").show();
        }

        recyclerView = findViewById(R.id.rvEntries);
        adapter = new EntryAdapter(this, entryList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        //Search
        SearchView svEntries = findViewById(R.id.svEntries);
        svEntries.setIconifiedByDefault(false);
        svEntries.clearFocus();

        svEntries.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }

       });
        // ----------

        //Create Entry
        ConstraintLayout newEntrylyo = findViewById(R.id.newEntrylyo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = sdf.format(new Date());

        newEntrylyo.setOnClickListener(v -> {
            createDiaryEntry("New Entry", dateString, "How was your day?", patientID);
        });
        //-----------


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

                            //adapter.notifyDataSetChanged(); //refresh adapter
                            adapter.replaceData(new ArrayList<>(entryList));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        toast.GetErrorToast("JSON parse error").show();
                    }
                },
                error -> {
                    Log.e("Volley", "Error: " + error.getMessage());
                    toast.GetErrorToast("Volley error: " + error.getMessage()).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void createDiaryEntry(String title, String date, String content, int authorId) {
        String url = "http://100.79.152.109/android/api.php?action=create_diary_entry";

        Log.d("CreateEntry", "Sending -> " +
                "Title: " + title +
                ", Date: " + date +
                ", Content: " + content +
                ", Author: " + authorId);

        JSONObject params = new JSONObject();
        try {
            params.put("diaryTitle", title);
            params.put("diaryDate", date);
            params.put("content", content);
            params.put("author", authorId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("CreateEntry", "Raw response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            int entryId = jsonResponse.getInt("entry_id");

                            toast.GetInfoToast("Entry created!").show();

                            Intent intent = new Intent(this, ViewEntry.class);
                            intent.putExtra("entryId", entryId);
                            startActivity(intent);
                           // finish();
                        } else {
                            toast.GetErrorToast("Error: " + jsonResponse.getString("message")).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        toast.GetErrorToast("JSON parse error").show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    toast.GetErrorToast("Volley error").show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("diaryTitle", title);
                params.put("diaryDate", date);
                params.put("content", content);
                params.put("author", String.valueOf(authorId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }


    @Override
    protected void onResume() {
        super.onResume();
        SetUpEntries();
    }
}
