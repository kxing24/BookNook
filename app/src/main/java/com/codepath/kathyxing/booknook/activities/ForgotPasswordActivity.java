package com.codepath.kathyxing.booknook.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.kathyxing.booknook.ParseQueryUtilities;
import com.codepath.kathyxing.booknook.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ForgotPasswordActivity extends AppCompatActivity {

    // activity parameters
    EditText etEmail;
    Button btnResetPassword;
    Button btnBackToLogin;

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
        // click handler for reset password button
        btnResetPassword.setOnClickListener(v -> {
            String userEmail = etEmail.getText().toString().trim();
            if (userEmail.equals("")) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "Please enter an email", Toast.LENGTH_SHORT).show();
            } else {
                RequestPasswordResetCallback requestPasswordResetCallback = e -> {
                    if (e == null) {
                        showAlert("Password reset", "Password reset instructions have been sent to email!", false);
                    }
                    else {
                        etEmail.getText().clear();
                        showAlert("Password reset failed", "Password could not be reset:" + e.getMessage(), true);
                    }
                };
                ParseQueryUtilities.resetPasswordAsync(userEmail, requestPasswordResetCallback);
            }
        });
        // click handler for back to login button
        btnBackToLogin.setOnClickListener(v -> goLoginActivity());
    }

    private void showAlert(String title, String message, boolean error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.cancel();
                    if (!error) {
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }

    private void goLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}