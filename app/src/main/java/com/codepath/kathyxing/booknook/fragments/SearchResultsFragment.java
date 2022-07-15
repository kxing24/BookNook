package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.MainActivity;
import com.codepath.kathyxing.booknook.adapters.BookAdapter;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.BookQueryManager;

import org.parceler.Parcels;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchResultsFragment extends Fragment {

    // activity parameters
    public static final String TAG = "SearchResultsFragment";
    public static final int MAX_RESULTS = 10;
    private RecyclerView rvBooks;
    private LottieAnimationView avBookSearchLoading;
    private TextView tvNoResults;
    private Button btnPrevPage;
    private Button btnNextPage;
    private TextView tvPageNumber;
    private BookAdapter bookAdapter;
    private ArrayList<Book> books;
    private int pageNumber = 0;
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
    public void onResume() {
        super.onResume();
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle("Search Results");
        }
        ((MainActivity) getActivity()).showBackButton();
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
        avBookSearchLoading = view.findViewById(R.id.avBookSearchLoading);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        tvPageNumber = view.findViewById(R.id.tvPageNumber);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
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
        // set up a click handler for bookAdapter
        bookAdapter.setOnItemClickListener((itemView, position) -> {
            // get the book clicked
            Book book = books.get(position);
            // swap in the book detail fragment
            BookDetailFragment nextFragment = new BookDetailFragment();
            Bundle newBundle = new Bundle();
            newBundle.putParcelable(Book.class.getSimpleName(), Parcels.wrap(book));
            nextFragment.setArguments(newBundle);
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
        avBookSearchLoading.setVisibility(View.VISIBLE);
        int startIndex = pageNumber * MAX_RESULTS;
        // smooth scroll to top
        rvBooks.smoothScrollToPosition(0);
        // adjust view visibility
        tvPageNumber.setVisibility(View.GONE);
        btnNextPage.setVisibility(View.GONE);
        btnPrevPage.setVisibility(View.GONE);
        // Remove books from the adapter
        bookAdapter.clear();
        // get API data
        BookQueryManager.getInstance().getBooks(savedQuery, startIndex, MAX_RESULTS, new BookQueryManager.BooksCallback() {
            @Override
            public void onSuccess(int statusCode, ArrayList<Book> bookList, int totalItems) {
                // Load model objects into the adapter
                books.addAll(bookList);
                bookAdapter.notifyDataSetChanged();
                // Set view visibilities
                btnPrevPage.setVisibility(pageNumber > 0 ? View.VISIBLE : View.GONE);
                btnNextPage.setVisibility(totalItems - startIndex <= MAX_RESULTS ? View.GONE : View.VISIBLE);
                tvPageNumber.setText("Page " + (pageNumber + 1));
                tvPageNumber.setVisibility(View.VISIBLE);
                avBookSearchLoading.setVisibility(View.GONE);
                Log.i(TAG, "fetch books success");
            }

            @Override
            public void onFailure(int errorCode) {
                if (errorCode == BookQueryManager.NO_BOOKS_FOUND) {
                    tvNoResults.setVisibility(View.VISIBLE);
                } else {
                    // Handle failed request here
                    Toast.makeText(getContext(), "Issue with query, try again!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Request failed with code " + errorCode);
                    // swap in the advanced search fragment
                    AdvancedSearchFragment nextFragment = new AdvancedSearchFragment();
                    if (getActivity() != null && getView() != null) {
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(((ViewGroup) getView().getParent()).getId(), nextFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                }
                avBookSearchLoading.setVisibility(View.GONE);
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