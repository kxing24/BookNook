package com.codepath.kathyxing.booknook.fragments;

import android.content.Intent;
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
import android.widget.Toast;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.BookAdapter;
import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.activities.BookDetailActivity;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.net.BookClient;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookSearchFragment extends Fragment {

    // the fragment initialization parameters
    public static final String TAG = "BookSearchFragment";
    private RecyclerView rvBooks;
    private BookAdapter bookAdapter;
    private BookClient client;
    private ArrayList<Book> abooks;

    // Required empty public constructor
    public BookSearchFragment() {}

    @Override
    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_search, container, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize fields
        rvBooks = view.findViewById(R.id.rvBooks);
        abooks = new ArrayList<>();
        bookAdapter = new BookAdapter(getContext(), abooks);

        // set up a click handler for bookAdapter
        bookAdapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                // get the book clicked and launch the book detail activity

                Book book = abooks.get(position);

                Intent selectedItemIntent = new Intent(getActivity().getBaseContext(), BookDetailActivity.class);
                selectedItemIntent.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
                startActivity(selectedItemIntent);
            }
        });

        // Attach the adapter to the RecyclerView
        rvBooks.setAdapter(bookAdapter);

        // Set layout manager to position the items
        rvBooks.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    // Executes an API call to the Google Books search endpoint, parses the results
    // Converts them into an array of book objects and adds them to the adapter
    private void fetchBooks(String query) {
        client = new BookClient();
        client.getBooks(query, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Headers headers, JSON response) {
                try {
                    JSONArray items;
                    if (response != null) {
                        // Get the items json array
                        items = response.jsonObject.getJSONArray("items");
                        // Parse json array into array of model objects
                        final ArrayList<Book> books = Book.fromJson(items);
                        // Remove all books from the adapter
                        abooks.clear();
                        // Load model objects into the adapter
                        for (Book book : books) {
                            abooks.add(book); // add book through the adapter
                        }

                        bookAdapter.notifyDataSetChanged();

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_book_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // TODO: user can search for query in author, in title, etc. (and/or)

        // Expand the search view and request focus
        searchItem.expandActionView();
        searchView.requestFocus();

        // Set the textlistener for the searchview
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            // TODO: results show up as user types

            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                fetchBooks(query);
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }
}