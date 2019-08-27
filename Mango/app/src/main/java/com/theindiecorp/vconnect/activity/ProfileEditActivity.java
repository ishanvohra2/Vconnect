package com.theindiecorp.vconnect.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.data.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProfileEditActivity extends AppCompatActivity {

    User user = new User();
    EditText nameTv, bioTv, emailTv, phoneNumberTv, userNameTV;
    TextView memberSinceTv, locationTv, languagesTv, birthdateTv;
    Button updateProfileBtn;
    ImageView profilePhoto;


    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }


    DatePickerDialog.OnDateSetListener dateSetListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        HomeActivity.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        nameTv = findViewById(R.id.profile_edit_name);
        bioTv = findViewById(R.id.profile_edit_bio);
        locationTv = findViewById(R.id.profile_edit_location);
        languagesTv = findViewById(R.id.profile_edit_languages);
        emailTv = findViewById(R.id.profile_edit_email);
        phoneNumberTv = findViewById(R.id.profile_edit_number);
        birthdateTv = findViewById(R.id.profile_edit_birthdate);
        updateProfileBtn = findViewById(R.id.profile_edit_update_profile_btn);
        memberSinceTv = findViewById(R.id.profile_edit_member_since_tv);
        userNameTV = findViewById(R.id.profile_edit_username);

        profilePhoto = findViewById(R.id.profile_edit_photo);

        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        final Spinner spinner = findViewById(R.id.signup_spinner);
        String[] sexSpinner = new String[]{"Male", "Female", "Others"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sexSpinner));

        FirebaseStorage.getInstance().getReference().child("users/" + HomeActivity.userId + "/images/profile_pic/profile_pic.jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ProfileEditActivity.this).load(uri).into(profilePhoto);
            }
        });



        birthdateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(ProfileEditActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener, year, month, day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                month = month + 1;

                String date = day + "/" + month + "/" + year;
                DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
                Date d = null;
                try {
                    d = dateFormat.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                birthdateTv.setText(simpleDateFormat.format(d));
            }
        };


        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                nameTv.setText(user.getDisplayName());
                bioTv.setText(user.getBio());
                emailTv.setText(user.getEmail());
                phoneNumberTv.setText(user.getNumber());
                birthdateTv.setText(simpleDateFormat.format(user.getBirthdate() == null ? new Date() : user.getBirthdate()));
                if(user.getUsername() != null){
                    userNameTV.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        databaseReference.child("privateData").child(HomeActivity.userId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = dataSnapshot.getValue(User.class);
//                if(user != null) {
//                    emailTv.setText(user.getEmail());
//                    phoneNumberTv.setText(user.getNumber());
//                    if (user.getBirthdate() != null)
//                        birthdateTv.setText(simpleDateFormat.format(user.getBirthdate()));
//                }else{
//                    databaseReference.child("privateData").child(HomeActivity.userId).setValue("true");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User();
                user.setDisplayName(nameTv.getText().toString());
                user.setBio(bioTv.getText().toString());
                user.setSex(User.Sex.values()[spinner.getSelectedItemPosition()]);
                user.setUsername(userNameTV.getText().toString());

                /// private data
                User privateUser = new User();
                if (emailTv.getText() != null)
                    privateUser.setEmail(emailTv.getText().toString());
                if (phoneNumberTv.getText() != null)
                    privateUser.setNumber(phoneNumberTv.getText().toString());
                Date date = new Date();
                try {
                    date = simpleDateFormat.parse(birthdateTv.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                privateUser.setBirthdate(date);
                databaseReference.child("users").child(HomeActivity.userId).child("displayName").setValue(user.getDisplayName());
                databaseReference.child("users").child(HomeActivity.userId).child("bio").setValue(user.getBio());
                databaseReference.child("users").child(HomeActivity.userId).child("sex").setValue(user.getSex());
                databaseReference.child("privateData").child(HomeActivity.userId).setValue(privateUser);
                databaseReference.child("users").child(HomeActivity.userId).child("username").setValue(user.getUsername());

                finish();
            }

        });
    }

    public void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, SignUpActivity.PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SignUpActivity.PICK_IMAGE) {
            profilePhoto.setImageURI(data.getData());
            String path = SignUpActivity.updateProfilePick(HomeActivity.userId, profilePhoto);
            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(path)).build();
            FirebaseAuth.getInstance().getCurrentUser().updateProfile(request);
        }
    }
}
