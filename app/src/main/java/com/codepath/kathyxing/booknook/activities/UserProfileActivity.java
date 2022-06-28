package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.Friend;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class UserProfileActivity extends AppCompatActivity {

    // activity parameters
    public static final String TAG = "UserProfileActivity";
    private ImageView ivProfilePicture;
    private TextView tvUsername;
    private Button btnAddFriend;
    private Button btnAcceptFriend;
    private TextView tvFriendRequestSent;
    private TextView tvFriends;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        // initialize views
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUsername = findViewById(R.id.tvUsername);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnAcceptFriend = findViewById(R.id.btnAcceptFriend);
        tvFriendRequestSent = findViewById(R.id.tvFriendRequestSent);
        tvFriends = findViewById(R.id.tvFriends);
        user = (User) getIntent().getExtras().get("user");
        // set the views
        tvUsername.setText(user.getUsername());
        // load in profile picture with glide
        ParseFile profilePicture = user.getProfilePicture();
        Glide.with(this).load(profilePicture.getUrl()).circleCrop().into(ivProfilePicture);
        // set view visibility
        if (!user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            Log.i(TAG, "user is " + user.getObjectId() + ", current user is " + ParseUser.getCurrentUser().getObjectId());
            setBtnAddFriendVisibility();
            setBtnAcceptFriendVisibility();
            setTvFriendRequestSentVisibility();
            setTvFriendsVisibility();
        }
    }

    private void setBtnAddFriendVisibility() {
        GetCallback getFriendStatusCallback = new GetCallback<Friend>() {
            @Override
            public void done(Friend object, ParseException e) {
                if (e != null) {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        // friend doesn't exist
                        btnAddFriend.setVisibility(View.VISIBLE);
                    } else {
                        // error is something other than the friend not existing
                        Log.e(TAG, "Error setting add friend button visibility", e);
                    }
                }
            }
        };
        ParseQueryUtilities.getFriendStatus(user, getFriendStatusCallback);
    }

    private void setBtnAcceptFriendVisibility() {
        GetCallback getReceivingFriendStatusCallback = new GetCallback<Friend>() {
            @Override
            public void done(Friend object, ParseException e) {
                if (e == null) {
                    // no error, friend exists
                    if (!object.getAccepted()) {
                        // current user receiving friend
                        btnAcceptFriend.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                        // error is something other than the friend not existing, debug
                        Log.e(TAG, "Error setting accept friend button visibility", e);
                    }
                }
            }
        };
        ParseQueryUtilities.getReceivingFriendStatusAsync(user, getReceivingFriendStatusCallback);
    }

    private void setTvFriendRequestSentVisibility() {
        GetCallback getRequestingFriendStatusCallback = new GetCallback<Friend>() {
            @Override
            public void done(Friend object, ParseException e) {
                if (e == null) {
                    // no error, friend exists
                    if (!object.getAccepted()) {
                        // current user requesting friend
                        btnAcceptFriend.setVisibility(View.VISIBLE);
                    }
                } else {
                    // error is something other than the friend not existing
                    if (e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                        // debug
                        Log.e(TAG, "Error setting accept friend button visibility", e);
                    }
                }
            }
        };
        ParseQueryUtilities.getRequestingFriendStatusAsync(user, getRequestingFriendStatusCallback);
    }

    private void setTvFriendsVisibility() {
        GetCallback getFriendStatusCallback = new GetCallback<Friend>() {
            @Override
            public void done(Friend object, ParseException e) {
                if (e == null) {
                    // no error, friend exists
                    if (object.getAccepted()) {
                        // current user and other user are friends
                        tvFriends.setVisibility(View.VISIBLE);
                    }
                } else {
                    // error is something other than the friend not existing
                    if (e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                        // debug
                        Log.e(TAG, "Error setting accept friend button visibility", e);
                    }
                }
            }
        };
    }
}