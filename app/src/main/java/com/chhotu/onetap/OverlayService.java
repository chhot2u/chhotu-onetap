package com.chhotu.onetap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Foreground service that displays a floating overlay icon.
 * Supports both tap and long-press trigger modes.
 */
public class OverlayService extends Service {
    
    private static final String TAG = "OverlayService";
    private static final String CHANNEL_ID = "chhotu_overlay_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long LONG_PRESS_DURATION = 500; // ms
    private static final long TAP_TIMEOUT = 300; // ms
    
    // Static flag to track if service is running
    private static boolean isRunning = false;
    
    private WindowManager windowManager;
    private View overlayView;
    private SettingsManager settingsManager;
    private Handler handler;
    private long lastTapTime = 0;
    
    private Runnable longPressRunnable;
    private boolean isMoving = false;
    private float touchStartX, touchStartY;
    
    /**
     * Check if the overlay service is currently running.
     */
    public static boolean isRunning() {
        return isRunning;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        
        Log.d(TAG, "OverlayService created");
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        settingsManager = new SettingsManager(this);
        handler = new Handler(Looper.getMainLooper());
        
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        showOverlay();
        
        Toast.makeText(this, "Tap the icon to drag!", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            longPressRunnable = null;
        }
        
        removeOverlay();
        Log.d(TAG, "OverlayService destroyed");
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Chhotu OneTap",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("One-tap drag overlay service");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification buildNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        
        builder.setContentTitle(getString(R.string.app_name))
            .setContentText("Tap icon to perform drag gesture")
            .setSmallIcon(R.drawable.ic_overlay)
            .setOngoing(true);
        
        return builder.build();
    }
    
    private void showOverlay() {
        int overlayType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        params.x = 16;
        params.y = 0;
        
        // Create overlay icon
        ImageView iconView = new ImageView(this);
        iconView.setImageResource(R.drawable.ic_overlay);
        iconView.setBackgroundResource(R.drawable.bg_overlay_circle);
        iconView.setAlpha(0.9f);
        
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        iconView.setPadding(padding, padding, padding, padding);
        
        overlayView = iconView;
        
        // Touch listener for tap and long-press
        overlayView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = event.getRawX();
                    touchStartY = event.getRawY();
                    isMoving = false;
                    
                    if (settingsManager.isLongPressMode()) {
                        longPressRunnable = () -> {
                            Log.d(TAG, "Long press triggered");
                            performDrag();
                        };
                        handler.postDelayed(longPressRunnable, LONG_PRESS_DURATION);
                    }
                    return true;
                    
                case MotionEvent.ACTION_MOVE:
                    float deltaX = Math.abs(event.getRawX() - touchStartX);
                    float deltaY = Math.abs(event.getRawY() - touchStartY);
                    if (deltaX > 15 || deltaY > 15) {
                        isMoving = true;
                        cancelLongPress();
                    }
                    return true;
                    
                case MotionEvent.ACTION_UP:
                    cancelLongPress();
                    
                    long duration = System.currentTimeMillis() - (lastTapTime - TAP_TIMEOUT);
                    lastTapTime = System.currentTimeMillis();
                    
                    if (!isMoving && settingsManager.isTapMode()) {
                        // Check if it's a quick tap
                        if (duration < TAP_TIMEOUT * 2) {
                            Log.d(TAG, "Tap detected, performing drag");
                            performDrag();
                        }
                    }
                    return true;
            }
            return false;
        });
        
        windowManager.addView(overlayView, params);
        Log.d(TAG, "Overlay view added");
    }
    
    private void cancelLongPress() {
        if (longPressRunnable != null) {
            handler.removeCallbacks(longPressRunnable);
            longPressRunnable = null;
        }
    }
    
    private void removeOverlay() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "View not attached: " + e.getMessage());
            }
            overlayView = null;
        }
    }
    
    /**
     * Performs the drag gesture using DragService.
     */
    private void performDrag() {
        Log.d(TAG, "performDrag called - service ready: " + DragService.isRunning());
        
        if (!DragService.isRunning()) {
            Toast.makeText(this, 
                "Accessibility service not enabled! Please enable in Settings.", 
                Toast.LENGTH_LONG).show();
            return;
        }
        
        DragService.queueDragGesture(
            this,
            settingsManager.getDragDirection(),
            settingsManager.getDragSpeed(),
            settingsManager.getStartXPercent(),
            settingsManager.getStartYPercent(),
            settingsManager.getEndXPercent(),
            settingsManager.getEndYPercent()
        );
    }
}
