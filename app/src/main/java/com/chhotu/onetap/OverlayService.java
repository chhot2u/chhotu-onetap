package com.chhotu.onetap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Advanced gaming overlay with multiple features:
 * - Quick Swipe
 * - Auto Fire
 * - Rapid Tap
 * - Combo Move
 * - Drag Mode
 * - Position Save
 */
public class OverlayService extends Service {
    
    private static final String TAG = "OverlayService";
    private static final String CHANNEL_ID = "gamepad_channel";
    private static final int NOTIF_ID = 2001;
    
    private static boolean running = false;
    
    private WindowManager windowManager;
    private FrameLayout mainPanel;
    private LinearLayout buttonContainer;
    private GameSettings settings;
    private GestureHelper gesture;
    private Handler handler;
    
    // Auto fire state
    private boolean autoFireActive = false;
    private Runnable autoFireRunnable;
    
    // Rapid tap state
    private boolean rapidTapActive = false;
    private Runnable rapidTapRunnable;
    
    public static boolean isRunning() { return running; }
    
    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        settings = new GameSettings(this);
        gesture = new GestureHelper(this);
        handler = new Handler(Looper.getMainLooper());
        
        createNotification();
        createPanel();
        
        Log.d(TAG, "Service created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) { return null; }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        stopAutoFire();
        stopRapidTap();
        removePanel();
        Log.d(TAG, "Service destroyed");
    }
    
    private void createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "GamePad Pro", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Gaming overlay");
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
        
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        
        Notification n = builder
            .setContentTitle("GamePad Pro Active")
            .setContentText("Tap the floating panel for actions")
            .setSmallIcon(R.drawable.ic_overlay)
            .setContentIntent(pi)
            .setOngoing(true)
            .build();
        
        startForeground(NOTIF_ID, n);
    }
    
    private void createPanel() {
        int overlayType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;
        
        // Get saved position
        int savedX = settings.getPanelX();
        int savedY = settings.getPanelY();
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = savedX;
        params.y = savedY;
        
        // Main container
        mainPanel = new FrameLayout(this);
        
        // Background
        mainPanel.setBackgroundColor(Color.parseColor("#DD1A1A2E"));
        
        // Button container
        buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setGravity(Gravity.CENTER);
        
        // Size based on settings
        float scale = getScale();
        int padding = (int)(12 * scale);
        buttonContainer.setPadding(padding, padding, padding, padding);
        
        // Create feature buttons
        createFeatureButtons();
        
        mainPanel.addView(buttonContainer);
        
        // Enable drag
        enableDrag(mainPanel, params);
        
        windowManager.addView(mainPanel, params);
        
        Log.d(TAG, "Panel created");
    }
    
    private void createFeatureButtons() {
        float scale = getScale();
        int btnHeight = (int)(56 * scale);
        int marginBottom = (int)(8 * scale);
        int textSize = (int)(14 * scale);
        
        // Quick Swipe button
        TextView swipeBtn = createButton("⚡ Quick Swipe", "#6C5CE7", textSize, btnHeight, marginBottom);
        swipeBtn.setOnClickListener(v -> performQuickSwipe());
        buttonContainer.addView(swipeBtn);
        
        // Auto Fire button
        TextView fireBtn = createButton("🔥 Auto Fire", "#FF6B6B", textSize, btnHeight, marginBottom);
        fireBtn.setOnClickListener(v -> toggleAutoFire());
        buttonContainer.addView(fireBtn);
        
        // Rapid Tap button
        TextView tapBtn = createButton("👆 Rapid Tap", "#51CF66", textSize, btnHeight, marginBottom);
        tapBtn.setOnClickListener(v -> toggleRapidTap());
        buttonContainer.addView(tapBtn);
        
        // Combo Move button
        TextView comboBtn = createButton("🎯 Combo Move", "#00CEFF", textSize, btnHeight, marginBottom);
        comboBtn.setOnClickListener(v -> performCombo());
        buttonContainer.addView(comboBtn);
        
        // Save Position button
        TextView saveBtn = createButton("📍 Save Pos", "#FFA502", textSize, btnHeight, marginBottom);
        saveBtn.setOnClickListener(v -> saveCurrentPosition());
        buttonContainer.addView(saveBtn);
    }
    
    private TextView createButton(String text, String color, int textSize, int height, int marginBottom) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(textSize);
        btn.setTextColor(Color.WHITE);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        
        // Background
        btn.setBackgroundColor(Color.parseColor(color));
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            height
        );
        lp.setMargins(0, 0, 0, marginBottom);
        btn.setLayoutParams(lp);
        
        return btn;
    }
    
    private float getScale() {
        switch (settings.getPanelSize()) {
            case 0: return 0.7f; // Small
            case 2: return 1.3f; // Large
            default: return 1.0f; // Normal
        }
    }
    
    // ==================== GESTURE ACTIONS ====================
    
    private void performQuickSwipe() {
        int dir = settings.getSwipeDirection();
        int speed = settings.getSwipeSpeed();
        float dist = settings.getSwipeDistancePercent();
        
        boolean ok = gesture.quickSwipe(dir, speed, dist);
        toast(ok ? "Swipe executed!" : "Swipe failed");
    }
    
    private void toggleAutoFire() {
        if (autoFireActive) {
            stopAutoFire();
            toast("Auto Fire OFF");
        } else {
            startAutoFire();
            toast("Auto Fire ON");
        }
    }
    
    private void startAutoFire() {
        autoFireActive = true;
        int speed = settings.getAutoFireSpeed();
        
        autoFireRunnable = new Runnable() {
            @Override
            public void run() {
                if (autoFireActive) {
                    // Tap center of screen
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    gesture.autoFire(dm.widthPixels / 2, dm.heightPixels / 2, speed);
                    handler.postDelayed(this, speed);
                }
            }
        };
        handler.post(autoFireRunnable);
    }
    
    private void stopAutoFire() {
        autoFireActive = false;
        if (autoFireRunnable != null) {
            handler.removeCallbacks(autoFireRunnable);
            autoFireRunnable = null;
        }
    }
    
    private void toggleRapidTap() {
        if (rapidTapActive) {
            stopRapidTap();
            toast("Rapid Tap OFF");
        } else {
            startRapidTap();
            toast("Rapid Tap ON");
        }
    }
    
    private void startRapidTap() {
        rapidTapActive = true;
        int interval = settings.getRapidTapInterval();
        
        rapidTapRunnable = new Runnable() {
            @Override
            public void run() {
                if (rapidTapActive) {
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    gesture.autoFire(dm.widthPixels / 2, dm.heightPixels / 2, interval);
                    handler.postDelayed(this, interval);
                }
            }
        };
        handler.post(rapidTapRunnable);
    }
    
    private void stopRapidTap() {
        rapidTapActive = false;
        if (rapidTapRunnable != null) {
            handler.removeCallbacks(rapidTapRunnable);
            rapidTapRunnable = null;
        }
    }
    
    private void performCombo() {
        new Thread(() -> {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int cx = dm.widthPixels / 2;
            int cy = dm.heightPixels / 2;
            
            // Tap then swipe
            gesture.tap(cx, cy);
            try { Thread.sleep(settings.getComboDelay()); } catch (InterruptedException e) {}
            gesture.quickSwipe(settings.getSwipeDirection(), 
                              settings.getSwipeSpeed(), 
                              settings.getSwipeDistancePercent());
        }).start();
        toast("Combo executed!");
    }
    
    private void saveCurrentPosition() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        settings.savePosition(dm.widthPixels / 2, dm.heightPixels / 2);
        toast("Position saved!");
    }
    
    // ==================== DRAG SUPPORT ====================
    
    private void enableDrag(View view, WindowManager.LayoutParams params) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            private long touchTime;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        touchTime = System.currentTimeMillis();
                        return true;
                    case MotionEvent.ACTION_UP:
                        // Save new position
                        settings.setPanelPosition(params.x, params.y);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = (int)(event.getRawX() - startX) + params.x;
                        params.y = (int)(event.getRawY() - startY) + params.y;
                        startX = event.getRawX();
                        startY = event.getRawY();
                        try {
                            windowManager.updateViewLayout(mainPanel, params);
                        } catch (Exception e) {}
                        return true;
                }
                return false;
            }
        });
    }
    
    private void removePanel() {
        if (mainPanel != null && windowManager != null) {
            try { windowManager.removeView(mainPanel); } catch (Exception e) {}
            mainPanel = null;
        }
    }
    
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
