package com.diserauto.diserappbeta10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;

    GoogleSignInClient mGoogleSignInClient;

    EditText mEmailEt, mPasswordEt;
    TextView notHaveAccntTv, mRecoverPassTv;
    Button mLoginBtn;
    SignInButton mGoogleLoginBtn;
    TextView LoginTv;
    private FirebaseAuth mAuth;
    ProgressDialog pd;
    public static final String myDataPrefs = "myDataPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Вход в аккаунт");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        mAuth = FirebaseAuth.getInstance();


        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        notHaveAccntTv = findViewById(R.id.nothave_accountTv);
        mRecoverPassTv = findViewById(R.id.recoverPassTv);
        mLoginBtn = findViewById(R.id.loginBtn);
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);



        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else
                {
                    if(passw.length() == 0)
                        mPasswordEt.setError("Invalid password");
                    else
                    {loginUser(email, passw);}
                }
            }
        });

        notHaveAccntTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDiealog();
            }
        });

        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Configure Google Sign In
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        pd = new ProgressDialog(this);

    }

    private void showRecoverPasswordDiealog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Восстановить пароль");

        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);


        builder.setPositiveButton("Восстановить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecovery(String email) {
        pd.setMessage("Отправляем письмо...");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if(task.isSuccessful())
                {
                    Toast.makeText(LoginActivity.this, "Проверьте почту", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Сбой...", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String passw)
    {
        pd.setMessage("Вход...");
        pd.show();

        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                            dbRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String email = dataSnapshot.child("email").getValue().toString();
                                    String phone = dataSnapshot.child("phone").getValue().toString();


                                    SharedPreferences sp = LoginActivity.this.getSharedPreferences(myDataPrefs, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("My_Email", email);
                                    editor.putString("My_Name", name);
                                    editor.putString("My_Phone", phone);
                                    editor.apply();
                                }


                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            Toast.makeText(LoginActivity.this, "Вы вошли как "+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            pd.dismiss();
                            Toast.makeText(LoginActivity.this, "Сбой аутентификации",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });


    }

    @Override
    public  boolean onSupportNavigateUp()
    {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Ошибка: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            // Toast.makeText(LoginActivity.this, "Creating profile...", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                            //if user is signing in first time then get and show user info from google account
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
                                //get user email and uid from auth
                                String email = user.getEmail();
                                String uid = user.getUid();
                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", "");
                                hashMap.put("onlineStatus", "online");
                                hashMap.put("typingTo", "noOne");
                                hashMap.put("phone", "");
                                hashMap.put("image", "");
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference reference = database.getReference("Users");
                                reference.child(uid).setValue(hashMap);
                            }

                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                            dbRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String email = dataSnapshot.child("email").getValue().toString();
                                    String phone = dataSnapshot.child("phone").getValue().toString();


                                    SharedPreferences sp = LoginActivity.this.getSharedPreferences(myDataPrefs, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("My_Email", email);
                                    editor.putString("My_Name", name);
                                    editor.putString("My_Phone", phone);
                                    editor.apply();
                                }


                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                            Toast.makeText(LoginActivity.this, "Вы вошли как "+user.getEmail(), Toast.LENGTH_SHORT).show();
                            Intent intent1 = new Intent(LoginActivity.this, DashboardActivity.class);
                            LoginActivity.this.startActivity(intent1);
                            /*
                            intent1.putExtra("NewOne", "true");

                            */

                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Сбой аутентификации...", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
