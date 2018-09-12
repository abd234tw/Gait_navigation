package com.example.allen.gait_navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;

public class DrawPath extends View {
    private Paint paint;//聲明畫筆
    float init=-250;
    float width,height;
    int pen=10,path_c,length=10;
    float d_x,d_y;
    ArrayList<Float> x=new ArrayList<>();
    ArrayList<Float> y=new ArrayList<>();
    int[] path;

    public DrawPath(Context context) {
        super(context);
        paint=new Paint(Paint.DITHER_FLAG);//創建一個畫筆
        paint.setStyle(Paint.Style.STROKE);//設置非填充
        paint.setStrokeWidth(pen);//筆寬5圖元
        paint.setColor(Color.BLUE);//設置為藍筆
        paint.setAntiAlias(true);//鋸齒不顯示

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
    public void draw_path(int[] dp)
    {
        path=dp;
    }
    public void draw_path_c(int dpc){path_c=dpc;}
    //畫圖
    @Override
    protected void onDraw(Canvas canvas ) {
        super.onDraw(canvas);

            width=canvas.getWidth()+init+x.get(path[0])*length;
            height=canvas.getHeight()+init-y.get(path[0])*length;

            for (int i=1;i<path_c;i++)
            {
                if(path[i]!=-1)
                {
                    d_x=(x.get(path[i])-x.get(path[i-1]))*length;
                    d_y=(y.get(path[i])-y.get(path[i-1]))*length;
                    canvas.drawLine(width,height,width+d_x,height-d_y,paint);
                    width=width+d_x;
                    height=height-d_y;
                }
            }

    }
}
