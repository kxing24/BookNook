package com.codepath.kathyxing.booknook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.parse_classes.User;
import com.parse.ParseUser;

import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnGoSignup;
    private Button btnForgotPassword;
    private RelativeLayout rlLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // If user is already logged in, don't show login screen
        if (ParseUser.getCurrentUser() != null) {
            goMainActivity(false);
        }
        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoSignup = findViewById(R.id.btnGoSignup);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        rlLogin = findViewById(R.id.rlLogin);
        // click handler for go sign up button
        btnGoSignup.setOnClickListener(v -> {
            Log.i(TAG, "onClick go signup button");
            goSignupActivity();
        });
        // click handler for login button
        btnLogin.setOnClickListener(v -> {
            Log.i(TAG, "onClick login button");
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();
            loginUser(username, password);
        });
        // click handler for forgot password button
        btnForgotPassword.setOnClickListener(v -> goForgotPasswordActivity());
        // hide keyboard when the relative layout is touched
        if (LoginActivity.this.getCurrentFocus() != null) {
            rlLogin.setOnTouchListener((v, event) -> {
                InputMethodManager imm = (InputMethodManager) LoginActivity.this.getSystemService(UserProfileActivity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(LoginActivity.this.getCurrentFocus().getWindowToken(), 0);
                return true;
            });
        }
    }

    private void loginUser(String username, String password) {
        Log.i(TAG, "Attempting to login user " + username);
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (e != null) {
                Log.e(TAG, "Issues with login", e);
                showAlert("Login Fail", e.getMessage() + " Please try again.", true);
            } else {
                goMainActivity(((User) user).getLastLogin() == null);
                ((User) user).setLastLogin(Calendar.getInstance().getTime());
                user.saveInBackground();
                Log.i(TAG, "Successfully logged in");
            }
        });
    }

    private void showAlert(String title, String message, boolean error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.cancel();
                    if (!error) {
                        Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }

    private void goMainActivity(boolean firstLogin) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("firstLogin", firstLogin);
        startActivity(i);
        finish();
    }

    private void goSignupActivity() {
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
        finish();
    }

    private void goForgotPasswordActivity() {
        Intent i = new Intent(this, ForgotPasswordActivity.class);
        startActivity(i);
    }
}