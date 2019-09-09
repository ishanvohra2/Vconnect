package com.theindiecorp.vconnect.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.data.Event;
import com.theindiecorp.vconnect.mainFeedRecyclerViewAdapter;

import java.util.ArrayList;

public class HidePostsFragment extends Fragment {

    ArrayList<Event> events = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hide_posts_fragment,container,false);

        RecyclerView recyclerView = view.findViewById(R.id.bookmarks_my_school_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        final mainFeedRecyclerViewAdapter adapter = new mainFeedRecyclerViewAdapter(new ArrayList<Event>(), getContext());
        recyclerView.setAdapter(adapter);

        Query query = FirebaseDatabase.getInstance().getReference("events");
        query.orderByChild("hostId").equalTo(HomeActivity.userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot eventSnapshot : dataSnapshot.getChildren()){
                    Event event = eventSnapshot.getValue(Event.class);
                    event.setId(eventSnapshot.getKey());
                    if(eventSnapshot.child("isPrivate").exists()){
                        events.add(event);
                    }
                }
                adapter.setEvents(events);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
}
