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
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    Button login,signUp;
    EditText username, password;
    DatabaseReference rootRef,demoRef;
    String user,pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isServicesOK()){
            init();
        }

        rootRef = FirebaseDatabase.getInstance().getReference();

    }

    private void init(){
        login=(Button)findViewById(R.id.btnlogin);
        signUp=(Button)findViewById(R.id.btnSignUp);
        username=(EditText)findViewById(R.id.txtuser);
        password=(EditText)findViewById(R.id.txtpass);
     login.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             user=username.getText().toString().trim();
             pass=password.getText().toString().trim();
             login();
         }
     });

     signUp.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             Intent intent = new Intent(MainActivity.this, SignUp.class);
             startActivity(intent);
         }
     });
     
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK,checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and user can make map service
            Log.d(TAG,"isServicesOK , everthing is fine");
            return true;

        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            // an error occured but we can resolved
            Log.d(TAG,"error occured we can fix it");
            Dialog dialog =GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            dialog.show();

    }
    else {
            Toast.makeText(this,"you cant make  map request",Toast.LENGTH_SHORT).show();

        }
        return false;

}


    private void login() {


        rootRef.child("Users").orderByChild("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        String username = issue.child("username").getValue(String.class);
                        String password = issue.child("password").getValue(String.class);
                        String uOrV = issue.child("Volunteer_User").getValue(String.class);
                        if(user.equals(username) && pass.equals(password)){
                            if(uOrV.equals("v")) {
                                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                                startActivity(intent);
                                return;
                            }
                            else if(uOrV.equals("u")) {
                                Intent intent = new Intent(MainActivity.this, MapActivity2.class);
                                startActivity(intent);
                                return;

                            }
                        }

                        }
                    Toast.makeText(MainActivity.this,"Wrong Username or Password", Toast.LENGTH_SHORT).show();



                    }
                }




            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });


        }
    }