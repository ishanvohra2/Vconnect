package com.theindiecorp.vconnect.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.theindiecorp.vconnect.data.Event;
import com.theindiecorp.vconnect.R;
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

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NewEventActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private EditText eventName, eventDescription;
    private Button addBtn, addImg;
    private ImageView image;
    Uri imgUri;
    private static final int PICK_IMAGE = 100;
    private EditText totalSpots;
    private TextView eventDate, eventTime;
    Spinner locationSpinner;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    DatabaseReference mDatabase;

    int PLACE_PICKER_REQUEST = 12;

    ArrayList<String> schoolIds = new ArrayList<>();
    String venue,venueId;
    final List<String> schools = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        mDatabase = FirebaseDatabase.getInstance().getReference("events");

        eventName = findViewById(R.id.eventName);
        eventDescription = findViewById(R.id.eventDescription);
        addBtn = findViewById(R.id.addBtn);
        image = findViewById(R.id.new_event_main_image);
        addImg = findViewById(R.id.addImg);
        totalSpots = findViewById(R.id.totalSpots);
        eventDate = findViewById(R.id.dateText);
        eventTime = findViewById(R.id.new_event_time_tv);
        locationSpinner = findViewById(R.id.new_event_location_tv);

        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(NewEventActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener, year, month, day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });

        final ArrayAdapter<String> schoolsAdapter = new ArrayAdapter<String>(NewEventActivity.this,android.R.layout.simple_spinner_item,schools);
        schoolsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ValueEventListener schoolListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String schoolName = snapshot.child("name").getValue(String.class);
                    schools.add(schoolName);
                    schoolIds.add(snapshot.getKey());
                }
                locationSpinner.setAdapter(schoolsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        locationSpinner.setOnItemSelectedListener(NewEventActivity.this);

        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference();
        locationRef.child("schools").addValueEventListener(schoolListener);

        locationRef.removeEventListener(schoolListener);

        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(NewEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

        Button addSchool = findViewById(R.id.new_event_Add_school);
        addSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NewEventActivity.this,AddSchoolActivity.class));
            }
        });
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

        DatePickerDialog dialog = new DatePickerDialog(NewEventActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                getDate(view), year, month, day);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PLACE_PICKER_REQUEST) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
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

        if (!TextUtils.isEmpty(eventName)) {

            String id = mDatabase.push().getKey();
            String description = eventDescription.getText().toString();
            String d = eventDate.getText().toString() + " " + eventTime.getText().toString();
            Location loc = null;
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
            event.setVenueId(venueId);
            mDatabase.child(id).setValue(event);
            Toast.makeText(this, "Event Added", Toast.LENGTH_LONG).show();

            finish();

        } else {
            Toast.makeText(this, "You should enter a Event Name", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        venue = parent.getItemAtPosition(position).toString();
        venueId = schoolIds.get(position);
        //Toast.makeText(NewEventActivity.this,schoolIds.get(position),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
