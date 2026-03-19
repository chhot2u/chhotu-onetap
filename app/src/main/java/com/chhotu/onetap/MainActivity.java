package com.chhotu.onetap;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Simple main activity with permission handling.
 */
public class MainActivity extends AppCompatActivity {
    
    private Button btnStart;
    private Button btnSettings;
    private TextView tvStatus;
    private TextView tvHint;
    
    private final ActivityResultLauncher<Intent> overlayLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            updateStatus();
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            }
        });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupListeners();
        updateStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
    
    private void initViews() {
        btnStart = findViewById(R.id.btn_start);
        btnSettings = findViewById(R.id.btn_settings);
        tvStatus = findViewById(R.id.tv_status);
        tvHint = findViewById(R.id.tv_hint);
    }
    
    private void setupListeners() {
        btnStart.setOnClickListener(v -> toggleService());
        btnSettings.setOnClickListener(v -> openSettings());
    }
    
    private void toggleService() {
        if (OverlayService.isRunning()) {
            stopService();
        } else {
            startService();
        }
    }
    
    private void startService() {
        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            showPermissionDialog();
            return;
        }
        
        // Start service
        Intent intent = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        
        Toast.makeText(this, "Service started! Tap the button to perform gesture.", Toast.LENGTH_LONG).show();
        updateStatus();
    }
    
    private void stopService() {
        Intent intent = new Intent(this, OverlayService.class);
        stopService(intent);
        updateStatus();
    }
    
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Chhotu OneTap needs permission to display over other apps.\n\n" +
                       "Tap 'Grant' to open settings, then enable 'Display over other apps' for this app.")
            .setPositiveButton("Grant", (d, w) -> requestOverlayPermission())
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void requestOverlayPermission() {
        Intent intent = new Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + getPackageName())
        );
        overlayLauncher.launch(intent);
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    
    private void updateStatus() {
        boolean hasOverlay = Settings.canDrawOverlays(this);
        boolean isRunning = OverlayService.isRunning();
        
        StringBuilder status = new StringBuilder();
        
        // Permission status
        status.append("📱 Permission: ");
        status.append(hasOverlay ? "✅ Granted" : "❌ Not Granted");
        status.append("\n\n");
        
        // Service status
        status.append("⚙️ Service: ");
        status.append(isRunning ? "🟢 Running" : "⚪ Stopped");
        
        tvStatus.setText(status.toString());
        
        // Update button text
        btnStart.setText(isRunning ? "Stop Service" : "Start Service");
        
        // Update hint
        if (isRunning) {
            tvHint.setText("A floating button appeared at the bottom of your screen. Tap it to perform a drag gesture!");
        } else if (hasOverlay) {
            tvHint.setText("Tap 'Start Service' to begin. Then tap the floating button to perform gestures.");
        } else {
            tvHint.setText("Grant overlay permission first, then start the service.");
        }
    }
}
