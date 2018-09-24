package com.example.allen.gait_navigation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private CardView add_new_map_card,reset_card,navigation_card,navigation_outdoor;
    Intent intent;
    //    Button getdata_btn,navigation_btn,reset_btn;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<String>user_get_place=new ArrayList<>();
    ArrayList<String> user_get_name = new ArrayList<String>();
    ArrayList<Integer> user_get_turn = new ArrayList<Integer>(), user_get_like = new ArrayList<>();
    ArrayList<Float> user_get_x = new ArrayList<Float>(), user_get_y = new ArrayList<Float>(), user_get_direction = new ArrayList<Float>();
    String mcurrent_user_id = mAuth.getCurrentUser().getUid();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

//        getdata_btn= view.findViewById(R.id.getdata_btn);
//        navigation_btn = view.findViewById(R.id.navigation_btn);
//        reset_btn = view.findViewById(R.id.reset_btn);

        //選單 CardView
        add_new_map_card = view.findViewById(R.id.add_new_map_card);
        reset_card = view.findViewById(R.id.reset_card);
        navigation_card = view.findViewById(R.id.navigation_card);
        navigation_outdoor=view.findViewById(R.id.navigation_outdoor);
        add_new_map_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),GetPoint.class);
                startActivity(intent);
            }
        });

        navigation_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),navigation.class);
                startActivity(intent);
            }
        });

        navigation_outdoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),MapsActivity.class);
                startActivity(intent);
            }
        });
//        getdata_btn.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent=new Intent(getContext(),GetPoint.class);
//                startActivity(intent);
//            }
//        });
//
//        navigation_btn.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getContext(),navigation.class);
//                startActivity(intent);
//            }
//        });

        DatabaseReference myRef2 = database.getReference("Map").child("Location");// 為了把主要地圖丟到每個使用者底下  <--  database_btn 做前面的事
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot2) {
                for (DataSnapshot ds2:dataSnapshot2.getChildren())
                    user_get_place.add(ds2.getValue().toString());

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        reset_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset();
            }
        });
//        reset_btn.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Reset();
//            }
//        });
//        return inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }


    private void Reset(){
        CharSequence options[] = new CharSequence[]{"確定", "取消"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("重置地圖");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Click Event for each item.
                if(which == 0){
                    database.getReference("Users").child(mcurrent_user_id).child("user_map").removeValue();//清空地圖
                    Resetlocation();
                    ResetDatabase();
                }

                if(which == 1) {

                }
            }
        });
        builder.show();
    }

    private void ResetDatabase() {

        DatabaseReference myRef3 = database.getReference("Map");
        myRef3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
                            DatabaseReference myRef_Name = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("name");
                            DatabaseReference myRef_X = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("X");
                            DatabaseReference myRef_Y = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("Y");
                            DatabaseReference mydir = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("direction");
                            DatabaseReference myturn = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("turn");
                            DatabaseReference mylike = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(user_get_place.get(j)).child(String.valueOf(k+1)).child(String.valueOf(i)).child("like");
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

    private  void Resetlocation()
    {
        database.getReference("Users").child(mcurrent_user_id).child("user_map").child("Location").removeValue();//清空地圖
        for (int i=0;i<user_get_place.size();i++){
            DatabaseReference myRef_Name = database.getReference("Users").child(mcurrent_user_id).child("user_map").child("Location").child(String.valueOf(i));
            myRef_Name.setValue(user_get_place.get(i));
        }
    }

}