package com.example.re_collectui;

import android.content.Context;
import android.content.Intent;
import android.util.Base64; // ✅ ADD THIS IMPORT
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PatientAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int VIEW_TYPE_PATIENT = 0;
    private static final int VIEW_TYPE_ADD = 1;

    Context context;
    ArrayList<Patient> patientList;
    ArrayList<Patient> patientListFull;
    private OnPatientDeleteListener deleteListener;

    public interface OnPatientDeleteListener {
        void onDeleteClick(Patient patient, int position);
    }

    public PatientAdapter(Context context, ArrayList<Patient> patientList, OnPatientDeleteListener listener) {
        this.context = context;
        this.patientList = patientList;
        this.patientListFull = new ArrayList<>(patientList);
        this.deleteListener = listener;
        setHasStableIds(false);
    }

    public static class AddPatientViewHolder extends RecyclerView.ViewHolder {
        public AddPatientViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName;
        TextView tvPatientIdentifier;
        ImageButton DeleteBtn;
        CircleImageView ivPatientImage;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvName);
            tvPatientIdentifier = itemView.findViewById(R.id.tvID);
            DeleteBtn = itemView.findViewById(R.id.deleteButton);
            ivPatientImage = itemView.findViewById(R.id.patientImageView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == patientList.size()) {
            return VIEW_TYPE_ADD;
        }
        return VIEW_TYPE_PATIENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_ADD) {
            View view = inflater.inflate(R.layout.item_add_patient, parent, false);
            return new AddPatientViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.recycler_patient_row, parent, false);
            return new PatientViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ADD) {
            AddPatientViewHolder addHolder = (AddPatientViewHolder) holder;
            addHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, CreatePatient.class);
                context.startActivity(intent);
            });
        } else {
            PatientViewHolder patientHolder = (PatientViewHolder) holder;
            Patient currentPatient = patientList.get(position);

            String fullName = currentPatient.getFirstName() + " " + currentPatient.getLastName();
            String patientIdString = "ID: " + currentPatient.getPatientID();

            patientHolder.tvPatientName.setText(fullName);
            patientHolder.tvPatientIdentifier.setText(patientIdString);

            // --- THIS IS THE FIX ---
            // The getImage() method now returns a Base64 string.
            String base64Image = currentPatient.getImage();

            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    // Decode the Base64 string into a byte array
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);

                    // Load the byte array directly with Glide
                    Glide.with(context)
                            .load(decodedString)
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(patientHolder.ivPatientImage);
                } catch (IllegalArgumentException e) {
                    // If the Base64 string is corrupted, show the default avatar
                    patientHolder.ivPatientImage.setImageResource(R.drawable.default_avatar);
                }
            } else {
                // If there is no image string, show the default avatar
                patientHolder.ivPatientImage.setImageResource(R.drawable.default_avatar);
            }
            // --- END FIX ---

            patientHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ViewPatient.class);
                intent.putExtra("patientID", currentPatient.getPatientID());
                context.startActivity(intent);
            });

            patientHolder.DeleteBtn.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    deleteListener.onDeleteClick(patientList.get(currentPosition), currentPosition);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return patientList.size() + 1;
    }

    public void replaceData(List<Patient> newData) {
        patientList.clear();
        patientList.addAll(newData);
        patientListFull.clear();
        patientListFull.addAll(newData);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < patientList.size()) {
            Patient patientToRemove = patientList.get(position);
            patientList.remove(position);
            patientListFull.remove(patientToRemove);
            notifyItemRemoved(position);
        }
    }

    @Override
    public Filter getFilter() {
        return patientFilter;
    }
    private final Filter patientFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Patient> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(patientListFull);
            } else {
                String query = constraint.toString().toLowerCase().trim();
                for (Patient patient : patientListFull) {
                    String firstName = patient.getFirstName() != null ? patient.getFirstName().toLowerCase() : "";
                    String lastName = patient.getLastName() != null ? patient.getLastName().toLowerCase() : "";
                    String diagnosis = patient.getDiagnosis() != null ? patient.getDiagnosis().toLowerCase() : "";
                    String patientID = String.valueOf(patient.getPatientID());

                    if (firstName.contains(query) || lastName.contains(query) || diagnosis.contains(query) || patientID.contains(query)) {
                        filteredList.add(patient);
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
            patientList.clear();
            patientList.addAll((List<Patient>) results.values);
            notifyDataSetChanged();
        }
    };
}