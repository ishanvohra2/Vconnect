package com.theindiecorp.mango.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theindiecorp.mango.R;
import com.theindiecorp.mango.data.Schools;

import java.io.ByteArrayOutputStream;

import static com.theindiecorp.mango.activity.SignUpActivity.PICK_IMAGE;

public class AddSchoolActivity extends AppCompatActivity {

    private EditText name,description,address,city,country;
    private Button uploadImgBtn,submitBtn;
    private ImageView image;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    int PLACE_PICKER_REQUEST = 12;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_school);

        name = findViewById(R.id.add_school_name);
        description = findViewById(R.id.add_school_description);
        address = findViewById(R.id.add_school_address);
        city = findViewById(R.id.add_school_city);
        country = findViewById(R.id.add_school_country);
        uploadImgBtn = findViewById(R.id.addImg);
        submitBtn = findViewById(R.id.add_school_add_btn);
        image = findViewById(R.id.add_school_main_image);

        uploadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = databaseReference.push().getKey();
                Schools school = new Schools();
                school.setName(name.getText().toString());
                school.setDescription(description.getText().toString());
                school.setAddress(address.getText().toString() + "," + city.getText().toString());
                school.setCountry(country.getText().toString());
                school.setFollowerCount(0);
                school.setId(id);
                school.setImgUrl(uploadImage(id));
                databaseReference.child("schools").child(id).setValue(school);
                startActivity(new Intent(AddSchoolActivity.this,NewEventActivity.class));
                finish();
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


    public String uploadImage(String schoolId) {

        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();

        String path = "schools/" + schoolId + "/images/image.jpeg";
        StorageReference storageReference = storage.getReference(path);

        UploadTask uploadTask = storageReference.putBytes(bitmapdata);

        return path;
    }

}
