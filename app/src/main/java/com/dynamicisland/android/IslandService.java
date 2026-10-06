package com.dynamicisland.android;

import android.app.*;
import android.animation.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.media.session.MediaController;
import android.media.MediaMetadata;
import android.graphics.Bitmap;
import android.media.session.PlaybackState;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

public class IslandService extends Service {
 MediaController mediaController;
 String mediaPackage="";
 boolean mediaActive=false, mediaPlaying=false;
 boolean notificationActive=false;
 boolean showingPlayer=true;
 boolean userSelectedView=false;
 String notificationTitle="", notificationDetail="";
 android.app.PendingIntent notificationIntent;
 String mediaTitle="מוזיקה", mediaArtist="", mediaTime="";
 WindowManager wm; LinearLayout pill,topRow,controls; TextView icon,label,sub,timeText; ImageButton switchButton, aiButton; ImageView artwork;
 Bitmap artworkBitmap; Handler h=new Handler(Looper.getMainLooper());
 boolean expanded=false; public static IslandService current;
 int widthDp=218,heightDp=42,topDp=6; int cornerPercent=100; int eventTimeout=3200,autoCloseMs=3000; boolean voiceAssistantButton=true;

 int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable bg(int c,float r){GradientDrawable x=new GradientDrawable();x.setColor(c);x.setCornerRadius(dp(r));x.setStroke(dp(1),Color.argb(55,255,255,255));return x;}\n float cornerRadius(){int h=expanded?132:heightDp;int w=expanded?Math.min(Math.max(widthDp,280),340):widthDp;return Math.min(w,h)*0.5f*(Math.max(0,Math.min(100,cornerPercent))/100f);}

 @Override public void onCreate(){super.onCreate();current=this;loadSettings();startForeground(7,notifyMe());show();if(IslandNotificationListener.pendingController!=null)setController(IslandNotificationListener.pendingController);if(!IslandNotificationListener.pendingTitle.isEmpty())showEvent(IslandNotificationListener.pendingTitle,IslandNotificationListener.pendingDetail);}

 void loadSettings(){SharedPreferences p=getSharedPreferences("island_settings",MODE_PRIVATE);widthDp=p.getInt("width_dp",218);heightDp=p.getInt("height_dp",42);topDp=p.getInt("top_dp",6);cornerPercent=p.getInt("corner_percent",100);eventTimeout=p.getInt("notification_timeout_ms",3200);autoCloseMs=p.getInt("auto_close_ms",3000);voiceAssistantButton=p.getBoolean("voice_assistant_button",true);}
 public static void applySettings(){if(current!=null){current.loadSettings();current.applyLayout();current.render();}}
 void applyLayout(){if(pill==null||wm==null)return;WindowManager.LayoutParams p=(WindowManager.LayoutParams)pill.getLayoutParams();p.width=dp(expanded?Math.min(Math.max(widthDp,280),340):widthDp);p.height=dp(expanded?132:heightDp);p.y=dp(topDp);try{wm.updateViewLayout(pill,p);}catch(Exception ignored){}}

 Notification notifyMe(){String c="island";NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);if(Build.VERSION.SDK_INT>=26)n.createNotificationChannel(new NotificationChannel(c,"Dynamic Island",NotificationManager.IMPORTANCE_LOW));return new Notification.Builder(this,c).setContentTitle("אי דינמי פעיל").setContentText("האי הדינמי פועל").setSmallIcon(android.R.drawable.ic_dialog_info).setOngoing(true).build();}

 void show(){if(!Settings.canDrawOverlays(this))return;wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  pill=new LinearLayout(this);pill.setOrientation(LinearLayout.VERTICAL);pill.setGravity(Gravity.CENTER);pill.setPadding(dp(12),dp(7),dp(12),dp(7));pill.setBackground(bg(Color.BLACK,22));
  topRow=new LinearLayout(this);topRow.setOrientation(LinearLayout.HORIZONTAL);topRow.setGravity(Gravity.CENTER_VERTICAL);
  artwork=new ImageView(this);artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);artwork.setBackground(bg(Color.rgb(45,45,55),10));
  icon=new TextView(this);icon.setTextSize(12);icon.setGravity(Gravity.CENTER);
  label=new TextView(this);label.setTextColor(Color.WHITE);label.setTextSize(16);label.setTypeface(null,1);label.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);label.setSingleLine(false);label.setMaxLines(2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);
  sub=new TextView(this);sub.setTextColor(Color.LTGRAY);sub.setTextSize(13);sub.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);sub.setSingleLine(false);sub.setMaxLines(2);sub.setEllipsize(android.text.TextUtils.TruncateAt.END);
  aiButton=new ImageButton(this);aiButton.setImageResource(android.R.drawable.ic_menu_manage);aiButton.setColorFilter(Color.WHITE);aiButton.setBackground(bg(Color.rgb(20,20,20),18));aiButton.setContentDescription("עוזר AI");aiButton.setOnClickListener(v->openAssistant());
  switchButton=new ImageButton(this);switchButton.setImageResource(android.R.drawable.ic_dialog_email);switchButton.setColorFilter(Color.WHITE);switchButton.setBackground(bg(Color.rgb(20,20,20),18));switchButton.setContentDescription("מעבר בין נגן להתראות");
  switchButton.setOnClickListener(v->{userSelectedView=true;if(mediaActive && notificationActive){showingPlayer=!showingPlayer;render();}else if(notificationActive){showingPlayer=false;render();}else if(mediaActive){showingPlayer=true;render();}});
  timeText=new TextView(this);timeText.setTextColor(Color.LTGRAY);timeText.setTextSize(10);timeText.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);timeText.setSingleLine(true);
  topRow.addView(artwork,new LinearLayout.LayoutParams(dp(34),dp(34)));
  topRow.addView(icon,new LinearLayout.LayoutParams(dp(22),-1));
  LinearLayout textBox=new LinearLayout(this);textBox.setOrientation(LinearLayout.VERTICAL);textBox.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
  textBox.addView(label,new LinearLayout.LayoutParams(-1,0,1));
  textBox.addView(sub,new LinearLayout.LayoutParams(-1,0,1));
  topRow.addView(textBox,new LinearLayout.LayoutParams(0,-1,1));
  topRow.addView(aiButton,new LinearLayout.LayoutParams(dp(28),dp(28)));
  topRow.addView(switchButton,new LinearLayout.LayoutParams(dp(28),dp(28)));
  pill.addView(topRow,new LinearLayout.LayoutParams(-1,0,1));
  pill.addView(timeText,new LinearLayout.LayoutParams(-1,dp(18)));
  controls=new LinearLayout(this);controls.setOrientation(LinearLayout.HORIZONTAL);controls.setGravity(Gravity.CENTER);controls.setVisibility(View.GONE);pill.addView(controls,new LinearLayout.LayoutParams(-1,dp(40)));
  addControl("⏭",v->sendMedia(PlaybackState.ACTION_SKIP_TO_NEXT));addControl("▶",v->toggleMedia());addControl("⏮",v->sendMedia(PlaybackState.ACTION_SKIP_TO_PREVIOUS));
  pill.setOnClickListener(v->toggle());
  artwork.setOnClickListener(v->openMusicApp());
  label.setOnClickListener(v->{if(notificationActive&&!showingPlayer)openNotification();else if(mediaActive)openMusicApp();});
  sub.setOnClickListener(v->{if(notificationActive&&!showingPlayer)openNotification();else if(mediaActive)openMusicApp();});
  WindowManager.LayoutParams p=new WindowManager.LayoutParams(dp(widthDp),dp(heightDp),Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;p.y=dp(topDp);wm.addView(pill,p);render();}

 void addControl(String text,View.OnClickListener l){Button x=new Button(this);x.setText(text);x.setTextColor(Color.WHITE);x.setTextSize(16);x.setAllCaps(false);x.setBackground(bg(Color.rgb(18,18,18),22));x.setOnClickListener(l);LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(0,dp(36),1);q.setMargins(dp(4),0,dp(4),0);controls.addView(x,q);}

 public static void setMediaController(MediaController c){if(current!=null)current.setController(c);}
 void setController(MediaController c){mediaController=c;mediaPackage=c==null?"":c.getPackageName();mediaActive=c!=null;readMedia();render();}
 String fmt(long ms){if(ms<0)ms=0;long sec=ms/1000;return String.format(java.util.Locale.US,"%d:%02d",sec/60,sec%60);}
 void readMedia(){if(mediaController==null)return;try{PlaybackState ps=mediaController.getPlaybackState();mediaPlaying=ps!=null&&ps.getState()==PlaybackState.STATE_PLAYING;MediaMetadata md=mediaController.getMetadata();String t=md==null?null:md.getString(MediaMetadata.METADATA_KEY_TITLE);CharSequence a=md==null?null:md.getText(MediaMetadata.METADATA_KEY_ARTIST);mediaArtist=a==null?"":a.toString();artworkBitmap=md==null?null:md.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);mediaTitle=t==null||t.isEmpty()?"מוזיקה":t;long pos=ps==null?0:ps.getPosition();long dur=md==null?0:md.getLong(MediaMetadata.METADATA_KEY_DURATION);mediaTime=fmt(pos)+" / "+fmt(dur); }catch(Exception ignored){}}
 void toggleMedia(){if(mediaController==null)return;try{if(mediaPlaying)mediaController.getTransportControls().pause();else mediaController.getTransportControls().play();}catch(Exception ignored){}h.postDelayed(()->{readMedia();render();},180);}
 void sendMedia(long action){if(mediaController==null)return;try{if(action==PlaybackState.ACTION_SKIP_TO_PREVIOUS)mediaController.getTransportControls().skipToPrevious();else mediaController.getTransportControls().skipToNext();}catch(Exception ignored){}h.postDelayed(()->{readMedia();render();},180);}
 void openAssistant(){try{Intent a=new Intent(Intent.ACTION_ASSIST);a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(a);}catch(Exception ignored){}}
 void openNotification(){try{if(notificationIntent!=null){notificationIntent.send();return;}}catch(Exception ignored){} }
 void openMusicApp(){try{if(mediaController!=null){android.app.PendingIntent pi=mediaController.getSessionActivity();if(pi!=null){try{pi.send();return;}catch(Exception ignored){}}}String pkg=mediaPackage.isEmpty()?(mediaController==null?null:mediaController.getPackageName()):mediaPackage;if(pkg!=null&&!pkg.isEmpty()){Intent launch=getPackageManager().getLaunchIntentForPackage(pkg);if(launch!=null){launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);startActivity(launch);return;}}}catch(Exception ignored){}}
 void updateMedia(String title,boolean playing){mediaTitle=title==null||title.isEmpty()?"מוזיקה":title;mediaPlaying=playing;mediaActive=true;render();}

 void render(){if(pill==null)return;
  if(artwork!=null){artwork.setContentDescription(mediaPackage.isEmpty()?"אלבום":"פתיחת האפליקציה שמנגנת עכשיו");if(artworkBitmap!=null)artwork.setImageBitmap(artworkBitmap);else artwork.setImageResource(android.R.drawable.ic_media_play);artwork.setVisibility(mediaActive?View.VISIBLE:View.GONE);}
  boolean showPlayer=mediaActive && (showingPlayer || !notificationActive);
  boolean showSwitch=mediaActive||notificationActive;
  switchButton.setVisibility(showSwitch && expanded?View.VISIBLE:View.GONE);
  aiButton.setVisibility(expanded && voiceAssistantButton?View.VISIBLE:View.GONE);
  if(expanded){
   pill.setBackground(bg(Color.BLACK,cornerRadius()));controls.setVisibility(showPlayer?View.VISIBLE:View.GONE);
   timeText.setVisibility(showPlayer&&expanded?View.VISIBLE:View.GONE);
   artwork.getLayoutParams().width=dp(showPlayer?56:32);artwork.getLayoutParams().height=dp(showPlayer?56:32);artwork.requestLayout();
  }else{
   pill.setBackground(bg(Color.BLACK,cornerRadius()));controls.setVisibility(View.GONE);timeText.setVisibility(View.GONE);
   artwork.getLayoutParams().width=dp(34);artwork.getLayoutParams().height=dp(34);artwork.requestLayout();
  }
  if(notificationActive && !showingPlayer){
   icon.setText("♢");icon.setTextColor(Color.rgb(90,210,255));label.setText(notificationTitle);sub.setText(notificationDetail);timeText.setText("");
   artwork.setVisibility(View.GONE);
   switchButton.setImageResource(android.R.drawable.ic_dialog_email);
  }else if(mediaActive){
   icon.setText("▶");icon.setTextColor(Color.rgb(255,90,180));label.setText(mediaTitle);sub.setText(expanded?(mediaArtist.isEmpty()?"מנגן":mediaArtist):"");timeText.setText(mediaTime);
   artwork.setVisibility(View.VISIBLE);
   switchButton.setImageResource(android.R.drawable.ic_media_play);
  }else{
   icon.setText("");label.setText("");sub.setText("");timeText.setText("");artwork.setVisibility(View.GONE);switchButton.setVisibility(View.GONE);aiButton.setVisibility(View.GONE);
  }
  if(controls.getChildCount()>1)((Button)controls.getChildAt(1)).setText(mediaPlaying?"Ⅱ":"▶");
 }
 void toggle(){expanded=!expanded;int oldW=pill.getLayoutParams().width,oldH=pill.getLayoutParams().height,oldY=((WindowManager.LayoutParams)pill.getLayoutParams()).y;int newW=dp(expanded?Math.min(Math.max(widthDp,250),310):widthDp),newH=dp(expanded?132:heightDp),newY=dp(topDp+(expanded?2:0));animateSize(oldW,oldH,oldY,newW,newH,newY);pill.setPadding(dp(14),dp(expanded?10:7),dp(14),dp(expanded?10:7));render();h.removeCallbacks(autoCollapse);if(expanded&&autoCloseMs>0)h.postDelayed(autoCollapse,autoCloseMs);}
 void animateSize(int oldW,int oldH,int oldY,int newW,int newH,int newY){ValueAnimator a=ValueAnimator.ofFloat(0f,1f);a.setDuration(220);a.setInterpolator(new DecelerateInterpolator());a.addUpdateListener(v->{float t=(Float)v.getAnimatedValue();WindowManager.LayoutParams q=(WindowManager.LayoutParams)pill.getLayoutParams();q.width=(int)(oldW+(newW-oldW)*t);q.height=(int)(oldH+(newH-oldH)*t);q.y=(int)(oldY+(newY-oldY)*t);try{wm.updateViewLayout(pill,q);}catch(Exception ignored){}});a.start();}
 public static void showNotificationIntent(android.app.PendingIntent pi){if(current!=null)current.notificationIntent=pi;}
 void showEvent(String title,String detail){if(pill==null)return;notificationTitle=title==null?"התראה":title;notificationDetail=detail==null?"":detail;notificationActive=true;if(!userSelectedView)showingPlayer=false;render();h.removeCallbacks(eventReset);if(eventTimeout>0)h.postDelayed(eventReset,eventTimeout);}
 final Runnable autoCollapse=()->{if(expanded){expanded=false;applyLayout();pill.setPadding(dp(12),dp(7),dp(12),dp(7));render();}};
 final Runnable eventReset=()->{notificationActive=false;render();};
 public int onStartCommand(Intent i,int f,int id){if(i!=null&&i.getBooleanExtra("demo",false))demo();return START_STICKY;}
 void demo(){showEvent("התראה לדוגמה","זה עובד ✦");}
 public static void event(String title,String detail){if(current!=null)current.showEvent(title,detail);else{IslandNotificationListener.pendingTitle=title==null?"התראה":title;IslandNotificationListener.pendingDetail=detail==null?"":detail;}}
 public void onDestroy(){current=null;if(pill!=null&&wm!=null)try{wm.removeView(pill);}catch(Exception ignored){}super.onDestroy();}
 public IBinder onBind(Intent i){return null;}
}