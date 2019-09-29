package com.theindiecorp.vconnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.activity.InboxActivity;
import com.theindiecorp.vconnect.data.Highlight;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BitmapTransformation;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class HighlightsAdapter extends RecyclerView.Adapter<HighlightsAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<Highlight> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public int setHighlights(ArrayList<Highlight> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView title,content;
        private ImageView imageView;

        public MyViewHolder(View itemView){
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.content = itemView.findViewById(R.id.content);
            this.imageView = itemView.findViewById(R.id.image);
        }
    }

    public HighlightsAdapter(Context context, ArrayList<Highlight> dataSet){
        this.context = context;
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.highlights_item,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final Highlight highlight = dataSet.get(listPosition);

        holder.title.setText(highlight.getTitle());
        holder.content.setText(highlight.getContent());

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(highlight.getType().equals("Message") && highlight.getType() != null){
//                    context.startActivity(new Intent(context, InboxActivity.class));
//                }
//            }
//        });

        if(highlight.getUrl()!=null){
            final BlurTransformation bitmapTransform = new BlurTransformation();
            StorageReference imageReference = storage.getReference().child(highlight.getUrl());
            imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context.getApplicationContext())
                            .load(uri)
                            .transform(new BlurTransformation(9))
                            .into(holder.imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(HomeActivity.TAG, exception.getMessage());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
