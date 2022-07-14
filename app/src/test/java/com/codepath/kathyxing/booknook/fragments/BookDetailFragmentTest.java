package com.codepath.kathyxing.booknook.fragments;

import com.codepath.kathyxing.booknook.models.Book;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.ArrayList;

public class BookDetailFragmentTest extends TestCase {

    private final String TITLE_TO = "Title 1";
    private final String ID_TO = "1";
    private final String DESCRIPTION_TO = "This book is for dummies";
    private final String MATURITY_RATING_TO = "NOT_MATURE";
    private final int PAGE_COUNT_TO = 10;
    private final String LANGUAGE_TO = "en";
    private final String TITLE_FROM_1 = "Title 2";
    private final String ID_FROM_1 = "2";
    private final String DESCRIPTION_FROM_1 = "This book is for dummies";
    private final String MATURITY_RATING_FROM_1 = "NOT_MATURE";
    private final int PAGE_COUNT_FROM_1 = 20;
    private final String LANGUAGE_FROM_1 = "en";
    private final String ID_FROM_2 = "3";
    private final int PAGE_COUNT_FROM_2 = 10;
    private Book toBook;
    private Book fromBook1;
    private Book fromBook2;
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
        JSONObject jsonObjectFrom1 = new JSONObject();
        jsonObjectFrom1.put("id", ID_FROM_1);
        JSONObject volumeInfoFrom1 = new JSONObject();
        volumeInfoFrom1.put("title", TITLE_FROM_1);
        volumeInfoFrom1.put("description", DESCRIPTION_FROM_1);
        volumeInfoFrom1.put("maturityRating", MATURITY_RATING_FROM_1);
        volumeInfoFrom1.put("pageCount", PAGE_COUNT_FROM_1);
        volumeInfoFrom1.put("language", LANGUAGE_FROM_1);
        jsonObjectFrom1.put("volumeInfo", volumeInfoFrom1);
        fromBook1 = Book.fromJson(jsonObjectFrom1);
        JSONObject jsonObjectFrom2 = new JSONObject();
        jsonObjectFrom2.put("id", ID_FROM_2);
        JSONObject volumeInfoFrom2 = new JSONObject();
        volumeInfoFrom2.put("pageCount", PAGE_COUNT_FROM_2);
        jsonObjectFrom2.put("volumeInfo", volumeInfoFrom2);
        fromBook2 = Book.fromJson(jsonObjectFrom2);
        testFragment = new BookDetailFragment();
    }

    public void testGetRecommendedBook() {
        ArrayList<Book> testList = new ArrayList<>();
        testList.add(fromBook1);
        testList.add(fromBook2);
        assertEquals(fromBook1, testFragment.getRecommendedBook(toBook, testList));
    }

    public void testGetBookDistance() {
        assertEquals((float) (1 + 0 + 0 + 2 + 1 + 0), (float) testFragment.getBookDistance(fromBook1, toBook));
    }
}