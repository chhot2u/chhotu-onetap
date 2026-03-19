package com.chhotu.onetap;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.IOException;

/**
 * Helper to execute gestures using shell commands.
 */
public class GestureHelper {
    
    private static final String TAG = "GestureHelper";
    private final Context context;
    
    public GestureHelper(Context ctx) {
        context = ctx;
    }
    
    /**
     * Execute swipe gesture.
     */
    public boolean swipe(int x1, int y1, int x2, int y2, int durationMs) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        x1 = clamp(x1, 1, dm.widthPixels - 1);
        y1 = clamp(y1, 1, dm.heightPixels - 1);
        x2 = clamp(x2, 1, dm.widthPixels - 1);
        y2 = clamp(y2, 1, dm.heightPixels - 1);
        durationMs = Math.max(50, durationMs);
        
        String cmd = String.format("input swipe %d %d %d %d %d", x1, y1, x2, y2, durationMs);
        Log.d(TAG, cmd);
        return exec(cmd);
    }
    
    /**
     * Execute single tap.
     */
    public boolean tap(int x, int y) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        x = clamp(x, 1, dm.widthPixels - 1);
        y = clamp(y, 1, dm.heightPixels - 1);
        
        String cmd = String.format("input tap %d %d", x, y);
        Log.d(TAG, cmd);
        return exec(cmd);
    }
    
    /**
     * Execute a quick swipe based on direction from saved position.
     */
    public boolean quickSwipe(int direction, int speed, float distancePercent) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w = dm.widthPixels;
        int h = dm.heightPixels;
        
        // Use saved position if available, otherwise center
        GameSettings settings = new GameSettings(context);
        int startX = settings.getSavedX();
        int startY = settings.getSavedY();
        if (startX <= 0 || startY <= 0) {
            startX = w / 2;
            startY = h / 2;
        }
        
        int endX = startX;
        int endY = startY;
        int distance = (int)(Math.min(w, h) * distancePercent);
        
        switch (direction) {
            case 0: endY = startY - distance; break; // Up
            case 1: endY = startY + distance; break; // Down
            case 2: endX = startX - distance; break; // Left
            case 3: endX = startX + distance; break; // Right
        }
        
        return swipe(startX, startY, endX, endY, speed);
    }
    
    /**
     * Execute a combo: tap + swipe.
     */
    public boolean combo(int tapX, int tapY, int swipeDir, int delay, int swipeSpeed, float distance) {
        tap(tapX, tapY);
        try { Thread.sleep(delay); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return quickSwipe(swipeDir, swipeSpeed, distance);
    }
    
    /**
     * Execute a single auto-fire tap at position.
     */
    public boolean autoFireTap(int x, int y) {
        return tap(x, y);
    }
    
    private boolean exec(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            int r = p.waitFor();
            if (r != 0) {
                Log.e(TAG, "Command failed with exit code " + r + ": " + cmd);
            }
            return r == 0;
        } catch (IOException e) {
            Log.e(TAG, "IO error: " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
