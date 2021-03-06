package com.theindiecorp.vconnect.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.theindiecorp.vconnect.fragments.NewEventFragment;
import com.theindiecorp.vconnect.fragments.NewPostFragment;
import com.theindiecorp.vconnect.fragments.MainFeedFragment;
import com.theindiecorp.vconnect.fragments.ProfileFragment;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.fragments.SearchFragment;
import com.theindiecorp.vconnect.fragments.NotificationsFragment;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    public static final String TAG = "stupid"; // TODO
    public static String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    public static int points;
    public static String postType = "";
    public static String groupId = "";
    String eventIdFromIntent = "";


    private static final String USER_ID = "USER_ID";
    private static final String EDITABLE = "EDITABLE";

    Toolbar toolbar;
    BottomNavigationView navigationView;


    public void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_frame_container, fragment);
        transaction.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener listener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment fragment;
            switch (menuItem.getItemId()) {
                case R.id.nav_home:
//                    toolbar.setTitle("Home");
                    fragment = new MainFeedFragment();
                    if(!TextUtils.isEmpty(eventIdFromIntent)){
                        Bundle args = new Bundle();
                        args.putString("eventId", eventIdFromIntent);
                        fragment.setArguments(args);
                    }
                    loadFragment(fragment);
                    return true;
                case R.id.nav_search:
//                    toolbar.setTitle("Explore");
                    fragment = new SearchFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.nav_new_post:
//                    toolbar.setTitle("Bookmarks");
                    fragment = new NewPostFragment()  ;
                    loadFragment(fragment);
                    return true;
                case R.id.nav_profile:
//                    toolbar.setTitle("Profile");
                    fragment = ProfileFragment.newInstance(HomeActivity.userId, true);
                    loadFragment(fragment);
                    return true;
                case R.id.nav_your_events:
//                    toolbar.setTitle("Your Events");
                    fragment = new NotificationsFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initFCM();

        Intent intent = getIntent();
        if (intent.hasExtra("intentType")) {
            if (intent.getStringExtra("intentType").equals("startActivityFromNotification")) {
                eventIdFromIntent = intent.getStringExtra("link");
            }
        }

        // added from here


        if(groupId.isEmpty() || postType.isEmpty()){
            loadFragment(new MainFeedFragment());
        }
        else if(postType.equals("post")){
            loadFragment(new NewPostFragment());
        }
        else if(postType.equals("post")){
            loadFragment(new NewEventFragment());
        }


        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(listener);

        // ended here

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(HomeActivity.userId).child("isOnline").setValue(true);

        databaseReference.child("users").child(HomeActivity.userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("points").exists()){
                    points = dataSnapshot.child("points").getValue(Integer.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("messagingToken")
                .setValue(token);
    }


    private void initFCM(){
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "initFCM: token: " + token);
        sendRegistrationToServer(token);

    }

    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(userId).child("isOnline").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(HomeActivity.userId).child("isOnline").setValue(false);
    }

}
