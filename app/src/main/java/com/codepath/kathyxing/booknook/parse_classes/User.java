package com.codepath.kathyxing.booknook.parse_classes;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.Date;

public class User extends ParseUser {
    public static final String KEY_PROFILE_PICTURE = "profilePicture";
    public static final String KEY_PROFILE_DESCRIPTION = "profileDescription";

    public ParseFile getProfilePicture() { return getParseFile(KEY_PROFILE_PICTURE); }

    public void setProfilePicture(ParseFile profilePicture) { put(KEY_PROFILE_PICTURE, profilePicture); }

    public String getProfileDescription() { return getString(KEY_PROFILE_DESCRIPTION); }

    public void setProfileDescription(String profileDescription) { put(KEY_PROFILE_DESCRIPTION, profileDescription); }
}
