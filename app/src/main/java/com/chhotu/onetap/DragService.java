package com.chhotu.onetap;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * AccessibilityService that performs auto drag-up gesture.
 * Receives speed parameter and performs gesture accordingly.
 */
public class DragService extends AccessibilityService {
    
    private static final String TAG = "DragService";
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not used - we trigger gestures programmatically
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int speed = intent.getIntExtra("speed", 500);
            performDragUp(speed);
        }
        return START_NOT_STICKY;
    }
    
    /**
     * Performs a drag-up gesture from bottom to top of screen.
     * 
     * @param durationMs Duration of the gesture in milliseconds
     */
    private void performDragUp(int durationMs) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.w(TAG, "Gesture dispatch requires Android N+");
            return;
        }
        
        // Get screen dimensions
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        
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
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "DragService destroyed");
    }
}
