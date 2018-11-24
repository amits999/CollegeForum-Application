package com.amitsharma.action.collegeforum;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;

public class FacultyTestQuiz extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    Spinner mBranch;
    TextView mSelectedSubjects;
    Button mSubmitBtn,mSelectSubjectsBtn;
    String[] listItems;
    boolean[] checkedItems;
    String item="",branch="",uid="";
    boolean doubleTap=false;

    ArrayList<Integer> mUserItems =new ArrayList<>();
// in class

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        //String uid =currentUser.getUid();
        
        if (currentUser==null){
            sendUserToMainActivity();
        }
  /*
        else {
            mDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("teaching_subjects").exists()){
                        Intent intent=new Intent(FacultyTestQuiz.this,StudentPanel.class);
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
*/    }
//.......................
@Override
public void onBackPressed() {

    if (doubleTap){
        super.onBackPressed();
    }
    else{
        Toast.makeText(FacultyTestQuiz.this,"Double tap to exit the app!",Toast.LENGTH_SHORT).show();
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_faculty_test_quiz);

        mBranch=(Spinner) findViewById(R.id.facultyTestBranchSpinner);
        mSelectedSubjects=(TextView) findViewById(R.id.faculty_selected_subject);
        mSelectSubjectsBtn=(Button) findViewById(R.id.faculty_choose_subject);
        mSubmitBtn=(Button) findViewById(R.id.faculty_test_submit_btn);
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if (currentUser!=null){
            uid =currentUser.getUid();
        }

        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users");


        listItems =getResources().getStringArray(R.array.subject_entries);
        checkedItems=new boolean[listItems.length];

        mBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                branch=parent.getItemAtPosition(position).toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSelectSubjectsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(FacultyTestQuiz.this);
                builder.setTitle("Select the subjects you teach...");
                builder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {

                        try{
                            if (isChecked){
                                if (!mUserItems.contains(position)){
                                    mUserItems.add(position);
                                }
                            }else if (mUserItems.contains(position)){
                                mUserItems.remove(position);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(FacultyTestQuiz.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setCancelable(false);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        for (int i=0;i<mUserItems.size();i++){
                            item=item + listItems[mUserItems.get(i)];
                            if (i != mUserItems.size() -1){
                                item=item + ",";
                            }
                        }
                        // in class
                        mSelectedSubjects.setText(item);
                        //...........................
                    }
                });

                builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i=0;i<checkedItems.length;i++){
                            checkedItems[i]=false;
                            mUserItems.clear();
                            mSelectedSubjects.setText("");
                        }
                    }
                });

                AlertDialog alertDialog=builder.create();
                alertDialog.show();
            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (branch.equals("Select Branch")){
                    Toast.makeText(FacultyTestQuiz.this, "Please select a branch!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (item.isEmpty()){
                    Toast.makeText(FacultyTestQuiz.this,"Please select your teaching subjects!",Toast.LENGTH_SHORT).show();
                    return;
                }


                mDatabase.child(uid).child("teaching_subjects").setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            mDatabase.child(uid).child("teaching_branch").setValue(branch).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(FacultyTestQuiz.this, "Details added successfully!", Toast.LENGTH_SHORT).show();

                                        Intent intent=new Intent(getApplicationContext(),StudentPanel.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();

                                    }else{
                                        Toast.makeText(FacultyTestQuiz.this, "Error Occurred: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }else{
                            Toast.makeText(FacultyTestQuiz.this, "Error Occurred: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(FacultyTestQuiz.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
