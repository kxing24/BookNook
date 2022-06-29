package com.codepath.kathyxing.booknook.adapters.UserSelectionAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codepath.kathyxing.booknook.parse_classes.User;

import java.util.List;

import androidx.recyclerview.selection.ItemKeyProvider;

public class UserSelectionKeyProvider extends ItemKeyProvider {
    private final List<User> itemList;

    public UserSelectionKeyProvider(int scope, List<User> itemList) {
        super(scope);
        this.itemList = itemList;
    }

    @Nullable
    @Override
    public Object getKey(int position) {
        return itemList.get(position);
    }

    @Override
    public int getPosition(@NonNull Object key) {
        return itemList.indexOf(key);
    }
}