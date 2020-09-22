package com.diserauto.diserappbeta10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.diserauto.diserappbeta10.adapters.AdapterChatlist;
import com.diserauto.diserappbeta10.notifications.Token;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    FloatingActionButton voiceAssis;
    int PERMISSION_ID = 44;
    String mUID;
    public static final String MyPREFERENCES = "myprefs";
    public static final String myDataPrefs = "myDataPrefs";
    public volatile String current_latitude;
    public volatile String current_longitude;
    FusedLocationProviderClient mFusedLocationClient;
    AdapterChatlist adapterChatlist;
    boolean firstt = true;
    volatile static int i;
    String NewOne;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Профиль");

        firebaseAuth = FirebaseAuth.getInstance();
        voiceAssis = findViewById(R.id.assis);
        voiceAssis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, VoiceAssistantActivity.class));
            }
        });

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        i = 0;
        actionBar.setTitle("Профиль");
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();
        navigationView.setSelectedItemId(R.id.nav_home);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLastLocation();

        checkUserStatus();

        //update token
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }

    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }

    private void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            if (actionBar.getTitle() != "Сообщения" || (firstt == true && actionBar.getTitle() == "Сообщения"))
                            {
                                firstt = false;
                                actionBar.setTitle("Сообщения");
                                HomeFragment fragment1 = new HomeFragment();
                                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                                ft1.replace(R.id.content, fragment1, "");
                                ft1.commit();
                            }

                            return true;


                        case R.id.nav_profile:
                            if (actionBar.getTitle() != "Профиль")
                            {
                                actionBar.setTitle("Профиль");
                                ProfileFragment fragment2 = new ProfileFragment();
                                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                                ft2.replace(R.id.content, fragment2, "");
                                ft2.commit();
                            }
                            return true;


                        case R.id.nav_users:
                            if (actionBar.getTitle() != "Пользователи")
                            {
                                actionBar.setTitle("Пользователи");
                                UsersFragment fragment3 = new UsersFragment();
                                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                                ft3.replace(R.id.content, fragment3, "");
                                ft3.commit();
                            }
                            return true;
                    }
                    return false;
                }

            };

    private void checkUserStatus()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null)
        {
            mUID = user.getUid();

            //потом сохранять также и тяжеловесные данные
            //save uid of currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
        }
        else
        {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart()
    {
        checkUserStatus();
        super.onStart();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            i++;
            Log.v("#@%@%#", "Volume button is pressed " + i + " times");
            if (i == 6) {
                Log.v("#@%@%#", "((((((");
               // notifyAllUsers();
                Intent intent1 = new Intent(this, AdapterChatlist.class);
                intent1.putExtra("clickAmount", i);
            }

            eraseThread eThread = new eraseThread();

            eThread.start();
        }
        return true;
    }

    class eraseThread extends Thread{
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
                i = 0;
            } catch (InterruptedException qq) {
                qq.printStackTrace();
            }


            super.run();
        }
    }

    //save to sp
    private void saveText() {

        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Current_Latitude", current_latitude);
        editor.putString("Current_Longitude", current_longitude);
        editor.apply();
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    current_latitude = location.getLatitude()+"";
                                    current_longitude = location.getLongitude()+"";
                                    saveText();
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }




    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        //mLocationRequest.setNumUpdates(1);
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(20000);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            current_latitude = mLastLocation.getLatitude()+"";
            current_longitude = mLastLocation.getLongitude()+"" ;
            saveText();
        }
    };



    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }


}
