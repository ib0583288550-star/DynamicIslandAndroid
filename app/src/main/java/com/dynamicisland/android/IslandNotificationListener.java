package com.dynamicisland.android;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;

public class IslandNotificationListener extends NotificationListenerService {
    @Override public void onNotificationPosted(StatusBarNotification sbn) {
        Notification n=sbn.getNotification();
        if(n==null)return;
        Bundle e=n.extras;
        CharSequence title=e.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text=e.getCharSequence(Notification.EXTRA_TEXT);
        if(title==null && text==null)return;
        String t=title!=null?title.toString():"התראה";
        String d=text!=null?text.toString():"";
        IslandService service=IslandServiceHolder.service;
        if(service!=null) service.showEvent(t,d,"●");
    }
}