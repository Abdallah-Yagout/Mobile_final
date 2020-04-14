package com.example.mobile_final;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    private EditText username, userEmail, userPhonenumber, userLocation;
    private CircleImageView userProfileImage;

    private Button locationButton;

    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference Root;
    private StorageReference userProfileImagesRef;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //locationButton = findViewById(R.id.loc_button);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        Root = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        username = findViewById(R.id.set_user_name);
        userEmail = findViewById(R.id.set_profile_status);
        userLocation = findViewById(R.id.set_user_location);
        userPhonenumber = findViewById(R.id.phone_number_profile);

        userProfileImage = findViewById(R.id.set_profile_image);
        loading = new ProgressDialog(this);


        UserProfileDetails();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 29);
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(ProfileActivity.this),1);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(data, this);
            String latitude = String.valueOf(place.getLatLng().latitude);
            String longitude = String.valueOf(place.getLatLng().longitude);
            Toast.makeText(this, latitude, Toast.LENGTH_LONG).show();
            Toast.makeText(this, longitude, Toast.LENGTH_LONG).show();
        }

        if (requestCode == 29 && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //Cropped Successfully
                loading.setTitle("Setting Profile Image");
                loading.setMessage("Uploading...");
                loading.setCanceledOnTouchOutside(false);
                loading.show();

                final Uri resultUri = result.getUri();
                StorageReference filePath = userProfileImagesRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(ProfileActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            String downloadedUrl = task.getResult().getDownloadUrl().toString();
                            Root.child("Users").child(currentUserID).child("image").setValue(downloadedUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ProfileActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();
                                                loading.dismiss();
                                            } else {
                                                String message = task.getException().toString();
                                                Toast.makeText(ProfileActivity.this, "Error On Save : " + message, Toast.LENGTH_SHORT).show();
                                                loading.dismiss();
                                            }
                                        }
                                    });
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(ProfileActivity.this, "Error On Upload: " + message, Toast.LENGTH_SHORT).show();
                            loading.dismiss();
                        }
                    }
                });
            }
        }

    }

    public void updateProfile(View view) {
        String setUserName = username.getText().toString().trim();
        String setEmail = userEmail.getText().toString().trim();
        String setPhoneNumber = userPhonenumber.getText().toString().trim();
        String setLocation = userLocation.getText().toString().trim();

        if (setUserName.isEmpty()) {

            Toast.makeText(this, "You must enter a username", Toast.LENGTH_SHORT).show();

        } else if (setEmail.isEmpty()) {
            Toast.makeText(this, "You must to enter a email", Toast.LENGTH_SHORT).show();

        }else if (setPhoneNumber.isEmpty()) {
            Toast.makeText(this, "You must to enter a number", Toast.LENGTH_SHORT).show();

        }else if (setLocation.isEmpty()) {
            Toast.makeText(this, "You must to enter a location", Toast.LENGTH_SHORT).show();

        }
        else {
            HashMap<String, String> profileMap = new HashMap<>();

            profileMap.put("id", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("email", setEmail);
            profileMap.put("phone", setPhoneNumber);
            profileMap.put("location", setLocation);


            Root.child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                MoveToMainActivity();
                                Toast.makeText(ProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(ProfileActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    private void UserProfileDetails() {
        Root.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {

                            String userName = dataSnapshot.child("name").getValue().toString();
                            String userEmail = dataSnapshot.child("email").getValue().toString();
                            String userImage = dataSnapshot.child("image").getValue().toString();
                            String userLocation = dataSnapshot.child("location").getValue().toString();
                            String userPhone = dataSnapshot.child("phone").getValue().toString();

                            ProfileActivity.this.userLocation.setText(userLocation);
                            ProfileActivity.this.userEmail.setText(userEmail);
                            ProfileActivity.this.userPhonenumber.setText(userPhone);
                            ProfileActivity.this.username.setText(userName);

                            Glide.with(getApplicationContext()).load(userImage).into(userProfileImage);


                        } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {

                            String userName = dataSnapshot.child("name").getValue().toString();
                            String userEmail = dataSnapshot.child("email").getValue().toString();
                            String userLocation = dataSnapshot.child("location").getValue().toString();
                            String userPhone = dataSnapshot.child("phone").getValue().toString();

                            ProfileActivity.this.userLocation.setText(userLocation);
                            ProfileActivity.this.userEmail.setText(userEmail);
                            ProfileActivity.this.userPhonenumber.setText(userPhone);
                            ProfileActivity.this.username.setText(userName);

                        }

                        else {

                            Toast.makeText(ProfileActivity.this, "Please Enter Information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void MoveToMainActivity() {
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }



}
