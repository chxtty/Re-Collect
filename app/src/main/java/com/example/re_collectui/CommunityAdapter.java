package com.example.re_collectui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.MyViewHolder> implements Filterable {

    private Context context;
    private ArrayList<Community_Member> memberList;
    private ArrayList<Community_Member> memberListFull;
    private boolean isCaregiver;

    // ✅ THIS IS THE ONLY CONSTRUCTOR THAT SHOULD EXIST.
    // It forces the calling activity to specify the user's role.
    public CommunityAdapter(Context context, ArrayList<Community_Member> memberList, boolean isCaregiver) {
        this.context = context;
        this.memberList = memberList;
        this.memberListFull = new ArrayList<>(memberList);
        this.isCaregiver = isCaregiver; // Set the role for this adapter instance
        Toast.makeText(context, "2. Adapter Created: Role isCaregiver = " + this.isCaregiver, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_community_member, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Community_Member member = memberList.get(position);
        holder.tvMemberName.setText(member.getCommFirstName() + " " + member.getCommLastName());
        holder.tvMemberType.setText(member.getCommType());

        String fullImageUrl = "http://100.104.224.68/android/" + member.getCommImage();
        Glide.with(context)
                .load(fullImageUrl)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(holder.ivMemberImage);

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "3. Adapter Click: Sending isCaregiver = " + this.isCaregiver, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, ViewCommunityMember.class);
            intent.putExtra("commID", member.getCommID());
            // This now correctly passes the role it received from the constructor
            intent.putExtra("isCaregiver", this.isCaregiver);
            context.startActivity(intent);
        });
    }

    // ... The rest of your adapter code (getItemCount, MyViewHolder, Filter, replaceData) is fine ...
    public void replaceData(List<Community_Member> newData) { memberList.clear(); memberList.addAll(newData); memberListFull.clear(); memberListFull.addAll(newData); notifyDataSetChanged(); }
    @Override public int getItemCount() { return memberList.size(); }
    public static class MyViewHolder extends RecyclerView.ViewHolder { CircleImageView ivMemberImage; TextView tvMemberName; TextView tvMemberType; public MyViewHolder(@NonNull View itemView) { super(itemView); ivMemberImage = itemView.findViewById(R.id.tvImage); tvMemberName = itemView.findViewById(R.id.tvName); tvMemberType = itemView.findViewById(R.id.tvType); } }
    @Override public Filter getFilter() { return memberFilter; }
    private final Filter memberFilter = new Filter() { @Override protected FilterResults performFiltering(CharSequence constraint) { List<Community_Member> filteredList = new ArrayList<>(); if (constraint == null || constraint.length() == 0) { filteredList.addAll(memberListFull); } else { String query = constraint.toString().toLowerCase().trim(); for (Community_Member member : memberListFull) { String fName = member.getCommFirstName() != null ? member.getCommFirstName().toLowerCase() : ""; String lName = member.getCommLastName() != null ? member.getCommLastName().toLowerCase() : ""; String type = member.getCommType() != null ? member.getCommType().toLowerCase() : ""; if (fName.contains(query) || lName.contains(query) || type.contains(query)) { filteredList.add(member); } } } FilterResults results = new FilterResults(); results.values = filteredList; return results; } @Override protected void publishResults(CharSequence constraint, FilterResults results) { memberList.clear(); memberList.addAll((List<Community_Member>) results.values); notifyDataSetChanged(); } };
}