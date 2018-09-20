package com.example.allen.gait_navigation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class GetPoint extends AppCompatActivity implements SensorEventListener{
    //sensor
    private SensorManager sensorManager;
    private Sensor magneticSensor;
    private Sensor accelerometerSensor;
    private Sensor gyroscopeSensor;
    private Sensor stepDetector;
    //button textview edittext timer checkbox
    public TextView gyroscope_view,orien_view,step_view;
    public Button mAdd_btn,mRemove_btn,timer_start_btn,timer_pause_btn,get_distance_btn;
    public EditText et_Name,Step_length_edit,et_place;
    public Timer timer;//計時器
    public CheckBox is_turn_floor,is_turn_branch,is_turn_end;
    //Firebase 用
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    String getPlace,getFloor,getName,get_X,get_Y,dir;
    //下拉式選單
    Spinner floor_sp;//,building_sp;
    String[] item_name = {"工程一館","工程二館","工程三館","工程四館","工程五館"};
    String[] item_name2 = {"1","2","3","4","5","6","7","8","9","10"};

    ArrayAdapter<String> spinner_adapter,spinner_adapter2;
    //變數
    long place_count;
    private static final float NS2S = 1.0f / 1000000000.0f;
    float[] angle=new float[3];
    float[] position_angle=new float[3];
    float[] step=new float[100];
    float[] accelerometerValues = new float[3];   //加速度xyz
    float[] magneticFieldValues = new float[3];   //陀螺儀xyz
    float theta,stepcount,distance,first_direction,direction,timestamp;
    int p_x=0,p_y=0,count,count2,turn,count_point=0;
    double point_x=0,point_y=0;
    boolean first=true,begin=true,start=false,first_get_place=true;
    String meter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//不休眠
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_point);
        // textview edittext
        gyroscope_view=findViewById(R.id.gyroscope_view);
        orien_view=findViewById(R.id.orien_view);
        step_view=findViewById(R.id.step_view);
        Step_length_edit=findViewById(R.id.Step_length_edit);
        et_place=findViewById(R.id.et_place);
        // button checkbox
        timer_start_btn=findViewById(R.id.timer_start_btn);
        timer_pause_btn = findViewById(R.id.timer_pause_btn);
        get_distance_btn=findViewById(R.id.get_distance_btn);
        is_turn_floor=findViewById(R.id.is_turn_floor);
        is_turn_branch=findViewById(R.id.is_turn_branch);
        is_turn_end=findViewById(R.id.is_turn_end);
        //Firebase
        mAdd_btn = findViewById(R.id.mAdd_btn);
        mRemove_btn = findViewById(R.id.mRemove_btn);
        et_Name = findViewById(R.id.et_Name);
       // building_sp = findViewById(R.id.building_sp);
        floor_sp = findViewById(R.id.floor_sp);
        //取得步長
        get_distance_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                getdistance();
            }
        });
        //輸出座標
        final Handler handler=new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                layout();   //輸出座標
            }

        };
        //一秒延遲後 每兩秒抓一次座標
        timer_start_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                start=true;
                TimerTask task=new TimerTask() {
                    @Override
                    public void run() {
                        coordinate();   //計算座標
                        Message m1=new Message();
                        handler.sendMessage(m1);
                    }
                };
                timer=new Timer(true);
                timer.schedule(task,1000,1000);
            }
        });
        //暫停timer
        timer_pause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
            }
        });
        //下拉式選單
        spinner_adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,item_name);
       /* building_sp.setAdapter(spinner_adapter);
        building_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/
        spinner_adapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,item_name2);
        floor_sp.setAdapter(spinner_adapter2);
        floor_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getFloor = item_name2[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //傳資料庫
        mAdd_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                getxy();
            }
        });
        //移除資料庫上的資料
        mRemove_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getName = String.valueOf(et_Name.getText());
                DatabaseReference myRef_remove = database.getReference(getPlace).child(getName);
                myRef_remove.removeValue();
            }
        });

        is_turn_floor.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) turn=-1;else turn=0;
            }
        });

        is_turn_branch.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) turn=2;else turn=0;
            }
        });
        is_turn_end.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) turn=1;else turn=0;
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magneticSensor =sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor =sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor =sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        stepDetector=sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        //注?陀螺??感器，并?定?感器向?用中?出的???隔?型是SensorManager.SENSOR_DELAY_GAME(20000微秒)
        //SensorManager.SENSOR_DELAY_FASTEST(0微秒)：最快。最低延?，一般不是特?敏感的?理不推荐使用，?模式可能在成手机?力大量消耗，由于??的?原始?据，?法不?理好?影?游???和UI的性能
        //SensorManager.SENSOR_DELAY_GAME(20000微秒)：游?。游?延?，一般?大多?的??性?高的游?都是用???
        //SensorManager.SENSOR_DELAY_NORMAL(200000微秒):普通。?准延?，?于一般的益智?或EASY??的游?可以使用，但?低的采?率可能?一些???游?有跳??象
        //SensorManager.SENSOR_DELAY_UI(60000微秒):用?界面。一般?于屏幕方向自?旋?使用，相??省?能和???理，一般游???中不使用
        sensorManager.registerListener(this,gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,magneticSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,stepDetector,SensorManager.SENSOR_DELAY_GAME);



        DatabaseReference myplace=database.getReference("Map").child("Location");
        myplace.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (first_get_place)
                {
                    place_count=dataSnapshot.getChildrenCount();
                    first_get_place=false;
                }


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



    }


    //坐??都是手机?左?到右?的水平方向?x?正向，?手机下部到上部?y?正向，垂直于手机屏幕向上?z?正向(不一定)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // 三?坐??方向上的?磁?度，?位是微特拉斯(micro-Tesla)，用uT表示，也可以是高斯(Gauss),1Tesla=10000Gauss
            magneticFieldValues = event.values.clone();
            calculateOrientation();
            // 手机的磁?感?器?外部采集?据的???隔是10000微秒
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            //? x、y、z ?的正向位置?看?于原始方位的??，如果??逆??旋?，??收到正值；否?，??值
            if(timestamp != 0)
            {
                // 得到?次??到手机旋?的??差（?秒），并?其?化?秒
                final float dT = (event.timestamp -timestamp) * NS2S;
                // ?手机在各??上的旋?角度相加，即可得到?前位置相?于初始位置的旋?弧度
                angle[0] += event.values[0] * dT;
                angle[1] += event.values[1] * dT;
                angle[2] += event.values[2] * dT;
                // ?弧度?化?角度
                theta= (float) Math.toDegrees(angle[2]);
                gyroscope_view.setText("(左正右負)Z :"+String.valueOf(theta));
            }
            //??前???值?timestamp
            timestamp = event.timestamp;
        } else if (event.sensor.getType()==Sensor.TYPE_STEP_DETECTOR) {
            if (start)
                stepcount+=event.values[0];
            step_view.setText(String.valueOf(stepcount));
        }

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //TODO Auto-generated method stub
    }
    @Override
    protected void onPause() {
        //TODO Auto-generated method stub
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    //計算方位
    private  void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);

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
        position_angle[0]=values[0];
        orien_view.setText(orien +":"+ values[0]);
    }
    //取得步長
    private void getdistance() {
        meter=String.valueOf(Step_length_edit.getText());
        Toast.makeText(GetPoint.this,"步長已輸入",Toast.LENGTH_SHORT).show();
    }
    //計算並取得座標
    private void coordinate() {
        if (first) {
            first_direction=theta;                     //初始面向方向角度為0  (左右轉角度) first_direction=theta
            direction=theta;
            step[count]=stepcount;                   //初始步數
            distance=0;                              //初始距離
            count++;
            first=false;
        } else {
            direction = theta;
            step[count]=stepcount;
            if (count==0)
            {
                distance=Float.valueOf(meter) *Float.valueOf(step[count]-step[99]);    //步長*步數
            }
            else
            {
                distance=Float.valueOf(meter) *Float.valueOf(step[count]-step[count-1]);    //步長*步數
            }

            while (direction < 0)    //繞圈調整
                direction += 360;

            while (direction > 360)
                direction -= 360;

            //座標 給定象限
            if ((direction >= 0 && direction < 22.5) || (direction >= 337.5)) {
                p_x = 0;
                p_y = 1;
            } else if (direction >= 22.5 && direction < 67.5) {
                p_x = -1;
                p_y = 1;
            } else if (direction >= 67.5 && direction < 112.5) {
                p_x = -1;
                p_y = 0;
            } else if (direction >= 112.5 && direction < 160) {
                p_x = -1;
                p_y = -1;
            } else if (direction >= 160 && direction < 202.5) {
                p_x = 0;
                p_y = -1;
            } else if (direction >= 202.5 && direction < 247.5) {
                p_x = 1;
                p_y = -1;
            } else if (direction >= 247.5 && direction < 292.5) {
                p_x = 1;
                p_y = 0;
            } else if (direction >= 292.5 && direction < 337.5) {
                p_x = 1;
                p_y = 1;
            }

            //直走 左轉 右轉(正方向前進)
            if (((direction - first_direction) >= 0 && (direction - first_direction) < 22.5) || ((direction - first_direction) >= 337.5)) {
                point_x = point_x + distance * Math.abs(Math.sin(0)) * p_x;
                point_y = point_y + distance * Math.abs(Math.cos(0)) * p_y;
            } else if ((direction - first_direction) >= 67.5 && (direction - first_direction) < 112.5) {
                point_x = point_x + distance * Math.abs(Math.sin(90 * Math.PI / 180)) * p_x;
                point_y = point_y + distance * Math.abs(Math.cos(90 * Math.PI / 180)) * p_y;
            } else if ((direction - first_direction) >= 160 && (direction - first_direction) < 202.5) {
                point_x = point_x + distance * Math.abs(Math.sin(0 * Math.PI / 180)) * p_x;
                point_y = point_y + distance * Math.abs(Math.cos(0 * Math.PI / 180)) * p_y;
            } else if ((direction - first_direction) >= 247.5 && (direction - first_direction) < 292.5) {
                point_x = point_x + distance * Math.abs(Math.sin(90 * Math.PI / 180)) * p_x;
                point_y = point_y + distance * Math.abs(Math.cos(90 * Math.PI / 180)) * p_y;
            } else {
                //原座標 + 路徑長*cos or sin 角度*象限
                point_x +=  distance * Math.abs(Math.sin(Math.abs(direction - first_direction) * Math.PI / 180)) * p_x;
                point_y +=  distance * Math.abs(Math.cos(Math.abs(direction - first_direction) * Math.PI / 180)) * p_y;
            }
            count++;
            if (count == 100)
                count=0;
        }
    }
    //Firebase
    private void   getxy() {
        getPlace=et_place.getText().toString();
        getName = String.valueOf(et_Name.getText());
        get_X = String.format("%.2f",point_x);
        get_Y = String.format("%.2f",point_y);
        dir=String.format("%.0f", position_angle[0]);
        DatabaseReference myRef_Name = database.getReference("Map").child(getPlace).child(getFloor).child(String.valueOf(count_point)).child("name");
        DatabaseReference myRef_X = database.getReference("Map").child(getPlace).child(getFloor).child(String.valueOf(count_point)).child("X");
        DatabaseReference myRef_Y = database.getReference("Map").child(getPlace).child(getFloor).child(String.valueOf(count_point)).child("Y");
        DatabaseReference mydir = database.getReference("Map").child(getPlace).child(getFloor).child(String.valueOf(count_point)).child("direction");
        DatabaseReference myturn = database.getReference("Map").child(getPlace).child(getFloor).child(String.valueOf(count_point)).child("turn");
        DatabaseReference mylike = database.getReference("Map").child(getPlace).child(getFloor).child(String.valueOf(count_point)).child("like");

        myRef_Name.setValue(getName);
        myRef_X.setValue(get_X);
        myRef_Y.setValue(get_Y);
        mydir.setValue(dir);
        myturn.setValue(turn);
        mylike.setValue(0);

        DatabaseReference myplace = database.getReference("Map").child("Location").child(String.valueOf(place_count));
        myplace.setValue(getPlace);
        count_point++;

    }



    //座標輸出
    private void layout() {

        TextView coordinate =new TextView(this);
        LinearLayout ll=findViewById(R.id.viewObj);
        TextView tv6 =new TextView(this);
        TextView tv12 =new TextView(this);

        if (begin) {
            count2=0;
            tv6.setText(orien_view.getText());
            tv12.setText(gyroscope_view.getText() + "\n");
            coordinate.setText("座標 " +String.valueOf(count2+1)+"=(" + String.format("%.2f", point_x) + "," + String.format("%.2f", point_y) + ") ");
            ll.addView(tv6);
            ll.addView(tv12);
            ll.addView(coordinate);
            begin=false;
            count2++;
        }
        else
        {
            //一直輸出座標
            //座標換紅色(站著不動時)
            if(distance==0)
            {
                coordinate.setTextColor(Color.RED);
                coordinate.setText("座標 " +String.valueOf(count2+1) + "=(" + String.format("%.2f", point_x) + "," + String.format("%.2f", point_y) + ")");
                ll.addView(coordinate);
            }
            else
            {
                coordinate.setTextColor(Color.BLUE);
                coordinate.setText("座標 " +String.valueOf(count2+1) + "=(" + String.format("%.2f", point_x) + "," + String.format("%.2f", point_y) + ")");
                ll.addView(coordinate);
            }
            count2++;
            //永遠顯示最新一筆資料
            final ScrollView getScrollView = findViewById(R.id.scrollView);
            getScrollView.post(new Runnable() {
                @Override
                public void run() {
                    getScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
            if (count2 == 100)
                count2=0;
        }
    }
    //感測器停止
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        Toast.makeText(this, "Unregister all sensor", Toast.LENGTH_LONG).show();
    }
}
