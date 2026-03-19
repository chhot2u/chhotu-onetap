package com.chhotu.onetap;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

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
        // Clamp coordinates to screen
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        x1 = clamp(x1, 1, dm.widthPixels - 1);
        y1 = clamp(y1, 1, dm.heightPixels - 1);
        x2 = clamp(x2, 1, dm.widthPixels - 1);
        y2 = clamp(y2, 1, dm.heightPixels - 1);
        
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
     * Execute multiple rapid taps at a location.
     */
    public boolean rapidTap(int x, int y, int count, int intervalMs) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        x = clamp(x, 1, dm.widthPixels - 1);
        y = clamp(y, 1, dm.heightPixels - 1);
        
        boolean success = true;
        for (int i = 0; i < count; i++) {
            if (!tap(x, y)) success = false;
            if (i < count - 1) {
                try { Thread.sleep(intervalMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
        return success;
    }
    
    /**
     * Execute a quick swipe based on direction.
     */
    public boolean quickSwipe(int direction, int speed, float distancePercent) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w = dm.widthPixels;
        int h = dm.heightPixels;
        
        int cx = w / 2;
        int cy = h / 2;
        
        int endX = cx, endY = cy;
        int distance = (int)(Math.min(w, h) * distancePercent);
        
        switch (direction) {
            case 0: // Up
                endY = cy - distance;
                break;
            case 1: // Down
                endY = cy + distance;
                break;
            case 2: // Left
                endX = cx - distance;
                break;
            case 3: // Right
                endX = cx + distance;
                break;
        }
        
        return swipe(cx, cy, endX, endY, speed);
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
     * Execute auto-fire: continuous rapid taps.
     */
    public void autoFire(int x, int y, int intervalMs) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        x = clamp(x, 1, dm.widthPixels - 1);
        y = clamp(y, 1, dm.heightPixels - 1);
        
        String cmd = String.format("input tap %d %d", x, y);
        exec(cmd);
    }
    
    private boolean exec(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            int r = p.waitFor();
            return r == 0;
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Exec failed: " + e.getMessage());
            return false;
        }
    }
    
    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
