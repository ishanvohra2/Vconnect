package com.theindiecorp.vconnect.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.SendPostAdapter;

import java.util.ArrayList;

public class SendPostActivity extends AppCompatActivity {

    private String postId;
    private ArrayList<String> followingIds = new ArrayList<>();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    SendPostAdapter sendPostAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_post_fragment);

        postId = getIntent().getStringExtra("postId");

        final SearchView searchView = findViewById(R.id.search_view);

        RecyclerView recyclerView = findViewById(R.id.send_post_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sendPostAdapter = new SendPostAdapter(this, new ArrayList<String>());
        recyclerView.setAdapter(sendPostAdapter);

        sendPostAdapter.setPostId(postId);

        databaseReference.child("users").child(HomeActivity.userId).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        followingIds.add(snapshot.getKey());
                    }
                    sendPostAdapter.setDataSet(followingIds);
                    sendPostAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryTxt) {
                Query q = FirebaseDatabase.getInstance().getReference("users")
                        .orderByChild("displayName")
                        .startAt(queryTxt)
                        .endAt(queryTxt + "\uf8ff");
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
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()) {
                followingIds = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingIds.add(snapshot.getKey());
                }
                sendPostAdapter.setDataSet(followingIds);
                sendPostAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
