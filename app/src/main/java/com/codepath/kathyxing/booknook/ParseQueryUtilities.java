package com.codepath.kathyxing.booknook;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public final class ParseQueryUtilities {
    // Check if the book group exists
    // If the book group exists, check if the user is in the group
    // Set the visibility of the join group, create group, and goto group buttons based on results
    public static void bookGroupStatusAsync(@NonNull Book book, GetCallback bookGroupStatusCallback) {
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.whereEqualTo(Group.KEY_BOOK_ID, book.getId());
        query.getFirstInBackground(bookGroupStatusCallback);
    }

    /*
    // Creates the group and adds the first user
    public static void bookGroupCreate(@NonNull Book book, @NonNull User user) {
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
                    addMemberWithGroupAsync(group, user);
                }
            }
        });
    }

     */

    // Add a user to a group
    public static void addMemberWithGroupAsync(@NonNull Group group, @NonNull User user, @NonNull Book book,
                                               @NonNull SaveCallback addMemberWithGroupCallback) {
        // Create a new member
        Member member = new Member();
        member.setFrom(user);
        member.setTo(group);
        member.setBookId(book.getId());
        member.saveInBackground(addMemberWithGroupCallback);
    }

    // Add a user to a group given the book
    public static void addMemberWithBookAsync(@NonNull Book book, @NonNull User user,
                                              @NonNull GetCallback addMemberWithBookCallback) {
        // create the query
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        // get results with the book id
        query.whereEqualTo(Group.KEY_BOOK_ID, book.getId());
        // get the group from the query
        query.getFirstInBackground(addMemberWithBookCallback);
    }

    // checks if a user is in a group given the book
    // set the goto group and join group buttons accordingly
    public static void userInGroupAsync(@NonNull Book book, @NonNull User user,
                                  @NonNull GetCallback userInGroupCallback) {
        // create the query
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        // get results with the user
        query.whereEqualTo(Member.KEY_FROM, user);
        // get results with the group
        query.whereEqualTo(Member.KEY_BOOK_ID, book.getId());
        // get the member from the query
        query.getFirstInBackground(userInGroupCallback);
    }
}
