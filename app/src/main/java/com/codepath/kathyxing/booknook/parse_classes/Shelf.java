package com.codepath.kathyxing.booknook.parse_classes;

import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;

@ParseClassName("Shelf")
public class Shelf extends ParseObject implements Parcelable {
    public static final String KEY_USER = "user";
    public static final String KEY_SHELF_NAME = "shelfName";
    public static final String KEY_BOOKS = "books";

    public User getUser() { return (User) getParseUser(KEY_USER); }

    public void setUser(User user) { put(KEY_USER, user); }

    public String getShelfName() { return getString(KEY_SHELF_NAME); }

    public void setShelfName(String shelfName) { put(KEY_SHELF_NAME, shelfName); }

    public JSONArray getBooks() { return getJSONArray(KEY_BOOKS); }

    public void setBooks(JSONArray books) { put(KEY_BOOKS, books); }
}
