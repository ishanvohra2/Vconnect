package com.theindiecorp.mango.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.theindiecorp.mango.data.Event;
import com.theindiecorp.mango.R;
import com.theindiecorp.mango.mainFeedRecyclerViewAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookmarksActivity extends AppCompatActivity {
    DatabaseReference databaseReference;

    ArrayList<Event> events = new ArrayList<>();
    mainFeedRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getString(R.string.bookmarked_events));

        RecyclerView recyclerView = findViewById(R.id.main_feed_my_school_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        adapter = new mainFeedRecyclerViewAdapter(events, getApplicationContext());
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("bookmarks").child(HomeActivity.userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events = new ArrayList<>();
                for (DataSnapshot eventId : dataSnapshot.getChildren()) {
                    final String id = eventId.getKey();
                    databaseReference.child("events").child(id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Event event = dataSnapshot.getValue(Event.class);
                            event.setId(id);
                            events.add(event);
                            adapter.setEvents(events);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}
