package com.theindiecorp.mango.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.mango.CommentsAdapter;
import com.theindiecorp.mango.R;
import com.theindiecorp.mango.data.Comment;

import java.util.ArrayList;

public class CommentActivity extends AppCompatActivity {

    private static final String TAG = "CommentActivity";

    ArrayList<Comment> commentsList = new ArrayList<>();
    RecyclerView commentsRecyclerView;
    EditText commentEditText;
    ImageView sendCommentBtn;
    ImageView noCommentsIllustration;
    TextView noCommentsText;
    String eventId;

    public final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Comments");
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.white)));

        commentsRecyclerView = findViewById(R.id.comments_recycler);
        commentEditText = findViewById(R.id.comment_edit_text);
        sendCommentBtn = findViewById(R.id.send_comment);
        noCommentsIllustration = findViewById(R.id.no_comments_image);
        noCommentsText = findViewById(R.id.no_comments_text);

        Glide.with(this).load(R.drawable.paper_plane).into(sendCommentBtn);
        Glide.with(this).load(R.drawable.no_comments_illustration).into(noCommentsIllustration);

        Intent intent = getIntent();
        String eventIdFromIntent = "";
        if (intent.hasExtra("intentType")) {
            if (intent.getStringExtra("intentType").equals("startActivityFromNotification")) {
                eventIdFromIntent = intent.getStringExtra("link");
            } else {
                eventIdFromIntent = intent.getStringExtra("eventId");
            }
        } else {
            eventIdFromIntent = intent.getStringExtra("eventId");
        }

        eventId = eventIdFromIntent;
        Log.e("eventId: ", eventId);

        final CommentsAdapter commentsAdapter = new CommentsAdapter(commentsList, this);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        databaseReference.child("events").child(eventId).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Refresh the comments
                commentsList.clear();

                //Fetch comments from database
                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    String userId = commentSnapshot.child("userId").getValue(String.class);
                    String message = commentSnapshot.child("message").getValue(String.class);

                    Comment comment = new Comment(userId, message, null);
                    commentsList.add(comment);
                }

                if (commentsList.size() == 0) {
                    commentsRecyclerView.setVisibility(View.GONE);
                    noCommentsIllustration.setVisibility(View.VISIBLE);
                    noCommentsText.setVisibility(View.VISIBLE);
                } else {
                    commentsRecyclerView.setVisibility(View.VISIBLE);
                    noCommentsIllustration.setVisibility(View.GONE);
                    noCommentsText.setVisibility(View.GONE);
                    commentsAdapter.setComments(commentsList);
                    commentsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //Push comments
        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = commentEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(message)) {
                    Comment comment = new Comment(userId, message, null);
                    databaseReference.child("events").child(eventId).child("comments")
                            .push()
                            .setValue(comment);

                    //Clear the comment edit text
                    commentEditText.setText("");
                }
            }
        });

    }
}
