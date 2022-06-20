package com.codepath.kathyxing.booknook.net;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.kathyxing.booknook.R;

public class BookClient {
    private static final String API_BASE_URL = "https://www.googleapis.com/";
    private AsyncHttpClient client;

    public BookClient() {
        this.client = new AsyncHttpClient();
    }

    private String getApiUrl(String relativeUrl) {
        return API_BASE_URL + relativeUrl;
    }

    // Method for accessing the search API
    public void getBooks(final String query, JsonHttpResponseHandler handler) {
        String url = getApiUrl("books/v1/volumes?q=" + query);
        client.get(url, handler);
    }
}
