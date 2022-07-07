package com.codepath.kathyxing.booknook.parse_classes;

import android.os.Parcelable;

import com.codepath.kathyxing.booknook.models.Book;
import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Group")
public class Group extends ParseObject implements Parcelable {
    public static final String KEY_BOOK_ID = "bookId";
    public static final String KEY_GROUP_NAME = "groupName";
    public static final String KEY_RECOMMENDED_BOOK_ID = "recommendedBookId";

    public String getBookId() { return getString(KEY_BOOK_ID); }

    public void setBookId(String id) { put(KEY_BOOK_ID, id); }

    public String getGroupName() { return getString(KEY_GROUP_NAME); }

    public void setGroupName(String groupName) { put(KEY_GROUP_NAME, groupName); }

    public String getRecommendedBookId() { return getString(KEY_RECOMMENDED_BOOK_ID); }

    public void setRecommendedBookId(String id) { put(KEY_RECOMMENDED_BOOK_ID, id); }
}
