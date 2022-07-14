package com.codepath.kathyxing.booknook.fragments;

import android.util.Log;

import com.codepath.kathyxing.booknook.models.Book;

import junit.framework.TestCase;

import org.apache.commons.text.similarity.CosineDistance;
import org.json.JSONObject;

import java.util.ArrayList;

public class BookDetailFragmentTest extends TestCase {

    private final String TITLE_TO = "Title 1";
    private final String ID_TO = "123";
    private final String DESCRIPTION_TO = "This book is for dummies";
    private final String MATURITY_RATING_TO = "NOT_MATURE";
    private final int PAGE_COUNT_TO = 10;
    private final String LANGUAGE_TO = "en";
    private final String TITLE_FROM = "Title 2";
    private final String ID_FROM = "321";
    private final String DESCRIPTION_FROM = "This book is for dummies";
    private final String MATURITY_RATING_FROM = "NOT_MATURE";
    private final int PAGE_COUNT_FROM = 20;
    private final String LANGUAGE_FROM = "en";
    private Book toBook;
    private Book fromBook;
    private BookDetailFragment testFragment;

    public void setUp() throws Exception {
        super.setUp();
        JSONObject jsonObjectTo = new JSONObject();
        jsonObjectTo.put("id", ID_TO);
        JSONObject volumeInfoTo = new JSONObject();
        volumeInfoTo.put("title", TITLE_TO);
        volumeInfoTo.put("description", DESCRIPTION_TO);
        volumeInfoTo.put("maturityRating", MATURITY_RATING_TO);
        volumeInfoTo.put("pageCount", PAGE_COUNT_TO);
        volumeInfoTo.put("language", LANGUAGE_TO);
        jsonObjectTo.put("volumeInfo", volumeInfoTo);
        toBook = Book.fromJson(jsonObjectTo);
        JSONObject jsonObjectFrom = new JSONObject();
        jsonObjectFrom.put("id", ID_FROM);
        JSONObject volumeInfoFrom = new JSONObject();
        volumeInfoFrom.put("title", TITLE_FROM);
        volumeInfoFrom.put("description", DESCRIPTION_FROM);
        volumeInfoFrom.put("maturityRating", MATURITY_RATING_FROM);
        volumeInfoFrom.put("pageCount", PAGE_COUNT_FROM);
        volumeInfoFrom.put("language", LANGUAGE_FROM);
        jsonObjectFrom.put("volumeInfo", volumeInfoFrom);
        fromBook = Book.fromJson(jsonObjectFrom);
        testFragment = new BookDetailFragment();
    }

    public void testGetRecommendedBook() {
        ArrayList<Book> testList = new ArrayList<>();
        testList.add(fromBook);
        assertEquals(fromBook, testFragment.getRecommendedBook(toBook, testList));
    }

    public void testGetBookDistance() {
        assertEquals((float) (1 + 0 + 0 + 2 + 1 + 0), (float) testFragment.getBookDistance(fromBook, toBook));
    }
}