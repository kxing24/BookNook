package com.codepath.kathyxing.booknook.parse_classes;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.Date;

public class User extends ParseUser {
    public static final String KEY_PROFILE_PICTURE = "profilePicture";
    public static final String KEY_PROFILE_DESCRIPTION = "profileDescription";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USERNAME_LOWERCASE = "usernameLowercase";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_EMAIL_VERIFIED = "emailVerified";

    public ParseFile getProfilePicture() { return getParseFile(KEY_PROFILE_PICTURE); }

    public void setProfilePicture(ParseFile profilePicture) { put(KEY_PROFILE_PICTURE, profilePicture); }

    public String getProfileDescription() { return getString(KEY_PROFILE_DESCRIPTION); }

    public void setProfileDescription(String profileDescription) { put(KEY_PROFILE_DESCRIPTION, profileDescription); }

    public String getUsernameLowercase() { return getString(KEY_USERNAME_LOWERCASE); }

    public void setUsernameLowercase(String usernameLowercase) { put(KEY_USERNAME_LOWERCASE, usernameLowercase); }
}
