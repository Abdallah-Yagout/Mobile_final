package com.example.mobile_final;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword ,Username, UserPhone, UserLocation;
    private Button AlreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference Root;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        Root = FirebaseDatabase.getInstance().getReference();

        CreateAccountButton = findViewById(R.id.register_button);


        UserEmail = findViewById(R.id.register_email);
        UserPassword = findViewById(R.id.register_password);

        Username = findViewById(R.id.register_set_user_name);
        UserLocation =findViewById(R.id.register_set_user_location);
        UserPhone = findViewById(R.id.register_phone_number_profile);


        loading = new ProgressDialog(this);
        AlreadyHaveAccountLink = findViewById(R.id.already_have_account_link);

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoveToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });


    }

    private void CreateNewAccount() {
        String email_Edit_text = UserEmail.getText().toString();
        String password_Edit_text = UserPassword.getText().toString();
        String name_Edit_text = Username.getText().toString();
        String location_Edit_text = UserLocation.getText().toString();
        String phone_Edit_text = UserPhone.getText().toString();

        if (name_Edit_text.isEmpty()) {
            Toast.makeText(this, "You must enter a username", Toast.LENGTH_SHORT).show();
        } else if (email_Edit_text.isEmpty()) {
            Toast.makeText(this, "You need to enter a email", Toast.LENGTH_SHORT).show();
        }else if (phone_Edit_text.isEmpty()) {
            Toast.makeText(this, "You need to enter a number", Toast.LENGTH_SHORT).show();
        }else if (location_Edit_text.isEmpty()) {
            Toast.makeText(this, "You need to enter a location", Toast.LENGTH_SHORT).show();
        }
        else {
            loading.setTitle("Creating a New Account");
            loading.setMessage("Wait while an account is created");
            loading.setCanceledOnTouchOutside(true);
            loading.show();

            mAuth.createUserWithEmailAndPassword(email_Edit_text,password_Edit_text)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String currentUserID= mAuth.getCurrentUser().getUid();
                                String email = mAuth.getCurrentUser().getEmail();
                                String phone = UserPhone.getText().toString();
                                String location = UserLocation.getText().toString().trim();
                                String name = Username.getText().toString().trim();

                                Root.child("Users").child(currentUserID).child("name").setValue(name);
                                Root.child("Users").child(currentUserID).child("id").setValue(currentUserID);
                                Root.child("Users").child(currentUserID).child("email").setValue(email);
                                Root.child("Users").child(currentUserID).child("phone").setValue(phone);
                                Root.child("Users").child(currentUserID).child("location").setValue(location);

                                Toast.makeText(RegisterActivity.this,
                                        "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                                MoveToMainActivity();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : "+message, Toast.LENGTH_LONG).show();
                                loading.dismiss();
                            }
                        }
                    });
        }

    }

    private void MoveToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    private void MoveToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}

