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
import android.graphics.drawable.GradientDrawable;
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
 * - Auto Fire (continuous)
 * - Rapid Tap (continuous)
 * - Combo Move
 * - Save Position
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
    private TextView fireBtn;
    
    // Rapid tap state
    private boolean rapidTapActive = false;
    private Runnable rapidTapRunnable;
    private TextView tapBtn;
    
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
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(ch);
            }
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
        
        // Main container with rounded corners
        mainPanel = new FrameLayout(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#EE1A1A2E"));
        bg.setCornerRadius(24);
        mainPanel.setBackground(bg);
        
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
        int cornerRadius = (int)(12 * scale);
        
        // Quick Swipe button
        TextView swipeBtn = createButton("⚡ Quick Swipe", "#6C5CE7", textSize, btnHeight, marginBottom, cornerRadius);
        swipeBtn.setOnClickListener(v -> performQuickSwipe());
        buttonContainer.addView(swipeBtn);
        
        // Auto Fire button
        fireBtn = createButton("🔥 Auto Fire", "#FF6B6B", textSize, btnHeight, marginBottom, cornerRadius);
        fireBtn.setOnClickListener(v -> toggleAutoFire());
        buttonContainer.addView(fireBtn);
        
        // Rapid Tap button
        tapBtn = createButton("👆 Rapid Tap", "#51CF66", textSize, btnHeight, marginBottom, cornerRadius);
        tapBtn.setOnClickListener(v -> toggleRapidTap());
        buttonContainer.addView(tapBtn);
        
        // Combo Move button
        TextView comboBtn = createButton("🎯 Combo Move", "#00CEFF", textSize, btnHeight, marginBottom, cornerRadius);
        comboBtn.setOnClickListener(v -> performCombo());
        buttonContainer.addView(comboBtn);
        
        // Save Position button
        TextView saveBtn = createButton("📍 Save Pos", "#FFA502", textSize, btnHeight, marginBottom, cornerRadius);
        saveBtn.setOnClickListener(v -> saveCurrentPosition());
        buttonContainer.addView(saveBtn);
    }
    
    private TextView createButton(String text, String bgColor, int textSize, int height, int marginBottom, int cornerRadius) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(textSize);
        btn.setTextColor(Color.WHITE);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        
        // Rounded background
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(cornerRadius);
        btn.setBackground(bg);
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            (int)(180 * getScale()),
            height
        );
        lp.setMargins(0, 0, 0, marginBottom);
        btn.setLayoutParams(lp);
        
        return btn;
    }
    
    private void updateButtonColor(TextView btn, String activeColor, String normalColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(activeColor));
        bg.setCornerRadius((int)(12 * getScale()));
        btn.setBackground(bg);
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
        
        new Thread(() -> {
            boolean ok = gesture.quickSwipe(dir, speed, dist);
            runOnUiThread(() -> toast(ok ? "⚡ Swipe!" : "Swipe failed"));
        }).start();
    }
    
    private void toggleAutoFire() {
        if (autoFireActive) {
            stopAutoFire();
            toast("🔥 Auto Fire OFF");
        } else {
            startAutoFire();
            toast("🔥 Auto Fire ON");
        }
    }
    
    private void startAutoFire() {
        autoFireActive = true;
        int speed = settings.getAutoFireSpeed();
        
        // Update button to show active state
        updateButtonColor(fireBtn, "#FF4444", "#FF6B6B");
        
        autoFireRunnable = new Runnable() {
            @Override
            public void run() {
                if (autoFireActive) {
                    new Thread(() -> {
                        // Use saved position or center
                        GameSettings s = new GameSettings(OverlayService.this);
                        int x = s.getSavedX();
                        int y = s.getSavedY();
                        if (x <= 0 || y <= 0) {
                            DisplayMetrics dm = getResources().getDisplayMetrics();
                            x = dm.widthPixels / 2;
                            y = dm.heightPixels / 2;
                        }
                        gesture.autoFireTap(x, y);
                    }).start();
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
        // Reset button color
        if (fireBtn != null) {
            updateButtonColor(fireBtn, "#FF6B6B", "#FF6B6B");
        }
    }
    
    private void toggleRapidTap() {
        if (rapidTapActive) {
            stopRapidTap();
            toast("👆 Rapid Tap OFF");
        } else {
            startRapidTap();
            toast("👆 Rapid Tap ON");
        }
    }
    
    private void startRapidTap() {
        rapidTapActive = true;
        int interval = settings.getRapidTapInterval();
        
        // Update button to show active state
        updateButtonColor(tapBtn, "#33CC33", "#51CF66");
        
        rapidTapRunnable = new Runnable() {
            @Override
            public void run() {
                if (rapidTapActive) {
                    new Thread(() -> {
                        GameSettings s = new GameSettings(OverlayService.this);
                        int x = s.getSavedX();
                        int y = s.getSavedY();
                        if (x <= 0 || y <= 0) {
                            DisplayMetrics dm = getResources().getDisplayMetrics();
                            x = dm.widthPixels / 2;
                            y = dm.heightPixels / 2;
                        }
                        gesture.autoFireTap(x, y);
                    }).start();
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
        // Reset button color
        if (tapBtn != null) {
            updateButtonColor(tapBtn, "#51CF66", "#51CF66");
        }
    }
    
    private void performCombo() {
        new Thread(() -> {
            GameSettings s = new GameSettings(OverlayService.this);
            int x = s.getSavedX();
            int y = s.getSavedY();
            if (x <= 0 || y <= 0) {
                DisplayMetrics dm = getResources().getDisplayMetrics();
                x = dm.widthPixels / 2;
                y = dm.heightPixels / 2;
            }
            
            gesture.tap(x, y);
            try { Thread.sleep(settings.getComboDelay()); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            gesture.quickSwipe(settings.getSwipeDirection(), 
                              settings.getSwipeSpeed(), 
                              settings.getSwipeDistancePercent());
            
            runOnUiThread(() -> toast("🎯 Combo!"));
        }).start();
    }
    
    private void saveCurrentPosition() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        settings.savePosition(dm.widthPixels / 2, dm.heightPixels / 2);
        toast("📍 Position saved!");
    }
    
    // ==================== DRAG SUPPORT ====================
    
    private void enableDrag(View view, WindowManager.LayoutParams params) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            private int initialX, initialY;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        initialX = params.x;
                        initialY = params.y;
                        return true;
                    case MotionEvent.ACTION_UP:
                        settings.setPanelPosition(params.x, params.y);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int)(event.getRawX() - startX);
                        params.y = initialY + (int)(event.getRawY() - startY);
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
    
    private void runOnUiThread(Runnable r) {
        handler.post(r);
    }
}
