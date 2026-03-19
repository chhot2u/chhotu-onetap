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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Simple overlay service that performs gestures using shell commands.
 * No accessibility service needed!
 */
public class OverlayService extends Service {
    
    private static final String TAG = "OverlayService";
    private static final String CHANNEL_ID = "chhotu_overlay_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private static boolean isRunning = false;
    
    private WindowManager windowManager;
    private View overlayView;
    private GestureHelper gestureHelper;
    private Handler handler;
    
    private float touchStartX, touchStartY;
    private long touchStartTime;
    
    public static boolean isRunning() {
        return isRunning;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Log.d(TAG, "Service created");
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        gestureHelper = new GestureHelper(this);
        handler = new Handler(Looper.getMainLooper());
        
        createNotification();
        createOverlay();
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
        removeOverlay();
        Log.d(TAG, "Service destroyed");
    }
    
    private void createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Chhotu OneTap",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Overlay service");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
        
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );
        
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        
        Notification notification = builder
            .setContentTitle("Chhotu OneTap Active")
            .setContentText("Tap the icon to perform gesture")
            .setSmallIcon(R.drawable.ic_overlay)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
        
        startForeground(NOTIFICATION_ID, notification);
    }
    
    private void createOverlay() {
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
        
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.x = 0;
        params.y = 100;
        
        // Create a circular button
        FrameLayout container = new FrameLayout(this);
        container.setBackgroundResource(R.drawable.bg_overlay_circle);
        
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_overlay);
        int padding = (int) (12 * getResources().getDisplayMetrics().density);
        icon.setPadding(padding, padding, padding, padding);
        
        container.addView(icon, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ));
        
        overlayView = container;
        
        // Touch listener
        overlayView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchStartX = event.getRawX();
                        touchStartY = event.getRawY();
                        touchStartTime = System.currentTimeMillis();
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        long duration = System.currentTimeMillis() - touchStartTime;
                        
                        // Only trigger on quick tap (not drag)
                        if (duration < 300) {
                            Log.d(TAG, "Tap detected, performing gesture");
                            performGesture();
                        }
                        return true;
                }
                return false;
            }
        });
        
        windowManager.addView(overlayView, params);
        Log.d(TAG, "Overlay created");
        
        Toast.makeText(this, "Tap the button to perform gesture!", Toast.LENGTH_SHORT).show();
    }
    
    private void removeOverlay() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) {
                Log.w(TAG, "Error removing overlay: " + e.getMessage());
            }
            overlayView = null;
        }
    }
    
    private void performGesture() {
        SettingsManager settings = new SettingsManager(this);
        
        // Get screen dimensions
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        // Calculate start position from percentages
        int startX = (int) (screenWidth * settings.getStartXPercent());
        int startY = (int) (screenHeight * settings.getStartYPercent());
        
        // Calculate distance based on direction
        int direction = settings.getDragDirection();
        int speed = settings.getDragSpeed();
        float distancePercent = settings.getDragDistancePercent();
        int distance = (int) (screenHeight * distancePercent);
        
        Log.d(TAG, "Performing gesture: direction=" + direction + 
              ", start=(" + startX + "," + startY + ")" +
              ", distance=" + distance + ", speed=" + speed);
        
        // Perform the gesture
        boolean success = gestureHelper.performSwipe(
            direction, startX, startY, distance, speed
        );
        
        if (!success) {
            // Try alternate method with coordinates
            tryAlternateGesture(settings, screenWidth, screenHeight);
        }
    }
    
    private void tryAlternateGesture(SettingsManager settings, int screenWidth, int screenHeight) {
        // Try a simple center swipe as fallback
        int centerX = screenWidth / 2;
        int startY = (int) (screenHeight * 0.8);
        int endY = (int) (screenHeight * 0.2);
        
        Log.d(TAG, "Trying alternate gesture: center swipe");
        
        try {
            String command = String.format("input swipe %d %d %d %d %d",
                centerX, startY, centerX, endY, settings.getDragSpeed());
            
            Process process = Runtime.getRuntime().exec(command);
            int result = process.waitFor();
            
            if (result == 0) {
                Toast.makeText(this, "Gesture performed!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Gesture failed - app may need root or ADB", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Alternate gesture failed: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
