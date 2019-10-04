package com.theindiecorp.vconnect.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theindiecorp.vconnect.R;
import com.theindiecorp.vconnect.data.Group;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AddGroupActivity extends AppCompatActivity {

    EditText nameEt, descriptionEt, numberOfMembersEt;
    Button uploadImageBtn, createBtn;
    ImageView image;

    private static final int PICK_IMAGE = 100;
    int PLACE_PICKER_REQUEST = 12;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Boolean found;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        nameEt = findViewById(R.id.group_name_et);
        descriptionEt = findViewById(R.id.group_desc_et);
        numberOfMembersEt = findViewById(R.id.members_et);
        uploadImageBtn = findViewById(R.id.addImg);
        createBtn = findViewById(R.id.addBtn);
        image = findViewById(R.id.group_image);

        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(nameEt.getText())){
                    nameEt.setError("Please enter the name of the group");
                    return;
                }

                if(TextUtils.isEmpty(descriptionEt.getText())){
                    descriptionEt.setError("Please enter the group description");
                    return;
                }

                final Group group = new Group();
                group.setAdminId(HomeActivity.userId);
                group.setGroupDescription(descriptionEt.getText().toString());
                group.setMaximumNumberOfMembers(Integer.parseInt(numberOfMembersEt.getText().toString()));
                group.setName(nameEt.getText().toString());
                group.setMembers(new ArrayList<String>());
                group.getMembers().add(HomeActivity.userId);
                group.setId(databaseReference.push().getKey());
                group.setUrl(uploadImage(group.getId()));

                databaseReference.child("groups").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        found = false;
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Group g = snapshot.getValue(Group.class);
                            if(g.getName().toLowerCase().equals(group.getName().toLowerCase())){
                                Toast.makeText(AddGroupActivity.this,"Name already taken",Toast.LENGTH_LONG).show();
                                found = true;
                                break;
                            }
                        }
                        if(!found){
                            databaseReference.child("groups").child(group.getId()).setValue(group);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                startActivity(new Intent(AddGroupActivity.this,HomeActivity.class));

            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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


    public String uploadImage(String groupId) {

        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();

        String path = "groups/" + groupId + "/images/image.jpeg";
        StorageReference storageReference = storage.getReference(path);

        UploadTask uploadTask = storageReference.putBytes(bitmapdata);

        return path;
    }

}
