package com.theindiecorp.mango.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.theindiecorp.mango.fragments.NewPostFragment;
import com.theindiecorp.mango.fragments.MainFeedFragment;
import com.theindiecorp.mango.fragments.ProfileFragment;
import com.theindiecorp.mango.R;
import com.theindiecorp.mango.fragments.SearchFragment;
import com.theindiecorp.mango.fragments.NotificationsFragment;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    public static final String TAG = "stupid"; // TODO
    public static String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    public static String userEmail;


    private static final String USER_ID = "USER_ID";
    private static final String EDITABLE = "EDITABLE";

    Toolbar toolbar;
    BottomNavigationView navigationView;


    private void loadFragment(Fragment fragment) {
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

        // added from here


        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(listener);
        loadFragment(new MainFeedFragment());

        // ended here
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
}
