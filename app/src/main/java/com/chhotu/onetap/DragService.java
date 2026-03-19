package com.chhotu.onetap;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * AccessibilityService that performs auto drag gestures.
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
     * Performs a drag gesture based on current settings.
     * 
     * @param settings The settings manager containing drag configuration
     */
    public void performDrag(SettingsManager settings) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.w(TAG, "Gesture dispatch requires Android N+");
            return;
        }
        
        // Get screen dimensions
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        
        if (screenWidth <= 0 || screenHeight <= 0) {
            Log.e(TAG, "Invalid screen dimensions: " + screenWidth + "x" + screenHeight);
            return;
        }
        
        // Get settings
        int durationMs = settings.getDragSpeed();
        int direction = settings.getDragDirection();
        float distancePercent = settings.getDragDistancePercent();
        
        // Calculate start and end positions based on direction and settings
        float startX, startY, endX, endY;
        
        // Get custom positions from settings
        float customStartX = settings.getStartXPercent();
        float customStartY = settings.getStartYPercent();
        float customEndX = settings.getEndXPercent();
        float customEndY = settings.getEndYPercent();
        
        switch (direction) {
            case SettingsManager.DRAG_UP:
                startX = screenWidth * customStartX;
                startY = screenHeight * customStartY;
                endX = screenWidth * customEndX;
                endY = screenHeight * (customStartY - distancePercent);
                break;
                
            case SettingsManager.DRAG_DOWN:
                startX = screenWidth * customStartX;
                startY = screenHeight * customStartY;
                endX = screenWidth * customEndX;
                endY = screenHeight * (customStartY + distancePercent);
                break;
                
            case SettingsManager.DRAG_LEFT:
                startX = screenWidth * customStartX;
                startY = screenHeight * customStartY;
                endX = screenWidth * (customStartX - distancePercent);
                endY = screenHeight * customEndY;
                break;
                
            case SettingsManager.DRAG_RIGHT:
                startX = screenWidth * customStartX;
                startY = screenHeight * customStartY;
                endX = screenWidth * (customStartX + distancePercent);
                endY = screenHeight * customEndY;
                break;
                
            default:
                // Default to up
                startX = screenWidth * 0.5f;
                startY = screenHeight * 0.8f;
                endX = screenWidth * 0.5f;
                endY = screenHeight * 0.2f;
        }
        
        // Clamp positions to screen bounds
        startX = clamp(startX, 0, screenWidth - 1);
        startY = clamp(startY, 0, screenHeight - 1);
        endX = clamp(endX, 0, screenWidth - 1);
        endY = clamp(endY, 0, screenHeight - 1);
        
        // Clamp duration
        durationMs = Math.max(SettingsManager.MIN_DRAG_SPEED, 
                     Math.min(SettingsManager.MAX_DRAG_SPEED, durationMs));
        
        Log.d(TAG, String.format("Performing drag: (%.0f,%.0f) -> (%.0f,%.0f) in %dms", 
            startX, startY, endX, endY, durationMs));
        
        // Create path for the gesture
        Path path = new Path();
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
                Log.d(TAG, "Gesture completed successfully");
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.w(TAG, "Gesture was cancelled");
            }
        }, null);
        
        if (!dispatched) {
            Log.e(TAG, "Failed to dispatch gesture");
        }
    }
    
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
