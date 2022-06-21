package com.codepath.kathyxing.booknook.parse_classes;

import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject implements Parcelable {
    public static final String KEY_GROUP = "group";
    public static final String KEY_USER = "user";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";

    public Group getGroup() { return (Group) getParseObject(KEY_GROUP); }

    public void setGroup(Group group) { put(KEY_GROUP, group); }

    public User getUser() {
        return (User) getParseUser(KEY_USER);
    }

    public void setUser(User user) {
        put(KEY_USER, user);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

}
