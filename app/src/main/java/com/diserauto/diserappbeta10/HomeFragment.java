package com.diserauto.diserappbeta10;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.diserauto.diserappbeta10.adapters.AdapterChat;
import com.diserauto.diserappbeta10.adapters.AdapterChatlist;
import com.diserauto.diserappbeta10.models.ModelChat;
import com.diserauto.diserappbeta10.models.ModelChatlist;
import com.diserauto.diserappbeta10.models.ModelUser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static android.content.Context.MODE_PRIVATE;
import static com.android.volley.VolleyLog.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatlist> chatlistList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;
    DialogFragment dialog1;
    ProgressBar pd;
    public static final String myDataPrefs = "myDataPrefs";
    private static TextToSpeech mTTS;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        pd = view.findViewById(R.id.progressHome);
        pd.setVisibility(View.VISIBLE);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.recyclerView);

        chatlistList = new ArrayList<>();
        if(currentUser != null) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("Chatlist");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatlistList.clear();
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                        chatlistList.add(chatlist);
                    }
                    adapterChatlist = new AdapterChatlist(getContext(), chatlistList);


                    try {
                        Collections.sort(chatlistList, ModelChatlist.ByDate);
                    }catch (Exception e){

                    }


                    recyclerView.setAdapter(adapterChatlist);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        else
        {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        }

        pd.setVisibility(View.GONE);
        return view;
    }
    /*
    private void loadChats() {

        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser user = ds.getValue(ModelUser.class);
                    for(ModelChatlist chatlist: chatlistList){
                        if(user.getUid() != null && user.getUid().equals(chatlist.getId())){
                            userList.add(user);
                            break;
                        }

                    }
                    adapterChatlist = new AdapterChatlist(getContext(), chatlistList);

                    Collections.sort(chatlistList, ModelChatlist.ByDate);

                    recyclerView.setAdapter(adapterChatlist);



                    for(int i=0; i<userList.size(); i++){
                        lastMessage(userList.get(i).getUid());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
     */
    /*
    private void lastMessage(final String userId) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("UserChats").child(currentUser.getUid()).child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                String theLastTime = "default";
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat == null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if(sender == null || receiver == null){
                        continue;
                    }
                    theLastMessage = chat.getMessage();
                    theLastTime = chat.getTimestamp();
                }

                adapterChatlist.setLastMessageMap(userId, theLastMessage, theLastTime);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
*/
    private void checkUserStatus()
    {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null)
        {

        }
        else
        {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInsanceState)
    {
        setHasOptionsMenu(true); //to show the menu
        super.onCreate(savedInsanceState);
        Context context = getActivity();
    }

    //inflates options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_random_user).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle menu item click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout)
        {
            firebaseAuth.signOut();

            SharedPreferences sp = getActivity().getSharedPreferences(myDataPrefs, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("My_Email", "");
            editor.putString("My_Name", "");
            editor.putString("My_Phone", "");
            editor.apply();

            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}
