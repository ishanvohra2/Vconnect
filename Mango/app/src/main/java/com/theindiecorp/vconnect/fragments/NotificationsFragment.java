package com.theindiecorp.vconnect.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.vconnect.NotificationsAdapter;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.data.Notification;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    ArrayList<Notification> notificationsList = new ArrayList<>();
    RecyclerView notificationsRecyclerView;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    public static NotificationsFragment newInstance(String param1, String param2) {
        NotificationsFragment fragment = new NotificationsFragment();
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
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        final NotificationsAdapter notificationsAdapter = new NotificationsAdapter(notificationsList, getContext());
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        notificationsRecyclerView.setAdapter(notificationsAdapter);

        notificationsList.clear();
        databaseReference.child("users").child(userId).child("notifications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    notification.setId(snapshot.getKey());
                    databaseReference.child("users").child(userId).child("notifications").child(notification.getId()).child("read").setValue(true);
                    notificationsList.add(notification);
                }

                //Reverse the list to show the latest notifications first
                Collections.reverse(notificationsList);
                notificationsAdapter.setNotifications(notificationsList);
                notificationsAdapter.notifyDataSetChanged();
                Log.e("size", String.valueOf(notificationsList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

}
