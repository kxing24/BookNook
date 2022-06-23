package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.List;

public class BookDetailActivity extends AppCompatActivity {

    public static final String TAG = "BookDetailActivity";

    private ImageView ivBookCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvDescription;
    private Button btnJoinGroup;
    private Button btnCreateGroup;
    private Button btnGoToGroup;

    private Book book;
    private Group bookGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Book Details");

        // have the toolbar show a back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize views
        ivBookCover = (ImageView) findViewById(R.id.ivBookCover);
        tvTitle = (TextView) findViewById(R.id.tvGroupTitle);
        tvAuthor = (TextView) findViewById(R.id.tvAuthor);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        btnJoinGroup = (Button) findViewById(R.id.btnJoinGroup);
        btnCreateGroup = (Button) findViewById(R.id.btnCreateGroup);
        btnGoToGroup = (Button) findViewById(R.id.btnGotoGroup);

        // Extract book object from intent extras
        book = (Book) Parcels.unwrap(getIntent().getParcelableExtra(Book.class.getSimpleName()));

        // Check if the book group exists and whether the user is already in the group
        bookGroupStatusAsync(book);

        // Set view text
        tvTitle.setText(book.getTitle());
        tvAuthor.setText("by " + book.getAuthor());
        tvDescription.setText(book.getDescription());

        // Load in the cover image
        Glide.with(this).load(book.getCoverUrl()).into(ivBookCover);

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
                addMemberAsync(book, (User) User.getCurrentUser());
                // set button visibility
                btnJoinGroup.setVisibility(View.GONE);
            }
        });

        // Click handler for goto group button
        btnGoToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BookDetailActivity.this, "Going to group!", Toast.LENGTH_SHORT).show();
                goGroupFeedActivity();
            }
        });
    }

    // Check if the book group exists
    // If the book group exists, check if the user is in the group
    // Set the visibility of the join group, create group, and goto group buttons based on results
    private void bookGroupStatusAsync(Book book) {
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.whereEqualTo("bookId", book.getId());
        query.getFirstInBackground(new GetCallback<Group>() {
            @Override
            public void done(Group object, ParseException e) {
                // book group exists
                if (e == null) {
                    // book group exists, set the group and check if the user is in the group
                    bookGroup = object;
                    userInGroupAsync(book, (User) User.getCurrentUser());
                }
                else {
                    // object doesn't exist
                    if(e.getCode() == ParseException.OBJECT_NOT_FOUND)
                    {
                        // set the create group button to visible
                        btnCreateGroup.setVisibility(View.VISIBLE);
                    }
                    // unknown error, debug
                    else
                    {
                        Log.e(TAG, "Query failed", e);
                    }
                }
            }
        });
    }

    // Creates the group and adds the first user
    private void bookGroupCreate(Book book, User user) {
        // creates the group
        Group group = new Group();
        group.setBookId(book.getId());
        group.setGroupName(book.getTitle() + " Group");
        group.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while creating group", e);
                    Toast.makeText(BookDetailActivity.this, "Error while creating group!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.i(TAG, "Successfully created group for " + group.getBookId());
                    // set the group
                    bookGroup = group;
                    // make the user a member of the group
                    addMemberAsync(group, user);
                }
            }
        });

    }

    // Add a user to a group
    private void addMemberAsync(Group group, User user) {
        // Create a new member
        Member member = new Member();
        member.setFrom(user);
        member.setTo(group);
        member.setBookId(book.getId());
        member.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                // Error while creating member
                if (e != null) {
                    Log.e(TAG, "Error while creating member", e);
                    Toast.makeText(BookDetailActivity.this, "Failed to join group!", Toast.LENGTH_SHORT).show();
                }
                // Successfully created member
                else {
                    Log.i(TAG, "Successfully added " + user.getUsername() + " to " + group.getBookId());
                    Toast.makeText(BookDetailActivity.this, "Joined group!", Toast.LENGTH_SHORT).show();

                    // User is now a member of the group: adjust visibility of buttons
                    btnGoToGroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    // Add a user to a group given the book
    private void addMemberAsync(Book book, User user) {
        // create the query
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        // get results with the book id
        query.whereEqualTo("bookId", book.getId());
        // get the group from the query
        query.getFirstInBackground(new GetCallback<Group>() {
            @Override
            public void done(Group group, ParseException e) {
                // get the group and create a member with the group and user
                if (e == null) {
                    addMemberAsync(group, user);
                }
                // an error occurred
                else {
                    Log.e(TAG, "Issue getting group", e);
                }
            }
        });
    }

    // checks if a user is in a group given the book
    // set the goto group and join group buttons accordingly
    private void userInGroupAsync(Book book, User user) {
        // create the query
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        // get results with the user
        query.whereEqualTo(Member.KEY_FROM, user);
        // get results with the group
        query.whereEqualTo(Member.KEY_BOOK_ID, book.getId());
        // get the member from the query
        query.getFirstInBackground(new GetCallback<Member>() {
            @Override
            public void done(Member object, ParseException e) {
                if (e == null) {
                    // user is in the group
                    // set the go to group button to be visible
                    btnGoToGroup.setVisibility(View.VISIBLE);
                }
                else {
                    if(e.getCode() == ParseException.OBJECT_NOT_FOUND)
                    {
                        // user is not in the group
                        // set the join group button to be visible
                        btnJoinGroup.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        // unknown error, debug
                        Log.e(TAG, "Query failed", e);
                    }
                }
            }
        });
    }

    private void goGroupFeedActivity() {
        Intent i = new Intent(this, GroupFeedActivity.class);
        i.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
        i.putExtra("group", bookGroup);
        startActivity(i);
    }


}