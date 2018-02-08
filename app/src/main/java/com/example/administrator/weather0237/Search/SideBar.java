package com.example.administrator.weather0237.Search;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
/**
 * Created by Administrator on 2016/12/5.
 */
public class SideBar extends View {
    public static String[] INDEX_STRING = {"A", "B", "C", "D", "E", "F", "G",
                "H","I","J","K","L","M","N","O","P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    private List<String> letterList;
    private int choose = -1;
    private Paint paint = new Paint();
    private TextView mTextDialog;

    public SideBar(Context context){
        this(context, null);
    }

    public SideBar(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs,defStyle);
        init();
    }

    private  void init(){
        setBackgroundColor(Color.parseColor("#F0F0F0"));//sidebar颜色
        letterList = Arrays.asList(INDEX_STRING);
    }

    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        int singleHeight = height / letterList.size();
        for(int i = 0; i < letterList.size(); i++){
            paint.setColor(Color.parseColor("#606060"));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
            paint.setTextSize(20);

            //选中
            if(i == choose){
                paint.setColor(Color.parseColor("#4F41FD"));
                paint.setFakeBoldText(true);
            }

            //x坐标 = 中间 - 字符串宽度的一半
            float xPos = width / 2 - paint.measureText(letterList.get(i))/2;
            float yPos = singleHeight * i +singleHeight/2;
            canvas.drawText(letterList.get(i),xPos,yPos,paint);
            paint.reset();

        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int)(y / getHeight() * letterList.size());

        switch (action){

            case MotionEvent.ACTION_UP:
                setBackgroundColor(Color.parseColor("#f0f0f0"));
                choose = -1;
                invalidate();
                if(mTextDialog != null){
                    mTextDialog.setVisibility(View.GONE);
                }
                break;
            default:
                setBackgroundColor(Color.parseColor("#cfcfcf"));//修改待修改
                if (oldChoose != c){
                    if(c >= 0 && c < letterList.size()){
                        if(listener != null){
                            listener.onTouchingLetterChanged(letterList.get(c));;
                        }
                        if(mTextDialog != null){
                            mTextDialog.setText(letterList.get(c));
                            mTextDialog.setVisibility(View.VISIBLE);
                        }
                        choose = c;
                        invalidate();
                    }
                }
                break;
        }

        return true;
    }

    //设置显示当前按下的字母的ListView
    public void setTextView(TextView mTextDialog){
        this.mTextDialog = mTextDialog;
    }

    public void setOnTouchingLetterChangedListener( OnTouchingLetterChangedListener onTouchingLetterChangedListener){
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public interface OnTouchingLetterChangedListener{
         void onTouchingLetterChanged(String s);
    }

}
