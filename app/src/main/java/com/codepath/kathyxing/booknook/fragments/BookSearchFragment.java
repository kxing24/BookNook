package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.adapters.BookAdapter;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.net.BookClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookSearchFragment extends Fragment {

    // the fragment parameters
    public static final String TAG = "BookSearchFragment";
    public static final int MAX_RESULTS = 10;
    private RecyclerView rvBooks;
    private ProgressBar pbLoading;
    private TextView tvNoResults;
    private Button btnAdvancedSearch;
    private Button btnPrevPage;
    private Button btnNextPage;
    private TextView tvPageNumber;
    private BookAdapter bookAdapter;
    private BookClient client;
    private ArrayList<Book> books;
    private String savedQuery;
    private int pageNumber = 0;
    private int totalItems;

    // Required empty public constructor
    public BookSearchFragment() {}

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
        pbLoading = view.findViewById(R.id.pbLoading);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        btnAdvancedSearch = view.findViewById(R.id.btnAdvancedSearch);
        tvPageNumber = view.findViewById(R.id.tvPageNumber);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        books = new ArrayList<>();
        bookAdapter = new BookAdapter(getContext(), books);

        // Attach the adapter to the RecyclerView
        rvBooks.setAdapter(bookAdapter);

        // Set layout manager to position the items
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));

        // set up a click handler for the advanced search button
        btnAdvancedSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // swap in the advanced search fragment
                AdvancedSearchFragment nextFragment= new AdvancedSearchFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(), nextFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // set up a click handler for bookAdapter
        bookAdapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                // get the book clicked
                Book book = books.get(position);
                // swap in the book detail fragment
                BookDetailFragment nextFragment = new BookDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Book.class.getSimpleName(), Parcels.wrap(book));
                nextFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(), nextFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // set up a click handler for the prev page button
        btnPrevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber--;
                // Get the new books
                fetchBooks(savedQuery, pageNumber);
            }
        });

        // set up a click handler for the next page button
        btnNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber++;
                // Get the new books
                fetchBooks(savedQuery, pageNumber);
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_book_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // set actions for when the searchItem expands and collapses
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // hide the advanced search button
                btnAdvancedSearch.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                tvNoResults.setVisibility(View.GONE);
                // show the advanced search button
                btnAdvancedSearch.setVisibility(View.VISIBLE);
                // clear the items from the adapter
                bookAdapter.clear();
                bookAdapter.notifyDataSetChanged();
                return true;
            }
        });

        // Set the textlistener for the searchview
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // TODO: results show up as user types
            @Override
            public boolean onQueryTextSubmit(String query) {
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
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

    // Executes an API call to the Google Books search endpoint, parses the results
    // Converts them into an array of book objects and adds them to the adapter
    private void fetchBooks(String query, int pageNumber) {
        pbLoading.setVisibility(View.VISIBLE);
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

        // initialize a book client to get API data
        client = new BookClient();
        client.getBooks(query, startIndex, MAX_RESULTS, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Headers headers, JSON response) {
                try {
                    JSONArray items;
                    if (response != null) {
                        totalItems = response.jsonObject.getInt("totalItems");
                        if(totalItems == 0) {
                            tvNoResults.setVisibility(View.VISIBLE);
                        }
                        else {
                            // Get the items json array
                            items = response.jsonObject.getJSONArray("items");
                            // Parse json array into array of model objects
                            final ArrayList<Book> b = Book.fromJson(items);
                            // Load model objects into the adapter
                            for (Book book : b) {
                                books.add(book); // add book through the adapter
                            }
                            bookAdapter.notifyDataSetChanged();
                            // Set view visibilities
                            btnNextPage.setVisibility(View.VISIBLE);
                            if(pageNumber > 0) {
                                btnPrevPage.setVisibility(View.VISIBLE);
                            }
                            if(totalItems - startIndex <= 10) {
                                btnNextPage.setVisibility(View.GONE);
                            }
                            tvPageNumber.setText("Page " + (pageNumber + 1));
                            tvPageNumber.setVisibility(View.VISIBLE);
                        }
                        pbLoading.setVisibility(View.GONE);
                        Log.i(TAG, "fetch books success");
                    }
                } catch (JSONException e) {
                    // Invalid JSON format, show appropriate error.
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String responseString, Throwable throwable) {
                // Handle failed request here
                Log.e(TAG, "Request failed with code " + statusCode + ". Response message: " + responseString);
            }
        });
    }
}