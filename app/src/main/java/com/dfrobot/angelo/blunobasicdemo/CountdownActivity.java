package com.dfrobot.angelo.blunobasicdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CountdownActivity extends AppCompatActivity {
    private TextView countdownText, instructionText;
    private int secondsLeft = 5;
    private Handler handler = new Handler();
    private Typeface balooFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        //set dark or light themes
        if (darkMode) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        balooFont = Typeface.createFromAsset(getAssets(), "fonts/baloo.ttf");

        countdownText = findViewById(R.id.countdownText);
        instructionText = findViewById(R.id.instructionText);
        countdownText.setTypeface(balooFont);
        instructionText.setTypeface(balooFont);

        startCountdown();
    }

    private void startCountdown() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                countdownText.setText(String.valueOf(secondsLeft));
                if (secondsLeft > 0) {
                    secondsLeft--;
                    handler.postDelayed(this, 1000);
                } else {
                    //move values along page to page
                    String bacValue = getIntent().getStringExtra("BAC_VALUE");
                    String estimate = getIntent().getStringExtra("estimate_time");

                    Intent intent = new Intent(CountdownActivity.this, detailsPage.class);
                    intent.putExtra("BAC_VALUE", bacValue);
                    intent.putExtra("estimate_time", estimate);
                    startActivity(intent);
                    finish();
                }
            }
        }, 0);
    }
}
