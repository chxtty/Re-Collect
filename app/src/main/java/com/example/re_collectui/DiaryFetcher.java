package com.example.re_collectui;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class DiaryFetcher {

    private static final String API_URL = "http://100.104.224.68/android/api.php?action=get_diary_entry";
    private final RequestQueue requestQueue;

    public DiaryFetcher(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public interface DiaryEntryCallback {
        void onSuccess(DiaryEntry entry);
        void onError(String errorMessage);
    }

    public static class DiaryEntry {
        public int entryId;
        public String diaryTitle;
        public String content;
        public String author;
        public String diaryDate;

        public DiaryEntry(int entryId, String diaryTitle, String content, String author, String diaryDate) {
            this.entryId = entryId;
            this.diaryTitle = diaryTitle;
            this.content = content;
            this.author = author;
            this.diaryDate = diaryDate;
        }
    }

    public void getDiaryEntry(int entryId, final DiaryEntryCallback callback) {
        String url = API_URL + "&entryId=" + entryId;

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    Log.d("DiaryFetcher", "Raw Response: " + response);

                    try {
                        // Try to parse JSON
                        JSONObject json = new JSONObject(response.trim());

                        if (json.optString("status").equals("success")) {
                            JSONObject entryJson = json.getJSONObject("entry");

                            DiaryEntry entry = new DiaryEntry(
                                    entryJson.optInt("entryID"),
                                    entryJson.optString("diaryTitle"),
                                    entryJson.optString("content"),
                                    entryJson.optString("author"),
                                    entryJson.optString("diaryDate")
                            );

                            callback.onSuccess(entry);
                        } else {
                            callback.onError(json.optString("message", "Unknown error"));
                        }
                    } catch (JSONException e) {
                        // Instead of crashing → act like your saveDiaryEntry and just return raw text
                        callback.onError("Raw response (not JSON): " + response);
                    }
                },
                error -> callback.onError("Volley error: " + error.toString())
        );

        requestQueue.add(stringRequest);
    }
}
