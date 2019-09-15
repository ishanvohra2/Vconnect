package com.theindiecorp.vconnect.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.data.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;


public class NewPostFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    int PLACE_PICKER_REQUEST = 12;
    Uri imgUri;
    private static final int PICK_IMAGE = 100;
    ImageView image;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    EditText articleText;

    public NewPostFragment() {
        // Required empty public constructor
    }

    int userPoints;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_new_post, container, false);

        articleText = v.findViewById(R.id.article_text);
        image = v.findViewById(R.id.new_article_main_image);

        Button moveToEventBtn = v.findViewById(R.id.share_event_btn);
        moveToEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new NewEventFragment());
            }
        });

        Button uploadImageBtn = v.findViewById(R.id.uploadImage);
        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        Button sharePostBtn = v.findViewById(R.id.shareBtn);
        sharePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addPost();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("users").child(HomeActivity.userId).child("points").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userPoints = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PLACE_PICKER_REQUEST) {
                Place place = PlacePicker.getPlace(data, getContext());
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(getContext(), toastMsg, Toast.LENGTH_LONG).show();
            } else if (requestCode == PICK_IMAGE) {
                image.setImageURI(data.getData());
            }
        }
    }

    public void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }


    public String uploadImage(String eventId) {

        if (image.getDrawable() != null) {
            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapdata = stream.toByteArray();

            String path = "events/" + eventId + "/images/image.jpeg";
            StorageReference storageReference = storage.getReference(path);

            UploadTask uploadTask = storageReference.putBytes(bitmapdata);

            return path;
        }

        return "";
    }

    private void addPost() throws ParseException {
        String articleTxt = articleText.getText().toString();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        if (!TextUtils.isEmpty(articleTxt)) {

            String id = databaseReference.push().getKey();
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            String date = day + "/" + month + "/" + year;

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String imgPath = uploadImage(id);

            final Event event = new Event();
            event.setType("article");
            event.setPublishDate(date);
            if (!TextUtils.isEmpty(imgPath))
                event.setImgUrl(imgPath);
            event.setHostId(userId);
            event.setEventName("");
            event.setDescription(articleTxt);
            event.setPoints(5);

            databaseReference.child("events").child(id).setValue(event);
            databaseReference.child("users").child(event.getHostId()).child("points").setValue(userPoints + 5);
            Toast.makeText(getContext(), "5 Points Rewarded!", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(getContext(), "You should enter some text first", Toast.LENGTH_LONG).show();
        }
    }
    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.home_frame_container, fragment);
        transaction.commit();
    }
}