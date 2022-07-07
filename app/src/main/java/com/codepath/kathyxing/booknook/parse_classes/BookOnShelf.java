package com.codepath.kathyxing.booknook.parse_classes;

import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("BookOnShelf")
public class BookOnShelf extends ParseObject implements Parcelable {
    public static final String KEY_SHELF = "shelf";
    public static final String KEY_USER = "user";
    public static final String KEY_BOOK_ID = "bookId";
    public static final String KEY_BOOK_TITLE = "bookTitle";
    public static final String KEY_BOOK_AUTHOR = "bookAuthor";
    public static final String KEY_BOOK_COVER_URL = "bookCoverUrl";
    public static final String KEY_SHELF_ID = "shelfId";

    public Shelf getShelf() { return (Shelf) get(KEY_SHELF); }

    public void setShelf(Shelf shelf) { put(KEY_SHELF, shelf); }

    public User getUser() { return (User) getParseUser(KEY_USER); }

    public void setUser(User user) { put(KEY_USER, user); }

    public String getBookId() { return getString(KEY_BOOK_ID); }

    public void setBookId(String bookId) { put(KEY_BOOK_ID, bookId); }

    public String getBookTitle() { return getString(KEY_BOOK_TITLE); }

    public void setBookTitle(String bookTitle) { put(KEY_BOOK_TITLE, bookTitle); }

    public String getBookAuthor() { return getString(KEY_BOOK_AUTHOR); }

    public void setBookAuthor(String bookAuthor) { put(KEY_BOOK_AUTHOR, bookAuthor); }

    public String getBookCoverUrl() { return getString(KEY_BOOK_COVER_URL); }

    public void setBookCoverUrl(String bookCoverUrl) { put(KEY_BOOK_COVER_URL, bookCoverUrl); }

    public String getShelfId() { return getString(KEY_SHELF_ID); }

    public void setShelfId(String shelfId) { put(KEY_SHELF_ID, shelfId); }
}
