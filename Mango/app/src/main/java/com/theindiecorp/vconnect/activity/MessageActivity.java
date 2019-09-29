package com.theindiecorp.vconnect.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.vconnect.MessageAdapter;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.data.Text;

import java.util.ArrayList;
import java.util.Calendar;

public class MessageActivity extends AppCompatActivity {

    private String receiverId;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        receiverId = getIntent().getStringExtra("userid");

        final EditText messageEt = findViewById(R.id.text_et);
        Button sendBtn = findViewById(R.id.sendBtn);
        final TextView toolbarTitle = findViewById(R.id.toolbar_title);

        final RecyclerView messageRecycler = findViewById(R.id.messages_recycler);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecycler.setLayoutManager(linearLayoutManager);

        final MessageAdapter messageAdapter = new MessageAdapter(this,new ArrayList<Text>());
        messageRecycler.setAdapter(messageAdapter);

        databaseReference.child("users").child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                toolbarTitle.setText(dataSnapshot.child("displayName").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(receiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<Text> texts = new ArrayList<>();
                        if(dataSnapshot.exists()){
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Text t = snapshot.getValue(Text.class);
                                databaseReference.child("messages").child(HomeActivity.userId).child(receiverId).child(snapshot.getKey()).child("read").setValue(true);
                                texts.add(t);
                            }
                        }
                        messageAdapter.setTexts(texts);
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(messageEt.getText())){
                    return;
                }
                sendMessage(messageEt.getText().toString());
                messageEt.setText("");
            }
        });


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(HomeActivity.userId).child("isOnline").setValue(true);
    }

    private void sendMessage(String toString) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String date = day + "/" + month + "/" + year;
        String time = hour + ":" + minute;

        Text text = new Text();
        text.setContent(toString);
        text.setDate(date);
        text.setTime(time);
        text.setSentBy(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String key = databaseReference.push().getKey();

        databaseReference.child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(receiverId).child(key).setValue(text);

        databaseReference.child("messages").child(receiverId)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key).setValue(text);


    }

    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(HomeActivity.userId).child("isOnline").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(HomeActivity.userId).child("isOnline").setValue(false);
    }
}
