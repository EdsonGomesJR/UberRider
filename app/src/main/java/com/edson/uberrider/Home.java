package com.edson.uberrider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.edson.uberrider.Common.Common;
import com.edson.uberrider.Helper.CustomInfoWindow;
import com.edson.uberrider.Model.FCMResponse;
import com.edson.uberrider.Model.Notification;
import com.edson.uberrider.Model.Rider;
import com.edson.uberrider.Model.Sender;
import com.edson.uberrider.Model.Token;
import com.edson.uberrider.Remote.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback {

    private AppBarConfiguration mAppBarConfiguration;
    SupportMapFragment mapFragment;

    private StringBuilder mResult;

    //Location
    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 70001;
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    Button btnPickupRequest;
    DatabaseReference ref;
    GeoFire geoFire;
    Marker mUserMarker, markerDestination;

    boolean isDriverFound = false;
    String driverID = "";
    int radius = 1; // 1km
    int distance = 1; // 3km
    private static final int LIMIT = 3;

    //send alert
    IFCMService mService;

    //Presence System
    DatabaseReference driversAvaliable;


    //bottom sheet
    ImageView imgExpandable;
    BottomSheetRiderFragment mBottomSheet;

    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;

    AutocompleteSupportFragment place_location, place_destination;
    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME,
            Place.Field.ID, Place.Field.LAT_LNG);

    FindAutocompletePredictionsRequest typeFilter;


    String mPlaceLocation, mPlaceDestination;

    /**
     * Posting my solution as an answer in case it helps anyone else.
     * <p>
     * I got it working by declaring a LocationCallback as a member variable and then initialising (or re-initialising) it in each method that requires it...
     * <p>
     * public void getCurrentLocationUpdates(final UserLocationCallback callback){
     * if (mIsReceivingUpdates){
     * callback.onFailedRequest("Device is already receiving updates");
     * return;
     * }
     * <p>
     * // Set up the LocationCallback for the request
     * mLocationCallback = new LocationCallback()
     * {
     *
     * @param savedInstanceState
     * @Override public void onLocationResult(LocationResult locationResult){
     * if (locationResult != null){
     * callback.onLocationResult(locationResult.getLastLocation());
     * } else {
     * callback.onFailedRequest("Location request returned null");
     * }
     * }
     * };
     * <p>
     * // Start the request
     * mLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
     * // Update the request state flag
     * mIsReceivingUpdates = true;
     * }
     * I check at the beginning of the method whether or not location updates are already being received and get out early if so. This prevents duplicate (and thus unstoppable) location update requests being initiated.
     * <p>
     * Calling the stopLocationUpdates (below for reference) method now works as it should.
     * <p>
     * public void stopLocationUpdates(){
     * <p>
     * mLocationClient.removeLocationUpdates(mLocationCallback);
     * mIsReceivingUpdates = false;
     * Log.i(TAG, "Location updates removed");
     * <p>
     * }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initPlaces();
        setUpPlaceAutoComplete();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getFCMService();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        buildLocationCallBack();
        buildLocationRequest();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
        displayLocation();
        Snackbar.make(mapFragment.getView(), "Você está Online!", Snackbar.LENGTH_SHORT)
                .show();


        //init view
        imgExpandable = findViewById(R.id.imgExpandable);


        btnPickupRequest = findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isDriverFound)
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                else
                    sendRequestToDriver(driverID);
            }
        });


        //events


        setUpLocation();
        updateFirebaseToken();
    }

    private void setUpPlaceAutoComplete() {

        //initialize the AutocompleteSupportFragment

        place_location = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.place_location);

        place_location.setPlaceFields(placeFields);
        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mPlaceLocation = place.getAddress();
                //Remove old marker
                mMap.clear();

                //add marker at new location

                mUserMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                        .title("Pickup Here"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        place_destination = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.place_destination);
        place_destination.setPlaceFields(placeFields);
        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mPlaceDestination = place.getAddress();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                        .title("Destination"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

                //show info in bottom
                BottomSheetRiderFragment mBottomSheet = (BottomSheetRiderFragment) BottomSheetRiderFragment.newInstance(mPlaceLocation, mPlaceDestination, false);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });


    }

    private void initPlaces() {
        //places api
        if (!Places.isInitialized()) {

            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_direction_api));
            placesClient = Places.createClient(this);
        }
    }

    private void updateFirebaseToken() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void sendRequestToDriver(String driverID) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                            Token token = postSnapShot.getValue(Token.class); //get token object drom database with key

                            //make raw payload - convert latlng to json
                            String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                            String riderToken = FirebaseInstanceId.getInstance().getToken(); // possivel erro pois está depreciado
                            Log.d("riderToken", "onDataChange: " + riderToken);
                            /** Caso dê erro utilizar esse método
                             * FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
                             *                 @Override
                             *                 public void onSuccess(InstanceIdResult instanceIdResult) {
                             *                       String deviceToken = instanceIdResult.getToken();
                             *                       // Do whatever you want with your token now
                             *                       // i.e. store it on SharedPreferences or DB
                             *                       // or directly send it to server
                             *                 }
                             * });
                             */
                            Notification notification = new Notification(riderToken, json_lat_lng); //send it to driver app and we will deserialize it again
                            Sender content = new Sender(token.getToken(), notification); //send this data to token


                            mService.sendMessage(content)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success == 1) {
                                                Toast.makeText(Home.this, "Request sent", Toast.LENGTH_SHORT).show();
                                                Log.d("EDS", "onResponse: RESQUEST FOI");
                                            } else
                                                Toast.makeText(Home.this, "Failed !", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                                            Log.e("ERROR", t.getMessage());

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void requestPickupHere(String uid) {

        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        if (mUserMarker.isVisible())
            mUserMarker.remove();

        //add new Marker
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pickup Here")
                .snippet("")
                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mUserMarker.showInfoWindow();
        btnPickupRequest.setText("Getting your DRIVER....");

        findDriver();
    }

    private void findDriver() {

        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gfDrivers = new GeoFire(drivers);
        GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //if found
                if (!isDriverFound) {
                    isDriverFound = true;
                    driverID = key;
                    btnPickupRequest.setText("CALL DRIVER");
                    //Toast.makeText(Home.this, " " + key, Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //if still not found driver, increase distance
                if (!isDriverFound && radius < LIMIT) {

                    radius++;
                    findDriver();
                } else {

                    Toast.makeText(Home.this, "No avaliable driver near you!", Toast.LENGTH_SHORT).show();
                    btnPickupRequest.setText("REQUEST PICKUP");

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {


            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    buildLocationCallBack();
                    buildLocationRequest();
                    displayLocation();
                }

        }

    }

    private void setUpLocation() {
        //copiar do driver app

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                )) {
            //request Runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_CODE);
        } else {

            buildLocationRequest();
            buildLocationCallBack();
            displayLocation();
        }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    mLastLocation = location;

                }
                displayLocation();
            }
        };
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                )) {

            return;

        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {
                        mLastLocation = location;

                        if (mLastLocation != null) {

                            //create LatLng from mLastLocation and this is the center point
                            LatLng center = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                            //Distance in metters
                            //heading 0 is northSide, 90 is east, 180 is south and 270 is west
                            //base on compact
                            LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
                            LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);

                            LatLngBounds bounds = LatLngBounds.builder()
                                    .include(northSide)
                                    .include(southSide)
                                    .build();
                            RectangularBounds rectbonds = RectangularBounds.newInstance(bounds);

                            place_location.setLocationBias(rectbonds);
                            place_location.setCountry("br");
                            //  place_location.setTypeFilter(TypeFilter.ADDRESS);

                            place_destination.setLocationBias(rectbonds);
                            place_destination.setCountry("br");
                            //place_destination.setTypeFilter(TypeFilter.ADDRESS);


                            /**FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                             .setLocationBias(rectbonds)
                             .setCountry("br")
                             .setSessionToken(token)
                             .setTypeFilter(TypeFilter.ADDRESS)
                             .build();

                             /**                        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
                             mResult = new StringBuilder();
                             for(AutocompletePrediction prediction : response.getAutocompletePredictions()){

                             prediction.getFullText(null).toString();
                             }
                             });*/


                            //presence system
                            driversAvaliable = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
                            driversAvaliable.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    //if have any change from drivers table, we will reload all drivers avaliable
                                    loadAllAvaliableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                            final double latitude = mLastLocation.getLatitude();
                            final double longitude = mLastLocation.getLongitude();


                            loadAllAvaliableDrivers(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                        } else {

                            Log.d("ERROR", "cannot get your location: ");
                        }
                    }
                });


    }

    private void loadAllAvaliableDrivers(final LatLng location) {


        //here we will clear all map  to delete old position of driver

        mMap.clear();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(location)
                .title("Você"));


        //mover a camera para essa posição
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));


        //load all avaliable Driver in distance 3km

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gf = new GeoFire(driverLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude, location.longitude), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                //use key to get email from table Users
                //Table Users is table when driver register account and update information
                //Just open your Driver to check this table name
                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //Because Rider and User model has the same properties
                                //So we can use Rider model to get User here

                                Rider rider = dataSnapshot.getValue(Rider.class);

                                //add driver to map
                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude, location.longitude))
                                        .flat(true)
                                        .title(rider.getName())
                                        .snippet("Phone  : " + rider.getPhone())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <= LIMIT) //distance just find for 3km
                {
                    distance++;
                    loadAllAvaliableDrivers(location);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void buildLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        try {

            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_style_map)
            );

            if (!isSuccess)
                Log.e("ERROR", "map style load failed");
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                //first, check markDestination
                //if is not null, just remove available marker

                if (markerDestination != null)
                    markerDestination.remove();

                markerDestination = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                        .position(latLng)
                        .title("Destination"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                //show bottom sheet

                //%f no string format não convertia os valores.. estava dando msg de bug que deveria utilizar o Locale, mas com o %s funcionou
                BottomSheetRiderFragment mBottomSheet = (BottomSheetRiderFragment) BottomSheetRiderFragment.newInstance(String.format("%s,%s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                        String.format("%s,%s", latLng.latitude, latLng.longitude), true);
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
