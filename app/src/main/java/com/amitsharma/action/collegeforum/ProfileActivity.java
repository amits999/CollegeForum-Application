package com.amitsharma.action.collegeforum;

import android.accounts.NetworkErrorException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkRequest;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView profileName,profileEmail;
    EditText profileEditName;
    Button profileSubmitBtn,profileDeleteBtn;
    ImageButton nameEditButton;
    final static int GalleryPick=1;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mProfileImgRef;
    private FirebaseUser currentUser;
    String userUid;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_profile);

        profileImage=(CircleImageView)findViewById(R.id.profile_image_view);
        profileName=(TextView) findViewById(R.id.profile_username_view);
        profileEmail=(TextView) findViewById(R.id.profile_user_email);
        profileEditName=(EditText) findViewById(R.id.profile_name_edit);
        profileSubmitBtn=(Button) findViewById(R.id.profile_submit_button);
        profileDeleteBtn=(Button) findViewById(R.id.profile_delete_button);
        nameEditButton=(ImageButton) findViewById(R.id.profile_name_edit_button);

        profileEditName.setEnabled(false);
        profileEditName.setAlpha(0.5f);
        mProgress=new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        userUid=currentUser.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(userUid);
        mProfileImgRef= FirebaseStorage.getInstance().getReference().child("Profile Images");

        nameEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileEditName.setEnabled(true);
                profileEditName.setAlpha(1f);
                profileEditName.setText("");
                profileEditName.clearFocus();

            }
        });

        profileSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newName=profileEditName.getText().toString().trim();

                if (newName.isEmpty() && newName.length()<6){
                    profileEditName.setError("Not a valid full name!");
                    profileEditName.requestFocus();
                    return;
                }else{
                    profileEditName.setError(null);
                }

                mDatabase.child("name").setValue(newName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            profileEditName.setText("");
                            profileEditName.setEnabled(false);
                            profileEditName.setAlpha(0.5f);
                            Toast.makeText(ProfileActivity.this, "Name Changed:)", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        profileDeleteBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Delete Account!")
                        .setMessage("Deleting this account will result in completely removing your account from the system and you won't be able to access the app.");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        mProgress.setTitle("Deleting Account.");
                        mProgress.setMessage("Please wait..");
                        mProgress.setCanceledOnTouchOutside(false);
                        mProgress.show();


                        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mProgress.dismiss();
                                if (task.isSuccessful()){

                                    DatabaseReference userRef=FirebaseDatabase.getInstance().getReference().child("Users").child(userUid);
                                    userRef.setValue(null);

                                    Toast.makeText(ProfileActivity.this, "Account Deleted Successfully!", Toast.LENGTH_LONG).show();

                                    Intent intent=new Intent(ProfileActivity.this,MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();

                                }else {
                                    //Toast.makeText(ProfileActivity.this,task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    Snackbar.make(v, "This is a sensitive operation require recent authentication! Login Again.", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }
                        });
                    }
                });

                builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog=builder.create();
                alertDialog.show();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallaryIntent=new Intent();
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent,GalleryPick);
            }
        });

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if (dataSnapshot.child("profile_image").exists()){
                        String image=dataSnapshot.child("profile_image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

            mProgress.setTitle("Profile Image");
            mProgress.setMessage("Please wait, while we update your profile !");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();


            if (resultCode==RESULT_OK){

                Uri resultUri=result.getUri();

                StorageReference file_path=mProfileImgRef.child(userUid + ".jpg");
                file_path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            //Toast.makeText(ProfileActivity.this, "Image stored successfully to firebase.", Toast.LENGTH_SHORT).show();

                            final String downloadUrl=task.getResult().getDownloadUrl().toString().trim();

                            mDatabase.child("profile_image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        Intent intent=new Intent(ProfileActivity.this,ProfileActivity.class);
                                        startActivity(intent);

                                        Toast.makeText(ProfileActivity.this, "Profile Image updated successfully!", Toast.LENGTH_SHORT).show();
                                        mProgress.dismiss();

                                    }else if (task.getException() instanceof NetworkErrorException){

                                        Toast.makeText(ProfileActivity.this, "No network found!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(ProfileActivity.this, "Error Occurred:"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        mProgress.dismiss();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(ProfileActivity.this, "Error Occurred: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        }
                    }
                });

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user =mAuth.getCurrentUser();
        if (user==null){
            sendUserToMainActivity();
        }

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("name").exists()){

                    String name=dataSnapshot.child("name").getValue().toString();
                    profileName.setText(name);

                    profileEmail.setText(mAuth.getCurrentUser().getEmail().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void goToStudentPanel(View view) {
        Intent intent=new Intent(ProfileActivity.this,StudentPanel.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
