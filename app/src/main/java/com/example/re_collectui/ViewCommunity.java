package com.example.re_collectui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class ViewCommunity extends AppCompatActivity {

    private FloatingActionButton fabAddMember;
    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private ArrayList<Community_Member> memberList = new ArrayList<>();
    private int patientID = -1;


    // In ViewCommunity.java

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_community);

        // Remove or keep this EdgeToEdge code as you see fit
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fabAddMember = findViewById(R.id.fabAddMember);
        fabAddMember.setOnClickListener(view -> {
            Intent intent = new Intent(ViewCommunity.this, AddCommMem.class);
            intent.putExtra("patientID", patientID);
            startActivity(intent);
        });

        patientID = getIntent().getIntExtra("patientID", -1);
        if (patientID == -1) {
            Toast.makeText(this, "Error: Could not identify the patient's community.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ImageView imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(e -> onBackPressed());

        recyclerView = findViewById(R.id.rvCommunity);

        // ✅ THIS IS THE FIX
        // 1. Determine the user's role by checking the session.
        SharedPreferences sharedPref = getSharedPreferences("userSession", MODE_PRIVATE);
        boolean isCaregiverViewing = sharedPref.getInt("patientID", -1) == -1;

        Toast.makeText(this, "1. ViewCommunity: Role isCaregiver = " + isCaregiverViewing, Toast.LENGTH_LONG).show();
        // 2. You MUST call the constructor with THREE arguments, passing the role.
        // The old two-argument call `new CommunityAdapter(this, memberList)` is wrong.
        adapter = new CommunityAdapter(this, memberList, isCaregiverViewing);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        SearchView svCommunity = findViewById(R.id.svCommunity);
        svCommunity.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void fetchCommunityMembers() {
        String url = "http://100.104.224.68/android/api.php?action=view_community_by_patient&patientId=" + patientID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray membersJson = response.getJSONArray("members");
                            ArrayList<Community_Member> newMembers = new ArrayList<>();
                            for (int i = 0; i < membersJson.length(); i++) {
                                JSONObject memberObject = membersJson.getJSONObject(i);
                                newMembers.add(Community_Member.fromJson(memberObject));
                            }
                            adapter.replaceData(newMembers);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("VolleyError", "Error: " + error.getMessage());
                    Toast.makeText(this, "Could not fetch community list", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (patientID != -1) {
            fetchCommunityMembers();
        }
    }
}