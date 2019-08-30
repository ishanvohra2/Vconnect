package com.theindiecorp.vconnect.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.vconnect.FollowersAdapter;
import com.theindiecorp.vconnect.InboxItemAdapter;
import com.theindiecorp.vconnect.R;

import java.util.ArrayList;
import java.util.Iterator;

public class InboxActivity extends AppCompatActivity {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    ArrayList<String> userIds = new ArrayList<>();
    ArrayList<String> activeIds = new ArrayList<>();
    private SearchView searchView;

    InboxItemAdapter inboxItemAdapter;
    FollowersAdapter followersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        LinearLayout onlineBox = findViewById(R.id.online_box);

        RecyclerView inboxRecycler = findViewById(R.id.recycler_view);
        inboxRecycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true));

        RecyclerView onlineRecycler = findViewById(R.id.members_online_recycler);
        onlineRecycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        inboxItemAdapter = new InboxItemAdapter(this,new ArrayList<String>());
        inboxRecycler.setAdapter(inboxItemAdapter);

        followersAdapter = new FollowersAdapter(activeIds,this);
        onlineRecycler.setAdapter(followersAdapter);

        databaseReference.child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(valueEventListener);

        if(userIds.isEmpty()){
            databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followers")
                    .addValueEventListener(valueEventListener);
        }

        searchView = findViewById(R.id.main_feed_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Query q = FirebaseDatabase.getInstance().getReference("users")
                        .orderByChild("displayName")
                        .startAt(query)
                        .endAt(query + "\uf8ff");
                q.addValueEventListener(valueEventListener);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Query q = FirebaseDatabase.getInstance().getReference("users")
                        .orderByChild("displayName")
                        .startAt(newText)
                        .endAt(newText + "\uf8ff");
                q.addValueEventListener(valueEventListener);
                return false;
            }
        });

        if(!userIds.isEmpty()){
            final Iterator i = userIds.iterator();
            while (i.hasNext()){
                databaseReference.child("users").child(i.next().toString()).child("isOnline").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.getValue(Boolean.class)){
                            activeIds.add(i.next().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            followersAdapter.setIds(activeIds);
            followersAdapter.notifyDataSetChanged();
        }

        if(activeIds.isEmpty()){
            onlineBox.setVisibility(View.GONE);
        }
        else
            onlineBox.setVisibility(View.VISIBLE);


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(HomeActivity.userId).child("isOnline").setValue(true);
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            userIds = new ArrayList<>();
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    userIds.add(snapshot.getKey());
                }
                inboxItemAdapter.setMessageId(userIds);
                inboxItemAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

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
