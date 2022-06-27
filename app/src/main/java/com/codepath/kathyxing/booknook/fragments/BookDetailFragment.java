package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.GroupFeedActivity;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.parceler.Parcels;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailFragment extends Fragment {

    // fragment parameters
    public static final String TAG = "BookDetailActivity";
    private ImageView ivBookCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvDescription;
    private Button btnJoinGroup;
    private Button btnCreateGroup;
    private Button btnGoToGroup;
    private ProgressBar pbLoading;
    private Book book;
    private Group bookGroup;
    // Create a callback for userInGroupAsync
    private GetCallback userInGroupCallback = new GetCallback<Member>() {
        @Override
        public void done(Member object, ParseException e) {
            if (e == null) {
                // user is in the group
                // set the go to group button to be visible
                btnGoToGroup.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.GONE);
            }
            else {
                if(e.getCode() == ParseException.OBJECT_NOT_FOUND)
                {
                    // user is not in the group
                    // set the join group button to be visible
                    btnJoinGroup.setVisibility(View.VISIBLE);
                    pbLoading.setVisibility(View.GONE);
                }
                else
                {
                    // unknown error, debug
                    Log.e(TAG, "Query failed", e);
                }
            }
        }
    };
    // Create a callback for bookGroupStatusAsync
    private GetCallback bookGroupStatusCallback = new GetCallback<Group>() {
        @Override
        public void done(Group object, ParseException e) {
            // book group exists
            if (e == null) {
                // book group exists, set the group and check if the user is in the group
                bookGroup = object;
                ParseQueryUtilities.userInGroupAsync(book, (User) User.getCurrentUser(), userInGroupCallback);
            }
            else {
                // object doesn't exist
                if(e.getCode() == ParseException.OBJECT_NOT_FOUND)
                {
                    // set the create group button to visible
                    btnCreateGroup.setVisibility(View.VISIBLE);
                    pbLoading.setVisibility(View.GONE);
                }
                // unknown error, debug
                else
                {
                    Log.e(TAG, "Query failed", e);
                }
            }
        }
    };
    // create a callback for addMemberWithGroup
    SaveCallback addMemberWithGroupCallback = new SaveCallback() {
        @Override
        public void done(ParseException e) {
            // Error while creating member
            if (e != null) {
                Log.e(TAG, "Error while creating member", e);
                Toast.makeText(getContext(), "Failed to join group!", Toast.LENGTH_SHORT).show();
            }
            // Successfully created member
            else {
                Toast.makeText(getContext(), "Joined group!", Toast.LENGTH_SHORT).show();
                // User is now a member of the group: adjust visibility of buttons
                btnGoToGroup.setVisibility(View.VISIBLE);
            }
        }
    };
    // create a callback for addMemberWithBook
    private GetCallback addMemberWithBookCallback = new GetCallback<Group>() {
        @Override
        public void done(Group group, ParseException e) {
            // get the group and create a member with the group and user
            if (e == null) {
                ParseQueryUtilities.addMemberWithGroupAsync(group,
                        (User) User.getCurrentUser(), book, addMemberWithGroupCallback);
            }
            // an error occurred
            else {
                Log.e(TAG, "Issue getting group", e);
            }
        }
    };

    // Required empty public constructor
    public BookDetailFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_detail, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle("Book Details");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize views
        ivBookCover = (ImageView) view.findViewById(R.id.ivBookCover);
        tvTitle = (TextView) view.findViewById(R.id.tvGroupTitle);
        tvAuthor = (TextView) view.findViewById(R.id.tvAuthor);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        btnJoinGroup = (Button) view.findViewById(R.id.btnJoinGroup);
        btnCreateGroup = (Button) view.findViewById(R.id.btnCreateGroup);
        btnGoToGroup = (Button) view.findViewById(R.id.btnGotoGroup);
        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);
        // Extract book object from the bundle
        Bundle bundle = this.getArguments();
        book = Parcels.unwrap(bundle.getParcelable(Book.class.getSimpleName()));
        // Check if the book group exists and whether the user is already in the group
        ParseQueryUtilities.bookGroupStatusAsync(book, bookGroupStatusCallback);
        //bookGroupStatusAsync(book);
        // Set view text
        tvTitle.setText(book.getTitle());
        tvAuthor.setText("by " + book.getAuthor());
        tvDescription.setText(book.getDescription());
        // Load in the cover image
        if(book.getCoverUrl() != null) {
            Glide.with(this)
                    .load(Uri.parse(book.getCoverUrl()))
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_nocover))
                    .into(ivBookCover);
        }
        else {
            Glide.with(this).load(R.drawable.ic_nocover).into(ivBookCover);
        }
        // Click handler for the create group button
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create the group and make the user a member
                bookGroupCreate(book, (User) User.getCurrentUser());
                // set button visibility
                btnCreateGroup.setVisibility(View.GONE);
            }
        });
        // Click handler for join group button
        btnJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // make the user a member of the group
                ParseQueryUtilities.addMemberWithBookAsync(book, (User) User.getCurrentUser(),
                        addMemberWithBookCallback);
                // set button visibility
                btnJoinGroup.setVisibility(View.GONE);
            }
        });
        // Click handler for goto group button
        btnGoToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Going to group!", Toast.LENGTH_SHORT).show();
                goGroupFeedActivity();
            }
        });
    }

    // Creates the group and adds the first user
    private void bookGroupCreate(@NonNull Book book, @NonNull User user) {
        // creates the group
        Group group = new Group();
        group.setBookId(book.getId());
        group.setGroupName(book.getTitle() + " Group");
        group.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while creating group", e);
                    Toast.makeText(getContext(), "Error while creating group!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.i(TAG, "Successfully created group for " + group.getBookId());
                    // set the group
                    bookGroup = group;
                    // make the user a member of the group
                    ParseQueryUtilities.addMemberWithGroupAsync(group, user, book, addMemberWithGroupCallback);
                }
            }
        });

    }

    private void goGroupFeedActivity() {
        Intent i = new Intent(getContext(), GroupFeedActivity.class);
        i.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
        i.putExtra("group", bookGroup);
        startActivity(i);
    }
}