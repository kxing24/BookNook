package com.codepath.kathyxing.booknook.parse_classes;

import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("BookRecommendation")
public class BookRecommendation extends ParseObject implements Parcelable {
    public static final String KEY_GENRE = "genre";
    public static final String KEY_BOOK_ID = "bookId";

    public String getGenre() { return getString(KEY_GENRE); }

    public void setGenre(String genre) { put(KEY_GENRE, genre); }

    public String getBookId() { return getString(KEY_BOOK_ID); }

    public void setBookId(String bookId) { put(KEY_BOOK_ID, bookId); }
}
