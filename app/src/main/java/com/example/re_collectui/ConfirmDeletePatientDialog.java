package com.example.re_collectui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ConfirmDeletePatientDialog extends DialogFragment {

    public interface DeletionAuthListener {
        void onAuthenticationSuccess(int patientId, String email, String password, int position);
    }

    private DeletionAuthListener authListener;

    private static final String ARG_PATIENT_ID = "patient_id";
    private static final String ARG_PATIENT_POS = "patient_position";

    public static ConfirmDeletePatientDialog newInstance(int patientId, int position) {
        ConfirmDeletePatientDialog fragment = new ConfirmDeletePatientDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_PATIENT_ID, patientId);
        args.putInt(ARG_PATIENT_POS, position);
        fragment.setArguments(args);
        return fragment;
    }

    // Set the listener from the hosting Activity
    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        try {
            authListener = (DeletionAuthListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DeletionAuthListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Use the new secure dialog layout
        View view = inflater.inflate(R.layout.dialog_confirm_delete_patient, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etEmail = view.findViewById(R.id.etCaregiverEmail);
        EditText etPassword = view.findViewById(R.id.etCaregiverPassword);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirmDelete);

        final int patientId = getArguments().getInt(ARG_PATIENT_ID);
        final int position = getArguments().getInt(ARG_PATIENT_POS);

        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter both email and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pass data back to the activity for server-side authentication
            if (authListener != null) {
                authListener.onAuthenticationSuccess(patientId, email, password, position);
            }
            dismiss();
        });

        return view;
    }
}