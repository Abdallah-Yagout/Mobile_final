package com.example.mobile_final;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar mToolbar;


    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference Root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Root = FirebaseDatabase.getInstance().getReference();

        mToolbar =  findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("YParts");


    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (currentUser == null) {
            MoveToLoginActivity();
        } else {
            EnterTheApp();
        }
    }

    private void EnterTheApp() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        Root.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String name= dataSnapshot.child("name").getValue().toString();
                    Toast.makeText(MainActivity.this, "Welcome "+name, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.logout) {
            mAuth.signOut();
            MoveToLoginActivity();
        }
        if (item.getItemId() == R.id.profile) {
            Intent settingsIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(settingsIntent);
        }

        return true;
    }


    private void MoveToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        //Does not allow you to go back to mainActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Stop the activity
    }




}