package com.theindiecorp.mango.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.theindiecorp.mango.FollowedSchoolsAdapter;
import com.theindiecorp.mango.FollowersAdapter;
import com.theindiecorp.mango.R;
import com.theindiecorp.mango.activity.NewArticleActivity;
import com.theindiecorp.mango.activity.NewEventActivity;
import com.theindiecorp.mango.data.Event;
import com.theindiecorp.mango.data.User;
import com.theindiecorp.mango.mainFeedRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MainFeedFragment extends Fragment {

    String userId,userEmail;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    ArrayList<String> followingUserIds = new ArrayList<>();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public MainFeedFragment() {
        // Required empty public constructor
    }


    public static MainFeedFragment newInstance(String param1, String param2) {
        MainFeedFragment fragment = new MainFeedFragment();
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
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main_feed, container, false);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        userId  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        //posts
        RecyclerView recyclerView = view.findViewById(R.id.main_feed_my_school_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));

        ArrayList<Event> events = new ArrayList<>();

        CoordinatorLayout layout = view.findViewById(R.id.main_feed_main_layout);

        final mainFeedRecyclerViewAdapter adapter = new mainFeedRecyclerViewAdapter(events, getContext(), layout);
        recyclerView.setAdapter(adapter);

        databaseReference.child("users").child(userId).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingUserIds = new ArrayList<>();
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        followingUserIds.add(snapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference eventReference = databaseReference.child("events");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Event> events = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    final Event event = eventSnapshot.getValue(Event.class);
                    event.setId(eventSnapshot.getKey());
                    if(followingUserIds.contains(event.getHostId()) || event.getHostId().equals(userId)){
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

        eventReference.addValueEventListener(postListener);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Category");
                builder.setCancelable(true);
                builder.setPositiveButton("Event", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getActivity(), NewEventActivity.class));
                    }
                });
                builder.setNegativeButton("Article", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getActivity(), NewArticleActivity.class));
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return view;
    }


}
