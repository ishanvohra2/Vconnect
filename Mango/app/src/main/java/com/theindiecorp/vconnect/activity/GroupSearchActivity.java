package com.theindiecorp.vconnect.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.vconnect.GroupSearchItemRecycler;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.data.Group;

import java.util.ArrayList;

public class GroupSearchActivity extends AppCompatActivity {
    SearchView searchView;
    RecyclerView recyclerView;

    GroupSearchItemRecycler groupAdapter;

    String type;
    ArrayList<String> groupIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_search);

        type = getIntent().getStringExtra("type");

        searchView = findViewById(R.id.article_tag);
        recyclerView = findViewById(R.id.group_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupAdapter = new GroupSearchItemRecycler(this,new ArrayList<String>(),type);
        recyclerView.setAdapter(groupAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {
                Query query = FirebaseDatabase.getInstance().getReference("groups")
                        .orderByChild("name").startAt(queryText).endAt(queryText + "\uf8ff");
                query.addValueEventListener(groupListener);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Query query = FirebaseDatabase.getInstance().getReference("groups")
                        .orderByChild("name").startAt(newText).endAt(newText + "\uf8ff");
                query.addValueEventListener(groupListener);

                return false;
            }
        });

        if(groupIds.isEmpty()){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("groups");
            databaseReference.addValueEventListener(groupListener);
        }
    }

    ValueEventListener groupListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            groupIds = new ArrayList<>();
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Group group = snapshot.getValue(Group.class);
                    if(group.getAdminId().equals(HomeActivity.userId))
                        groupIds.add(group.getId());
                }
                groupAdapter.setGroupIds(groupIds);
                groupAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
