package com.theindiecorp.vconnect.data;

public class Notification {
    private String notificationType;
    private String notification;
    private String link;

    public Notification() {
    }

    public Notification(String notificationType, String notification, String link) {
        this.notificationType = notificationType;
        this.notification = notification;
        this.link = link;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
