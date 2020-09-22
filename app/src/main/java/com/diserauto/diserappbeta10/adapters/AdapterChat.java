package com.diserauto.diserappbeta10.adapters;

import android.content.Context; // or //import com.google.firebase.database.core.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diserauto.diserappbeta10.ChatActivity;
import com.diserauto.diserappbeta10.R;
import com.diserauto.diserappbeta10.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static  TextToSpeech mTTS;

    Context context;
    List<ModelChat> chatList;
    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i==MSG_TYPE_LEFT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        String message = chatList.get(i).getMessage();
        String timeStamp = chatList.get(i).getTimestamp();
        myHolder.messageTv.setText(message);

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));

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

        if((dayNow == Integer.valueOf(dateDay)) && (monthNow + 1 == Integer.valueOf(dateMonth)) &&(yearNow == Integer.valueOf(dateYear)))
        {
            myHolder.timeTv.setText(dateTime1);
        }
        else
        {
            if((monthNow + 1 == Integer.valueOf(dateMonth)) &&(yearNow == Integer.valueOf(dateYear)))
            {
                myHolder.timeTv.setText(dateTime2);
            }
            else
            {
                if(yearNow == Integer.valueOf(dateYear))
                    myHolder.timeTv.setText(dateTime3);
                else
                    myHolder.timeTv.setText(dateTime4);
            }

        }
        myHolder.timeTv.setAlpha(0f);

        /*if (i==chatList.size()-1)
        {
            if (chatList.get(i).getIsSeen()) {
                myHolder.isSeenTv.setText("Seen");
            }
            else {
                myHolder.isSeenTv.setText("Delivered");
            }
        }
        else {

            myHolder.isSeenTv.setVisibility(View.GONE);
        }
*/
            mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
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

        myHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                String text = myHolder.messageTv.getText().toString();
                mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);

                //myHolder.blurredTv.setVisibility(View.VISIBLE);

                //Animation animation = AnimationUtils.loadAnimation(context, R.anim.fadein);
                //myHolder.timeTv.setAlpha(1f);
                //myHolder.timeTv.setVisibility(View.VISIBLE);
                //myHolder.timeTv.startAnimation(animation);
                myHolder.timeTv.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .setListener(null);

               /*
                if(chatList.get(i).getSender().equals(fUser.getUid())) {
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.zoomin);

                    myHolder.itemView.startAnimation(animation);
                }
                else
                {
                    Animation animation1 = AnimationUtils.loadAnimation(context, R.anim.zoomin_left);

                    myHolder.itemView.startAnimation(animation1);
                }
                */


                return false;
            }
        });
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position){
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {

        TextView messageTv, timeTv, isSeenTv, blurredTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            blurredTv = itemView.findViewById(R.id.blurred);
            messageTv = itemView.findViewById(R.id.messageTV);
            timeTv = itemView.findViewById(R.id.timeTV);
            //isSeenTv = itemView.findViewById(R.id.isSeenTv);

        }
    }


    public void onInit(int status) {
        // TODO Auto-generated method stub
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

    public static void onDestroy() {
        try {
            mTTS.stop();
            mTTS.shutdown();
        } catch (Exception ignore) {
        }

    }

}