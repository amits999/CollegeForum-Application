package com.amitsharma.action.collegeforum;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ChangeActivity extends AppCompatActivity{
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    Context context;

    public ChangeActivity(Context context,FirebaseAuth mAuth, DatabaseReference mDatabase) {
        this.mAuth = mAuth;
        this.mDatabase = mDatabase;
        this.context = context;
    }

    public void SelectUserType() {

        FirebaseUser current_user=mAuth.getCurrentUser();
        String uid = current_user.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String user_id = dataSnapshot.child("user_tag").getValue().toString().trim();

                if (user_id.equals("s")){

                    Intent intent=new Intent(context,StudentPanel.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{

                    Intent intent=new Intent(context,FacultyTestQuiz.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
