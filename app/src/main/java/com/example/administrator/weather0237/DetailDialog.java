package com.example.administrator.weather0237;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.weather0237.cn.edu.wzu.wujiajie.bean.TodayWeather;

/**
 * Created by Administrator on 2016/12/12.
 */
public class DetailDialog extends Dialog {

    private RelativeLayout rlBackgroud;
    private ImageView ivIcon;
    private TextView tvTitle,tvDetail;
    private TodayWeather mTodayWeather;
    private int type;

    private Context context;      // 上下文
    private int layoutResID;      // 布局文件id

    public DetailDialog(Context context, int layoutResID, int type, TodayWeather todayWeather) {
        super(context, R.style.dialog_custom); //dialog的样式
        this.context = context;
        this.layoutResID = layoutResID;

        mTodayWeather = todayWeather;
        this.type = type;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置为居中
       // window.setWindowAnimations(R.style.bottom_menu_animation); // 添加动画效果
        setContentView(layoutResID);
        setData(type,mTodayWeather);//

        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth()*4/5; // 设置dialog宽度为屏幕的4/5
        getWindow().setAttributes(lp);
        setCanceledOnTouchOutside(true);// 点击Dialog外部消失

    }

    void setData(int type,TodayWeather todayWeather){
        rlBackgroud = (RelativeLayout)findViewById(R.id.rl_bg);
        ivIcon = (ImageView)findViewById(R.id.iv_icon);
        tvTitle = (TextView)findViewById(R.id.tv_icon);
        tvDetail = (TextView)findViewById(R.id.tv_detail);

        switch (type){
            case 0:
            {
                ivIcon.setImageResource(R.drawable.umbrella);
                tvTitle.setText(todayWeather.getSg_Umbrella());
                tvDetail.setText(todayWeather.getSg_UmbrellaDetail());
                rlBackgroud.setBackgroundResource(R.drawable.bg_umbrella);
            }
            break;
            case 1:
            {
                ivIcon.setImageResource(R.drawable.cloth);
                tvTitle.setText(todayWeather.getSg_Cloth());
                tvDetail.setText(todayWeather.getSg_ClothDetail());
                rlBackgroud.setBackgroundResource(R.drawable.bg_cloth);
            }
            break;
            case 2:
            {
                ivIcon.setImageResource(R.drawable.spf);
                tvTitle.setText(todayWeather.getSg_Spf());
                tvDetail.setText(todayWeather.getSg_SpfDetail());
                rlBackgroud.setBackgroundResource(R.drawable.bg_spf);
            }
            break;
            case 3:
            {
                ivIcon.setImageResource(R.drawable.chenlian);
                tvTitle.setText(todayWeather.getSg_Sporting());
                tvDetail.setText(todayWeather.getSg_SportingDetail());
                rlBackgroud.setBackgroundResource(R.drawable.bg_chenlian);
            }
            break;
            case 4:
            {
                ivIcon.setImageResource(R.drawable.car);
                tvTitle.setText(todayWeather.getSg_Car());
                tvDetail.setText(todayWeather.getSg_CarDetail());
                rlBackgroud.setBackgroundResource(R.drawable.bg_car);
            }
            break;
        }
    }
}
