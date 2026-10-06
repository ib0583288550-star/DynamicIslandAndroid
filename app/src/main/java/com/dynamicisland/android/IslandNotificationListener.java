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

 @Override public void onListenerConnected(){
  super.onListenerConnected();
  try{
   mediaManager=(MediaSessionManager)getSystemService(MEDIA_SESSION_SERVICE);
   ComponentName cn=new ComponentName(this,IslandNotificationListener.class);
   mediaListener=sessions->{
    MediaController chosen=(sessions!=null&&!sessions.isEmpty())?sessions.get(0):null;
    IslandService.setMediaController(chosen);
   };
   mediaManager.addOnActiveSessionsChangedListener(mediaListener,cn);
   List<MediaController> sessions=mediaManager.getActiveSessions(cn);
   MediaController chosen=(sessions!=null&&!sessions.isEmpty())?sessions.get(0):null;
   IslandService.setMediaController(chosen);
  }catch(Exception ignored){}
 }

 @Override public void onListenerDisconnected(){
  try{if(mediaManager!=null&&mediaListener!=null)mediaManager.removeOnActiveSessionsChangedListener(mediaListener);}catch(Exception ignored){}
  IslandService.setMediaController(null);
  super.onListenerDisconnected();
 }

 @Override public void onNotificationPosted(StatusBarNotification sbn) {
  Notification n=sbn.getNotification();
  if(n==null)return;
  Bundle e=n.extras;
  if(e!=null && e.getParcelable(Notification.EXTRA_MEDIA_SESSION)!=null){
   return;
  }
  CharSequence title=e.getCharSequence(Notification.EXTRA_TITLE);
  CharSequence text=e.getCharSequence(Notification.EXTRA_TEXT);
  if(title==null&&text==null)return;
  IslandService.event(title==null?"התראה":title.toString(),text==null?"":text.toString());
  if(getSharedPreferences("island_settings",MODE_PRIVATE).getBoolean("only_island_notifications",false)){
   try{cancelNotification(sbn.getKey());}catch(Exception ignored){}
  }
 }
 @Override public void onNotificationRemoved(StatusBarNotification sbn){}
}
