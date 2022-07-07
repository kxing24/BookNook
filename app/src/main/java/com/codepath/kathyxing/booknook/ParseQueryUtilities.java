package com.codepath.kathyxing.booknook;

import androidx.annotation.NonNull;

import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.parse_classes.BookOnShelf;
import com.codepath.kathyxing.booknook.parse_classes.Friend;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Like;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.Post;
import com.codepath.kathyxing.booknook.parse_classes.Shelf;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ParseQueryUtilities {
    /**
     * Check if the book group exists
     *
     * @param book                    the group's book
     * @param bookGroupStatusCallback the callback for the async function
     */
    public static void bookGroupStatusAsync(@NonNull Book book, GetCallback<Group> bookGroupStatusCallback) {
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.whereEqualTo(Group.KEY_BOOK_ID, book.getId());
        query.getFirstInBackground(bookGroupStatusCallback);
    }

    /**
     * Add a user to a group given the group
     *
     * @param group                      the group that the user is being added to
     * @param user                       the user being added to the group
     * @param bookId                     the id of the group's book
     * @param addMemberWithGroupCallback the callback for the async function
     */
    public static void addMemberWithGroupAsync(@NonNull Group group, @NonNull User user, @NonNull String bookId,
                                               @NonNull SaveCallback addMemberWithGroupCallback) {
        // Create a new member
        Member member = new Member();
        member.setFrom(user);
        member.setTo(group);
        member.setBookId(bookId);
        member.setUserId(user.getObjectId());
        member.saveInBackground(addMemberWithGroupCallback);
    }

    /**
     * Get the group given the book
     *
     * @param book                     the group's book
     * @param getGroupFromBookCallback the callback for the async function
     */
    public static void getGroupFromBookAsync(@NonNull Book book,
                                             @NonNull GetCallback<Group> getGroupFromBookCallback) {
        // create the query
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        // get results with the book id
        query.whereEqualTo(Group.KEY_BOOK_ID, book.getId());
        // get the group from the query
        query.getFirstInBackground(getGroupFromBookCallback);
    }

    /**
     * Check if a user is in a group given the book
     * If the user is in the group, the query will be successful
     * If the user is not in the group, a ParseException with error code OBJECT_NOT_FOUND will occur
     *
     * @param book                the group's book
     * @param user                the user
     * @param userInGroupCallback the callback for the async function
     */
    public static void userInGroupAsync(@NonNull Book book, @NonNull User user,
                                        @NonNull GetCallback<Member> userInGroupCallback) {
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
     * Check if a user in a group given the group
     * If the user is in the group, the query will be successful
     * If the user is not in the group, a ParseException with error code OBJECT_NOT_FOUND will occur
     *
     * @param group               the group
     * @param user                the user
     * @param userInGroupCallback the callback for the async function
     */
    public static void userInGroupAsync(@NonNull Group group, @NonNull User user, @NonNull GetCallback<Member> userInGroupCallback) {
        ParseQuery<Member> queryMember = ParseQuery.getQuery(Member.class);
        queryMember.whereEqualTo(Member.KEY_USER_ID, user.getObjectId());
        queryMember.whereEqualTo(Member.KEY_BOOK_ID, group.getBookId());
        queryMember.getFirstInBackground(userInGroupCallback);
    }

    /**
     * Gets the group from the groupId
     *
     * @param groupId          the groupId
     * @param getGroupCallback the callback for the async function
     */
    public static void getGroupAsync(@NonNull String groupId, @NonNull GetCallback<Group> getGroupCallback) {
        ParseQuery<Group> query = ParseQuery.getQuery(Group.class);
        query.include(Group.KEY_BOOK_ID);
        query.getInBackground(groupId, getGroupCallback);
    }

    /**
     * Get the posts from the user's groups
     *
     * @param queryPostsCallback the callback for the async function
     */
    public static void queryHomeFeedPostsAsync(@NonNull FindCallback<Post> queryPostsCallback) {
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
     *
     * @param bookId             the id of the group's book
     * @param queryPostsCallback the callback for the async function
     */
    public static void queryGroupPostsAsync(@NonNull String bookId, @NonNull FindCallback<Post> queryPostsCallback) {
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
     *
     * @param queryGroupsCallback the callback for the async function
     */
    public static void queryGroupsAsync(@NonNull FindCallback<Member> queryGroupsCallback) {
        // specify what type of data we want to query - Groups.class
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        // get data where the "from" (user) parameter matches the current user
        query.whereEqualTo(Member.KEY_FROM, ParseUser.getCurrentUser());
        query.addDescendingOrder(Member.KEY_CREATED_AT);
        // limit query to latest 20 items
        query.setLimit(20);
        // start an asynchronous call for groups
        query.findInBackground(queryGroupsCallback);
    }

    /**
     * Get the current user's shelves
     *
     * @param queryShelvesCallback the callback for the async function
     */
    public static void queryShelvesAsync(@NonNull FindCallback<Shelf> queryShelvesCallback) {
        ParseQuery<Shelf> query = ParseQuery.getQuery(Shelf.class);
        query.whereEqualTo(Shelf.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(Shelf.KEY_CREATED_AT);
        query.setLimit(20);
        query.findInBackground(queryShelvesCallback);
    }

    /**
     * Get the current user's friends
     *
     * @param queryFriendsCallback the callback for the async function
     */
    public static void queryFriendsAsync(@NonNull FindCallback<User> queryFriendsCallback) {
        // query friends where the current user requested and the friend received
        ParseQuery<Friend> queryFriendRequest = ParseQuery.getQuery(Friend.class);
        ParseQuery<User> queryUserRequest = ParseQuery.getQuery(User.class);
        queryFriendRequest.whereEqualTo(Friend.KEY_REQUESTING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        queryFriendRequest.whereEqualTo(Friend.KEY_ACCEPTED, true);
        queryUserRequest.whereMatchesKeyInQuery(User.KEY_OBJECT_ID, Friend.KEY_RECEIVING_USER_ID, queryFriendRequest);
        // query friends where the current user received and the friend requested
        ParseQuery<Friend> queryFriendReceive = ParseQuery.getQuery(Friend.class);
        ParseQuery<User> queryUserReceive = ParseQuery.getQuery(User.class);
        queryFriendReceive.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        queryFriendReceive.whereEqualTo(Friend.KEY_ACCEPTED, true);
        queryUserReceive.whereMatchesKeyInQuery(User.KEY_OBJECT_ID, Friend.KEY_REQUESTING_USER_ID, queryFriendReceive);
        // Combine queries and get result
        List<ParseQuery<User>> queries = new ArrayList<>();
        queries.add(queryUserRequest);
        queries.add(queryUserReceive);
        ParseQuery<User> mainQuery = ParseQuery.or(queries);
        mainQuery.addAscendingOrder(User.KEY_USERNAME);
        mainQuery.findInBackground(queryFriendsCallback);
    }

    /**
     * Get the users with usernames contain a string
     *
     * @param query              the string
     * @param queryUsersCallback the callback for the async function
     */
    public static void queryUsersAsync(@NonNull String query, @NonNull FindCallback<User> queryUsersCallback) {
        // get results where query is in username
        ParseQuery<User> queryUsername = ParseQuery.getQuery(User.class);
        queryUsername.whereContains(User.KEY_USERNAME_LOWERCASE, query.toLowerCase(Locale.ROOT));
        // Exclude current user
        queryUsername.whereNotEqualTo(User.KEY_USERNAME, ParseUser.getCurrentUser().getUsername());
        // Only include users with verified emails
        queryUsername.whereEqualTo(User.KEY_EMAIL_VERIFIED, true);
        queryUsername.findInBackground(queryUsersCallback);
    }

    /**
     * query for friend where current user is the requesting user and other user is the receiving user
     *
     * @param user                              the receiving user
     * @param getRequestingFriendStatusCallback the callback for the async function
     */
    public static void getRequestingFriendStatusAsync(@NonNull User user, @NonNull GetCallback<Friend> getRequestingFriendStatusCallback) {
        ParseQuery<Friend> query = ParseQuery.getQuery(Friend.class);
        query.include(Friend.KEY_ACCEPTED);
        query.whereEqualTo(Friend.KEY_REQUESTING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, user.getObjectId());
        query.getFirstInBackground(getRequestingFriendStatusCallback);
    }

    /**
     * query for friend where current user is the receiving user and other user is the requesting user
     *
     * @param user                             the requesting user
     * @param getReceivingFriendStatusCallback the callback for the async function
     */
    public static void getReceivingFriendStatusAsync(@NonNull User user, @NonNull GetCallback<Friend> getReceivingFriendStatusCallback) {
        ParseQuery<Friend> query = ParseQuery.getQuery(Friend.class);
        query.include(Friend.KEY_ACCEPTED);
        query.whereEqualTo(Friend.KEY_REQUESTING_USER_ID, user.getObjectId());
        query.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.getFirstInBackground(getReceivingFriendStatusCallback);
    }

    /**
     * query for friend between current user and other user
     *
     * @param user                    the other user
     * @param getFriendStatusCallback the callback for the async function
     */
    public static void getFriendStatusAsync(@NonNull User user, @NonNull GetCallback<Friend> getFriendStatusCallback) {
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

    /**
     * The current user sends a friend request to the other user
     *
     * @param user                  the other user receiving the friend request
     * @param requestFriendCallback the callback for the async function
     */
    public static void requestFriendAsync(@NonNull User user, SaveCallback requestFriendCallback) {
        // create the friend
        Friend friend = new Friend();
        // set the core properties
        friend.setAccepted(false);
        friend.setRequestingUser((User) ParseUser.getCurrentUser());
        friend.setRequestingUserId(ParseUser.getCurrentUser().getObjectId());
        friend.setReceivingUser(user);
        friend.setReceivingUserId(user.getObjectId());
        // invoke saveInBackground
        friend.saveInBackground(requestFriendCallback);
    }

    /**
     * The current user accepts the other user's friend request
     *
     * @param friend               the friend relation being the users
     * @param acceptFriendCallback the callback for the async function
     */
    public static void acceptFriendAsync(@NonNull Friend friend,
                                         @NonNull SaveCallback acceptFriendCallback) {
        friend.setAccepted(true);
        friend.saveInBackground(acceptFriendCallback);
    }

    /**
     * Get the friend where the current user is receiving and the other user is requesting
     *
     * @param user                       the user requesting to be friends
     * @param getFriendReceivingCallback the callback for the async function
     */
    public static void getFriendReceivingAsync(@NonNull User user,
                                               @NonNull GetCallback<Friend> getFriendReceivingCallback) {
        ParseQuery<Friend> query = ParseQuery.getQuery(Friend.class);
        query.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.whereEqualTo(Friend.KEY_REQUESTING_USER_ID, user.getObjectId());
        query.getFirstInBackground(getFriendReceivingCallback);
    }

    /**
     * Get the users that are requesting to be friends with the current user
     *
     * @param getRequestingFriendsCallback the callback for the async function
     */
    public static void getRequestingFriendsAsync(@NonNull FindCallback<User> getRequestingFriendsCallback) {
        // get the friends where the current user is being requested
        ParseQuery<Friend> queryFriend = ParseQuery.getQuery(Friend.class);
        queryFriend.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        queryFriend.whereEqualTo(Friend.KEY_ACCEPTED, false);
        ParseQuery<User> queryUser = ParseQuery.getQuery(User.class);
        // get the users requesting to be friends with the current user
        queryUser.whereMatchesKeyInQuery(User.KEY_OBJECT_ID, Friend.KEY_REQUESTING_USER_ID, queryFriend);
        queryUser.findInBackground(getRequestingFriendsCallback);
    }

    /**
     * Get the number of members in a group
     *
     * @param group                        the group
     * @param getNumMembersInGroupCallback the callback for the async function
     */
    public static void getNumMembersInGroupAsync(@NonNull Group group, @NonNull CountCallback getNumMembersInGroupCallback) {
        try {
            ParseQuery<Member> queryMembers = ParseQuery.getQuery(Member.class);
            queryMembers.whereEqualTo(Member.KEY_BOOK_ID, group.fetchIfNeeded().getString(Group.KEY_BOOK_ID));
            queryMembers.countInBackground(getNumMembersInGroupCallback);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the number of posts in a group
     *
     * @param group                      the group
     * @param getNumPostsInGroupCallback the callback for the async function
     */
    public static void getNumPostsInGroupAsync(@NonNull Group group, @NonNull CountCallback getNumPostsInGroupCallback) {
        try {
            ParseQuery<Post> queryPosts = ParseQuery.getQuery(Post.class);
            queryPosts.whereEqualTo(Post.KEY_BOOK_ID, group.fetchIfNeeded().getString(Group.KEY_BOOK_ID));
            queryPosts.countInBackground(getNumPostsInGroupCallback);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the current user's friends who are not in the group
     *
     * @param bookId                       the group's book id
     * @param getFriendsNotInGroupCallback the callback for the async function
     */
    public static void getFriendsNotInGroupAsync(@NonNull String bookId,
                                                 @NonNull FindCallback<User> getFriendsNotInGroupCallback) {
        // query friends where the current user requested and the friend received
        ParseQuery<Friend> queryFriendRequest = ParseQuery.getQuery(Friend.class);
        ParseQuery<User> queryUserRequest = ParseQuery.getQuery(User.class);
        queryFriendRequest.whereEqualTo(Friend.KEY_REQUESTING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        queryFriendRequest.whereEqualTo(Friend.KEY_ACCEPTED, true);
        queryUserRequest.whereMatchesKeyInQuery(User.KEY_OBJECT_ID, Friend.KEY_RECEIVING_USER_ID, queryFriendRequest);
        // query friends where the current user received and the friend requested
        ParseQuery<Friend> queryFriendReceive = ParseQuery.getQuery(Friend.class);
        ParseQuery<User> queryUserReceive = ParseQuery.getQuery(User.class);
        queryFriendReceive.whereEqualTo(Friend.KEY_RECEIVING_USER_ID, ParseUser.getCurrentUser().getObjectId());
        queryFriendReceive.whereEqualTo(Friend.KEY_ACCEPTED, true);
        queryUserReceive.whereMatchesKeyInQuery(User.KEY_OBJECT_ID, Friend.KEY_REQUESTING_USER_ID, queryFriendReceive);
        // Combine queries and get result
        List<ParseQuery<User>> queries = new ArrayList<>();
        queries.add(queryUserRequest);
        queries.add(queryUserReceive);
        ParseQuery<User> queryFriends = ParseQuery.or(queries);
        // Get the members in the group
        ParseQuery<Member> queryMembers = ParseQuery.getQuery(Member.class);
        queryMembers.whereEqualTo(Member.KEY_BOOK_ID, bookId);
        // Get the friends that are not members in the group
        queryFriends.whereDoesNotMatchKeyInQuery(User.KEY_OBJECT_ID, Member.KEY_USER_ID, queryMembers);
        queryFriends.findInBackground(getFriendsNotInGroupCallback);
    }

    /**
     * Saves the user's profile description
     *
     * @param user                        the user whose description is being saved
     * @param description                 the description being saved
     * @param saveUserDescriptionCallback the callback for the async function
     */
    public static void saveProfileDescriptionAsync(@NonNull User user, @NonNull String description,
                                                   @NonNull SaveCallback saveUserDescriptionCallback) {
        user.setProfileDescription(description);
        user.saveInBackground(saveUserDescriptionCallback);
    }

    /**
     * Saves the user's profile picture
     *
     * @param user                       the user whose profile picture is being saved
     * @param image                      the image being saved
     * @param saveProfilePictureCallback the callback for the async function
     */
    public static void saveProfilePictureAsync(@NonNull User user, @NonNull ParseFile image,
                                               @NonNull SaveCallback saveProfilePictureCallback) {
        user.setProfilePicture(image);
        user.saveInBackground(saveProfilePictureCallback);
    }

    /**
     * Counts the number of likes a post has
     *
     * @param post                the post
     * @param getPostLikeCallback the callback for the async function
     */
    public static void getPostLikeCountAsync(@NonNull Post post, @NonNull CountCallback getPostLikeCallback) {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.whereEqualTo(Like.KEY_POST, post);
        query.countInBackground(getPostLikeCallback);
    }

    /**
     * Gets whether or not the user likes the post
     *
     * @param post                          the post
     * @param getUserLikePostStatusCallback the callback for the async function
     */
    public static void getUserLikePostStatusAsync(@NonNull Post post,
                                                  @NonNull GetCallback<Like> getUserLikePostStatusCallback) {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.whereEqualTo(Like.KEY_POST, post);
        query.whereEqualTo(Like.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.getFirstInBackground(getUserLikePostStatusCallback);
    }

    /**
     * Creates a like relation between the post and the current user
     *
     * @param post             the post
     * @param likePostCallback the callback for the async function
     */
    public static void likePostAsync(@NonNull Post post, @NonNull SaveCallback likePostCallback) {
        Like like = new Like();
        like.setPost(post);
        like.setUser((User) ParseUser.getCurrentUser());
        like.setUserId(ParseUser.getCurrentUser().getObjectId());
        like.saveInBackground(likePostCallback);
    }

    /**
     * Deletes the like relation between the post and the current user
     *
     * @param like               the like relation
     * @param unlikePostCallback the callback for the async function
     */
    public static void unlikePostAsync(@NonNull Like like, @NonNull DeleteCallback unlikePostCallback) {
        like.deleteInBackground(unlikePostCallback);
    }

    /**
     * Get the like relation between the post and the current user
     *
     * @param post                the post
     * @param getLikePostCallback the callback for the async function
     */
    public static void getLikePostAsync(@NonNull Post post, @NonNull GetCallback<Like> getLikePostCallback) {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.whereEqualTo(Like.KEY_POST, post);
        query.whereEqualTo(Like.KEY_USER_ID, ParseUser.getCurrentUser().getObjectId());
        query.getFirstInBackground(getLikePostCallback);
    }

    /**
     * Remove a member relation
     *
     * @param member             the member relation
     * @param leaveGroupCallback the callback for the async function
     */
    public static void leaveGroupAsync(@NonNull Member member, @NonNull DeleteCallback leaveGroupCallback) {
        member.deleteInBackground(leaveGroupCallback);
    }

    /**
     * Gets the member relation between a group and a user
     *
     * @param group             the group
     * @param user              the user
     * @param getMemberCallback the callback for the async function
     */
    public static void getMemberAsync(@NonNull Group group, @NonNull User user, @NonNull GetCallback<Member> getMemberCallback) {
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        query.whereEqualTo(Member.KEY_BOOK_ID, group.getBookId());
        query.whereEqualTo(Member.KEY_USER_ID, user.getObjectId());
        query.getFirstInBackground(getMemberCallback);
    }

    /**
     * Sends user an email to reset password
     *
     * @param email                        the user's email address
     * @param requestPasswordResetCallback the callback for the async function
     */
    public static void resetPasswordAsync(@NonNull String email, @NonNull RequestPasswordResetCallback requestPasswordResetCallback) {
        ParseUser.requestPasswordResetInBackground(email, requestPasswordResetCallback);
    }

    /**
     * Adds a shelf
     *
     * @param shelfName the shelf name
     * @param addShelfCallback the callback for the async function
     * @return the newly-created shelf
     */
    public static Shelf addShelfAsync(@NonNull String shelfName, @NonNull SaveCallback addShelfCallback) {
        Shelf shelf = new Shelf();
        shelf.setShelfName(shelfName);
        shelf.setUser((User) ParseUser.getCurrentUser());
        JSONArray books = new JSONArray();
        shelf.setBooks(books);
        shelf.saveInBackground(addShelfCallback);
        return shelf;
    }

    /**
     * Creates a group for the book
     *
     * @param book the book
     * @param createBookGroupCallback the callback for the async function
     * @return the newly-created group
     */
    public static Group createBookGroupAsync(@NonNull Book book, @NonNull SaveCallback createBookGroupCallback) {
        Group group = new Group();
        group.setBookId(book.getId());
        group.setGroupName(book.getTitle() + " Group");
        group.saveInBackground(createBookGroupCallback);
        return group;
    }

    /**
     * Get the book recommendations for a user
     *
     * @param user the user
     * @param queryBookRecommendationsCallback the callback for the async function
     */
    public static void queryBookRecommendationsAsync(@NonNull User user,
                                                @NonNull FindCallback<Group> queryBookRecommendationsCallback) {
        ParseQuery<Group> groupQuery = ParseQuery.getQuery(Group.class);
        // get the groups that have a recommendation
        groupQuery.whereExists(Group.KEY_RECOMMENDED_BOOK_ID);
        // get the book only if the user is not already in the group
        ParseQuery<Member> memberQuery = ParseQuery.getQuery(Member.class);
        memberQuery.whereEqualTo(Member.KEY_USER_ID, user.getObjectId());
        groupQuery.whereDoesNotMatchKeyInQuery(Group.KEY_RECOMMENDED_BOOK_ID, Member.KEY_BOOK_ID, memberQuery);
        // include the group's name and recommended book id
        groupQuery.include(Group.KEY_GROUP_NAME);
        groupQuery.include(Group.KEY_RECOMMENDED_BOOK_ID);
        // get the list of possible groups with recommendations
        groupQuery.findInBackground(queryBookRecommendationsCallback);
    }

    /**
     * Get the user's shelves that the book is not in
     *
     * @param book the book
     * @param user the user
     * @param getShelvesNotInCallback the callback for the async function
     */
    public static void getShelvesNotInAsync(@NonNull Book book, @NonNull User user, @NonNull FindCallback<Shelf> getShelvesNotInCallback) {
        // get the user's BookOnShelf objects that have the book
        ParseQuery<BookOnShelf> bookOnShelfQuery = ParseQuery.getQuery(BookOnShelf.class);
        bookOnShelfQuery.whereEqualTo(BookOnShelf.KEY_USER, user);
        bookOnShelfQuery.whereEqualTo(BookOnShelf.KEY_BOOK_ID, book.getId());
        // get the user's shelves that do not have the BookOnShelf objects
        ParseQuery<Shelf> shelfQuery = ParseQuery.getQuery(Shelf.class);
        shelfQuery.whereEqualTo(Shelf.KEY_USER, user);
        shelfQuery.whereDoesNotMatchKeyInQuery(Shelf.KEY_OBJECT_ID, BookOnShelf.KEY_SHELF_ID, bookOnShelfQuery);
        shelfQuery.findInBackground(getShelvesNotInCallback);
    }

    /**
     * Get the number of books on a shelf
     *
     * @param shelf the shelf
     * @param getShelfBookCountCallback the callback for the async function
     */
    public static void getShelfBookCountAsync(@NonNull Shelf shelf, @NonNull CountCallback getShelfBookCountCallback) {
        ParseQuery<BookOnShelf> query = ParseQuery.getQuery(BookOnShelf.class);
        // get the books that are on the shelf
        query.whereEqualTo(BookOnShelf.KEY_SHELF_ID, shelf.getObjectId());
        query.countInBackground(getShelfBookCountCallback);
    }

    /**
     * Add the book to the sher
     *
     * @param book the book
     * @param shelf the shelf
     * @param user the user adding the book to the shelf
     * @param addBookToShelfCallback the callback for the async function
     */
    public static void addBookToShelfAsync(@NonNull Book book, @NonNull Shelf shelf,
                                           @NonNull User user, @NonNull SaveCallback addBookToShelfCallback) {
        BookOnShelf bookOnShelf = new BookOnShelf();
        bookOnShelf.setShelf(shelf);
        bookOnShelf.setShelfId(shelf.getObjectId());
        bookOnShelf.setBookId(book.getId());
        bookOnShelf.setBookAuthor(book.getAuthor());
        bookOnShelf.setBookTitle(book.getTitle());
        bookOnShelf.setBookCoverUrl(book.getCoverUrl());
        bookOnShelf.setUser(user);
        bookOnShelf.saveInBackground(addBookToShelfCallback);
    }

    /**
     * Get the books on a shelf
     *
     * @param shelf the shelf
     * @param getBooksOnShelfCallback the callback for the async function
     */
    public static void getBooksOnShelfAsync(@NonNull Shelf shelf,
                                            @NonNull FindCallback<BookOnShelf> getBooksOnShelfCallback) {
        ParseQuery<BookOnShelf> query = ParseQuery.getQuery(BookOnShelf.class);
        query.whereEqualTo(BookOnShelf.KEY_SHELF_ID, shelf.getObjectId());
        query.whereEqualTo(BookOnShelf.KEY_USER, ParseUser.getCurrentUser());
        query.findInBackground(getBooksOnShelfCallback);
    }
}
