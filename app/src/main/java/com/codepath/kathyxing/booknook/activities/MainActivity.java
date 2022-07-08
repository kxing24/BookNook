package com.codepath.kathyxing.booknook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.fragments.BookSearchFragment;
import com.codepath.kathyxing.booknook.fragments.FriendsFragment;
import com.codepath.kathyxing.booknook.fragments.HomeFragment;
import com.codepath.kathyxing.booknook.fragments.MyGroupsFragment;
import com.codepath.kathyxing.booknook.fragments.ShelvesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // handle navigation selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.action_home:
                    fragment = new HomeFragment();
                    break;
                case R.id.action_book_search:
                    fragment = new BookSearchFragment();
                    break;
                case R.id.action_shelves:
                    fragment = new ShelvesFragment();
                    break;
                case R.id.action_friends:
                    fragment = new FriendsFragment();
                    break;
                case R.id.action_my_groups:
                    fragment = new MyGroupsFragment();
                    break;
                default:
                    return true;
            }
            fragmentManager.beginTransaction().replace(R.id.rlContainer, fragment).commit();
            return true;
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_profile:
                Intent intent = new Intent(this, UserProfileActivity.class);
                intent.putExtra("user", ParseUser.getCurrentUser());
                startActivity(intent);
                return true;
            case R.id.action_logout:
                logoutUser();
                return true;
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                hideHomeButton();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showBackButton() { getSupportActionBar().setDisplayHomeAsUpEnabled(true); }
    public void hideHomeButton() { getSupportActionBar().setDisplayHomeAsUpEnabled(false); }
}