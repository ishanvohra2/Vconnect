package com.theindiecorp.mango;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.theindiecorp.mango.activity.CommentActivity;
import com.theindiecorp.mango.activity.HomeActivity;
import com.theindiecorp.mango.activity.ProfileViewActivity;
import com.theindiecorp.mango.data.Notification;

import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.MyViewHolder>{
    private ArrayList<Notification> dataSet;

    Context context;

    //Constants
    private final String NOTIFICATION_TYPE_POST_LIKE = "postLike";
    private final String NOTIFICATION_TYPE_FOLLOW = "follow";
    private final String NOTIFICATION_TYPE_POST_COMMENT = "postComment";

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView notificationType, notificationMessage;
        private ImageView notificationIcon;
        private ConstraintLayout notificationsContainer;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.notificationType = itemView.findViewById(R.id.notification_type);
            this.notificationMessage = itemView.findViewById(R.id.notification_message);
            this.notificationIcon = itemView.findViewById(R.id.notification_type_icon);
            this.notificationsContainer = itemView.findViewById(R.id.notification_container);
        }
    }


    public NotificationsAdapter(ArrayList<Notification> data, Context context) {
        this.dataSet = data;
        this.context = context;
    }

    public void setNotifications(ArrayList<Notification> notifications) {
        this.dataSet = notifications;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int listPosition) {

        final String notificationTypeString = dataSet.get(listPosition).getNotificationType();
        switch (notificationTypeString){
            case NOTIFICATION_TYPE_POST_LIKE:
                Glide.with(context).load(R.drawable.liked).into(holder.notificationIcon);
                holder.notificationType.setText("Like");
                break;
            case NOTIFICATION_TYPE_FOLLOW:
                Glide.with(context).load(R.drawable.followed).into(holder.notificationIcon);
                holder.notificationType.setText("Follow");
                break;
            case NOTIFICATION_TYPE_POST_COMMENT:
                holder.notificationIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.comment_icon));
                holder.notificationType.setText("Comment");
        }
        holder.notificationMessage.setText(dataSet.get(listPosition).getNotification());

        holder.notificationsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                switch (notificationTypeString){
                    case NOTIFICATION_TYPE_POST_LIKE:
                        intent = new Intent(context, HomeActivity.class);
                        break;
                    case NOTIFICATION_TYPE_FOLLOW:
                        intent = new Intent(context, ProfileViewActivity.class);
                        break;
                    case NOTIFICATION_TYPE_POST_COMMENT:
                        intent = new Intent(context, CommentActivity.class);
                }
                intent.putExtra("intentType", "startActivityFromNotification");
                intent.putExtra("link", dataSet.get(listPosition).getLink());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
