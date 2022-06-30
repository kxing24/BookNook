package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.GroupFeedActivity;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;

import org.parceler.Parcels;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailFragment extends Fragment {

    // fragment parameters
    public static final String TAG = "BookDetailActivity";
    private static final int GET_LEFT_GROUP = 10;
    private ImageView ivBookCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvDescription;
    private TextView tvNumMembers;
    private Button btnJoinGroup;
    private Button btnCreateGroup;
    private Button btnGoToGroup;
    private ProgressBar pbLoading;
    private Book book;
    private Group bookGroup;
    private int numMembers;

    // Required empty public constructor
    public BookDetailFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle("Book Details");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize views
        ivBookCover = view.findViewById(R.id.ivBookCover);
        tvTitle = view.findViewById(R.id.tvGroupTitle);
        tvAuthor = view.findViewById(R.id.tvAuthor);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvNumMembers = view.findViewById(R.id.tvNumMembers);
        btnJoinGroup = view.findViewById(R.id.btnJoinGroup);
        btnCreateGroup = view.findViewById(R.id.btnCreateGroup);
        btnGoToGroup = view.findViewById(R.id.btnGotoGroup);
        pbLoading = view.findViewById(R.id.pbLoading);
        // Extract book object from the bundle
        Bundle bundle = this.getArguments();
        if(bundle != null) {
            book = Parcels.unwrap(bundle.getParcelable(Book.class.getSimpleName()));
        }
        // Check if the book group exists and whether the user is already in the group
        bookGroupStatus();
        // Set view text
        tvTitle.setText(book.getTitle());
        if(book.getAuthor().equals("")) {
            tvAuthor.setText("no author available");
        } else {
            tvAuthor.setText("by " + book.getAuthor());
        }
        tvDescription.setText(book.getDescription());
        // Load in the cover image
        if (book.getCoverUrl() != null) {
            Glide.with(this)
                    .load(Uri.parse(book.getCoverUrl()))
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_nocover))
                    .into(ivBookCover);
        } else {
            Glide.with(this).load(R.drawable.ic_nocover).into(ivBookCover);
        }
        // Click handler for the create group button
        btnCreateGroup.setOnClickListener(v -> {
            // create the group and make the user a member
            bookGroupCreate(book);
            // set button visibility
            btnCreateGroup.setVisibility(View.GONE);
        });
        // Click handler for join group button
        btnJoinGroup.setOnClickListener(v -> {
            // make the user a member of the group
            addMemberWithBook();
            // set button visibility
            btnJoinGroup.setVisibility(View.GONE);
            tvNumMembers.setText(++numMembers + " members");
        });
        // Click handler for goto group button
        btnGoToGroup.setOnClickListener(v -> {
            goGroupFeedActivity();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_LEFT_GROUP && resultCode == getActivity().RESULT_OK) {
            Boolean leftGroup = data.getExtras().getBoolean("leftGroup");
            if(leftGroup) {
                btnGoToGroup.setVisibility(View.GONE);
                btnJoinGroup.setVisibility(View.VISIBLE);
                tvNumMembers.setText(--numMembers + " members");
            }
        }
    }

    private void bookGroupStatus() {
        // Create a callback for bookGroupStatusAsync
        GetCallback bookGroupStatusCallback = (GetCallback<Group>) (object, e) -> {
            // book group exists
            if (e == null) {
                // book group exists, set the group and check if the user is in the group
                bookGroup = object;
                userInGroup();
                // set the number of members in the group
                setNumMembers(object);
            } else {
                // book group doesn't exist
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    // set the create group button to visible
                    btnCreateGroup.setVisibility(View.VISIBLE);
                    tvNumMembers.setText("Book group does not exist");
                    pbLoading.setVisibility(View.GONE);
                }
                // unknown error, debug
                else {
                    Log.e(TAG, "Query failed", e);
                }
            }
        };
        ParseQueryUtilities.bookGroupStatusAsync(book, bookGroupStatusCallback);
    }

    private void userInGroup() {
        // Create a callback for userInGroupAsync
        GetCallback userInGroupCallback = (GetCallback<Member>) (object, e) -> {
            if (e == null) {
                // user is in the group
                // set the go to group button to be visible
                btnGoToGroup.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.GONE);
            } else {
                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    // user is not in the group
                    // set the join group button to be visible
                    btnJoinGroup.setVisibility(View.VISIBLE);
                    pbLoading.setVisibility(View.GONE);
                } else {
                    // unknown error, debug
                    Log.e(TAG, "Query failed", e);
                }
            }
        };
        ParseQueryUtilities.userInGroupAsync(book, (User) User.getCurrentUser(), userInGroupCallback);
    }

    private void addMemberWithBook() {
        // create a callback for getGroupFromBookCallback
        GetCallback getGroupFromBookCallback = (GetCallback<Group>) (group, e) -> {
            // get the group and create a member with the group and user
            if (e == null) {
                addMemberWithGroup(group);
            }
            // an error occurred
            else {
                Log.e(TAG, "Issue getting group", e);
            }
        };
        ParseQueryUtilities.getGroupFromBookAsync(book, getGroupFromBookCallback);
    }

    private void addMemberWithGroup(@NonNull Group group) {
        // create a callback for addMemberWithGroup
        SaveCallback addMemberWithGroupCallback = e -> {
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
        };
        ParseQueryUtilities.addMemberWithGroupAsync(group,
                (User) User.getCurrentUser(), book.getId(), addMemberWithGroupCallback);
    }

    // Creates the group and adds the first user
    private void bookGroupCreate(@NonNull Book book) {
        // creates the group
        Group group = new Group();
        group.setBookId(book.getId());
        group.setGroupName(book.getTitle() + " Group");
        group.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "Error while creating group", e);
                Toast.makeText(getContext(), "Error while creating group!", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "Successfully created group for " + group.getBookId());
                // set the group
                bookGroup = group;
                // make the user a member of the group
                addMemberWithGroup(group);
            }
        });
    }

    // set the number of members in the group
    private void setNumMembers(Group group) {
        CountCallback getNumMembersInGroupCallback = (count, e) -> {
            tvNumMembers.setText(count + " members");
            numMembers = count;
        };
        ParseQueryUtilities.getNumMembersInGroupAsync(group, getNumMembersInGroupCallback);
    }

    private void goGroupFeedActivity() {
        Intent i = new Intent(getContext(), GroupFeedActivity.class);
        i.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
        i.putExtra("group", bookGroup);
        startActivityForResult(i, GET_LEFT_GROUP);
    }
}