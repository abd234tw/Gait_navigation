package com.example.allen.gait_navigation;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private EditText mEtPassword,mEtCheckPassword;
    private Button mCreateBtn;
    private Button mBtnPassword,mBtnCheckPassword;
    private TextView mSignIn_btn;
    private Toolbar mToolbar;

    //Firebase Auth
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    ArrayList<String>user_get_place=new ArrayList<>();
    ArrayList<String> user_get_name = new ArrayList<String>();
    ArrayList<Integer> user_get_turn = new ArrayList<Integer>(), user_get_like = new ArrayList<>();
    ArrayList<Float> user_get_x = new ArrayList<Float>(), user_get_y = new ArrayList<Float>(), user_get_direction = new ArrayList<Float>();
    private DatabaseReference mDatabase;
    String uid;

    private ProgressBar mProgressbar_cycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar Set
        mToolbar = (Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressbar_cycle = (ProgressBar) findViewById(R.id.progressBar_cyclic);
        mProgressbar_cycle.setVisibility(View.GONE);

        //Android Field
        mDisplayName = (TextInputLayout)findViewById(R.id.reg_displayname);
        mEmail = (TextInputLayout)findViewById(R.id.reg_email);
        mPassword = (TextInputLayout)findViewById(R.id.reg_password);
        mEtPassword = findViewById(R.id.et_register_password);
        mEtCheckPassword = findViewById(R.id.et_register_checkpassword);
        mSignIn_btn = findViewById(R.id.sign_in);
        mCreateBtn = (Button)findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                //String password = mPassword.getEditText().getText().toString();
                String password = mEtPassword.getText().toString();//為了加eye button做了修改
                String checkpassword = mEtCheckPassword.getText().toString();

                //判斷Edit 是否空白
                if (!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    if (password.equals(checkpassword)){
                        mProgressbar_cycle.setVisibility(View.VISIBLE);
                        register_user(display_name, email, password);//用來放firebase create new user account (assistant Step 4.)
                    }else {
                        Toast.makeText(RegisterActivity.this,"[Error] Please check password.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(RegisterActivity.this,"[Error] Please check the form and try again.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mSignIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent = new Intent(RegisterActivity.this,StartActivity.class);
                startActivity(reg_intent);
            }
        });


    }

    private void register_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    //Part 9 Database
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", display_name);
                    userMap.put("status", "Hi，我正在使用鼴鼠導遊APP");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");
                    userMap.put("user_map", "");
                    userMap.put("height","170");

                    Resetlocation();
                    ResetDatabase();

                    //mDatabase.setValue(userMap);//設定完成 啟動 (這行跟下面這段差別在判斷是否完成，和可命令完成後要做甚麼)
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);//成功就去MainActivity
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });

                }else{
                    mProgressbar_cycle.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this,"You got some error. Please check the form and try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void ResetDatabase() {
//        Toast.makeText(getContext(),"ResetDatabase",Toast.LENGTH_SHORT).show();
        DatabaseReference myRef3 = database.getReference("Map");
        myRef3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                //String uid = current_user.getUid();

                for(int j=0;j<dataSnapshot.getChildrenCount()-1;j++)  //剪掉location那個  數有幾個館
                {
                    for (int k = 0; k < dataSnapshot.child(user_get_place.get(j)).getChildrenCount(); k++)//數館底下有幾樓
                    {
                        for (int i=0;i<dataSnapshot.child(user_get_place.get(j)).child(String.valueOf(k+1)).getChildrenCount();i++) //數館哪層樓底下有多少點
                        {
                            user_get_x.add(Float.valueOf(dataSnapshot.child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("X").getValue().toString()));
                            user_get_y.add(Float.valueOf(dataSnapshot.child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("Y").getValue().toString()));
                            user_get_direction.add(Float.valueOf(dataSnapshot.child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("direction").getValue().toString()));
                            user_get_turn.add(Integer.valueOf(dataSnapshot.child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("turn").getValue().toString()));
                            user_get_name.add(dataSnapshot.child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("name").getValue().toString());
                            user_get_like.add(Integer.valueOf(dataSnapshot.child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("like").getValue().toString()));
                            DatabaseReference myRef_Name = database.getReference("Users").child(uid).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("name");
                            DatabaseReference myRef_X = database.getReference("Users").child(uid).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("X");
                            DatabaseReference myRef_Y = database.getReference("Users").child(uid).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("Y");
                            DatabaseReference mydir = database.getReference("Users").child(uid).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("direction");
                            DatabaseReference myturn = database.getReference("Users").child(uid).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("turn");
                            DatabaseReference mylike = database.getReference("Users").child(uid).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("like");
                            myRef_Name.setValue(user_get_name.get(i));
                            myRef_X.setValue(String.valueOf(user_get_x.get(i)));
                            myRef_Y.setValue(String.valueOf(user_get_y.get(i)));
                            mydir.setValue(String.valueOf(user_get_direction.get(i)));
                            myturn.setValue(user_get_turn.get(i));
                            mylike.setValue(user_get_like.get(i));
                        }
                        user_get_name.clear();
                        user_get_x.clear();
                        user_get_y.clear();
                        user_get_direction.clear();
                        user_get_turn.clear();
                        user_get_like.clear();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private  void Resetlocation() {
//        Toast.makeText(RegisterActivity.this,user_get_place.get(0)+user_get_place.get(1),Toast.LENGTH_SHORT).show();

        //FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        //String uid = current_user.getUid();

//        database.getReference("Users").child(uid).child("user_map").child("Location").removeValue();//清空地圖
        DatabaseReference myRef2 = database.getReference("Map").child("Location");// 為了把主要地圖丟到每個使用者底下  <--  database_btn 做前面的事
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot2) {
                for (DataSnapshot ds2:dataSnapshot2.getChildren())
                    user_get_place.add(ds2.getValue().toString());
                for (int i=0;i<user_get_place.size();i++){
                    DatabaseReference myRef_Name = database.getReference("Users").child(uid).child("user_map").child("Location").child(String.valueOf(i));
                    myRef_Name.setValue(user_get_place.get(i));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
