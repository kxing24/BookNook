package com.codepath.kathyxing.booknook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.activities.BookDetailActivity;
import com.codepath.kathyxing.booknook.activities.LoginActivity;
import com.codepath.kathyxing.booknook.models.Book;
import com.codepath.kathyxing.booknook.net.BookClient;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;

import okhttp3.Headers;

public class BookSearchActivity extends AppCompatActivity {

    public static final String TAG = "BookSearchActivity";

    private RecyclerView rvBooks;
    private BookAdapter bookAdapter;
    private BookClient client;
    private ArrayList<Book> abooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        // set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // initialize fields
        rvBooks = findViewById(R.id.rvBooks);
        abooks = new ArrayList<>();
        bookAdapter = new BookAdapter(this, abooks);

        // set up a click handler for bookAdapter
        bookAdapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                // get the book clicked and launch the book detail activity

                Book book = abooks.get(position);

                Intent selectedItemIntent = new Intent(getBaseContext(), BookDetailActivity.class);
                selectedItemIntent.putExtra(Book.class.getSimpleName(), Parcels.wrap(book));
                startActivity(selectedItemIntent);
            }
        });

        // Attach the adapter to the RecyclerView
        rvBooks.setAdapter(bookAdapter);

        // Set layout manager to position the items
        rvBooks.setLayoutManager(new LinearLayoutManager(this));
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

    private void logoutUser() {
        Log.i(TAG, "Logging out");
        ParseUser.logOutInBackground();
        goLoginActivity();
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Expand the search view and request focus
        searchItem.expandActionView();
        searchView.requestFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

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

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                logoutUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}