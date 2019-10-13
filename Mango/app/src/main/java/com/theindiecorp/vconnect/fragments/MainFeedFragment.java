package com.theindiecorp.vconnect.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.theindiecorp.vconnect.HighlightsAdapter;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.activity.AddGroupActivity;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.activity.InboxActivity;
import com.theindiecorp.vconnect.activity.NewArticleActivity;
import com.theindiecorp.vconnect.activity.NewEventActivity;
import com.theindiecorp.vconnect.data.Event;
import com.theindiecorp.vconnect.data.Group;
import com.theindiecorp.vconnect.data.Highlight;
import com.theindiecorp.vconnect.mainFeedRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class MainFeedFragment extends Fragment {

    String userId,userEmail;
    ArrayList<String> followingUserIds = new ArrayList<>();
    public static ArrayList<Group> groups = new ArrayList<>();

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
        TextView newGroupBtn = view.findViewById(R.id.new_group);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        userId  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        String eventIdArgument = "";
        if(getArguments() !=null && !getArguments().isEmpty()) {
            eventIdArgument = getArguments().getString("eventId");
        }
        final String eventId = eventIdArgument;

        //posts
        final RecyclerView recyclerView = view.findViewById(R.id.main_feed_my_school_recycler_view);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);

        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.setNestedScrollingEnabled(false);

        ArrayList<Event> events = new ArrayList<>();

        LinearLayout layout = view.findViewById(R.id.main_feed_main_layout);

        final mainFeedRecyclerViewAdapter adapter = new mainFeedRecyclerViewAdapter(events, getContext(), layout);
        recyclerView.setAdapter(adapter);

        RecyclerView highlightsRecycler = view.findViewById(R.id.main_feed_highlights_recycler_view);
        highlightsRecycler.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        final HighlightsAdapter highlightsAdapter = new HighlightsAdapter(getContext(),new ArrayList<Group>());
        highlightsRecycler.setAdapter(highlightsAdapter);

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
                Event EventFromNotification = new Event();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    final Event event = eventSnapshot.getValue(Event.class);
                    event.setId(eventSnapshot.getKey());
                    if(!eventSnapshot.child("isPrivate").exists()){
                        if(followingUserIds.contains(event.getHostId()) || event.getHostId().equals(userId)){
                            events.add(event);
                        }
                        if(eventSnapshot.getKey().equals(eventId)){
                            EventFromNotification = event;
                        }
                    }
                }
                Collections.reverse(events);
                adapter.setEvents(events);
                adapter.notifyDataSetChanged();

                if(!TextUtils.isEmpty(eventId) && EventFromNotification != null){
                    recyclerView.scrollToPosition(events.indexOf(EventFromNotification));
                }
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

        Button messageBtn = view.findViewById(R.id.message_btn);
        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), InboxActivity.class));
            }
        });

        databaseReference.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Group group = new Group();
                    group = snapshot.getValue(Group.class);
                    group.setId(snapshot.getKey());
                    if(group.getMembers().contains(HomeActivity.userId)){
                        groups.add(group);
                    }
                }
                highlightsAdapter.setHighlights(groups);
                highlightsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        newGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AddGroupActivity.class));
            }
        });

        return view;
    }
}
