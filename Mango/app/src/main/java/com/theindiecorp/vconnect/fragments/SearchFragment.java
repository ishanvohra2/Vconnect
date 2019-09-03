package com.theindiecorp.vconnect.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.theindiecorp.vconnect.AttendeeAdapter;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.data.Event;
import com.theindiecorp.vconnect.mainFeedRecyclerViewAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    mainFeedRecyclerViewAdapter adapter;
    AttendeeAdapter attendeeAdapter;

    ArrayList<Event> events = new ArrayList<>();
    ArrayList<String> userIds = new ArrayList<>();

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        final RecyclerView recyclerView = v.findViewById(R.id.main_feed_my_school_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new mainFeedRecyclerViewAdapter(new ArrayList<Event>(), getContext());
        recyclerView.setAdapter(adapter);
        final SearchView searchView = v.findViewById(R.id.main_feed_search_view);

        final RecyclerView userRecyclerView = v.findViewById(R.id.search_view_users_list);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        attendeeAdapter = new AttendeeAdapter(new ArrayList<String>(),getContext());
        userRecyclerView.setAdapter(attendeeAdapter);

        final Button showPosts,showUsers;

        showPosts = v.findViewById(R.id.search_view_post_btn);
        showUsers = v.findViewById(R.id.search_view_people_btn);

        showUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.GONE);
                userRecyclerView.setVisibility(View.VISIBLE);
                showPosts.setTextColor(getResources().getColor(android.R.color.black));
                showUsers.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        });

        showPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.VISIBLE);
                userRecyclerView.setVisibility(View.GONE);
                showUsers.setTextColor(getResources().getColor(android.R.color.black));
                showPosts.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        });

        searchView.setQueryHint("Search");
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Query q = FirebaseDatabase.getInstance().getReference("events")
                        .orderByChild("eventName")
                        .startAt(query)
                        .endAt(query + "\uf8ff");
                q.addValueEventListener(valueEventListener);

                Query q1 = FirebaseDatabase.getInstance().getReference("users")
                        .orderByChild("username")
                        .startAt(query)
                        .endAt(query + "\uf8ff");
                q1.addValueEventListener(userEventListener);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Query q = FirebaseDatabase.getInstance().getReference("events")
                        .orderByChild("eventName")
                        .startAt(newText)
                        .endAt(newText + "\uf8ff");
                q.addListenerForSingleValueEvent(valueEventListener);

                Query q1 = FirebaseDatabase.getInstance().getReference("users")
                        .orderByChild("username")
                        .startAt(newText)
                        .endAt(newText + "\uf8ff");
                q1.addValueEventListener(userEventListener);
                return false;
            }
        });

        if(userIds.isEmpty()){
            databaseReference.child("users").addValueEventListener(userEventListener);
        }

        if(events.isEmpty()){
            databaseReference.child("events").addValueEventListener(valueEventListener);
        }

        return v;
    }
    ValueEventListener userEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            userIds = new ArrayList<>();
            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                String userId = snapshot.getKey();
                userIds.add(userId);
            }
            attendeeAdapter.setIds(userIds);
            attendeeAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            events = new ArrayList<>();
            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                Event event = eventSnapshot.getValue(Event.class);
                event.setId(eventSnapshot.getKey());
                if(!eventSnapshot.child("isPrivate").exists()){
                    events.add(event);
                }
            }
            adapter.setEvents(events);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
