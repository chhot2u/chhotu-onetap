package com.chhotu.onetap;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages app settings using SharedPreferences.
 * Stores trigger mode (tap vs long-press) and drag speed.
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
        prefs.edit().putInt(KEY_TRIGGER_MODE, mode).apply();
    }
    
    public int getDragSpeed() {
        return prefs.getInt(KEY_DRAG_SPEED, DEFAULT_DRAG_SPEED);
    }
    
    public void setDragSpeed(int speedMs) {
        prefs.edit().putInt(KEY_DRAG_SPEED, speedMs).apply();
    }
    
    public boolean isTapMode() {
        return getTriggerMode() == TRIGGER_MODE_TAP;
    }
    
    public boolean isLongPressMode() {
        return getTriggerMode() == TRIGGER_MODE_LONG_PRESS;
    }
}
