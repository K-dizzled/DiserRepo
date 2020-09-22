package com.diserauto.diserappbeta10.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diserauto.diserappbeta10.ChatActivity;
import com.diserauto.diserappbeta10.VoiceChatActivity;
import com.diserauto.diserappbeta10.models.ModelUser;
import com.diserauto.diserappbeta10.R;
import com.squareup.picasso.Picasso;

import java.util.List;


public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>   {

    Context context;
    List<ModelUser> usersList;
    public static final String VISIONprefs = "visionPrefs";

    public AdapterUsers(Context context, List<ModelUser> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get data
        final String hisUID = usersList.get(i).getUid();
        String userImage = usersList.get(i).getImage();
        final String userEmail = usersList.get(i).getEmail();
        String userName = usersList.get(i).getName();

        //set data
        if(!userName.equals("")){
            myHolder.mNameTv.setText(userName);
        }
        else
            myHolder.mNameTv.setText("Некто");
        myHolder.mEmailTv.setText(userEmail);
        //Временно убираю картинку у пользователя, дабы нормально рпботпть м приложением и не ловить столько ошибок

        /*
        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_planet_icon)
                    .into(myHolder.mAvatarTv);
        }
        catch (Exception e){

        }
        */
        Picasso.get().load(R.drawable.ic_planet_icon).into(myHolder.mAvatarTv);

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = context.getSharedPreferences(VISIONprefs, Context.MODE_PRIVATE);
                if(sharedPreferences.getBoolean("Chat_version_blind", false)){
                    Intent intent1 = new Intent(context, VoiceChatActivity.class);
                    intent1.putExtra("hisUid", hisUID);
                    context.startActivity(intent1);
                }
                else
                {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("hisUid", hisUID);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarTv;
        TextView mNameTv, mEmailTv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mAvatarTv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
        }
    }
}
