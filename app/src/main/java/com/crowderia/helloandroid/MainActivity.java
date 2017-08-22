package com.crowderia.helloandroid;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TimePicker;
import android.widget.Toast;

import com.crowderia.helloandroid.common.AlertDialogUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Button;
//import android.location.LocationListener;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.TimePickerDialog;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        LocationListener {

    /* Position */
    private static final int MINIMUM_TIME = 5000;  // 10s
    private static final int MINIMUM_DISTANCE = 3; // 50m

    private static final int ALARM_DISTANCE = 50;
    private  static final int ZOOM_LEVEL = 15;

    private LocationManager locationManager;
    private String mProviderName;
    private LatLng destination;

    AlarmManager alarmManager;
    MediaPlayer mMediaPlayer = new MediaPlayer();
    GoogleMap mGoogleMap;
    Location mLastLocation;

    AlertDialog dialogSetalarmDistance;

    int hour ;
    int minute;

    public static float distanceBetween(Location myLocation, LatLng destLatLng) {
        Location dest = new Location("");
        dest.setLatitude(destLatLng.latitude);
        dest.setLongitude(destLatLng.longitude);

        return myLocation.distanceTo(dest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDrawer();
        initNavigationView();
        initMapFragment();
        initLocationManager();
    }

    protected void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    protected void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void initMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void initLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Get the best provider between gps, network and passive
        Criteria criteria = new Criteria();
        mProviderName = locationManager.getBestProvider(criteria, true);

        if (PermissionUtil.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                && PermissionUtil.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {        // No one provider activated: prompt GPS
            if (mProviderName == null || mProviderName.equals("")) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }

            locationManager.requestLocationUpdates(mProviderName, MINIMUM_TIME, MINIMUM_DISTANCE, this);
            Location location = locationManager.getLastKnownLocation(mProviderName);
            if (location != null) {
                onLocationChanged(location);
            }

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        } else {

            if (!PermissionUtil.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                PermissionUtil.requestPermissions(this, "ACCESS_COARSE_LOCATION");
            }
            if (!PermissionUtil.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionUtil.requestPermissions(this, "ACCESS_FINE_LOCATION");
            }
        }
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng latLng) {
        destination = latLng;
    }

    @Override
    public void onLocationChanged(Location location) {

        Toast.makeText(getApplicationContext(), "onLocationChanged", Toast.LENGTH_SHORT).show();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String gpsOnTime = preferences.getString("GPS_ON_TIME", "");
        Toast.makeText(getApplicationContext(), "GPS_ON_TIME -" + gpsOnTime, Toast.LENGTH_SHORT).show();

        mLastLocation = location;

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //move map camera
        if(mGoogleMap!=null){
            LatLng myLatLng = getMyLatLng();
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, ZOOM_LEVEL));
        }

        // location alarm

        if (!mMediaPlayer.isPlaying()) {
            LatLng dest = getDestination();

            if (dest != null) {
                Toast.makeText(getApplicationContext(), "destination lat lon" + (float) destination.latitude + " : " + (float) destination.longitude, Toast.LENGTH_SHORT).show();

                float dist = distanceBetween(location, dest);

                Integer distance = preferences.getInt("ALARM_DISTANCE", 0);
                Toast.makeText(getApplicationContext(), "SharedPreferences-" + distance, Toast.LENGTH_SHORT).show();

                if(distance == null || distance.equals(0))
                {
                    distance = ALARM_DISTANCE;
                }else {
                    distance = distance;
                }

                if (dist < ALARM_DISTANCE) {
                    Toast.makeText(getApplicationContext(), "dist < 80", Toast.LENGTH_SHORT).show();
                    playAlarm();

                } else {
                    Toast.makeText(getApplicationContext(), "dist else", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onProviderDisabled(String arg0) {

        Log.i("called", "onProviderDisabled");
    }

    @Override
    public void onProviderEnabled(String arg0) {

        Log.i("called", "onProviderEnabled");
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

        Log.i("called", "onStatusChanged");
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        LatLng myLatLng = getMyLatLng();
        // LatLng sydney = new LatLng(-34, 151);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));

        return false;
    }

    private Location
    getMyLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (PermissionUtil.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            PermissionUtil.requestPermissions(this, "ACCESS_COARSE_LOCATION");
        }
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        }

        return myLocation;
    }

    public LatLng getMyLatLng() {
        Location myLocation = getMyLocation();
        LatLng myLatLng;
        if (myLocation != null) {
            myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        } else {
            myLatLng = new LatLng(0, 0);
        }
        return myLatLng;
    }

    private void playAlarm() {

        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            mMediaPlayer.setDataSource(this, alert);

            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            int maxVolume = audioManager.getStreamVolume(audioManager.STREAM_RING);
//            int maxVolume = audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();

        } catch (Exception e) {
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;


        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionUtil.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Location Permission already granted
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                PermissionUtil.requestPermissions(this, "ACCESS_FINE_LOCATION");
            }
        } else {
            mGoogleMap.setMyLocationEnabled(true);
        }

        enableMyLocation();
        LatLng myLatLng = getMyLatLng();
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, ZOOM_LEVEL));
        mGoogleMap.setMaxZoomPreference(mGoogleMap.getMaxZoomLevel());
        mGoogleMap.setOnMyLocationButtonClickListener(this);
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {


                setDestination(latLng);
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                markerOptions.draggable(true);

                // Clears the previously touched position
                mGoogleMap.clear();

                // Animating to the touched position
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mGoogleMap.setMaxZoomPreference(mGoogleMap.getMaxZoomLevel());

                // Placing a marker on the touched position
                mGoogleMap.addMarker(markerOptions);
                Toast.makeText(getApplicationContext(), "MY DESTINATION lat lon" + (float) latLng.latitude + " : " + (float) latLng.longitude, Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {

        if (!PermissionUtil.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            PermissionUtil.requestPermissions(this, "ACCESS_COARSE_LOCATION");
        }
        if (!PermissionUtil.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PermissionUtil.requestPermissions(this, "ACCESS_FINE_LOCATION");
        } else if (mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(true);
//            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_alarm_distance) {
            AlertDialog.Builder builderSetAlarmDistance = setalarmDistance();
            dialogSetalarmDistance = builderSetAlarmDistance.create();
            dialogSetalarmDistance.show();

            final Button buttonSave = dialogSetalarmDistance.getButton(AlertDialog.BUTTON_POSITIVE);
            final EditText alarmDistance = (EditText)dialogSetalarmDistance.findViewById(R.id.set_alarm_distance);

            buttonSave.setOnClickListener(new OnClickListener() {
                public void onClick(final View v){
                    String distance = alarmDistance.getText().toString();
                    int intDistance=Integer.parseInt(distance);

//                    Log.i("view",alarmDistance.getText().toString());
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("ALARM_DISTANCE", intDistance);
                    editor.apply();
                    dialogSetalarmDistance.dismiss();
                }
            });

        } else if (id == R.id.nav_gps_on_time) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                            LocalTime time1 = new LocalTime(hourOfDay, minute);
                            Calendar now = Calendar.getInstance();
                            now.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            now.set(Calendar.MINUTE, minute);

                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String formattedDate = df.format(now.getTime());

                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("GPS_ON_TIME", formattedDate);
                            editor.apply();
                            Log.d("timepick",formattedDate) ;
                            turnGPSOff();

                        }
                    }, hour, minute, DateFormat.is24HourFormat(MainActivity.this));
            timePickerDialog.show();

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            // startActivity( , MapsActivity.class));
            // startActivity(new Intent(getApplicationContext(),MapsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public AlertDialog.Builder setalarmDistance() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
//        LayoutInflater inflater = this.getLayoutInflater();
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.dialog_alarm_distance, null);
        builder.setTitle(getResources().getString(R.string.set_alarm_distance));
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("save",dialog.toString()+"-"+id);

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("cancel",dialog.toString()+"-"+id);
                    }
                });

        return builder;
    }

    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    private void turnGPSOff(){
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", false);
        sendBroadcast(intent);
    }
}
