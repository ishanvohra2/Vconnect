package com.theindiecorp.vconnect;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.data.Text;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Text> dataSet;

    public int setTexts(ArrayList<Text> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView date,textContent, postTitle, postContent;
        private LinearLayout textBox, postBox;
        private ImageView postImage;
        private CircularImageView profileImg;

        public MyViewHolder(View view){
            super(view);
            this.date = view.findViewById(R.id.date);
            this.textContent = view.findViewById(R.id.text);
            this.textBox = view.findViewById(R.id.text_box);
            this.postBox = view.findViewById(R.id.post_box);
            this.postTitle = view.findViewById(R.id.main_item_title);
            this.postContent = view.findViewById(R.id.main_item_description);
            this.postImage = view.findViewById(R.id.main_item_main_image);
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
        }
        else{
            holder.postBox.setVisibility(View.VISIBLE);
            holder.textBox.setVisibility(View.GONE);
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
