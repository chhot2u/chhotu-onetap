package com.chhotu.onetap;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Advanced game settings with profiles support.
 */
public class GameSettings {
    
    private static final String PREFS = "gamepad_prefs";
    
    private final SharedPreferences p;
    
    public GameSettings(Context ctx) {
        p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
    
    // Panel settings (0=small, 1=normal, 2=large)
    public int getPanelSize() { return clamp(p.getInt("panel_size", 1), 0, 2); }
    public void setPanelSize(int v) { p.edit().putInt("panel_size", clamp(v, 0, 2)).apply(); }
    
    public int getOpacity() { return clamp(p.getInt("opacity", 80), 10, 100); }
    public void setOpacity(int v) { p.edit().putInt("opacity", clamp(v, 10, 100)).apply(); }
    
    // Swipe settings (0=up, 1=down, 2=left, 3=right)
    public int getSwipeDirection() { return clamp(p.getInt("swipe_dir", 0), 0, 3); }
    public void setSwipeDirection(int v) { p.edit().putInt("swipe_dir", clamp(v, 0, 3)).apply(); }
    
    public int getSwipeSpeed() { return clamp(p.getInt("swipe_speed", 300), 100, 1000); }
    public void setSwipeSpeed(int v) { p.edit().putInt("swipe_speed", clamp(v, 100, 1000)).apply(); }
    
    public int getSwipeDistance() { return clamp(p.getInt("swipe_dist", 3), 0, 3); }
    public void setSwipeDistance(int v) { p.edit().putInt("swipe_dist", clamp(v, 0, 3)).apply(); }
    
    public float getSwipeDistancePercent() {
        switch (getSwipeDistance()) {
            case 0: return 0.20f;
            case 1: return 0.40f;
            case 2: return 0.60f;
            default: return 0.80f;
        }
    }
    
    // Auto fire (50-500ms)
    public int getAutoFireSpeed() { return clamp(p.getInt("auto_fire", 100), 50, 500); }
    public void setAutoFireSpeed(int v) { p.edit().putInt("auto_fire", clamp(v, 50, 500)).apply(); }
    
    // Rapid tap (20-200ms)
    public int getRapidTapInterval() { return clamp(p.getInt("rapid_tap", 50), 20, 200); }
    public void setRapidTapInterval(int v) { p.edit().putInt("rapid_tap", clamp(v, 20, 200)).apply(); }
    
    // Combo (100-1000ms)
    public int getComboDelay() { return clamp(p.getInt("combo_delay", 200), 100, 1000); }
    public void setComboDelay(int v) { p.edit().putInt("combo_delay", clamp(v, 100, 1000)).apply(); }
    
    // Save position for gestures
    public int getSavedX() { return p.getInt("saved_x", 0); }
    public int getSavedY() { return p.getInt("saved_y", 0); }
    public void savePosition(int x, int y) {
        p.edit().putInt("saved_x", x).putInt("saved_y", y).apply();
    }
    
    // Panel position (stored from overlay drag)
    public int getPanelX() { return p.getInt("panel_x", 0); }
    public int getPanelY() { return p.getInt("panel_y", 100); }
    public void setPanelPosition(int x, int y) {
        p.edit().putInt("panel_x", x).putInt("panel_y", y).apply();
    }
    
    public void reset() {
        p.edit().clear().apply();
    }
    
    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
