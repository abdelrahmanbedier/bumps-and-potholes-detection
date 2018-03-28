package com.example.bedier.googlemapsbumps;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isServicesOK()){
            init();
        }
    }

    private void init(){
     Button BtnMap = (Button) findViewById(R.id.btnMap);
     BtnMap.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             Intent intent = new Intent(MainActivity.this,MapActivity.class);
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










}
