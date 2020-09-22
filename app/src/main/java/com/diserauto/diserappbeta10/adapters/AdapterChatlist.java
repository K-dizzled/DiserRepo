package com.diserauto.diserappbeta10.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.diserauto.diserappbeta10.ChatActivity;

import com.diserauto.diserappbeta10.DashboardActivity;
import com.diserauto.diserappbeta10.R;
import com.diserauto.diserappbeta10.VoiceChatActivity;
import com.diserauto.diserappbeta10.models.ModelChatlist;
import com.diserauto.diserappbeta10.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    Context context;
    List<ModelUser> userList;

    List<ModelChatlist> chatlistList;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    private HashMap<String, String> lastMessageMap;
    private HashMap<String, String> lastM_timeStamp;
    private HashMap<String, HashMap<String, String>> chatlistMap; // hashMAp<time, HashMap<Uid, message>>;
    DialogFragment dialog1;
    volatile static int i;
    public static String DIALOG_RESULT = "result";

    //shared preferences
    public static final String VISIONprefs = "visionPrefs";

    public String mInput;


    public AdapterChatlist(Context context, List<ModelChatlist> chatlistList) {
        this.context = context;
        this.chatlistList = chatlistList;
        lastMessageMap = new HashMap<>();
        lastM_timeStamp = new HashMap<>();
        chatlistMap = new HashMap<>();

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist,viewGroup, false);
        /*
        final Intent intent1 = ((DashboardActivity) context).getIntent();
        if(intent1.getStringExtra("clickAmount") != null) {
            if (intent1.getStringExtra("clickAmount").equals("6")) {
                notifyAllUsers();
            }
        }
        */

        return new MyHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyHolder myholder, int i) {



        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        final String hisUid = chatlistList.get(i).getId();

        String userName = chatlistList.get(i).getName();
        String userEmail = chatlistList.get(i).getEmail();
        String lastMessage = chatlistList.get(i).getLastMessage();
        String lastTime = chatlistList.get(i).getTimestamp();
        String sender = chatlistList.get(i).getSender();

        //String userImage = userList.get(i).getImage();




        if(!userName.equals("")){
            myholder.nameTv.setText(userName);

        }
        else
        {
            myholder.nameTv.setTextSize(18);
            myholder.nameTv.setText(userEmail);
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(VISIONprefs, Context.MODE_PRIVATE);

        if(!sharedPreferences.getBoolean("Chat_version_blind", false)) {
            if (lastMessage == null || lastMessage.equals("default")) {
                myholder.lastTimeTv.setVisibility(View.GONE);
                myholder.lastMessageTv.setVisibility(View.GONE);
            } else {
                myholder.lastMessageTv.setVisibility(View.VISIBLE);
                if(sender.equals(currentUser.getUid())){
                    myholder.lastMessageTv.setText("Вы: " + lastMessage);
                }
                else
                {
                    myholder.lastMessageTv.setText(lastMessage);
                }
                myholder.lastTimeTv.setVisibility(View.VISIBLE);


                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(Long.parseLong(lastTime));

                int yearNow = Calendar.getInstance().get(Calendar.YEAR);
                int monthNow = Calendar.getInstance().get(Calendar.MONTH);
                int dayNow = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                //long weekNow = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH);

                //просто вывести время
                String dateTime1 = DateFormat.format("HH:mm", cal).toString();
                String dateTime2 = DateFormat.format("EEE", cal).toString();
                String dateTime3 = DateFormat.format("MMM dd", cal).toString();
                String dateTime4 = DateFormat.format("MMM dd, yyyy", cal).toString();

                String dateYear = DateFormat.format("yyyy", cal).toString();
                String dateMonth = DateFormat.format("MM", cal).toString();
                String dateDay = DateFormat.format("dd", cal).toString();
                //String dateWeek = DateFormat.format("W", cal).toString();

                Log.d("#@#@#", "dateyear: "+dateYear);
                Log.d("#@#@#", "dateWeek: "+dayNow/7);
                Log.d("#@#@", String.valueOf(Integer.valueOf(dateDay)/7));

                if ((dayNow == Integer.valueOf(dateDay)) && (monthNow + 1 == Integer.valueOf(dateMonth)) && (yearNow == Integer.valueOf(dateYear))) {
                    myholder.lastTimeTv.setText(dateTime1);
                } else {
                    if ((dayNow/7 == Integer.valueOf(dateDay)/7)&&(monthNow + 1 == Integer.valueOf(dateMonth))&&(yearNow == Integer.valueOf(dateYear))) {
                        myholder.lastTimeTv.setText(dateTime2);
                    } else {
                        if (yearNow == Integer.valueOf(dateYear))
                            myholder.lastTimeTv.setText(dateTime3);
                        else
                            myholder.lastTimeTv.setText(dateTime4);
                    }

                }
            }
        }
        else
        {
            myholder.lastMessageTv.setText("Audio");
            myholder.lastTimeTv.setVisibility(View.GONE);
        }
        //Временно убираю картинку у пользователя, дабы нормально работать с приложением и не ловить столько ошибок
        /*
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_planet_icon).into(myholder.profileIv);
        }
        catch (Exception e)
        {
            Picasso.get().load(R.drawable.ic_planet_icon).into(myholder.profileIv);

        }
*/
        Picasso.get().load(R.drawable.ic_planet_icon).into(myholder.profileIv);




        //turning off online status
        /*
        if(onlineStatus.equals("online"))
        {
            Picasso.get().load(R.drawable.online_c).into(myholder.onlineStatusIv);
        }
        else
        {
            //offline
        }
         */


        myholder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(VISIONprefs, Context.MODE_PRIVATE);
                if(sharedPreferences.getBoolean("Chat_version_blind", false)){
                    Intent intent1 = new Intent(context, VoiceChatActivity.class);
                    intent1.putExtra("hisUid", hisUid);
                    context.startActivity(intent1);
                }
                else
                {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("hisUid", hisUid);
                    context.startActivity(intent);
                    Thread.currentThread().setPriority(10);
                }

            }
        });

        myholder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialog(v, hisUid);
                return false;
            }
        });
    }

    public void showDialog(View view, final String hisUid){
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);

        builder.setTitle("S.O.S.")
                .setMessage("Вы в опасности? Отправить сигнал и координаты:")
                .setCancelable(false)
                .setIcon(R.drawable.ic_error_black_24dp)
                .setPositiveButton( "Этому пользователю", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Отправлено", Toast.LENGTH_LONG).show();
                         DIALOG_RESULT = "1";
                         sendDialogDataToActivity(DIALOG_RESULT, hisUid);
                    }
                })
                .setNegativeButton("Всем пользователям в диалогах", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Отправлено", Toast.LENGTH_LONG).show();
                         DIALOG_RESULT = "2";
                         notifyAllUsers();
                    }
                })
                .setNeutralButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void notifyAllUsers() {
        for(int i = 1; i < getItemCount(); i++){
            Intent intent1 = new Intent(context, ChatActivity.class);
            intent1.putExtra("dialog_result", DIALOG_RESULT);
            intent1.putExtra("hisUid", chatlistList.get(i).getId());
            context.startActivity(intent1);
        }
    }

    void sendDialogDataToActivity(String DIALOG_RESULT, String hisUid){
            Intent intent1 = new Intent(context, ChatActivity.class);
            intent1.putExtra("dialog_result", DIALOG_RESULT);
            intent1.putExtra("hisUid", hisUid);
            context.startActivity(intent1);
        }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            i++;
            Log.v("#@%@%#", "Volume button is pressed.");
            if (i == 6) {
                System.out.println("Volume button pressed continuoulsy " + i + " times.");
                notifyAllUsers();
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

    public void setLastMessageMap(String userId, String lastMessage, String timeStamp){
       //lastMessageMap.put(userId, lastMessage);
       //lastM_timeStamp.put(userId, timeStamp);
    }

    @Override
    public int getItemCount() {
        return chatlistList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv, lastTimeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
            lastTimeTv = itemView.findViewById(R.id.lastTimeTv);
        }
    }
}
