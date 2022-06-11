package ChatApp.android.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import ChatApp.android.Fragments.AccountDetail;
import ChatApp.android.Fragments.ContactUser;
import ChatApp.android.Fragments.ConversationUser;
import ChatApp.android.Fragments.Timelines;
import ChatApp.android.Model.User;
import ChatApp.android.R;

import ChatApp.android.databinding.ActivityUserHomeChatBinding;


public class UserHomeChat extends AppCompatActivity {
    private ActivityUserHomeChatBinding binding;

    ImageButton ButtonScanQRCode;
    User user;
    ConversationUser conversationUserFragment=new ConversationUser();
    AccountDetail accountDetailFragment=new AccountDetail();
    ContactUser contactUserFragment=new ContactUser();
    Timelines timelinesFragment=new Timelines();
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityUserHomeChatBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_user_home_chat);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,conversationUserFragment).commit();
        bottomNavigationView=findViewById(R.id.bottomNavigationView);

//        String token = FirebaseMessaging.getInstance().getToken().toString();
//        Log.d("TOKEN",token);
//        FirebaseMessaging.getInstance().deleteToken();
            FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
//                        NotificationModel nm = new NotificationModel(task.getResult(),"test","haha");
//                        new PushNotificationSender().execute(nm);
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseDatabase.getInstance().getReference("users").child(uid).child("token").setValue(task.getResult());
                                Log.d("TOKEN", task.getResult());
                            }
                        });
//        String uid = FirebaseAuth.getInstance().getUid();
//        Log.d("UID",uid);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem item) {
              switch (item.getItemId()){
                  case R.id.message:
                      getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,conversationUserFragment).commit();
                      return  true;
                  case R.id.contact:

                      getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,contactUserFragment).commit();
                      return true;
                  case R.id.timeline:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,timelinesFragment).commit();
                      return true;
                  case R.id.account:
                      getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,accountDetailFragment).commit();
                      return  true;
              }
              return false;
          }
      });
      onScanQRCode();
}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }
    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);

        transaction.commit();
    }

    private void onScanQRCode()
    {
        ButtonScanQRCode = findViewById(R.id.ButtonQrScan);
        ButtonScanQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UserHomeChat.this,ScanQrCode.class);
                startActivity(i);
            }
        });
    }
    

    
}