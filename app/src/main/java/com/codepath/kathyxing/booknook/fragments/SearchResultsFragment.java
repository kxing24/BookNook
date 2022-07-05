package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.adapters.BookAdapter;
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
public class SearchResultsFragment extends Fragment {

    // activity parameters
    public static final String TAG = "SearchResultsFragment";
    public static final int MAX_RESULTS = 10;
    private RecyclerView rvBooks;
    private ProgressBar pbLoading;
    private TextView tvNoResults;
    private Button btnPrevPage;
    private Button btnNextPage;
    private TextView tvPageNumber;
    private BookAdapter bookAdapter;
    private BookClient client;
    private ArrayList<Book> books;
    private int pageNumber = 0;
    private int totalItems;
    private String anyField;
    private String title;
    private String author;
    private String publisher;
    private String subject;
    private String isbn;
    private String savedQuery = "";
    private boolean firstParameter = true;

    // Required empty public constructor
    public SearchResultsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle("Search Results");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Extract strings from the bundle
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            anyField = bundle.getString("anyField");
            title = bundle.getString("title");
            author = bundle.getString("author");
            publisher = bundle.getString("publisher");
            subject = bundle.getString("subject");
            isbn = bundle.getString("isbn");
        }

        // set the query based on the extras
        setQuery();
        Log.i(TAG, "query is: " + savedQuery);

        // initialize fields
        rvBooks = view.findViewById(R.id.rvBooks);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        tvPageNumber = view.findViewById(R.id.tvPageNumber);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        books = new ArrayList<>();
        bookAdapter = new BookAdapter(getContext(), books);

        // Attach the adapter to the RecyclerView
        rvBooks.setAdapter(bookAdapter);

        // Set layout manager to position the items
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));

        // set up a click handler for bookAdapter
        bookAdapter.setOnItemClickListener((itemView, position) -> {
            // get the book clicked
            Book book = books.get(position);
            // swap in the book detail fragment
            BookDetailFragment nextFragment = new BookDetailFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putParcelable(Book.class.getSimpleName(), Parcels.wrap(book));
            nextFragment.setArguments(bundle1);
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
            fetchBooks(pageNumber);
        });

        // set up a click handler for the next page button
        btnNextPage.setOnClickListener(v -> {
            pageNumber++;
            // Get the new books
            fetchBooks(pageNumber);
        });

        fetchBooks(pageNumber);
    }

    // Executes an API call to the Google Books search endpoint, parses the results
    // Converts them into an array of book objects and adds them to the adapter
    private void fetchBooks(int pageNumber) {
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
        client.getBooks(savedQuery, startIndex, MAX_RESULTS, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON response) {
                try {
                    JSONArray items;
                    if (response != null) {
                        // Set the total items
                        totalItems = response.jsonObject.getInt("totalItems");
                        if (totalItems == 0) {
                            tvNoResults.setVisibility(View.VISIBLE);
                        } else {
                            // Get the items json array
                            items = response.jsonObject.getJSONArray("items");
                            // Parse json array into array of model objects
                            final ArrayList<Book> b = Book.fromJson(items);
                            // Load model objects into the adapter
                            books.addAll(b);
                            bookAdapter.notifyDataSetChanged();
                            // Set view visibilities
                            btnNextPage.setVisibility(View.VISIBLE);
                            if (pageNumber > 0) {
                                btnPrevPage.setVisibility(View.VISIBLE);
                            }
                            if (totalItems - startIndex <= 10) {
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
                    //TODO: handle the case "no value for items"
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String responseString, Throwable throwable) {
                // Handle failed request here
                if (statusCode == 400) {
                    // query missing
                    Toast.makeText(getContext(), "Did not receive a query, try again!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Issue with query, try again!", Toast.LENGTH_SHORT).show();
                }
                pbLoading.setVisibility(View.GONE);
                Log.e(TAG, "Request failed with code " + statusCode + ". Response message: " + responseString);
                // swap in the advanced search fragment
                AdvancedSearchFragment nextFragment = new AdvancedSearchFragment();
                if (getActivity() != null && getView() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(((ViewGroup) getView().getParent()).getId(), nextFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
    }

    private void setQuery() {
        if (!anyField.equals("")) {
            if (!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += anyField;
        }
        if (!title.equals("")) {
            if (!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += ("intitle:" + title);
        }
        if (!author.equals("")) {
            if (!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += ("inauthor:" + author);
        }
        if (!publisher.equals("")) {
            if (!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += ("inpublisher:" + publisher);
        }
        if (!subject.equals("")) {
            if (!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += ("subject:" + "\"" + subject + "\"");
        }
        if (!isbn.equals("")) {
            if (!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += ("isbn:" + isbn);
        }
    }
}