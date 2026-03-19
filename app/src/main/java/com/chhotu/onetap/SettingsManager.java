package com.chhotu.onetap;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple settings manager.
 */
public class SettingsManager {
    
    private static final String PREFS = "chhotu_prefs";
    private static final String K_DIR = "direction";
    private static final String K_SPEED = "speed";
    private static final String K_DIST = "distance";
    private static final String K_SX = "start_x";
    private static final String K_SY = "start_y";
    private static final String K_EX = "end_x";
    private static final String K_EY = "end_y";
    
    // Directions
    public static final int DRAG_UP = 0;
    public static final int DRAG_DOWN = 1;
    public static final int DRAG_LEFT = 2;
    public static final int DRAG_RIGHT = 3;
    
    // Distance presets
    public static final int DIST_SHORT = 0;
    public static final int DIST_MEDIUM = 1;
    public static final int DIST_LONG = 2;
    public static final int DIST_FULL = 3;
    
    private final SharedPreferences p;
    
    public SettingsManager(Context ctx) {
        p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    
    public int getDragDirection() {
        return p.getInt(K_DIR, DRAG_UP);
    }
    public void setDragDirection(int d) {
        p.edit().putInt(K_DIR, d).apply();
    }
    
    public int getDragSpeed() {
        return p.getInt(K_SPEED, 300);
    }
    public void setDragSpeed(int s) {
        p.edit().putInt(K_SPEED, clamp(s, 100, 1000)).apply();
    }
    
    public int getDragDistance() {
        return p.getInt(K_DIST, DIST_FULL);
    }
    public void setDragDistance(int d) {
        p.edit().putInt(K_DIST, d).apply();
    }
    
    public float getDragDistancePercent() {
        switch (getDragDistance()) {
            case DIST_SHORT: return 0.20f;
            case DIST_MEDIUM: return 0.40f;
            case DIST_LONG: return 0.60f;
            default: return 0.80f;
        }
    }
    
    public float getStartXPercent() {
        return p.getFloat(K_SX, 0.50f);
    }
    public void setStartXPercent(float v) {
        p.edit().putFloat(K_SX, clampF(v, 0, 1)).apply();
    }
    
    public float getStartYPercent() {
        return p.getFloat(K_SY, 0.70f);
    }
    public void setStartYPercent(float v) {
        p.edit().putFloat(K_SY, clampF(v, 0, 1)).apply();
    }
    
    public float getEndXPercent() {
        return p.getFloat(K_EX, 0.50f);
    }
    public void setEndXPercent(float v) {
        p.edit().putFloat(K_EX, clampF(v, 0, 1)).apply();
    }
    
    public float getEndYPercent() {
        return p.getFloat(K_EY, 0.30f);
    }
    public void setEndYPercent(float v) {
        p.edit().putFloat(K_EY, clampF(v, 0, 1)).apply();
    }
    
    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
    
    private float clampF(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
