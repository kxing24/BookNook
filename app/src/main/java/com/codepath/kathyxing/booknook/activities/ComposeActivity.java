package com.codepath.kathyxing.booknook.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.codepath.kathyxing.booknook.ImageSelectionUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;

public class ComposeActivity extends AppCompatActivity {
    // TODO: add a button to remove the image

    public static final String TAG = "ComposeActivity";
    public final static int PICK_PHOTO_CODE = 1046;
    public final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public String photoFileName = "photo.jpg";
    private EditText etDescription;
    private Button btnCaptureImage;
    private Button btnGetImageFromGallery;
    private ImageView ivPostImage;
    private Button btnSubmit;
    private File photoFile;
    private ProgressBar pbLoading;
    private Group group;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // Extract group from intent extras
        group = (Group) getIntent().getExtras().get("group");
        // unwrap the book passed in via intent, using its simple name as a key
        book = (Book) Parcels.unwrap(getIntent().getParcelableExtra(Book.class.getSimpleName()));

        // initialize the views
        etDescription = findViewById(R.id.etDescription);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        btnGetImageFromGallery = findViewById(R.id.btnGetImageFromGallery);
        ivPostImage = findViewById(R.id.ivPostImage);
        btnSubmit = findViewById(R.id.btnSubmit);
        pbLoading = findViewById(R.id.pbLoading);

        // click handler for CaptureImage button
        // launch the camera so that the user can take a picture
        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        // click handler for GetImageFromGallery button
        // go to the gallery so that the user can choose an image
        btnGetImageFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPhoto();
            }
        });

        // click handler for submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, currentUser, photoFile);
            }
        });

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
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

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

    // save a post to parse
    private void savePost(String description, ParseUser currentUser, File photoFile) {
        pbLoading.setVisibility(View.VISIBLE);

        // create a new post
        Post post = new Post();
        // set the post's parameters
        post.setDescription(description);
        if (photoFile != null) {
            post.setImage(new ParseFile(photoFile));
        }
        post.setUser((User) currentUser);
        post.setGroup(group);
        post.setBookId(book.getId());
        // save the parameter to parse
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(ComposeActivity.this, "Error while saving!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "Post save was successful!");
                }
                // set the activity's views
                etDescription.setText("");
                ivPostImage.setImageResource(0);
                pbLoading.setVisibility(View.INVISIBLE);
                // put post metadata into the intent
                Intent intent = new Intent();
                intent.putExtra("post", post);
                // set result code and bundle data for response
                setResult(RESULT_OK, intent);
                finish();
            }
        });
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
                ivPostImage.setImageBitmap(takenImage);
                ivPostImage.setVisibility(View.VISIBLE);
            }
            if (requestCode == PICK_PHOTO_CODE) {
                Uri photoUri = data.getData();

                // Load the image located at photoUri into selectedImage with the correct orientation
                Bitmap selectedImage = null;
                try {
                    selectedImage = ImageSelectionUtilities.rotateBitmapOrientationGallery(photoUri, this.getContentResolver());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Load the taken image into a preview
                ivPostImage.setImageBitmap(selectedImage);
                ivPostImage.setVisibility(View.VISIBLE);

            }
        } else {
            Toast.makeText(this, "Issue with getting picture!", Toast.LENGTH_SHORT).show();
        }
    }

}