package com.codepath.kathyxing.booknook.adapters.UserSelectionAdapter;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

import com.codepath.kathyxing.booknook.parse_classes.User;

public class UserSelectionDetail extends ItemDetailsLookup.ItemDetails {
    private final int adapterPosition;
    private final User selectionKey;

    public UserSelectionDetail(int adapterPosition, User selectionKey) {
        this.adapterPosition = adapterPosition;
        this.selectionKey = selectionKey;
    }

    @Override
    public int getPosition() {
        return adapterPosition;
    }

    @Nullable
    @Override
    public Object getSelectionKey() {
        return selectionKey;
    }
}
