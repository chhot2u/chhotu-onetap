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
    
    // Panel settings
    public int getPanelSize() { return p.getInt("panel_size", 1); } // 0=small, 1=normal, 2=large
    public void setPanelSize(int v) { p.edit().putInt("panel_size", v).apply(); }
    
    public int getOpacity() { return p.getInt("opacity", 80); }
    public void setOpacity(int v) { p.edit().putInt("opacity", v).apply(); }
    
    // Swipe settings
    public int getSwipeDirection() { return p.getInt("swipe_dir", 0); } // 0=up, 1=down, 2=left, 3=right
    public void setSwipeDirection(int v) { p.edit().putInt("swipe_dir", v).apply(); }
    
    public int getSwipeSpeed() { return p.getInt("swipe_speed", 300); }
    public void setSwipeSpeed(int v) { p.edit().putInt("swipe_speed", v).apply(); }
    
    public int getSwipeDistance() { return p.getInt("swipe_dist", 3); } // 0=short, 1=med, 2=long, 3=full
    public void setSwipeDistance(int v) { p.edit().putInt("swipe_dist", v).apply(); }
    
    public float getSwipeDistancePercent() {
        switch (getSwipeDistance()) {
            case 0: return 0.20f;
            case 1: return 0.40f;
            case 2: return 0.60f;
            default: return 0.80f;
        }
    }
    
    // Auto fire
    public int getAutoFireSpeed() { return p.getInt("auto_fire", 100); }
    public void setAutoFireSpeed(int v) { p.edit().putInt("auto_fire", v).apply(); }
    
    // Rapid tap
    public int getRapidTapInterval() { return p.getInt("rapid_tap", 50); }
    public void setRapidTapInterval(int v) { p.edit().putInt("rapid_tap", v).apply(); }
    
    // Combo
    public int getComboDelay() { return p.getInt("combo_delay", 200); }
    public void setComboDelay(int v) { p.edit().putInt("combo_delay", v).apply(); }
    
    // Save position
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
    
    // Profile support
    public void saveProfile(String name) {
        SharedPreferences.Editor editor = p.edit();
        editor.putString("profile_" + name, getCurrentSettingsString());
        editor.putString("active_profile", name);
        editor.apply();
    }
    
    public String getActiveProfile() { return p.getString("active_profile", "Default"); }
    
    private String getCurrentSettingsString() {
        return getPanelSize() + "," + getOpacity() + "," + getSwipeDirection() + "," +
               getSwipeSpeed() + "," + getSwipeDistance() + "," + getAutoFireSpeed() + "," +
               getRapidTapInterval() + "," + getComboDelay();
    }
    
    public void reset() {
        p.edit().clear().apply();
    }
}
