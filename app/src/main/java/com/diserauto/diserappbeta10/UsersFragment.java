package com.diserauto.diserappbeta10;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ProgressBar;
import android.widget.SearchView;


import com.diserauto.diserappbeta10.adapters.AdapterUsers;
import com.diserauto.diserappbeta10.models.ModelUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {


    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;
    FirebaseAuth firebaseAuth;
    ProgressBar pd;
    public static final String myDataPrefs = "myDataPrefs";
    public UsersFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        pd = view.findViewById(R.id.progressUsers);

        pd.setVisibility(View.VISIBLE);
        userList = new ArrayList<>();

        getAllUsers();
        /*
        KeyboardVisibilityEvent.setEventListener(getActivity(), new KeyboardVisibilityEventListener()
        {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                // write your code

            }
        });
        */

        pd.setVisibility(View.GONE);
        return view;
    }

    private void getAllUsers() {

        //get current user

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get this user path for info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    if((modelUser != null)&&(fUser != null)) {
                        if (!modelUser.getUid().equals(fUser.getUid())) {
                            userList.add(modelUser);
                        }
                    }

                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(final String query) {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get this user path for info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    if(!modelUser.getUid().equals(fUser.getUid())){
                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase())
                            || (modelUser.getEmail().toLowerCase().contains(query.toLowerCase())))
                        {
                            userList.add(modelUser);
                        }
                    }

                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

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
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        setHasOptionsMenu(true); //to show the menu
        super.onCreate(savedInstanceState);
    }

    //inflates options menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {

        inflater.inflate(R.menu.menu_main,menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }
                else
                {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if(!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }
                else
                {
                    getAllUsers();
                }

                return false;
            }
        });

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
