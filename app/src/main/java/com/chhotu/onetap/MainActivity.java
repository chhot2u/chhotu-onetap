package com.chhotu.onetap;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Main activity for Chhotu OneTap.
 * 
 * Provides controls to:
 * - Start/stop the overlay service
 * - Open settings for trigger mode and speed configuration
 * - Grant required permissions (overlay, accessibility)
 */
public class MainActivity extends AppCompatActivity {
    
    private SettingsManager settingsManager;
    private Button btnToggleService;
    private Button btnSettings;
    private TextView tvServiceStatus;
    
    private boolean isServiceRunning = false;
    
    // Launcher for overlay permission
    private final ActivityResultLauncher<String> overlayPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show();
                startOverlayService();
            } else {
                Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show();
            }
        });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        settingsManager = new SettingsManager(this);
        
        initViews();
        setupListeners();
        updateServiceStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }
    
    private void initViews() {
        btnToggleService = findViewById(R.id.btn_toggle_service);
        btnSettings = findViewById(R.id.btn_settings);
        tvServiceStatus = findViewById(R.id.tv_service_status);
    }
    
    private void setupListeners() {
        btnToggleService.setOnClickListener(v -> toggleService());
        btnSettings.setOnClickListener(v -> openSettings());
    }
    
    private void toggleService() {
        if (isServiceRunning) {
            stopOverlayService();
        } else {
            if (checkPermissions()) {
                startOverlayService();
            }
        }
    }
    
    private boolean checkPermissions() {
        // Check overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
                );
                overlayPermissionLauncher.launch(intent);
                return false;
            }
        }
        
        // Check accessibility permission
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, 
                "Please enable accessibility service for Chhotu OneTap", 
                Toast.LENGTH_LONG).show();
            
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            return false;
        }
        
        return true;
    }
    
    private boolean isAccessibilityServiceEnabled() {
        try {
            String enabledServices = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            return enabledServices != null && 
                   enabledServices.contains(getPackageName() + "/" + DragService.class.getName());
        } catch (Exception e) {
            return false;
        }
    }
    
    private void startOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        isServiceRunning = true;
        updateServiceStatus();
    }
    
    private void stopOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        stopService(intent);
        isServiceRunning = false;
        updateServiceStatus();
    }
    
    private void updateServiceStatus() {
        if (isServiceRunning) {
            tvServiceStatus.setText(R.string.service_running);
            btnToggleService.setText(R.string.stop_service);
        } else {
            tvServiceStatus.setText(R.string.service_stopped);
            btnToggleService.setText(R.string.start_service);
        }
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
