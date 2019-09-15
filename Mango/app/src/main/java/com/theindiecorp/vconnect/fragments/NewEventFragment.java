package com.theindiecorp.vconnect.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.activity.HomeActivity;
import com.theindiecorp.vconnect.activity.NewEventActivity;
import com.theindiecorp.vconnect.data.Event;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class NewEventFragment extends Fragment {

    private EditText eventName, eventDescription;
    private Button addBtn, addImg;
    private ImageView image;
    Uri imgUri;
    private static final int PICK_IMAGE = 100;
    private EditText totalSpots,locationEt;
    private TextView eventDate, eventTime;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    DatabaseReference mDatabase;

    int PLACE_PICKER_REQUEST = 12;

    ArrayList<String> schoolIds = new ArrayList<>();
    String venue,venueId;
    final List<String> schools = new ArrayList<>();
    int userPoints;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_event,container,false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        eventName = view.findViewById(R.id.eventName);
        eventDescription = view.findViewById(R.id.eventDescription);
        addBtn = view.findViewById(R.id.addBtn);
        image = view.findViewById(R.id.new_event_main_image);
        addImg = view.findViewById(R.id.addImg);
        totalSpots = view.findViewById(R.id.totalSpots);
        eventDate = view.findViewById(R.id.dateText);
        eventTime = view.findViewById(R.id.new_event_time_tv);
        locationEt = view.findViewById(R.id.new_event_location_tv);

        Button moveToArticleBtn = view.findViewById(R.id.share_article_btn);
        moveToArticleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new NewPostFragment());
            }
        });

        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener, year, month, day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });

        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = hourOfDay + ":" + minute;
                        eventTime.setText(time);
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });

        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(eventDate);
            }
        });

        addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addEvent();
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

        return view;
    }

    private DatePickerDialog.OnDateSetListener getDate(final TextView view) {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = day + "/" + month + "/" + year;
                view.setText(date);
            }
        };
    }

    public void setDate(TextView view){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                getDate(view), year, month, day);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
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

        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();

        String path = "events/" + eventId + "/images/image.jpeg";
        StorageReference storageReference = storage.getReference(path);

        UploadTask uploadTask = storageReference.putBytes(bitmapdata);

        return path;
    }

    private void addEvent() throws ParseException{
        String eventName = this.eventName.getText().toString();

        if (!TextUtils.isEmpty(eventName) && !TextUtils.isEmpty(eventDate.getText().toString() )
                && !TextUtils.isEmpty(eventTime.getText().toString()) && !TextUtils.isEmpty(locationEt.getText().toString())) {

            String id = mDatabase.push().getKey();
            String description = eventDescription.getText().toString();
            String d = eventDate.getText().toString() + " " + eventTime.getText().toString();

            int total = Integer.parseInt(totalSpots.getText().toString());
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String imgPath = uploadImage(id);
            String hostName = auth.getCurrentUser().getDisplayName();

            Event event = new Event();
            event.setHostId(userId);
            event.setEventName(eventName);
            event.setDescription(description);
            event.setImgUrl(imgPath);
            event.setDate(d);
            event.setPeopleCount(0);
            event.setTotalSpots(total);
            event.setType("event");
            event.setVenueId(locationEt.getText().toString());
            event.setPoints(5);

            mDatabase.child("events").child(id).setValue(event);
            mDatabase.child("users").child(event.getHostId()).child("points").setValue(userPoints + 5);
            Toast.makeText(getContext(), "5 Points Rewarded!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "You should enter a Event Name", Toast.LENGTH_LONG).show();
        }
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.home_frame_container, fragment);
        transaction.commit();
    }
}