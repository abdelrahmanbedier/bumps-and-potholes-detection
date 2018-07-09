package com.example.bedier.googlemapsbumps;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import java.util.UUID;

/**
 * Created by User on 10/2/2017.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,168),new LatLng(71,136));
    public double longitude;
    public double latitude;
    public double mlongitude;
    public double mlatitude;
    public int speedInKm;
    public int vel1;
    public int vel2;
    MediaPlayer buzzer;


    //widgets
    private AutoCompleteTextView mSearchText;


    //vars

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location location;
    private LocationSource.OnLocationChangedListener mListener;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private Marker mm;
    //bluetooth var


    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter=1;
    int notifyFlag=1;
    int velocityFlag=1;
    volatile boolean stopWorker;
    LocationManager locationManager;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    android.location.LocationListener locationListener;
    TextView txt;

    DatabaseReference rootRef,demoRef;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        if (mLocationPermissionsGranted) {
            getCurrentLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();

        }


        try {
            findBT();
            openBT();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSearchText =(AutoCompleteTextView) findViewById(R.id.input_search);
        final ImageView bumpButton = (ImageView) findViewById(R.id.bumpButton);
        final ImageView bumpButtonUsed = (ImageView) findViewById(R.id.bumpButtonUsed);
        final ImageView currentLocationBtn = (ImageView) findViewById(R.id.getLocationButton);
        buzzer= MediaPlayer.create(this,R.raw.buzzer);

        rootRef = FirebaseDatabase.getInstance().getReference();
        demoRef = rootRef.child("Coordinates");
        getLocationPermission();
        currentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng currentLocBtn = new LatLng(mlatitude,mlongitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocBtn, DEFAULT_ZOOM));
            }
        });
        bumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bumpButton.setVisibility(View.INVISIBLE);
                bumpButtonUsed.setVisibility(View.VISIBLE);

                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        add2FirebaseManually(latLng);
                        Toast.makeText(MapActivity.this,"Bump added !", Toast.LENGTH_SHORT).show();
                        mMap.setOnMapLongClickListener(null);
                        bumpButton.setVisibility(View.VISIBLE);
                        bumpButtonUsed.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
        updateSpeed();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                mMap.setMapType(mMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.item2:
                mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
                return true;

            case R.id.item3:
                mMap.setMapType(mMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    void detect_bump(){

        //if (mLocationPermissionsGranted) {
        add2Firebase();
        //  if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        //        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
        //      Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        // return;
        //}
        //mMap.setMyLocationEnabled(true);
        //mMap.getUiSettings().setMyLocationButtonEnabled(false);

//    }
    }
    private void init(){
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });
    }
    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }
        if(list.size() > 0){
            if(mm!=null)
                mm.remove();
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            LatLng currentLatLng = new LatLng(mlatitude,mlongitude);
            LatLng addressLatLng = new LatLng(address.getLatitude(),address.getLongitude());
            float distance[] = new float[20];
            Location.distanceBetween(latitude,longitude,address.getLatitude(),address.getLongitude(),distance);
             MarkerOptions options = new MarkerOptions()
                    .position(addressLatLng)
                    .title(address.getAddressLine(0));
            Log.d(TAG, "moveCamera: moving the camera to: lat: " + addressLatLng.latitude + ", lng: " + addressLatLng.longitude );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(addressLatLng, DEFAULT_ZOOM));
            options.snippet("Distance : "+Math.round(distance[0])+"m");
            mm=mMap.addMarker(options);
            String url = getRequestUrl(currentLatLng,addressLatLng);
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(url);
        }
    }
    private void add2Firebase(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

                            longitude = latLng.longitude;
                            latitude = latLng.latitude;

                            rootRef.child("Coordinates").orderByChild("x").startAt(latitude-0.0002).endAt(latitude+0.0002).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                            double ydouble = issue.child("y").getValue(Double.class);
                                            double xdouble = issue.child("x").getValue(Double.class);
                                            double zdouble = issue.child("z").getValue(Double.class);
                                            if(ydouble > longitude-0.0002 && ydouble < longitude+0.0002) {
                                            xdouble = ((zdouble * xdouble) + latitude) / (zdouble + 1);
                                            ydouble = ((zdouble * ydouble) + longitude) / (zdouble + 1);
                                            zdouble += 1;
                                            DatabaseReference xref = issue.child("x").getRef();
                                            xref.setValue(xdouble);
                                            DatabaseReference yref = issue.child("y").getRef();
                                            yref.setValue(ydouble);
                                            DatabaseReference zref = issue.child("z").getRef();
                                            zref.setValue(zdouble);

                                            return;
                                        }

                                        }


                                    }

                                    double coorx =latitude ;
                                    double coory =longitude ;
                                    HashMap<String, Double> coordinates = new HashMap<String, Double>();

                                    coordinates.put("x", coorx);
                                    coordinates.put("y", coory);
                                    coordinates.put("z",1.0);
                                    demoRef.push().setValue(coordinates);
                                    LatLng latLng = new LatLng(latitude,longitude);
                                    MarkerOptions options = new MarkerOptions()
                                            .position(latLng)
                                            .title("Bump!");

                                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                    mMap.addMarker(options);

                                }



                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }
    private void add2FirebaseManually(LatLng latLng){
        longitude = latLng.longitude;
        latitude = latLng.latitude;
        rootRef.child("Coordinates").orderByChild("x").startAt(latitude-0.0002).endAt(latitude+0.0002).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double currentTime = System.currentTimeMillis();
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        //String name = (String) issue.child("y").getValue();
                        double ydouble = issue.child("y").getValue(Double.class);
                        double xdouble = issue.child("x").getValue(Double.class);
                        double zdouble = issue.child("z").getValue(Double.class);


                        if(ydouble > longitude-0.0002 && ydouble < longitude+0.0002) {
                            xdouble = ((zdouble * xdouble) + latitude) / (zdouble + 1);
                            ydouble = ((zdouble * ydouble) + longitude) / (zdouble + 1);
                            zdouble += 1;

                            DatabaseReference xref = issue.child("x").getRef();
                            xref.setValue(xdouble);
                            DatabaseReference yref = issue.child("y").getRef();
                            yref.setValue(ydouble);
                            DatabaseReference zref = issue.child("z").getRef();
                            zref.setValue(zdouble);
                            DatabaseReference tref = issue.child("time").getRef();
                            tref.setValue(currentTime);

                            return;
                        }

                    }


                }

                double coorx =latitude ;
                double coory =longitude ;
                HashMap<String, Double> coordinates = new HashMap<String, Double>();

                coordinates.put("x", coorx);
                coordinates.put("y", coory);
                coordinates.put("z",1.0);
                coordinates.put("time",currentTime);


                // usersRef = ref.child("Users").child(name);
                demoRef.push().setValue(coordinates);
                LatLng latLng = new LatLng(latitude,longitude);
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title("Bump!");

                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                mMap.addMarker(options);

            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });







    }
    private void updateSpeed(){
        txt = (TextView) this.findViewById(R.id.textview);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        locationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                float nCurrentSpeed = location.getSpeed();
                speedInKm =(int) (nCurrentSpeed*18)/5;
                txt.setText(speedInKm + " km/h");
                mlatitude=location.getLatitude();
                mlongitude=location.getLongitude();
                notifyUser();


            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }
    private void getCurrentLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

                            Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                            longitude = latLng.longitude;
                            latitude = latLng.latitude;
                            rootRef.child("Coordinates").orderByChild("x").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    double currentTime = System.currentTimeMillis();
                                    if (dataSnapshot.exists()) {
                                        // dataSnapshot is the "issue" node with all children with id 0
                                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                            double time = issue.child("time").getValue(Double.class);
                                            double difference = bumpTimeOut(time,currentTime);
                                            if(difference<7){
                                            double ydouble = issue.child("y").getValue(Double.class);
                                            double xdouble = issue.child("x").getValue(Double.class);
                                            LatLng bumpsLatLng = new LatLng(xdouble, ydouble);
                                            MarkerOptions options = new MarkerOptions()
                                                    .position(bumpsLatLng)
                                                    .title("Bump!");
                                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                            mMap.addMarker(options);

                                        }
                                        else{
///////////////////
                                            }
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }
    private void notifyUser(){
        rootRef.child("Coordinates").orderByChild("x").startAt(mlatitude-0.0005).endAt(mlatitude+0.0005).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double xdouble=0;
                double ydouble=0;
                double zdouble=0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        xdouble = issue.child("x").getValue(Double.class);
                        ydouble = issue.child("y").getValue(Double.class);
                        zdouble = issue.child("z").getValue(Double.class);

                        if (ydouble > mlongitude - 0.0005 && ydouble < mlongitude + 0.0005) {
                            if (notifyFlag == 1 && zdouble>=10) {
                                Toast.makeText(getApplicationContext(), "Bump Ahead !", Toast.LENGTH_SHORT).show();
                                vel1=speedInKm;
                                buzzer.start();
                                notifyFlag = 0;
                            }


                        } else {
                            notifyFlag = 1;
                        }

                        if ((xdouble > mlatitude - 0.0001 && xdouble < mlatitude + 0.0001) &&
                                (ydouble > mlongitude - 0.0001 && ydouble < mlongitude+0.0001) &&
                                zdouble>=10) {
                            if (velocityFlag==1)
                            {
                                vel2=speedInKm;
                                velocityFlag=0;
                            }

                        }
                        else{
                            velocityFlag=1;
                            if (vel1>=40 && vel2<=20){//Fast at notify and slow in range
                                zdouble++;
                            }
                            else if (vel2>=40){//Fast in range
                                zdouble--;
                            }
                            DatabaseReference zref = issue.child("z").getRef();
                            zref.setValue(zdouble);


                        }

                    }

                } else {
                    notifyFlag = 1;
                }


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }










    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("HC-05")) // this name have to be
                // replaced with your
                // bluetooth device name
                {
                    mmDevice = device;
                    Log.v("ArduinoBT",
                            "findBT found device named " + mmDevice.getName());
                    Log.v("ArduinoBT",
                            "device address is " + mmDevice.getAddress());
                    break;
                }
            }
        }
    }

    void openBT() throws IOException {

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard
        // SerialPortService
        // ID

        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        Log.d(TAG, "ya raaaaaaaab");

        mmSocket.connect();
        Log.d(TAG, "eshta3'al?");

        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        Log.d(TAG, "haa");

        beginListenForData();

    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; // This is the ASCII code for a newline
        // character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {

                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        //Log.d(TAG, "fe bytes?"+bytesAvailable);

                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0,
                                            encodedBytes, 0,
                                            encodedBytes.length);
                                    final String data = new String(
                                            encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {

                                            detect_bump();

                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }

                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    public double bumpTimeOut(double startDate, double endDate) {
        //milliseconds
        double different = endDate- startDate;


        double secondsInMilli = 1000;
        double minutesInMilli = secondsInMilli * 60;
        double hoursInMilli = minutesInMilli * 60;
        double daysInMilli = hoursInMilli * 24;

        double elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        double elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        double elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        double elapsedSeconds = different / secondsInMilli;

        return elapsedDays;
    }
    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_org = "origin=" + origin.latitude +","+origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude+","+dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //Build the full param
        String param = str_org +"&" + str_dest + "&" +sensor+"&" +mode;
        //Output format
        String output = "json";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }
    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DataParser dataParser = new DataParser();
                routes = dataParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions!=null) {
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }

        }
    }
}











