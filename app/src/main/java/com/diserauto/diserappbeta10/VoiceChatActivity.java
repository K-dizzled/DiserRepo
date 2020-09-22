package com.diserauto.diserappbeta10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.diserauto.diserappbeta10.adapters.AdapterChat;
import com.diserauto.diserappbeta10.models.ModelChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.diserauto.diserappbeta10.DashboardActivity.MyPREFERENCES;

public class VoiceChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    ImageView profileTV;
    TextView nameTV, userStatusTV;
    EditText messageEt;
    ImageButton sendBtn;

    //voice message
    private ImageButton mRecordButton;
    private ImageButton mListenButton;
    private MediaRecorder recorder;
    MediaPlayer mediaPlayer;
    private boolean playPause;
    private boolean initialStage = true;
    ProgressDialog progressDialog;
    private String fileName = null;
    private static final String LOG_TAG = "Record_log";
    private ProgressDialog mProgress;
    public String messageUri;
    private String message_URI;

    StorageReference mStorage;
    StorageReference hStorage;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    int i;
    String hisUid;
    String myUid;

    //notifications
    private RequestQueue requestQueue;
    private boolean notify = false;


    String hisImage;
    ActionBar actionBar;

    private static final String TAG = "ChatActivity";

    //threads
    MyTask mt;

    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            profileTV = findViewById(R.id.profileTV);
            nameTV = findViewById(R.id.nameTv);
            userStatusTV = findViewById(R.id.userStatusTv);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(VoiceChatActivity.this);
            linearLayoutManager.setStackFromEnd(true);
            Intent intent = getIntent();
            hisUid = intent.getStringExtra("hisUid");
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseDatabase = FirebaseDatabase.getInstance();
            usersDbRef = firebaseDatabase.getReference("Users");

            //Поток для отправки сообщений


        }

        @Override
        protected Void doInBackground(Void... voids) {
            Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
            userQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        String name =""+ ds.child("name").getValue();

                        //hisImage =""+ ds.child("image").getValue();
                        String typingStstus =""+ ds.child("typingTo").getValue();

                        if(typingStstus.equals(myUid)){
                            userStatusTV.setText("Печатает...");
                        }
                        else {
                            String onlineStatus = "" + ds.child("onlineStatus").getValue();
                            if (onlineStatus.equals("online")) {
                                userStatusTV.setText(onlineStatus);
                            } else {
                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                cal.setTimeInMillis(Long.parseLong(onlineStatus));

                                int yearNow = Calendar.getInstance().get(Calendar.YEAR);
                                int monthNow = Calendar.getInstance().get(Calendar.MONTH);
                                int dayNow = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

                                String dateTime1 = DateFormat.format("hh:mm aa", cal).toString();
                                String dateTime2 = DateFormat.format("d/ hh:mm aa", cal).toString();
                                String dateTime3 = DateFormat.format("M/d/ hh:mm aa", cal).toString();
                                String dateTime4 = DateFormat.format("M/d/yyyy/ hh:mm aa", cal).toString();

                                String dateYear = DateFormat.format("yyyy", cal).toString();
                                String dateMonth = DateFormat.format("MM", cal).toString();
                                String dateDay = DateFormat.format("dd", cal).toString();

                                if ((dayNow == Integer.valueOf(dateDay)) && (monthNow + 1 == Integer.valueOf(dateMonth)) && (yearNow == Integer.valueOf(dateYear))) {
                                    userStatusTV.setText("Был в сети: " + dateTime1);
                                } else {
                                    if ((monthNow + 1 == Integer.valueOf(dateMonth)) && (yearNow == Integer.valueOf(dateYear))) {
                                        userStatusTV.setText("Был в сети: " + dateTime2);
                                    } else {
                                        if (yearNow == Integer.valueOf(dateYear))
                                            userStatusTV.setText("Был в сети: " + dateTime3);
                                        else
                                            userStatusTV.setText("Был в сети: " + dateTime4);
                                    }

                                }
                            }
                        }

                        if(!name.equals(""))
                            nameTV.setText(name);
                        else
                            nameTV.setText("Некто");

                        Picasso.get().load(R.drawable.ic_planet_icon).into(profileTV);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            FirebaseUser user = firebaseAuth.getCurrentUser();
            myUid = user.getUid();


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        profileTV = findViewById(R.id.profileTV);
        nameTV = findViewById(R.id.nameTv);
        userStatusTV = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        progressBar = findViewById(R.id.progress);

        //voice message
        mRecordButton = findViewById(R.id.sendVoiceMessageBtn);
        mListenButton = findViewById(R.id.listenLastVoiceMBtn);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        progressDialog = new ProgressDialog(this);

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";
        mStorage = FirebaseStorage.getInstance().getReference();
        mProgress = new ProgressDialog(this);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        final Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");


        mt = new MyTask();
        mt.execute();

        mRecordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    requestAudioPermissions();
                }
                else if((event.getAction() == MotionEvent.ACTION_UP) && (recorder!= null)){
                    stopRecording();
                }
                return false;
            }
        });

        mListenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playPause){
                    mListenButton.setImageResource(R.drawable.ic_pause_circle_outline_brown_200dp);
                    if(initialStage){
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                        StorageReference audioReference = storageReference.child("UserChat_VoiceMessages").child(myUid).child(hisUid).child("Audio_"+hisUid+".3gp");
                        audioReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.e("Tuts+", "uri: " + uri.toString());
                                //Handle whatever you're going to do with the URL here
                                messageUri = uri.toString();
                                new Player().execute(messageUri);
                            }
                        });
                    }
                    else
                    {
                        if(!mediaPlayer.isPlaying()){
                            mediaPlayer.start();
                        }
                    }
                    playPause = true;
                }
                else
                {
                    mListenButton.setImageResource(R.drawable.ic_play_circle_outline_brown_200dp);
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                    }
                    playPause = false;
                }
            }
        });
    }



    class Player extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading...");
             progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            Boolean prepared = false;
            try{
                mediaPlayer.setDataSource(strings[0]);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        initialStage = true;
                        playPause = false;
                        mListenButton.setImageResource(R.drawable.ic_play_circle_outline_brown_200dp);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });
                mediaPlayer.prepare();
                prepared = true;
            } catch (IOException e) {
                Log.e("DiserApp", e.getMessage());
                prepared = false;
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(progressDialog.isShowing()){
                progressDialog.cancel();
            }
            mediaPlayer.start();
            initialStage = false;
        }
    }

    private void startRecording() {

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.voice_record);
        mRecordButton.startAnimation(animation);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {

        try {
            recorder.stop();
            recorder.release();
            recorder = null;
            mRecordButton.clearAnimation();
            uploadAudio();
        } catch(RuntimeException stopException) {
            // handle cleanup here
            recorder = null;
            mRecordButton.clearAnimation();
        }

        //recorder.release();
        recorder = null;

        //uploadAudio();
    }

    private void uploadAudio() {
        mProgress.setMessage("Sending...");
        mProgress.show();

        final Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");
        FirebaseUser user = firebaseAuth.getCurrentUser();
        myUid = user.getUid();

        StorageReference myfilepath = mStorage.child("UserChat_VoiceMessages").child(myUid).child(hisUid).child("Audio_"+ myUid + ".3gp");
        StorageReference hisfilepath = mStorage.child("UserChat_VoiceMessages").child(hisUid).child(myUid).child("Audio_"+ myUid + ".3gp");

        Uri uri = Uri.fromFile(new File(fileName));
        myfilepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mProgress.dismiss();
            }
        });
        Uri uri1 = Uri.fromFile(new File(fileName));
        hisfilepath.putFile(uri1);
    }

    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            startRecording();
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    startRecording();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myUid = user.getUid();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        //userRefForSeen.removeEventListener(seenListener);
        super.onPause();
        /*
        if(mediaPlayer != null){
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        */
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
