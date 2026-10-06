package com.dynamicisland.android;

import android.app.*;import android.content.*;import android.graphics.*;import android.graphics.drawable.GradientDrawable;import android.os.*;import android.provider.Settings;import android.view.*;import android.widget.*;

public class IslandService extends Service{
 WindowManager wm;LinearLayout pill;TextView label;Handler h=new Handler(Looper.getMainLooper());boolean expanded;
 int dp(float v){return(int)(v*getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable g(int c,float r){GradientDrawable x=new GradientDrawable();x.setColor(c);x.setCornerRadius(dp(r));return x;}
 public void onCreate(){super.onCreate();startForeground(7,notifyMe());show();}
 Notification notifyMe(){String c="island";NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);if(Build.VERSION.SDK_INT>=26)n.createNotificationChannel(new NotificationChannel(c,"Dynamic Island",NotificationManager.IMPORTANCE_LOW));return new Notification.Builder(this,c).setContentTitle("Dynamic Island פעיל").setContentText("האי הדינמי פועל").setSmallIcon(android.R.drawable.ic_dialog_info).build();}
 void show(){if(!Settings.canDrawOverlays(this))return;wm=(WindowManager)getSystemService(WINDOW_SERVICE);pill=new LinearLayout(this);pill.setOrientation(LinearLayout.VERTICAL);pill.setGravity(Gravity.CENTER);pill.setPadding(dp(14),dp(5),dp(14),dp(5));pill.setBackground(g(Color.BLACK,50));label=new TextView(this);label.setGravity(17);label.setText("●  ✦  Dynamic Island  ✦  ●");label.setTextColor(Color.WHITE);label.setTextSize(14);pill.addView(label,new LinearLayout.LayoutParams(-1,-1));pill.setOnClickListener(v->toggle());
 WindowManager.LayoutParams p=new WindowManager.LayoutParams(dp(215),dp(44),Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;p.y=dp(7);wm.addView(pill,p);}
 void toggle(){expanded=!expanded;WindowManager.LayoutParams p=(WindowManager.LayoutParams)pill.getLayoutParams();p.width=expanded?dp(340):dp(215);p.height=expanded?dp(125):dp(44);pill.setBackground(g(expanded?Color.rgb(31,20,45):Color.BLACK,expanded?30:50));label.setText(expanded?"🎵  Playing  •  🔋 100%\n\n⚡ Wi‑Fi   •   Bluetooth   •   פעיל":"●  ✦  Dynamic Island  ✦  ●");wm.updateViewLayout(pill,p);}
 public int onStartCommand(Intent i,int f,int id){if(i!=null&&i.getBooleanExtra("demo",false)&&pill!=null){label.setText("✨  🔵  אנימציה  🟣  ✨");pill.setBackground(g(Color.rgb(77,36,150),32));h.postDelayed(()->{if(pill!=null){pill.setBackground(g(Color.BLACK,50));label.setText("●  ✦  Dynamic Island  ✦  ●");}},1600);}return START_STICKY;}
 public void onDestroy(){if(pill!=null&&wm!=null)wm.removeView(pill);super.onDestroy();}
 public IBinder onBind(Intent i){return null;}
}