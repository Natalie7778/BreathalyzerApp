package com.dfrobot.angelo.blunobasicdemo;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Set;

public class MainActivity extends BlunoLibrary {
	private Button buttonScan;
	private Button startSensorButton;
	//display for testing
	//private EditText serialSendText;
	private TextView serialReceivedText;
	private String lastReceivedBAC = "0.00";

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
		setContentView(R.layout.activity_main);

		Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse);
		Typeface cuteFont = Typeface.createFromAsset(getAssets(), "fonts/baloo.ttf");

		TextView titleText = findViewById(R.id.titleText);
		TextView tagline = findViewById(R.id.tagline);
		Animation carAnim = AnimationUtils.loadAnimation(this, R.anim.car_translate);
		tagline.startAnimation(carAnim);
		TextView countdown = findViewById(R.id.countdownText);
		serialReceivedText = findViewById(R.id.serialReveicedText);

		Button settingsButton = findViewById(R.id.settingsButton);
		Button profileButton = findViewById(R.id.profileButton);
		startSensorButton = findViewById(R.id.startSensorButton);
		buttonScan = findViewById(R.id.buttonScan);
		ImageButton helpButton = findViewById(R.id.helpButton);


		// APPLY the Font here
		titleText.setTypeface(cuteFont);
		tagline.setTypeface(cuteFont);
		countdown.setTypeface(cuteFont);
		serialReceivedText.setTypeface(cuteFont);
		settingsButton.setTypeface(cuteFont);
		profileButton.setTypeface(cuteFont);
		startSensorButton.setTypeface(cuteFont);
		buttonScan.setTypeface(cuteFont);


		settingsButton.setOnClickListener(v -> {
			v.startAnimation(pulseAnim);
			startActivity(new Intent(MainActivity.this, SettingsActivity.class));
		});

		profileButton.setOnClickListener(v -> {
			v.startAnimation(pulseAnim);
			startActivity(new Intent(MainActivity.this, ProfileActivity.class));
		});

		helpButton.setOnClickListener(v -> showHelpDialog());

		// Permissions
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
					checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{
						Manifest.permission.BLUETOOTH_SCAN,
						Manifest.permission.BLUETOOTH_CONNECT
				}, 1001);
			}
		}

		request(1000, new OnPermissionsResult() {
			@Override
			public void OnSuccess() {
				Toast.makeText(MainActivity.this, "SUCCESS", Toast.LENGTH_SHORT).show();
			}
			@Override
			public void OnFail(List<String> noPermissions) {
				Toast.makeText(MainActivity.this, "FAIL", Toast.LENGTH_SHORT).show();
			}
		});

		onCreateProcess();
		serialBegin(115200);

		buttonScan.setOnClickListener(v -> {
			v.startAnimation(pulseAnim);
			try {
				buttonScanOnClickProcess();
			} catch (Exception e) {
				Log.e("ScanError", "Scan button crashed", e);
				Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
		});

		startSensor();
	}

	//Help button pop-up containing instructions and credits
	private void showHelpDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = LayoutInflater.from(this).inflate(R.layout.help_dialog, null);
		builder.setView(view);

		TextView helpText = view.findViewById(R.id.helpText);
		TextView helpTitle = view.findViewById(R.id.helpTitle);
		Typeface balooFont = Typeface.createFromAsset(getAssets(), "fonts/baloo.ttf");

		helpTitle.setTypeface(balooFont);
		helpText.setTypeface(balooFont);

		String content = "1. First, connect to the sensor by tapping the \"Connect Sensor\" button.\n\n"
				+ "2. Blow into the sensor for 5 seconds.\n\n"
				+ "3. Wait for the app to display your BAC result.\n\n"
				+ "---\n\n"
				+ "Credits:\n"
				+ "• Lucas\n"
				+ "• Chaoran\n"
				+ "• Natalie\n"
				+ "• Harris\n"
				+ "• Jacob";

		helpText.setText(content);

		builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

		AlertDialog dialog = builder.create();
		dialog.show();

		dialog.getButton(AlertDialog.BUTTON_POSITIVE)
				.setTextColor(ContextCompat.getColor(this, R.color.pink));
	}


	//start button functionality
	private void startSensor() {
		Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse);

		startSensorButton.setOnClickListener(v -> {
			v.startAnimation(pulseAnim);

			// Reset lastReceivedBAC before starting
			lastReceivedBAC = "0.00";

			serialSend("START\n");
			Toast.makeText(MainActivity.this, "Blow into the Sensor!", Toast.LENGTH_SHORT).show();
			showSmoothCountdown(5);

			// Wait for sensor reading, then go to CountdownActivity
			new Handler(Looper.getMainLooper()).postDelayed(() -> {
				Intent intent = new Intent(MainActivity.this, CountdownActivity.class);

				// Use the actual received BAC value
				intent.putExtra("BAC_VALUE", lastReceivedBAC);
				intent.putExtra("estimate_time", "Estimated below 0.03% in 2 hours");

				Log.d("BAC_TRANSFER", "Sending BAC to CountdownActivity: " + lastReceivedBAC);
				startActivity(intent);
			}, 5000); // Wait for full countdown
		});
	}

	private void showSmoothCountdown(int totalSeconds) {
		TextView countdownText = findViewById(R.id.countdownText);
		countdownText.setVisibility(View.VISIBLE);
		Handler handler = new Handler(Looper.getMainLooper());
		final int[] seconds = {totalSeconds};
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (seconds[0] > 0) {
					countdownText.setText(String.valueOf(seconds[0]));
					countdownText.setScaleX(0f);
					countdownText.setScaleY(0f);
					countdownText.setAlpha(0f);
					countdownText.animate()
							.scaleX(1f)
							.scaleY(1f)
							.alpha(1f)
							.setDuration(500)
							.start();
					seconds[0]--;
					handler.postDelayed(this, 1000);
				} else {
					countdownText.setVisibility(View.GONE);
				}
			}
		};
		handler.post(runnable);
	}

	@Override protected void onResume() { super.onResume(); onResumeProcess(); }
	@Override protected void onPause() { super.onPause(); onPauseProcess(); }
	@Override protected void onStop() { super.onStop(); onStopProcess(); }
	@Override protected void onDestroy() { super.onDestroy(); onDestroyProcess(); }
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {
		switch (theConnectionState) {
			case isConnected:
				buttonScan.setText("Connected");
				SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
				String existingMac = prefs.getString("SavedDevice", null);
				if (existingMac == null || existingMac.equals("manual_trigger")) {
					BluetoothDevice device = getLastBondedDevice();
					if (device != null) {
						prefs.edit().putString("SavedDevice", device.getAddress()).apply();
						Log.d("BLUNO_DEBUG", "Saved MAC: " + device.getAddress());
					}
				}
				break;
			case isConnecting: buttonScan.setText("Connecting"); break;
			case isToScan: buttonScan.setText("Scan"); break;
			case isScanning: buttonScan.setText("Scanning"); break;
			case isDisconnecting: buttonScan.setText("Disconnecting"); break;
			default: break;
		}
	}

	private BluetoothDevice getLastBondedDevice() {
		android.bluetooth.BluetoothAdapter adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
		if (adapter != null && (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
				checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
			Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
			if (bondedDevices != null && !bondedDevices.isEmpty()) {
				return bondedDevices.iterator().next();
			}
		}
		return null;
	}

	@Override
	public void onSerialReceived(String theString) {
		serialReceivedText.setText("");
		serialReceivedText.append(theString);
		((ScrollView) serialReceivedText.getParent()).fullScroll(View.FOCUS_DOWN);


		if (theString.contains("Peak BAC")) {
			try {
				String[] parts = theString.split(":");
				if (parts.length > 1) {
					lastReceivedBAC = parts[1].trim();
					Log.d("BAC_TRANSFER", "Updated BAC from Peak BAC: " + lastReceivedBAC);
					serialReceivedText.setText(lastReceivedBAC + " %");
				}
			} catch (Exception e) {
				lastReceivedBAC = "0.00";
			}
		} else {
			// Handle direct BAC value (just a number)
			try {
				String cleanValue = theString.trim();
				double bacValue = Double.parseDouble(cleanValue);
				if (bacValue >= 0.0 && bacValue <= 1.0) { // Reasonable BAC range
					lastReceivedBAC = String.format("%.4f", bacValue);
					Log.d("BAC_TRANSFER", "Updated BAC from direct value: " + lastReceivedBAC);
					serialReceivedText.setText(lastReceivedBAC + " %");
				}
			} catch (NumberFormatException e) {

				Log.d("BAC_TRANSFER", "Received non-numeric data: " + theString);
			}
		}
	}
}
