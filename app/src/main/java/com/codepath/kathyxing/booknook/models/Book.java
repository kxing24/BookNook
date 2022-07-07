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
    private String subject;
    private String maturityRating;
    private String printType;
    private String language;
    private int pageCount;
    private ArrayList<String> authorArray;

    // empty constructor for parcel
    public Book() {
    }

    // Returns a Book given the expected JSON
    public static Book fromJson(JSONObject jsonObject) {
        Book book = new Book();
        try {
            // Deserialize json into object fields
            book.id = jsonObject.getString("id");
            if (jsonObject.getJSONObject("volumeInfo").has("title")) {
                book.title = jsonObject.getJSONObject("volumeInfo").getString("title");
            } else {
                book.title = "";
            }
            if (jsonObject.getJSONObject("volumeInfo").has("subtitle")) {
                book.subtitle = jsonObject.getJSONObject("volumeInfo").getString("subtitle");
            } else {
                book.subtitle = "";
            }
            book.author = getAuthor(jsonObject);
            book.authorArray = getAuthorArray(jsonObject);
            if (jsonObject.getJSONObject("volumeInfo").has("description")) {
                book.description = jsonObject.getJSONObject("volumeInfo").getString("description");
            } else {
                book.description = "no description available";
            }
            if (jsonObject.getJSONObject("volumeInfo").has("imageLinks")) {
                book.thumbnailUrl = jsonObject.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("thumbnail");
                book.coverUrl = jsonObject.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("thumbnail");
            } else {
                book.thumbnailUrl = null;
                book.coverUrl = null;
            }
            //TODO: set the coverUrl to something higher-quality if it exists
            //book.coverUrl = jsonObject.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("medium");
            if (jsonObject.getJSONObject("volumeInfo").has("categories")) {
                book.subject = jsonObject.getJSONObject("volumeInfo").getJSONArray("categories").getString(0);
            } else {
                book.subject = "";
            }
            if (jsonObject.getJSONObject("volumeInfo").has("maturityRating")) {
                book.maturityRating = jsonObject.getJSONObject("volumeInfo").getString("maturityRating");
            } else {
                book.maturityRating = "";
            }
            if (jsonObject.getJSONObject("volumeInfo").has("printType")) {
                book.printType = jsonObject.getJSONObject("volumeInfo").getString("printType");
            } else {
                book.printType = "";
            }
            if (jsonObject.getJSONObject("volumeInfo").has("language")) {
                book.language = jsonObject.getJSONObject("volumeInfo").getString("language");
            } else {
                book.language = "";
            }
            if (jsonObject.getJSONObject("volumeInfo").has("pageCount")) {
                book.pageCount = jsonObject.getJSONObject("volumeInfo").getInt("pageCount");
            } else {
                book.pageCount = -1;
            }
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

    // Return list of authors
    private static ArrayList<String> getAuthorArray(final JSONObject jsonObject) {
        try {
            final JSONArray authors = jsonObject.getJSONObject("volumeInfo").getJSONArray("authors");
            int numAuthors = authors.length();
            ArrayList<String> authorArray = new ArrayList<>();
            for (int i = 0; i < numAuthors; i++) {
                authorArray.add(authors.getString(i));
            }
            return authorArray;
        } catch (JSONException e) {
            return null;
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

    public String getId() {
        return id;
    }

    public String getTitle() {
        if (title.equals("")) {
            return "no title available";
        } else if (subtitle.equals("")) {
            return title;
        }
        return title + ": " + subtitle;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getSubject() {
        return subject;
    }

    public String getMaturityRating() {
        return maturityRating;
    }

    public String getPrintType() {
        return printType;
    }

    public String getLanguage() {
        return language;
    }

    public int getPageCount() {
        return pageCount;
    }

    public ArrayList<String> getAuthorArray() {
        return authorArray;
    }

}
