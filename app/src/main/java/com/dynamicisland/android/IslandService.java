package com.dynamicisland.android;

import android.app.*;import android.content.*;import android.graphics.*;import android.graphics.drawable.GradientDrawable;import android.os.*;import android.provider.Settings;import android.view.*;import android.widget.*;import java.util.*;

public class IslandService extends Service {
 WindowManager wm; LinearLayout pill; TextView label; Handler h=new Handler(Looper.getMainLooper()); boolean expanded=false;
 int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable shape(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
 public void onCreate(){super.onCreate(); startForeground(7,notification()); show();}
 Notification notification(){String ch="island";NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(ch,"Dynamic Island",NotificationManager.IMPORTANCE_LOW));return new Notification.Builder(this,ch).setContentTitle("Dynamic Island פעיל").setContentText("האי הדינמי פועל ברקע").setSmallIcon(android.R.drawable.ic_dialog_info).build();}
 void show(){if(!Settings.canDrawOverlays(this))return;wm=(WindowManager)getSystemService(WINDOW_SERVICE);pill=new LinearLayout(this);pill.setOrientation(LinearLayout.HORIZONTAL);pill.setGravity(Gravity.CENTER);pill.setPadding(dp(16),0,dp(16),0);pill.setBackground(shape(Color.BLACK,50));label=new TextView(this);label.setText("●   Dynamic Island   ●");label.setTextColor(Color.WHITE);label.setTextSize(14);pill.addView(label);pill.setOnClickListener(v->toggle());
  WindowManager.LayoutParams p=new WindowManager.LayoutParams(dp(210),dp(42),Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;p.y=dp(8);wm.addView(pill,p);
 }
 void toggle(){expanded=!expanded; WindowManager.LayoutParams p=(WindowManager.LayoutParams)pill.getLayoutParams();p.width=expanded?dp(330):dp(210);p.height=expanded?dp(110):dp(42);label.setText(expanded?"🎵  Dynamic Island\n\n🔋 100%   •   Wi‑Fi   •   פעיל":"●   Dynamic Island   ●");pill.setBackground(shape(expanded?Color.rgb(22,18,32):Color.BLACK,expanded?28:50));wm.updateViewLayout(pill,p);}
 public int onStartCommand(Intent i,int f,int id){if(i!=null&&i.getBooleanExtra("demo",false)&&pill!=null){label.setText("✨  Animation!  ✨");h.postDelayed(()->{if(pill!=null)label.setText("●   Dynamic Island   ●");},1800);}return START_STICKY;}
 public void onDestroy(){if(pill!=null)wm.removeView(pill);super.onDestroy();}
 public android.os.IBinder onBind(Intent i){return null;}
}
