package com.theindiecorp.mango.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.mango.R;
import com.theindiecorp.mango.data.Event;
import com.theindiecorp.mango.mainFeedRecyclerViewAdapter;

import java.util.ArrayList;

public class BookmarkFragment extends Fragment {

    int size;
    DatabaseReference databaseReference;
    mainFeedRecyclerViewAdapter adapter;
    ArrayList<Event> events = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookmark_fragment,container,false);
        final LinearLayout waterMark = view.findViewById(R.id.watermark);

        RecyclerView recyclerView = view.findViewById(R.id.bookmarks_my_school_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        CoordinatorLayout frameLayout = view.findViewById(R.id.bookmark_main_layout);
        adapter = new mainFeedRecyclerViewAdapter(events, getContext(), true, frameLayout,true);
        recyclerView.setAdapter(adapter);

        final LinearLayout watermark = view.findViewById(R.id.watermark);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("bookmarks").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
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
                                    size = adapter.setEvents(events);
                                    adapter.notifyDataSetChanged();

                                    if(events.isEmpty()){
                                        waterMark.setVisibility(View.VISIBLE);
                                    }
                                    else
                                        waterMark.setVisibility(View.GONE);
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

        return view;
    }
}
