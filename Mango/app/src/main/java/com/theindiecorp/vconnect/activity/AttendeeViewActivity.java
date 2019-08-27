package com.theindiecorp.vconnect.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.theindiecorp.vconnect.AttendeeAdapter;
import com.theindiecorp.vconnect.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AttendeeViewActivity extends AppCompatActivity {


    AttendeeAdapter adapter;
    ArrayList<String> userIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee_view);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.attendee_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        String eventId = getIntent().getStringExtra("eventId");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        userIds = new ArrayList<>();
        adapter = new AttendeeAdapter(userIds, this);
        recyclerView.setAdapter(adapter);
        databaseReference.child("attendees").child(eventId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userIds = new ArrayList<>();
                for(DataSnapshot userId : dataSnapshot.getChildren()){
                    userIds.add(userId.getKey());
                }
                adapter.setIds(userIds);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
