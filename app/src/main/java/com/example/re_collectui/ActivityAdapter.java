package com.example.re_collectui;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<Activity> activityList;

    public ActivityAdapter(List<Activity> activityList) {
        this.activityList = activityList;
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
        String act = "";
        // Display date + start-end time
        switch(activity.getActivityId()){
            case 1: act = "Jogging"; holder.imgIcon.setImageResource(R.drawable.jogging_icon);
                break;
            case 2: act = "Swimming"; holder.imgIcon.setImageResource(R.drawable.swimming_icon);
                break;
            case 3: act = "Painting"; holder.imgIcon.setImageResource(R.drawable.painting_icon);
                break;
            default: act = "Unknown"; holder.imgIcon.setImageResource(R.drawable.activity_icon_row);
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

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}

