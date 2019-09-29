package com.theindiecorp.vconnect;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.theindiecorp.vconnect.data.Text;

import java.util.ArrayList;
import java.util.Calendar;

public class SendPostAdapter extends RecyclerView.Adapter<SendPostAdapter.MyViewHolder> {

    private Context context;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private ArrayList<String> dataSet;
    private String postId;

    public int setDataSet(ArrayList<String> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private CircularImageView profileImg;
        private Button sendPostBtn;

        public MyViewHolder(View itemView){
            super(itemView);
            this.sendPostBtn = itemView.findViewById(R.id.send_btn);
            this.name = itemView.findViewById(R.id.attendee_item_title);
            this.profileImg = itemView.findViewById(R.id.attendee_item_image);
        }
    }

    public SendPostAdapter (Context context, ArrayList<String> dataSet){
        this.dataSet = dataSet;
        this.context = context;
    }

    public void setPostId(String postId){
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.send_post_recycler_item,parent,false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final String userId = dataSet.get(listPosition);

        // profile image reference
        StorageReference profileImageReference = storage.getReference().child("users/" + userId + "/images/profile_pic/profile_pic.jpeg");
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

        databaseReference.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.name.setText(dataSnapshot.child("displayName").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.sendPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH) + 1;
                int year = calendar.get(Calendar.YEAR);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                String date = day + "/" + month + "/" + year;
                String time = hour + ":" + minute;

                Text t = new Text();
                t.setPostId(postId);
                t.setDate(date);
                t.setTime(time);
                t.setSentBy(HomeActivity.userId);
                t.setPost(true);

                String id = databaseReference.push().getKey();

                databaseReference.child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(userId).child(id).setValue(t);
                databaseReference.child("messages").child(userId)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(id).setValue(t);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
