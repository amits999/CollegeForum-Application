package com.amitsharma.action.collegeforum;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentPanel extends AppCompatActivity {

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef,mQuestionDatabase;
    RecyclerView mRecyclerView;
    String name="",uid="";
    private FirebaseUser currentuser;

    private CircleImageView navProfileImageView;
    private TextView navProfileUserName;
    LinearLayoutManager linearLayoutManager;
    SharedPreferences mSharedPref;

    boolean doubleTap=false;

    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    Context context;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_student_panel);

        final View parentLayout = findViewById(android.R.id.content);

        mAuth=FirebaseAuth.getInstance();

        context=getApplicationContext();

        mToolbar = (Toolbar) findViewById(R.id.student_page_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("GLA Forum");

        mRecyclerView= findViewById(R.id.student_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mSharedPref=getSharedPreferences("SortSettings",MODE_PRIVATE);
        String mSorting=mSharedPref.getString("Sort","newest");

        if (mSorting.equals("newest")){
            sortItemAsNewest();
        }else if (mSorting.equals("oldest")){
            sortItemAsOldest();
        }

        mRecyclerView.setLayoutManager(linearLayoutManager);

        mQuestionDatabase= FirebaseDatabase.getInstance().getReference().child("Questions");
        mRef= FirebaseDatabase.getInstance().getReference().child("Users");

        drawerLayout=(DrawerLayout) findViewById(R.id.student_drawer_layout);
        actionBarDrawerToggle=new ActionBarDrawerToggle(StudentPanel.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView=(NavigationView) findViewById(R.id.student_navigation_view);
        fab=(FloatingActionButton) findViewById(R.id.fab);


        View navView=navigationView.inflateHeaderView(R.layout.navigation_header);

        navProfileImageView=(CircleImageView) navView.findViewById(R.id.user_profile_image);
        navProfileUserName=(TextView) navView.findViewById(R.id.user_name);

        if (mAuth.getCurrentUser()!=null){
            String currentUserId=mAuth.getCurrentUser().getUid();
            mRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if (dataSnapshot.child("name").exists()){

                            String username=dataSnapshot.child("name").getValue().toString();
                            navProfileUserName.setText(username);

                            Snackbar.make(parentLayout, "Welcome "+username, Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();

                        }

                        if (dataSnapshot.child("profile_image").exists()){
                            String image=dataSnapshot.child("profile_image").getValue().toString();
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(StudentPanel.this,AskQuestionActivity.class));
                }
            });

            // DisplayAllUserQuestions();

        }

        navProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StudentPanel.this,ProfileActivity.class));
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                UserMenuSelector(menuItem);
                return false;
            }
        });

    }

    private void sortItemAsNewest() {
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
    }

    private void sortItemAsOldest() {
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
    }

    private void DisplayAllUserQuestions() {
        FirebaseRecyclerAdapter<Model,QuestionViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Model, QuestionViewHolder>(
                Model.class,
                R.layout.question_recycler,
                QuestionViewHolder.class,
                mQuestionDatabase
        ) {
            @Override
            protected void populateViewHolder(QuestionViewHolder viewHolder, Model model, int position) {
//from here we can check if user is anonymous or not than we can display profile image and name of user accordingly

                final String postKey=getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setSubject(model.getSubject());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setProfile_image(model.getProfile_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent replyQuestionIntent=new Intent(StudentPanel.this,CompleteQuestionActivity.class);
                        replyQuestionIntent.putExtra("postKey",postKey);
                        startActivity(replyQuestionIntent);
                    }
                });
            }
        };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public QuestionViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
        }
        public void setName(String name){
            TextView username=(TextView) mView.findViewById(R.id.que_user_name);
            username.setText(name);
        }

        public void setTitle(String title){
            TextView mTitle=(TextView) mView.findViewById(R.id.que_title);
            mTitle.setText("> "+title);
        }

        public void setSubject(String subject){
            TextView mSubject=(TextView) mView.findViewById(R.id.que_subject);
            mSubject.setText("."+subject);
        }

        public void setTime(String time){
            TextView mTime=(TextView) mView.findViewById(R.id.que_time);
            mTime.setText(time);
        }

        public void setDate(String date){
            TextView mDate=(TextView) mView.findViewById(R.id.que_date);
            mDate.setText(date);
        }
        public void setProfile_image(String profile_image){
            CircleImageView mProfileImage=(CircleImageView) mView.findViewById(R.id.que_profile_image);
            Picasso.get().load(profile_image).into(mProfileImage);
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleTap){
            super.onBackPressed();
        }
        else {
            Toast.makeText(context, "Double tap back to exit the app!", Toast.LENGTH_SHORT).show();
            doubleTap=true;
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleTap=false;
                }
            },500); //half second
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentuser=mAuth.getCurrentUser();
        if (currentuser==null){

            //Toast.makeText(context, "Login First!", Toast.LENGTH_SHORT).show();
            sendUserToMainActivity();

        }else{
            if (!currentuser.isEmailVerified()){
                Intent intent=new Intent(StudentPanel.this,VerifyUser.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
// changes made in class
        if (currentuser!=null){
            String uid=currentuser.getUid();
            mRef.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("user_tag").exists()){
                        String user_type=dataSnapshot.child("user_tag").getValue().toString().trim();
                        if (user_type.equals("f")){
                            if (!dataSnapshot.child("teaching_subjects").exists()){
                                Toast.makeText(StudentPanel.this, "First fill these details!", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(StudentPanel.this,FacultyTestQuiz.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        DisplayAllUserQuestions();
//......................................
    }

    private void UserMenuSelector(MenuItem menuItem) {

        switch (menuItem.getItemId()){

            case R.id.nav_my_home:
                Toast.makeText(getApplicationContext(), "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_imp_marked:
                Toast.makeText(getApplicationContext(), "Important marked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_my_profile:
                Intent profileIntent=new Intent(StudentPanel.this,ProfileActivity.class);
                //profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(profileIntent);
                //finish();
                break;

            case R.id.nav_log_out:
                AlertDialog.Builder builder=new AlertDialog.Builder(StudentPanel.this);
                builder.setTitle("Dou you really want to logout!");
                builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        sendUserToMainActivity();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog=builder.create();
                alertDialog.show();
                break;

            case R.id.nav_about_us:
                Intent aboutIntent=new Intent(StudentPanel.this,AboutUsActivity.class);
                startActivity(aboutIntent);
                break;
        }
    }

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(StudentPanel.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.student_panel_menu,menu);
        MenuItem item=menu.findItem(R.id.action_search);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
/*

        if (item.getItemId()==R.id.action_search){
            Toast.makeText(context, "Searching under process!", Toast.LENGTH_SHORT).show();
            return true;
        }
*/

        if (item.getItemId()==R.id.action_sort){
            //Toast.makeText(context, "Working Fine", Toast.LENGTH_SHORT).show();
            showSortingAlertDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void showSortingAlertDialog() {

        String[] sortOptions={"Newest","Oldest"};

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Sort by")
                .setIcon(R.drawable.ic_action_sort)
                .setItems(sortOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){

                            SharedPreferences.Editor editor=mSharedPref.edit();
                            editor.putString("Sort","newest");
                            editor.apply();
                            recreate();

                        }else if (which==1){

                            SharedPreferences.Editor editor=mSharedPref.edit();
                            editor.putString("Sort","oldest");
                            editor.apply();
                            recreate();

                        }
                    }
                });
        builder.show();

    }

    private void firebaseSearch(String searchText){
        Query firebaseSearchQuery=mRef.orderByChild("title").startAt(searchText).endAt(searchText  +"\uf8ff");

        FirebaseRecyclerAdapter<Model,QuestionViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Model, QuestionViewHolder>(
                Model.class,
                R.layout.question_recycler,
                QuestionViewHolder.class,
                firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(QuestionViewHolder viewHolder, Model model, int position) {
            //from here we can check if user is anonymous or not than we can display profile image and name of user accordingly

                final String postKey=getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setSubject(model.getSubject());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setProfile_image(model.getProfile_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent replyQuestionIntent=new Intent(StudentPanel.this,CompleteQuestionActivity.class);
                        replyQuestionIntent.putExtra("postKey",postKey);
                        startActivity(replyQuestionIntent);
                    }
                });
            }
        };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }
}
