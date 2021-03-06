package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.adapters.BookAdapter;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.BookQueryManager;
import com.codepath.kathyxing.booknook.parse_classes.BookRecommendation;
import com.codepath.kathyxing.booknook.parse_classes.Group;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookSearchFragment extends Fragment {

    // the fragment parameters
    public static final String TAG = "BookSearchFragment";
    public static final int MAX_RESULTS = 10;
    private RecyclerView rvBooks;
    private LottieAnimationView avBookSearchLoading;
    private TextView tvNoResults;
    private Button btnAdvancedSearch;
    private Button btnPrevPage;
    private Button btnNextPage;
    private TextView tvPageNumber;
    private TextView tvRecommendationTitle;
    private RelativeLayout rlBookRecommendation;
    private ImageView ivBookCover;
    private TextView tvTitle;
    private TextView tvAuthor;
    private BookAdapter bookAdapter;
    private ArrayList<Book> books;
    private String savedQuery;
    private int pageNumber = 0;
    private Book recommendedBook;
    private SearchView searchView;

    // Required empty public constructor
    public BookSearchFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_search, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getString(R.string.book_search));
        }
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize fields
        rvBooks = view.findViewById(R.id.rvBooks);
        avBookSearchLoading = view.findViewById(R.id.avBookSearchLoading);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        btnAdvancedSearch = view.findViewById(R.id.btnAdvancedSearch);
        tvPageNumber = view.findViewById(R.id.tvPageNumber);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        tvRecommendationTitle = view.findViewById(R.id.tvRecommendationTitle);
        rlBookRecommendation = view.findViewById(R.id.rlBookRecommendation);
        ivBookCover = view.findViewById(R.id.ivBookCover);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvAuthor = view.findViewById(R.id.tvAuthor);
        books = new ArrayList<>();
        bookAdapter = new BookAdapter(getContext(), books);
        // Attach the adapter to the RecyclerView
        rvBooks.setAdapter(bookAdapter);
        // Set layout manager to position the items
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvBooks.setLayoutManager(layoutManager);
        // add divider between items
        DividerItemDecoration itemDecor = new DividerItemDecoration(requireContext(), layoutManager.getOrientation());
        rvBooks.addItemDecoration(itemDecor);
        // set up a click handler for the advanced search button
        btnAdvancedSearch.setOnClickListener(v -> {
            // swap in the advanced search fragment
            if (getActivity() != null && getView() != null) {
                AdvancedSearchFragment nextFragment = new AdvancedSearchFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup) getView().getParent()).getId(), nextFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        // set up a click handler for the book recommendation
        rlBookRecommendation.setOnClickListener(v -> {
            // swap in the book detail fragment
            BookDetailFragment nextFragment = new BookDetailFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putParcelable(Book.class.getSimpleName(), Parcels.wrap(recommendedBook));
            nextFragment.setArguments(bundle1);
            if (getActivity() != null && getView() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup) getView().getParent()).getId(), nextFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        // set up a click handler for bookAdapter
        bookAdapter.setOnItemClickListener((itemView, position) -> {
            // get the book clicked
            Book book = books.get(position);
            // swap in the book detail fragment
            BookDetailFragment nextFragment = new BookDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(Book.class.getSimpleName(), Parcels.wrap(book));
            nextFragment.setArguments(bundle);
            if (getActivity() != null && getView() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup) getView().getParent()).getId(), nextFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        // set up a click handler for the prev page button
        btnPrevPage.setOnClickListener(v -> {
            pageNumber--;
            // Get the new books
            fetchBooks(savedQuery, pageNumber);
        });
        // set up a click handler for the next page button
        btnNextPage.setOnClickListener(v -> {
            pageNumber++;
            // Get the new books
            fetchBooks(savedQuery, pageNumber);
        });
        getBookRecommendation();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_book_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // set actions for when the searchItem expands and collapses
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // hide the advanced search button and book recommendation
                btnAdvancedSearch.setVisibility(View.GONE);
                tvRecommendationTitle.setVisibility(View.GONE);
                rlBookRecommendation.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                tvNoResults.setVisibility(View.GONE);
                btnNextPage.setVisibility(View.GONE);
                btnPrevPage.setVisibility(View.GONE);
                tvPageNumber.setVisibility(View.GONE);
                // show the advanced search button and book recommendation
                btnAdvancedSearch.setVisibility(View.VISIBLE);
                tvRecommendationTitle.setVisibility(View.VISIBLE);
                rlBookRecommendation.setVisibility(View.VISIBLE);
                // clear the items from the adapter
                bookAdapter.clear();
                return true;
            }
        });
        // Set the textlistener for the searchview
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // set the page number back to 0
                pageNumber = 0;
                // perform query here
                fetchBooks(query, pageNumber);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                searchView.clearFocus();
                // save the query
                savedQuery = query;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                tvNoResults.setVisibility(View.GONE);
                if (newText.length() > 3) {
                    // set the page number back to 0
                    pageNumber = 0;
                    // perform query here
                    fetchBooks(newText, pageNumber);
                    // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                    //searchView.clearFocus();
                    // save the query
                    savedQuery = newText;
                } else {
                    // clear the adapter
                    bookAdapter.clear();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Executes an API call to the Google Books search endpoint, parses the results
    // Converts them into an array of book objects and adds them to the adapter
    private void fetchBooks(String query, int pageNumber) {
        avBookSearchLoading.setVisibility(View.VISIBLE);
        int startIndex = pageNumber * MAX_RESULTS;
        Log.i(TAG, "startIndex is " + startIndex);
        // smooth scroll to top
        rvBooks.smoothScrollToPosition(0);
        // adjust view visibility
        tvPageNumber.setVisibility(View.GONE);
        btnNextPage.setVisibility(View.GONE);
        btnPrevPage.setVisibility(View.GONE);
        // Remove books from the adapter
        bookAdapter.clear();
        // get API data
        BookQueryManager.getInstance().getBooks(query, startIndex, MAX_RESULTS, new BookQueryManager.BooksCallback() {
            @Override
            public void onSuccess(int statusCode, ArrayList<Book> bookList, int totalItems) {
                if (totalItems == 0) {
                    tvNoResults.setVisibility(View.VISIBLE);
                    bookAdapter.clear();
                } else {
                    tvNoResults.setVisibility(View.GONE);
                    // Load model objects into the adapter
                    books.addAll(bookList);
                    bookAdapter.notifyDataSetChanged();
                    // Set view visibilities
                    btnPrevPage.setVisibility(pageNumber > 0 ? View.VISIBLE : View.GONE);
                    btnNextPage.setVisibility(totalItems - startIndex <= MAX_RESULTS ? View.GONE : View.VISIBLE);
                    tvPageNumber.setText("Page " + (pageNumber + 1));
                    tvPageNumber.setVisibility(View.VISIBLE);
                }
                avBookSearchLoading.setVisibility(View.GONE);
                Log.i(TAG, "fetch books success");
            }

            @Override
            public void onFailure(int errorCode) {
                if (errorCode == BookQueryManager.NO_BOOKS_FOUND) {
                    tvNoResults.setVisibility(View.VISIBLE);
                    bookAdapter.clear();
                } else {
                    Log.e(TAG, "Request failed with code " + errorCode);
                }
                avBookSearchLoading.setVisibility(View.GONE);
            }
        });
        // hide the keyboard on recyclerview touch
        rvBooks.setOnTouchListener((v, event) -> {
            //Hide keyboard code
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = requireActivity().getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(getContext());
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return false;
        });
    }

    private void getBookRecommendation() {
        // randomly get a recommendation from group or genre
        Random random = new Random();
        if (random.nextBoolean()) {
            // get recommendation from group
            getBookRecommendationFromGroup();
        } else {
            // get recommendation from genre
            getBookRecommendationFromGenre();
        }
    }

    private void getBookRecommendationFromGroup() {
        // get the list of groups with possible recommendations
        FindCallback<Group> queryBookRecommendationsCallback = (groups, e) -> {
            if (e == null && !groups.isEmpty()) {
                // randomly get a recommendation from group
                Collections.shuffle(groups);
                Group group = groups.get(0);
                String recommendationTitle = "Because you are in " + group.getGroupName() + ", you might enjoy:";
                displayBookRecommendation(recommendationTitle, group.getRecommendedBookId());
            } else {
                // get a recommendation from genre
                getBookRecommendationFromGenre();
                if (e != null) {
                    Log.e(TAG, "issue getting groups with book recommendations", e);
                }
            }
        };
        ParseQueryUtilities.queryBookRecommendationsAsync((User) ParseUser.getCurrentUser(), queryBookRecommendationsCallback);
    }

    private void getBookRecommendationFromGenre() {
        FindCallback<BookRecommendation> getBookRecommendationsCallback = (recommendations, e) -> {
            if (e == null && !recommendations.isEmpty()) {
                Log.i(TAG, "number of recommendations: " + recommendations.size());
                Collections.shuffle(recommendations);
                String recommendationTitle = "Because you are interested in " + recommendations.get(0).getGenre() + ", you might enjoy:";
                displayBookRecommendation(recommendationTitle, recommendations.get(0).getBookId());
            } else if (recommendations.isEmpty()) {
                Log.e(TAG, "no recommendations");
            } else {
                Log.e(TAG, "issue getting recommendations", e);
            }
        };
        ParseQueryUtilities.getBookRecommendationAsync((User) ParseUser.getCurrentUser(), getBookRecommendationsCallback);
    }

    private void displayBookRecommendation(String recommendationTitle, String bookId) {
        // get the book from the bookId
        BookQueryManager.getInstance().getBook(bookId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Book book = Book.fromJson(json.jsonObject);
                recommendedBook = book;
                // display the recommendation
                tvRecommendationTitle.setText(recommendationTitle);
                if (book != null) {
                    tvTitle.setText(book.getTitle());
                    tvAuthor.setText(book.getAuthor());
                    if (getContext() != null) {
                        if (book.getCoverUrl() != null) {
                            Glide.with(getContext())
                                    .load(Uri.parse(book.getCoverUrl()))
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.ic_nocover))
                                    .into(ivBookCover);
                        } else {
                            Glide.with(getContext()).load(R.drawable.ic_nocover).into(ivBookCover);
                        }
                    }
                }
                if (searchView != null) {
                    tvRecommendationTitle.setVisibility(searchView.isIconified() ? View.VISIBLE : View.GONE);
                    rlBookRecommendation.setVisibility(searchView.isIconified() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "Request failed with code " + statusCode + ". Response message: " + response);
            }
        });
    }
}