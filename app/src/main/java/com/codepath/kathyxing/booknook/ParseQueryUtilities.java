package com.codepath.kathyxing.booknook;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.Friend;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
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
     * @param bookId
     * @param addMemberWithGroupCallback
     */
    public static void addMemberWithGroupAsync(@NonNull Group group, @NonNull User user, @NonNull String bookId,
                                               @NonNull SaveCallback addMemberWithGroupCallback) {
        // Create a new member
        Member member = new Member();
        member.setFrom(user);
        member.setTo(group);
        member.setBookId(bookId);
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

    /**
     * Get the posts from the user's groups
     * @param queryPostsCallback
     */
    public static void queryHomeFeedPostsAsync(@NonNull FindCallback queryPostsCallback) {
        // first query the groups from the user
        ParseQuery<Member> memberQuery = ParseQuery.getQuery(Member.class);
        memberQuery.whereEqualTo(Member.KEY_FROM, ParseUser.getCurrentUser());
        // query the groups from the posts
        ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
        postQuery.include(Post.KEY_CREATED_AT);
        postQuery.include(Post.KEY_USER);
        // find the results where the groups match across queries
        // this yield the posts of the groups that the user is in
        postQuery.whereMatchesKeyInQuery(Post.KEY_GROUP, Member.KEY_TO, memberQuery);
        // sort the posts in descending order by creation date
        postQuery.addDescendingOrder(Post.KEY_CREATED_AT);
        postQuery.findInBackground(queryPostsCallback);
    }

    /**
     * Get the posts from a group
     * @param bookId
     * @param queryPostsCallback
     */
    public static void queryGroupPostsAsync(@NonNull String bookId, @NonNull FindCallback queryPostsCallback) {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.KEY_USER);
        // include post creation time
        query.include(Post.KEY_CREATED_AT);
        // query data where the post's book id is equal to the group book's id
        query.whereEqualTo(Post.KEY_BOOK_ID, bookId);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(queryPostsCallback);
    }

    /**
     * query for friend where current user is the requesting user and other user is the receiving user
     * @param user
     * @param getRequestingFriendStatusCallback
     */
    public static void getRequestingFriendStatusAsync(@NonNull User user, @NonNull GetCallback getRequestingFriendStatusCallback) {
        ParseQuery<Friend> query = ParseQuery.getQuery(Friend.class);
        query.include(Friend.KEY_ACCEPTED);
        query.whereEqualTo(Friend.KEY_REQUESTING_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Friend.KEY_RECEIVING_USER, user);
        query.getFirstInBackground(getRequestingFriendStatusCallback);
    }

    /**
     * query for friend where current user is the receiving user and other user is the requesting user
     * @param user
     * @param getReceivingFriendStatusCallback
     */
    public static void getReceivingFriendStatusAsync(@NonNull User user, @NonNull GetCallback getReceivingFriendStatusCallback) {
        ParseQuery<Friend> query = ParseQuery.getQuery(Friend.class);
        query.include(Friend.KEY_ACCEPTED);
        query.whereEqualTo(Friend.KEY_REQUESTING_USER, user);
        query.whereEqualTo(Friend.KEY_RECEIVING_USER, ParseUser.getCurrentUser());
        query.getFirstInBackground(getReceivingFriendStatusCallback);
    }
}
