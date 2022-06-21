package com.codepath.kathyxing.booknook.parse_classes;

import android.os.Parcelable;

import com.codepath.kathyxing.booknook.models.Book;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Member")
public class Member extends ParseObject implements Parcelable {
    public static final String KEY_FROM = "from";
    public static final String KEY_TO = "to";

    public User getFrom() { return (User) getParseUser(KEY_FROM); }

    public void setFrom(User user) { put(KEY_FROM, user); }

    public Group getTo() { return (Group) getParseObject(KEY_TO); }

    public void setTo(Group group) { put(KEY_TO, group); }
}
