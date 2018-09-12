package com.example.allen.gait_navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class Drawl extends View {
    private Paint paint,paint2;//聲明畫筆
     float init=-200;
     float width,height;
     int pen=10,c=0,path_c,length=10;
     float d_x,d_y;
    ArrayList<Float> x=new ArrayList<>();
    ArrayList<Float> y=new ArrayList<>();
    ArrayList<Integer> turn=new ArrayList<>();
    ArrayList<Integer> branch=new ArrayList<>();
    int[] path;


    public Drawl(Context context) {
        super(context);
        paint=new Paint(Paint.DITHER_FLAG);//創建一個畫筆
        paint.setStyle(Paint.Style.STROKE);//設置非填充
        paint.setStrokeWidth(pen);//筆寬5圖元
        paint.setColor(Color.RED);//設置為紅筆
        paint.setAntiAlias(true);//鋸齒不顯示
        paint2=new Paint(Paint.DITHER_FLAG);//創建一個畫筆
        paint2.setStyle(Paint.Style.STROKE);//設置非填充
        paint2.setStrokeWidth(pen/2);//筆寬5圖元
        paint2.setColor(Color.BLUE);//設置為紅筆
        paint2.setAntiAlias(true);//鋸齒不顯示
    }


    //設定 x y
    public void draw_x(ArrayList<Float> dx)
    {
        x=dx;
    }
    public void draw_y(ArrayList<Float> dy)
    {
        y=dy;
    }
    public void draw_branch(ArrayList<Integer> dt)
    {
        branch=dt;
    }
    public void draw_turn(ArrayList<Integer> dt)
    {
        turn=dt;
    }
    public void draw_path(int[] dp)
    {
        path=dp;
    }
    public void draw_path_c(int dpc){path_c=dpc;}

    //畫圖
    @Override
    protected void onDraw(Canvas canvas ) {
        super.onDraw(canvas);
        width=canvas.getWidth()+init;
        height=canvas.getHeight()+init;
        int branch_c=branch.size();
        do
        {
            if (turn.get(c)==0||turn.get(c)>=2||turn.get(c)==-2)
            {
                d_x=(x.get(c+1)-x.get(c))*length;
                d_y=(y.get(c+1)-y.get(c))*length;
                canvas.drawLine(width,height,width+d_x,height-d_y,paint);
                width=width+d_x;
                height=height-d_y;
            }
            else
            {
                width=canvas.getWidth()+init+x.get(branch.get(branch_c-1))*length;
                height=canvas.getHeight()+init-y.get(branch.get(branch_c-1))*length;
//                d_x=(x.get(c+1)-x.get(branch.get(branch_c-1)))*length;
//                d_y=(y.get(c+1)-y.get(branch.get(branch_c-1)))*length;
                canvas.drawLine(width,height,width+d_x,height-d_y,paint);
                branch_c--;
            }
               c++;
        }while(c<x.size()-1);


        width=canvas.getWidth()+init+x.get(path[0])*length;
        height=canvas.getHeight()+init-y.get(path[0])*length;

        for (int i=1;i<path_c;i++)
        {
            if(path[i]!=-1)
            {
                d_x=(x.get(path[i])-x.get(path[i-1]))*length;
                d_y=(y.get(path[i])-y.get(path[i-1]))*length;
                canvas.drawLine(width,height,width+d_x,height-d_y,paint2);
                width=width+d_x;
                height=height-d_y;
            }
        }



    }


}



