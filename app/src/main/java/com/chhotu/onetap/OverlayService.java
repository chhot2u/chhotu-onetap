package com.chhotu.onetap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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
    
    private static final String CHANNEL_ID = "chhotu_overlay_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long LONG_PRESS_DURATION = 500; // ms
    
    // Static flag to track if service is running (survives app restart)
    private static boolean isRunning = false;
    
    private WindowManager windowManager;
    private View overlayView;
    private SettingsManager settingsManager;
    private Handler handler;
    
    private boolean isLongPressTriggered = false;
    private Runnable longPressRunnable;
    
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
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        settingsManager = new SettingsManager(this);
        handler = new Handler(Looper.getMainLooper());
        
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        showOverlay();
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
        
        // Clean up handler callbacks to prevent memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            longPressRunnable = null;
        }
        
        removeOverlay();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows floating overlay icon");
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
        
        return builder
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_description))
            .setSmallIcon(R.drawable.ic_overlay)
            .build();
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
        
        // Create overlay icon with circular background
        ImageView iconView = new ImageView(this);
        iconView.setImageResource(R.drawable.ic_overlay);
        iconView.setBackgroundResource(R.drawable.bg_overlay_circle);
        iconView.setAlpha(0.9f);
        
        // Add padding so icon sits inside the circle
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        iconView.setPadding(padding, padding, padding, padding);
        
        overlayView = iconView;
        
        // Set up touch listener for tap and long-press
        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY;
            private long touchStartTime;
            private boolean isMoving = false;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchStartTime = System.currentTimeMillis();
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        isMoving = false;
                        isLongPressTriggered = false;
                        
                        // Start long press timer
                        if (settingsManager.isLongPressMode()) {
                            longPressRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (!isMoving) {
                                        isLongPressTriggered = true;
                                        triggerDragUp();
                                    }
                                }
                            };
                            handler.postDelayed(longPressRunnable, LONG_PRESS_DURATION);
                        }
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = Math.abs(event.getRawX() - initialX);
                        float deltaY = Math.abs(event.getRawY() - initialY);
                        if (deltaX > 10 || deltaY > 10) {
                            isMoving = true;
                            if (longPressRunnable != null) {
                                handler.removeCallbacks(longPressRunnable);
                                longPressRunnable = null;
                            }
                        }
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        if (longPressRunnable != null) {
                            handler.removeCallbacks(longPressRunnable);
                            longPressRunnable = null;
                        }
                        
                        long duration = System.currentTimeMillis() - touchStartTime;
                        
                        // Handle tap mode (short tap, not moving)
                        if (!isMoving && duration < 300 && settingsManager.isTapMode()) {
                            triggerDragUp();
                        }
                        
                        return true;
                }
                return false;
            }
        });
        
        windowManager.addView(overlayView, params);
    }
    
    private void removeOverlay() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (IllegalArgumentException e) {
                // View not attached or already removed
            }
            overlayView = null;
        }
    }
    
    /**
     * Triggers the drag gesture via DragService singleton.
     * Uses the singleton pattern because AccessibilityService cannot be started via startService().
     */
    private void triggerDragUp() {
        DragService dragService = DragService.getInstance();
        if (dragService != null) {
            dragService.performDrag(settingsManager);
        } else {
            Toast.makeText(this, 
                R.string.accessibility_permission_required, 
                Toast.LENGTH_SHORT).show();
        }
    }
}
