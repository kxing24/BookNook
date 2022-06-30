package com.codepath.kathyxing.booknook.parse_classes;

import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Like")
public class Like extends ParseObject implements Parcelable {
    public static final String KEY_POST = "post";
    public static final String KEY_USER = "user";
    public static final String KEY_USER_ID = "userId";

    public Post getPost() { return (Post) get(KEY_POST); }

    public void setPost(Post post) { put(KEY_POST, post); }

    public User getUser() { return (User) getParseUser(KEY_USER); }

    public void setUser(User user) { put(KEY_USER, user); }

    public String getUserId() { return getString(KEY_USER_ID); }

    public void setUserId(String userId) { put(KEY_USER_ID, userId); }
}
