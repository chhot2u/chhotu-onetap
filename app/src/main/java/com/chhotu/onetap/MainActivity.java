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
    
    // Launcher for overlay permission
    private final ActivityResultLauncher<String> overlayPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show();
                startOverlayService();
            } else {
                Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show();
                updateServiceStatus();
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
        // Use static flag to check actual service state
        if (OverlayService.isRunning()) {
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
            if (enabledServices == null) {
                return false;
            }
            String expectedComponent = getPackageName() + "/" + DragService.class.getName();
            return enabledServices.contains(expectedComponent);
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
        updateServiceStatus();
    }
    
    private void stopOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        stopService(intent);
        updateServiceStatus();
    }
    
    private void updateServiceStatus() {
        // Check actual service state via static flag
        boolean serviceRunning = OverlayService.isRunning();
        
        if (serviceRunning) {
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
