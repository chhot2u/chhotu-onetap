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
 * Settings activity for configuring all drag options.
 * 
 * Features:
 * - Trigger mode (Tap / Long-Press)
 * - Drag direction (Up / Down / Left / Right)
 * - Drag speed (50ms - 2000ms)
 * - Drag distance (Short / Medium / Long / Full / Custom)
 * - Start position (X% / Y%)
 * - End position (X% / Y%)
 */
public class SettingsActivity extends AppCompatActivity {
    
    private SettingsManager settingsManager;
    
    // Trigger mode
    private RadioGroup rgTriggerMode;
    
    // Drag direction
    private RadioGroup rgDragDirection;
    
    // Drag speed
    private SeekBar sbDragSpeed;
    private TextView tvSpeedValue;
    
    // Drag distance
    private Spinner spinnerDistance;
    private String[] distanceOptions;
    
    // Start position
    private SeekBar sbStartX, sbStartY;
    private TextView tvStartX, tvStartY;
    
    // End position
    private SeekBar sbEndX, sbEndY;
    private TextView tvEndX, tvEndY;
    
    // Buttons
    private Button btnSave, btnReset;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        settingsManager = new SettingsManager(this);
        
        initViews();
        setupDistanceSpinner();
        loadCurrentSettings();
        setupListeners();
    }
    
    private void initViews() {
        // Trigger mode
        rgTriggerMode = findViewById(R.id.rg_trigger_mode);
        
        // Drag direction
        rgDragDirection = findViewById(R.id.rg_drag_direction);
        
        // Drag speed
        sbDragSpeed = findViewById(R.id.sb_drag_speed);
        tvSpeedValue = findViewById(R.id.tv_speed_value);
        
        // Drag distance
        spinnerDistance = findViewById(R.id.spinner_distance);
        
        // Start position
        sbStartX = findViewById(R.id.sb_start_x);
        sbStartY = findViewById(R.id.sb_start_y);
        tvStartX = findViewById(R.id.tv_start_x);
        tvStartY = findViewById(R.id.tv_start_y);
        
        // End position
        sbEndX = findViewById(R.id.sb_end_x);
        sbEndY = findViewById(R.id.sb_end_y);
        tvEndX = findViewById(R.id.tv_end_x);
        tvEndY = findViewById(R.id.tv_end_y);
        
        // Buttons
        btnSave = findViewById(R.id.btn_save);
        btnReset = findViewById(R.id.btn_reset);
    }
    
    private void setupDistanceSpinner() {
        distanceOptions = new String[] {
            getString(R.string.distance_short),
            getString(R.string.distance_medium),
            getString(R.string.distance_long),
            getString(R.string.distance_full),
            getString(R.string.distance_custom)
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_dropdown_item, distanceOptions
        );
        spinnerDistance.setAdapter(adapter);
    }
    
    private void loadCurrentSettings() {
        // Trigger mode
        rgTriggerMode.check(
            settingsManager.getTriggerMode() == SettingsManager.TRIGGER_MODE_LONG_PRESS
                ? R.id.rb_long_press
                : R.id.rb_tap
        );
        
        // Drag direction
        int direction = settingsManager.getDragDirection();
        switch (direction) {
            case SettingsManager.DRAG_UP: rgDragDirection.check(R.id.rb_up); break;
            case SettingsManager.DRAG_DOWN: rgDragDirection.check(R.id.rb_down); break;
            case SettingsManager.DRAG_LEFT: rgDragDirection.check(R.id.rb_left); break;
            case SettingsManager.DRAG_RIGHT: rgDragDirection.check(R.id.rb_right); break;
        }
        
        // Drag speed
        int speed = settingsManager.getDragSpeed();
        sbDragSpeed.setProgress(speed);
        updateSpeedLabel(speed);
        
        // Drag distance
        spinnerDistance.setSelection(settingsManager.getDragDistance());
        
        // Start position
        int startX = (int) (settingsManager.getStartXPercent() * 100);
        int startY = (int) (settingsManager.getStartYPercent() * 100);
        sbStartX.setProgress(startX);
        sbStartY.setProgress(startY);
        updatePositionLabel(tvStartX, "X", startX);
        updatePositionLabel(tvStartY, "Y", startY);
        
        // End position
        int endX = (int) (settingsManager.getEndXPercent() * 100);
        int endY = (int) (settingsManager.getEndYPercent() * 100);
        sbEndX.setProgress(endX);
        sbEndY.setProgress(endY);
        updatePositionLabel(tvEndX, "X", endX);
        updatePositionLabel(tvEndY, "Y", endY);
    }
    
    private void setupListeners() {
        // Speed slider
        sbDragSpeed.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) updateSpeedLabel(progress);
            }
        });
        
        // Start X slider
        sbStartX.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) updatePositionLabel(tvStartX, "X", progress);
            }
        });
        
        // Start Y slider
        sbStartY.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) updatePositionLabel(tvStartY, "Y", progress);
            }
        });
        
        // End X slider
        sbEndX.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) updatePositionLabel(tvEndX, "X", progress);
            }
        });
        
        // End Y slider
        sbEndY.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) updatePositionLabel(tvEndY, "Y", progress);
            }
        });
        
        // Save button
        btnSave.setOnClickListener(v -> saveSettings());
        
        // Reset button
        btnReset.setOnClickListener(v -> resetToDefaults());
    }
    
    private void updateSpeedLabel(int speedMs) {
        tvSpeedValue.setText(String.format("%d ms", speedMs));
    }
    
    private void updatePositionLabel(TextView tv, String axis, int percent) {
        tv.setText(String.format("%s: %d%%", axis, percent));
    }
    
    private void saveSettings() {
        // Save trigger mode
        int triggerMode = rgTriggerMode.getCheckedRadioButtonId() == R.id.rb_long_press
            ? SettingsManager.TRIGGER_MODE_LONG_PRESS
            : SettingsManager.TRIGGER_MODE_TAP;
        settingsManager.setTriggerMode(triggerMode);
        
        // Save drag direction
        int directionId = rgDragDirection.getCheckedRadioButtonId();
        int direction;
        if (directionId == R.id.rb_down) {
            direction = SettingsManager.DRAG_DOWN;
        } else if (directionId == R.id.rb_left) {
            direction = SettingsManager.DRAG_LEFT;
        } else if (directionId == R.id.rb_right) {
            direction = SettingsManager.DRAG_RIGHT;
        } else {
            direction = SettingsManager.DRAG_UP;
        }
        settingsManager.setDragDirection(direction);
        
        // Save drag speed
        settingsManager.setDragSpeed(sbDragSpeed.getProgress());
        
        // Save drag distance
        settingsManager.setDragDistance(spinnerDistance.getSelectedItemPosition());
        
        // Save start position
        settingsManager.setStartXPercent(sbStartX.getProgress() / 100f);
        settingsManager.setStartYPercent(sbStartY.getProgress() / 100f);
        
        // Save end position
        settingsManager.setEndXPercent(sbEndX.getProgress() / 100f);
        settingsManager.setEndYPercent(sbEndY.getProgress() / 100f);
        
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void resetToDefaults() {
        // Reset trigger mode
        rgTriggerMode.check(R.id.rb_tap);
        
        // Reset drag direction
        rgDragDirection.check(R.id.rb_up);
        
        // Reset drag speed
        sbDragSpeed.setProgress(SettingsManager.DEFAULT_DRAG_SPEED);
        updateSpeedLabel(SettingsManager.DEFAULT_DRAG_SPEED);
        
        // Reset drag distance
        spinnerDistance.setSelection(SettingsManager.DISTANCE_FULL);
        
        // Reset start position (50%, 70%)
        sbStartX.setProgress(50);
        sbStartY.setProgress(70);
        updatePositionLabel(tvStartX, "X", 50);
        updatePositionLabel(tvStartY, "Y", 70);
        
        // Reset end position (50%, 30%)
        sbEndX.setProgress(50);
        sbEndY.setProgress(30);
        updatePositionLabel(tvEndX, "X", 50);
        updatePositionLabel(tvEndY, "Y", 30);
        
        Toast.makeText(this, "Defaults restored", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Simple seek bar listener with empty method implementations.
     */
    private static class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
        
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
