package com.amitsharma.action.collegeforum;

import android.accounts.NetworkErrorException;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AskQuestionActivity extends AppCompatActivity {
    Spinner queSubject;
    EditText queTitle,queText;
    Button quePostBtn;
    ImageButton addImage;
    String subject="",uid;
    public static final int GalleryPick=1;
    String isAnonymous="no",saveCurrentDate="",saveCurrentTime="",postRandomName="",time;
    String downloadUrl="";

    ProgressDialog mProgress;
    HashMap userMap;

    private FirebaseAuth mAuth;
    FirebaseUser current_user;
    private DatabaseReference mUserRef,mQuestionRef;
    private StorageReference mQuestionImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_ask_question);

        mAuth=FirebaseAuth.getInstance();

        current_user = mAuth.getCurrentUser();
        uid= current_user.getUid();

        mProgress=new ProgressDialog(this);

        queSubject=(Spinner) findViewById(R.id.ask_que_subject);
        queTitle=(EditText) findViewById(R.id.ask_que_title);
        queText=(EditText) findViewById(R.id.ask_que_text);
        //queAnonymous=(CheckBox) findViewById(R.id.ask_que_anonymous);
        quePostBtn=(Button) findViewById(R.id.ask_que_button);
        addImage=(ImageButton) findViewById(R.id.ask_question_add_image);

        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        mQuestionRef= FirebaseDatabase.getInstance().getReference().child("Questions");
        mQuestionImage= FirebaseStorage.getInstance().getReference().child("Questions Images");

        long milis=System.currentTimeMillis();
        time=Long.toString(milis);

        Calendar calendarForDate= Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd:MMMM:yyyy");
        saveCurrentDate=currentDate.format(calendarForDate.getTime());

        Calendar calendarForTime= Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calendarForTime.getTime());

        postRandomName=saveCurrentDate+saveCurrentTime;


        queSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                subject=parent.getItemAtPosition(position).toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        quePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postQuestion();
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImageToQuestion();
            }
        });

    }

    private void addImageToQuestion() {
        Intent gallaryIntent=new Intent();
        gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
        gallaryIntent.setType("image/*");
        startActivityForResult(gallaryIntent,GalleryPick);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){

            Uri ImageUri=data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result= CropImage.getActivityResult(data);

            mProgress.setMessage("Adding Image");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();


            if (resultCode==RESULT_OK){

                Uri resultUri=result.getUri();

                StorageReference file_path=mQuestionImage.child(postRandomName).child(time + ".jpg");
                file_path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            mProgress.dismiss();
                            downloadUrl=task.getResult().getDownloadUrl().toString().trim();
                            Toast.makeText(AskQuestionActivity.this, "Image stored successfully to firebase.", Toast.LENGTH_SHORT).show();



                        }else{
                            Toast.makeText(AskQuestionActivity.this, "Error Occurred: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        }
                    }
                });

            }
        }
    }



    private void postQuestion() {
        final String title=queTitle.getText().toString().trim();
        final String question=queText.getText().toString().trim();

        if (title.isEmpty()){
            queTitle.setError("Please enter que title!");
            queTitle.requestFocus();
            return;
        }

        if (title.length()>40){
            queTitle.setError("Title length should not exceed 40!");
            queTitle.requestFocus();
            return;
        }

        if (title.length()<5){
            queTitle.setError("Title length should not be less than 5!");
            queTitle.requestFocus();
            return;
        }

        if (question.isEmpty()){
            queText.setError("Enter your question!");
            queText.requestFocus();
            return;
        }

        if (question.length()<10){
            queText.setError("Question length should not be less than 10!");
            queText.requestFocus();
            return;
        }

        if (subject.equals("Select Subject")){
            Toast.makeText(AskQuestionActivity.this, "Please select a subject!", Toast.LENGTH_SHORT).show();
            return;
        }



        mProgress.setTitle("Posting Question");
        mProgress.setMessage("Please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.dismiss();

        mUserRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String fullName=dataSnapshot.child("name").getValue().toString();
                    if (dataSnapshot.child("profile_image").exists()){
                        String profileImage=dataSnapshot.child("profile_image").getValue().toString();

                        userMap = new HashMap<>();
                        userMap.put("title", title);
                        userMap.put("que_text", question);
                        userMap.put("anonymous", isAnonymous);
                        userMap.put("subject", subject);
                        userMap.put("posted_user_id", uid);
                        userMap.put("date", saveCurrentDate);
                        userMap.put("time", saveCurrentTime);
                        userMap.put("name", fullName);
                        userMap.put("profile_image", profileImage);
                        userMap.put("question_image", downloadUrl);
                    }else{
                        userMap = new HashMap<>();
                        userMap.put("title", title);
                        userMap.put("que_text", question);
                        userMap.put("anonymous", isAnonymous);
                        userMap.put("subject", subject);
                        userMap.put("posted_user_id", uid);
                        userMap.put("date", saveCurrentDate);
                        userMap.put("time", saveCurrentTime);
                        userMap.put("name", fullName);
                        userMap.put("question_image", downloadUrl);
                    }

        /*            mQuestionRef.child("profile_image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                Intent intent=new Intent(AskQuestionActivity.this,ProfileActivity.class);
                                startActivity(intent);

                                Toast.makeText(AskQuestionActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                                mProgress.dismiss();

                            }else {
                                Toast.makeText(AskQuestionActivity.this, "Error Occurred:"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                mProgress.dismiss();
                            }
                        }
                    });
*/
                    mQuestionRef.child(uid + postRandomName).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                mProgress.dismiss();
                                Toast.makeText(AskQuestionActivity.this, "Question posted successfully :)", Toast.LENGTH_LONG).show();

                                Intent intent=new Intent(AskQuestionActivity.this,StudentPanel.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            }else{

                                mProgress.dismiss();
                                Toast.makeText(AskQuestionActivity.this, task.getException().getMessage()+"Please retry!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

        public void onCheckboxClicked(View view) {
            // Is the view now checked?
            boolean checked = ((CheckBox) view).isChecked();

            switch(view.getId()) {
                case R.id.ask_que_anonymous:
                    if (checked)
                    isAnonymous="yes";
                    else {
                        isAnonymous="no";
                    }
                    break;

            }
        }
}
