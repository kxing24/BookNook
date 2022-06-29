package com.codepath.kathyxing.booknook.parse_classes;

import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Friend")
public class Friend extends ParseObject implements Parcelable {
    public static final String KEY_REQUESTING_USER = "requestingUser";
    public static final String KEY_RECEIVING_USER = "receivingUser";
    public static final String KEY_REQUESTING_USER_ID = "requestingUserId";
    public static final String KEY_RECEIVING_USER_ID = "receivingUserId";
    public static final String KEY_ACCEPTED = "accepted";

    public User getRequestingUser() { return (User) getParseUser(KEY_REQUESTING_USER); }

    public void setRequestingUser(User user) { put(KEY_REQUESTING_USER, user); }

    public User getReceivingUser() { return (User) getParseUser(KEY_RECEIVING_USER); }

    public void setReceivingUser(User user) { put(KEY_RECEIVING_USER, user); }

    public String getRequestingUserId() { return getString(KEY_REQUESTING_USER_ID); }

    public void setRequestingUserId(String userId) { put(KEY_REQUESTING_USER_ID, userId); }

    public String getReceivingUserId() { return getString(KEY_RECEIVING_USER_ID); }

    public void setReceivingUserId(String userId) { put(KEY_RECEIVING_USER_ID, userId); }

    public Boolean getAccepted() { return getBoolean(KEY_ACCEPTED); }

    public void setAccepted(Boolean accepted) { put(KEY_ACCEPTED, accepted); }
}
