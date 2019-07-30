package com.theindiecorp.mango.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.theindiecorp.mango.FollowedSchoolsAdapter;
import com.theindiecorp.mango.FollowersAdapter;
import com.theindiecorp.mango.R;
import com.theindiecorp.mango.activity.HomeActivity;
import com.theindiecorp.mango.activity.LoginActivity;
import com.theindiecorp.mango.activity.NewArticleActivity;
import com.theindiecorp.mango.activity.NewEventActivity;
import com.theindiecorp.mango.activity.ProfileEditActivity;
import com.theindiecorp.mango.activity.ResetActivity;
import com.theindiecorp.mango.data.Event;
import com.theindiecorp.mango.data.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theindiecorp.mango.mainFeedRecyclerViewAdapter;

import java.util.ArrayList;


public class ProfileFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER_ID = "USER_ID";
    private static final String EDITABLE = "EDITABLE";
    ArrayList<String> followerIds;


    private String userId;
    private Boolean editable;

    public ProfileFragment() {
        // Required empty public constructor
    }

    CircularImageView profilePhotoImg;

    TextView bioTv, displayNameTv, sexTv;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    public static ProfileFragment newInstance(String userId, Boolean editable) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        args.putBoolean(EDITABLE, editable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID);
            editable = getArguments().getBoolean(EDITABLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePhotoImg = v.findViewById(R.id.profile_photo);
        Button signOutBtn = v.findViewById(R.id.profile_sign_out_btn);
        Button forgotPasswordBtn = v.findViewById(R.id.profile_reset_btn);
        final TextView followingCount = v.findViewById(R.id.my_profile_followers_count_tv);
        final TextView postCount = v.findViewById(R.id.my_profile_post_count_tv);
        final LinearLayout followersBox = v.findViewById(R.id.followers_box);
        final TextView followerText = v.findViewById(R.id.my_profile_following_count_tv);

        dl = (DrawerLayout) v.findViewById(R.id.activity_main);
        t = new ActionBarDrawerToggle(getActivity(), dl, R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        nv = (NavigationView) v.findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.saved_posts) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.home_frame_container, new BookmarkFragment());
                    transaction.commit();
                } else if (id == R.id.logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getContext(), LoginActivity.class));
                } else if (id == R.id.forgot_your_password) {
                    startActivity(new Intent(getContext(), ResetActivity.class));
                }
                return true;
            }
        });

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        RecyclerView recyclerView = v.findViewById(R.id.my_profile_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Button editProfileBtn = v.findViewById(R.id.profile_edit_porfile_btn);
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton fab = v.findViewById(R.id.fab);
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

        bioTv = v.findViewById(R.id.profile_description_tv);
        displayNameTv = v.findViewById(R.id.profile_display_name_tv);
        sexTv = v.findViewById(R.id.profile_sex_tv);


        final TextView joinedOnTv = v.findViewById(R.id.profile_joined_on_tv);

        if (!editable) {
            signOutBtn.setVisibility(View.GONE);
            forgotPasswordBtn.setVisibility(View.GONE);
            editProfileBtn.setVisibility(View.GONE);
        }

        FirebaseDatabase.getInstance().getReference().child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                String bio = user.getBio();
                if (bio != null)
                    bioTv.setText(user.getBio());
                else
                    bioTv.setText("No description added");

                displayNameTv.setText(user.getDisplayName());
                sexTv.setText(user.getSex().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final Context context = getActivity().getApplicationContext();
        if (context != null) {
            String path = "users/" + userId + "/images/profile_pic/profile_pic.jpeg";
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(profilePhotoImg);
                }
            });
        }

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog(getContext()).show();
            }
        });

        forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ResetActivity.class));
            }
        });

        //followedUsers
        RecyclerView followersRecycler = v.findViewById(R.id.my_profile_followed_users_list);
        followersRecycler.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));

        followerIds = new ArrayList<>();

        ArrayList<Event> events = new ArrayList<>();

        final mainFeedRecyclerViewAdapter adapter = new mainFeedRecyclerViewAdapter(events, getContext());
        recyclerView.setAdapter(adapter);

        final FollowersAdapter followersAdapter = new FollowersAdapter(followerIds,getContext());
        followersRecycler.setAdapter(followersAdapter);

        final DatabaseReference followerReference = FirebaseDatabase.getInstance().getReference();
        followerReference.child("users").child(userId).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followerIds = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followerIds.add(snapshot.getKey());
                }
                followersAdapter.setIds(followerIds);
                followersAdapter.notifyDataSetChanged();
                followingCount.setText(followerIds.size() + "");

                if(followerIds.isEmpty()){
                    followersBox.setVisibility(View.GONE);
                }else{
                    followersBox.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child("events").orderByChild("hostId").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Event> events = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    final Event event = eventSnapshot.getValue(Event.class);
                    event.setId(eventSnapshot.getKey());
                    events.add(event);
                }
                adapter.setEvents(events);
                adapter.notifyDataSetChanged();
                postCount.setText(events.size() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i =0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child("followers").exists()){
                        if(checkIsFollowed(snapshot.getKey())){
                            i++;
                        }
                    }
                }
                followerText.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }

    private boolean checkIsFollowed(String key) {
        final Boolean[] found = {false};
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(key).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.getKey().equals(userId)){
                        found[0] = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return found[0];
    }

    private AlertDialog dialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });
        builder.setNegativeButton("Cancel", null);
        return builder.create();
    }


}
