package com.amitsharma.action.collegeforum;

import android.accounts.NetworkErrorException;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    TextInputEditText name,email,password,conPassword,mobileNo;
    Spinner branchSpinner,courseSpinner;
    Button signupBtn;
    String branch="",course="",user_code="s";

    ProgressDialog mProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_sign_up);

        mProgress=new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        name=(TextInputEditText) findViewById(R.id.signupNameField);
        email=(TextInputEditText) findViewById(R.id.signupEmailField);
        password=(TextInputEditText) findViewById(R.id.signupPasswordField);
        conPassword=(TextInputEditText) findViewById(R.id.signupConfirmPassword);
        mobileNo=(TextInputEditText) findViewById(R.id.signupMobileField);
        signupBtn=(Button) findViewById(R.id.signupCreateAccount);

        branchSpinner=(Spinner) findViewById(R.id.signupBranchSpinner);
        courseSpinner=(Spinner) findViewById(R.id.signupCourseSpinner);

        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                branch=parent.getItemAtPosition(position).toString().trim();
                //Toast.makeText(SignUpActivity.this, branch, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                course=parent.getItemAtPosition(position).toString().trim();
                //Toast.makeText(SignUpActivity.this, course, Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        final String myName=name.getText().toString().trim();
        String myEmail=email.getText().toString().trim();
        String myPass=password.getText().toString().trim();
        String myConPass=conPassword.getText().toString().trim();
        final String myMobileNo=mobileNo.getText().toString().trim();


        if (myName.isEmpty()){
            name.setError("Name should not be empty");
            name.requestFocus();
            return;
        }

        if (myEmail.isEmpty()){
            email.setError("Email id required");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(myEmail).matches()){
            email.setError("Please enter a valid email address");
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

        if (!myConPass.equals(myPass)){
            conPassword.setError("Passwords did not match");
            conPassword.requestFocus();
            return;
        }

        if (myMobileNo.length()!=10){
            mobileNo.setError("Mobile no. should be of 10 digits");
            mobileNo.requestFocus();
            return;
        }

        if (course.equals("Select Course")){
            Toast.makeText(SignUpActivity.this, "Please select a course!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (branch.equals("Select Branch")){
            Toast.makeText(SignUpActivity.this, "Please select a branch!", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgress.setTitle("Registering User");
        mProgress.setMessage("Please wait while we create your account !");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mAuth.createUserWithEmailAndPassword(myEmail, myPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", myName);
                            userMap.put("mobile", myMobileNo);
                            userMap.put("course", course);
                            userMap.put("branch", branch);
                            userMap.put("user_tag", user_code);

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    mProgress.dismiss();

                                    if(task.isSuccessful()){

                                        Toast.makeText(SignUpActivity.this, "Authentication Successful, Login to your account.",
                                                Toast.LENGTH_LONG).show();

                                        Intent mainIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();

                                    }else{

                                        Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        } else {

                            mProgress.dismiss();
                            if (task.getException() instanceof FirebaseAuthUserCollisionException){
                                Toast.makeText(SignUpActivity.this, "You are already registered!",
                                        Toast.LENGTH_SHORT).show();

                            }
                            else if (task.getException() instanceof NetworkErrorException){
                                Toast.makeText(SignUpActivity.this, "Please check your internet connection!",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {

                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();

                            }
                        }

                    }
                });

    }

    public void onLoginActivity(View view) {
        Intent intent=new Intent(SignUpActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    public void onFacultySignup(View view) {
        Intent intent=new Intent(SignUpActivity.this,FacultySignUpActivity.class);
        startActivity(intent);
    }


}
