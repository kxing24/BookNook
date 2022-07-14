package com.codepath.kathyxing.booknook.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import com.codepath.kathyxing.booknook.parse_classes.Shelf;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final String[] genres = {"Juvenile Fiction", "Young Adult Fiction", "Fantasy", "Science Fiction", "Romance", "Mystery", "Thriller", "Art", "History", "Political Science", "Biography"};
    public static final int NUM_SELECTED_GENRES = 3;
    private boolean firstLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // get whether or not this is the user's first login
        firstLogin = getIntent().getExtras().getBoolean("firstLogin");
        Log.i(TAG, "first login is " + firstLogin);
        // if it's the user's first login, have them select their top three book genres
        if (firstLogin) {
            showGenresAlertDialog();
        }
        // set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // handle navigation selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            hideBackButton();
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
                hideBackButton();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showGenresAlertDialog() {
        ArrayList<String> selectedGenres = new ArrayList<>();
        boolean[] selected = new boolean[genres.length];
        // Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set title
        builder.setTitle("Select your top " + NUM_SELECTED_GENRES + " genres");
        // set dialog non cancelable
        builder.setCancelable(false);
        builder.setMultiChoiceItems(genres, selected, new DialogInterface.OnMultiChoiceClickListener() {
            int count = 0;
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                // kep track of the number of items selected
                count += isChecked ? 1 : -1;
                // make sure the count does not exceed the number of genres to select
                if (count > NUM_SELECTED_GENRES) {
                    Toast.makeText(MainActivity.this, "You can only select " + NUM_SELECTED_GENRES + " genres", Toast.LENGTH_SHORT).show();
                    selected[position] = false;
                    count--;
                    ((AlertDialog) dialogInterface).getListView().setItemChecked(position, false);
                } else {
                    if (isChecked) {
                        // when checkbox selected, add position to selectedShelvesPositions
                        selectedGenres.add(genres[position]);
                    } else {
                        // when checkbox unselected, remove position from selectedShelvesPositions
                        selectedGenres.remove(genres[position]);
                    }
                }
                // if the user has selected the correct number of genres, enable the button
                if (count == NUM_SELECTED_GENRES) {
                    ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
        builder.setPositiveButton("Done", (dialogInterface, i) -> {
            // Add the genres to the user
            saveGenres(selectedGenres);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        // Initially disable the done button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void saveGenres(ArrayList<String> genres) {
        User currentUser = (User) ParseUser.getCurrentUser();
        currentUser.setFavoriteGenres(new JSONArray(genres));
        currentUser.saveInBackground();
    }

    public void showBackButton() { getSupportActionBar().setDisplayHomeAsUpEnabled(true); }
    public void hideBackButton() { getSupportActionBar().setDisplayHomeAsUpEnabled(false); }
}