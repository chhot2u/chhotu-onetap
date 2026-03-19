package com.chhotu.onetap;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * AccessibilityService that performs auto drag-up gesture.
 * 
 * Uses a singleton pattern so OverlayService can trigger gestures
 * without calling startService() (which doesn't work for AccessibilityService).
 */
public class DragService extends AccessibilityService {
    
    private static final String TAG = "DragService";
    
    private static DragService instance;
    
    /**
     * Returns the running instance of DragService, or null if not enabled.
     */
    public static DragService getInstance() {
        return instance;
    }
    
    /**
     * Checks if the accessibility service is currently running.
     */
    public static boolean isRunning() {
        return instance != null;
    }
    
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "Accessibility service connected");
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not used - we trigger gestures programmatically
    }
    
    @Override
    public void onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "Accessibility service destroyed");
    }
    
    /**
     * Performs a drag-up gesture from bottom to top of screen.
     * 
     * @param durationMs Duration of the gesture in milliseconds
     */
    public void performDragUp(int durationMs) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.w(TAG, "Gesture dispatch requires Android N+");
            return;
        }
        
        // Clamp duration to valid range
        durationMs = Math.max(SettingsManager.MIN_DRAG_SPEED, 
                     Math.min(SettingsManager.MAX_DRAG_SPEED, durationMs));
        
        // Get screen dimensions
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        
        if (screenWidth <= 0 || screenHeight <= 0) {
            Log.e(TAG, "Invalid screen dimensions: " + screenWidth + "x" + screenHeight);
            return;
        }
        
        // Create path from bottom center to top center
        Path path = new Path();
        float startX = screenWidth / 2f;
        float startY = screenHeight * 0.8f; // Start 80% from top
        float endX = screenWidth / 2f;
        float endY = screenHeight * 0.2f; // End 20% from top
        
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        
        // Build gesture description
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(
            path, 
            0, 
            durationMs
        ));
        
        // Dispatch gesture
        boolean dispatched = dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "Drag-up gesture completed successfully");
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.w(TAG, "Drag-up gesture was cancelled");
            }
        }, null);
        
        if (!dispatched) {
            Log.e(TAG, "Failed to dispatch drag-up gesture");
        }
    }
}
