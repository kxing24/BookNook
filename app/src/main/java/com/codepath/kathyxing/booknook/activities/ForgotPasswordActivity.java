package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.codepath.kathyxing.booknook.AlertUtilities;
import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ForgotPasswordActivity extends BaseActivity {

    // activity parameters
    private TextInputEditText etEmail;
    private Button btnResetPassword;
    private Button btnBackToLogin;
    private RelativeLayout rlForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        // set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        // have the toolbar show a back button
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        // initialize views
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        rlForgotPassword = findViewById(R.id.rlForgotPassword);
        // click handler for reset password button
        btnResetPassword.setOnClickListener(v -> {
            String userEmail = etEmail.getText().toString().trim();
            if (userEmail.equals("")) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "Please enter an email", Toast.LENGTH_SHORT).show();
            } else {
                RequestPasswordResetCallback requestPasswordResetCallback = e -> {
                    AlertUtilities.GoActivity goActivity = () -> {
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    };
                    if (e == null) {
                        AlertUtilities.showAlert(ForgotPasswordActivity.this, "Password reset", "Password reset instructions have been sent to email!", false, goActivity);
                    }
                    else {
                        etEmail.getText().clear();
                        AlertUtilities.showAlert(ForgotPasswordActivity.this, "Password reset failed", "Password could not be reset:" + e.getMessage(), true, goActivity);
                    }
                };
                ParseQueryUtilities.resetPasswordAsync(userEmail, requestPasswordResetCallback);
            }
        });
        // click handler for back to login button
        btnBackToLogin.setOnClickListener(v -> goLoginActivity());
        // hide keyboard when the relative layout is touched
        setupUI(rlForgotPassword);
    }

    private void goLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}