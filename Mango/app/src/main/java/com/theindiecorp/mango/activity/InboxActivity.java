package com.theindiecorp.mango.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.mango.InboxItemAdapter;
import com.theindiecorp.mango.R;

import java.util.ArrayList;

public class InboxActivity extends AppCompatActivity {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    ArrayList<String> userIds = new ArrayList<>();
    private SearchView searchView;

    InboxItemAdapter inboxItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        RecyclerView inboxRecycler = findViewById(R.id.recycler_view);
        inboxRecycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true));

        inboxItemAdapter = new InboxItemAdapter(this,new ArrayList<String>());
        inboxRecycler.setAdapter(inboxItemAdapter);

        databaseReference.child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(valueEventListener);

        if(userIds.isEmpty()){
            databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("followers").addValueEventListener(new ValueEventListener() {
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
            });
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
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            userIds = new ArrayList<>();
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(!snapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
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
}
