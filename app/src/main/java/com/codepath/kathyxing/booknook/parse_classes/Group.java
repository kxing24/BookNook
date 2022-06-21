package com.codepath.kathyxing.booknook.parse_classes;

import android.os.Parcelable;

import com.codepath.kathyxing.booknook.models.Book;
import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Group")
public class Group extends ParseObject implements Parcelable {
    public static final String KEY_BOOK_ID = "bookId";

    public String getBookId() { return getString(KEY_BOOK_ID); }

    public void setBookId(String id) { put(KEY_BOOK_ID, id); }
}
