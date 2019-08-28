package com.theindiecorp.vconnect;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.theindiecorp.vconnect.activity.AttendeeViewActivity;
import com.theindiecorp.vconnect.activity.CommentActivity;
import com.theindiecorp.vconnect.activity.EventViewActivity;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.activity.ProfileViewActivity;
import com.theindiecorp.vconnect.data.Event;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class mainFeedRecyclerViewAdapter extends RecyclerView.Adapter<mainFeedRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<Event> dataSet;
    private String name;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private boolean isBookmarked = false;
    private boolean isLiked = false;
    int likeCounter = 0;

    Context context;
    LinearLayout layout;

    BottomSheetBehavior bottomSheetBehavior;

    public void addEvent(Event event) {
        this.dataSet.add(event);
    }

    public int setEvents(ArrayList<Event> events) {
        this.dataSet = events;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView title, place, description, date, eventName, peopleCount, likeCount;
        private ImageView profileImg, mainImg, bookmarkBtn, menuImg,likeBtn,commentBtn;
        LinearLayout profileBar;
        Boolean bookmarked,liked;


        public MyViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.main_item_title);
            this.place = itemView.findViewById(R.id.main_item_sub_title);
            this.description = itemView.findViewById(R.id.main_item_description);
            this.profileImg = itemView.findViewById(R.id.main_item_profile_pic);
            this.mainImg = itemView.findViewById(R.id.main_item_main_image);
            this.date = itemView.findViewById(R.id.main_item_date);
            this.eventName = itemView.findViewById(R.id.main_item_event_name);
            this.peopleCount = itemView.findViewById(R.id.main_item_people_count);
            this.bookmarkBtn = itemView.findViewById(R.id.main_item_bookmark_btn);
            this.profileBar = itemView.findViewById(R.id.profile_bar);
            this.menuImg = itemView.findViewById(R.id.main_item_menu_img);
            this.likeBtn = itemView.findViewById(R.id.main_item_like_btn);
            this.likeCount = itemView.findViewById(R.id.main_post_likes);
            this.commentBtn = itemView.findViewById(R.id.main_item_comment_btn);
        }
    }


    public mainFeedRecyclerViewAdapter(ArrayList<Event> data, Context context) {
        this.dataSet = data;
        this.context = context;
    }


    public mainFeedRecyclerViewAdapter(ArrayList<Event> data, Context context, LinearLayout coordinatorLayout) {
        this.dataSet = data;
        this.context = context;
        this.layout = coordinatorLayout;
    }


    public mainFeedRecyclerViewAdapter(ArrayList<Event> data, Context context, boolean isBookmarked, LinearLayout frameLayout,boolean isLiked) {
        this.dataSet = data;
        this.context = context;
        this.isBookmarked = isBookmarked;
        this.layout = frameLayout;
        this.isLiked = isLiked;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_main_recycler_view_item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    private Dialog onCreateDialog(final String eventId) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.report_dialog_layout);

        final EditText messageTxt = dialog.findViewById(R.id.report_message);
        Button submitBtn = dialog.findViewById(R.id.report_dialog_submit_btn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("reports").child(eventId).child("message").setValue(messageTxt.getText().toString());
                dialog.dismiss();
            }
        });
        return dialog;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int listPosition) {
        final Event event = dataSet.get(listPosition);

        // profile image reference
        StorageReference profileImageReference = storage.getReference().child("users/" + event.getHostId() + "/images/profile_pic/profile_pic.jpeg");
        profileImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .apply(new RequestOptions()
                                .override(50,50))
                        .load(uri)
                        .into(holder.profileImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(HomeActivity.TAG, exception.getMessage());
            }
        });

        holder.menuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, holder.menuImg);
                popup.inflate(R.menu.menu_main_item);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.see_people_who_are_going:
                                Intent intent = new Intent(context, AttendeeViewActivity.class);
                                intent.putExtra("eventId", event.getId());
                                context.startActivity(intent);
                                break;
                            case R.id.menu_bookmark:
                                bookmark(holder.bookmarked, event.getId());
                                break;
                            case R.id.menu_report:
                                Dialog dialog = onCreateDialog(event.getId());
                                dialog.show();
                                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                                int width = metrics.widthPixels;
                                int height = metrics.heightPixels;
                                dialog.getWindow().setLayout((6 * width) / 7, height * 2 / 3);
                                break;
                            default:
                                return false;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();

            }
        });

        databaseReference.child("users").child(event.getHostId()).child("displayName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.title.setText(dataSnapshot.getValue(String.class));
                event.setHostName(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.title.setText(event.getHostName());
        holder.description.setText(event.getDescription());
        holder.eventName.setText(event.getEventName());
        holder.peopleCount.setText(String.valueOf(event.getPeopleCount()) + " going");


        String date = event.getDate();
        holder.date.setText(date);

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    context.startActivity(new Intent(context, ProfileViewActivity.class).putExtra("userId",event.getHostId()));
            }
        });

        databaseReference.child("bookmarks").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(event.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.bookmarked = dataSnapshot.exists();
                if (holder.bookmarked)
                    holder.bookmarkBtn.setImageDrawable(holder.bookmarkBtn.getResources().getDrawable(R.drawable.ic_checked));
                else
                    holder.bookmarkBtn.setImageDrawable(holder.bookmarkBtn.getResources().getDrawable(R.drawable.ic_bookmark_black));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("likes").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(event.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.liked = dataSnapshot.exists();
                if(holder.liked)
                    holder.likeBtn.setImageDrawable(holder.likeBtn.getResources().getDrawable(R.drawable.liked));
                else
                    holder.likeBtn.setImageDrawable(holder.likeBtn.getResources().getDrawable(R.drawable.notliked));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("events").child(event.getId()).child("likeCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.getValue(Integer.class) != null){
                    likeCounter = dataSnapshot.getValue(Integer.class);
                    holder.likeCount.setText(likeCounter + "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventId = dataSet.get(listPosition).getId();
                Intent i = new Intent(context, CommentActivity.class);
                i.putExtra("eventId", eventId);
                context.startActivity(i);
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                like(holder.liked,event.getId(),event.getHostName(),event.getHostId());
            }
        });

        holder.bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookmark(holder.bookmarked, event.getId());
            }
        });

        if (isBookmarked) {
            holder.mainImg.setVisibility(View.GONE);
        } else {
            // main image reference
            if(event.getImgUrl() != null) {
                StorageReference imageReference = storage.getReference().child(event.getImgUrl());
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context.getApplicationContext())
                                .load(uri)
                                .into(holder.mainImg);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(HomeActivity.TAG, exception.getMessage());
                    }
                });
            }
        }

        if(event.getType().equals("article")){
            holder.peopleCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(event.getType().equals("article")){
                    return;
                }
                Intent intent = new Intent(context, EventViewActivity.class);
                intent.putExtra("eventName", event.getEventName());
                intent.putExtra("hostName", event.getHostName());
                intent.putExtra("hostId", event.getHostId());
                intent.putExtra("location", event.getLocation());
                intent.putExtra("peopleCount", event.getPeopleCount());
                intent.putExtra("description", event.getDescription());
                intent.putExtra("mainImgUrl", event.getImgUrl());
                intent.putExtra("eventId", event.getId());
                intent.putExtra("userIds", event.getUserIds());
                intent.putExtra("date", event.getDate());
                intent.putExtra("venueId",event.getVenueId());

//                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, holder.eventName, "title");
                context.startActivity(intent);
            }
        });

        if(TextUtils.isEmpty(holder.description.getText())){
            holder.description.setVisibility(View.GONE);
        }

        if(TextUtils.isEmpty(holder.eventName.getText())){
            holder.eventName.setVisibility(View.GONE);
        }

        if(TextUtils.isEmpty(holder.date.getText())){
            holder.date.setVisibility(View.GONE);
        }

        if(event.getType().equals("event")){
            holder.description.setVisibility(View.GONE);
            holder.eventName.setVisibility(View.VISIBLE);
            holder.date.setVisibility(View.VISIBLE);
        }else{
            holder.description.setVisibility(View.VISIBLE);
        }
    }

    private void like(Boolean liked,String id, String hostName, String hostId){
        Calendar mCurrentTime = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String date = formatter.format(mCurrentTime.getTime());

        if(liked != null && liked){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("likes").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(id)
                    .removeValue();
            databaseReference.child("events").child(id).child("likeCount").setValue(likeCounter-1);
        }
        else{
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("likes").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(id)
                    .setValue(true);
            databaseReference.child("events").child(id).child("likeCount").setValue(likeCounter+1);
//            databaseReference.child("notifications").child(hostId).child(date)
//                    .setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + " has liked your event." + id);
        }
    }

    private void bookmark(Boolean bookmarked, String id) {
        if (bookmarked != null && bookmarked) {
            databaseReference.child("bookmarks").child(HomeActivity.userId).child(id).removeValue();
            Snackbar.make(layout, "Bookmark removed", Snackbar.LENGTH_SHORT).show();
        } else {
            databaseReference.child("bookmarks").child(HomeActivity.userId).child(id).setValue(true);
            Snackbar.make(layout, "Bookmark added", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
