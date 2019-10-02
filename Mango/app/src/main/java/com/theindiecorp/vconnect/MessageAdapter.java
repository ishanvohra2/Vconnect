package com.theindiecorp.vconnect;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.data.Event;
import com.theindiecorp.vconnect.data.Text;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Text> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Event e = new Event();

    public int setTexts(ArrayList<Text> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView date,textContent,eventName, postContent, hostName;
        private LinearLayout textBox, postBox;
        private ImageView postImage;
        private CircularImageView profileImg;

        public MyViewHolder(View view){
            super(view);
            this.date = view.findViewById(R.id.date);
            this.textContent = view.findViewById(R.id.text);
            this.textBox = view.findViewById(R.id.message_box);
            this.postBox = view.findViewById(R.id.post_layout);
            this.postContent = view.findViewById(R.id.main_item_description);
            this.postImage = view.findViewById(R.id.main_image);
            this.eventName = view.findViewById(R.id.main_item_event_name);
            this.hostName = view.findViewById(R.id.main_item_title);
            this.profileImg = view.findViewById(R.id.main_item_profile_pic);
        }
    }

    public MessageAdapter (Context context, ArrayList<Text> dataSet){
        this.context = context;
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        Text text = dataSet.get(listPosition);

        holder.textContent.setText(text.getContent());
        holder.date.setText(text.getDate() + " : " + text.getTime());

        if(text.getSentBy().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            holder.textContent.setBackground(context.getDrawable(R.drawable.sent_box));
            holder.textContent.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            holder.date.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        }
        else{
            holder.textContent.setBackground(context.getDrawable(R.drawable.received_box));
            holder.textContent.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.date.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        }

        if(text.getPost() != null){
            holder.textBox.setVisibility(View.GONE);
            holder.postBox.setVisibility(View.VISIBLE);

            databaseReference.child("events").child(text.getPostId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    e = dataSnapshot.getValue(Event.class);
                    holder.postContent.setText(e.getDescription());
                    if(e.getType().equals("event")){
                        holder.eventName.setText(e.getEventName());
                    }
                    else
                        holder.eventName.setVisibility(View.GONE);

                    StorageReference profileImageReference = storage.getReference().child("users/" + e.getHostId() + "/images/profile_pic/profile_pic.jpeg");
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

                    if(e.getImgUrl() != null) {
                        StorageReference imageReference = storage.getReference().child(e.getImgUrl());
                        imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(context.getApplicationContext())
                                        .load(uri)
                                        .into(holder.postImage);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.d(HomeActivity.TAG, exception.getMessage());
                            }
                        });
                    }

                    databaseReference.child("users").child(e.getHostId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            holder.hostName.setText(dataSnapshot.child("displayName").getValue(String.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else{
            holder.postBox.setVisibility(View.GONE);
            holder.textBox.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.date.getVisibility() == View.GONE){
                    holder.date.setVisibility(View.VISIBLE);
                }
                else{
                    holder.date.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
