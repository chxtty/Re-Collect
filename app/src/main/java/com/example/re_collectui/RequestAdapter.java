package com.example.re_collectui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    public interface OnItemClickListener {
        void onClick(RequestItem requestItem); // for going to request edit page
    }
    private final List<RequestItem> requests;
    private final OnItemClickListener listener;

    public RequestAdapter(List<RequestItem> requests, OnItemClickListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RequestItem item = requests.get(position);

        holder.txtName.setText(item.getName());
        holder.txtAuthor.setText("Request by " + item.getAuthor());

        if (item.getType() == RequestItem.RequestType.ACTIVITY) {
            holder.imgType.setImageResource(R.drawable.activity_icon);
        } else {
            holder.imgType.setImageResource(R.drawable.community);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView imgType;
        TextView txtName, txtAuthor;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            imgType = itemView.findViewById(R.id.imgType);
            txtName = itemView.findViewById(R.id.txtName);
            txtAuthor = itemView.findViewById(R.id.txtAuthor);
        }
    }
}
