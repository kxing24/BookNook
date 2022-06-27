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
    /**
     * Check if the book group exists
     * @param book
     * @param bookGroupStatusCallback
     */
    public static void bookGroupStatusAsync(@NonNull Book book, GetCallback bookGroupStatusCallback) {
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.whereEqualTo(Group.KEY_BOOK_ID, book.getId());
        query.getFirstInBackground(bookGroupStatusCallback);
    }

    /**
     * Add a user to a group given the group
     * @param group
     * @param user
     * @param book
     * @param addMemberWithGroupCallback
     */
    public static void addMemberWithGroupAsync(@NonNull Group group, @NonNull User user, @NonNull Book book,
                                               @NonNull SaveCallback addMemberWithGroupCallback) {
        // Create a new member
        Member member = new Member();
        member.setFrom(user);
        member.setTo(group);
        member.setBookId(book.getId());
        member.saveInBackground(addMemberWithGroupCallback);
    }

    /**
     * Add a user to a group given the book
     * @param book
     * @param user
     * @param addMemberWithBookCallback
     */
    public static void addMemberWithBookAsync(@NonNull Book book, @NonNull User user,
                                              @NonNull GetCallback addMemberWithBookCallback) {
        // create the query
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        // get results with the book id
        query.whereEqualTo(Group.KEY_BOOK_ID, book.getId());
        // get the group from the query
        query.getFirstInBackground(addMemberWithBookCallback);
    }

    /**
     * Check if a user is in a group given the book
     * @param book
     * @param user
     * @param userInGroupCallback
     */
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
