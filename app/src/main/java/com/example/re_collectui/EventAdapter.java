package com.example.re_collectui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;
    private int expandedPosition = -1;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Set text content
        holder.eventTitle.setText(event.getTitle());
        holder.eventDates.setText(event.getStartDate() + " - " + event.getEndDate());
        holder.eventLocation.setText(event.getLocation());

        // Handle expand/collapse visibility
        holder.expandableLayout.setVisibility(event.isExpanded() ? View.VISIBLE : View.GONE);

        // Handle item click to expand/collapse
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Event clickedEvent = eventList.get(pos);

                boolean isExpanded = !clickedEvent.isExpanded();

                // Collapse all
                for (int i = 0; i < eventList.size(); i++) {
                    eventList.get(i).setExpanded(false);
                }

                // Expand the clicked one
                clickedEvent.setExpanded(isExpanded);

                notifyDataSetChanged();
            }
        });

        // TODO: Add click listeners for edit/delete here
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDates, eventLocation;
        ImageButton editButton, deleteButton;
        LinearLayout expandableLayout;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDates = itemView.findViewById(R.id.eventDates);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
        }
    }
}
