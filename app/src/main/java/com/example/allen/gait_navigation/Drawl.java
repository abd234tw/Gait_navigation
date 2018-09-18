package com.example.allen.gait_navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;

public class Drawl extends View {
    private Paint paint,paint2,paint3,paint4,paint5,paint6;//聲明畫筆
     float init=-200;
     float width,height;
     int pen=10,c,path_c,length=15;
     float d_x,d_y;
    ArrayList<Float> x=new ArrayList<>();
    ArrayList<Float> y=new ArrayList<>();
    ArrayList<Integer> turn=new ArrayList<>();
    ArrayList<Integer> branch=new ArrayList<>();
    ArrayList<String>name=new ArrayList<>();
    int[] path;




    public Drawl(Context context) {
        super(context);
        paint=new Paint(Paint.DITHER_FLAG);//創建一個畫筆    地圖
        paint.setStyle(Paint.Style.STROKE);//設置非填充
        paint.setStrokeWidth(pen*7);//筆寬
        paint.setColor(getResources().getColor(R.color.colorgray));//設置為灰色筆
        paint.setAntiAlias(true);//鋸齒不顯示

        paint2=new Paint(Paint.DITHER_FLAG);//創建一個畫筆   路徑
        paint2.setStyle(Paint.Style.STROKE);//設置非填充
        paint2.setStrokeWidth(pen/2);//筆寬
        paint2.setColor(getResources().getColor(R.color.colorlightblue));//設置為淺藍筆
        paint2.setAntiAlias(true);//鋸齒不顯示

        paint3=new Paint(Paint.DITHER_FLAG);//創建一個畫筆   起點 終點
        paint3.setStrokeWidth(pen);//筆寬
        paint3.setColor(getResources().getColor(R.color.colororange));//設置為橘色筆
        paint3.setAntiAlias(true);//鋸齒不顯示


        paint4=new Paint(Paint.UNDERLINE_TEXT_FLAG);//創建一個畫筆   起點終點文字
        paint4.setColor(Color.WHITE);//設置為白筆
        paint4.setAntiAlias(true);//鋸齒不顯示
        paint4.setTextSize(40);//字體大小

        paint5=new Paint(Paint.DITHER_FLAG);//創建一個畫筆   其他點
        paint5.setStrokeWidth(pen);//筆寬
        paint5.setColor(getResources().getColor(R.color.colornavyblue));//設置為深藍筆
        paint5.setAntiAlias(true);//鋸齒不顯示

        paint6=new Paint(Paint.DEV_KERN_TEXT_FLAG);//創建一個畫筆   其他點文字
        paint6.setColor(Color.WHITE);//設置為白筆
        paint6.setAntiAlias(true);//鋸齒不顯示
        paint6.setTextSize(25);//字體大小


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
    public void draw_name(ArrayList<String> dn){name=dn;}
    //畫圖
    @Override
    protected void onDraw(Canvas canvas ) {
        super.onDraw(canvas);
            width=canvas.getWidth()+init;
            height=canvas.getHeight()+init;
            int branch_c=branch.size();
            c=0;
            do                              //畫地圖
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
                    d_x=(x.get(c+1)-x.get(branch.get(branch_c-1)))*length;
                    d_y=(y.get(c+1)-y.get(branch.get(branch_c-1)))*length;
                    canvas.drawLine(width,height,width+d_x,height-d_y,paint);
                    branch_c--;
                }
                c++;
                if (c==x.size()-1)
                    c++;
            }while(c<x.size());


            width=canvas.getWidth()+init+x.get(path[0])*length;
            height=canvas.getHeight()+init-y.get(path[0])*length;

            for (int i=1;i<path_c;i++) {
                d_x = (x.get(path[i]) - x.get(path[i - 1])) * length;
                d_y = (y.get(path[i]) - y.get(path[i - 1])) * length;
                if (i == 1) {
                    canvas.drawCircle(width, height, pen * 3, paint3);//起點
                    canvas.drawText(name.get(path[i - 1]), width, height, paint4);
                    canvas.drawCircle(width + d_x, height - d_y, pen * 2, paint5);//其他點
                    canvas.drawText(name.get(path[i]), width + d_x, height - d_y, paint6);
                } else if (i == path_c - 1) {
                    canvas.drawCircle(width + d_x, height - d_y, pen * 3, paint3);//終點
                    canvas.drawText(name.get(path[i]), width + d_x, height - d_y, paint4);
                } else {
                    canvas.drawCircle(width + d_x, height - d_y, pen * 2, paint5);//其他點
                    canvas.drawText(name.get(path[i]), width + d_x, height - d_y, paint6);
                }
                canvas.drawLine(width, height, width + d_x, height - d_y, paint2);//路徑
                width = width + d_x;
                height = height - d_y;

            }
    }




}



