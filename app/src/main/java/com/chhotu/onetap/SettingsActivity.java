package com.chhotu.onetap;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Advanced settings for gaming overlay.
 */
public class SettingsActivity extends AppCompatActivity {
    
    private GameSettings s;
    
    // UI
    private SeekBar sbSize, sbOpacity, sbSpeed, sbFire, sbTap, sbCombo;
    private TextView tvSize, tvOpacity, tvSpeed, tvFire, tvTap, tvCombo;
    private RadioGroup rgDir;
    private Spinner spDist;
    
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_settings);
        
        s = new GameSettings(this);
        
        init();
        load();
        listeners();
    }
    
    private void init() {
        sbSize = findViewById(R.id.sb_panel_size);
        sbOpacity = findViewById(R.id.sb_opacity);
        sbSpeed = findViewById(R.id.sb_swipe_speed);
        sbFire = findViewById(R.id.sb_auto_fire);
        sbTap = findViewById(R.id.sb_rapid_tap);
        sbCombo = findViewById(R.id.sb_combo_delay);
        
        tvSize = findViewById(R.id.tv_panel_size);
        tvOpacity = findViewById(R.id.tv_opacity);
        tvSpeed = findViewById(R.id.tv_swipe_speed);
        tvFire = findViewById(R.id.tv_auto_fire);
        tvTap = findViewById(R.id.tv_rapid_tap);
        tvCombo = findViewById(R.id.tv_combo_delay);
        
        rgDir = findViewById(R.id.rg_swipe_dir);
        spDist = findViewById(R.id.spinner_distance);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this, R.array.distances, android.R.layout.simple_spinner_dropdown_item
        );
        spDist.setAdapter(adapter);
    }
    
    private void load() {
        sbSize.setProgress(s.getPanelSize());
        tvSize.setText(getSizeName(s.getPanelSize()));
        
        sbOpacity.setProgress(s.getOpacity());
        tvOpacity.setText(s.getOpacity() + "%");
        
        int dir = s.getSwipeDirection();
        if (dir == 0) rgDir.check(R.id.rb_up);
        else if (dir == 1) rgDir.check(R.id.rb_down);
        else if (dir == 2) rgDir.check(R.id.rb_left);
        else rgDir.check(R.id.rb_right);
        
        sbSpeed.setProgress(s.getSwipeSpeed());
        tvSpeed.setText(s.getSwipeSpeed() + " ms");
        
        spDist.setSelection(s.getSwipeDistance());
        
        sbFire.setProgress(s.getAutoFireSpeed());
        tvFire.setText(s.getAutoFireSpeed() + " ms");
        
        sbTap.setProgress(s.getRapidTapInterval());
        tvTap.setText(s.getRapidTapInterval() + " ms");
        
        sbCombo.setProgress(s.getComboDelay());
        tvCombo.setText(s.getComboDelay() + " ms");
    }
    
    private void listeners() {
        sbSize.setOnSeekBarChangeListener(new SimpleSeek() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvSize.setText(getSizeName(p));
            }
        });
        
        sbOpacity.setOnSeekBarChangeListener(new SimpleSeek() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvOpacity.setText(p + "%");
            }
        });
        
        sbSpeed.setOnSeekBarChangeListener(new SimpleSeek() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvSpeed.setText(p + " ms");
            }
        });
        
        sbFire.setOnSeekBarChangeListener(new SimpleSeek() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvFire.setText(p + " ms");
            }
        });
        
        sbTap.setOnSeekBarChangeListener(new SimpleSeek() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvTap.setText(p + " ms");
            }
        });
        
        sbCombo.setOnSeekBarChangeListener(new SimpleSeek() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                tvCombo.setText(p + " ms");
            }
        });
        
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
        findViewById(R.id.btn_reset).setOnClickListener(v -> reset());
    }
    
    private void save() {
        s.setPanelSize(sbSize.getProgress());
        s.setOpacity(sbOpacity.getProgress());
        
        int dir = 0;
        if (rgDir.getCheckedRadioButtonId() == R.id.rb_down) dir = 1;
        else if (rgDir.getCheckedRadioButtonId() == R.id.rb_left) dir = 2;
        else if (rgDir.getCheckedRadioButtonId() == R.id.rb_right) dir = 3;
        s.setSwipeDirection(dir);
        
        s.setSwipeSpeed(sbSpeed.getProgress());
        s.setSwipeDistance(spDist.getSelectedItemPosition());
        s.setAutoFireSpeed(sbFire.getProgress());
        s.setRapidTapInterval(sbTap.getProgress());
        s.setComboDelay(sbCombo.getProgress());
        
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void reset() {
        s.reset();
        load();
        Toast.makeText(this, "Reset to defaults", Toast.LENGTH_SHORT).show();
    }
    
    private String getSizeName(int size) {
        switch (size) {
            case 0: return "Small";
            case 2: return "Large";
            default: return "Normal";
        }
    }
    
    private static class SimpleSeek implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar b, int p, boolean u) {}
        public void onStartTrackingTouch(SeekBar b) {}
        public void onStopTrackingTouch(SeekBar b) {}
    }
}
