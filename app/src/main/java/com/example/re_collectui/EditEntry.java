package com.example.re_collectui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.OnBackPressedCallback;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditEntry extends AppCompatActivity {

    String title;
    String date;
    String content;
    String originalTitle;
    String originalContent;
    int author;
    int entryId;

    private CustomToast toast;

    TextView tvDate, etTitle, etEdit;

    ImageView imgBack;
    ImageView imgSave;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        toast = new CustomToast(this);

        Intent intent = getIntent();
        entryId = intent.getIntExtra("entryId", -1);

        etTitle = findViewById(R.id.etTitle);
        etEdit = findViewById(R.id.etEdit);
        tvDate = findViewById(R.id.tvDate);

        imgBack = findViewById(R.id.backButton);
        imgSave = findViewById(R.id.saveButton);
        imgBack.setOnClickListener( e -> {
            getOnBackPressedDispatcher().onBackPressed();
        });


        fetchInfo();
        SaveButton(imgSave);
        setupOnBackPressed();

    }



    public void fetchInfo(){
        DiaryFetcher apiHelper = new DiaryFetcher(this);
        apiHelper.getDiaryEntry(entryId, new DiaryFetcher.DiaryEntryCallback() {
            @Override
            public void onSuccess(DiaryFetcher.DiaryEntry entry) {
                originalTitle = (entry.diaryTitle != null) ? entry.diaryTitle : "";
                originalContent = (entry.content != null) ? entry.content : "";

                etTitle.setText(originalTitle);
                etEdit.setText(originalContent);
                date = entry.diaryDate;
                dateFormatter(date);
            }

            @Override
            public void onError(String errorMessage) {
                // Show error (optional)
                ((TextView) findViewById(R.id.etTitle)).setText("Error loading entry: " + errorMessage);
            }
        });
    }

    public void dateFormatter(String date){
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

    private void saveDiaryEntry(String title, String content) {
        try {
            URL url = new URL(GlobalVars.apiPath + "update_diary_entry");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String postData =
                    "entryId=" + URLEncoder.encode(String.valueOf(entryId), "UTF-8") +
                            "&diaryTitle=" + URLEncoder.encode(title, "UTF-8") +
                            "&content=" + URLEncoder.encode(content, "UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            runOnUiThread(() -> {
                toast.GetErrorToast("Server Response: " + response.toString()).show();
            });
            Intent intent = new Intent();
            intent.putExtra("entryUpdated", true);
            setResult(RESULT_OK, intent);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    toast.GetErrorToast("Error: " + e.getMessage()).show()
            );
        }
    }

    public void SaveButton(ImageView saveButton){

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString().trim();
                String content = etEdit.getText().toString().trim();

                if (title.isEmpty()) {
                    toast.GetErrorToast("Please enter a title").show();
                    return;
                }

                new Thread(() -> saveDiaryEntry(title, content)).start();
            }
        });
    }

    /**
     * Checks if the user has changed the title or content.
     * @return true if changes were made, false otherwise.
     */
    private boolean hasChanges() {
        // Get the current text from the EditTexts
        String currentTitle = etTitle.getText().toString();
        String currentContent = etEdit.getText().toString();

        // Avoid a crash if originals haven't loaded yet
        if (originalTitle == null || originalContent == null) {
            return false;
        }

        // Return true if either field is different from its original value
        return !originalTitle.equals(currentTitle) || !originalContent.equals(currentContent);
    }

    /**
     * Sets up the custom logic for the system back button.
     */
    private void setupOnBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasChanges()) {
                    showUnsavedChangesDialog();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };

        // Add the callback to the activity's dispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**
     * Creates and shows the CustomPopupDialogFragment.
     */
    private void showUnsavedChangesDialog() {
        String message = "Are you sure you want to leave without saving?";
        String positiveText = "Leave";
        String negativeText = "Stay";

        // Use the newInstance factory method from your CustomPopupDialogFragment
        CustomPopupDialogFragment popup = CustomPopupDialogFragment.newInstance(message, positiveText, negativeText);

        // Set the listener for the "Leave" button
        popup.setOnPositiveClickListener(new CustomPopupDialogFragment.OnPositiveClickListener() {
            @Override
            public void onPositiveClick() {
                // User confirmed they want to leave, so finish the activity.
                finish();
            }
        });

        // Show the dialog
        popup.show(getSupportFragmentManager(), "UnsavedChangesDialog");
    }

}
