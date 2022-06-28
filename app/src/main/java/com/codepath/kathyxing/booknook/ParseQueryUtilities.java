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
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
     * Get the current user's groups
     * @param queryGroupsCallback
     */
    public static void queryGroupsAsync(@NonNull FindCallback queryGroupsCallback) {
        // specify what type of data we want to query - Groups.class
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        // get data where the "from" (user) parameter matches the current user
        query.whereEqualTo(Member.KEY_FROM, ParseUser.getCurrentUser());
        // limit query to latest 20 items
        query.setLimit(20);
        // start an asynchronous call for groups
        query.findInBackground(queryGroupsCallback);
    }

    /**
     * Get the current user's friends
     * @param queryFriendsCallback
     */
    public static void queryFriendsAsync(@NonNull FindCallback queryFriendsCallback) {
        // query friends where the current user requested and the friend received
        ParseQuery<Friend> queryFriendRequest = ParseQuery.getQuery(Friend.class);
        ParseQuery<User> queryUserRequest = ParseQuery.getQuery(User.class);
        queryFriendRequest.whereEqualTo(Friend.KEY_REQUESTING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        queryFriendRequest.whereEqualTo(Friend.KEY_ACCEPTED, true);
        queryUserRequest.whereMatchesKeyInQuery(User.KEY_OBJECT_ID, Friend.KEY_RECEIVING_USER, queryFriendRequest);
        // query friends where the current user received and the friend requested
        ParseQuery<Friend> queryFriendReceive = ParseQuery.getQuery(Friend.class);
        ParseQuery<User> queryUserReceive = ParseQuery.getQuery(User.class);
        queryFriendReceive.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        queryFriendReceive.whereEqualTo(Friend.KEY_ACCEPTED, true);
        queryUserReceive.whereMatchesKeyInQuery(User.KEY_OBJECT_ID, Friend.KEY_REQUESTING_USER, queryFriendReceive);
        // Combine queries and get result
        List<ParseQuery<User>> queries = new ArrayList<>();
        queries.add(queryUserRequest);
        queries.add(queryUserReceive);
        ParseQuery<User> mainQuery = ParseQuery.or(queries);
        mainQuery.addAscendingOrder(User.KEY_USERNAME);
        mainQuery.findInBackground(queryFriendsCallback);
    }

    public static void queryUsersAsync(@NonNull String query, @NonNull FindCallback queryUsersCallback) {
        // get results where query is in username
        ParseQuery<User> queryUsername = ParseQuery.getQuery(User.class);
        queryUsername.whereContains(User.KEY_USERNAME_LOWERCASE, query.toLowerCase(Locale.ROOT));
        // get results where query is in email
        ParseQuery<User> queryEmail = ParseQuery.getQuery(User.class);
        queryEmail.whereContains(User.KEY_EMAIL, query.toLowerCase(Locale.ROOT));
        // Combine queries and get result
        List<ParseQuery<User>> queries = new ArrayList<>();
        queries.add(queryUsername);
        queries.add(queryEmail);
        ParseQuery<User> mainQuery = ParseQuery.or(queries);
        // Exclude current user
        mainQuery.whereNotEqualTo(User.KEY_USERNAME, ParseUser.getCurrentUser().getUsername());
        // Only include users with verified emails
        mainQuery.whereEqualTo(User.KEY_EMAIL_VERIFIED, true);
        mainQuery.findInBackground(queryUsersCallback);
    }

    /**
     * query for friend where current user is the requesting user and other user is the receiving user
     * @param user
     * @param getRequestingFriendStatusCallback
     */
    public static void getRequestingFriendStatusAsync(@NonNull User user, @NonNull GetCallback getRequestingFriendStatusCallback) {
        ParseQuery<Friend> query = ParseQuery.getQuery(Friend.class);
        query.include(Friend.KEY_ACCEPTED);
        query.whereEqualTo(Friend.KEY_REQUESTING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, user.getObjectId());
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
        query.whereEqualTo(Friend.KEY_REQUESTING_USER_ID, user.getObjectId());
        query.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.getFirstInBackground(getReceivingFriendStatusCallback);
    }

    /**
     * query for friend between current user and other user
     * @param user
     * @param getFriendStatusCallback
     */
    public static void getFriendStatus(@NonNull User user, @NonNull GetCallback getFriendStatusCallback) {
        // Get results where current user is requesting other user
        ParseQuery<Friend> queryRequesting = ParseQuery.getQuery(Friend.class);
        queryRequesting.whereEqualTo(Friend.KEY_REQUESTING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        queryRequesting.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, user.getObjectId());
        // Get results where current user is receiving other user
        ParseQuery<Friend> queryReceiving = ParseQuery.getQuery(Friend.class);
        queryReceiving.whereEqualTo(Friend.KEY_REQUESTING_USER_ID, user.getObjectId());
        queryReceiving.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        // Combine queries and get result
        List<ParseQuery<Friend>> queries = new ArrayList<>();
        queries.add(queryRequesting);
        queries.add(queryReceiving);
        ParseQuery<Friend> mainQuery = ParseQuery.or(queries);
        mainQuery.include(Friend.KEY_ACCEPTED);
        mainQuery.getFirstInBackground(getFriendStatusCallback);
    }
}
