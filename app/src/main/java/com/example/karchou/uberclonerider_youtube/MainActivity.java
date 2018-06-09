package com.example.karchou.uberclonerider_youtube;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.karchou.uberclonerider_youtube.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends Activity {

    Button btnsignin, btnresgister;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference riders;
    RelativeLayout relativeLayout;
    AlertDialog waitingdialog;
    

    private final static int PERMISSION=1000;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void onCreate (Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);

        btnsignin = (Button) findViewById(R.id.btnsign_in);
        btnresgister = (Button) findViewById(R.id.btnregister);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        riders = firebaseDatabase.getReference("Riders");
        relativeLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        btnresgister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSigninDialog();
            }
        });
        
    }


    private void showSigninDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to SIGN IN");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View login_layout = layoutInflater.inflate(R.layout.layout_login, null);

        final MaterialEditText edtMail = login_layout.findViewById(R.id.edtmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtpwd);
        dialog.setView(login_layout);

        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                btnsignin.setEnabled(false);

                if (TextUtils.isEmpty(edtMail.getText().toString())) {
                    Snackbar.make(relativeLayout, "Please enter your email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(relativeLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                waitingdialog=new SpotsDialog(MainActivity.this);
                waitingdialog.show();

                firebaseAuth.signInWithEmailAndPassword(edtMail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingdialog.dismiss();
                                startActivity(new Intent(MainActivity.this,welcome.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingdialog.dismiss();
                                Toast.makeText(MainActivity.this,"Failed : "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                btnsignin.setEnabled(true);
                            }
                        });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void showRegisterDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View register_layout = layoutInflater.inflate(R.layout.layout_register, null);

        final MaterialEditText edtMail = register_layout.findViewById(R.id.edtmail);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtpwd);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);

        dialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (TextUtils.isEmpty(edtMail.getText().toString())) {
                    Snackbar.make(relativeLayout, "Please enter your email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                    Snackbar.make(relativeLayout, "Please enter phone number", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(relativeLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtPhone.getText().toString().length() < 6) {
                    Snackbar.make(relativeLayout, "Password too short", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.createUserWithEmailAndPassword(edtMail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(edtMail.getText().toString());
                                user.setName(edtName.getText().toString());
                                user.setPassword(edtPassword.getText().toString());
                                user.setPhone(edtPhone.getText().toString());

                                riders.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                        setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(relativeLayout, "Registered succesfully", Snackbar.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(relativeLayout, "Registeration error: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(relativeLayout, "Failed: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
