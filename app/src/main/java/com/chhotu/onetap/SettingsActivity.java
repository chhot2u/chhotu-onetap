package com.chhotu.onetap;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Settings activity for gesture configuration.
 */
public class SettingsActivity extends AppCompatActivity {
    
    private SettingsManager settings;
    
    private RadioGroup rgDirection;
    private SeekBar sbSpeed;
    private TextView tvSpeed;
    private Spinner spinnerDistance;
    
    private TextView tvStartX, tvStartY;
    private SeekBar sbStartX, sbStartY;
    
    private TextView tvEndX, tvEndY;
    private SeekBar sbEndX, sbEndY;
    
    private String[] distanceLabels = {
        "Short (20%)", "Medium (40%)", "Long (60%)", "Full (80%)"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        settings = new SettingsManager(this);
        
        initViews();
        setupDistanceSpinner();
        loadSettings();
        setupListeners();
    }
    
    private void initViews() {
        rgDirection = findViewById(R.id.rg_direction);
        sbSpeed = findViewById(R.id.sb_speed);
        tvSpeed = findViewById(R.id.tv_speed);
        spinnerDistance = findViewById(R.id.spinner_distance);
        
        tvStartX = findViewById(R.id.tv_start_x);
        tvStartY = findViewById(R.id.tv_start_y);
        sbStartX = findViewById(R.id.sb_start_x);
        sbStartY = findViewById(R.id.sb_start_y);
        
        tvEndX = findViewById(R.id.tv_end_x);
        tvEndY = findViewById(R.id.tv_end_y);
        sbEndX = findViewById(R.id.sb_end_x);
        sbEndY = findViewById(R.id.sb_end_y);
    }
    
    private void setupDistanceSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_dropdown_item, distanceLabels
        );
        spinnerDistance.setAdapter(adapter);
    }
    
    private void loadSettings() {
        // Direction
        int dir = settings.getDragDirection();
        switch (dir) {
            case SettingsManager.DRAG_UP: rgDirection.check(R.id.rb_up); break;
            case SettingsManager.DRAG_DOWN: rgDirection.check(R.id.rb_down); break;
            case SettingsManager.DRAG_LEFT: rgDirection.check(R.id.rb_left); break;
            case SettingsManager.DRAG_RIGHT: rgDirection.check(R.id.rb_right); break;
        }
        
        // Speed
        sbSpeed.setProgress(settings.getDragSpeed());
        tvSpeed.setText(settings.getDragSpeed() + " ms");
        
        // Distance
        spinnerDistance.setSelection(settings.getDragDistance());
        
        // Start position
        int sx = (int)(settings.getStartXPercent() * 100);
        int sy = (int)(settings.getStartYPercent() * 100);
        sbStartX.setProgress(sx);
        sbStartY.setProgress(sy);
        tvStartX.setText("X: " + sx + "%");
        tvStartY.setText("Y: " + sy + "%");
        
        // End position
        int ex = (int)(settings.getEndXPercent() * 100);
        int ey = (int)(settings.getEndYPercent() * 100);
        sbEndX.setProgress(ex);
        sbEndY.setProgress(ey);
        tvEndX.setText("X: " + ex + "%");
        tvEndY.setText("Y: " + ey + "%");
    }
    
    private void setupListeners() {
        // Speed
        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvSpeed.setText(p + " ms");
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });
        
        // Start X
        sbStartX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvStartX.setText("X: " + p + "%");
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });
        
        // Start Y
        sbStartY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvStartY.setText("Y: " + p + "%");
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });
        
        // End X
        sbEndX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvEndX.setText("X: " + p + "%");
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });
        
        // End Y
        sbEndY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvEndY.setText("Y: " + p + "%");
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });
        
        // Save button
        findViewById(R.id.btn_save).setOnClickListener(v -> saveSettings());
    }
    
    private void saveSettings() {
        // Direction
        int dirId = rgDirection.getCheckedRadioButtonId();
        int dir = SettingsManager.DRAG_UP;
        if (dirId == R.id.rb_down) dir = SettingsManager.DRAG_DOWN;
        else if (dirId == R.id.rb_left) dir = SettingsManager.DRAG_LEFT;
        else if (dirId == R.id.rb_right) dir = SettingsManager.DRAG_RIGHT;
        settings.setDragDirection(dir);
        
        // Speed
        settings.setDragSpeed(sbSpeed.getProgress());
        
        // Distance
        settings.setDragDistance(spinnerDistance.getSelectedItemPosition());
        
        // Start position
        settings.setStartXPercent(sbStartX.getProgress() / 100f);
        settings.setStartYPercent(sbStartY.getProgress() / 100f);
        
        // End position
        settings.setEndXPercent(sbEndX.getProgress() / 100f);
        settings.setEndYPercent(sbEndY.getProgress() / 100f);
        
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
