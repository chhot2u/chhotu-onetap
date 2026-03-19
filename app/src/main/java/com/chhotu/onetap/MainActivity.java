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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * GamePad Pro - Main Activity
 */
public class MainActivity extends AppCompatActivity {
    
    private Button btnStart;
    private TextView tvStatus;
    
    private final ActivityResultLauncher<Intent> overlayLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            updateUI();
        });
    
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);
        
        init();
        updateUI();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
    
    private void init() {
        btnStart = findViewById(R.id.btn_start);
        tvStatus = findViewById(R.id.tv_status);
        
        btnStart.setOnClickListener(v -> toggle());
        
        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
        
        findViewById(R.id.btn_profiles).setOnClickListener(v -> {
            showInfo("Profiles", "Save different settings for each game!\n\nFeature coming in next update.");
        });
    }
    
    private void toggle() {
        if (OverlayService.isRunning()) {
            stop();
        } else {
            start();
        }
    }
    
    private void start() {
        if (!Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("GamePad Pro needs overlay permission to show the floating panel.")
                .setPositiveButton("Grant", (d, w) -> {
                    Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                    );
                    overlayLauncher.launch(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
            return;
        }
        
        Intent intent = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        
        updateUI();
        Toast.makeText(this, "GamePad Pro activated!", Toast.LENGTH_SHORT).show();
    }
    
    private void stop() {
        Intent intent = new Intent(this, OverlayService.class);
        stopService(intent);
        updateUI();
    }
    
    private void updateUI() {
        boolean running = OverlayService.isRunning();
        
        tvStatus.setText(running ? "Service: RUNNING" : "Service: STOPPED");
        tvStatus.setTextColor(running ? 0xFF51CF66 : 0xFFFF6B6B);
        btnStart.setText(running ? "STOP OVERLAY" : "START OVERLAY");
    }
    
    private void showInfo(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
}
