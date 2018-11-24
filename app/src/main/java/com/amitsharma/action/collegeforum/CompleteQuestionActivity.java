package com.amitsharma.action.collegeforum;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CompleteQuestionActivity extends AppCompatActivity {
    private TextView mName,mDate,mTime,mSubject,mTitle,mQuestion,mImpQue;
    TextInputLayout mCommentLayout;
    ImageButton mImpButton,mAttachImageButton;
    private TextInputEditText mCommentField;
    private Button mCommentButton;
    private CircleImageView mPostProfile;
    private String mPostProfileText,PostKey,curUid,saveCurrentDate="",saveCurrentTime="",postRandomName="",time="";
    private DatabaseReference mQuestionPost,mCommentsDatabase,mRef,mImpRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    RecyclerView mReplyRecyclerView;
    boolean impChecker=false;
    int countImp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_complete_question);

        Toolbar myChildToolbar = (Toolbar) findViewById(R.id.comp_que_bar);
        setSupportActionBar(myChildToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setElevation(3f);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        curUid=currentUser.getUid();

        PostKey=getIntent().getStringExtra("postKey");
        mRef= FirebaseDatabase.getInstance().getReference().child("Users").child(curUid);
        mQuestionPost= FirebaseDatabase.getInstance().getReference().child("Questions").child(PostKey);
        mImpRef=FirebaseDatabase.getInstance().getReference().child("Imp_Questions");
        mCommentsDatabase= mQuestionPost.child("Comments");

        mReplyRecyclerView= findViewById(R.id.que_replies_recycler_view);
        mReplyRecyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mReplyRecyclerView.setLayoutManager(linearLayoutManager);

        mName=(TextView) findViewById(R.id.comp_que_user_name);
        mDate=(TextView) findViewById(R.id.comp_que_date);
        mTime=(TextView) findViewById(R.id.comp_que_time);
        mSubject=(TextView) findViewById(R.id.comp_que_subject);
        mTitle=(TextView) findViewById(R.id.comp_que_title);
        mQuestion=(TextView) findViewById(R.id.comp_que_text);

        mCommentField=(TextInputEditText) findViewById(R.id.comp_reply_field);
        mCommentButton=(Button) findViewById(R.id.comp_reply_button);
        mPostProfile=(CircleImageView) findViewById(R.id.comp_que_profile_image);
        mCommentLayout=(TextInputLayout) findViewById(R.id.reply_comment_layout) ;
        mImpButton=(ImageButton) findViewById(R.id.comp_imp_button);
        mAttachImageButton=(ImageButton) findViewById(R.id.comp_attach_image_button);
        mImpQue=(TextView) findViewById(R.id.comp_imp_count_view);

        Calendar calendarForDate= Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd MMM");
        saveCurrentDate=currentDate.format(calendarForDate.getTime());

        Calendar calendarForTime= Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calendarForTime.getTime());


        long milis=System.currentTimeMillis();
        time=Long.toString(milis);

        displayQuestionInformation();
        displayImpQuestionStatus(PostKey);

        mImpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                impChecker=true;

                mImpRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (impChecker==true){

                            if (dataSnapshot.child(PostKey).hasChild(curUid)){

                                mImpRef.child(PostKey).child(curUid).removeValue();
                                impChecker=false;

                            }else{

                                mImpRef.child(PostKey).child(curUid).setValue(true);
                                impChecker=false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        mCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentLayout.setVisibility(View.VISIBLE);
                mCommentButton.setText("Send");
                final String comment=mCommentLayout.getEditText().getText().toString();

                if (comment.length()<5) {
                    mCommentField.setError("Comment length should be greater than 5");
                    mCommentField.requestFocus();
                    return;
                }

                mRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String fullName=dataSnapshot.child("name").getValue().toString();
                            String profileImage=dataSnapshot.child("profile_image").getValue().toString();

                            HashMap replyMap=new HashMap();
                            replyMap.put("name",fullName);
                            replyMap.put("date",saveCurrentDate);
                            replyMap.put("time",saveCurrentTime);
                            replyMap.put("profile_image",profileImage);
                            replyMap.put("comment",comment);

                            mCommentsDatabase.child(time).updateChildren(replyMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        mCommentField.setText("");
                                        mCommentLayout.setVisibility(View.GONE);
                                        mCommentField.clearFocus();
                                        mCommentButton.setText("Add Comment");
                                        Toast.makeText(CompleteQuestionActivity.this, "Comment Successful.", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(CompleteQuestionActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
        });

//.........................................................

        mAttachImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuestionPost.child("question_image").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String QuestionImageUrl=dataSnapshot.getValue().toString();

                        Target target = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                Uri url=insertImage(getContentResolver(),bitmap,time,"new Question");
                                Toast.makeText(CompleteQuestionActivity.this, url.toString(), Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType( url , "image/*");
                                getApplicationContext().startActivity(intent);

                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                            }
                        };

                        Picasso.get().load(QuestionImageUrl).into(target);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

//..............................................................................

        DisplayAllUserComments();
    }
//......................................
    public static final Uri insertImage(ContentResolver cr,
                                           Bitmap source,
                                           String title,
                                           String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                //storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return url;
    }
//.....................................


    private void openImage(Uri uri) {
        Intent gallaryIntent=new Intent();
        gallaryIntent.setAction(Intent.CATEGORY_OPENABLE);
        gallaryIntent.setType("image/*");
        gallaryIntent.setData(uri);
        startActivity(gallaryIntent);
    }


    private void displayImpQuestionStatus(final String postKey) {

        mImpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postKey).hasChild(curUid)){

                    countImp=(int)dataSnapshot.child(postKey).getChildrenCount();
                    mImpButton.setImageResource(R.drawable.like);
                    mImpQue.setText(Integer.toString(countImp));
                }else {
                    countImp=(int)dataSnapshot.child(postKey).getChildrenCount();
                    mImpButton.setImageResource(R.drawable.dislike);
                    mImpQue.setText(Integer.toString(countImp));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void DisplayAllUserComments() {
        FirebaseRecyclerAdapter<QuestionModel,CommentViewHolder> firebaseReplyRecyclerAdapter=new FirebaseRecyclerAdapter<QuestionModel, CommentViewHolder>(
                QuestionModel.class,
                R.layout.question_reply_recycler,
                CommentViewHolder.class,
                mCommentsDatabase
        ) {
            @Override
            protected void populateViewHolder(CommentViewHolder viewHolder, QuestionModel model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setComment(model.getComment());
                viewHolder.setProfile_image(model.getProfile_image());
            }
        };
        mReplyRecyclerView.setAdapter(firebaseReplyRecyclerAdapter);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public CommentViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setName(String name){
            TextView username=(TextView) mView.findViewById(R.id.reply_user_name);
            username.setText(name);
        }
        public void setDate(String date){
            TextView myDate=(TextView) mView.findViewById(R.id.reply_date);
            myDate.setText(date);
        }
        public void setTime(String time){
            TextView myTime=(TextView) mView.findViewById(R.id.reply_time);
            myTime.setText(time);
        }

        public void setComment(String comment){
            TextView myComment=(TextView) mView.findViewById(R.id.reply_answer);
            myComment.setText(comment);
        }

        public void setProfile_image(String profile_image){
            CircleImageView mProfileImage=(CircleImageView) mView.findViewById(R.id.reply_profile_image);
            Picasso.get().load(profile_image).into(mProfileImage);
        }
    }

    private void displayQuestionInformation() {
        mQuestionPost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    mName.setText("-"+dataSnapshot.child("name").getValue().toString());
                    mDate.setText(dataSnapshot.child("date").getValue().toString());
                    mTime.setText(dataSnapshot.child("time").getValue().toString());
                    mTitle.setText(dataSnapshot.child("title").getValue().toString());
                    mSubject.setText("."+dataSnapshot.child("subject").getValue().toString());
                    mQuestion.setText(dataSnapshot.child("que_text").getValue().toString());

                    mPostProfileText=dataSnapshot.child("profile_image").getValue().toString();
                    Picasso.get().load(mPostProfileText).into(mPostProfile);

                    if (dataSnapshot.child("question_image").exists()){
                        mAttachImageButton.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
