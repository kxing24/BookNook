package com.codepath.kathyxing.booknook.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.codepath.kathyxing.booknook.ImageSelectionUtilities;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.Friend;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;

public class UserProfileActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    // activity parameters
    public static final String TAG = "UserProfileActivity";
    public final static int PICK_PHOTO_CODE = 1046;
    public final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public String photoFileName = "photo.jpg";
    private File photoFile;
    private ImageView ivProfilePicture;
    private TextView tvUsername;
    private TextView tvProfileDescription;
    private Button btnAddFriend;
    private Button btnAcceptFriend;
    private TextView tvFriendRequestSent;
    private TextView tvFriends;
    private Button btnEditProfile;
    private EditText etProfileDescription;
    private Button btnSave;
    private Button btnCancel;
    private ImageButton ibEditProfilePicture;
    private RelativeLayout rlUserProfileActivity;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        // set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
        }
        // have the toolbar show a back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        // initialize views
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvUsername = findViewById(R.id.tvUsername);
        tvProfileDescription = findViewById(R.id.tvProfileDescription);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnAcceptFriend = findViewById(R.id.btnAcceptFriend);
        tvFriendRequestSent = findViewById(R.id.tvFriendRequestSent);
        tvFriends = findViewById(R.id.tvFriends);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        etProfileDescription = findViewById(R.id.etProfileDescription);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        ibEditProfilePicture = findViewById(R.id.ibEditProfilePicture);
        rlUserProfileActivity = findViewById(R.id.rlUserProfileActivity);
        user = (User) getIntent().getExtras().get("user");
        // set the views
        tvUsername.setText(user.getUsername());
        if (user.getProfileDescription() == null || user.getProfileDescription().equals("")) {
            tvProfileDescription.setText("This user has no description");
            etProfileDescription.setText("");
        } else {
            tvProfileDescription.setText(user.getProfileDescription());
            etProfileDescription.setText(user.getProfileDescription());
        }
        // load in profile picture with glide
        Glide.with(this).load(user.getProfilePicture().getUrl()).circleCrop().into(ivProfilePicture);
        // set view visibility
        if (!user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            setBtnAddFriendVisibility();
            setBtnAcceptFriendVisibility();
            setTvFriendRequestSentVisibility();
            setTvFriendsVisibility();
        } else {
            btnEditProfile.setVisibility(View.VISIBLE);
        }
        // set click handler for add friend button
        btnAddFriend.setOnClickListener(v -> requestFriend());
        // set click handler for accept friend button
        btnAcceptFriend.setOnClickListener(v -> acceptFriend());
        // set click handler for edit profile button
        btnEditProfile.setOnClickListener(v -> {
            btnEditProfile.setVisibility(View.GONE);
            tvProfileDescription.setVisibility(View.GONE);
            etProfileDescription.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            ibEditProfilePicture.setVisibility(View.VISIBLE);
        });
        // set click handler for save button
        btnSave.setOnClickListener(v -> {
            String newDescription = etProfileDescription.getText().toString();
            saveProfileDescription(newDescription);
            if (newDescription.equals("")) {
                newDescription = "This user has no description";
            }
            saveProfilePicture();
            doneEditingProfile(newDescription);
            Toast.makeText(UserProfileActivity.this, "Saved profile changes!", Toast.LENGTH_SHORT).show();
        });
        // set click handler for cancel button
        btnCancel.setOnClickListener(v -> {
            String oldDescription = tvProfileDescription.getText().toString();
            Glide.with(this).load(user.getProfilePicture().getUrl()).circleCrop().into(ivProfilePicture);
            doneEditingProfile(oldDescription);
        });
        // set click handler for edit profile picture button
        ibEditProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
        // hide keyboard when the relative layout is touched
        if (UserProfileActivity.this.getCurrentFocus() != null) {
            rlUserProfileActivity.setOnTouchListener((v, event) -> {
                InputMethodManager imm = (InputMethodManager) UserProfileActivity.this.getSystemService(UserProfileActivity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(UserProfileActivity.this.getCurrentFocus().getWindowToken(), 0);
                return true;
            });
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Log.i(TAG, "menu item clicked!");
        int id = item.getItemId();
        switch (id) {
            case R.id.gallery:
                onPickPhoto();
                return true;
            case R.id.camera:
                launchCamera();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = ImageSelectionUtilities.rotateBitmapOrientationCamera(photoFile.getPath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivProfilePicture.setImageBitmap(ImageSelectionUtilities.getCroppedBitmap(takenImage));
            }
            if (requestCode == PICK_PHOTO_CODE && data != null) {
                Uri photoUri = data.getData();
                try {
                    Bitmap selectedImage = MediaStore.Images.Media.getBitmap(UserProfileActivity.this.getContentResolver(), photoUri);
                    ivProfilePicture.setImageBitmap(ImageSelectionUtilities.getCroppedBitmap(selectedImage));
                } catch (IOException e) {
                    Log.i("TAG", "Some exception " + e);
                }
            }
        } else {
            Toast.makeText(this, "Issue with getting picture!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_user_profile);
        popup.show();
    }

    private void saveProfileDescription(String description) {
        SaveCallback saveUserDescriptionCallback = e -> {
            if (e != null) {
                Log.e(TAG, "Error while saving description", e);
            } else {
                Log.i(TAG, "Profile description save was successful");
            }
        };
        ParseQueryUtilities.saveProfileDescriptionAsync(user, description, saveUserDescriptionCallback);
    }

    private void saveProfilePicture() {
        if (photoFile != null) {
            SaveCallback saveProfilePictureCallback = e -> {
                if (e != null) {
                    Log.e(TAG, "Error while image", e);
                } else {
                    Log.i(TAG, "Profile picture save was successful");
                }
            };
            ParseQueryUtilities.saveProfilePictureAsync(user, new ParseFile(photoFile), saveProfilePictureCallback);
            user.setProfilePicture(new ParseFile(photoFile));
        }
    }

    private void doneEditingProfile(String description) {
        tvProfileDescription.setText(description);
        tvProfileDescription.setVisibility(View.VISIBLE);
        etProfileDescription.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        ibEditProfilePicture.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.VISIBLE);
    }

    private void requestFriend() {
        SaveCallback requestFriendCallback = e -> {
            if (e == null) {
                btnAddFriend.setVisibility(View.GONE);
                tvFriendRequestSent.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(UserProfileActivity.this,
                        "Error sending friend request!", Toast.LENGTH_SHORT).show();
            }
        };
        ParseQueryUtilities.requestFriendAsync(user, requestFriendCallback);
    }

    private void acceptFriend() {
        SaveCallback acceptFriendCallback = e -> {
            if (e == null) {
                Log.i(TAG, "Successfully accepted friend");
                btnAcceptFriend.setVisibility(View.GONE);
                tvFriends.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(UserProfileActivity.this, "Error accepting friend!",
                        Toast.LENGTH_SHORT).show();
            }
        };
        GetCallback<Friend> getFriendReceivingCallback = (friend, e) -> {
            if (e == null) {
                ParseQueryUtilities.acceptFriendAsync(friend, acceptFriendCallback);
            }
        };
        ParseQueryUtilities.getFriendReceivingAsync(user, getFriendReceivingCallback);
    }

    private void setBtnAddFriendVisibility() {
        GetCallback<Friend> getFriendStatusCallback = (object, e) -> {
            if (e != null) {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    // friend doesn't exist
                    btnAddFriend.setVisibility(View.VISIBLE);
                } else {
                    // error is something other than the friend not existing
                    Log.e(TAG, "Error setting add friend button visibility", e);
                }
            }
        };
        ParseQueryUtilities.getFriendStatusAsync(user, getFriendStatusCallback);
    }

    private void setBtnAcceptFriendVisibility() {
        GetCallback<Friend> getReceivingFriendStatusCallback = (object, e) -> {
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
        };
        ParseQueryUtilities.getReceivingFriendStatusAsync(user, getReceivingFriendStatusCallback);
    }

    private void setTvFriendRequestSentVisibility() {
        GetCallback<Friend> getRequestingFriendStatusCallback = (object, e) -> {
            if (e == null) {
                // no error, friend exists
                if (!object.getAccepted()) {
                    // current user requesting friend
                    tvFriendRequestSent.setVisibility(View.VISIBLE);
                }
            } else {
                // error is something other than the friend not existing
                if (e.getCode() != ParseException.OBJECT_NOT_FOUND) {
                    // debug
                    Log.e(TAG, "Error setting accept friend button visibility", e);
                }
            }
        };
        ParseQueryUtilities.getRequestingFriendStatusAsync(user, getRequestingFriendStatusCallback);
    }

    private void setTvFriendsVisibility() {
        GetCallback<Friend> getFriendStatusCallback = (object, e) -> {
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
        };
        ParseQueryUtilities.getFriendStatusAsync(user, getFriendStatusCallback);
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = ImageSelectionUtilities.getPhotoFileUri(photoFileName, this, TAG);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Trigger gallery selection for a photo
    private void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        // Create a File reference for future access
        photoFile = ImageSelectionUtilities.getPhotoFileUri(photoFileName, this, TAG);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }
}