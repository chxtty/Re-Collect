package com.example.re_collectui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewEntry extends AppCompatActivity {

    String title;
    String date;
    String content;
    int author;
    int entryId;




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



    public void Listeners(){
        ImageView imgEdit = findViewById(R.id.imgEdit);
        imgEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ViewEntry.this, EditEntry.class);
            intent.putExtra("author", author);
            intent.putExtra("entryId", entryId);
            startActivityForResult(intent,200);
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
