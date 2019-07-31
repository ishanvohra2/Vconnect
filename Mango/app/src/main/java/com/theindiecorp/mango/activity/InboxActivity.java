package com.theindiecorp.mango.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.mango.InboxItemAdapter;
import com.theindiecorp.mango.R;

import java.util.ArrayList;

public class InboxActivity extends AppCompatActivity {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        RecyclerView inboxRecycler = findViewById(R.id.recycler_view);
        inboxRecycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true));

        InboxItemAdapter inboxItemAdapter = new InboxItemAdapter(this,new ArrayList<String>());
        inboxRecycler.setAdapter(inboxItemAdapter);

        databaseReference.child("messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
