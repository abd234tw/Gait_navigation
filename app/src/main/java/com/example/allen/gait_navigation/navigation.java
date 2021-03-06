package com.example.allen.gait_navigation;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import junit.framework.Test;

import java.lang.reflect.Array;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class navigation extends AppCompatActivity implements SensorEventListener{

    //Firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    String mcurrent_user_id=mAuth.getCurrentUser().getUid();
    //****************Compass*******************
    Button scan_btn,start_nav_btn,voice_btn,checkin_btn,Choice_place1_btn,Choice_place2_btn
            ,Choice_start_btn,Choice_end_btn,Choice_floor1_btn,Choice_floor2_btn,start_again_btn,
            ad_msg_btn;
    TextView tv_degree,test,results;
    ImageView img_arrow;
    //    EditText name_edit;
    private static SensorManager sensorManager;
    Sensor mSensor;
    Sensor magneticSensor;
    Sensor accelerometerSensor;
    Sensor stepDetector;
    float currentDegree;
    Drawl bDrawl;
    Bundle bundle=new Bundle();
    //****************變數*********************
    ArrayList<String> get_start_floor=new ArrayList<>(),get_name=new ArrayList<String>(),user_get_name=new ArrayList<String>();
    ArrayList<String> get_end_floor=new ArrayList<>(),get_name_2=new ArrayList<String>(),user_get_name_2=new ArrayList<String>();
    ArrayList<String> get_message=new ArrayList<>();
    ArrayList<Integer> get_turn=new ArrayList<>(),user_get_turn=new ArrayList<>(),get_like = new ArrayList<>(),get_like2 = new ArrayList<>();
    ArrayList<Integer> get_turn_2=new ArrayList<>(),user_get_turn_2=new ArrayList<>();
    ArrayList<Float> get_x=new ArrayList<Float>(),get_y=new ArrayList<Float>(),get_direction=new ArrayList<Float>(),
            user_get_x=new ArrayList<Float>(),user_get_y=new ArrayList<Float>(),user_get_direction=new ArrayList<Float>();
    ArrayList<Float> get_x_2=new ArrayList<Float>(),get_y_2=new ArrayList<Float>(),get_direction_2=new ArrayList<Float>(),
            user_get_x_2=new ArrayList<Float>(),user_get_y_2=new ArrayList<Float>(),user_get_direction_2=new ArrayList<Float>();
    double distance=0,stepDistance=0.6,Walking_distance=0,totalstep=0;
    int get_floor1=1,get_floor2=1,start_int,end_int,stepCount,getStepCount_before,requestCode=1,requestCode2=2,requestCode0=0,get_path_floor,user_get_floor,user_get_floor2,checkin_c=0,checkin_c2=0;
    int[] path=new int [100];
    int[] path2=new int[100];
    int index=0, index2=0,path_c=0,path_c2=0,insert_c;
    double[][] dist = new double[100][100];
    double[][] dist2= new double[100][100];
    float[][] dir=new float[100][100];
    float[][] dir2=new float[100][100];
    String[] msg;
    float Current_direction;
    Boolean set=false,start_navigation=false,start_dir=false,checkin_bl=false,up_down_floor=false,start_again=false,path_not_finish=true,checkin_bl2=false,qr=false,start_voice=false;
    Timer timer;
    //***************下拉選單*******************
    Spinner spinner_start,spinner_end;
    //*******************************************



    //語音導航
    private TextToSpeech mTTS;
    boolean hasbeen_spoke = false;

    //選單
    AlertDialog alertDialog;
    View view;
    List<String> data = new ArrayList<>();
    ListView listView;
    ArrayAdapter<String> place_adapter,floor1_adapter,floor2_adapter,list_start_adapter,list_end_adapter;
    EditText search_et;

    long floor_count=0;
    String getPlace_str1,getPlace_str2;
    ArrayList<String> get_place=new ArrayList<>();

    boolean start_btn_bool = false;

    String ad_message;//廣告推播
    Boolean ad_time=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        //初始化路徑陣列
        for (int i=0;i<path.length;i++)
            path[i]=-1;
        for(int i=0;i<dir.length;i++)
            for(int j=0;j<dir.length;j++)
                dir[i][j]=1000;

        //**************Compass  宣告***************
        results=findViewById(R.id.results);
        tv_degree = findViewById(R.id.tv_degree);
        img_arrow = findViewById(R.id.img_arrow);
        test = findViewById(R.id.test);
        voice_btn = findViewById(R.id.voice_btn);//語音輸入
        start_nav_btn = findViewById(R.id.start_nav_btn);
        scan_btn = findViewById(R.id.scan_btn);//QRcode
        checkin_btn = findViewById(R.id.checkin_btn);
        start_again_btn=findViewById(R.id.start_again_btn);

        Choice_place1_btn = findViewById(R.id.Choice_place1_btn);//選擇地點(建築)
        Choice_place2_btn = findViewById(R.id.Choice_place2_btn);//選擇地點(建築)
        Choice_floor1_btn = findViewById(R.id.Choice_floor1_btn);//選擇樓層
        Choice_floor2_btn = findViewById(R.id.Choice_floor2_btn);
        Choice_start_btn = findViewById(R.id.Choice_start_btn);//選擇起點
        Choice_end_btn = findViewById(R.id.Choice_end_btn);//選擇目的地
        ad_msg_btn = findViewById(R.id.ad_msg_btn);//廣告推播

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_GAME);

        DatabaseReference myRef_height = database.getReference("Users").child(mcurrent_user_id).child("height");  //從使用者抓下來的地圖資訊
        myRef_height.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                stepDistance = Double.parseDouble(dataSnapshot.getValue().toString())*0.45/100;
                Toast.makeText(navigation.this,dataSnapshot.getValue().toString(),Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

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
//                        mButtonSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        ad_msg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(navigation.this,ad_message,Toast.LENGTH_SHORT).show();
            }
        });

        //******************地點(建築)資料*******************
        DatabaseReference myRef_place = database.getReference("Users").child(mcurrent_user_id).child("user_map").child("Location");
        myRef_place.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
//                    Toast.makeText(navigation.this, "hi", Toast.LENGTH_SHORT).show();
                    get_place.add(ds.getValue().toString());
                }
                get_place.add("請選擇以上地點");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Choice_place1_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = getLayoutInflater().inflate(R.layout.alertdialog_listview, null);
                alertDialog = new AlertDialog.Builder(navigation.this).setTitle("選擇開始地點")
                        .setView(view)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface,
                                                int paramAnonymousInt) {

                            }
                        }).create();

                listView = view.findViewById(R.id.listView);
                search_et = view.findViewById(R.id.search_et);
                place_adapter = new ArrayAdapter<String>(navigation.this,android.R.layout.simple_list_item_1,get_place);
                listView.setAdapter(place_adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        Toast.makeText(navigation.this, "你選的是" + position, Toast.LENGTH_SHORT).show();
                        if (position==get_place.size()-1)
                            position=0;
                        Choice_place1_btn.setText(get_place.get(position));
                        getPlace_str1=get_place.get(position);
                        change_start_place(getPlace_str1);
                        alertDialog.hide();
                    }
                });
                alertDialog.show();

                //選單搜尋
                search_et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        navigation.this.place_adapter.getFilter().filter(s);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                if (position==get_place.size()-1)
                                    position=0;
                                Choice_place1_btn.setText(String.valueOf(parent.getItemAtPosition(position)));
                                getPlace_str1 = String.valueOf(parent.getItemAtPosition(position));
                                change_start_place(getPlace_str1);
                                alertDialog.hide();
                            }
                        });
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });

        Choice_place2_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = getLayoutInflater().inflate(R.layout.alertdialog_listview, null);
                alertDialog = new AlertDialog.Builder(navigation.this).setTitle("選擇開始地點")
                        .setView(view)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface,
                                                int paramAnonymousInt) {

                            }
                        }).create();

                listView = view.findViewById(R.id.listView);
                search_et = view.findViewById(R.id.search_et);
                place_adapter = new ArrayAdapter<String>(navigation.this,android.R.layout.simple_list_item_1,get_place);
                listView.setAdapter(place_adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        Toast.makeText(navigation.this, "你選的是" + position, Toast.LENGTH_SHORT).show();
                        if (position==get_place.size()-1)
                            position=0;
                        Choice_place2_btn.setText(get_place.get(position));
                        getPlace_str2=get_place.get(position);
                        change_end_place(getPlace_str2);
                        alertDialog.hide();
                    }
                });
                alertDialog.show();

                //選單搜尋
                search_et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        navigation.this.place_adapter.getFilter().filter(s);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                if (position==get_place.size()-1)
                                    position=0;
                                Choice_place2_btn.setText(String.valueOf(parent.getItemAtPosition(position)));
                                getPlace_str2 = String.valueOf(parent.getItemAtPosition(position));
                                change_end_place(getPlace_str2);
                                alertDialog.hide();
                            }
                        });
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });

        Choice_floor1_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = getLayoutInflater().inflate(R.layout.alertdialog_listview, null);
                alertDialog = new AlertDialog.Builder(navigation.this).setTitle("選擇樓層")
                        .setView(view)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface,
                                                int paramAnonymousInt) {

                            }
                        }).create();

                listView = view.findViewById(R.id.listView);
                search_et = view.findViewById(R.id.search_et);
                floor1_adapter = new ArrayAdapter<String>(navigation.this,android.R.layout.simple_list_item_1,get_start_floor);
                listView.setAdapter(floor1_adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        Toast.makeText(navigation.this, "你選的是" + position, Toast.LENGTH_SHORT).show();
                        Choice_floor1_btn.setText(get_start_floor.get(position)+"F");
                        get_floor1 = position+1;
                        change_start_floor(get_floor1);
                        alertDialog.hide();
                    }
                });
                alertDialog.show();

                //選單搜尋
                search_et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        navigation.this.floor1_adapter.getFilter().filter(s);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Choice_floor1_btn.setText(String.valueOf(parent.getItemAtPosition(position))+"F");
                                get_floor1 = Integer.valueOf(String.valueOf(parent.getItemAtPosition(position)));
                                change_start_floor(get_floor1);
                                alertDialog.hide();
                            }
                        });
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });

        Choice_floor2_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = getLayoutInflater().inflate(R.layout.alertdialog_listview, null);
                alertDialog = new AlertDialog.Builder(navigation.this).setTitle("選擇樓層")
                        .setView(view)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface,
                                                int paramAnonymousInt) {

                            }
                        }).create();

                listView = view.findViewById(R.id.listView);
                search_et = view.findViewById(R.id.search_et);
                floor2_adapter = new ArrayAdapter<String>(navigation.this,android.R.layout.simple_list_item_1,get_end_floor);
                listView.setAdapter(floor2_adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        Toast.makeText(navigation.this, "你選的是" + position, Toast.LENGTH_SHORT).show();
                        Choice_floor2_btn.setText(get_end_floor.get(position)+"F");
                        get_floor2 = position+1;
                        change_end_floor(get_floor2);
                        alertDialog.hide();
                    }
                });
                alertDialog.show();

                //選單搜尋
                search_et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        navigation.this.floor2_adapter.getFilter().filter(s);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Choice_floor2_btn.setText(String.valueOf(parent.getItemAtPosition(position))+"F");
                                get_floor2 = Integer.valueOf(String.valueOf(parent.getItemAtPosition(position)));
                                change_end_floor(get_floor2);
                                alertDialog.hide();
                            }
                        });
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

            }
        });


        Choice_start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = getLayoutInflater().inflate(R.layout.alertdialog_listview, null);
                alertDialog = new AlertDialog.Builder(navigation.this).setTitle("選擇起點")
                        .setView(view)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface,
                                                int paramAnonymousInt) {

                            }
                        }).create();

                listView = view.findViewById(R.id.listView);
                search_et = view.findViewById(R.id.search_et);
                list_start_adapter = new ArrayAdapter<String>(navigation.this,android.R.layout.simple_list_item_1,get_name);
                listView.setAdapter(list_start_adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        Toast.makeText(navigation.this, "你選的是" + position, Toast.LENGTH_SHORT).show();
                        if (position==get_name.size()-1)
                            position=0;
                        Choice_start_btn.setText(get_name.get(position));
                        start_int = position;
                        alertDialog.hide();
                    }
                });
                alertDialog.show();

                //選單搜尋
                search_et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        navigation.this.list_start_adapter.getFilter().filter(s);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                int ad_position=list_start_adapter.getPosition(String.valueOf(parent.getItemAtPosition(position)));
//                                Toast.makeText(navigation.this, String.valueOf(test), Toast.LENGTH_SHORT).show();

                                if (position==get_name.size()-1)
                                    position=0;
                                Choice_start_btn.setText(String.valueOf(parent.getItemAtPosition(position)));
                                start_int = ad_position;
                                alertDialog.hide();
                            }
                        });
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });

        Choice_end_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = getLayoutInflater().inflate(R.layout.alertdialog_listview, null);
                alertDialog = new AlertDialog.Builder(navigation.this).setTitle("選擇目的地")
                        .setView(view)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface,
                                                int paramAnonymousInt) {

                            }
                        }).create();

                listView = view.findViewById(R.id.listView);
                search_et = view.findViewById(R.id.search_et);
                list_end_adapter = new ArrayAdapter<String>(navigation.this,android.R.layout.simple_list_item_1,get_name_2);
                listView.setAdapter(list_end_adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        Toast.makeText(navigation.this, "你選的是" + position, Toast.LENGTH_SHORT).show();
                        if (position==get_name_2.size()-1)
                            position=0;
                        Choice_end_btn.setText(get_name_2.get(position));
                        end_int = position;
                        alertDialog.hide();
                    }
                });
                alertDialog.show();

                //選單搜尋
                search_et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        navigation.this.list_end_adapter.getFilter().filter(s);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                int ad_position=list_end_adapter.getPosition(String.valueOf(parent.getItemAtPosition(position)));

                                Choice_end_btn.setText(String.valueOf(parent.getItemAtPosition(position)));
                                end_int = ad_position;
                                alertDialog.hide();
                            }
                        });
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });

        //************************按鈕***************************
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                layout();   //輸出座標
                if (ad_time == true){
                    Ad_message(path[index]);
                    ad_time = false;
                }

            }
        };
        start_again_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_again=true;
                start_navigation=true;
                hasbeen_spoke = false;//語音導航
                tv_degree.setText("請繼續前進");
            }
        });



        voice_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說話..."); //語音辨識 Dialog 上要顯示的提示文字
                startActivityForResult(intent, requestCode0);
            }
        });

        scan_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(navigation.this,QRcode.class);
                startActivityForResult(intent,requestCode);
            }
        });


        checkin_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                view = getLayoutInflater().inflate(R.layout.alertdialog_edittext, null);
                final EditText checkin_name_edt = view.findViewById(R.id.alertdialog_edt);
                alertDialog = new AlertDialog.Builder(navigation.this).setTitle("打卡")
                        .setView(view)
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface,
                                                int paramAnonymousInt) {
                                //--------新增使用者打卡的點-----------
                                if (checkin_name_edt.getText().toString().isEmpty()){//*******還有bug，輸入文字但沒有開始導航時按打卡會爆掉
                                    Toast.makeText(navigation.this,"請輸入打卡名稱",Toast.LENGTH_SHORT).show();
                                } else{
                                    if (!up_down_floor) //不是上下樓
                                    {

                                        if (path[index]<path[index+1])//0~1 正走
                                        {
                                            insert_c=1;
                                        }else
                                        {
                                            insert_c=0;   //反走
                                        }

                                        //存入資料庫要的資料 我的最愛、地點名稱、方向、旗標、XY座標、樓層
                                        user_get_floor=get_floor1;  //取得打卡樓層
                                        get_like.add(path[index+insert_c]+checkin_c,1);
                                        user_get_name.add(path[index+insert_c]+checkin_c,checkin_name_edt.getText().toString());   // 地點名稱       path[index]現在的點
                                        user_get_direction.add(path[index+insert_c]+checkin_c,get_direction.get(path[index]));//方向跟目前的點一樣 因為在兩點之間
                                        user_get_turn.add(path[index+insert_c]+checkin_c,0);  //兩點間只有一條路 設 0

                                        if ((user_get_x.get(path[index])<user_get_x.get(path[index+1])))   //下一點比較大 往大的走用加的
                                            user_get_x.add(path[index+insert_c]+checkin_c,  user_get_x.get(path[index])+ Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                        else if(user_get_x.get(path[index])>user_get_x.get(path[index+1]))  //下一點比較小 往小的走用減的
                                            user_get_x.add(path[index+insert_c]+checkin_c,  user_get_x.get(path[index])- Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                        else   //等於零 表示同一個X
                                            user_get_x.add(path[index+insert_c]+checkin_c,  user_get_x.get(path[index]));


                                        if ((user_get_y.get(path[index])<user_get_y.get(path[index+1])))   //下一點比較大 往大的走用加的
                                            user_get_y.add(path[index+insert_c]+checkin_c,  user_get_y.get(path[index])+ Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                        else if(user_get_y.get(path[index])>user_get_y.get(path[index+1]))  //下一點比較小 往小的走用減的
                                            user_get_y.add(path[index+insert_c]+checkin_c,  user_get_y.get(path[index])- Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                        else   //等於零 表示同一個X
                                            user_get_y.add(path[index+insert_c]+checkin_c,  user_get_y.get(path[index]));

                                        checkin_c++;
                                        checkin_bl=true;
                                    }else//是上下樓
                                    {

                                        if (!start_again)    // 還沒上下樓
                                        {
                                            if (path[index]<path[index+1])//0~1 正走
                                            {
                                                insert_c=1;
                                            }else
                                            {
                                                insert_c=0;   //反走
                                            }

                                            //存入資料庫要的資料 我的最愛、地點名稱、方向、旗標、XY座標、樓層
                                            user_get_floor=get_floor1;  //取得打卡樓層
                                            get_like.add(path[index+insert_c]+checkin_c,1);
                                            user_get_name.add(path[index+insert_c]+checkin_c,checkin_name_edt.getText().toString());   // 地點名稱       path[index]現在的點
                                            user_get_direction.add(path[index+insert_c]+checkin_c,get_direction.get(path[index]));//方向跟目前的點一樣 因為在兩點之間
                                            user_get_turn.add(path[index+insert_c]+checkin_c,0);  //兩點間只有一條路 設 0

                                            if ((user_get_x.get(path[index])<user_get_x.get(path[index+1])))   //下一點比較大 往大的走用加的
                                                user_get_x.add(path[index+insert_c]+checkin_c,  user_get_x.get(path[index])+ Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                            else if(user_get_x.get(path[index])>user_get_x.get(path[index+1]))  //下一點比較小 往小的走用減的
                                                user_get_x.add(path[index+insert_c]+checkin_c,  user_get_x.get(path[index])- Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                            else   //等於零 表示同一個X
                                                user_get_x.add(path[index+insert_c]+checkin_c,  user_get_x.get(path[index]));


                                            if ((user_get_y.get(path[index])<user_get_y.get(path[index+1])))   //下一點比較大 往大的走用加的
                                                user_get_y.add(path[index+insert_c]+checkin_c,  user_get_y.get(path[index])+ Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                            else if(user_get_y.get(path[index])>user_get_y.get(path[index+1]))  //下一點比較小 往小的走用減的
                                                user_get_y.add(path[index+insert_c]+checkin_c,  user_get_y.get(path[index])- Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                            else   //等於零 表示同一個X
                                                user_get_y.add(path[index+insert_c]+checkin_c,  user_get_y.get(path[index]));

                                            checkin_bl=true;
                                            checkin_c++;
                                        }else
                                        {
                                            if (path2[index2]<path2[index2+1])//0~1 正走
                                            {
                                                insert_c=1;
                                            }else
                                            {
                                                insert_c=0;   //反走
                                            }

                                            user_get_floor2=get_floor2;
                                            get_like.add(path[index2+insert_c]+checkin_c2,1);
                                            user_get_name_2.add(path2[index2+insert_c]+checkin_c2,checkin_name_edt.getText().toString());   // 地點名稱       path[index]現在的點
                                            user_get_direction_2.add(path2[index2+insert_c]+checkin_c2,get_direction_2.get(path2[index2]));//方向跟目前的點一樣 因為在兩點之間
                                            user_get_turn_2.add(path2[index2+insert_c]+checkin_c2,0);  //兩點間只有一條路 設 0


                                            if ((user_get_x_2.get(path2[index2])<user_get_x_2.get(path2[index2+1])))   //下一點比較大 往大的走用加的
                                                user_get_x_2.add(path2[index2+insert_c]+checkin_c2,  user_get_x_2.get(path2[index2])+ Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                            else if(user_get_x_2.get(path2[index2])>user_get_x_2.get(path2[index2+1]))  //下一點比較小 往小的走用減的
                                                user_get_x_2.add(path2[index2+insert_c]+checkin_c2,  user_get_x_2.get(path2[index2])- Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                            else   //等於零 表示同一個X
                                                user_get_x_2.add(path2[index2+insert_c]+checkin_c2,  user_get_x_2.get(path2[index2]));


                                            if ((user_get_y_2.get(path2[index2])<user_get_y_2.get(path2[index2+1])))   //下一點比較大 往大的走用加的
                                                user_get_y_2.add(path2[index2+insert_c]+checkin_c2,  user_get_y_2.get(path2[index2])+ Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                            else if(user_get_y_2.get(path2[index2])>user_get_y_2.get(path2[index2+1]))  //下一點比較小 往小的走用減的
                                                user_get_y_2.add(path2[index2+insert_c]+checkin_c2,  user_get_y_2.get(path2[index2])- Float.valueOf(String.valueOf((stepCount-getStepCount_before)*stepDistance)));
                                            else   //等於零 表示同一個X
                                                user_get_y_2.add(path2[index2+insert_c]+checkin_c2,  user_get_y_2.get(path2[index2]));

                                            checkin_c2++;
                                            checkin_bl2=true;
                                        }
                                    }
                                    Toast.makeText(navigation.this,"請繼續完成導航，以便新增新地點",Toast.LENGTH_SHORT).show();
                                }
                                alertDialog.hide();
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

        //*********************導航*******************************
        start_nav_btn.setOnClickListener(new Button.OnClickListener() {     //導航
            @Override
            public void onClick(final View view) {

                if (floor1_adapter==null||floor2_adapter==null||list_end_adapter==null||list_start_adapter==null){
                    Toast.makeText(navigation.this,"請先選擇起點和目的地",Toast.LENGTH_SHORT).show();
                }else{

                    if (!start_btn_bool){
                        start_btn_bool = true;
                        scan_btn.setVisibility(View.INVISIBLE);//隱藏QRcode
                        voice_btn.setVisibility(View.INVISIBLE);//隱藏語音鍵
                        checkin_btn.setVisibility(View.VISIBLE);//顯示打卡鍵

                        //按鈕變化
                        start_nav_btn.setTextColor(Color.parseColor("#FC3E4F"));
                        start_nav_btn.setBackground(getResources().getDrawable(R.drawable.round_button_red));
                        start_nav_btn.setText("結束");

                        if (get_floor1!=get_floor2)
                        {
                            up_down_floor=true;
                            best_path();
                            best_path_floor();
                            GetStepCount();
                        }else
                        {
                            up_down_floor=false;
                            best_path();
                            GetStepCount();
                        }
                        start_dir=true;
                        set=true;
                        drawmap();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {

                                if (!up_down_floor)  //不是上下樓 (同樓層)
                                {

                                    if (( Math.round(Current_direction) - (Math.round(dir[path[index]][path[index+1]]))<=20&&Math.round(Current_direction) - (Math.round(dir[path[index]][path[index+1]]))>=-20)||path[0]==path[1]) {
                                        start_navigation = true;
                                        if (start_voice == false) {
                                            speak(-1);//語音輸出: 開始導航
                                            start_voice = true;
                                        }
                                    }
                                    if (start_navigation&&path[0]!=path[1])  //路徑跟下個不一樣 開始
                                    {
                                        distance = dist[path[index]][path[index+1]];

                                        //語音導航，判斷向左、向右 (左-、右+)
                                        int dir_judge = 0;
                                        if (index<path_c-2){
                                            if ((dir[path[index]][path[index+1]] > -179 && dir[path[index]][path[index+1]] < -90)){//第一條位於-90~-180
                                                if (dir[path[index]][path[index+1]] > dir[path[index+1]][path[index+2]]){
                                                    dir_judge = 1;//左轉
                                                }else if (dir[path[index]][path[index+1]] < dir[path[index+1]][path[index+2]]){
                                                    if (dir[path[index+1]][path[index+2]] > -179 && dir[path[index+1]][path[index+2]] < 0){//第二條
                                                        dir_judge = 2;//右轉
                                                    }else{
                                                        dir_judge = 1;//左轉
                                                    }

                                                }else{
                                                    dir_judge = 3;//繼續直走
                                                }
                                            }else if((dir[path[index]][path[index+1]] > 90 && dir[path[index]][path[index+1]] < 179)){//第一條位於90~180
                                                if (dir[path[index]][path[index+1]] > dir[path[index+1]][path[index+2]]){
                                                    if (dir[path[index+1]][path[index+2]] > 0 && dir[path[index+1]][path[index+2]] < 180){//第二條
                                                        dir_judge = 1;//左轉
                                                    }else{
                                                        dir_judge = 2;//右轉
                                                    }
                                                }else if (dir[path[index]][path[index+1]] < dir[path[index+1]][path[index+2]]){
                                                    dir_judge = 2;//右轉
                                                }else{
                                                    dir_judge = 3;//繼續直走
                                                }
                                            }else{
                                                if (dir[path[index]][path[index+1]] > dir[path[index+1]][path[index+2]]){
                                                    dir_judge = 1;//左轉
                                                }else if (dir[path[index]][path[index+1]] < dir[path[index+1]][path[index+2]]){
                                                    dir_judge = 2;//右轉
                                                }else{
                                                    dir_judge = 3;//繼續直走
                                                }
                                            }
                                        }

                                        //語音導航 目標前幾步發出提醒
                                        if (distance - Walking_distance < 5 && hasbeen_spoke == false) {
                                            speak(dir_judge);
                                            hasbeen_spoke = true;
                                        }

                                        if (distance - Walking_distance > 1) {
                                            Walking_distance = (stepCount - getStepCount_before) * stepDistance;
                                        } else {
                                            index++;
                                            getStepCount_before = stepCount;
                                            Walking_distance = 0;
                                            hasbeen_spoke = false;
                                            ad_time = true;
                                            ad_msg_btn.setVisibility(View.GONE);
                                        }

                                    }
                                    if (path[index]==end_int||path[0]==path[1]) //路徑跟下個一樣 結束
                                    {
                                        start_navigation=false;
                                        start_dir=false;
                                        Intent intent=new Intent(navigation.this,arrive.class);
                                        int size=user_get_name.size();
                                        float[] user_x=new float[size],user_y=new float[size],user_direction=new float[size];
                                        for(int i=0; i<size; i++){
                                            user_x[i] = user_get_x.get(i);
                                            user_y[i] = user_get_y.get(i);
                                            user_direction[i] = user_get_direction.get(i);
                                        }
                                        bundle.putStringArrayList("user_name",user_get_name);
                                        bundle.putIntegerArrayList("user_turn",user_get_turn);
                                        bundle.putFloatArray("user_x",user_x);
                                        bundle.putFloatArray("user_y",user_y);
                                        bundle.putFloatArray("user_direction",user_direction);
                                        bundle.putBoolean("check_bl",checkin_bl);
                                        bundle.putInt("size",size);
                                        bundle.putString("ID",mcurrent_user_id);
                                        bundle.putString("place",getPlace_str1);
                                        bundle.putInt("floor",user_get_floor);
                                        bundle.putIntegerArrayList("like",get_like);
                                        timer.cancel();
                                        intent.putExtras(bundle);
                                        startActivityForResult(intent,requestCode2);
                                    }
                                }
                                else
                                {
                                    if (path_not_finish)
                                    {
                                        //跨樓層part1
                                        if (( Math.round(Current_direction) - (Math.round(dir[path[index]][path[index+1]]))<=20&&Math.round(Current_direction) - (Math.round(dir[path[index]][path[index+1]]))>=-20)||path[0]==path[1]) {
                                            start_navigation = true;
                                            if (start_voice == false) {
                                                speak(-1);//語音輸出: 開始導航
                                                start_voice = true;
                                            }
                                        }
                                        if (start_navigation&&path[0]!=path[1])  //path1
                                        {
                                            distance = dist[path[index]][path[index+1]];

                                            //語音導航，判斷向左、向右 (左-、右+)
                                            int dir_judge = 0;
                                            if (index<path_c-2){
                                                if ((dir[path[index]][path[index+1]] > -179 && dir[path[index]][path[index+1]] < -90)){//第一條位於-90~-180
                                                    if (dir[path[index]][path[index+1]] > dir[path[index+1]][path[index+2]]){
                                                        dir_judge = 1;//左轉
                                                    }else if (dir[path[index]][path[index+1]] < dir[path[index+1]][path[index+2]]){
                                                        if (dir[path[index+1]][path[index+2]] > -179 && dir[path[index+1]][path[index+2]] < 0){//第二條
                                                            dir_judge = 2;//右轉
                                                        }else{
                                                            dir_judge = 1;//左轉
                                                        }

                                                    }else{
                                                        dir_judge = 3;//繼續直走
                                                    }
                                                }else if((dir[path[index]][path[index+1]] > 90 && dir[path[index]][path[index+1]] < 179)){//第一條位於90~180
                                                    if (dir[path[index]][path[index+1]] > dir[path[index+1]][path[index+2]]){
                                                        if (dir[path[index+1]][path[index+2]] > 0 && dir[path[index+1]][path[index+2]] < 180){//第二條
                                                            dir_judge = 1;//左轉
                                                        }else{
                                                            dir_judge = 2;//右轉
                                                        }
                                                    }else if (dir[path[index]][path[index+1]] < dir[path[index+1]][path[index+2]]){
                                                        dir_judge = 2;//右轉
                                                    }else{
                                                        dir_judge = 3;//繼續直走
                                                    }
                                                }else{
                                                    if (dir[path[index]][path[index+1]] > dir[path[index+1]][path[index+2]]){
                                                        dir_judge = 1;//左轉
                                                    }else if (dir[path[index]][path[index+1]] < dir[path[index+1]][path[index+2]]){
                                                        dir_judge = 2;//右轉
                                                    }else{
                                                        dir_judge = 3;//繼續直走
                                                    }
                                                }
                                            }

                                            //語音導航 目標前幾步發出提醒
                                            if (distance - Walking_distance < 5 && hasbeen_spoke == false) {
                                                speak(dir_judge);
                                                hasbeen_spoke = true;
                                            }

                                            if (distance - Walking_distance > 1) {
                                                Walking_distance = (stepCount - getStepCount_before) * stepDistance;
                                            } else {
                                                index++;
                                                getStepCount_before = stepCount;
                                                Walking_distance = 0;
                                                hasbeen_spoke = false;
                                            }
                                        }
                                    }
                                    if (path[index]==get_path_floor&&!start_again)
                                    {
                                        start_dir=false;
                                        path_not_finish=false;
                                        start_navigation=false;
                                        Walking_distance=0;
                                        stepCount=0;
                                        getStepCount_before=0;
                                    }
                                    if (start_again)
                                    {
                                        //跨樓層part2
                                        distance = dist2[path2[index2]][path2[index2+1]];

                                        //語音導航，判斷向左、向右
                                        int dir_judge = 0;
                                        if (index2<path_c2-2){
                                            if (dir2[path2[index2]][path2[index2+1]] > -179 && dir2[path2[index2]][path2[index2+1]] < -90){
                                                if (dir2[path2[index2]][path2[index2+1]] > dir2[path2[index2+1]][path2[index2+2]]){
                                                    dir_judge = 2;//右轉
                                                }else if (dir2[path2[index2]][path2[index2+1]] < dir2[path2[index2+1]][path2[index2+2]]){
                                                    if (dir2[path2[index2+1]][path2[index2+2]] > -179 && dir2[path2[index2+1]][path2[index2+2]] < 0){//第二條
                                                        dir_judge = 2;//右轉
                                                    }else{
                                                        dir_judge = 1;//左轉
                                                    }
                                                }else{
                                                    dir_judge = 3;//繼續直走
                                                }
                                            }else{
                                                if (dir2[path2[index2]][path2[index2+1]] > dir2[path2[index2+1]][path2[index2+2]]){
                                                    dir_judge = 1;//左轉
                                                }else if (dir2[path2[index2]][path2[index2+1]] < dir2[path2[index2+1]][path2[index2+2]]){
                                                    dir_judge = 2;//右轉
                                                }else{
                                                    dir_judge = 3;//繼續直走
                                                }
                                            }
                                        }

                                        //語音導航 目標前幾步發出提醒
                                        if (distance - Walking_distance < 5 && hasbeen_spoke == false) {
                                            speak(dir_judge);
                                            hasbeen_spoke = true;
                                        }

                                        if (distance - Walking_distance > 1) {
                                            Walking_distance = (stepCount - getStepCount_before) * stepDistance;
                                        } else {
                                            index2++;
                                            getStepCount_before = stepCount;
                                            Walking_distance = 0;
                                            hasbeen_spoke = false;
                                        }
                                        if (path2[index2]==end_int)
                                        {
                                            start_navigation=false;
                                            Intent intent=new Intent(navigation.this,arrive.class);
                                            int size=user_get_name.size();
                                            int size2=user_get_name_2.size();
                                            float[] user_x=new float[size],user_y=new float[size],user_direction=new float[size];
                                            float[] user_x2=new float[size2],user_y2=new float[size2],user_direction2=new float[size2];
                                            for(int i=0; i<size; i++){
                                                user_x[i] = user_get_x.get(i);
                                                user_y[i] = user_get_y.get(i);
                                                user_direction[i] = user_get_direction.get(i);
                                            }
                                            for(int i=0; i<size2; i++){
                                                user_x2[i] = user_get_x_2.get(i);
                                                user_y2[i] = user_get_y_2.get(i);
                                                user_direction2[i] = user_get_direction_2.get(i);
                                            }
                                            bundle.putStringArrayList("user_name",user_get_name);
                                            bundle.putStringArrayList("user_name2",user_get_name_2);
                                            bundle.putIntegerArrayList("user_turn",user_get_turn);
                                            bundle.putIntegerArrayList("user_turn2",user_get_turn_2);
                                            bundle.putFloatArray("user_x",user_x);
                                            bundle.putFloatArray("user_x2",user_x2);
                                            bundle.putFloatArray("user_y",user_y);
                                            bundle.putFloatArray("user_y2",user_y2);
                                            bundle.putFloatArray("user_direction",user_direction);
                                            bundle.putFloatArray("user_direction2",user_direction2);
                                            bundle.putBoolean("check_bl",checkin_bl);
                                            bundle.putBoolean("check_bl2",checkin_bl2);
                                            bundle.putInt("size",size);
                                            bundle.putInt("size2",size2);
                                            bundle.putString("ID",mcurrent_user_id);
                                            bundle.putString("place",getPlace_str1);
                                            bundle.putString("place2",getPlace_str2);
                                            bundle.putInt("floor",get_floor1);
                                            bundle.putInt("floor2",get_floor2);
                                            bundle.putIntegerArrayList("like",get_like);
                                            bundle.putIntegerArrayList("like2",get_like2);
                                            timer.cancel();
                                            intent.putExtras(bundle);
                                            startActivityForResult(intent,requestCode2);
                                        }
                                    }

                                }
                                Message m1 = new Message();
                                handler.sendMessage(m1);
                            }
                        };
                        timer =new Timer(true);
                        timer.schedule(task,1000,500);
                    }else{
                        restartActivity();
                    }


                }

            }
        });
        //*****************按鈕結尾*****************
    }

    //網路是否有連線
    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else{
            return false;
        }
    }
    //沒網路則顯示提醒
    public android.app.AlertDialog.Builder buildDialog(Context c) {
        View view = getLayoutInflater().inflate(R.layout.alertdialog_no_wifi, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(c);
        builder.setView(view);
        builder.setTitle("網路連線失敗");
//        builder.setMessage("請開啟Wifi或手機行動網路");

        builder.setPositiveButton("確認", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder;
    }

    public void restartActivity(){
        Intent mIntent = getIntent();
        finish();
        startActivity(mIntent);
    }

    //語音導航
    private void speak(int dir_judge) {
        String text = "";//mEditText.getText().toString();
        if (dir_judge == -1){
            text = "開始導航";
        }else if (dir_judge == 1){
            text = "前方路口向左轉";
        }else if (dir_judge == 2){
            text = "前方路口向右轉";
        }else if (dir_judge == 3){
            text = "請繼續直走";
        }
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

    private void Ad_message(int index){
        ad_message = get_message.get(index);
        if (ad_message.isEmpty()){
            ad_msg_btn.setVisibility(View.GONE);
        }else {
            ad_msg_btn.setVisibility(View.VISIBLE);
            Toast.makeText(navigation.this,ad_message,Toast.LENGTH_SHORT).show();
        }

    }

    //實際距離、行走距離、步數、前步數
    private void layout() {
        //test.setText("步數: "+String.valueOf(getStepCount_before) +" "+String.valueOf(stepCount)+"剩餘距離: "+String.format("%.2f",distance)+" 已走距離: "+String.format("%.2f",Walking_distance));
        test.setText("目前步數: "+String.valueOf(stepCount-getStepCount_before)+" 離下一點步數: "+Math.round(distance/stepDistance)+"總步數"+Math.round(totalstep));
        if(path[index]==get_path_floor&&!start_again)
        {
            if (get_floor1>get_floor2)
            {
                tv_degree.setText("請往下到"+get_floor2+"樓"+"\n"+"然後按下「已上下樓」按鈕");
                if (hasbeen_spoke == false){
                    String text = tv_degree.getText().toString();
                    mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    hasbeen_spoke = true;
                }
                start_again_btn.setVisibility(View.VISIBLE);//顯示上下樓鍵
            }
            else if(get_floor1<get_floor2)
            {
                tv_degree.setText("請往上到"+get_floor2+"樓"+"\n"+"然後按下「已上下樓」按鈕");
                if (hasbeen_spoke == false){
                    String text = tv_degree.getText().toString();
                    mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    hasbeen_spoke = true;
                }
                start_again_btn.setVisibility(View.VISIBLE);//顯示上下樓鍵
            }
        }

    }
    //***firebase get data***//
    void change_start_place(String place){
        if (list_start_adapter!=null){
            list_start_adapter.clear();
            Choice_start_btn.setText("選擇起點");
        }
        if (floor1_adapter!=null){
            floor1_adapter.clear();
            Choice_floor1_btn.setText("選擇樓層");
        }

        DatabaseReference myRef = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(place);  //從使用者抓下來的地圖資訊
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                floor_count = dataSnapshot.getChildrenCount();
                get_start_floor.clear();
                for (int i=0;i<floor_count;i++){
                    get_start_floor.add(String.valueOf(i+1));
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    void change_end_place(String place){
        if (list_end_adapter!=null){
            list_end_adapter.clear();
            Choice_end_btn.setText("選擇目的地");
        }
        if (floor2_adapter!=null){
            floor2_adapter.clear();
            Choice_floor2_btn.setText("選擇樓層");
        }

        DatabaseReference myRef = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(place);  //從使用者抓下來的地圖資訊
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                floor_count = dataSnapshot.getChildrenCount();
                get_end_floor.clear();
                for (int i=0;i<floor_count;i++){
                    get_end_floor.add(String.valueOf(i+1));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    void change_start_floor(int floor){
        if (list_start_adapter!=null)
            list_start_adapter.clear();
        DatabaseReference myRef = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(getPlace_str1);  //從使用者抓下來的地圖資訊
        myRef.child(String.valueOf(floor)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!start_btn_bool) {
                    get_x.clear();
                    get_y.clear();
                    get_direction.clear();
                    get_turn.clear();
                    get_name.clear();
                    user_get_x.clear();
                    user_get_y.clear();
                    user_get_direction.clear();
                    user_get_turn.clear();
                    user_get_name.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        //導航要用不能動到
                        get_x.add(Float.valueOf(String.valueOf(ds.child("X").getValue())));
                        get_y.add(Float.valueOf(String.valueOf(ds.child("Y").getValue())));
                        get_direction.add(Float.valueOf(String.valueOf(ds.child("direction").getValue())));
                        get_turn.add(Integer.valueOf(String.valueOf(ds.child("turn").getValue())));
                        get_name.add(String.valueOf(ds.child("name").getValue()));
                        get_like.add(Integer.valueOf(String.valueOf(ds.child("like").getValue())));
//                        if (ds.hasChild("message")){
//                            get_message.add(String.valueOf(ds.child("message").getValue()));
//                        }
                        get_message.add(String.valueOf(ds.child("message").getValue()));
                        //所以新增的打卡點存在下面
                        user_get_x.add(Float.valueOf(String.valueOf(ds.child("X").getValue())));
                        user_get_y.add(Float.valueOf(String.valueOf(ds.child("Y").getValue())));
                        user_get_direction.add(Float.valueOf(String.valueOf(ds.child("direction").getValue())));
                        user_get_turn.add(Integer.valueOf(String.valueOf(ds.child("turn").getValue())));
                        user_get_name.add(String.valueOf(ds.child("name").getValue()));
                    }
                    get_name.add("請選擇以上地點");
                }
                if (qr)
                {
                    for (int i=0;i<get_name.size();i++)
                        if (get_name.get(i).equals(msg[2]))
                            start_int=i;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    void change_end_floor(int floor){
        if (list_end_adapter!=null)
            list_end_adapter.clear();
        DatabaseReference myRef = database.getReference("Users").child(mcurrent_user_id).child("user_map").child(getPlace_str2);  //從使用者抓下來的地圖資訊
        myRef.child(String.valueOf(floor)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!start_btn_bool)
                {
                    get_x_2.clear();
                    get_y_2.clear();
                    get_direction_2.clear();
                    get_turn_2.clear();
                    get_name_2.clear();
                    user_get_x_2.clear();
                    user_get_y_2.clear();
                    user_get_direction_2.clear();
                    user_get_turn_2.clear();
                    user_get_name_2.clear();
                    for (DataSnapshot ds:dataSnapshot.getChildren())
                    {
                        //導航要用不能動到
                        get_x_2.add(Float.valueOf( String.valueOf(ds.child("X").getValue())));
                        get_y_2.add(Float.valueOf( String.valueOf(ds.child("Y").getValue())));
                        get_direction_2.add(Float.valueOf( String.valueOf(ds.child("direction").getValue())));
                        get_turn_2.add(Integer.valueOf(String.valueOf(ds.child("turn").getValue())));
                        get_name_2.add(String.valueOf(ds.child("name").getValue()));
                        get_like2.add(Integer.valueOf(String.valueOf(ds.child("like").getValue())));

                        //所以新增的打卡點存在下面
                        user_get_x_2.add(Float.valueOf( String.valueOf(ds.child("X").getValue())));
                        user_get_y_2.add(Float.valueOf( String.valueOf(ds.child("Y").getValue()))) ;
                        user_get_direction_2.add(Float.valueOf( String.valueOf(ds.child("direction").getValue())));
                        user_get_turn_2.add(Integer.valueOf(String.valueOf(ds.child("turn").getValue())));
                        user_get_name_2.add(String.valueOf(ds.child("name").getValue()));
                    }
                    get_name_2.add("請選擇以上地點");

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensor != null){
            sensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }else{
            Toast.makeText(navigation.this,"Sensor Not Supported!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // sensor
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // 三个坐标轴方向上的电磁强度，单位是微特拉斯(micro-Tesla)，用uT表示，也可以是高斯(Gauss),1Tesla=10000Gauss
            magneticFieldValues = event.values.clone();
            calculateOrientation();

        }else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            int degree = Math.round(event.values[0]);
            currentDegree = -degree;
            RotateAnimation rotateAnimation;

            if (start_dir) {
                rotateAnimation = new RotateAnimation(currentDegree + dir[path[index]][path[index + 1]], 0, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);    //from degree  to degree   //currentDegree指北 0度 +  座標指向的方向  =  前進方向
                rotateAnimation.setDuration(500);
                rotateAnimation.setFillAfter(true);
                img_arrow.setAnimation(rotateAnimation);

            }
            else if(start_again)
            {
                rotateAnimation = new RotateAnimation(currentDegree + dir2[path2[index2]][path2[index2 + 1]], 0, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);    //from degree  to degree   //currentDegree指北 0度 +  座標指向的方向  =  前進方向
                rotateAnimation.setDuration(500);
                rotateAnimation.setFillAfter(true);
                img_arrow.setAnimation(rotateAnimation);
            }
            else
            {
                rotateAnimation = new RotateAnimation(currentDegree, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);    //from degree  to degree   //currentDegree指北 0度 +  座標指向的方向  =  前進方向
                rotateAnimation.setDuration(500);
                rotateAnimation.setFillAfter(true);
                img_arrow.setAnimation(rotateAnimation);
            }

          /*  if (set)
            {
                if (( Math.round(Current_direction) - (Math.round(dir[path[index]][path[index+1]]))<=20&&Math.round(Current_direction) - (Math.round(dir[path[index]][path[index+1]]))>=-20)||path[0]==path[1])
                    start_navigation=true;
                set=false;
            }
            if (start_navigation) {
                drawmap();
                if (!up_down_floor)  //不是上下樓 (同樓層)
                {
                    if (path[0]!=path[1])  //當前路徑跟下個不一樣 開始
                    {
                        distance = dist[path[index]][path[index+1]];
                        if (distance - Walking_distance > 2) {
                            Walking_distance = (stepCount - getStepCount_before) * stepDistance;
                        } else {
                            index++;
                            getStepCount_before = stepCount;
                            Walking_distance = 0;
                        }
                    }
                    if (path[index]==end_int||path[0]==path[1]) //路徑跟下個一樣 結束
                    {
                        start_navigation=false;
                        start_dir=false;
                        Intent intent=new Intent(navigation.this,arrive.class);
                        int size=user_get_name.size();
                        float[] user_x=new float[size],user_y=new float[size],user_direction=new float[size];
                        for(int i=0; i<size; i++){
                            user_x[i] = user_get_x.get(i);
                            user_y[i] = user_get_y.get(i);
                            user_direction[i] = user_get_direction.get(i);
                        }
                        bundle.putStringArrayList("user_name",user_get_name);
                        bundle.putIntegerArrayList("user_turn",user_get_turn);
                        bundle.putFloatArray("user_x",user_x);
                        bundle.putFloatArray("user_y",user_y);
                        bundle.putFloatArray("user_direction",user_direction);
                        bundle.putBoolean("check_bl",checkin_bl);
                        bundle.putInt("size",size);
                        bundle.putString("ID",mcurrent_user_id);
                        bundle.putString("place",getPlace_str1);
                        bundle.putInt("floor",user_get_floor);
                        bundle.putIntegerArrayList("like",get_like);
                        intent.putExtras(bundle);
                        startActivityForResult(intent,requestCode2);
                    }
                }
                else   //不同樓層
                {
                    if (path_not_finish)  //第一層導航
                    {
                        if (path[0]!=path[1])  //path1
                        {
                            distance = dist[path[index]][path[index+1]];
                            if (distance - Walking_distance > 2) {
                                Walking_distance = (stepCount - getStepCount_before) * stepDistance;
                            } else {
                                index++;
                                getStepCount_before = stepCount;
                                Walking_distance = 0;

                            }
                        }
                    }
                    if (path[index]==get_path_floor&&!start_again)
                    {
                        start_dir=false;
                        path_not_finish=false;
                        start_navigation=false;
                        Walking_distance=0;
                        stepCount=0;
                        getStepCount_before=0;
                    }
                    if (start_again)   //第二層導航
                    {
                        distance = dist2[path2[index2]][path2[index2+1]];
                        if (distance - Walking_distance > 2) {
                            Walking_distance = (stepCount - getStepCount_before) * stepDistance;
                        } else {
                            index2++;
                            getStepCount_before = stepCount;
                            Walking_distance = 0;
                        }
                        if (path2[index2]==end_int)
                        {
                            start_navigation=false;
                            Intent intent=new Intent(navigation.this,arrive.class);
                            int size=user_get_name.size();
                            int size2=user_get_name_2.size();
                            float[] user_x=new float[size],user_y=new float[size],user_direction=new float[size];
                            float[] user_x2=new float[size2],user_y2=new float[size2],user_direction2=new float[size2];
                            for(int i=0; i<size; i++){
                                user_x[i] = user_get_x.get(i);
                                user_y[i] = user_get_y.get(i);
                                user_direction[i] = user_get_direction.get(i);
                            }
                            for(int i=0; i<size2; i++){
                                user_x2[i] = user_get_x_2.get(i);
                                user_y2[i] = user_get_y_2.get(i);
                                user_direction2[i] = user_get_direction_2.get(i);
                            }
                            bundle.putStringArrayList("user_name",user_get_name);
                            bundle.putStringArrayList("user_name2",user_get_name_2);
                            bundle.putIntegerArrayList("user_turn",user_get_turn);
                            bundle.putIntegerArrayList("user_turn2",user_get_turn_2);
                            bundle.putFloatArray("user_x",user_x);
                            bundle.putFloatArray("user_x2",user_x2);
                            bundle.putFloatArray("user_y",user_y);
                            bundle.putFloatArray("user_y2",user_y2);
                            bundle.putFloatArray("user_direction",user_direction);
                            bundle.putFloatArray("user_direction2",user_direction2);
                            bundle.putBoolean("check_bl",checkin_bl);
                            bundle.putBoolean("check_bl2",checkin_bl2);
                            bundle.putInt("size",size);
                            bundle.putInt("size2",size2);
                            bundle.putString("ID",mcurrent_user_id);
                            bundle.putString("place",getPlace_str1);
                            bundle.putString("place2",getPlace_str2);
                            bundle.putInt("floor",get_floor1);
                            bundle.putInt("floor2",get_floor2);
                            bundle.putIntegerArrayList("like",get_like);
                            bundle.putIntegerArrayList("like2",get_like2);
                            intent.putExtras(bundle);
                            startActivityForResult(intent,requestCode2);
                        }
                    }

                }
            }
            layout();*/



        }else if (event.sensor.getType()==Sensor.TYPE_STEP_DETECTOR) {
            if (start_navigation) {
                stepCount += event.values[0];
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private  void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        values[0] = (float) Math.toDegrees(values[0]);
        //values[1] = (float) Math.toDegrees(values[1]);
        //values[2] = (float) Math.toDegrees(values[2]);

        String orien = "No";
        if(values[0] >= -5 && values[0] < 5){
            orien= "正北";
        } else if(values[0] >= 5 && values[0] < 85){
            orien= "東北";
        } else if(values[0] >= 85 && values[0] <95){
            orien= "正東";
        } else if(values[0] >= 95 && values[0] <175){
            orien= "東南";
        } else if((values[0] >= 175 && values[0] <= 180) || (values[0]) >= -180 && values[0] < -175){
            orien= "正南";
        } else if(values[0] >= -175 && values[0] <-95){
            orien= "西南";
        } else if(values[0] >= -95 && values[0] < -85){
            orien= "正西";
        } else if(values[0] >= -85 && values[0] <-5){
            orien= "西北";
        }
        Current_direction=values[0];
    }


    //最佳路徑演算法
    private void best_path(){
        int[] didilong = new int [100];
        int branch[] = new int [8];
        int x=0,y,b=0,didi,bp_start=end_int,bp_end=start_int,branch_c=0,f_min,min_up=0,min_down=0,min_right=0,min_left=0;
        double path_min=1000,min_dis_up,min_dis_down,min_dis_right,min_dis_left;
        do
        {
            if(get_turn.get(x)>=2)  //該點到下一點是岔路2表示兩條3表示三條.....
            {
                min_dis_up=1000;
                min_dis_down=1000;
                min_dis_right=1000;
                min_dis_left=1000;
                for (int i=x+1;i<get_x.size();i++)
                {
                    if (Math.round(get_x.get(x))==Math.round(get_x.get(i)))//同一個X
                    {
                        if (get_y.get(x)<get_y.get(i))//上面
                        {
                            if (min_dis_up>Math.sqrt(Math.pow((get_x.get(i) - get_x.get(x)), 2.0) +Math.pow((get_y.get(i) - get_y.get(x)), 2.0)))
                            {
                                min_dis_up=Math.sqrt(Math.pow((get_x.get(i) - get_x.get(x)), 2.0) +Math.pow((get_y.get(i) - get_y.get(x)), 2.0));
                                min_up=i;
                            }
                        }
                        else//下面
                        {
                            if (min_dis_down>Math.sqrt(Math.pow((get_x.get(i) - get_x.get(x)), 2.0) +Math.pow((get_y.get(i) - get_y.get(x)), 2.0)))
                            {
                                min_dis_down=Math.sqrt(Math.pow((get_x.get(i) - get_x.get(x)), 2.0) +Math.pow((get_y.get(i) - get_y.get(x)), 2.0));
                                min_down=i;
                            }
                        }
                    }else if(Math.round(get_y.get(x))==Math.round(get_y.get(i)))//同Y
                    {
                        if (get_x.get(x)<get_x.get(i))//  下一點在右邊
                        {
                            if (min_dis_right>Math.sqrt(Math.pow((get_x.get(i) - get_x.get(x)), 2.0) +Math.pow((get_y.get(i) - get_y.get(x)), 2.0)))//找最接近這點的座標 建關係 (通常是第一個)
                            {
                                min_dis_right=Math.sqrt(Math.pow((get_x.get(i) - get_x.get(x)), 2.0) +Math.pow((get_y.get(i) - get_y.get(x)), 2.0));
                                min_right=i;
                            }
                        }
                        else //左邊
                        {
                            if (min_dis_left>Math.sqrt(Math.pow((get_x.get(i) - get_x.get(x)), 2.0) +Math.pow((get_y.get(i) - get_y.get(x)), 2.0)))//找最接近這點的座標 建關係 (通常是第一個)
                            {
                                min_dis_left=Math.sqrt(Math.pow((get_x.get(i) - get_x.get(x)), 2.0) +Math.pow((get_y.get(i) - get_y.get(x)), 2.0));
                                min_left=i;
                            }
                        }
                    }
                }
                //-------up----
                if (min_dis_up!=1000)
                {
                    dir[x][min_up]=get_direction.get(0);
                    if(get_direction.get(0)+180>180)
                        dir[min_up][x]=get_direction.get(0)+180-360;
                    else
                        dir[min_up][x]=get_direction.get(0)+180;
                    dist[x][min_up]=min_dis_up;//兩點距離
                    dist[min_up][x]=dist[x][min_up];//雙向
                }
                //------down----
                if (min_dis_down!=1000)
                {
                    if (get_direction.get(0) + 180 > 180)
                        dir[x][min_down] = get_direction.get(0) + 180 - 360;
                    else
                        dir[x][min_down] = get_direction.get(0) + 180;
                    dir[min_down][x] = get_direction.get(0);
                    dist[x][min_down]=min_dis_down;//兩點距離
                    dist[min_down][x]=dist[x][min_down];//雙向
                }
                //------right-------
                if (min_dis_right!=1000) {
                    if (get_direction.get(0) > 90) {
                        dir[x][min_right] = get_direction.get(0) + 90 - 360;
                        dir[min_right][x] = get_direction.get(0) - 90;
                    } else if (get_direction.get(0) < -90) {
                        dir[x][min_right] = get_direction.get(0) + 90;
                        dir[min_right][x] = get_direction.get(0) - 90 + 360;
                    }
                    else
                    {
                        dir[x][min_right] = get_direction.get(0) + 90;
                        dir[min_right][x] = get_direction.get(0) - 90;
                    }
                    dist[x][min_right]=min_dis_right;//兩點距離
                    dist[min_right][x]=dist[x][min_right];//雙向
                }
                //-------left--------
                if (min_dis_left!=1000) {
                    if (get_direction.get(0) > 90) {
                        dir[x][min_left] = get_direction.get(0) - 90;
                        dir[min_left][x] = get_direction.get(0) + 90 - 360;
                    } else if (get_direction.get(0) < -90) {
                        dir[x][min_left] = get_direction.get(0) - 90 + 360;
                        dir[min_left][x] = get_direction.get(0) + 90;
                    }
                    else
                    {
                        dir[x][min_left] = get_direction.get(0) - 90;
                        dir[min_left][x] = get_direction.get(0) + 90;
                    }
                    dist[x][min_left]=min_dis_left;//兩點距離
                    dist[min_left][x]=dist[x][min_left];//雙向
                }
            }
            else if(get_turn.get(x)==0||get_turn.get(x)==-2)
            {
                y=x+1;
                dist[x][y]=Math.sqrt(Math.pow((get_x.get(y) - get_x.get(x)), 2.0) +Math.pow((get_y.get(y) - get_y.get(x)), 2.0));//兩點距離
                dist[y][x]=dist[x][y];//雙向
                dir[x][y]=get_direction.get(x);
                if (get_direction.get(x)>0)
                    dir[y][x]=get_direction.get(x)-180;
                else
                    dir[y][x]=get_direction.get(x)+180;
            }
            x++;
        }while(x<get_turn.size()-1);

        /* ------------------------------看數值可註解
        for(int i=0;i<get_turn.size();i++)
        {
            //results.setText(results.getText()+String.valueOf(Math.round(get_x.get(i)))+" "+String.valueOf(Math.round(get_y.get(i)))+"\n");
            for(int j=0;j<20;j++)
            {
                if (dir[i][j]!=1000)
                    results.setText(results.getText()+"dist["+i+"]["+j+"]="+Math.round(dist[i][j])+" "+dir[i][j]+" "+branch_c+"\n");
            }
        }*/


        if(up_down_floor)
        {
            f_min=-1;
            for (int i=0;i<get_turn.size();i++)
            {
                if ( get_turn.get(i)==-1||get_turn.get(i)==-2)
                {
                    if (path_min>Math.sqrt(Math.pow((get_x.get(i) - get_x.get(bp_end)), 2.0) +Math.pow((get_y.get(i) - get_y.get(bp_end)), 2.0)))
                    {
                        path_min=Math.sqrt(Math.pow((get_x.get(i) - get_x.get(bp_end)), 2.0) +Math.pow((get_y.get(i) - get_y.get(bp_end)), 2.0));
                        f_min=i;//最後得最近樓梯位置
                    }
                }
            }
            bp_start=f_min; //將終點設為樓梯
            get_path_floor=f_min;
        }




        if (bp_end!=bp_start)
        {
            double [] d = new double [100];
            double [][] dist_swap=new double[100][100];
            for(int i=0;i<dist.length;i++)
            {
                for(int j=0;j<dist.length;j++)
                {
                    dist_swap[i][j]=dist[i][j];
                }
            }
            double swap1;
            for(int i = 0;i<100;i++) {
                swap1 = dist_swap[bp_start][i];
                dist_swap[bp_start][i] = dist_swap[0][i];
                dist_swap[0][i] = swap1;

            }
            for(int i = 0;i<100;i++) {
                swap1 = dist_swap[i][bp_start];
                dist_swap[i][bp_start] = dist_swap[i][0];
                dist_swap[i][0] = swap1;

            }


            for(int i = 0 ;i<dist_swap.length;i++) {
                for(int j = 0;j<dist_swap.length;j++) {
                    if(dist_swap[i][j]==0) {
                        dist_swap[i][j] = 100000;
                    }
                }
            }
            boolean [] visit = new boolean [100];
            d[0] = 0;
            visit[0] = true;

            for(int k= 0; k < 100 - 1; k++)
            {
                int a = -1, bb = -1;
                double min = 10000;
                for(int i = 0; i < 100; i++)
                {
                    if (visit[i])
                    {
                        for(int j = 0; j < 100; j++)
                        {
                            if (!visit[j])
                            {
                                if (d[i] + dist_swap[i][j] < min)
                                {
                                    a = i;
                                    bb = j;
                                    min = d[i] +dist_swap[i][j];
                                }
                            }
                        }
                    }

                }
                if (a == -1 || bb == -1) break;
                d[bb] = min;
                visit[bb] = true;
                didilong[bb] = a;
            }
            didi = bp_end;
            path[path_c]=bp_end;
            path_c++;
            if(bp_end==0)
            {
                didi=bp_end+1;
                path[path_c]=didi;
                path_c++;
            }


            while(true) {
                if(didi == 0) break;
                if(didilong[didi]==0) {
                    path[path_c]=bp_start;
                    path_c++;
                }
                else {
                    path[path_c]=didilong[didi];
                    path_c++;
                }
                if(didi != 0)didi = didilong[didi];
                else break;
            }

        }else
        {
            path[path_c]=bp_start;
            path_c++;
            path[path_c]=bp_end;
            path_c++;
        }
        /*------------------------看數值可註解
        for (int i=0;i<path_c;i++)
        {
            if(path[i]!=-1)
                results.setText(results.getText()+String.valueOf(path[i])+" ");
        }
        results.setText(results.getText()+"\n");*/
    }

    private void best_path_floor(){
        int[] didilong = new int [100];
        int branch[] = new int [8];
        int x=0,y,b=0,didi,bp_start=end_int,bp_end=start_int,branch_c=1,min_up=0,min_down=0,min_right=0,min_left=0;
        double path_min=1000,min_dis_up,min_dis_down,min_dis_right,min_dis_left;
        boolean first_end_point=false;
        for (int i=0;i<get_x_2.size();i++)
        {
            if ( (get_x_2.get(i).equals(get_x.get(path[path_c-1])))&&(get_y_2.get(i).equals(get_y.get(path[path_c-1]))) )
            {
                bp_end=i;
                break;
            }
        }
        do
        {
            if(get_turn_2.get(x)>=2)  //該點到下一點是岔路2表示兩條3表示三條.....
            {
                min_dis_up=1000;
                min_dis_down=1000;
                min_dis_right=1000;
                min_dis_left=1000;
                for (int i=x+1;i<get_x_2.size();i++)
                {
                    if (Math.round(get_x_2.get(x))==Math.round(get_x_2.get(i)))//同一個X
                    {
                        if (get_y_2.get(x)<get_y_2.get(i))//上面
                        {
                            if (min_dis_up>Math.sqrt(Math.pow((get_x_2.get(i) - get_x_2.get(x)), 2.0) +Math.pow((get_y_2.get(i) - get_y_2.get(x)), 2.0)))
                            {
                                min_dis_up=Math.sqrt(Math.pow((get_x_2.get(i) - get_x_2.get(x)), 2.0) +Math.pow((get_y.get(i) - get_y_2.get(x)), 2.0));
                                min_up=i;
                            }
                        }
                        else//下面
                        {
                            if (min_dis_down>Math.sqrt(Math.pow((get_x_2.get(i) - get_x_2.get(x)), 2.0) +Math.pow((get_y_2.get(i) - get_y_2.get(x)), 2.0)))
                            {
                                min_dis_down=Math.sqrt(Math.pow((get_x_2.get(i) - get_x_2.get(x)), 2.0) +Math.pow((get_y_2.get(i) - get_y_2.get(x)), 2.0));
                                min_down=i;
                            }
                        }
                    }else if(Math.round(get_y_2.get(x))==Math.round(get_y_2.get(i)))//同Y
                    {
                        if (get_x_2.get(x)<get_x_2.get(i))//  下一點在右邊
                        {
                            if (min_dis_right>Math.sqrt(Math.pow((get_x_2.get(i) - get_x_2.get(x)), 2.0) +Math.pow((get_y_2.get(i) - get_y_2.get(x)), 2.0)))//找最接近這點的座標 建關係 (通常是第一個)
                            {
                                min_dis_right=Math.sqrt(Math.pow((get_x_2.get(i) - get_x_2.get(x)), 2.0) +Math.pow((get_y_2.get(i) - get_y_2.get(x)), 2.0));
                                min_right=i;
                            }
                        }
                        else //左邊
                        {
                            if (min_dis_left>Math.sqrt(Math.pow((get_x_2.get(i) - get_x_2.get(x)), 2.0) +Math.pow((get_y_2.get(i) - get_y_2.get(x)), 2.0)))//找最接近這點的座標 建關係 (通常是第一個)
                            {
                                min_dis_left=Math.sqrt(Math.pow((get_x_2.get(i) - get_x_2.get(x)), 2.0) +Math.pow((get_y_2.get(i) - get_y_2.get(x)), 2.0));
                                min_left=i;
                            }
                        }
                    }
                }
                //-------up----
                if (min_dis_up!=1000)
                {
                    dir2[x][min_up]=get_direction_2.get(0);
                    if(get_direction_2.get(0)+180>180)
                        dir2[min_up][x]=get_direction_2.get(0)+180-360;
                    else
                        dir2[min_up][x]=get_direction_2.get(0)+180;
                    dist2[x][min_up]=min_dis_up;//兩點距離
                    dist2[min_up][x]=dist2[x][min_up];//雙向
                }
                //------down----
                if (min_dis_down!=1000)
                {
                    if (get_direction.get(0) + 180 > 180)
                        dir2[x][min_down] = get_direction_2.get(0) + 180 - 360;
                    else
                        dir2[x][min_down] = get_direction_2.get(0) + 180;
                    dir2[min_down][x] = get_direction_2.get(0);
                    dist2[x][min_down]=min_dis_down;//兩點距離
                    dist2[min_down][x]=dist2[x][min_down];//雙向
                }
                //------right-------
                if (min_dis_right!=1000) {
                    if (get_direction_2.get(0) > 90) {
                        dir2[x][min_right] = get_direction_2.get(0) + 90 - 360;
                        dir2[min_right][x] = get_direction_2.get(0) - 90;
                    } else if (get_direction_2.get(0) < -90) {
                        dir[x][min_right] = get_direction_2.get(0) + 90;
                        dir[min_right][x] = get_direction_2.get(0) - 90 + 360;
                    }
                    else
                    {
                        dir2[x][min_right] = get_direction_2.get(0) + 90;
                        dir2[min_right][x] = get_direction_2.get(0) - 90;
                    }
                    dist2[x][min_right]=min_dis_right;//兩點距離
                    dist2[min_right][x]=dist2[x][min_right];//雙向
                }
                //-------left--------
                if (min_dis_left!=1000) {
                    if (get_direction_2.get(0) > 90) {
                        dir2[x][min_left] = get_direction_2.get(0) - 90;
                        dir2[min_left][x] = get_direction_2.get(0) + 90 - 360;
                    } else if (get_direction_2.get(0) < -90) {
                        dir2[x][min_left] = get_direction_2.get(0) - 90 + 360;
                        dir2[min_left][x] = get_direction_2.get(0) + 90;
                    }
                    else
                    {
                        dir2[x][min_left] = get_direction_2.get(0) - 90;
                        dir2[min_left][x] = get_direction_2.get(0) + 90;
                    }
                    dist2[x][min_left]=min_dis_left;//兩點距離
                    dist2[min_left][x]=dist2[x][min_left];//雙向
                }
            }
            else if(get_turn_2.get(x)==0||get_turn_2.get(x)==-2)
            {
                y=x+1;
                dist2[x][y]=Math.sqrt(Math.pow((get_x_2.get(y) - get_x_2.get(x)), 2.0) +Math.pow((get_y_2.get(y) - get_y_2.get(x)), 2.0));//兩點距離
                dist2[y][x]=dist[x][y];//雙向
                dir2[x][y]=get_direction_2.get(x);
                if (get_direction_2.get(x)>0)
                    dir2[y][x]=get_direction_2.get(x)-180;
                else
                    dir2[y][x]=get_direction_2.get(x)+180;
            }
            x++;
        }while(x<get_turn_2.size()-1);
        /* ------------------------------看數值可註解
        for(int i=0;i<11;i++)
        {
            for(int j=0;j<11;j++)
            {
                if (dir[i][j]!=1000)
                    results.setText(results.getText()+"dist["+i+"]["+j+"]="+dist2[i][j]+" "+dir2[i][j]+"\n");
            }
        }
        */
        double [] d = new double [100];
        double [][] dist_swap=new double[100][100];
        for(int i=0;i<dist2.length;i++)
        {
            for(int j=0;j<dist2.length;j++)
            {
                dist_swap[i][j]=dist2[i][j];
            }
        }
        double swap1;
        for(int i = 0;i<100;i++) {
            swap1 = dist_swap[bp_start][i];
            dist_swap[bp_start][i] = dist_swap[0][i];
            dist_swap[0][i] = swap1;

        }
        for(int i = 0;i<100;i++) {
            swap1 = dist_swap[i][bp_start];
            dist_swap[i][bp_start] = dist_swap[i][0];
            dist_swap[i][0] = swap1;

        }
        for(int i = 0 ;i<dist_swap.length;i++) {
            for(int j = 0;j<dist_swap.length;j++) {
                if(dist_swap[i][j]==0) {
                    dist_swap[i][j] = 100000;
                }
            }
        }
        boolean [] visit = new boolean [100];
        d[0] = 0;
        visit[0] = true;

        for(int k= 0; k < 100 - 1; k++)
        {
            int a = -1, bb = -1;
            double min = 10000;
            for(int i = 0; i < 100; i++)
            {
                if (visit[i])
                {
                    for(int j = 0; j < 100; j++)
                    {
                        if (!visit[j])
                        {
                            if (d[i] + dist_swap[i][j] < min)
                            {
                                a = i;
                                bb = j;
                                min = d[i] +dist_swap[i][j];
                            }
                        }
                    }
                }

            }
            if (a == -1 || bb == -1) break;
            d[bb] = min;
            visit[bb] = true;
            didilong[bb] = a;
        }
        didi = bp_end;
        path2[path_c2]=bp_end;
        path_c2++;
        if(bp_end==0)
        {
            didi=bp_end+1;
            path2[path_c2]=didi;
            path_c2++;
        }


        while(true) {
            if(didi == 0) break;
            if(didilong[didi]==0) {
                path2[path_c2]=bp_start;
                path_c2++;
            }
            else {
                if (bp_end!=bp_start)
                {
                    path2[path_c2]=didilong[didi];
                    path_c2++;
                }
            }
            if(didi != 0)didi = didilong[didi];
            else break;
        }


        //------------------------看數值可註解
        for (int i=0;i<path_c2;i++)
        {
            if(path2[i]!=-1)
                results.setText(results.getText()+String.valueOf(path2[i])+" ");
        }
        //------------------------------------
        results.setText(results.getText()+"\n");
    }


    public void drawmap()
    {

        if(!start_navigation)
        {
            LinearLayout layout=findViewById(R.id.d_map);
            //layout.setBackgroundColor(Color.BLACK);
            bDrawl=new  Drawl(this);
            bDrawl.draw_x(get_x);
            bDrawl.draw_y(get_y);
            bDrawl.draw_turn(get_turn);
            bDrawl.draw_path(path);
            bDrawl.draw_path_c(path_c);
            bDrawl.draw_name(get_name);
            bDrawl.draw_dir(dir);
            //  bDrawl.draw_stepdis(Float.valueOf(String.valueOf(stepDistance)));
            // bDrawl.draw_step_c(stepCount);
            //   bDrawl.draw_step_cb(getStepCount_before);
            //  bDrawl.draw_index(index);

           /* for (int i=0;i<3;i++)
            {
                Button newbtn= new Button(this);
                newbtn.setBackground(this.getResources().getDrawable(R.drawable.round_button));
                newbtn.setText(String.valueOf(i));
                newbtn.setX(50+i*10);
                newbtn.setY(50);
                layout.addView(newbtn,100,100);
            }*/
            layout.addView(bDrawl);


        }
    }

    public  void GetStepCount()
    {
        for (int i=0;i<path_c;i++)
        {
            if (i!=path_c-1)
                totalstep=totalstep+dist[path[i]][path[i+1]]/stepDistance;
        }
    }

    //QRcode 返回  & arrive返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        speechRecognitionAction(requestCode, resultCode, data);//語音辨識
        switch (resultCode)
        {
            case 1:
                msg = data.getExtras().getStringArray("location");
                qr=data.getExtras().getBoolean("QR_boolean");
                //地點
                Choice_place1_btn.setText(msg[0]);
                getPlace_str1=msg[0];
                change_start_place(getPlace_str1);
                //樓層
                floor1_adapter = new ArrayAdapter<String>(navigation.this,android.R.layout.simple_list_item_1,get_start_floor);
                Choice_floor1_btn.setText(msg[1]+"F");
                get_floor1 = Integer.valueOf(msg[1]);
                change_start_floor(get_floor1);
                //位置
                list_start_adapter = new ArrayAdapter<String>(navigation.this,android.R.layout.simple_list_item_1,get_name);
                Choice_start_btn.setText(msg[2]);
                break;
            case 2:
                navigation.this.recreate();
                // spinner_start.setSelection(0);
                // spinner_end.setSelection(0);
                checkin_bl=false;
                start_navigation=false;
                start_dir=false;
                break;
            default:
                break;
        }


    }
    //語音辨識
    public void speechRecognitionAction(int requestCode, int resultCode, Intent data) {

        if (requestCode == requestCode0&&resultCode == RESULT_OK) {

            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String str=result.get(0);
            String[] array_voice=str.split("往");
            boolean isError0=true,isError1=true;
            String error_str="";
            //判斷有沒有講錯語法 (Ex: XX到XX)
            if (array_voice.length!=2){
                Toast.makeText(this, "Error - Please try again!",Toast.LENGTH_SHORT).show();
            }else {
                for (int i = 0; i < get_name.size(); i++){
                    if (array_voice[0].equals(get_name.get(i))) {
                        //   spinner_start.setSelection(i);
                        isError0 = false;
                    }
                }
                for (int j=0;j<get_name.size();j++){
                    if (array_voice[1].equals(get_name.get(j))) {
                        //   spinner_end.setSelection(j);
                        isError1 = false;
                    }
                }
                // 判斷有沒有講錯la
                if (isError0 && isError1){
                    error_str=array_voice[0]+" and "+array_voice[1];
                    Toast.makeText(this, "Cannot find "+error_str,Toast.LENGTH_SHORT).show();
                }else if(isError0 && !isError1){
                    error_str=array_voice[0];
                    Toast.makeText(this, "Cannot find "+error_str,Toast.LENGTH_SHORT).show();
                }else if(!isError0 && isError1){
                    error_str=array_voice[1];
                    Toast.makeText(this, "Cannot find "+error_str,Toast.LENGTH_SHORT).show();
                }
            }

            results.setText(str);
        }
    }


}
