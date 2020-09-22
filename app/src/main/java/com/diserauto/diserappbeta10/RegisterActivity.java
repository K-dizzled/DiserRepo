package com.diserauto.diserappbeta10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity
{

    EditText mEmailEt, mPasswordEt, mNameEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;
    public static final String myDataPrefs = "myDataPrefs";

    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mEmailEt = findViewById(R.id.emailEt);
        mNameEt = findViewById(R.id.nameEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        mRegisterBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                String name = mNameEt.getText().toString().trim();
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();


                if(name.length() < 3){
                    mNameEt.setError("Слишком короткое имя");
                    mEmailEt.setFocusable(true);
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    Toast.makeText(RegisterActivity.this, "Некорректный Email", Toast.LENGTH_SHORT).show();
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else if (password.length() < 6){
                    Toast.makeText(RegisterActivity.this, "Пароль должен состоять из 6 и более символов", Toast.LENGTH_SHORT).show();
                    mPasswordEt.setError("Invalid password");
                    mEmailEt.setFocusable(true);
                }
                else
                {
                    registerUser(email, password, name);
                }
            }

        });
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

    }


    private void registerUser(String email, String password, String name)
    {
        progressDialog.show();

        final String username = name;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            String email = user.getEmail();

                            SharedPreferences sp = RegisterActivity.this.getSharedPreferences(myDataPrefs, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("My_Email", email);
                            editor.apply();

                            String uid = user.getUid();

                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", username);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("phone", "");
                            hashMap.put("image", "");
                            hashMap.put("number", "");

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(RegisterActivity.this, "Аккаунт: "+user.getEmail()+" создан", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else
                        {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
    @Override
    public void onFailure(@NonNull Exception e)
    {
        progressDialog.dismiss();

        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
});
}
@Override
protected void onDestroy() {

    super.onDestroy();
}
    @Override
    public  boolean onSupportNavigateUp()
    {
            onBackPressed();
            return super.onSupportNavigateUp();
    }
}