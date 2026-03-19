package com.chhotu.onetap;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/**
 * AccessibilityService that performs auto drag gestures.
 * 
 * Uses a singleton pattern with a pending gesture queue to handle
 * timing issues when the service is first enabled.
 */
public class DragService extends AccessibilityService {
    
    private static final String TAG = "DragService";
    private static final long GESTURE_RETRY_DELAY = 500; // ms
    private static final int MAX_GESTURE_RETRIES = 3;
    
    private static DragService instance;
    private static Handler mainHandler;
    
    // Pending gesture data
    private static int pendingDirection = -1;
    private static int pendingSpeed = -1;
    private static float pendingStartX = -1;
    private static float pendingStartY = -1;
    private static float pendingEndX = -1;
    private static float pendingEndY = -1;
    private static int pendingRetries = 0;
    
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
    
    /**
     * Queue a drag gesture. If service is not ready, it will retry automatically.
     * Called from OverlayService.
     */
    public static void queueDragGesture(Context context, int direction, int speed,
            float startX, float startY, float endX, float endY) {
        
        pendingDirection = direction;
        pendingSpeed = speed;
        pendingStartX = startX;
        pendingStartY = startY;
        pendingEndX = endX;
        pendingEndY = endY;
        pendingRetries = 0;
        
        Log.d(TAG, "Queued drag gesture: direction=" + direction + ", speed=" + speed);
        
        if (instance != null) {
            // Service is ready, execute immediately
            instance.performDragInternal(direction, speed, startX, startY, endX, endY);
        } else {
            // Service not ready, try to retry
            if (mainHandler == null) {
                mainHandler = new Handler(Looper.getMainLooper());
            }
            retryGesture();
        }
    }
    
    private static void retryGesture() {
        if (pendingRetries >= MAX_GESTURE_RETRIES) {
            Log.e(TAG, "Max gesture retries reached, giving up");
            pendingDirection = -1;
            return;
        }
        
        if (instance != null && pendingDirection >= 0) {
            instance.performDragInternal(pendingDirection, pendingSpeed, 
                pendingStartX, pendingStartY, pendingEndX, pendingEndY);
            pendingDirection = -1; // Clear after success
        } else {
            pendingRetries++;
            Log.d(TAG, "Service not ready, retry " + pendingRetries + "/" + MAX_GESTURE_RETRIES);
            if (mainHandler != null) {
                mainHandler.postDelayed(() -> retryGesture(), GESTURE_RETRY_DELAY);
            }
        }
    }
    
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "Accessibility service connected");
        
        // Process any pending gesture
        if (pendingDirection >= 0) {
            Log.d(TAG, "Processing pending gesture after service connect");
            performDragInternal(pendingDirection, pendingSpeed, 
                pendingStartX, pendingStartY, pendingEndX, pendingEndY);
            pendingDirection = -1;
        }
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not used
    }
    
    @Override
    public void onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        pendingDirection = -1;
        Log.d(TAG, "Accessibility service destroyed");
    }
    
    /**
     * Performs a drag gesture.
     */
    private void performDragInternal(int direction, int speed, 
            float startXPercent, float startYPercent, 
            float endXPercent, float endYPercent) {
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.w(TAG, "Gesture dispatch requires Android N+");
            showToast("Gesture requires Android 7.0+");
            return;
        }
        
        // Get screen dimensions
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        
        if (screenWidth <= 0 || screenHeight <= 0) {
            Log.e(TAG, "Invalid screen dimensions: " + screenWidth + "x" + screenHeight);
            showToast("Error: Invalid screen size");
            return;
        }
        
        // Calculate pixel positions
        float startX, startY, endX, endY;
        
        switch (direction) {
            case SettingsManager.DRAG_UP:
                startX = screenWidth * startXPercent;
                startY = screenHeight * startYPercent;
                endX = screenWidth * endXPercent;
                endY = screenHeight * endYPercent;
                // Ensure endY is above startY
                if (endY > startY) {
                    endY = Math.max(0, startY - screenHeight * 0.5f);
                }
                break;
                
            case SettingsManager.DRAG_DOWN:
                startX = screenWidth * startXPercent;
                startY = screenHeight * startYPercent;
                endX = screenWidth * endXPercent;
                endY = screenHeight * endYPercent;
                // Ensure endY is below startY
                if (endY < startY) {
                    endY = Math.min(screenHeight - 1, startY + screenHeight * 0.5f);
                }
                break;
                
            case SettingsManager.DRAG_LEFT:
                startX = screenWidth * startXPercent;
                startY = screenHeight * startYPercent;
                endX = screenWidth * endXPercent;
                endY = screenHeight * endYPercent;
                // Ensure endX is to the left of startX
                if (endX > startX) {
                    endX = Math.max(0, startX - screenWidth * 0.5f);
                }
                break;
                
            case SettingsManager.DRAG_RIGHT:
                startX = screenWidth * startXPercent;
                startY = screenHeight * startYPercent;
                endX = screenWidth * endXPercent;
                endY = screenHeight * endYPercent;
                // Ensure endX is to the right of startX
                if (endX < startX) {
                    endX = Math.min(screenWidth - 1, startX + screenWidth * 0.5f);
                }
                break;
                
            default:
                // Default: drag up from bottom center
                startX = screenWidth * 0.5f;
                startY = screenHeight * 0.8f;
                endX = screenWidth * 0.5f;
                endY = screenHeight * 0.2f;
        }
        
        // Clamp positions to screen bounds
        int finalStartX = clamp((int) startX, 0, screenWidth - 1);
        int finalStartY = clamp((int) startY, 0, screenHeight - 1);
        int finalEndX = clamp((int) endX, 0, screenWidth - 1);
        int finalEndY = clamp((int) endY, 0, screenHeight - 1);
        
        // Clamp speed
        int finalSpeed = Math.max(SettingsManager.MIN_DRAG_SPEED, 
                         Math.min(SettingsManager.MAX_DRAG_SPEED, speed));
        
        Log.d(TAG, String.format("Drag: (%d,%d) -> (%d,%d) in %dms (screen: %dx%d)", 
            finalStartX, finalStartY, finalEndX, finalEndY, finalSpeed, screenWidth, screenHeight));
        
        // Create path for the gesture
        Path path = new Path();
        path.moveTo(finalStartX, finalStartY);
        path.lineTo(finalEndX, finalEndY);
        
        // Build gesture description
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(
            path, 
            0, 
            finalSpeed
        ));
        
        // Dispatch gesture
        GestureResultCallback callback = new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "Gesture completed");
                showToast("Drag complete!");
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.w(TAG, "Gesture cancelled");
                showToast("Gesture cancelled");
            }
        };
        
        boolean dispatched = dispatchGesture(builder.build(), callback, null);
        
        if (!dispatched) {
            Log.e(TAG, "Failed to dispatch gesture");
            showToast("Failed to dispatch gesture");
        }
    }
    
    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Toast failed: " + e.getMessage());
            }
        });
    }
    
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
