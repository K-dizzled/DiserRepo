package com.diserauto.diserappbeta10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.diserauto.diserappbeta10.models.ModelChat;
import com.diserauto.diserappbeta10.models.ModelChatlist;
import com.diserauto.diserappbeta10.models.ModelUser;
import com.diserauto.diserappbeta10.notifications.Data;
import com.diserauto.diserappbeta10.notifications.Sender;
import com.diserauto.diserappbeta10.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.Manifest.permission.RECORD_AUDIO;
import static com.diserauto.diserappbeta10.DashboardActivity.MyPREFERENCES;

public class VoiceAssistantActivity extends AppCompatActivity {

    //notifications
    private RequestQueue requestQueue;
    private boolean notify = false;

    private SpeechRecognizer speechRecognizer;
    private SpeechRecognizer speechRecognizer1;
    private SpeechRecognizer speechRecognizer2;
    public volatile String current_latitude;
    public volatile String current_longitude;
    private Intent intentRecognizer;
    private TextView textView;
    ImageButton imageButton;
    FirebaseAuth firebaseAuth;
    DatabaseReference ChatlistsRef;
    String myUid;
    String myEmail;
    String hisEmail;
    String action;
    TextToSpeech mTTS;
    String hisUid;
    String hisName;
    Boolean result;
    String message;
    public Boolean isWorking = false;
    Long amountCh = Long.valueOf(0);
    waitThread wThread;
    public static final String myDataPrefs = "myDataPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_voice_assistant);

        imageButton = findViewById(R.id.reco);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        myUid = user.getUid();
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);
        textView = findViewById(R.id.textView);
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH  );
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer1 = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer2 = SpeechRecognizer.createSpeechRecognizer(this);
        result = false;
        action = "";

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {

                    Locale locale = new Locale("ru");

                    int result = mTTS.setLanguage(locale);
                    //int result = mTTS.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Извините, этот язык не поддерживается");
                    } else {
                    }

                } else {
                    Log.e("TTS", "Ошибка!");
                }
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.startListening(intentRecognizer);
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Animation animation = AnimationUtils.loadAnimation(VoiceAssistantActivity.this, R.anim.voice_record);
                imageButton.startAnimation(animation);
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                Log.d("#@#@", "SpeechRec");
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String string;
                if(matches!=null) {
                    string = matches.get(0);
                            if ((string.trim().equals("отправить")) || (string.trim().equals("сообщение")) || (string.trim().equals("отправить сообщение")))
                            {
                                //mTTS.speak("Кому бы вы хотели отправить сообщение?", TextToSpeech.QUEUE_FLUSH, null);
                                action = "sendMsg";
                                textView.setText(string);
                                speechRecognizer1.startListening(intentRecognizer);
                            }
                            else
                            {
                                if((string.trim().equals("sos")) || (string.trim().equals("сос"))){
                                    action = "sendSOS";
                                    textView.setText(string);
                                    speechRecognizer1.startListening(intentRecognizer);
                                }
                                else
                                {
                                    action = "";
                                    mTTS.speak("Не уверена, что поняла вас", TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                            if(string.trim().equals("время") || string.trim().equals("сколько время") || string.trim().equals("время сейчас") || string.trim().equals("time"))
                            {
                                final String timestamp = String.valueOf(System.currentTimeMillis());
                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                cal.setTimeInMillis(Long.parseLong(timestamp));
                                String day = DateFormat.format("EEEE", cal).toString();
                                String time = DateFormat.format("HH:mm", cal).toString();
                                textView.setText(day + "\n" + time);
                                mTTS.speak("Сейчас " + day + ", " + time, TextToSpeech.QUEUE_FLUSH, null);

                            }

                }
                imageButton.clearAnimation();

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        speechRecognizer1.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Animation animation = AnimationUtils.loadAnimation(VoiceAssistantActivity.this, R.anim.voice_record);
                imageButton.startAnimation(animation);
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                Log.d("#@#@", "SpeechRec1");
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String string;
                if(matches!=null) {
                    string = matches.get(0);

                    textView.setText(string);

                    if(action.equals("sendSOS") && (string.equals("всем") || string.equals("все")) && (!isWorking))
                    {
                        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

                        current_latitude = sharedPreferences.getString("Current_Latitude", "None");
                        current_longitude = sharedPreferences.getString("Current_Longitude", "None");

                        message = "S.O.S." + "\n" + "Я здесь: " + current_latitude + ", " + current_longitude;

                        DatabaseReference chatlistsRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("Chatlist");
                        chatlistsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                                    ModelChatlist chat = ds.getValue(ModelChatlist.class);
                                    {
                                       sendMessage(chat.getId(), message);

                                    }
                                  }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    else
                    {
                        if(action.equals("sendMsg") || action.equals("sendSOS"))
                            checkName(string);
                    }

                }
                imageButton.clearAnimation();

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        speechRecognizer2.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Animation animation = AnimationUtils.loadAnimation(VoiceAssistantActivity.this, R.anim.voice_record);
                imageButton.startAnimation(animation);
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                Log.d("#@#@", "SpeechRec2");
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String string;
                if(matches!=null) {
                    string = matches.get(0);
                    if(!isWorking)
                        sendMessage(hisUid, string);
                    textView.setText("Отправлено");
                    mTTS.speak("Сообщение " + string + " Отправлено", TextToSpeech.QUEUE_FLUSH, null);
                    action = "";
                }
                imageButton.clearAnimation();
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    //Все проверки пройдены и данные получены
    private void sendMessage(final String hisUid, final String message) {
        notify = true;
        isWorking = true;
        waitThread wThread = new waitThread();
        wThread.start();
        speechRecognizer2.stopListening();
        Log.d("#@#@", "SendMessage");
        SharedPreferences sharedPreferences = VoiceAssistantActivity.this.getSharedPreferences(myDataPrefs, Context.MODE_PRIVATE);

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

        //Завершено
    }


    public void checkName(final String name){
        Log.d("#@#@", "CheckName");
        result = false;
        DatabaseReference chatlistsRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("Chatlist");
        chatlistsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChatlist chat = ds.getValue(ModelChatlist.class);
                    {
                        if(chat.getName().trim().equals(name.trim())){
                            if(action.equals("sendMsg")) {
                                result = true;
                                hisUid = chat.getId();
                                speechRecognizer2.startListening(intentRecognizer);
                            }
                            else
                            {
                                SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

                                current_latitude = sharedPreferences.getString("Current_Latitude", "None");
                                current_longitude = sharedPreferences.getString("Current_Longitude", "None");
                                hisUid = chat.getId();
                                message = "S.O.S." + "\n" + "Я здесь: " + current_latitude + ", " + current_longitude;
                                if(!isWorking)
                                    sendMessage(hisUid, message);
                            }
                        }
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

    class waitThread extends Thread{
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
                isWorking = false;
            } catch (InterruptedException qq) {
                qq.printStackTrace();
            }


            super.run();
        }
    }

    @Override
    protected void onStop() {
        try {
            mTTS.stop();
            mTTS.shutdown();
        } catch (Exception ignore) {
        }
        super.onStop();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        speechRecognizer.stopListening();
        speechRecognizer1.stopListening();
        speechRecognizer2.stopListening();
    }
}
