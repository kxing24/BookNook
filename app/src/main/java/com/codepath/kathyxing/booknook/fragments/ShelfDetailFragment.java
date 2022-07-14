package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.MainActivity;
import com.codepath.kathyxing.booknook.adapters.ShelfDetailAdapter;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.BookQueryManager;
import com.codepath.kathyxing.booknook.parse_classes.BookOnShelf;
import com.codepath.kathyxing.booknook.parse_classes.Shelf;
import com.parse.FindCallback;

import org.parceler.Parcels;

import java.util.ArrayList;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShelfDetailFragment extends Fragment {

    // fragment parameters
    public static final String TAG = "ShelfDetailFragment";
    private RecyclerView rvBooksOnShelf;
    private TextView tvNoBooks;
    private ProgressBar pbLoading;
    private ShelfDetailAdapter shelfDetailAdapter;
    private ArrayList<BookOnShelf> booksOnShelf;
    private Shelf shelf;

    public ShelfDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle("Shelf Details");
        }
        ((MainActivity) getActivity()).showBackButton();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shelf_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // extract the shelf object
        if (this.getArguments() != null) {
            shelf = this.getArguments().getParcelable("shelf");
        }
        // initialize fields
        rvBooksOnShelf = view.findViewById(R.id.rvBooksOnShelf);
        tvNoBooks = view.findViewById(R.id.tvNoBooks);
        pbLoading = view.findViewById(R.id.pbLoading);
        booksOnShelf = new ArrayList<>();
        shelfDetailAdapter = new ShelfDetailAdapter(getContext(), booksOnShelf);
        // Attach the adapter to the RecyclerView
        rvBooksOnShelf.setAdapter(shelfDetailAdapter);
        // Set layout manager to position the items
        rvBooksOnShelf.setLayoutManager(new LinearLayoutManager(getContext()));
        // set up a click handler for shelfDetailAdapter
        shelfDetailAdapter.setOnItemClickListener((itemView, position) -> {
            // get the BookOnShelf clicked
            BookOnShelf bookOnShelf = booksOnShelf.get(position);
            // get the book
            BookQueryManager.getInstance().getBook(bookOnShelf.getBookId(), new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Book book = Book.fromJson(json.jsonObject);
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
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    // Handle failed request here
                    Log.e(TAG, "Request failed with code " + statusCode + ". Response message: " + response);
                }
            });
        });
        getBooksOnShelf();
    }

    private void getBooksOnShelf() {
        pbLoading.setVisibility(View.VISIBLE);
        // smooth scroll to top
        rvBooksOnShelf.smoothScrollToPosition(0);
        // Remove books from the adapter
        shelfDetailAdapter.clear();
        // get the books on the shelf
        FindCallback<BookOnShelf> getBooksOnShelfCallback = (objects, e) -> {
            if (e == null) {
                // Load model objects into the adapter
                booksOnShelf.addAll(objects);
                // Set view visibilities
                if (booksOnShelf.isEmpty()) {
                    tvNoBooks.setVisibility(View.VISIBLE);
                }
                Log.i(TAG, "fetch books on shelf success");
            } else {
                Log.e(TAG, "issue getting books on shelf", e);
            }
            pbLoading.setVisibility(View.GONE);
        };
        ParseQueryUtilities.getBooksOnShelfAsync(shelf, getBooksOnShelfCallback);
    }
}