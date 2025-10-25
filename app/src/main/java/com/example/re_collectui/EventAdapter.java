package com.example.re_collectui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    private int expandedPosition = RecyclerView.NO_POSITION;
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
        if (event.getAllDay()){
            holder.eventDates.setText(event.getStartDate());
        } else {
            holder.eventDates.setText(event.getStartDate() + " - " + event.getEndDate());
        }
        holder.eventLocation.setText(event.getLocation());
        holder.eventDescription.setText(event.getDescription());


        holder.expandableLayout.setVisibility(position == expandedPosition ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int oldExpanded = expandedPosition;
            if (oldExpanded == position) {
                expandedPosition = RecyclerView.NO_POSITION;
            } else {
                expandedPosition = position;
            }

            android.transition.TransitionSet transitionSet = new android.transition.TransitionSet()
                    .addTransition(new AutoTransition())
                    .addTransition(new Fade(Fade.IN))
                    .addTransition(new Fade(Fade.OUT))
                    .setDuration(300);

            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView.getParent(), transitionSet);

            if (oldExpanded != RecyclerView.NO_POSITION)
                notifyItemChanged(oldExpanded);
            notifyItemChanged(position);
        });

        holder.deleteButton.setOnClickListener(v -> showDeleteDialog(event));

        holder.editButton.setOnClickListener(v -> {
            showEditEventDialog(event);
        });
    }

    private void showDeleteDialog(Event event) {
        CustomPopupDialogFragment dialog = CustomPopupDialogFragment.newInstance(
                "Are you sure you want to delete this event?",
                "Yes",
                "Cancel"
        );
        dialog.setOnPositiveClickListener(() -> deleteEventFromServer(event));
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "DeleteEventDialog");
    }

    private void showEditEventDialog(Event event) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.event_create_dialog);
        CustomToast toast1 = new CustomToast(context);


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

        if(event.getAllDay()){
            endDate.setEnabled(false);
            endDate.setAlpha(0.5f);
            endDate.setText(event.getStartDate());
        }

        allDay.setOnCheckedChangeListener((v,b) -> {
            if (b){
                endDate.setEnabled(false);
                endDate.setAlpha(0.5f);
            } else {
                endDate.setEnabled(true);
                endDate.setAlpha(1.0f);
            }
        });

        btnCreate.setText("Update");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String updatedTitle = title.getText().toString().trim();
            String updatedStart = startDate.getText().toString().trim();
            String updatedEnd = endDate.getText().toString().trim();
            boolean updatedAllDay = allDay.isChecked();
            String updatedLocation = location.getText().toString().trim();
            String updatedDesc = description.getText().toString().trim();

            if (!updatedAllDay) {
                updatedEnd = endDate.getText().toString().trim();

            } else {
                updatedEnd = updatedStart;
            }


            if (updatedTitle.isEmpty() || updatedStart.isEmpty() || updatedEnd.isEmpty()) {
                toast1.GetErrorToast("Please fill in required fields").show();
                return;
            }

            if (updatedStart.compareTo(updatedEnd) > 0) {
                toast1.GetErrorToast( "Start date must be before the End date").show();
                return;
            }

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

        dialog.getWindow().setLayout((int) (context.getResources().getDisplayMetrics().widthPixels *0.9),
                RecyclerView.LayoutParams.WRAP_CONTENT);
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
        String url = GlobalVars.apiPath + "update_event";
        CustomToast toast = new CustomToast(context);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            toast.GetInfoToast("Event updated").show();
                            Event updatedEvent = new Event(eventID, title, startDate, endDate, description, location, allDay);
                            updateEventInList(updatedEvent);
                        } else {
                            // Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                       // Toast.makeText(context, "Error parsing update response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> toast.GetErrorToast("Update failed").show()
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
        String url = GlobalVars.apiPath + "delete_event";

        CustomToast toast = new CustomToast(context);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            eventList.removeIf(e -> e.getEventID() == eventToDelete.getEventID());
                            searchList.removeIf(e -> e.getEventID() == eventToDelete.getEventID());
                            notifyDataSetChanged();
                            toast.GetInfoToast("Event deleted").show();
                        } else {
                           // Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // Toast.makeText(context, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> toast.GetErrorToast("Delete failed").show()
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

        if (currentQuery.isEmpty() || event.getTitle().toLowerCase(Locale.getDefault()).contains(currentQuery.toLowerCase(Locale.getDefault()))) {
            searchList.add(event);
            notifyItemInserted(searchList.size() - 1);
        } else {
            notifyDataSetChanged();
        }
    }

    public void deleteEvent(Event eventToDelete) {
        Iterator<Event> iterator = eventList.iterator();
        while (iterator.hasNext()) {
            Event e = iterator.next();
            if (e.getEventID() == eventToDelete.getEventID()) {
                iterator.remove();
                break;
            }
        }

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
