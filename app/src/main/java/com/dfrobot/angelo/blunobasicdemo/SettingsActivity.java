package com.dfrobot.angelo.blunobasicdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class SettingsActivity extends AppCompatActivity {

    protected Button backBtn, sharedataButton, registerBtn, loginBtn;
    protected Switch themeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);

        if (darkMode) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();

        themeSwitch.setChecked(darkMode);
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();
            recreate();
        });

        setupUI();
    }

    private void initializeViews() {
        themeSwitch = findViewById(R.id.themeSwitch);
        backBtn = findViewById(R.id.backBtn);
    }

    private void setupUI() {
        sharedataButton = findViewById(R.id.sharedataButton);
        registerBtn = findViewById(R.id.registerBtn);
        loginBtn = findViewById(R.id.loginBtn);

        Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse);

        sharedataButton.setOnClickListener(v -> {
            v.startAnimation(pulseAnim);
            datashare_consent dialog = datashare_consent.newInstance();
            dialog.show(getSupportFragmentManager(), "ConsentDialog");
        });

        backBtn.setOnClickListener(v -> {
            v.startAnimation(pulseAnim);
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        registerBtn.setOnClickListener(v -> {
            v.startAnimation(pulseAnim);
            Intent intent = new Intent(SettingsActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        loginBtn.setOnClickListener(v -> {
            v.startAnimation(pulseAnim);
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
