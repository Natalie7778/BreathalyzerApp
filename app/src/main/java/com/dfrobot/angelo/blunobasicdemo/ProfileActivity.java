package com.dfrobot.angelo.blunobasicdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView textUserId;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        //set dark or light theme
        if (darkMode) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textUserId = findViewById(R.id.textUserId);
        logoutBtn = findViewById(R.id.logoutBtn);

        String username = prefs.getString("logged_in_user", null);
        //check if user is currently logged in
        if (username == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);

        new Thread(() -> {
            User user = db.userDao().getUserByUsername(username);
            runOnUiThread(() -> {
                if (user != null) {
                    textUserId.setText("Username: " + user.getUsername());
                } else {
                    Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
        LinearLayout historyLayout = findViewById(R.id.historyLayout);

        new Thread(() -> {
            List<TestResult> history = db.testResultDao().getAllResultsForUser(username);
            runOnUiThread(() -> {
                for (TestResult result : history) {
                    TextView recordView = new TextView(ProfileActivity.this);
                    recordView.setText(
                            "BAC: " + result.bacValue +
                                    "\n" + result.locationInfo +
                                    "\nTime: " + result.timestamp +
                                    "\n-----------------------------"
                    );
                    recordView.setTextColor(getColor(R.color.cartoon_purple));
                    recordView.setTextSize(16);
                    recordView.setPadding(12, 12, 12, 12);
                    recordView.setTypeface(getResources().getFont(R.font.baloo));
                    historyLayout.addView(recordView);
                }
            });
        }).start();

        //log out button
        logoutBtn.setOnClickListener(v -> {
            prefs.edit().remove("logged_in_user").apply();
            Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
