package com.dfrobot.angelo.blunobasicdemo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.Normalizer;
import java.util.regex.Pattern;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class detailsPage extends AppCompatActivity {

    private TextView textBAC, textTimeLocation, textDriveAdvice;
    private Button buttonHome;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private LocationCallback locationCallback;
    protected double limitBreak = 0.1;


    private double bac = 0.0;
    private String currentProvince = "Unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        //set dark or light mode
        if (darkMode) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        textBAC = findViewById(R.id.textBAC);
        textTimeLocation = findViewById(R.id.text_timeLocation);
        textDriveAdvice = findViewById(R.id.text_driveAdvice);
        buttonHome = findViewById(R.id.button_home);
        LinearLayout detailsBCKGRND = findViewById(R.id.detailsLayout);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Animation pulseTwo = AnimationUtils.loadAnimation(this, R.anim.pulsetwo);
        buttonHome.startAnimation(pulseTwo);

        //display BAC value
        String bacResult = getIntent().getStringExtra("BAC_VALUE");
        if (bacResult != null && !bacResult.trim().isEmpty()) {
            try {
                bac = Double.parseDouble(bacResult.trim());
                textBAC.setText(String.format(Locale.getDefault(), "BAC: %.4f %%", bac));
            } catch (NumberFormatException e) {
                textBAC.setText("BAC: N/A");
            }
        }

        //Set initial location text & fetch ===
        textTimeLocation.setText("Time: " + getCurrentTime() + "\nLocation: Fetching...");
        getLocation();

        //Save result if user logged in
        String username = prefs.getString("logged_in_user", null);
        if (username != null) {
            new Thread(() -> {
                TestResult result = new TestResult();
                result.username = username;
                result.bacValue = String.format(Locale.getDefault(), "%.4f", bac);
                result.estimateTime = "N/A";
                result.locationInfo = textTimeLocation.getText().toString();
                result.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                AppDatabase.getInstance(detailsPage.this).testResultDao().insert(result);
            }).start();
        }

        // Button listener
        buttonHome.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
            startActivity(new Intent(detailsPage.this, MainActivity.class));
        });
    }
    private String removeAccents(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(1000)
                .setNumUpdates(1);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    updateLocationDetails(location);
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }


    private void updateLocationDetails(Location location) {
        //update location
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality() != null ? address.getLocality() : "Unknown City";
                currentProvince = address.getAdminArea() != null ? address.getAdminArea() : "Unknown Province";
                String country = address.getCountryName() != null ? address.getCountryName() : "Unknown Country";

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                String rawProvince = address.getAdminArea();
                currentProvince = rawProvince != null ? removeAccents(rawProvince).trim() : "Unknown Province";
                double legalLimit = getLegalLimit(currentProvince);
                limitBreak = legalLimit;


                String locationText = "Time: " + getCurrentTime() +
                        "\nLocation: " + city + ", " + currentProvince + ", " + country +
                        "\nCoordinates: " + String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude) +
                        "\nLegal BAC Limit: " + legalLimit + "%";

                textTimeLocation.setText(locationText);

                if (bac >= legalLimit) {
                    textDriveAdvice.setText("Illegal toz Drive - Over Legal Limit (" + legalLimit + "%)");
                    textDriveAdvice.setTextColor(ContextCompat.getColor(this, R.color.goHome));
                } else {
                    textDriveAdvice.setText("Legal to Drive - Under Legal Limit (" + legalLimit + "%)");
                    textDriveAdvice.setTextColor(ContextCompat.getColor(this, R.color.good));
                }

            } else {
                textTimeLocation.setText("Time: " + getCurrentTime() + "\nLocation: Unknown");
                textDriveAdvice.setText(" Unable to determine location.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            textTimeLocation.setText("Time: " + getCurrentTime() + "\nLocation: Unknown");
        }
        // Define colors to be used based on BAC
        LinearLayout detailsBCKGRND = findViewById(R.id.detailsLayout);
        int good = ContextCompat.getColor(this, R.color.good);
        int uhOh = ContextCompat.getColor(this, R.color.uhOh);
        int goHome = ContextCompat.getColor(this, R.color.goHome);
        int goodBack = ContextCompat.getColor(this, R.color.goodBACK);
        int uhOhBack = ContextCompat.getColor(this, R.color.uhOhBACK);
        int goHomeBack = ContextCompat.getColor(this, R.color.goHomeBACK);
        //Use green colors if under half the limit
        //Use yellow colors when between half the limit and the actual limit
        //Use red theme when over the limit
        if (bac > limitBreak) {
            textBAC.setTextColor(goHome);
            detailsBCKGRND.setBackgroundColor(goHomeBack);
        } else if (bac > (limitBreak/2)) {
            textBAC.setTextColor(uhOh);
            detailsBCKGRND.setBackgroundColor(uhOhBack);
        } else {
            textBAC.setTextColor(good);
            detailsBCKGRND.setBackgroundColor(goodBack);
            //Log.d("joe", "uhoh "+(limitBreak/2));
        }
    }
    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    //Change the limit value depending on current province
    private double getLegalLimit(String province) {
        switch (province) {
            case "Nunavut":
            case "Prince Edward Island":
            case "Nova Scotia":
            case "New Brunswick":
            case "Newfoundland and Labrador":
            case "Manitoba":
            case "Ontario":
            case "British Columbia":
            case "Alberta":
                return 0.05;
            case "Saskatchewan":
                return 0.04;
            case "Yukon":
            case "Quebec":
                return 0.08;
            default:
                return 0.00;
       }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            textTimeLocation.setText("Time: " + getCurrentTime() + "\nLocation: Permission Denied");
        }
    }
}
