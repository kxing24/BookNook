package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class SearchResultsActivity extends AppCompatActivity {

    // activity parameters
    public static final String TAG = "SearchResultsActivity";
    public static final int MAX_RESULTS = 10;
    private RecyclerView rvBooks;
    private ProgressBar pbLoading;
    private Button btnPrevPage;
    private Button btnNextPage;
    private TextView tvPageNumber;
    private BookAdapter bookAdapter;
    private BookClient client;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // extract the extras from the intent
        anyField = getIntent().getExtras().getString("anyField");
        title = getIntent().getExtras().getString("title");
        author = getIntent().getExtras().getString("author");
        publisher = getIntent().getExtras().getString("publisher");
        subject = getIntent().getExtras().getString("subject");
        isbn = getIntent().getExtras().getString("isbn");

        // set the query based on the extras
        setQuery();
        Log.i(TAG, "query is: " + savedQuery);

        // initialize fields
        rvBooks = findViewById(R.id.rvBooks);
        pbLoading = findViewById(R.id.pbLoading);
        tvPageNumber = findViewById(R.id.tvPageNumber);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);
        books = new ArrayList<>();
        bookAdapter = new BookAdapter(this, books);

        // Attach the adapter to the RecyclerView
        rvBooks.setAdapter(bookAdapter);

        // Set layout manager to position the items
        rvBooks.setLayoutManager(new LinearLayoutManager(this));

        // set up a click handler for bookAdapter
        bookAdapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                // get the book clicked
                Book book = books.get(position);
                // launch the book detail activity
                Intent selectedItemIntent = new Intent(SearchResultsActivity.this, BookDetailActivity.class);
                selectedItemIntent.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
                startActivity(selectedItemIntent);
            }
        });

        // set up a click handler for the prev page button
        btnPrevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber--;
                // Get the new books
                fetchBooks(pageNumber);
            }
        });

        // set up a click handler for the next page button
        btnNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber++;
                // Get the new books
                fetchBooks(pageNumber);
            }
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
                        pbLoading.setVisibility(View.GONE);
                        btnNextPage.setVisibility(View.VISIBLE);
                        if(pageNumber > 0) {
                            btnPrevPage.setVisibility(View.VISIBLE);
                        }
                        tvPageNumber.setText("Page " + (pageNumber + 1));
                        tvPageNumber.setVisibility(View.VISIBLE);

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
                Log.e(TAG, "Request failed with code " + statusCode + ". Response message: " + responseString);
            }
        });
    }

    private void setQuery() {
        if(!anyField.equals("")) {
            if(!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += anyField;
        }
        if(!title.equals("")) {
            if(!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += ("intitle:" + title);
        }
        if(!author.equals("")) {
            if(!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += ("inauthor:" + author);
        }
        if(!publisher.equals("")) {
            if(!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += ("inpublisher:" + publisher);
        }
        if(!subject.equals("")) {
            if(!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += ("subject:" + subject);
        }
        if(!isbn.equals("")) {
            if(!firstParameter) {
                savedQuery += "+";
            }
            firstParameter = false;
            savedQuery += ("isbn:" + isbn);
        }
    }
}