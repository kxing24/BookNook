package com.codepath.kathyxing.booknook.models;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class BookTest {

    private final String TITLE = "UnitTestsForDummy";
    private final String ID = "123";
    private final String DESCRIPTION = "This book is for dummies";
    private Book mBook;
    @Before
    public void setUp() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", ID);
        JSONObject volumeInfo = new JSONObject();
        volumeInfo.put("title", TITLE);
        volumeInfo.put("description", DESCRIPTION);
        jsonObject.put("volumeInfo", volumeInfo);
        mBook = Book.fromJson(jsonObject);
    }

    @Test
    public void getTitle() {
        assertEquals(TITLE, mBook.getTitle());
    }


    @Test
    public void getId() {
        assertEquals(ID, mBook.getId());
    }

    @Test
    public void getDescription() {
        assertEquals(DESCRIPTION, mBook.getDescription());
    }

}