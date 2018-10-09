package com.example.allen.gait_navigation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private DrawerLayout drawer;
    private long backPressedTime;

    Button getdata_btn,navigation_btn,reset_btn;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mRootRef;

    int db_index=0;
    ArrayList<String> user_get_name = new ArrayList<String>();
    ArrayList<Integer> user_get_turn = new ArrayList<Integer>();
    ArrayList<Float> user_get_x = new ArrayList<Float>(), user_get_y = new ArrayList<Float>(), user_get_direction = new ArrayList<Float>();
    String mCurrent_user_id;

    TextView user_name,user_email;
    String user_name_st;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();//得到目前 USER 資料並儲存到 firebase

        //如果使用者沒有登錄會是null
        if (currentUser == null){
            sendToStart();//到StartActivity
        }else {
//            mUserRef.child("online").setValue(true);//Part 26 online feature
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar Set
        //mToolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("鼴鼠導遊");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();//得到目前 USER 資料並儲存到 firebase

        //如果使用者沒有登錄會是null
        if (currentUser == null){
            //會直接去跑onStart()
        }else{
            mCurrent_user_id = mAuth.getCurrentUser().getUid();

            mRootRef = FirebaseDatabase.getInstance().getReference();
            mRootRef.child("Users").child(mCurrent_user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user_name_st = dataSnapshot.child("name").getValue().toString();
                    user_name.setText(user_name_st);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // Drawer Menu
            drawer = findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            View hView = navigationView.getHeaderView(0);
            user_name = hView.findViewById(R.id.header_name);
            user_email = hView.findViewById(R.id.header_email);
            //名字的設定在上面(不知道為啥只能寫在onChange裡面...外面會變成空白)
            user_email.setText(user.getEmail());


            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_home);//預設顯示fragment_home頁面
            }

        }

    }

    //Drawer Menu
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_navigation:
                Intent intent=new Intent(MainActivity.this,navigation.class);
                startActivity(intent);
                break;
            case R.id.nav_map:

                break;
            case R.id.nav_favorite:

                break;
            case R.id.nav_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_setting:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SettingFragment()).commit();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    //創建右上角Menu (log out 、about...)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }
    //右上角Menu清單選擇 (log out 、about...)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn){
            CharSequence options[] = new CharSequence[]{"Yes", "No"};

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Logout confirmation");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //Click Event for each item.
                    if(which == 0){//Open Profile
                        FirebaseAuth.getInstance().signOut();//登出
                        sendToStart();//登出並回到StartActivity
                    }

                    if(which == 1) {//Send message

                    }
                }
            });
            builder.show();
        }

        if (item.getItemId() == R.id.main_settings_btn){
//            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
//            startActivity(settingsIntent);
        }

        if (item.getItemId() == R.id.main_about_btn){
//            Intent settingsIntent = new Intent(MainActivity.this, UsersActivity.class);
//            startActivity(settingsIntent);
        }
        return true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("確認視窗")
                    .setMessage("確定要結束應用程式嗎?")
                    .setIcon(R.drawable.ic_launcher_background)
                    .setPositiveButton("確定",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    finish();
                                    System.exit(0);
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub

                                }
                            }).show();
        }
        return true;
    }
}
