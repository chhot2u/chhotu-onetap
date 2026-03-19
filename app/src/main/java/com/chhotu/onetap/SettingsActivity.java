package com.chhotu.onetap;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Settings activity for configuring trigger mode and drag speed.
 * 
 * Features:
 * - Toggle between Tap and Long-Press trigger modes
 * - Custom speed slider (100ms to 2000ms)
 * - Real-time speed preview
 */
public class SettingsActivity extends AppCompatActivity {
    
    private SettingsManager settingsManager;
    
    private RadioGroup rgTriggerMode;
    private SeekBar sbDragSpeed;
    private TextView tvSpeedValue;
    private Button btnSave;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        settingsManager = new SettingsManager(this);
        
        initViews();
        loadCurrentSettings();
        setupListeners();
    }
    
    private void initViews() {
        rgTriggerMode = findViewById(R.id.rg_trigger_mode);
        sbDragSpeed = findViewById(R.id.sb_drag_speed);
        tvSpeedValue = findViewById(R.id.tv_speed_value);
        btnSave = findViewById(R.id.btn_save);
    }
    
    private void loadCurrentSettings() {
        // Load trigger mode
        int triggerMode = settingsManager.getTriggerMode();
        if (triggerMode == SettingsManager.TRIGGER_MODE_LONG_PRESS) {
            rgTriggerMode.check(R.id.rb_long_press);
        } else {
            rgTriggerMode.check(R.id.rb_tap);
        }
        
        // Load drag speed
        int dragSpeed = settingsManager.getDragSpeed();
        sbDragSpeed.setProgress(dragSpeed);
        updateSpeedLabel(dragSpeed);
    }
    
    private void setupListeners() {
        // Trigger mode change
        rgTriggerMode.setOnCheckedChangeListener((group, checkedId) -> {
            // Settings will be saved when save button is clicked
        });
        
        // Speed slider change
        sbDragSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateSpeedLabel(progress);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Save button
        btnSave.setOnClickListener(v -> saveSettings());
    }
    
    private void updateSpeedLabel(int speedMs) {
        tvSpeedValue.setText(String.format("%d ms", speedMs));
    }
    
    private void saveSettings() {
        // Save trigger mode
        int triggerMode = rgTriggerMode.getCheckedRadioButtonId() == R.id.rb_long_press
            ? SettingsManager.TRIGGER_MODE_LONG_PRESS
            : SettingsManager.TRIGGER_MODE_TAP;
        settingsManager.setTriggerMode(triggerMode);
        
        // Save drag speed
        int dragSpeed = sbDragSpeed.getProgress();
        settingsManager.setDragSpeed(dragSpeed);
        
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
