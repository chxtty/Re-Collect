package com.example.re_collectui;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<Activity> activityList;


    public ActivityAdapter(List<Activity> activityList) {
        this.activityList = activityList;
    }
    public void replaceData(List<Activity> newData) {
        this.activityList.clear();
        this.activityList.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_activity_row, parent, false); // <-- use your row layout filename
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        String base64Icon = activity.getActIconBase64();
        int detailId = activity.getDetailId();
        String act = "";

        holder.expandableLayout.setVisibility(activity.isExpanded() ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> {
            activity.setExpanded(!activity.isExpanded());
            notifyItemChanged(position);
        });
        holder.tvDuration.setText(activity.getStartTime() +" | " + activity.getEndTime());
        holder.imgDelete.setOnClickListener(e -> {
            Context context = e.getContext();

            if (context instanceof AppCompatActivity) {
                AppCompatActivity activityContext = (AppCompatActivity) context;

                CustomPopupDialogFragment dialog = CustomPopupDialogFragment.newInstance(
                        "Are you sure you want to delete this activity?",
                        "Yes",
                        "No"
                );

                dialog.setOnPositiveClickListener(() -> {
                    deleteActivity(context, detailId);
                    activityList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                });

                dialog.show(activityContext.getSupportFragmentManager(), "DeleteActivityDialog");
            }
        });

        holder.imgEdit.setOnClickListener(e -> {
            Context context = e.getContext();
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activityContext = (AppCompatActivity) context;

                // Create a dialog instance using the new factory method for editing
                ActivityDialog dialog = ActivityDialog.newInstance(
                        activity.getDetailId(),
                        activity.getActivityId(),
                        activity.getActDate(),
                        activity.getStartTime(),
                        activity.getEndTime()
                );

                dialog.show(activityContext.getSupportFragmentManager(), "EditActivityDialog");
            }
        });

        if (base64Icon != null && !base64Icon.isEmpty()) {
            byte[] decodedString = android.util.Base64.decode(base64Icon, android.util.Base64.DEFAULT);
            Bitmap iconBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imgIcon.setImageBitmap(iconBitmap);
        } else {
            holder.imgIcon.setImageResource(R.drawable.activity_icon_row); // fallback
        }



        switch(activity.getActivityId()){
            case 1: act = "Jogging";
                break;
            case 2: act = "Swimming";
                break;
            case 3: act = "Painting";
                break;
            default: act = "Unknown";
        }

        String info = activity.getActDate() + "   |   " + act;
        holder.tvInfo.setText(info);
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView tvInfo;
        ImageView imgIcon;
        LinearLayout expandableLayout;
        TextView tvDuration;
        Button btnDelete;
        ImageView imgEdit, imgDelete;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
    private void deleteActivity(Context context, int detailId) {
        String url = "http://100.79.152.109/android/api.php?action=delete_activity";
        CustomToast toast = new CustomToast(context);

        Map<String, Integer> params = new HashMap<>();
        params.put("detailId", detailId);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");
                        toast.GetDeleteToast("Activity deleted!").show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast.GetErrorToast("JSON parse error").show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    toast.GetErrorToast("Network error: " + error.getMessage()).show();
                });

        Volley.newRequestQueue(context).add(request);
    }


}

