package com.example.re_collectui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> implements Filterable {

    private List<Event> eventList;
    private List<Event> searchList;
    private Context context;
    private int expandedPosition = -1;
    private String currentQuery = "";

    public EventAdapter(Context context, List<Event> eventList, List<Event> searchList) {
        this.context = context;
        this.eventList = eventList;
        this.searchList = searchList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = searchList.get(position);

        holder.eventTitle.setText(event.getTitle());
        holder.eventDates.setText(event.getStartDate() + " - " + event.getEndDate());
        holder.eventLocation.setText(event.getLocation());
        holder.eventDescription.setText(event.getDescription());


        holder.expandableLayout.setVisibility(event.isExpanded() ? View.VISIBLE : View.GONE);


        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Event clickedEvent = searchList.get(holder.getAdapterPosition());
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

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deleteEventFromServer(event);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        holder.editButton.setOnClickListener(v -> {
            showEditEventDialog(event);
        });
    }

    private void showEditEventDialog(Event event) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.event_create_dialog);

        EditText title = dialog.findViewById(R.id.editTitle);
        EditText startDate = dialog.findViewById(R.id.editStartDate);
        EditText endDate = dialog.findViewById(R.id.editEndDate);
        CheckBox allDay = dialog.findViewById(R.id.checkBox);
        EditText location = dialog.findViewById(R.id.editLocation);
        EditText description = dialog.findViewById(R.id.editDescription);

        Button btnCreate = dialog.findViewById(R.id.btnCreate);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);


        title.setText(event.getTitle());
        startDate.setText(event.getStartDate());
        endDate.setText(event.getEndDate());
        allDay.setChecked(event.getAllDay());
        location.setText(event.getLocation());
        description.setText(event.getDescription());
        startDate.setOnClickListener(v -> showDatePicker(startDate));
        endDate.setOnClickListener(v -> showDatePicker(endDate));

        btnCreate.setText("Update");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String updatedTitle = title.getText().toString().trim();
            String updatedStart = startDate.getText().toString().trim();
            String updatedEnd = endDate.getText().toString().trim();
            boolean updatedAllDay = allDay.isChecked();
            String updatedLocation = location.getText().toString().trim();
            String updatedDesc = description.getText().toString().trim();

            updateEventOnServer(
                    event.getEventID(),
                    updatedTitle,
                    updatedStart,
                    updatedEnd,
                    updatedAllDay,
                    updatedLocation,
                    updatedDesc
            );
            dialog.dismiss();
        });

        dialog.show();
    }


    private void showDatePicker(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    targetEditText.setText(date);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void updateEventInList(Event updatedEvent) {
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getEventID() == updatedEvent.getEventID()) {
                eventList.set(i, updatedEvent);
                break;
            }
        }
        for (int i = 0; i < searchList.size(); i++) {
            if (searchList.get(i).getEventID() == updatedEvent.getEventID()) {
                searchList.set(i, updatedEvent);
                break;
            }
        }
        notifyDataSetChanged();
    }

    private void updateEventOnServer(int eventID, String title, String startDate, String endDate, boolean allDay, String location, String description) {
        String url = "http://10.0.2.2/recollect/api.php?action=update_event";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            Toast.makeText(context, "Event updated", Toast.LENGTH_SHORT).show();
                            Event updatedEvent = new Event(eventID, title, startDate, endDate, description, location, allDay);
                            updateEventInList(updatedEvent);
                        } else {
                            Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(context, "Error parsing update response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("eventID", String.valueOf(eventID));
                params.put("title", title);
                params.put("startDate", startDate);
                params.put("endDate", endDate);
                params.put("allDay", allDay ? "1" : "0");
                params.put("location", location);
                params.put("description", description);
                return params;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    private void deleteEventFromServer(Event eventToDelete) {
        String url = "http://10.0.2.2/recollect/api.php?action=delete_event";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            eventList.removeIf(e -> e.getEventID() == eventToDelete.getEventID());
                            searchList.removeIf(e -> e.getEventID() == eventToDelete.getEventID());
                            notifyDataSetChanged();
                            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(context, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("eventID", String.valueOf(eventToDelete.getEventID()));
                return params;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    @Override
    public Filter getFilter() {
        return eventFilter;
    }

    private final Filter eventFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Event> filtered = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filtered.addAll(eventList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Event event : eventList) {
                    if (event.getTitle().toLowerCase().contains(filterPattern) ||
                            event.getLocation().toLowerCase().contains(filterPattern) ||
                            event.getDescription().toLowerCase().contains(filterPattern)) {
                        filtered.add(event);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filtered;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            searchList.clear();
            searchList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public void filter(String query) {
        currentQuery = query.toLowerCase(Locale.getDefault());
        searchList.clear();

        if (currentQuery.isEmpty()) {
            searchList.addAll(eventList);
        } else {
            for (Event event : eventList) {
                if (event.getTitle().toLowerCase(Locale.getDefault()).contains(currentQuery)) {
                    searchList.add(event);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void addEvent(Event event, String currentQuery) {
        eventList.add(event);

        // Update the filtered list according to current search
        if (currentQuery.isEmpty() || event.getTitle().toLowerCase(Locale.getDefault()).contains(currentQuery.toLowerCase(Locale.getDefault()))) {
            searchList.add(event);
            notifyItemInserted(searchList.size() - 1);
        } else {
            // If event does not match filter, just notify that data changed for filtering
            notifyDataSetChanged();
        }
    }

    public void deleteEvent(Event eventToDelete) {
        // Remove from full list
        Iterator<Event> iterator = eventList.iterator();
        while (iterator.hasNext()) {
            Event e = iterator.next();
            if (e.getEventID() == eventToDelete.getEventID()) {
                iterator.remove();
                break;
            }
        }

        // Re-filter the search list to reflect removal
        filter(currentQuery);
    }

    public void updateEvents(List<Event> newEvents) {
        eventList.clear();
        eventList.addAll(newEvents);
        searchList.clear();
        searchList.addAll(newEvents);
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDates, eventLocation, eventDescription;
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
            eventDescription = itemView.findViewById(R.id.eventDescription);
        }
    }
}
