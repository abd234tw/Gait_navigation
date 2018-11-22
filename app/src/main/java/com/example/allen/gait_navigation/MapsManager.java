package com.example.allen.gait_navigation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.security.AccessController.getContext;

public class MapsManager extends AppCompatActivity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    String mcurrent_user_id=mAuth.getCurrentUser().getUid();
    ArrayList<String> get_all_maps=new ArrayList<>(),get_my_maps=new ArrayList<>(),temp_maps=new ArrayList<>();
    ArrayList<String> user_get_name = new ArrayList<String>(),user_get_message = new ArrayList<>();
    ArrayList<Integer> user_get_turn = new ArrayList<Integer>(), user_get_like = new ArrayList<>();
    ArrayList<Float> user_get_x = new ArrayList<Float>(), user_get_y = new ArrayList<Float>(), user_get_direction = new ArrayList<Float>();

    ArrayAdapter<String> all_maps,my_maps;
    //選單
    AlertDialog alertDialog;
    boolean isexist=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_manager);
        final TabHost tabhost=findViewById(R.id.TabH);
        final ListView listView1=findViewById(R.id.listView1);
        final ListView listView2=findViewById(R.id.listView2);
        tabhost.setup();
        all_maps = new ArrayAdapter(this, android.R.layout.simple_list_item_1, get_all_maps);
        my_maps = new ArrayAdapter(this, android.R.layout.simple_list_item_1, get_my_maps);
        //----------------------------------------------------------------------------------
        DatabaseReference myRef_all_maps = database.getReference("Map").child("Location");
        myRef_all_maps.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                        get_all_maps.add(ds.getValue().toString());
                }
                //get_all_maps.add("請選擇以上地點新增");  //解spinner bug
                listView1.setAdapter(all_maps);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference myRef_my_maps = database.getReference("Users").child(mcurrent_user_id).child("user_map").child("Location");
        myRef_my_maps.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                get_my_maps.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                        get_my_maps.add(ds.getValue().toString());
                }
                //get_my_maps.add("請選擇以上地點刪除");  //解spinner bug
                listView2.setAdapter(my_maps);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        //---------------------------------------------------------------------------




        listView1.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
                alertDialog = new AlertDialog.Builder(MapsManager.this).setTitle("確定要新增此地圖檔嗎?")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface,
                                                int paramAnonymousInt) {
                                for(int i=0;i<get_my_maps.size();i++)
                                {
                                    if(get_my_maps.get(i).equals(get_all_maps.get(arg2)))
                                    {
                                        isexist=true;
                                    }
                                }
                                if(!isexist)
                                {
                                    //----------------map----------------------
                                    DatabaseReference myRef3 = database.getReference("Map");
                                    myRef3.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (int k = 0; k < dataSnapshot.child(get_all_maps.get(arg2)).getChildrenCount(); k++)//數館底下有幾樓
                                    {
                                        for (int i=0;i<dataSnapshot.child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).getChildrenCount();i++) //數館哪層樓底下有多少點
                                        {
                                            user_get_x.add(Float.valueOf(dataSnapshot.child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("X").getValue().toString()));
                                            user_get_y.add(Float.valueOf(dataSnapshot.child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("Y").getValue().toString()));
                                            user_get_direction.add(Float.valueOf(dataSnapshot.child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("direction").getValue().toString()));
                                            user_get_turn.add(Integer.valueOf(dataSnapshot.child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("turn").getValue().toString()));
                                            user_get_name.add(dataSnapshot.child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("name").getValue().toString());
                                            user_get_like.add(Integer.valueOf(dataSnapshot.child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("like").getValue().toString()));
                                            user_get_message.add(dataSnapshot.child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("message").getValue().toString());
                                            DatabaseReference myRef_Name = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("name");
                                            DatabaseReference myRef_X = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("X");
                                            DatabaseReference myRef_Y = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("Y");
                                            DatabaseReference mydir = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("direction");
                                            DatabaseReference myturn = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("turn");
                                            DatabaseReference mylike = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("like");
                                            DatabaseReference myRef_message = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(get_all_maps.get(arg2)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("message");

                                            myRef_Name.setValue(user_get_name.get(i));
                                            myRef_X.setValue(String.valueOf(user_get_x.get(i)));
                                            myRef_Y.setValue(String.valueOf(user_get_y.get(i)));
                                            mydir.setValue(String.valueOf(user_get_direction.get(i)));
                                            myturn.setValue(user_get_turn.get(i));
                                            mylike.setValue(user_get_like.get(i));
                                            myRef_message.setValue(user_get_message.get(i));
                                        }
                                        user_get_name.clear();
                                        user_get_x.clear();
                                        user_get_y.clear();
                                        user_get_direction.clear();
                                        user_get_turn.clear();
                                        user_get_like.clear();
                                        user_get_message.clear();
                                    }
                                    }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                    });
                                    //--------------location-------------

                                    DatabaseReference myRef_Name = database.getReference("Users").child(mcurrent_user_id).child("user_map").child("Location").child(String.valueOf(get_my_maps.size()));
                                    myRef_Name.setValue(get_all_maps.get(arg2));
                                    listView2.setAdapter(my_maps);
                                    Toast.makeText(MapsManager.this, "正在儲存地圖", Toast.LENGTH_SHORT).show();
                                }else
                                {
                                    Toast.makeText(MapsManager.this, "已存在的地圖", Toast.LENGTH_SHORT).show();
                                    isexist=false;//重設旗標
                                }

                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.hide();
                            }
                        }).create();
                alertDialog.show();

            }
        });



        listView2.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {

                alertDialog = new AlertDialog.Builder(MapsManager.this).setTitle("確定要刪除地圖嗎?")
                        .setPositiveButton("確定",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface,
                                                int paramAnonymousInt) {
                                database.getReference("Users").child(mcurrent_user_id).child("user_map").child(get_my_maps.get(arg2)).removeValue();//清空選取地圖
                                get_my_maps.remove(arg2);
                                database.getReference("Users").child(mcurrent_user_id).child("user_map").child("Location").removeValue();//清選取的Location
                                for (int i=0;i<get_my_maps.size();i++){
                                    DatabaseReference myRef_Name = database.getReference("Users").child(mcurrent_user_id).child("user_map").child("Location").child(String.valueOf(i));
                                    myRef_Name.setValue(get_my_maps.get(i));
                                }
                                listView2.setAdapter(my_maps);
                            }

                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.hide();
                            }
                        }).create();
                alertDialog.show();

            }

        });

        //---------------------------------------------------------------------------
        TabHost.TabSpec TS;

        TS=tabhost.newTabSpec("");
        TS.setContent(R.id.tab1);
        TS.setIndicator("提供地圖");
        tabhost.addTab(TS);

        TS=tabhost.newTabSpec("");
        TS.setContent(R.id.tab2);
        TS.setIndicator("個人地圖");
        tabhost.addTab(TS);

    }





}
