package com.codepath.kathyxing.booknook.fragments;

import static android.app.Activity.RESULT_OK;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.GroupFeedActivity;
import com.codepath.kathyxing.booknook.activities.MainActivity;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.BookQueryManager;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.Member;
import com.codepath.kathyxing.booknook.parse_classes.Shelf;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.apache.commons.text.similarity.CosineDistance;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class SameSubjectBookStructure {
    Book book;
    double distance;
}

class SameSubjectBookComparator implements Comparator<SameSubjectBookStructure> {

    // sort in ascending order by distance
    @Override
    public int compare(SameSubjectBookStructure o1, SameSubjectBookStructure o2) {
        return Double.compare(o1.distance, o2.distance);
    }
}

/**
 * A simple {@link Fragment} subclass.
 */

public class BookDetailFragment extends Fragment {

    // fragment parameters
    public static final String TAG = "BookDetailActivity";
    public static final int MAX_RESULTS = 40;
    public static final int TOTAL_RESULTS = 200;
    public static final int NUM_RECOMMENDED_BOOKS = 5;
    private static final int GET_LEFT_GROUP = 10;
    private Button btnShelveBook;
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
    public void onResume() {
        super.onResume();
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle("Book Details");
        }
        ((MainActivity) getActivity()).showBackButton();
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
        ImageView ivBookCover = view.findViewById(R.id.ivBookCover);
        TextView tvTitle = view.findViewById(R.id.tvGroupTitle);
        TextView tvAuthor = view.findViewById(R.id.tvAuthor);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        btnShelveBook = view.findViewById(R.id.btnShelveBook);
        tvNumMembers = view.findViewById(R.id.tvNumMembers);
        btnJoinGroup = view.findViewById(R.id.btnJoinGroup);
        btnCreateGroup = view.findViewById(R.id.btnCreateGroup);
        btnGoToGroup = view.findViewById(R.id.btnGotoGroup);
        pbLoading = view.findViewById(R.id.pbLoading);
        // Extract book object from the bundle
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            book = Parcels.unwrap(bundle.getParcelable(Book.class.getSimpleName()));
        }
        // Check if the book group exists and whether the user is already in the group
        // Get the recommended books
        bookGroupStatus();
        // Set view text
        tvTitle.setText(book.getTitle());
        if (book.getAuthor().equals("")) {
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
        // Click handler for the shelve book button
        btnShelveBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the list of shelves that the book is not in
                FindCallback<Shelf> getShelvesNotInCallback = new FindCallback<Shelf>() {
                    @Override
                    public void done(List<Shelf> shelves, ParseException e) {
                        if (e == null) {
                            showShelvesAlertDialog(new ArrayList<>(shelves));
                        } else {
                            Log.e(TAG, "issue getting shelves", e);
                        }
                    }
                };
                ParseQueryUtilities.getShelvesNotInAsync(book, (User) ParseUser.getCurrentUser(), getShelvesNotInCallback);
            }
        });
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
            addMemberWithBook(book);
            // set button visibility
            btnJoinGroup.setVisibility(View.GONE);
            tvNumMembers.setText(++numMembers + " members");
        });
        // Click handler for goto group button
        btnGoToGroup.setOnClickListener(v -> goGroupFeedActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_LEFT_GROUP && resultCode == RESULT_OK) {
            if (data != null) {
                boolean leftGroup = data.getExtras().getBoolean("leftGroup");
                if (leftGroup) {
                    btnGoToGroup.setVisibility(View.GONE);
                    btnJoinGroup.setVisibility(View.VISIBLE);
                    tvNumMembers.setText(--numMembers + " members");
                }
            }
        }
    }

    private void bookGroupStatus() {
        // Create a callback for bookGroupStatusAsync
        GetCallback<Group> bookGroupStatusCallback = (object, e) -> {
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
        GetCallback<Member> userInGroupCallback = (object, e) -> {
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

    private void addMemberWithBook(Book book) {
        // create a callback for getGroupFromBookCallback
        GetCallback<Group> getGroupFromBookCallback = (group, e) -> {
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
                tvNumMembers.setText(++numMembers + " members");
            }
        };
        ParseQueryUtilities.addMemberWithGroupAsync(group,
                (User) User.getCurrentUser(), book.getId(), addMemberWithGroupCallback);
    }

    // Creates the group and adds the first user
    private void bookGroupCreate(@NonNull Book book) {
        // creates the group
        SaveCallback createBookGroupCallback = e -> {
            if (e != null) {
                Log.e(TAG, "Error while creating group", e);
                Toast.makeText(getContext(), "Error while creating group!", Toast.LENGTH_SHORT).show();
            } else {
                // make the user a member of the group
                addMemberWithGroup(bookGroup);
                // if the book has a subject, add book recommendations
                if (!book.getSubject().equals("")) {
                    setBookRecommendation(book, bookGroup);
                }
            }
        };
        bookGroup = ParseQueryUtilities.createBookGroupAsync(book, createBookGroupCallback);
    }

    // set the number of members in the group
    private void setNumMembers(@NonNull Group group) {
        CountCallback getNumMembersInGroupCallback = (count, e) -> {
            tvNumMembers.setText(count + " members");
            numMembers = count;
        };
        ParseQueryUtilities.getNumMembersInGroupAsync(group, getNumMembersInGroupCallback);
    }

    // return an array of books with the same subject as the given book
    private void setBookRecommendation(@NonNull Book book, @NonNull Group group) {
        String query = "subject:\"" + book.getSubject() + "\"";
        // create an array to store all the books from the query
        ArrayList<Book> bookArray = new ArrayList<>();
        for (int i = 0; i < TOTAL_RESULTS / MAX_RESULTS; i++) {
            int startIndex = i * MAX_RESULTS;
            // make API call to get books
            BookQueryManager.getInstance().getBooks(query, startIndex, MAX_RESULTS, new BookQueryManager.BooksCallback() {
                @Override
                public void onSuccess(int statusCode, ArrayList<Book> books, int totalItems) {
                    if (totalItems < startIndex) {
                        return;
                    } else {
                        // Add the books to the overall array
                        bookArray.addAll(books);
                    }
                    if (totalItems < startIndex + MAX_RESULTS || startIndex + MAX_RESULTS >= TOTAL_RESULTS) {
                        // Finished querying items
                        Book recommendedBook = getRecommendedBook(book, bookArray);
                        group.setRecommendedBookId(recommendedBook.getId());
                        group.saveInBackground();
                    }
                    Log.i(TAG, "fetch books success");
                }

                @Override
                public void onFailure(int errorCode) {
                    Log.e(TAG, "Request failed with code " + errorCode);
                }
            });
        }
    }

    // get something similar to the book from sameSubjectBooks
    private Book getRecommendedBook(Book book, ArrayList<Book> sameSubjectBooks) {
        // create an arraylist to store the five most similar books to recommend
        ArrayList<SameSubjectBookStructure> similarBooks = new ArrayList<>();
        for (int i = 0; i < sameSubjectBooks.size(); i++) {
            if (!sameSubjectBooks.get(i).getId().equals(book.getId())) {
                SameSubjectBookStructure recommendedBook = new SameSubjectBookStructure();
                recommendedBook.book = sameSubjectBooks.get(i);
                recommendedBook.distance = getBookDistance(sameSubjectBooks.get(i), book);
                if (similarBooks.size() < NUM_RECOMMENDED_BOOKS) {
                    similarBooks.add(recommendedBook);
                } else if (recommendedBook.distance < similarBooks.get(NUM_RECOMMENDED_BOOKS - 1).distance) {
                    similarBooks.set(NUM_RECOMMENDED_BOOKS - 1, recommendedBook);
                }
                Collections.sort(similarBooks, new SameSubjectBookComparator());
            }
        }
        // return the first book in the arraylist (the most similar)
        return similarBooks.get(0).book;
    }

    private double getBookDistance(@NonNull Book from, @NonNull Book to) {
        double distance = 0;
        ArrayList<String> fromAuthorArray = from.getAuthorArray();
        ArrayList<String> toAuthorArray = to.getAuthorArray();
        // compare authors
        if (fromAuthorArray == null || toAuthorArray == null) {
            distance += 1;
        } else {
            fromAuthorArray.retainAll(toAuthorArray);
            if (fromAuthorArray.isEmpty()) {
                // no similar authors
                distance += 1;
            }
        }
        // compare descriptions
        if (from.getDescription().equals("") || to.getDescription().equals("")) {
            distance += 10;
        } else {
            CosineDistance cosineDistance = new CosineDistance();
            distance += cosineDistance.apply(from.getDescription(), to.getDescription()) * (double) 10;
        }
        // compare maturity rating
        if (from.getMaturityRating().equals("") || to.getMaturityRating().equals("") || !from.getMaturityRating().equals(to.getMaturityRating())) {
            // different maturity ratings
            distance += 0.5;
        }
        // compare print type
        if (from.getPrintType().equals("") || to.getPrintType().equals("") || !from.getPrintType().equals(to.getPrintType())) {
            // different print types
            distance += 2;
        }
        // compare page count
        if (from.getPageCount() < 0 || to.getPageCount() <= 0) {
            distance += 1;
        } else {
            distance += Double.min(1, (double) Math.abs(from.getPageCount() - to.getPageCount()) / to.getPageCount());
        }
        // compare language
        if (from.getLanguage().equals("") || to.getLanguage().equals("") || !from.getLanguage().equals(to.getLanguage())) {
            // different languages
            distance += 10;
        }
        return distance;
    }

    private void showShelvesAlertDialog(ArrayList<Shelf> shelves) {
        ArrayList<Shelf> selectedShelves = new ArrayList<>();
        boolean[] selected = new boolean[shelves.size()];
        String[] shelfNames = new String[shelves.size()];
        for (int i = 0; i < shelves.size(); i++) {
            shelfNames[i] = shelves.get(i).getShelfName();
        }
        // Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // set title
        builder.setTitle("Select Shelves");
        // set dialog non cancelable
        builder.setCancelable(false);
        builder.setMultiChoiceItems(shelfNames, selected, (dialogInterface, i, b) -> {
            // check condition
            if (b) {
                // when checkbox selected, add position to selectedShelvesPositions
                selectedShelves.add(shelves.get(i));
            } else {
                // when checkbox unselected, remove position from selectedShelvesPositions
                selectedShelves.remove(shelves.get(i));
            }
        });
        builder.setPositiveButton("Add", (dialogInterface, i) -> {
            // Add the books to the shelves
            addBookToShelves(book, selectedShelves);
            Toast.makeText(getContext(), "Added book to shelves!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            // dismiss dialog
            dialogInterface.dismiss();
        });
        builder.show();
    }

    // add the book to the selected shelves
    private void addBookToShelves(Book book, ArrayList<Shelf> shelves) {
        for (Shelf shelf : shelves) {
            SaveCallback addBookToShelfCallback = e -> {
                if (e != null) {
                    Log.e(TAG, "issue adding book to shelf", e);
                }
            };
            ParseQueryUtilities.addBookToShelfAsync(book, shelf, (User) ParseUser.getCurrentUser(),
                    addBookToShelfCallback);
        }
    }

    private void goGroupFeedActivity() {
        Intent i = new Intent(getContext(), GroupFeedActivity.class);
        i.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
        i.putExtra("group", bookGroup);
        startActivityForResult(i, GET_LEFT_GROUP);
    }
}