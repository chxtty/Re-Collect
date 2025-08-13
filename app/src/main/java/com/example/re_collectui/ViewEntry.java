package com.example.re_collectui;

import android.os.Bundle;
import android.widget.TextView;

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

        title = getIntent().getStringExtra("title");
        date = getIntent().getStringExtra("date");
        content = getIntent().getStringExtra("content");
        author = getIntent().getIntExtra("author", -1);
        entryId = getIntent().getIntExtra("entryId", -1);
        setUp();
    }

    protected void setUp() {
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvContent = findViewById(R.id.tvContent);

        if(this.date == null || this.date.isEmpty()){
            tvDate.setText("Date not available");
            return;
        }

        try{
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = inputFormat.parse(this.date);
            if(parsedDate != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                this.date = outputFormat.format(parsedDate);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            this.date = this.date;
        }

        try{
            if(tvTitle != null){
                tvTitle.setText(this.title);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            if(tvTitle != null){
                tvTitle.setText("Title not available");
            }
        }
        try{
            if(tvContent != null){
                tvContent.setText(this.content);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            if(tvContent != null){
                tvContent.setText("Content not available");
            }
        }

        tvDate.setText(this.date);
    }


}
