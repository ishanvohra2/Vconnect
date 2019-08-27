package com.theindiecorp.vconnect.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.theindiecorp.vconnect.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventViewActivity extends AppCompatActivity {
    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    final DatabaseReference eventIdsReference = databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("eventIds");

    Boolean joined;

    int peopleCount;

    public static void joinEvent(String eventId, String userId, DatabaseReference databaseReference) {
        databaseReference.child("attendees").child(eventId).child(userId).setValue(true);
        databaseReference.child("eventsAttendedByUsers").child(userId).child(eventId).setValue(true);
    }

    public static void exitEvent(String eventId, String userId, DatabaseReference databaseReference) {
        databaseReference.child("attendees").child(eventId).child(userId).removeValue();
        databaseReference.child("eventsAttendedByUsers").child(userId).child(eventId).removeValue();
    }

    void openProfile(String hostId){
        Intent intent = new Intent(EventViewActivity.this, ProfileViewActivity.class);
        intent.putExtra("userId", hostId);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        final String eventName = getIntent().getStringExtra("eventName");
        String hostName = getIntent().getStringExtra("hostName");
        String location = getIntent().getStringExtra("location");
        String groupName = getIntent().getStringExtra("groupName");
        String description = getIntent().getStringExtra("description");
        String mainImgUrl = getIntent().getStringExtra("mainImgUrl");
        final String venueId = getIntent().getStringExtra("venueId");
        final String eventId = getIntent().getStringExtra("eventId");
        final String hostId = getIntent().getStringExtra("hostId");
        final ArrayList<String> userIds = getIntent().getStringArrayListExtra("userIds");
        Date date = new Date();
        date.setTime(getIntent().getLongExtra("date", 0L));


        TextView eventNameTv = findViewById(R.id.event_view_event_name);
        TextView hostNameTv = findViewById(R.id.event_view_title);
        TextView hostedByTv = findViewById(R.id.event_view_host_by_tv);
        final TextView descriptionTv = findViewById(R.id.event_view_description);
        final TextView readMoreTv = findViewById(R.id.event_view_read_more_tv);
        final TextView peopleCountTv = findViewById(R.id.event_view_people_count);
        final ImageView mainImg = findViewById(R.id.new_event_main_image);
        TextView dateTv = findViewById(R.id.event_view_time);
        final ImageView profileImg = findViewById(R.id.event_view_profile_pic);
        Button messageBtn = findViewById(R.id.event_view_message_btn);
        final TextView venue = findViewById(R.id.event_view_location);
        LinearLayout hostNameLayout = findViewById(R.id.host_name_layout);

        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference();
        postReference.child("schools").child(venueId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                venue.setText(dataSnapshot.child("name").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        venue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EventViewActivity.this,SchoolViewActivity.class).putExtra("schoolId",venueId));
            }
        });

        eventNameTv.setText(eventName);
        hostNameTv.setText(hostName);
        hostedByTv.setText("Hosted by " + hostName);
        descriptionTv.setText(description);

        hostedByTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile(hostId);
            }
        });

        hostNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile(hostId);
            }
        });

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd MMMM\nhh:mm a");
        dateTv.setText(simpleDateFormat.format(date));

        peopleCountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventViewActivity.this, AttendeeViewActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            }
        });

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EventViewActivity.this, MessageActivity.class));

            }
        });


        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference().child(mainImgUrl).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext())
                        .load(uri)
                        .into(mainImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(EventViewActivity.this, "Not able to load images, please check you network connection and restart the app", Toast.LENGTH_SHORT).show();
            }
        });

        storage.getReference().child(getProfileImageUrl(hostId)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext())
                        .load(uri)
                        .into(profileImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(EventViewActivity.this, "Not able to load images, please check you network connection and restart the app", Toast.LENGTH_SHORT).show();
            }
        });

        final Button joinBtn = findViewById(R.id.event_view_join_btn);

        databaseReference.child("events").child(eventId).child("peopleCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                peopleCount = dataSnapshot.getValue(Integer.class);
                peopleCountTv.setText(peopleCount + " people are going currently");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        readMoreTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descriptionTv.setMaxLines(100);
                readMoreTv.setVisibility(View.GONE);
            }
        });


        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                joined = dataSnapshot.exists();
                if (joined) {
                    joinBtn.setText(R.string.exit);
                    joinBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    joinBtn.setText(R.string.join);
                    joinBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        databaseReference.child("eventsAttendedByUsers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(eventId).addValueEventListener(listener);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                join(joined, eventId, peopleCount, databaseReference, eventName, hostId);
            }
        });
    }


    public static void join(Boolean joined, String eventId, int peopleCount, DatabaseReference databaseReference, String eventName, String hostId){
        Calendar mCurrentTime = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String date = formatter.format(mCurrentTime.getTime());

        if (joined != null && joined) {
            exitEvent(eventId, FirebaseAuth.getInstance().getCurrentUser().getUid(), databaseReference);
            databaseReference.child("events").child(eventId).child("peopleCount").setValue(peopleCount - 1);
        } else {
            joinEvent(eventId, FirebaseAuth.getInstance().getCurrentUser().getUid(), databaseReference);
            databaseReference.child("events").child(eventId).child("peopleCount").setValue(peopleCount + 1);
//            databaseReference.child("notifications").child(hostId).child(date)
//                    .setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName() + " has joined your event." + eventId);
        }
    }


    public static String getProfileImageUrl(String id) {
        return "users/" + id + "/images/profile_pic/profile_pic.jpeg";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
