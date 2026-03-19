package com.chhotu.onetap;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages app settings using SharedPreferences.
 * Stores trigger mode (tap vs long-press) and drag speed.
 * 
 * All speed values are automatically clamped to valid range.
 */
public class SettingsManager {
    
    private static final String PREFS_NAME = "chhotu_onetap_prefs";
    private static final String KEY_TRIGGER_MODE = "trigger_mode";
    private static final String KEY_DRAG_SPEED = "drag_speed";
    
    public static final int TRIGGER_MODE_TAP = 0;
    public static final int TRIGGER_MODE_LONG_PRESS = 1;
    
    public static final int DEFAULT_DRAG_SPEED = 500;
    public static final int MIN_DRAG_SPEED = 100;
    public static final int MAX_DRAG_SPEED = 2000;
    
    private final SharedPreferences prefs;
    
    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public int getTriggerMode() {
        return prefs.getInt(KEY_TRIGGER_MODE, TRIGGER_MODE_TAP);
    }
    
    public void setTriggerMode(int mode) {
        // Validate: only accept known trigger modes
        if (mode == TRIGGER_MODE_TAP || mode == TRIGGER_MODE_LONG_PRESS) {
            prefs.edit().putInt(KEY_TRIGGER_MODE, mode).apply();
        }
    }
    
    public int getDragSpeed() {
        return prefs.getInt(KEY_DRAG_SPEED, DEFAULT_DRAG_SPEED);
    }
    
    public void setDragSpeed(int speedMs) {
        // Clamp speed to valid range
        int clampedSpeed = clampSpeed(speedMs);
        prefs.edit().putInt(KEY_DRAG_SPEED, clampedSpeed).apply();
    }
    
    /**
     * Clamps a speed value to the valid range [MIN_DRAG_SPEED, MAX_DRAG_SPEED].
     */
    private int clampSpeed(int speedMs) {
        if (speedMs < MIN_DRAG_SPEED) {
            return MIN_DRAG_SPEED;
        }
        if (speedMs > MAX_DRAG_SPEED) {
            return MAX_DRAG_SPEED;
        }
        return speedMs;
    }
    
    public boolean isTapMode() {
        return getTriggerMode() == TRIGGER_MODE_TAP;
    }
    
    public boolean isLongPressMode() {
        return getTriggerMode() == TRIGGER_MODE_LONG_PRESS;
    }
}
