package com.theindiecorp.mango;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.theindiecorp.mango.activity.CommentActivity;
import com.theindiecorp.mango.activity.HomeActivity;
import com.theindiecorp.mango.activity.LoginActivity;
import com.theindiecorp.mango.activity.ProfileViewActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    /*private static final int BROADCAST_NOTIFICATION_ID = 1;
    public static boolean SOUND = true;
    public static boolean VIBRATION = false;
    public static boolean FLOATING = false;
    public static Uri NOTIFICATION_TONE = null;*/

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e("msg", "received");

        Log.e(TAG, "onMessageReceived: new incoming message.");
        String dataType = remoteMessage.getData().get("data_type");
        String title = remoteMessage.getData().get("data_title");
        String shortMessage = remoteMessage.getData().get("data_message_short");
        String longMessage = remoteMessage.getData().get("data_message_long");
        String messageId = remoteMessage.getData().get("data_message_id");
        String link = remoteMessage.getData().get("data_link");
        sendMessageNotification(dataType, title, shortMessage, longMessage, messageId, link);

        Log.e("short", shortMessage);
        Log.e("long", longMessage);
        Log.e("dataType", dataType);
        Log.e("link", link);

    }

    /**
     * Build a push notification for a chat message
     */
    private void sendMessageNotification(String dataType, String title, String shortMessage, String longMessage, String messageId, String link) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e(TAG, "sendChatmessageNotification: building a chatmessage notification");

            //get the notification id
            int notificationId = buildNotificationId(messageId);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //default channel
            CharSequence name= "Default";
            String channel_id = "default";
            String description = "";
            NotificationChannel channel = new NotificationChannel(channel_id, name, importance);

            if(dataType.equals("postLike")) {
                name = "Post Like";
                description = "Notification sent when another user likes your post";
                channel_id = "post_like";
            }
            else if(dataType.equals("follow")){
                name = "Follow";
                description = "Notification sent when a user follows you";
                channel_id = "follow";
            }
            else if(dataType.equals("postComment")){
                name = "Post Comment";
                description = "Notification sent when a user comments on your post";
                channel_id = "post_comment";
            }
            channel = new NotificationChannel(channel_id, name, importance);
            channel.setDescription(description);

            /*if(!SOUND){
                channel.setSound(null,null);
            }
            else if(NOTIFICATION_TONE != null){
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();

                channel.setSound(NOTIFICATION_TONE, audioAttributes);

            }
            if(!VIBRATION){
                channel.enableVibration(false);
            }
            else{
                channel.enableVibration(true);
            }
            if(FLOATING){
                channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            }
            else{
                channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            }*/


            // Instantiate a Builder object.
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id);
            // Creates an Intent for the Activity
            Intent pendingIntent = new Intent();
            switch (dataType){
                case "postLike":
                    pendingIntent = new Intent(this, HomeActivity.class);
                    break;
                case "follow":
                    pendingIntent = new Intent(this, ProfileViewActivity.class);
                    break;
                case "postComment":
                    pendingIntent = new Intent(this, CommentActivity.class);
            }
            pendingIntent.putExtra("intentType", "startActivityFromNotification");
            pendingIntent.putExtra("link", link);
            // Sets the Activity to start in a new, empty task
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Creates the PendingIntent
            PendingIntent notifyPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            pendingIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            //add properties to the builder
            builder.setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setColor(getColor(R.color.colorPrimary))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            if(!TextUtils.isEmpty(shortMessage)){
                builder.setContentText(shortMessage);
            }
            if(!TextUtils.isEmpty(longMessage)){
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(longMessage));
            }

            builder.setContentIntent(notifyPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(channel);

            mNotificationManager.notify(notificationId, builder.build());

        }
    }


    private int buildNotificationId(String id) {
        Log.e(TAG, "buildNotificationId: building a notification id.");

        int notificationId = 0;
        for (int i = 0; i < 9; i++) {
            notificationId = notificationId + id.charAt(0);
        }
        Log.e(TAG, "buildNotificationId: id: " + id);
        Log.e(TAG, "buildNotificationId: notification id:" + notificationId);
        return notificationId;
    }

}
