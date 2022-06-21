package com.codepath.kathyxing.booknook.parse_classes;

import com.parse.ParseClassName;
import com.parse.ParseUser;

import java.util.Date;

public class User extends ParseUser {
    public static final String KEY_PROFILE_DESCRIPTION = "profileDescription";

    public String getProfileDescription() { return getString(KEY_PROFILE_DESCRIPTION); }
}
