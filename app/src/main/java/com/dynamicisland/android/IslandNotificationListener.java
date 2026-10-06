package com.dynamicisland.android;

import android.app.Notification;
import android.content.ComponentName;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import java.util.List;

public class IslandNotificationListener extends NotificationListenerService {
 MediaSessionManager mediaManager;
 MediaSessionManager.OnActiveSessionsChangedListener mediaListener;
 static MediaController pendingController;
 static String pendingTitle="", pendingDetail="";

 @Override public void onListenerConnected(){
  super.onListenerConnected();
  try{
   mediaManager=(MediaSessionManager)getSystemService(MEDIA_SESSION_SERVICE);
   ComponentName cn=new ComponentName(this,IslandNotificationListener.class);
   mediaListener=sessions->{
    MediaController chosen=(sessions!=null&&!sessions.isEmpty())?sessions.get(0):null;
    pendingController=chosen;
    IslandService.setMediaController(chosen);
   };
   mediaManager.addOnActiveSessionsChangedListener(mediaListener,cn);
   List<MediaController> sessions=mediaManager.getActiveSessions(cn);
   MediaController chosen=(sessions!=null&&!sessions.isEmpty())?sessions.get(0):null;
   pendingController=chosen;
   IslandService.setMediaController(chosen);
   if(!pendingTitle.isEmpty()) IslandService.event(pendingTitle,pendingDetail);\n   postLatestNotification();
  }catch(Exception ignored){}
 }

 @Override public void onListenerDisconnected(){
  try{if(mediaManager!=null&&mediaListener!=null)mediaManager.removeOnActiveSessionsChangedListener(mediaListener);}catch(Exception ignored){}
  pendingController=null;
  IslandService.setMediaController(null);
  super.onListenerDisconnected();
 }

 @Override public void onNotificationPosted(StatusBarNotification sbn) {
  if(getPackageName().equals(sbn.getPackageName()))return;
  Notification n=sbn.getNotification();
  if(n==null)return;
  Bundle e=n.extras;
  if(e==null)return;
  CharSequence title=e.getCharSequence(Notification.EXTRA_TITLE);
  CharSequence text=e.getCharSequence(Notification.EXTRA_TEXT);\n  if(text==null) text=e.getCharSequence(Notification.EXTRA_BIG_TEXT);
  if(title==null&&text==null)return;
  pendingTitle=title==null?"התראה":title.toString();
  pendingDetail=text==null?"":text.toString();
  IslandService.event(pendingTitle,pendingDetail);
  if(getSharedPreferences("island_settings",MODE_PRIVATE).getBoolean("only_island_notifications",false)){
   try{cancelNotification(sbn.getKey());}catch(Exception ignored){}
  }
 }
 @Override public void onNotificationRemoved(StatusBarNotification sbn){}
}
