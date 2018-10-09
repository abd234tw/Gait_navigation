package com.example.allen.gait_navigation;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class arrive extends AppCompatActivity {

    int RESULT_OK=2;
    //語音導航
    private TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrive);

        ImageView imageView=findViewById(R.id.imageView);
        Glide.with(this).load(R.drawable.firework).into(imageView);

        //語音導航
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.TAIWAN);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        speak();//輸出到達目的地提醒
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });


        //------------------新增打卡點-----------------
        Bundle bundle =this.getIntent().getExtras();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        int size=bundle.getInt("size"),
                floor=bundle.getInt("floor"),
                size2=bundle.getInt("size2"),
                floor2=bundle.getInt("floor2");

        String ID=bundle.getString("ID"),place= bundle.getString("place"),place2=bundle.getString("place2");
        boolean check=bundle.getBoolean("check_bl"),check2=bundle.getBoolean("check_bl2");
        ArrayList<String> user_name=new ArrayList<>();
        ArrayList<String> user_name2=new ArrayList<>();
        ArrayList<Integer> user_turn=new ArrayList<>();
        ArrayList<Integer> user_turn2=new ArrayList<>();
        ArrayList<Integer> user_like=new ArrayList<>();
        ArrayList<Integer> user_like2=new ArrayList<>();

        float[] user_x=new float[size],user_y=new float[size],user_direction=new float[size],user_x2=new float[size2],user_y2=new float[size2],user_direction2=new float[size2];
        user_like=bundle.getIntegerArrayList("like");
        user_like2=bundle.getIntegerArrayList("like2");

        user_name=bundle.getStringArrayList("user_name");
        user_turn=bundle.getIntegerArrayList("user_turn");
        user_x=bundle.getFloatArray("user_x");
        user_y=bundle.getFloatArray("user_y");
        user_direction=bundle.getFloatArray("user_direction");
        user_name2=bundle.getStringArrayList("user_name2");
        user_turn2=bundle.getIntegerArrayList("user_turn2");
        user_x2=bundle.getFloatArray("user_x2");
        user_y2=bundle.getFloatArray("user_y2");
        user_direction2=bundle.getFloatArray("user_direction2");



        if(check)
        {
            for (int i=0;i<size;i++){
                DatabaseReference myRef_Name = database.getReference("Users").child(ID).child("user_map").child(place).child(String.valueOf(floor)).child(String.valueOf(i)).child("name");
                DatabaseReference myRef_X = database.getReference("Users").child(ID).child("user_map").child(place).child(String.valueOf(floor)).child(String.valueOf(i)).child("X");
                DatabaseReference myRef_Y = database.getReference("Users").child(ID).child("user_map").child(place).child(String.valueOf(floor)).child(String.valueOf(i)).child("Y");
                DatabaseReference mydir = database.getReference("Users").child(ID).child("user_map").child(place).child(String.valueOf(floor)).child(String.valueOf(i)).child("direction");
                DatabaseReference myturn = database.getReference("Users").child(ID).child("user_map").child(place).child(String.valueOf(floor)).child(String.valueOf(i)).child("turn");
                DatabaseReference mylike = database.getReference("Users").child(ID).child("user_map").child(place).child(String.valueOf(floor)).child(String.valueOf(i)).child("like");

                myRef_Name.setValue(user_name.get(i));
                myRef_X.setValue(String.valueOf(user_x[i]));
                myRef_Y.setValue(String.valueOf(user_y[i]));
                mydir.setValue(String.valueOf(user_direction[i]));
                myturn.setValue(user_turn.get(i));
                mylike.setValue(user_like.get(i));

            }

            Toast.makeText(arrive.this,"已新增"+floor+"樓打卡點",Toast.LENGTH_SHORT).show();
        }

        if(check2)
        {
            for (int i=0;i<size2;i++){
                DatabaseReference myRef_Name2 = database.getReference("Users").child(ID).child("user_map").child(place2).child(String.valueOf(floor2)).child(String.valueOf(i)).child("name");
                DatabaseReference myRef_X2 = database.getReference("Users").child(ID).child("user_map").child(place2).child(String.valueOf(floor2)).child(String.valueOf(i)).child("X");
                DatabaseReference myRef_Y2 = database.getReference("Users").child(ID).child("user_map").child(place2).child(String.valueOf(floor2)).child(String.valueOf(i)).child("Y");
                DatabaseReference mydir2 = database.getReference("Users").child(ID).child("user_map").child(place2).child(String.valueOf(floor2)).child(String.valueOf(i)).child("direction");
                DatabaseReference myturn2 = database.getReference("Users").child(ID).child("user_map").child(place2).child(String.valueOf(floor2)).child(String.valueOf(i)).child("turn");
                DatabaseReference mylike2 = database.getReference("Users").child(ID).child("user_map").child(place2).child(String.valueOf(floor2)).child(String.valueOf(i)).child("like");

                myRef_Name2.setValue(user_name2.get(i));
                myRef_X2.setValue(String.valueOf(user_x2[i]));
                myRef_Y2.setValue(String.valueOf(user_y2[i]));
                mydir2.setValue(String.valueOf(user_direction2[i]));
                myturn2.setValue(user_turn2.get(i));
                mylike2.setValue(user_like2.get(i));
            }
            Toast.makeText(arrive.this,"已新增"+floor2+"樓打卡點",Toast.LENGTH_SHORT).show();
        }

        Button return_btn2=findViewById(R.id.return_btn);
        return_btn2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(arrive.this,navigation.class);
                setResult(RESULT_OK, intent);
                arrive.this.finish();


            }
        });

    }

    //語音導航
    private void speak() {
        String text = "成功抵達目的地";
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }

        super.onDestroy();
    }
}
