package com.amitsharma.action.collegeforum;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.media.CamcorderProfile.get;


/**
 * A simple {@link Fragment} subclass.
 */
public class ImpQuestionFragment extends Fragment {
    private View ImpQuestionView;

    private DatabaseReference mRef,mImpRef,mQuestionDatabase;
    RecyclerView mRecyclerView;
    String name="",uid="";
    private FirebaseUser currentuser;
    private FirebaseAuth mAuth;

    LinearLayoutManager linearLayoutManager;

    public ImpQuestionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ImpQuestionView= inflater.inflate(R.layout.fragment_imp_question, container, false);

        mRecyclerView= ImpQuestionView.findViewById(R.id.imp_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        mQuestionDatabase=FirebaseDatabase.getInstance().getReference().child("Questions");
        mImpRef= FirebaseDatabase.getInstance().getReference().child("Imp_Questions");
        mRef= FirebaseDatabase.getInstance().getReference().child("Users");

        return ImpQuestionView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(mQuestionDatabase, Model.class)
                .build();


        FirebaseRecyclerAdapter<Model,ImpQuestionViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Model,ImpQuestionViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ImpQuestionViewHolder holder, final int position, @NonNull final Model model) {
                //from here we can check if user is anonymous or not than we can display profile image and name of user accordingly

                final String postKey=getRef(position).getKey();
                mAuth=FirebaseAuth.getInstance();
                final String curId=mAuth.getCurrentUser().getUid();
                boolean found=false;

                mImpRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(postKey).hasChild(curId)){

                            holder.setName(model.getName());
                            holder.setDate(model.getDate());
                            holder.setTime(model.getTime());
                            holder.setSubject(model.getSubject());
                            holder.setTitle(model.getTitle());
                            holder.setProfile_image(model.getProfile_image());


                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent replyQuestionIntent=new Intent(getActivity(),CompleteQuestionActivity.class);
                                    replyQuestionIntent.putExtra("postKey",postKey);
                                    startActivity(replyQuestionIntent);
                                }
                            });
                        }else{
                            holder.hide();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ImpQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.question_recycler,parent,false);
                ImpQuestionFragment.ImpQuestionViewHolder viewHolder=new ImpQuestionViewHolder(view);
                return viewHolder;
            }
        };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public static class ImpQuestionViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public ImpQuestionViewHolder(View itemView) {
            super(itemView);
            //itemView.setVisibility(View.GONE);

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

        public void hide(){
            itemView.setVisibility(View.GONE);
        }

        public void show(){
            itemView.setVisibility(View.VISIBLE);
        }

    }
}
