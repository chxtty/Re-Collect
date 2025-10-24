package com.example.re_collectui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {

    private Context context;
    private Toast toast;
    private View layout;

    LayoutInflater inflater;

    public CustomToast (Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        layout = inflater.inflate(R.layout.custom_toast, null);
        toast = new Toast(context);
    }

    public Toast GetInfoToast(String message) {
        TextView toast_text = layout.findViewById(R.id.toast_text);
        toast_text.setText(message);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        icon.setImageResource(R.drawable.ic_info);
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        return toast;
    }

    public Toast GetDeleteToast(String message) {
        TextView toast_text = layout.findViewById(R.id.toast_text);
        toast_text.setText(message);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        icon.setImageResource(R.drawable.ic_trash);
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        return toast;
    }

    public Toast GetErrorToast(String message) {
        LinearLayout container = layout.findViewById(R.id.custom_layout);
        container.setBackgroundTintList(
                context.getResources().getColorStateList(R.color.pastel_red)
        );
        TextView toast_text = layout.findViewById(R.id.toast_text);
        toast_text.setText(message);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        icon.setImageResource(R.drawable.ic_warning);
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        return toast;
    }

    public Toast GetGreatingToast(String message) {
        LinearLayout container = layout.findViewById(R.id.custom_layout);
        container.setBackgroundTintList(
                context.getResources().getColorStateList(R.color.Pastel_gray_green_darker)
        );
        TextView toast_text = layout.findViewById(R.id.toast_text);
        toast_text.setText(message);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        icon.setImageResource(R.drawable.ic_wave);
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        return toast;
    }




}
