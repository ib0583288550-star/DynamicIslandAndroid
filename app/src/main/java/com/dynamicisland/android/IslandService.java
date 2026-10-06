package com.dynamicisland.android;

import android.app.*;
import android.animation.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

public class IslandService extends Service {
    WindowManager wm;
    LinearLayout pill;
    TextView icon,label,sub;
    Handler h=new Handler(Looper.getMainLooper());
    boolean expanded=false;
    public static IslandService current;
    long demoUntil=0;
    String mediaTitle="אין מדיה";
    boolean mediaPlaying=false;
    long timerEnd=0;
    String timerLabel="";
    int battery=100;
    boolean charging=false;

    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    GradientDrawable bg(int c,float r){
        GradientDrawable x=new GradientDrawable();
        x.setColor(c);
        x.setCornerRadius(dp(r));
        x.setStroke(dp(1),Color.argb(55,255,255,255));
        return x;
    }

    @Override public void onCreate(){
        super.onCreate();
        current=this;
        registerReceiver(batteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        startForeground(7,notifyMe());
        show();
    }

    final BroadcastReceiver batteryReceiver=new BroadcastReceiver(){
        @Override public void onReceive(Context c,Intent i){
            int level=i.getIntExtra("level",100);
            int scale=i.getIntExtra("scale",100);
            battery=scale>0?(level*100/scale):100;
            int status=i.getIntExtra("status",-1);
            charging=status==BatteryManager.BATTERY_STATUS_CHARGING ||
                     status==BatteryManager.BATTERY_STATUS_FULL;
            refresh();
        }
    };

    Notification notifyMe(){
        String c="island";
        NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26)
            n.createNotificationChannel(new NotificationChannel(c,"Dynamic Island",NotificationManager.IMPORTANCE_LOW));
        return new Notification.Builder(this,c)
            .setContentTitle("Dynamic Island פעיל")
            .setContentText("האי הדינמי פועל")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true).build();
    }

    void show(){
        if(!Settings.canDrawOverlays(this))return;
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);

        pill=new LinearLayout(this);
        pill.setOrientation(LinearLayout.HORIZONTAL);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        pill.setPadding(dp(12),dp(4),dp(12),dp(4));
        pill.setBackground(bg(Color.rgb(5,5,8),50));

        icon=new TextView(this);
        icon.setText("●");
        icon.setTextSize(12);
        icon.setTextColor(Color.rgb(170,90,255));
        icon.setGravity(Gravity.CENTER);

        label=new TextView(this);
        label.setTextColor(Color.WHITE);
        label.setTextSize(13);
        label.setTypeface(null,1);
        label.setGravity(Gravity.CENTER_VERTICAL);

        sub=new TextView(this);
        sub.setTextColor(Color.LTGRAY);
        sub.setTextSize(11);
        sub.setGravity(Gravity.CENTER_VERTICAL);

        pill.addView(icon,new LinearLayout.LayoutParams(dp(22),-1));
        pill.addView(label,new LinearLayout.LayoutParams(0,-1,1));
        pill.addView(sub,new LinearLayout.LayoutParams(dp(54),-1));
        pill.setOnClickListener(v->toggle());

        WindowManager.LayoutParams p=new WindowManager.LayoutParams(
            dp(218),dp(42),
            Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;
        p.y=dp(6);
        wm.addView(pill,p);
        refresh();
    }

    void updateMedia(String title, boolean playing){
        mediaTitle=title==null||title.isEmpty()?"מדיה":title;
        mediaPlaying=playing;
        if(pill!=null){
            icon.setText(playing?"▶":"Ⅱ");
            icon.setTextColor(Color.rgb(255,90,180));
            label.setText(mediaTitle);
            sub.setText(playing?"מנגן":"מושהה");
            h.postDelayed(()->refresh(),2600);
        }
    }

    void startTimer(long seconds,String title){
        timerEnd=System.currentTimeMillis()+seconds*1000L;
        timerLabel=title==null?"טיימר":title;
        h.post(timerTick);
    }

    final Runnable timerTick=new Runnable(){
        public void run(){
            if(pill==null)return;
            long left=Math.max(0,timerEnd-System.currentTimeMillis());
            if(left>0){
                long sec=(left+999)/1000;
                icon.setText("⏱");
                icon.setTextColor(Color.rgb(255,190,70));
                label.setText(timerLabel);
                sub.setText(String.format(java.util.Locale.US,"%02d:%02d",sec/60,sec%60));
                h.postDelayed(this,500);
            }else if(timerEnd>0){
                timerEnd=0;
                icon.setText("✓");
                label.setText("הטיימר הסתיים");
                sub.setText("עכשיו");
                h.postDelayed(()->refresh(),2200);
            }
        }
    };

    void refresh(){
        if(pill==null)return;
        icon.setText(charging?"⚡":"●");
        icon.setTextColor(charging?Color.rgb(80,255,180):Color.rgb(170,90,255));
        label.setText(charging?"טוען":"Dynamic Island");
        sub.setText(battery+"%");
        if(expanded){
            label.setText(charging?"טעינה פעילה":"האי הדינמי");
            sub.setText("🔋 "+battery+"%   •   Wi‑Fi   •   פעיל");
        }
    }

    void toggle(){
        expanded=!expanded;
        WindowManager.LayoutParams p=(WindowManager.LayoutParams)pill.getLayoutParams();
        int oldW=p.width, oldH=p.height, oldY=p.y;
        int newW=expanded?dp(350):dp(218);
        int newH=expanded?dp(132):dp(42);
        int newY=expanded?dp(8):dp(6);
        pill.setOrientation(expanded?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        pill.setPadding(dp(14),dp(8),dp(14),dp(8));
        pill.setBackground(bg(expanded?Color.rgb(25,18,38):Color.rgb(5,5,8),expanded?30:50));
        p.width=newW; p.height=newH; p.y=newY;
        animateSize(oldW,oldH,oldY,newW,newH,newY);
        if(expanded){
            icon.setText(charging?"⚡":"◉");
            label.setText(charging?"⚡  טעינה":"✨  Dynamic Island");
            sub.setText("🔋 "+battery+"%   •   Wi‑Fi   •   פעיל");
        }else refresh();
    }

    void animateSize(int oldW,int oldH,int oldY,int newW,int newH,int newY){
        ValueAnimator a=ValueAnimator.ofFloat(0f,1f);
        a.setDuration(220);
        a.setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(v->{
            float t=(Float)v.getAnimatedValue();
            WindowManager.LayoutParams q=(WindowManager.LayoutParams)pill.getLayoutParams();
            q.width=(int)(oldW+(newW-oldW)*t);
            q.height=(int)(oldH+(newH-oldH)*t);
            q.y=(int)(oldY+(newY-oldY)*t);
            try{wm.updateViewLayout(pill,q);}catch(Exception ignored){}
        });
        a.start();
    }

    void showEvent(String title,String detail,String symbol){
        if(pill==null)return;
        icon.setText(symbol==null?"●":symbol);
        icon.setTextColor(Color.rgb(90,210,255));
        label.setText(title==null?"התראה":title);
        sub.setText(detail==null?"":detail);
        pill.setBackground(bg(Color.rgb(24,35,48),36));
        h.removeCallbacks(eventReset);
        h.postDelayed(eventReset,3200);
    }

    final Runnable eventReset=()->{
        if(pill!=null){
            pill.setBackground(bg(expanded?Color.rgb(25,18,38):Color.rgb(5,5,8),expanded?30:50));
            refresh();
        }
    };

    @Override public int onStartCommand(Intent i,int f,int id){
        if(i!=null && i.getBooleanExtra("demo",false)) demo();
        return START_STICKY;
    }

    void demo(){
        if(pill==null)return;
        pill.setBackground(bg(Color.rgb(92,45,180),32));
        icon.setText("✦");
        label.setText("✨  מצב הדגמה");
        sub.setText("LIVE");
        h.postDelayed(()->{
            if(pill!=null){
                pill.setBackground(bg(Color.rgb(10,10,16),50));
                refresh();
            }
        },1800);
    }

    public static void event(String title,String detail){
        if(current!=null) current.showEvent(title,detail,"●");
    }

    @Override public void onDestroy(){
        current=null;
        try{unregisterReceiver(batteryReceiver);}catch(Exception ignored){}
        if(pill!=null&&wm!=null)try{wm.removeView(pill);}catch(Exception ignored){}
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent i){return null;}
}