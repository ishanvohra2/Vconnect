package com.theindiecorp.mango.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.theindiecorp.mango.R;
import com.theindiecorp.mango.data.Event;
import com.theindiecorp.mango.data.Schools;
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

public class NewArticleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    int PLACE_PICKER_REQUEST = 12;
    Uri imgUri;
    private static final int PICK_IMAGE = 100;
    ImageView image;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    EditText articleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_article);

        articleText = findViewById(R.id.article_text);
        image = findViewById(R.id.new_article_main_image);
        Spinner locationSpinner = findViewById(R.id.new_article_location_tv);

        final ArrayList<String> schools = new ArrayList<>();

        findSchools(schools);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.location_spinner_layout, schools);
        locationSpinner.setAdapter(dataAdapter);

        locationSpinner.setOnItemSelectedListener(this);

        Button uploadImageBtn = findViewById(R.id.uploadImage);
        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        Button sharePostBtn = findViewById(R.id.shareBtn);
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

    private void findSchools(final ArrayList<String> schools) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("schools").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Schools school = snapshot.getValue(Schools.class);
                    school.setId(snapshot.getKey());
                    schools.add(school.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addPost() throws ParseException {
        String articleTxt = articleText.getText().toString();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        if (!TextUtils.isEmpty(articleTxt)) {

            String id = databaseReference.push().getKey();
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            String date = day + "/" + month + "/" + year;

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String imgPath = uploadImage(id);

            Event event = new Event();
            event.setType("article");
            event.setPublishDate(date);
            event.setImgUrl(imgPath);
            event.setHostId(userId);
            event.setEventName("");
            event.setDescription(articleTxt);

            Toast.makeText(this, "Shared", Toast.LENGTH_LONG).show();
            startActivity(new Intent(NewArticleActivity.this,HomeActivity.class));

            databaseReference.child("events").child(id).setValue(event);

            finish();

        } else {
            Toast.makeText(this, "You should enter some text first", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selection = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
