package com.example.re_collectui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.MyViewHolder> implements Filterable {
    Context  context;
    ArrayList<Entry> entryList;
    ArrayList<Entry> entryListFull;

    public EntryAdapter(Context context, ArrayList<Entry> entryList){
        this.context = context;
        this.entryList = entryList;
        entryListFull = new ArrayList<>(entryList);
        setHasStableIds(false);
    }

    public void replaceData(List<Entry> newData) {
        entryList.clear();
        entryList.addAll(newData);
        entryListFull.clear();
        entryListFull.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This is where you inflate the layout (Giving a look to our rows)
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_entry_row, parent, false);
        return new EntryAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryAdapter.MyViewHolder holder, int position) {
        //assigning values to the views we created in the recycler_view_row layout file
        //based on the position of the recycler view
        holder.tvTitle.setText(entryList.get(position).getTitle());
        holder.tvDate.setText(entryList.get(position).getDate());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewEntry.class);
            Entry entry = entryList.get(position);
            intent.putExtra("entryId", entry.getEntryId());
            intent.putExtra("date", entry.getDate());
            intent.putExtra("title", entry.getTitle());
            intent.putExtra("content", entry.getContent());
            intent.putExtra("author", entry.getAuthor());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        //the recycler view just wants to know the number of items
        return entryList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //grabbing the views from our recycler_view_row layout file
        //kinda like in the onCreate method

        TextView tvDate;
        TextView tvTitle;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);


        }
    }


    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter(){
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Entry> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(entryListFull);
            }
            else {
                String query = constraint.toString().toLowerCase().trim();
                for (Entry entry : entryListFull) {
                    String title = entry.getTitle() != null ? entry.getTitle().toLowerCase() : "";
                    String content = entry.getContent() != null ? entry.getContent().toLowerCase() : "";
                    String date = entry.getDate() != null ? entry.getDate().toLowerCase() : "";
                    if (title.contains(query) || content.contains(query) || date.contains(query)) {
                        filteredList.add(entry);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            entryList.clear();
            entryList.addAll((List<Entry>) results.values);
            notifyDataSetChanged();
        }


    };


}
