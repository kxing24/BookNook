package com.codepath.kathyxing.booknook;

import android.util.Log;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.models.Book;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import okhttp3.Headers;

public class BookQueryManager {
    private static final String API_BASE_URL = "https://www.googleapis.com/";
    private static final String TAG = BookQueryManager.class.getSimpleName();
    private final AsyncHttpClient client;

    // The singleton instance of this class
    private static volatile BookQueryManager INSTANCE;

    public static BookQueryManager getInstance() {
        if (INSTANCE == null) {
            synchronized (BookQueryManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BookQueryManager();
                }
            }
        }
        return INSTANCE;
    }

    private BookQueryManager() {
        // Create one instance of AsyncHttpClient
        this.client = new AsyncHttpClient();
    }

    private String getApiUrl(String relativeUrl) {
        return API_BASE_URL + relativeUrl;
    }

    // Define appropriate error codes
    public static int NO_BOOKS_FOUND = 1000;

    // Define appropriate status codes with range,
    public static int BOOKS_SUCCESS = 0;

    public interface BooksCallback {
        void onSuccess(int statusCode, final ArrayList<Book> books, int totalItems);
        void onFailure(int errorCode);
    }

    // Method for accessing the search API
    public void getBooks(final String query, int startIndex, int maxResults, final BooksCallback booksCallback) {
        String url = getApiUrl("books/v1/volumes?q=" + query + "&startIndex=" + startIndex + "&maxResults=" + maxResults);
        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON response) {
                try {
                    if (response != null) {
                        if (0 == response.jsonObject.getInt("totalItems")) {
                            booksCallback.onFailure(NO_BOOKS_FOUND);
                        } else {
                            JSONArray items = response.jsonObject.getJSONArray("items");
                            int totalItems = response.jsonObject.getInt("totalItems");
                            booksCallback.onSuccess(BOOKS_SUCCESS, Book.fromJson(items), totalItems);
                        }
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

    // Gets the book from the API given the book id
    public void getBook(final String bookId, JsonHttpResponseHandler handler) {
        String url = getApiUrl("books/v1/volumes/" + bookId);
        client.get(url, handler);
    }
}
