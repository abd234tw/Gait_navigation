package com.example.allen.gait_navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Currency;

import static android.media.session.PlaybackState.STATE_NONE;

public class Drawl extends SurfaceView implements SurfaceHolder.Callback/*,View.OnTouchListener*/{
    private Paint paint,paint2,paint3,paint4,paint5,paint6,paint7;//聲明畫筆
     float init_x=-200,init_y=-200;
     float width,height;
     int pen=10,c,path_c,length=15;
     float d_x,d_y;
    ArrayList<Float> x=new ArrayList<>();
    ArrayList<Float> y=new ArrayList<>();
    ArrayList<Integer> turn=new ArrayList<>();
    ArrayList<Integer> branch=new ArrayList<>();
    ArrayList<String>name=new ArrayList<>();
    int[] path;
    float dir[][];

    //------------------
    private SurfaceHolder holder;
    private RefreshThread renderThread;
    private boolean isDraw = false;// 控制绘制的开关
    float stepdis,currentX,currentY;
    int stepcount,index,stepcountb;
    Canvas canvas;
    //---------------------新加的
    //初始狀態的Matrix
    private Matrix mMatrix = new Matrix();
    //進行變動狀況下的Matrix
    private Matrix mChangeMatrix = new Matrix();
    //手機畫面尺寸資訊
    private DisplayMetrics mDisplayMetrics;
    //第一點按下的座標
    private PointF mFirstPointF = new PointF();
    //第二點按下的座標
    private PointF mSecondPointF = new PointF();
    //當下的狀態
    private int mState = STATE_NONE;
    //圖片狀態 - 初始狀態
    private  static final int STATE_NONE = 0;
    //圖片狀態 - 拖動狀態
    private static final int STATE_DRAG = 1;
    //圖片狀態 - 縮放狀態
    private static final int STATE_ZOOM = 2;
    //兩點距離
    private float mDistance = 1f;
    //設定縮放最小比例
    private float mMinScale = 0.8f;
    //設定縮放最大比例
    private float mMaxScale = 1.5f;
    //----------------------
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        isDraw = true;
        renderThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isDraw = false;
    }


    public Drawl(Context context) {
        super(context);
        //----------
        holder = this.getHolder();
        holder.addCallback(this);
        renderThread = new RefreshThread(holder);
        //-------
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

        paint7=new Paint(Paint.DITHER_FLAG);//創建一個畫筆
        paint7.setStrokeWidth(pen);//筆寬
        paint7.setColor(Color.YELLOW);//設置為黃筆
        paint7.setAntiAlias(true);//鋸齒不顯示


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
    public void draw_stepdis(float dsd){stepdis=dsd;}
    public void draw_step_c(int dsc){stepcount=dsc;}
    public void draw_index(int di){index=di;}
    public void draw_step_cb(int dscb){stepcountb=dscb;}
    public void draw_dir(float ddir[][]){dir=ddir;}

    /*////--------------------------------------------
    //兩點距離
    private float Spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Float.valueOf(String.valueOf( Math.sqrt(x * x + y * y)));
    }

    //兩點中心
    private void MidPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            //第一點按下進入
            case MotionEvent.ACTION_DOWN:
                mChangeMatrix.set(mMatrix);
                mFirstPointF.set(event.getX(), event.getY());
                mState = STATE_DRAG;
                break;

            //第二點按下進入
            case MotionEvent.ACTION_POINTER_DOWN:
                mDistance = Spacing(event);
                //只要兩點距離大於10就判定為多點觸碰
                if (Spacing(event) > 10f) {
                    mChangeMatrix.set(mMatrix);
                    MidPoint(mSecondPointF, event);
                    mState = STATE_ZOOM;
                }
                break;

            //離開觸碰
            case MotionEvent.ACTION_UP:
                break;

            //離開觸碰，狀態恢復
            case MotionEvent.ACTION_POINTER_UP:
                mState = STATE_NONE;
                break;

            //滑動過程進入
            case MotionEvent.ACTION_MOVE:
                if (mState == STATE_DRAG) {
                    mMatrix.set(mChangeMatrix);
                    mMatrix.postTranslate(event.getX() - mFirstPointF.x, event.getY() - mFirstPointF.y);
                } else if (mState == STATE_ZOOM) {
                    float NewDistance = Spacing(event);
                    if (NewDistance > 10f) {
                        mMatrix.set(mChangeMatrix);
                        float NewScale = NewDistance / mDistance;
                        mMatrix.postScale(NewScale, NewScale, mSecondPointF.x, mSecondPointF.y);
                    }
                }
                break;
            }

            canvas.setMatrix(mMatrix);
        //縮放設定
        Scale();

        return true;
    }

    //圖片縮放層級設定
    private void Scale()
    {
        //取得圖片縮放的層級
        float level[] = new float[9];
        mMatrix.getValues(level);

        //狀態為縮放時進入
        if (mState == STATE_ZOOM)
        {
            //若層級小於1則縮放至原始大小
            if (level[0] < mMinScale)
            {
                canvas.scale(mMinScale, mMinScale);
            }

            //若縮放層級大於最大層級則顯示最大層級
            if (level[0] > mMaxScale)  mMatrix.set(mChangeMatrix);
        }
    }
    *///-----------------------------------------




    class RefreshThread extends Thread {
        private SurfaceHolder holder;
        public RefreshThread(SurfaceHolder holder) {
            this.holder = holder;
            isDraw = true;
        }

        public void onDraw() {
            try {
                synchronized (holder) {
                    canvas = holder.lockCanvas();
                    // TODO: consider storing these as member variables to reduce
                    canvas.scale(0.8f,0.8f);
                   /*if ((x.get(path[index])<x.get(path[index+1])))   //下一點比較大 往大的走用加的
                        currentX=canvas.getWidth()+init+x.get(path[index])*length;//+(stepcount-stepcountb )*stepdis*length;
                    else if(x.get(path[index])>x.get(path[index+1]))  //下一點比較小 往小的走用減的
                        currentX=canvas.getWidth()+init+x.get(path[index])*length;//-(stepcount-stepcountb)*stepdis*length;
                    if ((y.get(path[index])<y.get(path[index+1])))   //下一點比較大 往大的走用加的
                        currentY=canvas.getHeight()+init+y.get(path[index])*length;//-(stepcount-stepcountb)*stepdis*length;
                    else if(y.get(path[index])>y.get(path[index+1]))  //下一點比較小 往小的走用減的
                        currentY=canvas.getHeight()+init+y.get(path[index])*length;//+(stepcount-stepcountb)*stepdis*length;*/

                    canvas.drawColor(Color.BLACK);
                    //---------------
                    int branch_c=branch.size(),min_up=0,min_down=0,min_right=0,min_left=0;
                    double min_dis_up,min_dis_down,min_dis_right,min_dis_left;
                    c=0;

                    for (int i=0;i<name.size();i++)
                    {
                        if (name.get(i).equals("服務中心"))
                        {
                            length=20;
                            init_x=-500;
                            init_y=-100;
                        }

                    }

                    do
                    {
                        if(turn.get(c)>=2)  //該點到下一點是岔路2表示兩條3表示三條.....
                        {
                            min_dis_up=1000;
                            min_dis_down=1000;
                            min_dis_right=1000;
                            min_dis_left=1000;
                            for (int i=c+1;i<x.size();i++)
                            {
                                if (Math.round(x.get(c))==Math.round(x.get(i)))//同一個X
                                {
                                    if (y.get(c)<y.get(i))//上面
                                    {
                                        if (min_dis_up>Math.sqrt(Math.pow((x.get(i) - x.get(c)), 2.0) +Math.pow((y.get(i) - y.get(c)), 2.0)))
                                        {
                                            min_dis_up=Math.sqrt(Math.pow((x.get(i) - x.get(c)), 2.0) +Math.pow((y.get(i) - y.get(c)), 2.0));
                                            min_up=i;
                                        }
                                    }
                                    else//下面
                                    {
                                        if (min_dis_down>Math.sqrt(Math.pow((x.get(i) - x.get(c)), 2.0) +Math.pow((y.get(i) - y.get(c)), 2.0)))
                                        {
                                            min_dis_down=Math.sqrt(Math.pow((x.get(i) - x.get(c)), 2.0) +Math.pow((y.get(i) - y.get(c)), 2.0));
                                            min_down=i;
                                        }
                                    }
                                }else if(Math.round(y.get(c))==Math.round(y.get(i)))//同Y
                                {
                                    if (x.get(c)<x.get(i))//  下一點在右邊
                                    {
                                        if (min_dis_right>Math.sqrt(Math.pow((x.get(i) - x.get(c)), 2.0) +Math.pow((y.get(i) - y.get(c)), 2.0)))//找最接近這點的座標 建關係 (通常是第一個)
                                        {
                                            min_dis_right=Math.sqrt(Math.pow((x.get(i) - x.get(c)), 2.0) +Math.pow((y.get(i) - y.get(c)), 2.0));
                                            min_right=i;
                                        }
                                    }
                                    else //左邊
                                    {
                                        if (min_dis_left>Math.sqrt(Math.pow((x.get(i) - x.get(c)), 2.0) +Math.pow((y.get(i) - y.get(c)), 2.0)))//找最接近這點的座標 建關係 (通常是第一個)
                                        {
                                            min_dis_left=Math.sqrt(Math.pow((x.get(i) - x.get(c)), 2.0) +Math.pow((y.get(i) - y.get(c)), 2.0));
                                            min_left=i;
                                        }
                                    }
                                }
                            }
                            //-------up----
                            if (min_dis_up!=1000)
                            {
                                width=canvas.getWidth()+init_x+x.get(c)*length;
                                height=canvas.getHeight()+init_y-y.get(c)*length;
                                d_x=(x.get(min_up)-x.get(c))*length;
                                d_y=(y.get(min_up)-y.get(c))*length;
                                canvas.drawLine(width,height,width+d_x,height-d_y,paint);
                            }
                            //------down----
                            if (min_dis_down!=1000)
                            {
                                width=canvas.getWidth()+init_x+x.get(c)*length;
                                height=canvas.getHeight()+init_y-y.get(c)*length;
                                d_x=(x.get(min_down)-x.get(c))*length;
                                d_y=(y.get(min_down)-y.get(c))*length;
                                canvas.drawLine(width,height,width+d_x,height-d_y,paint);
                            }
                            //------right-------
                            if (min_dis_right!=1000) {
                                width=canvas.getWidth()+init_x+x.get(c)*length;
                                height=canvas.getHeight()+init_y-y.get(c)*length;
                                d_x=(x.get(min_right)-x.get(c))*length;
                                d_y=(y.get(min_right)-y.get(c))*length;
                                canvas.drawLine(width,height,width+d_x,height-d_y,paint);
                            }
                            //-------left--------
                            if (min_dis_left!=1000) {
                                width=canvas.getWidth()+init_x+x.get(c)*length;
                                height=canvas.getHeight()+init_y-y.get(c)*length;
                                d_x=(x.get(min_left)-x.get(c))*length;
                                d_y=(y.get(min_left)-y.get(c))*length;
                                canvas.drawLine(width,height,width+d_x,height-d_y,paint);
                            }
                        }
                        else if(turn.get(c)==0||turn.get(c)==-2)
                        {
                            width=canvas.getWidth()+init_x+x.get(c)*length;
                            height=canvas.getHeight()+init_y-y.get(c)*length;
                            d_x=(x.get(c+1)-x.get(c))*length;
                            d_y=(y.get(c+1)-y.get(c))*length;
                            canvas.drawLine(width,height,width+d_x,height-d_y,paint);
                        }
                        c++;
                    }while(c<turn.size()-1);


                    width=canvas.getWidth()+init_x+x.get(path[0])*length;
                    height=canvas.getHeight()+init_y-y.get(path[0])*length;

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

                    //---------------

                    currentX=canvas.getWidth()+init_x+x.get(path[index])*length;
                    currentY=canvas.getHeight()+init_y-y.get(path[index])*length;
                   /* if (path[index]!=path[path_c-1])
                        index++;
                    else
                        index=0;*/

                    canvas.drawCircle(currentX, currentY, pen*2, paint7);


                    holder.unlockCanvasAndPost(canvas);//结束锁定画图，并提交改变。
                    Thread.sleep(1000);//睡眠时间为1秒



                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            while (isDraw) {
                onDraw();
            }
        }
    }

}



