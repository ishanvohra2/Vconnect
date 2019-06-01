package com.theindiecorp.mango.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.theindiecorp.mango.R;
import com.theindiecorp.mango.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 100;
    private EditText inputEmail, inputPassword, inputName;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    CircularImageView profilePhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = findViewById(R.id.signup_log_in_btn);
        btnSignUp = findViewById(R.id.signup_register_btn);
        inputEmail = findViewById(R.id.reset_email);
        inputName = findViewById(R.id.signup_display_name);
        inputPassword = findViewById(R.id.login_password);
        progressBar = findViewById(R.id.progressBar);
        btnResetPassword = findViewById(R.id.reset_password_reset_btn);
        profilePhoto = findViewById(R.id.signup_profile_photo);
//
//        final Spinner spinner = findViewById(R.id.signup_spinner);
//        String[] sexSpinner = new String[]{"Male", "Female", "Others"};
//        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sexSpinner));

        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, ResetActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                final String date = day + "/" + month + "/" + year;

                final String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (inputName.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please provide a Display Name!", Toast.LENGTH_SHORT).show();
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);
                //create user

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    String path = updateProfilePick(auth.getCurrentUser().getUid(), profilePhoto);
                                    UserProfileChangeRequest request;
                                    if (path != null) {
                                        request = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(inputName.getText().toString())
                                                .setPhotoUri(Uri.parse(path)).build();
                                    } else {
                                        request = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(inputName.getText().toString()).build();
                                    }
                                    auth.getCurrentUser().updateProfile(request);
                                    String id = auth.getCurrentUser().getUid();
                                    User user = new User();
                                    user.setDisplayName(inputName.getText().toString());
                                    user.setJoinedOn(date);
                                    user.setSex(User.Sex.MALE);
                                    User privateUser = new User();
                                    user.setEmail(email);

                                    writeNewUser(user, id);
                                    updatePrivateInfo(privateUser, id);
                                    Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                                    intent.putExtra("name", auth.getCurrentUser().getDisplayName());
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });

            }
        });
    }

    public static String updateProfilePick(String userId, ImageView imageView) {
        if (imageView.getDrawable() != null) {

            FirebaseStorage storage = FirebaseStorage.getInstance();


            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapdata = stream.toByteArray();

            String path = "users/" + userId + "/images/profile_pic/profile_pic.jpeg";
            StorageReference storageReference = storage.getReference(path);

            storageReference.putBytes(bitmapdata);

            return path;
        }
        return null;
    }

    public void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            profilePhoto.setImageURI(data.getData());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    private void writeNewUser(User user, String id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(id).setValue(user);
    }

    private void updatePrivateInfo(User user, String id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("privateData").child(id).setValue(user);
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }
}