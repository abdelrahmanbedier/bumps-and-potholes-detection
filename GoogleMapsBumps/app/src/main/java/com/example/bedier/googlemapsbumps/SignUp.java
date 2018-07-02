package com.example.bedier.googlemapsbumps;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {
    private static final int ERROR_DIALOG_REQUEST = 9001;
    Button signUp;
    EditText username, password;
    RadioGroup rdGroup;
    String uOrV="v";
    DatabaseReference rootRef,demoRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        rootRef = FirebaseDatabase.getInstance().getReference();
        demoRef = rootRef.child("Users");
        signUp = (Button) findViewById(R.id.btnSignUp);
        username = (EditText) findViewById(R.id.txtuser);
        password = (EditText) findViewById(R.id.txtpass);
        rdGroup = (RadioGroup) findViewById(R.id.radio_group);
        rdGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.volunteer) {
                    uOrV="v";
                } else if (checkedId == R.id.user) {
                    uOrV="u";

                }
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString().trim();
                String pass = password.getText().toString().trim();
                HashMap<String, String> users = new HashMap<String, String>();

                users.put("username", user);
                users.put("password", pass);
                users.put("Volunteer_User",uOrV);
                demoRef.push().setValue(users);


                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }
}