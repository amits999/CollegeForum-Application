package com.amitsharma.action.collegeforum;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    boolean doubleTap=false;

    TextInputEditText email,password;
    Button loginBtn;

    TextView forgetPass;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mProgress=new ProgressDialog(this);

        email=(TextInputEditText) findViewById(R.id.loginEmailField);
        password=(TextInputEditText) findViewById(R.id.loginPasswordField);
        loginBtn=(Button) findViewById(R.id.loginLoginButton);
        forgetPass=(TextView) findViewById(R.id.loginForgetPass);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_user();
            }
        });

        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (doubleTap){
            super.onBackPressed();
        }
        else{
            Toast.makeText(LoginActivity.this,"Double tap to exit the app!",Toast.LENGTH_SHORT).show();
            doubleTap=true;
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleTap=false;

                }
            },500);
        }
    }

    private void login_user() {
        String myEmail=email.getText().toString().trim();
        String myPass=password.getText().toString().trim();

        if (myEmail.isEmpty()){
            email.setError("Please enter your email address!");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(myEmail).matches()){
            email.setError("Not a valid email!");
            email.requestFocus();
            return;
        }

        if (myPass.isEmpty()){
            password.setError("Password should not be empty");
            password.requestFocus();
            return;
        }

        if (myPass.length()<6){
            password.setError("Password length should be 6");
            password.requestFocus();
            return;
        }

        mProgress.setTitle("Logging In");
        mProgress.setMessage("Please wait while we verify your inputs !");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mAuth.signInWithEmailAndPassword(myEmail,myPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    final FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    if (!current_user.isEmailVerified()){
                        mProgress.dismiss();
                        Intent intent=new Intent(getApplicationContext(),VerifyUser.class);
                        startActivity(intent);

                    }else{

                        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                        mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child("user_tag").exists()){
                                    String user_id = dataSnapshot.child("user_tag").getValue().toString().trim();
                                    if (user_id.equals("s")){

                                        mProgress.dismiss();
                                        Intent intent=new Intent(getApplicationContext(),StudentPanel.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        mDatabase.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.child("teaching_subjects").exists()){
                                                    mProgress.dismiss();
                                                    Intent intent=new Intent(getApplicationContext(),FacultyTestQuiz.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }else {
                                                    mProgress.dismiss();
                                                    Intent intent=new Intent(getApplicationContext(),StudentPanel.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                }else {

                    mProgress.dismiss();

                    if (task.getException() instanceof FirebaseAuthInvalidUserException){
                        Toast.makeText(getApplicationContext(),"User with email address does not exist!",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    public void onSignUp(View view) {
        Intent intent=new Intent(LoginActivity.this,SignUpActivity.class);
        startActivity(intent);
    }
}
