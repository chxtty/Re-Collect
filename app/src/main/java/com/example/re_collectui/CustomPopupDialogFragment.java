package com.example.re_collectui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView; // Import TextView
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CustomPopupDialogFragment extends DialogFragment {

    public interface OnPositiveClickListener {
        void onPositiveClick();
    }

    private OnPositiveClickListener positiveClickListener;

    // 1. Define keys for the arguments
    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_POSITIVE_TEXT = "arg_positive_text";
    private static final String ARG_NEGATIVE_TEXT = "arg_negative_text";

    public void setOnPositiveClickListener(OnPositiveClickListener listener) {
        this.positiveClickListener = listener;
    }

    // 2. Create a static newInstance factory method
    public static CustomPopupDialogFragment newInstance(String message, String positiveButtonText, String negativeButtonText) {
        CustomPopupDialogFragment fragment = new CustomPopupDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_POSITIVE_TEXT, positiveButtonText);
        args.putString(ARG_NEGATIVE_TEXT, negativeButtonText);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_custom_popup, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 3. Get the views from the layout
        TextView tvMessage = view.findViewById(R.id.dialog_message);
        Button btnNegative = view.findViewById(R.id.btnNegative);
        Button btnPositive = view.findViewById(R.id.btnPositive);

        // 4. Retrieve arguments and set the text
        if (getArguments() != null) {
            tvMessage.setText(getArguments().getString(ARG_MESSAGE));
            btnPositive.setText(getArguments().getString(ARG_POSITIVE_TEXT));
            btnNegative.setText(getArguments().getString(ARG_NEGATIVE_TEXT));
        }

        // Setup button listeners
        btnNegative.setOnClickListener(v -> dismiss());
        btnPositive.setOnClickListener(v -> {
            if (positiveClickListener != null) {
                positiveClickListener.onPositiveClick();
            }
            dismiss();
        });

        return view;
    }
}