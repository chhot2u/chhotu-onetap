package com.chhotu.onetap;

import android.content.ComponentName;
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
 * Main activity for Chhotu OneTap.
 */
public class MainActivity extends AppCompatActivity {
    
    private SettingsManager settingsManager;
    private Button btnToggleService;
    private Button btnSettings;
    private Button btnCheckPermissions;
    private TextView tvServiceStatus;
    private TextView tvPermissionStatus;
    
    // Launcher for overlay permission
    private final ActivityResultLauncher<Intent> overlayPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            updatePermissionStatus();
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission granted!", Toast.LENGTH_SHORT).show();
            }
        });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        settingsManager = new SettingsManager(this);
        
        initViews();
        setupListeners();
        updatePermissionStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus();
        updateServiceStatus();
    }
    
    private void initViews() {
        btnToggleService = findViewById(R.id.btn_toggle_service);
        btnSettings = findViewById(R.id.btn_settings);
        btnCheckPermissions = findViewById(R.id.btn_check_permissions);
        tvServiceStatus = findViewById(R.id.tv_service_status);
        tvPermissionStatus = findViewById(R.id.tv_permission_status);
    }
    
    private void setupListeners() {
        btnToggleService.setOnClickListener(v -> toggleService());
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        btnCheckPermissions.setOnClickListener(v -> checkPermissions());
    }
    
    private void checkPermissions() {
        boolean hasOverlay = Settings.canDrawOverlays(this);
        boolean hasAccessibility = isAccessibilityServiceEnabled();
        
        StringBuilder status = new StringBuilder();
        status.append("Overlay: ").append(hasOverlay ? "✓" : "✗").append("\n");
        status.append("Accessibility: ").append(hasAccessibility ? "✓" : "✗").append("\n\n");
        
        if (!hasOverlay) {
            status.append("Grant overlay permission first.");
        } else if (!hasAccessibility) {
            status.append("Grant accessibility permission next.");
        } else {
            status.append("All permissions granted!");
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Permission Status")
            .setMessage(status.toString())
            .setPositiveButton("Grant Overlay", (d, w) -> requestOverlayPermission())
            .setNeutralButton("Grant Accessibility", (d, w) -> requestAccessibilityPermission())
            .setNegativeButton("OK", null)
            .show();
    }
    
    private void updatePermissionStatus() {
        boolean hasOverlay = Settings.canDrawOverlays(this);
        boolean hasAccessibility = isAccessibilityServiceEnabled();
        
        String status = (hasOverlay ? "✓" : "✗") + " Overlay\n" +
                       (hasAccessibility ? "✓" : "✗") + " Accessibility";
        
        tvPermissionStatus.setText(status);
    }
    
    private void updateServiceStatus() {
        if (OverlayService.isRunning()) {
            tvServiceStatus.setText("Service: RUNNING");
            btnToggleService.setText(R.string.stop_service);
        } else {
            tvServiceStatus.setText("Service: STOPPED");
            btnToggleService.setText(R.string.start_service);
        }
    }
    
    private void toggleService() {
        if (OverlayService.isRunning()) {
            stopOverlayService();
        } else {
            startOverlayService();
        }
    }
    
    private void startOverlayService() {
        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
            return;
        }
        
        // Check accessibility permission
        if (!isAccessibilityServiceEnabled()) {
            new AlertDialog.Builder(this)
                .setTitle("Accessibility Required")
                .setMessage("Chhotu OneTap needs Accessibility Service to perform gestures.\n\n" +
                           "Please enable 'Chhotu OneTap' in Accessibility settings.")
                .setPositiveButton("Open Settings", (d, w) -> requestAccessibilityPermission())
                .setNegativeButton("Cancel", null)
                .show();
            return;
        }
        
        // Start the service
        Intent intent = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        
        updateServiceStatus();
        Toast.makeText(this, "Service started! Tap the icon to drag.", Toast.LENGTH_SHORT).show();
    }
    
    private void stopOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        stopService(intent);
        updateServiceStatus();
    }
    
    private void requestOverlayPermission() {
        Intent intent = new Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + getPackageName())
        );
        overlayPermissionLauncher.launch(intent);
    }
    
    private void requestAccessibilityPermission() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
    
    private boolean isAccessibilityServiceEnabled() {
        try {
            String enabledServices = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            if (enabledServices == null) return false;
            
            ComponentName serviceComponent = new ComponentName(this, DragService.class);
            return enabledServices.contains(serviceComponent.flattenToString());
        } catch (Exception e) {
            return false;
        }
    }
}
