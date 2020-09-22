package com.diserauto.diserappbeta10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.paging.DataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.diserauto.diserappbeta10.adapters.AdapterChat;
import com.diserauto.diserappbeta10.models.ModelChat;
import com.diserauto.diserappbeta10.models.ModelUser;
import com.diserauto.diserappbeta10.notifications.Data;
import com.diserauto.diserappbeta10.notifications.Sender;
import com.diserauto.diserappbeta10.notifications.Token;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Exchanger;

import static com.diserauto.diserappbeta10.DashboardActivity.MyPREFERENCES;


public class ChatActivity extends AppCompatActivity
{

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileTV;
    TextView nameTV, userStatusTV;
    EditText messageEt;
    ImageButton sendBtn;

    //voice message
    private ImageButton mRecordButton;
    private MediaRecorder recorder;
    private String fileName = null;
    private static final String LOG_TAG = "Record_log";
    private ProgressDialog mProgress;

    StorageReference mStorage;
    StorageReference hStorage;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    DatabaseReference userrefForseen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    int i;
    String hisUid;
    String myUid;
    String myEmail;
    String myName;
    public volatile String message;
    String hisName;
    String hisEmail;

    //notifications
    private RequestQueue requestQueue;
    private boolean notify = false;

    //pagination
    Integer PagesScrolled;
    Boolean isScrolling = false;
    ProgressBar progressBar;
    int currentItems, totalItems, scrollOutItems;

    String hisImage;
    ActionBar actionBar;

    private static final String TAG = "ChatActivity";
    public static final String myDataPrefs = "myDataPrefs";

    //threads
    MyTask mt;
    sendTask st;

    //sos message
    public Boolean marker = false;
    public volatile String current_latitude;
    public volatile String current_longitude;
    String dialog_result;

    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            recyclerView = findViewById(R.id.chat_recyclerView);
            profileTV = findViewById(R.id.profileTV);
            nameTV = findViewById(R.id.nameTv);
            userStatusTV = findViewById(R.id.userStatusTv);
            messageEt = findViewById(R.id.messageEt);
            sendBtn = findViewById(R.id.sendBtn);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
            linearLayoutManager.setStackFromEnd(true);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            Intent intent = getIntent();
            hisUid = intent.getStringExtra("hisUid");
            PagesScrolled = 0;
            firebaseAuth = FirebaseAuth.getInstance();
            firebaseDatabase = FirebaseDatabase.getInstance();
            usersDbRef = firebaseDatabase.getReference("Users");
            progressBar = findViewById(R.id.progress);
            progressBar.setVisibility(View.VISIBLE);
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

                        hisName = ds.child("name").getValue().toString();
                        hisEmail = ds.child("email").getValue().toString();

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

                                //просто вывести время
                                String dateTime1 = DateFormat.format("HH:mm", cal).toString();
                                String dateTime2 = DateFormat.format("EEEE", cal).toString();
                                String dateTime3 = DateFormat.format("MMM dd", cal).toString();
                                String dateTime4 = DateFormat.format("MMM dd, yyyy", cal).toString();

                                String dateYear = DateFormat.format("yyyy", cal).toString();
                                String dateMonth = DateFormat.format("MM", cal).toString();
                                String dateDay = DateFormat.format("dd", cal).toString();

                                if ((dayNow == Integer.valueOf(dateDay)) && (monthNow + 1 == Integer.valueOf(dateMonth)) && (yearNow == Integer.valueOf(dateYear))) {
                                    userStatusTV.setText("Был в сети: " + dateTime1);                                } else {
                                    if ((dayNow/7 == Integer.valueOf(dateDay)/7)&&(monthNow + 1 == Integer.valueOf(dateMonth))&&(yearNow == Integer.valueOf(dateYear))) {
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
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            FirebaseUser user = firebaseAuth.getCurrentUser();
            myUid = user.getUid();
            final DatabaseReference chRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("Chatlist");
             chRef.child(hisUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        KeyboardVisibilityEvent.setEventListener(ChatActivity.this, new KeyboardVisibilityEventListener() {
                            @Override
                            public void onVisibilityChanged(boolean isOpen) {
                                // write your code
                                if(recyclerView.getAdapter() != null)
                                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                            }
                        });
                    } else {
                        //user does not exist, do something else
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    class sendTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //requestQueue = Volley.newRequestQueue(getApplicationContext());


        }

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences sharedPreferences = ChatActivity.this.getSharedPreferences(myDataPrefs, Context.MODE_PRIVATE);

            final Intent intent = getIntent();
            hisUid = intent.getStringExtra("hisUid");
            FirebaseUser user = firebaseAuth.getCurrentUser();
            myUid = user.getUid();
            myEmail = user.getEmail();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

            final String timestamp = String.valueOf(System.currentTimeMillis());

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", myUid);
            hashMap.put("receiver", hisUid);
            hashMap.put("message", message);
            hashMap.put("timestamp", timestamp);
            hashMap.put("isSeen", false);
            //hashMap.put("index", chatList.size() + 1);

            databaseReference.child(myUid).child("UserChats").child(hisUid).push().setValue(hashMap);
            databaseReference.child(hisUid).child("UserChats").child(myUid).push().setValue(hashMap);


            final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Users")
                    .child(myUid)
                    .child("Chatlist")
                    .child(hisUid);

            final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Users")
                    .child(hisUid)
                    .child("Chatlist")
                    .child(myUid);


            chatRef1.child("id").setValue(hisUid);
            if(hisName != null)
            chatRef1.child("name").setValue(hisName);
            if(hisEmail != null)
            chatRef1.child("email").setValue(hisEmail);
            chatRef1.child("sender").setValue(myUid);
            chatRef1.child("lastMessage").setValue(message);
            chatRef1.child("timestamp").setValue(timestamp);


            String name = sharedPreferences.getString("My_Name", "");
            String email = sharedPreferences.getString("My_Email", "");


            chatRef2.child("id").setValue(myUid);
            chatRef2.child("name").setValue(name);
            chatRef2.child("email").setValue(email);
            chatRef2.child("sender").setValue(myUid);
            chatRef2.child("lastMessage").setValue(message);
            chatRef2.child("timestamp").setValue(timestamp);
            chatRef2.child("isSeen").setValue(false);

            final int yearNow = Calendar.getInstance().get(Calendar.YEAR);
            final int monthNow = Calendar.getInstance().get(Calendar.MONTH);
            final int dayNow = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("UserChats").child(hisUid);
            final DatabaseReference refer = FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("UserChats").child(myUid);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        ModelChat chat = ds.getValue(ModelChat.class);
                        {

                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(chat.getTimestamp()));
                            String dateYear = DateFormat.format("yyyy", cal).toString();
                            String dateMonth = DateFormat.format("MM", cal).toString();
                            String dateDay = DateFormat.format("dd", cal).toString();
                            amountCh = dataSnapshot.getChildrenCount();


                            if((amountCh > 50) && (((yearNow != Integer.valueOf(dateYear)) || ((monthNow + 1 == Integer.valueOf(dateMonth)) && (dayNow - Integer.valueOf(dateDay) > 3)) || ((monthNow == Integer.valueOf(dateMonth)) && (dayNow > 3))))){
                                String msg = ds.getKey();
                                reference.child(msg).removeValue();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            refer.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                        ModelChat chat = ds.getValue(ModelChat.class);
                        {

                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(chat.getTimestamp()));
                            String dateYear = DateFormat.format("yyyy", cal).toString();
                            String dateMonth = DateFormat.format("MM", cal).toString();
                            String dateDay = DateFormat.format("dd", cal).toString();
                            amountCh = dataSnapshot.getChildrenCount();


                            if((amountCh > 50) && (((yearNow != Integer.valueOf(dateYear)) || ((monthNow + 1 == Integer.valueOf(dateMonth)) && (dayNow - Integer.valueOf(dateDay) > 3)) || ((monthNow == Integer.valueOf(dateMonth)) && (dayNow > 3))))){
                                String msg = ds.getKey();
                                reference.child(msg).removeValue();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ModelUser user = dataSnapshot.getValue(ModelUser.class);

                    if(notify){
                        sendNotification(hisUid, user.getName(), message);
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            if(marker){
                ChatActivity.this.finish();
            }
            return null;
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
            toolbar.setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileTV = findViewById(R.id.profileTV);
        nameTV = findViewById(R.id.nameTv);
        userStatusTV = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);

        //notifications
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        //voice message
        mRecordButton = findViewById(R.id.sendVoiceMBtn);
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";
        mStorage = FirebaseStorage.getInstance().getReference();
        mProgress = new ProgressDialog(this);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //use the actual Fused Location Provider API to get users current position

        final Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        current_latitude = sharedPreferences.getString("Current_Latitude", "None");
        current_longitude = sharedPreferences.getString("Current_Longitude", "None");
        final Intent intent1 = getIntent();
        dialog_result = intent1.getStringExtra("dialog_result");
        if(dialog_result != null){
            if(dialog_result.equals("1")){
                notify = true;
                message = "S.O.S." + "\n" + "Я здесь: " + current_latitude + ", " + current_longitude;
                st = new sendTask();
                st.execute();
            }
            else
                if(dialog_result.equals("2")){
                    notify = true;
                    message = "S.O.S." + "\n" + "Я здесь: " + current_latitude + ", " + current_longitude;
                    st = new sendTask();
                    st.execute();
                    marker = true;
            }
        }


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

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                message = messageEt.getText().toString().trim();

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatActivity.this, "Нельзя отправить пустое сообщение...", Toast.LENGTH_SHORT).show();
                }
                else {
                    st = new sendTask();
                    st.execute();
                    messageEt.setText("");
                }
            }
        });

        class readThread extends Thread {
            @Override
            public void run() {
                chatList = new ArrayList<>();
                hisUid = intent.getStringExtra("hisUid");
                FirebaseUser user = firebaseAuth.getCurrentUser();
                myUid = user.getUid();
                i = 0;
                final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("UserChats").child(hisUid);

                Query query = dbRef;
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                       //стирает и перезабивает заново RecyclerView

                        chatList.clear();

                        //пошаманить нужно чтоб не перезаписывались все сообщения, а добавлялсь только новые
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelChat chat = ds.getValue(ModelChat.class);
                            {
                                chatList.add(chat);
                            }

                            adapterChat = new AdapterChat(ChatActivity.this, chatList);
                            adapterChat.notifyDataSetChanged();
                            recyclerView.setAdapter(adapterChat);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isSeen", true);
/*
                            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                                        isScrolling = true;
                                    }
                                }

                                @Override
                                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);

                                    Log.d(TAG, "dx:" + dx);
                                    Log.d(TAG, "dy:" + dy);

                                    if(dy <= -70){
                                        isScrolling = false;
                                        fetchData();
                                    }

                                }
                            });
*/
                            //прокрутка вниз recyclerView
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                super.run();
            }
        }

        class seenThread extends Thread{
            public void run(){
                hisUid = intent.getStringExtra("hisUid");
                FirebaseUser user = firebaseAuth.getCurrentUser();
                myUid = user.getUid();
                userRefForSeen = FirebaseDatabase.getInstance().getReference("UserChats").child(myUid).child(hisUid);
                userrefForseen = FirebaseDatabase.getInstance().getReference("UserChats").child(hisUid).child(myUid);
                seenListener = userRefForSeen.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {

                            ModelChat chat = ds.getValue(ModelChat.class);

                            if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                                HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                                hasSeenHashMap.put("isSeen", true);
                                ds.getRef().updateChildren(hasSeenHashMap);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                seenListener = userrefForseen.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {

                            ModelChat chat = ds.getValue(ModelChat.class);

                            if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                                HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                                hasSeenHashMap.put("isSeen", true);
                                ds.getRef().updateChildren(hasSeenHashMap);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        readThread rThread = new readThread();

        rThread.start();

        //seenThread snThread = new seenThread();

        //snThread.start();
        //seenMessage();
    }

    //voice message recording
    private void startRecording() {

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fadein);
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
    private void fetchData() {
        Log.d(TAG, "SCROLLED");
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //fetch new data
                    i+= 20;
                Log.d(TAG, "i:" + i);
                    adapterChat.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

            }
        }, 1000);
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {

                    ModelChat chat = ds.getValue(ModelChat.class);

                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(final String hisUid, final String name, final String message)
    {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUid, name +": " + message, "Новое сообщение", hisUid, R.drawable.ic_logo2);

                    Sender sender = new Sender(data, token.getToken());

                    //fcm json object request
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //response of the request
                                        Log.d("JSON_RESPONSE", "onResponse: " + response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: " + error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                //put params
                                Map<String , String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAoR8gdkA:APA91bFvnLcSKJ52TfpE88jZJWVUrsMHtrBEoK0_K-BLRVoLLydaGLZDVAJofX86QnNUYUWVLD4Kj9vkmXIke07HSWV4pfTx0gfXZHouhWo_VTCc9hQvT0kv7xP-J2ornJZsdPiEzBQa");

                                return headers;
                            }
                        };

                        //Add this to request
                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        menu.findItem(R.id.action_logout).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout)
        {
            firebaseAuth.signOut();

            SharedPreferences sp = ChatActivity.this.getSharedPreferences(myDataPrefs, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("My_Email", "");
            editor.putString("My_Name", "");
            editor.putString("My_Phone", "");
            editor.apply();

            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    Long amountCh = Long.valueOf(0);

    @Override
    public void onDestroy() {
        AdapterChat.onDestroy();
        super.onDestroy();
    }
}
