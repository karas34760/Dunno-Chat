package ChatApp.android.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ChatApp.android.Adapters.TimelineAdapter;
import ChatApp.android.Adapters.UserAdapter;
import ChatApp.android.Model.Post;
import ChatApp.android.Model.User;
import ChatApp.android.R;
import ChatApp.android.databinding.FragmentTimelinesBinding;


public class Timelines extends Fragment {

    private FragmentTimelinesBinding binding;
    RecyclerView timelinerv;
    DatabaseReference database;
    ArrayList<Post> posts;
    TimelineAdapter timelineAdapter;
    EditText postplace;
    AppCompatButton postbtn;
    Post post;
    String current_uid;
    boolean is_post = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding=FragmentTimelinesBinding.inflate(inflater,container,false);
        View view= binding.getRoot();
        createTimeline();
        PostPlace();
        PostButton();
        return view;

    }

    //get all the posts in current user timeline
    public void createTimeline(){
        timelinerv = binding.timelineRecyclerview;

        current_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        posts = new ArrayList<>();

        database=FirebaseDatabase.getInstance().getReference("timeline").child(current_uid);
        database.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                HashMap<String, String> likeslistMap = new HashMap<String, String>();
                                ArrayList<String> likeslist = new ArrayList<>();
                                posts.clear();
                                likeslist.clear();
                                likeslistMap.clear();
                                for(DataSnapshot datas: snapshot.getChildren()){
                                    post = new Post();
                                    post.setPostid(datas.getKey());
                                    post.setUid(datas.child("uid").getValue().toString());
                                    post.setContent(datas.child("content").getValue().toString());
                                    post.setTimestamp((Long) datas.child("timestamp").getValue());
                                    if(datas.child("likes").exists())
                                    {
                                        int i = 0;
                                        post.setLiked(false);

                                        likeslistMap = (HashMap<String, String>) datas.child("likes").getValue();

                                        Set<String> keySet = likeslistMap.keySet();

                                        likeslist = new ArrayList<String>(keySet);
                                        if(likeslist.contains(current_uid))
                                        {
                                            post.setLiked(true);
                                        }

                                        post.setLikes(likeslist);
                                    }
                                    posts.add(post);
                                }
                                timelineAdapter.notifyDataSetChanged();
                            }
                        },300);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        timelineAdapter = new TimelineAdapter(getActivity(), posts);
        timelinerv.setLayoutManager(new LinearLayoutManager(getActivity()));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        timelinerv.setAdapter(timelineAdapter);

    }

    //initialize the place for creating post
    public void PostPlace()
    {
        postplace = binding.timelinePostplace;
        postplace.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    postplace.setLines(2);
                    postplace.setMaxLines(4);
                } else {
                    postplace.setLines(1);
                    hideKeyboard(view);

                }
            }
        });
    }

    //set behaviour for posting button
    public void PostButton()
    {

        postbtn = binding.timelinePostbutton;
        postbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(postplace.getText() != null) {

                    Date date = new Date();

                    //set check behaviour
                    is_post = true;

                    //create random id for post
                    final String postId = FirebaseDatabase.getInstance().getReference().push().getKey();

                    database = FirebaseDatabase.getInstance().getReference("timeline").child(current_uid).child(postId);

                    HashMap<String, Object> newpost = new HashMap<>();
                    newpost.put("uid", current_uid);
                    newpost.put("content", postplace.getText().toString());
                    newpost.put("timestamp", date.getTime());

                    Toast.makeText(getContext(), "Posting...", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Create post in current user timeline
                            database.updateChildren(newpost).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), "Done...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    },1000);

                    ///Clear posting place after posting
                    postplace.setText("");

                    //create posts in current user's friend list
                    FirebaseDatabase.getInstance().getReference("users").child(current_uid).
                            child("friends").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(is_post) {
                                is_post = false;
                                for (DataSnapshot uids : snapshot.getChildren()) {
                                    String friend_uid = uids.getKey().trim();
                                    FirebaseDatabase.getInstance().getReference("timeline")
                                            .child(friend_uid).child(postId).updateChildren(newpost);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else
                {
                    Toast.makeText(getContext(), "Please input texts", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //hide keyboard when click outside of the posting place
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}