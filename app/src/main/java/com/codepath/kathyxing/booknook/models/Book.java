package com.codepath.kathyxing.booknook.models;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class Book {
    private String id;
    private String author;
    private String title;
    private String subtitle;
    private String description;
    private String thumbnailUrl;
    private String coverUrl;

    // empty constructor for parcel
    public Book() {}

    public String getId() {
        return id;
    }

    public String getTitle() {
        if (title.equals("")) {
            return "no title available";
        }
        if (subtitle.equals("")) {
            return title;
        }
        return title + ": " + subtitle;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubtitle() { return subtitle; }

    public String getDescription() { return description; }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    // Returns a Book given the expected JSON
    public static Book fromJson(JSONObject jsonObject) {
        Book book = new Book();
        try {
            // Deserialize json into object fields
            book.id = jsonObject.getString("id");
            if(jsonObject.getJSONObject("volumeInfo").has("title")) {
                book.title = jsonObject.getJSONObject("volumeInfo").getString("title");
            }
            else {
                book.title = "";
            }
            if(jsonObject.getJSONObject("volumeInfo").has("subtitle")) {
                book.subtitle = jsonObject.getJSONObject("volumeInfo").getString("subtitle");
            }
            else {
                book.subtitle = "";
            }
            book.author = getAuthor(jsonObject);
            if(jsonObject.getJSONObject("volumeInfo").has("description")) {
                book.description = jsonObject.getJSONObject("volumeInfo").getString("description");
            }
            else {
                book.description = "no description available";
            }
            if(jsonObject.getJSONObject("volumeInfo").has("imageLinks")) {
                book.thumbnailUrl = jsonObject.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("thumbnail");
                book.coverUrl = jsonObject.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("thumbnail");
            }
            else {
                book.thumbnailUrl = null;
                book.coverUrl = null;
            }
            //TODO: set the coverUrl to something higher-quality if it exists
            //book.coverUrl = jsonObject.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("medium");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // Return new object
        return book;
    }

    // Return comma separated author list when there is more than one author
    private static String getAuthor(final JSONObject jsonObject) {
        try {
            final JSONArray authors = jsonObject.getJSONObject("volumeInfo").getJSONArray("authors");
            int numAuthors = authors.length();
            final String[] authorStrings = new String[numAuthors];
            for (int i = 0; i < numAuthors; ++i) {
                authorStrings[i] = authors.getString(i);
            }
            return TextUtils.join(", ", authorStrings);
        } catch (JSONException e) {
            return "";
        }
    }

    // Decodes array of book json results into business model objects
    public static ArrayList<Book> fromJson(JSONArray jsonArray) {
        ArrayList<Book> books = new ArrayList<>(jsonArray.length());
        // Process each result in json array, decode and convert to business
        // object
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject bookJson;
            try {
                bookJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            Book book = Book.fromJson(bookJson);
            if (book != null) {
                books.add(book);
            }
        }
        return books;
    }

}
