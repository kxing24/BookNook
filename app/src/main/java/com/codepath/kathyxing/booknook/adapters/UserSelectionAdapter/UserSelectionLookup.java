package com.codepath.kathyxing.booknook.adapters.UserSelectionAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;

import com.codepath.kathyxing.booknook.adapters.UserSelectionAdapter.UserSelectionAdapter;

public class UserSelectionLookup extends ItemDetailsLookup {

    private final RecyclerView recyclerView;

    public UserSelectionLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails getItemDetails(@NonNull MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof UserSelectionAdapter.UserViewHolder) {
                return ((UserSelectionAdapter.UserViewHolder) viewHolder).getItemDetails();
            }
        }

        return null;
    }
}