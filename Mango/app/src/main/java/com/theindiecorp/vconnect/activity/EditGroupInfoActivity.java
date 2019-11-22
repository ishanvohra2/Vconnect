package com.theindiecorp.vconnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class EditGroupInfoActivity extends AppCompatActivity {

    EditText nameEt, descriptionEt;
    Button uploadImageBtn, createBtn, deleteBtn;
    ImageView image;

    private static final int PICK_IMAGE = 100;
    int PLACE_PICKER_REQUEST = 12;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private Boolean found;
    private String groupId;
    private Group group = new Group();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_info);

        groupId = getIntent().getStringExtra("groupId");

        nameEt = findViewById(R.id.group_name_et);
        descriptionEt = findViewById(R.id.group_desc_et);
        uploadImageBtn = findViewById(R.id.addImg);
        createBtn = findViewById(R.id.addBtn);
        deleteBtn = findViewById(R.id.delete_group_btn);
        image = findViewById(R.id.group_image);

        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        databaseReference.child("groups").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                group = dataSnapshot.getValue(Group.class);
                nameEt.setText(group.getName());
                descriptionEt.setText(group.getGroupDescription());

                if(group.getUrl()!=null){
                    StorageReference imageReference = storage.getReference().child(group.getUrl());
                    imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(EditGroupInfoActivity.this)
                                    .load(uri)
                                    .into(image);
                            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d(HomeActivity.TAG, exception.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                group.setName(nameEt.getText().toString());
                group.setMembers(new ArrayList<String>());
                group.getMembers().add(HomeActivity.userId);
                group.setId(groupId);
                group.setUrl(uploadImage(group.getId()));

                databaseReference.child("groups").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        found = false;
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Group g = snapshot.getValue(Group.class);
                            if(g.getName().toLowerCase().equals(group.getName().toLowerCase())){
                                Toast.makeText(EditGroupInfoActivity.this,"Name already taken",Toast.LENGTH_LONG).show();
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

                startActivity(new Intent(EditGroupInfoActivity.this,GroupViewActivity.class).putExtra("groupId",groupId));

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditGroupInfoActivity.this);
                builder.setMessage("Are you sure you want to delete the group ?");
                builder.setCancelable(true);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        databaseReference.child("groups").child(groupId).removeValue();
                        startActivity(new Intent(EditGroupInfoActivity.this,HomeActivity.class));
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
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
