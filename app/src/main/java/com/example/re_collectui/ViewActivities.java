package com.example.re_collectui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViewActivities extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<Activity> activityList;

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

        activityList = new ArrayList<>();
        activityList.add(new Activity("2025-06-15",1));
        activityList.add(new Activity("2025-06-16",2));
        activityList.add(new Activity("2025-06-17",3));
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ActivityAdapter adapter = new ActivityAdapter(activityList);
        recyclerView.setAdapter(adapter);

    }

}
