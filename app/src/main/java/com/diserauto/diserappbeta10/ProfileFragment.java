package com.diserauto.diserappbeta10;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.diserauto.diserappbeta10.models.ModelChatlist;
import com.diserauto.diserappbeta10.models.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.HashMap;
import java.util.Objects;

import co.aenterhy.toggleswitch.ToggleSwitchButton;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    public static final String VISIONprefs = "visionPrefs";
    public static final String myDataPrefs = "myDataPrefs";

    //storage
    StorageReference storageReference;
    //path where images of user profile will be stored
    String storagePath = "Users_Profile_Imgs/";

    //views from xml
    ImageView avatarIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;
    ToggleSwitchButton toggle;

    //progress dialog

    ProgressDialog pd;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //arrays of permissions to be requested
    String cameraPermissions[];
    String strogarePermissions[];

    //uri of picked image
    Uri image_uri;

    //for checking profile photo
    String profilePhoto;

    //массив со всеми данными пользователя
    String[] userData = new String[4];


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //firebase storage reference

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        strogarePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);

        //init progress dialog
        pd = new ProgressDialog(getActivity());

        //toggle blind / not blind
        toggle = view.findViewById(R.id.toggle);
        toggle.setOnTriggerListener(new ToggleSwitchButton.OnTriggerListener() {
            @Override
            public void toggledUp() {
                Toast.makeText(getContext(), "Blind mode ON", Toast.LENGTH_SHORT).show();

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(VISIONprefs, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("Chat_version_blind", true);
                editor.apply();
            }

            @Override
            public void toggledDown() {
                Toast.makeText(getContext(), "Blind mode OFF", Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(VISIONprefs, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("Chat_version_blind", false);
                editor.apply();
            }
        });
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(myDataPrefs, Context.MODE_PRIVATE);

        String name = ""+ sharedPreferences.getString("My_Name", "");
        String email = ""+ sharedPreferences.getString("My_Email", "");
        String phone = ""+ sharedPreferences.getString("My_Phone", "");

        if(name != "")
            nameTv.setText(name);
        else
            nameTv.setText("Некто");
        if(phone != "")
            phoneTv.setText(phone);
        else
            phoneTv.setText("Не указан");
        emailTv.setText(email);

        Picasso.get().load(R.drawable.ic_planet_icon).into(avatarIv);
        //fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
        return view;
    }

    private boolean checkStrogarePermission(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStrogarePermission(){

        requestPermissions(strogarePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission(){

        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String options[] = {"Сменить аватарку", "Сменить имя", "Сменить номер"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Выберите действие");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0){
                    //Edit Profile clicked

                    pd.setMessage("Обновляем...");
                    profilePhoto = "image";
                    showImagePicDialog();
                }
                else if(which == 1){
                    //Edit Name clicked
                    pd.setMessage("Обновляем");
                    showNamePhoneUpdateDialog("name");
                }
                else if(which == 2){
                    //Edit Phone clicked
                    pd.setMessage("Обновляем");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });

        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Меняем "+ key); //Update name OR Update phone

        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        //add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Введите "+ key); //Edit name OR Edit phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add buttons in dialog to update
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                //validate if user has entered smth or not
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    user = FirebaseAuth.getInstance().getCurrentUser();
                    final String myUid = user.getUid();
                    databaseReference.child(myUid).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //updated, dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Успешно...", Toast.LENGTH_SHORT).show();

                                    databaseReference = firebaseDatabase.getReference("Users");
                                    Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds: dataSnapshot.getChildren()){
                                                //get data
                                                String name = ""+ ds.child("name").getValue();
                                                String phone = ""+ ds.child("phone").getValue();

                                                //Log.d("#@#@#@", name +" , "+ phone);

                                                SharedPreferences sp = Objects.requireNonNull(getActivity()).getSharedPreferences(myDataPrefs, Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sp.edit();
                                                editor.putString("My_Name", name);
                                                editor.putString("My_Phone", phone);
                                                editor.apply();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed, dismiss progress. get and show error message
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Ошибка: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else{
                    Toast.makeText(getActivity(), "Введите "+key, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //add buttons in dialog to cancel
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //create and show dialog
        builder.create().show();

    }

    private void showImagePicDialog() {
        //show dialog containig options Camera nad Gallery to pick the image
        String options[] = {"Камера", "Галерея"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Изображение");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0){
                    //Camera clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }

                }
                else if(which == 1){
                    //Gallery clicked
                    if(!checkStrogarePermission()){
                        requestStrogarePermission();
                    }
                    else{
                        pickFromGallery();
                    }
                }

            }
        });

        builder.create().show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //picking from camera, first check if camera and strogare permissions allowed or not
                if(grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStogareAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && writeStogareAccepted){
                        //permissions enabled
                        pickFromCamera();
                    }

                    else{
                        //permissions denied
                        Toast.makeText(getActivity(), "Пожалуйста, предоставьте доступ к камере и хранилищу", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{

                //picking from gallery, first check if strogare permissions allowed or not
                if(grantResults.length >0){
                    boolean writeStogareAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(writeStogareAccepted){
                        //permissions enabled
                        pickFromGallery();
                    }

                    else{
                        //permissions denied
                        Toast.makeText(getActivity(), "Пожалуйста, предоставьте доступ к хранилищу", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){

            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //image is picked from gallery, get uri of image
                image_uri = data.getData();

                uploadProfilePhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //image is picked from camera, get uri of image
                uploadProfilePhoto(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfilePhoto(Uri uri) {

        //show progress
        pd.show();

        //path and name of image to be stored in firebase storage
        String filePathAndName = storagePath+ ""+ profilePhoto +"_"+ user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage, now get it's uri and store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //check is image is uploaded/not and uri is received
                        if(uriTask.isSuccessful()){
                            //image uploaded
                            //add/update uri in user's database
                            HashMap<String, Object> results = new HashMap<>();

                            results.put(profilePhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //uri in database of esers is added successfully
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Успешно...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding url in database of user
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Ошибка...", Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }
                        else{
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some error(s), get and show error message, dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void pickFromCamera() {
        //Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);

    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
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
    public void onCreate(@Nullable Bundle savedInsanceState)
    {
        setHasOptionsMenu(true); //to show the menu
        super.onCreate(savedInsanceState);
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
