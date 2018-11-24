package com.amitsharma.action.collegeforum;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;

public class ResetPasswordActivity extends AppCompatActivity {
    TextInputEditText email;
    Button resetBtn;

    FirebaseAuth mAuth;
    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_reset_password);

        mProgress=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();
        email=(TextInputEditText) findViewById(R.id.resetEmailField);
        resetBtn=(Button) findViewById(R.id.resetButton);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String myEmail=email.getText().toString().trim();

        if (myEmail.isEmpty()){
            email.setError("Please provide your email address!");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(myEmail).matches()){
            email.setError("Please enter a valid email address");
            email.requestFocus();
            return;
        }

        mProgress.setTitle("Sending Reset Password Link");
        mProgress.setMessage("Please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();


        mAuth.sendPasswordResetEmail(myEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mProgress.dismiss();

                if (task.isSuccessful()){
                    Toast.makeText(ResetPasswordActivity.this, "Password Reset link Sent :), Login to your account.",
                            Toast.LENGTH_LONG).show();

                    Intent mainIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                }else{
                    Toast.makeText(ResetPasswordActivity.this, task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onLoginActivity(View view) {
        startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
    }
}
