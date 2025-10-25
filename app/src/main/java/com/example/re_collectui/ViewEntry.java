package com.example.re_collectui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewEntry extends AppCompatActivity {

    String title;
    String date;
    String content;
    int author;
    int entryId;
    private CustomToast toast;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        toast = new CustomToast(this);

        Intent intent = getIntent();
        entryId = intent.getIntExtra("entryId",-1);

        fetchInfo();
        Listeners();
        ImageView imgBack;

        imgBack = findViewById(R.id.backButton);
        imgBack.setOnClickListener( e -> {
            onBackPressed();
        });

    }
    private void deleteEntry(int entryId) {
        String url = GlobalVars.apiPath + "delete_diary_entry";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    toast.GetDeleteToast("Entry deleted successfully").show();
                    finish(); // close this activity and go back to list
                },
                error -> {
                    toast.GetErrorToast("Error: " + error.getMessage()).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("entryId", String.valueOf(entryId));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }




    public void Listeners(){
        ImageView imgEdit = findViewById(R.id.imgEdit);
        imgEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ViewEntry.this, EditEntry.class);
            intent.putExtra("author", author);
            intent.putExtra("entryId", entryId);
            startActivityForResult(intent,200);
        });

        ImageView imgDelete = findViewById(R.id.imgDelete);
        imgDelete.setOnClickListener(v -> {
            CustomPopupDialogFragment dialog = CustomPopupDialogFragment.newInstance(
                    "Are you sure you want to delete this entry?",
                    "Yes",
                    "Cancel"
            );

            dialog.setOnPositiveClickListener(() -> {
                deleteEntry(entryId);
            });

            dialog.show(getSupportFragmentManager(), "DeleteConfirmationDialog");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchInfo();
    }

    public void fetchInfo(){
        DiaryFetcher apiHelper = new DiaryFetcher(this);
        apiHelper.getDiaryEntry(entryId, new DiaryFetcher.DiaryEntryCallback() {
            @Override
            public void onSuccess(DiaryFetcher.DiaryEntry entry) {
                ((TextView) findViewById(R.id.tvTitle)).setText(entry.diaryTitle);
                ((TextView) findViewById(R.id.tvContent)).setText(entry.content);
                date = entry.diaryDate;
                dateFormatter(date);
            }

            @Override
            public void onError(String errorMessage) {
                // Show error (optional)
                ((TextView) findViewById(R.id.tvTitle)).setText("Error loading entry: " + errorMessage);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 200 && resultCode == RESULT_OK){
           boolean updated = data.getBooleanExtra("entryUpdated", false);
           if(updated){
               fetchInfo();
           }
        }
    }

    public void dateFormatter(String date){
        TextView tvDate = findViewById(R.id.tvDate);
        this.date = date;
        if(date == null || date.isEmpty()){
            tvDate.setText("Date not available");
            return;
        }

        try{
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = inputFormat.parse(date);
            if(parsedDate != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                date = outputFormat.format(parsedDate);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        tvDate.setText(date);
    }


}
