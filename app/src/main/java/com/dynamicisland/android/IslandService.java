package com.dynamicisland.android;

import android.app.*;
import android.animation.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaController;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

public class IslandService extends Service {
 WindowManager wm; LinearLayout pill,topRow,controls; TextView icon,label,sub; Handler h=new Handler(Looper.getMainLooper());
 boolean expanded=false; public static IslandService current;
 String mediaTitle="אין מדיה"; boolean mediaPlaying=false; long timerEnd=0; String timerLabel="";
 int battery=100; boolean charging=false; int widthDp=218,heightDp=42,topDp=6; int eventTimeout=3200;

 int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable bg(int c,float r){GradientDrawable x=new GradientDrawable();x.setColor(c);x.setCornerRadius(dp(r));x.setStroke(dp(1),Color.argb(55,255,255,255));return x;}

 @Override public void onCreate(){super.onCreate();current=this;loadSettings();startForeground(7,notifyMe());show();}

 void loadSettings(){SharedPreferences p=getSharedPreferences("island_settings",MODE_PRIVATE);widthDp=p.getInt("width_dp",218);heightDp=p.getInt("height_dp",42);topDp=p.getInt("top_dp",6);eventTimeout=p.getInt("notification_timeout_ms",3200);}
 public static void applySettings(){if(current!=null){current.loadSettings();current.applyLayout();current.render();}}
 void applyLayout(){if(pill==null||wm==null)return;WindowManager.LayoutParams p=(WindowManager.LayoutParams)pill.getLayoutParams();p.width=dp(expanded?Math.max(widthDp,300):widthDp);p.height=dp(expanded?132:heightDp);p.y=dp(topDp);try{wm.updateViewLayout(pill,p);}catch(Exception ignored){}}

 final BroadcastReceiver batteryReceiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){int level=i.getIntExtra("level",100),scale=i.getIntExtra("scale",100);battery=scale>0?(level*100/scale):100;int status=i.getIntExtra("status",-1);charging=status==BatteryManager.BATTERY_STATUS_CHARGING||status==BatteryManager.BATTERY_STATUS_FULL;refresh();}};
 Notification notifyMe(){String c="island";NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);if(Build.VERSION.SDK_INT>=26)n.createNotificationChannel(new NotificationChannel(c,"Dynamic Island",NotificationManager.IMPORTANCE_LOW));return new Notification.Builder(this,c).setContentTitle("אי דינמי פעיל").setContentText("האי הדינמי פועל").setSmallIcon(android.R.drawable.ic_dialog_info).setOngoing(true).build();}

 void show(){if(!Settings.canDrawOverlays(this))return;wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  pill=new LinearLayout(this);pill.setOrientation(LinearLayout.VERTICAL);pill.setGravity(Gravity.CENTER);pill.setPadding(dp(12),dp(5),dp(12),dp(5));pill.setBackground(bg(Color.BLACK,50));
  topRow=new LinearLayout(this);topRow.setOrientation(LinearLayout.HORIZONTAL);topRow.setGravity(Gravity.CENTER_VERTICAL);
  icon=new TextView(this);icon.setTextSize(12);icon.setGravity(Gravity.CENTER);
  label=new TextView(this);label.setTextColor(Color.WHITE);label.setTextSize(13);label.setTypeface(null,1);label.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);label.setSingleLine(true);
  sub=new TextView(this);sub.setTextColor(Color.LTGRAY);sub.setTextSize(11);sub.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);sub.setSingleLine(true);
  topRow.addView(icon,new LinearLayout.LayoutParams(dp(24),-1));topRow.addView(label,new LinearLayout.LayoutParams(0,-1,1));topRow.addView(sub,new LinearLayout.LayoutParams(dp(64),-1));pill.addView(topRow,new LinearLayout.LayoutParams(-1,0,1));
  controls=new LinearLayout(this);controls.setOrientation(LinearLayout.HORIZONTAL);controls.setGravity(Gravity.CENTER);controls.setVisibility(View.GONE);pill.addView(controls,new LinearLayout.LayoutParams(-1,dp(44)));
  addControl("⏮",v->sendMedia(PlaybackState.ACTION_SKIP_TO_PREVIOUS));addControl("▶",v->toggleMedia());addControl("⏭",v->sendMedia(PlaybackState.ACTION_SKIP_TO_NEXT));
  pill.setOnClickListener(v->toggle());
  WindowManager.LayoutParams p=new WindowManager.LayoutParams(dp(widthDp),dp(heightDp),Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;p.y=dp(topDp);wm.addView(pill,p);render();}
 void addControl(String text,View.OnClickListener l){Button x=new Button(this);x.setText(text);x.setTextColor(Color.WHITE);x.setTextSize(16);x.setAllCaps(false);x.setBackground(bg(Color.rgb(45,35,60),22));x.setOnClickListener(l);LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(0,dp(40),1);q.setMargins(dp(4),0,dp(4),0);controls.addView(x,q);}

 public static void setMediaController(MediaController c){if(current!=null)current.setController(c);}
 void setController(MediaController c){mediaController=c;mediaActive=c!=null;readMedia();render();}
 void readMedia(){if(mediaController==null)return;try{PlaybackState ps=mediaController.getPlaybackState();mediaPlaying=ps!=null&&ps.getState()==PlaybackState.STATE_PLAYING;MediaMetadata md=mediaController.getMetadata();String t=md==null?null:md.getString(MediaMetadata.METADATA_KEY_TITLE);mediaTitle=t==null||t.isEmpty()?"מוזיקה":t;}catch(Exception ignored){}}
 void toggleMedia(){if(mediaController==null)return;try{if(mediaPlaying)mediaController.getTransportControls().pause();else mediaController.getTransportControls().play();}catch(Exception ignored){}h.postDelayed(()->{readMedia();render();},180);}
 void sendMedia(long action){if(mediaController==null)return;try{if(action==PlaybackState.ACTION_SKIP_TO_PREVIOUS)mediaController.getTransportControls().skipToPrevious();else mediaController.getTransportControls().skipToNext();}catch(Exception ignored){}h.postDelayed(()->{readMedia();render();},180);}
 void updateMedia(String title,boolean playing){mediaTitle=title==null||title.isEmpty()?"מוזיקה":title;mediaPlaying=playing;mediaActive=true;render();}
 void render(){if(pill==null)return;
  if(expanded){pill.setBackground(bg(Color.rgb(25,18,38),30));controls.setVisibility(mediaActive?View.VISIBLE:View.GONE);}
  else {pill.setBackground(bg(Color.BLACK,50));controls.setVisibility(View.GONE);}
  if(notificationActive){icon.setText("●");icon.setTextColor(Color.rgb(90,210,255));label.setText(notificationTitle);sub.setText(notificationDetail);}
  else if(mediaActive){icon.setText(mediaPlaying?"▶":"Ⅱ");icon.setTextColor(Color.rgb(255,90,180));label.setText(mediaTitle);sub.setText(mediaPlaying?"מנגן":"מושהה");}
  else {icon.setText("");label.setText("");sub.setText("");}
  if(controls.getChildCount()>1)((Button)controls.getChildAt(1)).setText(mediaPlaying?"Ⅱ":"▶");
 }
 void toggle(){expanded=!expanded;int oldW=pill.getLayoutParams().width,oldH=pill.getLayoutParams().height,oldY=((WindowManager.LayoutParams)pill.getLayoutParams()).y;int newW=dp(expanded?Math.max(widthDp,300):widthDp),newH=dp(expanded?136:heightDp),newY=dp(topDp+(expanded?2:0));animateSize(oldW,oldH,oldY,newW,newH,newY);pill.setPadding(dp(14),dp(expanded?8:5),dp(14),dp(expanded?8:5));render();}
 void animateSize(int oldW,int oldH,int oldY,int newW,int newH,int newY){ValueAnimator a=ValueAnimator.ofFloat(0f,1f);a.setDuration(220);a.setInterpolator(new DecelerateInterpolator());a.addUpdateListener(v->{float t=(Float)v.getAnimatedValue();WindowManager.LayoutParams q=(WindowManager.LayoutParams)pill.getLayoutParams();q.width=(int)(oldW+(newW-oldW)*t);q.height=(int)(oldH+(newH-oldH)*t);q.y=(int)(oldY+(newY-oldY)*t);try{wm.updateViewLayout(pill,q);}catch(Exception ignored){}});a.start();}
 void showEvent(String title,String detail){if(pill==null)return;notificationTitle=title==null?"התראה":title;notificationDetail=detail==null?"":detail;notificationActive=true;render();h.removeCallbacks(eventReset);if(eventTimeout>0)h.postDelayed(eventReset,eventTimeout);}
 final Runnable eventReset=()->{notificationActive=false;render();};
 public int onStartCommand(Intent i,int f,int id){if(i!=null&&i.getBooleanExtra("demo",false))demo();return START_STICKY;}
 void demo(){showEvent("התראה לדוגמה","זה עובד ✦");h.postDelayed(()->{notificationActive=false;render();},1800);}
 public static void event(String title,String detail){if(current!=null)current.showEvent(title,detail);}
 public void onDestroy(){current=null;if(pill!=null&&wm!=null)try{wm.removeView(pill);}catch(Exception ignored){}super.onDestroy();}
 public IBinder onBind(Intent i){return null;}
}
