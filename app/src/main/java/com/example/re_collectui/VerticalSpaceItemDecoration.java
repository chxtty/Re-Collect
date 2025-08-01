package com.example.re_collectui;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
//for the gaps between elements in the recyclerview
public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int verticalSpaceHeight;

    public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
        this.verticalSpaceHeight = verticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // Add top spacing only to items *after* the first one
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = verticalSpaceHeight;
        }
    }
}