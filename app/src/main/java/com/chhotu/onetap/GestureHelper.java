package com.chhotu.onetap;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Helper class to perform gestures using shell commands.
 * This is the most reliable method on Android.
 */
public class GestureHelper {
    
    private static final String TAG = "GestureHelper";
    private static final String SWIPE_COMMAND = "input swipe %d %d %d %d %d";
    
    private final Context context;
    
    public GestureHelper(Context context) {
        this.context = context;
    }
    
    /**
     * Perform a swipe gesture from (x1, y1) to (x2, y2) with given duration in ms.
     */
    public boolean swipe(int x1, int y1, int x2, int y2, int durationMs) {
        String command = String.format(SWIPE_COMMAND, x1, y1, x2, y2, durationMs);
        Log.d(TAG, "Executing: " + command);
        
        boolean success = executeCommand(command);
        
        if (success) {
            Log.d(TAG, "Swipe completed successfully");
            showToast("Swipe completed!");
        } else {
            Log.e(TAG, "Swipe failed");
            showToast("Swipe failed - check permissions");
        }
        
        return success;
    }
    
    /**
     * Perform a swipe based on direction and parameters.
     */
    public boolean performSwipe(int direction, int startX, int startY, int distance, int durationMs) {
        int endX = startX;
        int endY = startY;
        
        switch (direction) {
            case SettingsManager.DRAG_UP:
                endY = startY - distance;
                break;
            case SettingsManager.DRAG_DOWN:
                endY = startY + distance;
                break;
            case SettingsManager.DRAG_LEFT:
                endX = startX - distance;
                break;
            case SettingsManager.DRAG_RIGHT:
                endX = startX + distance;
                break;
        }
        
        // Clamp to screen bounds
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        
        endX = clamp(endX, 0, screenWidth - 1);
        endY = clamp(endY, 0, screenHeight - 1);
        
        return swipe(startX, startY, endX, endY, durationMs);
    }
    
    /**
     * Execute shell command with root if needed.
     */
    private boolean executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            
            int result = process.waitFor();
            return result == 0;
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Command execution failed: " + e.getMessage());
            
            // Try without root
            return executeCommandWithoutRoot(command);
        }
    }
    
    private boolean executeCommandWithoutRoot(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            int result = process.waitFor();
            return result == 0;
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Non-root command failed: " + e.getMessage());
            return false;
        }
    }
    
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
