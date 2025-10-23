package com.example.re_collectui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class RequestsView extends AppCompatActivity {

    private List<RequestItem> allRequests = new ArrayList<>();
    private List<RequestItem> filteredRequests = new ArrayList<>();
    private List<RequestItem> activityRequests = new ArrayList<>();
    private List<RequestItem> communityRequests = new ArrayList<>();

    private RequestAdapter adapter;
    private RecyclerView recyclerView;
    private SearchView searchView;

    private RequestItem.RequestType selectedType = null;
    private boolean filterDeclined = false;
    private boolean sortByName = true;
    private boolean sortAscending = true;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_requests_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.edtSearchRequests);

        adapter = new RequestAdapter(filteredRequests, this::onRequestClick); // for going to request view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(30));

        fetchRequests(); // get requests from db

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { // allows user to search
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query.trim();
                applyFiltersAndSearch();
                return true; }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText.trim();
                applyFiltersAndSearch();
                return true; }
        });

        findViewById(R.id.crdFilterEvents).setOnClickListener(v -> showFilterDialog());
    }

private void fetchRequests() {
    RequestQueue queue = Volley.newRequestQueue(this);
    String url = "http://10.0.2.2/recollect/api.php?action=get_requests";

    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                allRequests.clear();
                try {
                    JSONArray data = response.optJSONArray("data");
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.optJSONObject(i);
                            if (obj == null) continue;

                            String typeStr = obj.optString("type", "ACTIVITY");
                            RequestItem.RequestType type = "ACTIVITY".equalsIgnoreCase(typeStr) ?
                                    RequestItem.RequestType.ACTIVITY :
                                    RequestItem.RequestType.COMMUNITY;

                            String name, author;
                            if (type == RequestItem.RequestType.ACTIVITY) {
                                name = obj.optString("actType", "Activity");
                                author = obj.optString("author", "Unknown");
                            } else {
                                name = obj.optString("commFirstName", "") + " " + obj.optString("commLastName", "");
                                author = obj.optString("author", "Unknown");
                            }

                            RequestItem item = new RequestItem(
                                    obj.optInt("id", 0), type,
                                    obj.optInt("patientID", 0),
                                    obj.optInt("careGiverID", 0),
                                    obj.optString("status", "pending"), author, name,
                                    obj.optString("actType", null),
                                    obj.optString("actDescription", null),
                                    obj.optString("commType", null),
                                    obj.optString("commFirstName", null),
                                    obj.optString("commLastName", null),
                                    obj.optString("commDescription", null),
                                    obj.optString("commCuteMessage", null),
                                    obj.optString("commImage", null)
                            );
                            allRequests.add(item);
                        }
                    }

                    applyFiltersAndSearch();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            error -> Log.e( "Network error ",  error.getMessage())
    );

    queue.add(request);
}

private void applyFiltersAndSearch() {
    filteredRequests.clear();

    for (RequestItem item : allRequests) {
        boolean matches = true;

        if (selectedType != null) {
            if (selectedType == RequestItem.RequestType.ACTIVITY && item.getType() != RequestItem.RequestType.ACTIVITY) {
                matches = false;
            } else if (selectedType == RequestItem.RequestType.COMMUNITY && item.getType() != RequestItem.RequestType.COMMUNITY) {
                matches = false;
            }
        }

        if (filterDeclined && !"DECLINED".equalsIgnoreCase(item.getStatus())) {
            matches = false;
        }

        if (matches && !searchQuery.isEmpty()) {
            String lowerQuery = searchQuery.toLowerCase();
            if (!(item.getName().toLowerCase().contains(lowerQuery) ||
                  item.getAuthor().toLowerCase().contains(lowerQuery))) {
                matches = false;
            }
        }

        if (matches) filteredRequests.add(item);
    }

        Comparator<RequestItem> comparator = sortByName
                ? Comparator.comparing(RequestItem::getName, String.CASE_INSENSITIVE_ORDER)
                : Comparator.comparing(RequestItem::getAuthor, String.CASE_INSENSITIVE_ORDER);

        if (sortAscending) {
            Collections.sort(filteredRequests, comparator);
        } else {
            Collections.sort(filteredRequests, comparator.reversed());
        }

    adapter.notifyDataSetChanged();
}

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.request_filter_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        RadioButton rbAll = dialogView.findViewById(R.id.rgbAllReq);
        RadioButton rgbActivity = dialogView.findViewById(R.id.rgbActivitiesReq);
        RadioButton rgbCommunity = dialogView.findViewById(R.id.rgbCommunityReq);
        RadioButton rgbName = dialogView.findViewById(R.id.rgbNameReq);
        RadioButton rgbAuthor = dialogView.findViewById(R.id.rgbAuthorReq);
        RadioButton rgbAsc = dialogView.findViewById(R.id.rgbAscReq);
        RadioButton rgbDesc = dialogView.findViewById(R.id.rgbDescReq);
        CheckBox chkDeclined = dialogView.findViewById(R.id.checkDecline);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelFilter);
        Button btnSave = dialogView.findViewById(R.id.btnSaveFilter);

        if (selectedType == null) rbAll.setChecked(true);
            else if (selectedType == RequestItem.RequestType.ACTIVITY) rgbActivity.setChecked(true);
            else rgbCommunity.setChecked(true);

        if (sortByName) rgbName.setChecked(true);
            else rgbAuthor.setChecked(true);

        if (sortAscending) rgbAsc.setChecked(true);
            else rgbDesc.setChecked(true);

            chkDeclined.setChecked(filterDeclined);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            if (rgbActivity.isChecked()) selectedType = RequestItem.RequestType.ACTIVITY;
            else if (rgbCommunity.isChecked()) selectedType = RequestItem.RequestType.COMMUNITY;
            else selectedType = null;

            filterDeclined = chkDeclined.isChecked();
            sortByName = rgbName.isChecked();
            sortAscending = rgbAsc.isChecked();

            applyFiltersAndSearch();
            dialog.dismiss();
    });

        dialog.show();
    }

    private void onRequestClick(RequestItem requestItem) {
        if (requestItem.getType() == RequestItem.RequestType.ACTIVITY)
            startActivity(new Intent(this, RequestEditActivity.class).putExtra("requestItem", requestItem));
        else
            startActivity(new Intent(this, RequestEditCommunity.class).putExtra("requestItem", requestItem));
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchRequests(); // refresh list
    }
}
