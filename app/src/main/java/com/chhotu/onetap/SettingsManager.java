package com.chhotu.onetap;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages app settings using SharedPreferences.
 * Stores trigger mode, drag direction, speed, and position configuration.
 */
public class SettingsManager {
    
    private static final String PREFS_NAME = "chhotu_onetap_prefs";
    
    // Trigger mode keys
    private static final String KEY_TRIGGER_MODE = "trigger_mode";
    
    // Drag direction keys
    private static final String KEY_DRAG_DIRECTION = "drag_direction";
    
    // Drag speed keys
    private static final String KEY_DRAG_SPEED = "drag_speed";
    
    // Drag position keys (percentages 0-100)
    private static final String KEY_START_X = "start_x";
    private static final String KEY_START_Y = "start_y";
    private static final String KEY_END_X = "end_x";
    private static final String KEY_END_Y = "end_y";
    
    // Drag distance keys
    private static final String KEY_DRAG_DISTANCE = "drag_distance";
    
    // Trigger modes
    public static final int TRIGGER_MODE_TAP = 0;
    public static final int TRIGGER_MODE_LONG_PRESS = 1;
    
    // Drag directions
    public static final int DRAG_UP = 0;
    public static final int DRAG_DOWN = 1;
    public static final int DRAG_LEFT = 2;
    public static final int DRAG_RIGHT = 3;
    
    // Drag distance presets
    public static final int DISTANCE_SHORT = 0;    // 20% of screen
    public static final int DISTANCE_MEDIUM = 1;   // 40% of screen
    public static final int DISTANCE_LONG = 2;     // 60% of screen
    public static final int DISTANCE_FULL = 3;     // 80% of screen
    public static final int DISTANCE_CUSTOM = 4;   // User-defined
    
    // Speed constraints
    public static final int DEFAULT_DRAG_SPEED = 300;
    public static final int MIN_DRAG_SPEED = 50;
    public static final int MAX_DRAG_SPEED = 2000;
    
    // Default drag distance percentage
    public static final int DEFAULT_DRAG_DISTANCE = DISTANCE_FULL;
    
    private final SharedPreferences prefs;
    
    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // ==================== TRIGGER MODE ====================
    
    public int getTriggerMode() {
        return prefs.getInt(KEY_TRIGGER_MODE, TRIGGER_MODE_TAP);
    }
    
    public void setTriggerMode(int mode) {
        if (mode == TRIGGER_MODE_TAP || mode == TRIGGER_MODE_LONG_PRESS) {
            prefs.edit().putInt(KEY_TRIGGER_MODE, mode).apply();
        }
    }
    
    public boolean isTapMode() {
        return getTriggerMode() == TRIGGER_MODE_TAP;
    }
    
    public boolean isLongPressMode() {
        return getTriggerMode() == TRIGGER_MODE_LONG_PRESS;
    }
    
    // ==================== DRAG DIRECTION ====================
    
    public int getDragDirection() {
        return prefs.getInt(KEY_DRAG_DIRECTION, DRAG_UP);
    }
    
    public void setDragDirection(int direction) {
        if (direction >= DRAG_UP && direction <= DRAG_RIGHT) {
            prefs.edit().putInt(KEY_DRAG_DIRECTION, direction).apply();
        }
    }
    
    public String getDragDirectionName() {
        switch (getDragDirection()) {
            case DRAG_UP: return "Up";
            case DRAG_DOWN: return "Down";
            case DRAG_LEFT: return "Left";
            case DRAG_RIGHT: return "Right";
            default: return "Up";
        }
    }
    
    // ==================== DRAG SPEED ====================
    
    public int getDragSpeed() {
        return prefs.getInt(KEY_DRAG_SPEED, DEFAULT_DRAG_SPEED);
    }
    
    public void setDragSpeed(int speedMs) {
        prefs.edit().putInt(KEY_DRAG_SPEED, clamp(speedMs, MIN_DRAG_SPEED, MAX_DRAG_SPEED)).apply();
    }
    
    // ==================== DRAG DISTANCE ====================
    
    public int getDragDistance() {
        return prefs.getInt(KEY_DRAG_DISTANCE, DEFAULT_DRAG_DISTANCE);
    }
    
    public void setDragDistance(int distance) {
        if (distance >= DISTANCE_SHORT && distance <= DISTANCE_CUSTOM) {
            prefs.edit().putInt(KEY_DRAG_DISTANCE, distance).apply();
        }
    }
    
    /**
     * Returns the drag distance as a percentage of screen size.
     */
    public float getDragDistancePercent() {
        switch (getDragDistance()) {
            case DISTANCE_SHORT: return 0.20f;
            case DISTANCE_MEDIUM: return 0.40f;
            case DISTANCE_LONG: return 0.60f;
            case DISTANCE_FULL: return 0.80f;
            case DISTANCE_CUSTOM: return getCustomDistancePercent();
            default: return 0.80f;
        }
    }
    
    private float getCustomDistancePercent() {
        return prefs.getFloat("custom_distance", 0.50f);
    }
    
    public void setCustomDistancePercent(float percent) {
        prefs.edit().putFloat("custom_distance", clamp(percent, 0.10f, 1.0f)).apply();
    }
    
    // ==================== START POSITION ====================
    
    public float getStartXPercent() {
        return prefs.getFloat(KEY_START_X, 0.50f); // Default: center
    }
    
    public float getStartYPercent() {
        return prefs.getFloat(KEY_START_Y, 0.70f); // Default: 70% from top
    }
    
    public void setStartXPercent(float percent) {
        prefs.edit().putFloat(KEY_START_X, clamp(percent, 0.0f, 1.0f)).apply();
    }
    
    public void setStartYPercent(float percent) {
        prefs.edit().putFloat(KEY_START_Y, clamp(percent, 0.0f, 1.0f)).apply();
    }
    
    // ==================== END POSITION ====================
    
    public float getEndXPercent() {
        return prefs.getFloat(KEY_END_X, 0.50f); // Default: center
    }
    
    public float getEndYPercent() {
        return prefs.getFloat(KEY_END_Y, 0.30f); // Default: 30% from top
    }
    
    public void setEndXPercent(float percent) {
        prefs.edit().putFloat(KEY_END_X, clamp(percent, 0.0f, 1.0f)).apply();
    }
    
    public void setEndYPercent(float percent) {
        prefs.edit().putFloat(KEY_END_Y, clamp(percent, 0.0f, 1.0f)).apply();
    }
    
    // ==================== HELPERS ====================
    
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
