package com.example.administrator.weather0237;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.ant.liao.GifView;
import com.example.administrator.weather0237.app.MyApplication;
import com.example.administrator.weather0237.cn.edu.wzu.wujiajie.bean.City;
import com.example.administrator.weather0237.cn.edu.wzu.wujiajie.bean.TodayWeather;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Administrator on 2016/11/1.
 */
public class MainActivity0237 extends Activity{
    private ImageView mUpdateBtn,mSelectCityBtn,mGpsBtn,mShareBtn;
    private TextView cityTv,timeTv,humidityTv,weekTv,pmDataTv,pmQualityTv,temperatureTv,climateTv,windTv,city_nameTv,now_wenduTv;
    private TextView tvDate1,tvDate2,tvDate3,tvDate4,tvDate5,tvWeek3,tvWeek4,tvWeek5,tvWeather1,tvWeather2,tvWeather3,tvWeather4,tvWeather5,
                tvWendu1,tvWendu2,tvWendu3,tvWendu4,tvWendu5;
    private ImageView ivType1,ivType2,ivType3,ivType4,ivType5;
    private TextView tv_sgUmb,tv_sgCloth,tv_sgSpf,tv_sgSport,tv_sgCar,
                tv_sgUmbDetail,tv_sgClothDetail,tv_sgSpfDetail,tv_sgSportDetail,tv_sgCarDetail;
    private ImageView weatherImg,pmImg;
    private ImageView mUmBtn,mClothBtn,mSpfBtn,mSportBtn,mCarBtn;
    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int NO_SUCH_CITY = 0;
    private GifView gif;

    private TodayWeather todayWeather;

    //定位功能
    //声明mLocationOption对象
    private AMapLocationClientOption mLocationOption = null;
    private AMapLocationClient mlocationClient = null;
    private String gpsCity;
    private MyApplication app = (MyApplication) MyApplication.getInstance();
    private int iGPSflag = 0;
    private static final int WRITE_COARSE_LOCATION_REQUEST_CODE = 1;

    ///
    private DetailDialog detailDialog;

    private Handler mHandler = new Handler(){
        public  void handleMessage(android.os.Message msg){
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather)msg.obj);
                    break;
                case NO_SUCH_CITY:
                    updateNoCity();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.weather_info);

        //气球动画
        gif = (GifView)findViewById(R.id.wvAirBall);
        gif.setGifImage(R.drawable.airball2);

        initView();
        initStart();//定位前先刷新，因为定位较慢
        setGPSData();//打开app自动定位

        //天气更新
        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initStart();
                Toast.makeText(MainActivity0237.this,"更新成功！",Toast.LENGTH_SHORT).show();
            }
        });

        //城市选择
        mSelectCityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity0237.this,SelectCity.class);
                startActivity(intent);
            }
        });

        //GPS定位
        mGpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGPSData();
            }
        });

        //分享
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bmp = getActivityPic();
                long time = System.currentTimeMillis();
                String path = "/sdcard/DCIM/Screenshots/" + "img" + time + "_wjjWeather" + ".png";
                saveBitmapForSdCard(path, bmp);

                shareMsg("分享","WJJ天气","天气",path);
            }
        });

        ///建议按钮
        mUmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailDialog = new DetailDialog(MainActivity0237.this,R.layout.detail_dlg,0,todayWeather);
                detailDialog.show();
            }
        });

        mClothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailDialog = new DetailDialog(MainActivity0237.this,R.layout.detail_dlg,1,todayWeather);
                detailDialog.show();
            }
        });

        mSpfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailDialog = new DetailDialog(MainActivity0237.this,R.layout.detail_dlg,2,todayWeather);
                detailDialog.show();
            }
        });

        mSportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailDialog = new DetailDialog(MainActivity0237.this,R.layout.detail_dlg,3,todayWeather);
                detailDialog.show();
            }
        });

        mCarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailDialog = new DetailDialog(MainActivity0237.this,R.layout.detail_dlg,4,todayWeather);
                detailDialog.show();
            }
        });


    }

    void initView(){
        mUmBtn = (ImageView)findViewById(R.id.rl_btn_umbrella);
        mClothBtn = (ImageView)findViewById(R.id.rl_btn_cloth);
        mSpfBtn = (ImageView)findViewById(R.id.rl_btn_spf);
        mSportBtn = (ImageView)findViewById(R.id.rl_btn_chenlian);
        mCarBtn = (ImageView)findViewById(R.id.rl_btn_car);

        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mSelectCityBtn = (ImageView)findViewById(R.id.title_city_manager);
        mGpsBtn = (ImageView)findViewById(R.id.title_location);
        mShareBtn = (ImageView)findViewById(R.id.title_share);

        city_nameTv = (TextView)findViewById(R.id.title_city_name);
        cityTv = (TextView)findViewById(R.id.city);
        timeTv = (TextView)findViewById(R.id.time);
        humidityTv = (TextView)findViewById(R.id.humidity);
        weekTv = (TextView)findViewById(R.id.week_tody);
        pmDataTv = (TextView)findViewById(R.id.pm_data);
        pmQualityTv = (TextView)findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView)findViewById(R.id.temperature);
        climateTv = (TextView)findViewById(R.id.climate);
        windTv = (TextView)findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        now_wenduTv = (TextView)findViewById(R.id.tv_nowWendu);

        ivType1 = (ImageView)findViewById(R.id.rl_iv_todayType);
        ivType2 = (ImageView)findViewById(R.id.rl_iv_yesdayType);
        ivType3 = (ImageView)findViewById(R.id.rl_iv_day3Type);
        ivType4 = (ImageView)findViewById(R.id.rl_iv_day4Type);
        ivType5 = (ImageView)findViewById(R.id.rl_iv_day5Type);
        tvWeather1 = (TextView)findViewById(R.id.rl_tv_todayTypeAndFenli);
        tvWeather2 = (TextView)findViewById(R.id.rl_tv_yestodayTypeAndFenli);
        tvWeather3 = (TextView)findViewById(R.id.rl_tv_day3TypeAndFenli);
        tvWeather4 = (TextView)findViewById(R.id.rl_tv_day4TypeAndFenli);
        tvWeather5 = (TextView)findViewById(R.id.rl_tv_day5TypeAndFenli);

        tvWendu1 = (TextView)findViewById(R.id.rl_tv_todayLowToHight);
        tvWendu2 = (TextView)findViewById(R.id.rl_tv_yesdayLowToHight);
        tvWendu3 = (TextView)findViewById(R.id.rl_tv_day3LowToHight);
        tvWendu4 = (TextView)findViewById(R.id.rl_tv_day4LowToHight);
        tvWendu5 = (TextView) findViewById(R.id.rl_tv_day5LowToHight);

        tvDate1 = (TextView)findViewById(R.id.tv_date_today);
        tvDate2 = (TextView)findViewById(R.id.tv_date_yestoday);
        tvDate3 = (TextView)findViewById(R.id.tv_date_day3);
        tvDate4 = (TextView)findViewById(R.id.tv_date_day4);
        tvDate5 = (TextView)findViewById(R.id.tv_date_day5);

        tvWeek3 = (TextView)findViewById(R.id.rl_tv_day3);
        tvWeek4 = (TextView)findViewById(R.id.rl_tv_day4);
        tvWeek5 = (TextView)findViewById(R.id.rl_tv_day5);

        tv_sgSport = (TextView)findViewById(R.id.rl_tv_chenlian);
        tv_sgSportDetail = (TextView)findViewById(R.id.rl_tv_chenlianDetail);
        tv_sgUmb = (TextView)findViewById(R.id.rl_tv_umbrella);
        tv_sgUmbDetail = (TextView)findViewById(R.id.rl_tv_umbrellaDetail);
        tv_sgCloth = (TextView)findViewById(R.id.rl_tv_cloth);
        tv_sgClothDetail = (TextView)findViewById(R.id.rl_tv_clothDetail);
        tv_sgSpf = (TextView)findViewById(R.id.rl_tv_spf);
        tv_sgSpfDetail = (TextView)findViewById(R.id.rl_tv_spfDetail);
        tv_sgCar = (TextView)findViewById(R.id.rl_tv_car);
        tv_sgCarDetail = (TextView)findViewById(R.id.rl_tv_carDetail);
    }

    private void initStart(){
        SharedPreferences sharedPreferences = getSharedPreferences("config" , MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("cityNum","101010100");
        Log.d("myWeather",cityCode);

        if (NetUtils.isConnected(MainActivity0237.this) != false) {
            queryWeatherCode(cityCode);
        }else
        {
            Toast.makeText(MainActivity0237.this,"网络挂了！", Toast.LENGTH_LONG).show();
        }

    }

    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode;

        Log.d("myWeather",address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
                todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather",str);
                    }
                    String respanseStr = response.toString();
                    Log.d("myWeather-2",respanseStr);

                    todayWeather = parseXML(respanseStr);
                    if(todayWeather != null && todayWeather.getCity() != null){
                        Log.d("myWeather-3",todayWeather.toString());

                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;

                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);

                    }else {
                        Message msg = new Message();
                        msg.what = NO_SUCH_CITY;
                        mHandler.sendMessage(msg);
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather=null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        int valuwCount =  0;
        int detailCount = 0;

        try{
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null){
                            if(xmlPullParser.getName().equals("city")){
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("updatetime")){
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("wendu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("shidu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("pm25")){
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("quality")){
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if(xmlPullParser.getName().equals("fengli")){
                                eventType = xmlPullParser.next();
                                setWeatherFengli(todayWeather,fengliCount,xmlPullParser.getText());
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals("date")){
                                eventType = xmlPullParser.next();
                                setWeatherDate(todayWeather,dateCount,xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high")){
                                eventType = xmlPullParser.next();
                                setWeatherHigh(todayWeather,highCount,xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low")){
                                eventType = xmlPullParser.next();
                                setWeatherLow(todayWeather,lowCount,xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type")){
                                eventType = xmlPullParser.next();
                                setWeatherType(todayWeather,typeCount,xmlPullParser.getText());
                                typeCount++;
                            }else if(xmlPullParser.getName().equals("sunrise_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setSunrise(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("sunset_1")){
                                eventType = xmlPullParser.next();
                                todayWeather.setSunset(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("value")){
                                eventType = xmlPullParser.next();
                                setSuggestion(todayWeather,valuwCount,xmlPullParser.getText(),1);
                                valuwCount++;
                            }else if(xmlPullParser.getName().equals("detail")){
                                eventType = xmlPullParser.next();
                                setSuggestion(todayWeather,detailCount,xmlPullParser.getText(),2);
                                detailCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }

    //更新所有天气视图
    void updateTodayWeather(TodayWeather todayWeather){
        city_nameTv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(" | "+todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度" + todayWeather.getShidu());
        //pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText("今天 "+todayWeather.getWeek());
        temperatureTv.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
        windTv.setText(todayWeather.getFengxiang() + todayWeather.getFengli());
        now_wenduTv.setText(todayWeather.getWendu() + "℃");

        //有些城市html没有PM25数据
        if(todayWeather.getPm25() == null)
            pmDataTv.setText("N/A");
        else {
            pmDataTv.setText(todayWeather.getPm25());
            int i = Integer.parseInt(todayWeather.getPm25());
            updateImagePm25(i);
        }

        //计算时间,判定白天晚上,根据时间修改天气
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        String ss = String.format("%02d:%02d",hour,minute );

        if( ss.compareTo(todayWeather.getSunrise()) > 0 && ss.compareTo(todayWeather.getSunset()) < 0 ){
            climateTv.setText(todayWeather.getType());
            updateImageWeather(todayWeather.getType(),true,weatherImg);
            updateFanImageWeather(todayWeather.getType(),ivType1);
        } else{
            climateTv.setText(todayWeather.getNightType());
            updateImageWeather(todayWeather.getNightType(),false,weatherImg);
            updateFanImageWeather(todayWeather.getNightType(),ivType1);
        }

        //更新五天所有信息
        updateFanImageWeather(todayWeather.getType2(),ivType2);
        updateFanImageWeather(todayWeather.getType3(),ivType3);
        updateFanImageWeather(todayWeather.getType4(),ivType4);
        updateFanImageWeather(todayWeather.getType5(),ivType5);

        setWeatherText(todayWeather.getType(),todayWeather.getNightType(),tvWeather1,todayWeather.getFengli1());
        setWeatherText(todayWeather.getType2(),todayWeather.getNightType2(),tvWeather2,todayWeather.getFengli2());
        setWeatherText(todayWeather.getType3(),todayWeather.getNightType3(),tvWeather3,todayWeather.getFengli3());
        setWeatherText(todayWeather.getType4(),todayWeather.getNightType4(),tvWeather4,todayWeather.getFengli4());
        setWeatherText(todayWeather.getType5(),todayWeather.getNightType5(),tvWeather5,todayWeather.getFengli5());

        tvWendu1.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
        tvWendu2.setText(todayWeather.getLow2()+"~"+todayWeather.getHigh2());
        tvWendu3.setText(todayWeather.getLow3()+"~"+todayWeather.getHigh3());
        tvWendu4.setText(todayWeather.getLow4()+"~"+todayWeather.getHigh4());
        tvWendu5.setText(todayWeather.getLow5()+"~"+todayWeather.getHigh5());

        tvDate1.setText(todayWeather.getDate());
        tvDate2.setText(todayWeather.getDate2());
        tvDate3.setText(todayWeather.getDate3());
        tvDate4.setText(todayWeather.getDate4());
        tvDate5.setText(todayWeather.getDate5());

        tvWeek3.setText(todayWeather.getWeek3());
        tvWeek4.setText(todayWeather.getWeek4());
        tvWeek5.setText(todayWeather.getWeek5());

        tv_sgUmb.setText(todayWeather.getSg_Umbrella());
        tv_sgUmbDetail.setText(todayWeather.getSg_UmbrellaDetail());
        tv_sgCloth.setText(todayWeather.getSg_Cloth());
        tv_sgClothDetail.setText(todayWeather.getSg_ClothDetail());
        tv_sgSpf.setText(todayWeather.getSg_Spf());
        tv_sgSpfDetail.setText(todayWeather.getSg_SpfDetail());
        tv_sgSport.setText(todayWeather.getSg_Sporting());
        tv_sgSportDetail.setText(todayWeather.getSg_SportingDetail());
        tv_sgCar.setText(todayWeather.getSg_Car());
        tv_sgCarDetail.setText(todayWeather.getSg_CarDetail());

        //Toast.makeText(MainActivity0237.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }

    //更新pm25图片
    void updateImagePm25(int i){
        if(i <= 50){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        }else if(i >50 && i <=100){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
        }else if(i >100 && i <=150){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
        }else if(i >150 && i <=200){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
        }else if(i >200 && i <=300){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
        }else if(i >300){
            pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
        }
    }

    //更新天气图片
    void updateImageWeather(String s,boolean isNight,ImageView imageView){

        switch (s){
            case "晴":
            {
                if(isNight){
                    imageView.setImageResource(R.drawable.biz_plugin_weather_qing);
                } else{
                    imageView.setImageResource(R.drawable.biz_plugin_weather_yeqing);
                }
            }
                break;
            case "阴":
            {
                if(isNight){
                    imageView.setImageResource(R.drawable.biz_plugin_weather_yin);
                } else{
                    imageView.setImageResource(R.drawable.biz_plugin_weather_yeyin);
                }
            }
                break;
            case "多云":
                imageView.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                break;
            case "小雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                break;
            case "中雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                break;
            case "大雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_dayu);
                break;
            case "暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                break;
            case "大暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                break;
            case "特大暴雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                break;
            case "阵雨":
            {
                if( isNight){
                    imageView.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                }else {
                    imageView.setImageResource(R.drawable.biz_plugin_weather_yezhenyu);
                }
            }
                break;
            case "雷阵雨":
                imageView.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                imageView.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                break;
            case "雨夹雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                break;
            case "沙尘暴":
                imageView.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                break;
            case "雾":
                imageView.setImageResource(R.drawable.biz_plugin_weather_wu);
                break;
            case "小雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                break;
            case "中雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                break;
            case "大雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_daxue);
                break;
            case "阵雪":
            {
                if(isNight){
                    imageView.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                }else {
                    imageView.setImageResource(R.drawable.biz_plugin_weather_yezhenxue);
                }
            }
                break;
            case "暴雪":
                imageView.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                break;
            case "扬沙":
                imageView.setImageResource(R.drawable.biz_plugin_weather_yangsha);
                break;
            case "浮沉":
                imageView.setImageResource(R.drawable.biz_plugin_weather_fuchen);
                break;
        }
    }

    void updateFanImageWeather(String s,ImageView imageView){

        switch (s){
            case "晴":
                imageView.setImageResource(R.drawable.biz_fan_weather_qing);
                break;
            case "阴":
                imageView.setImageResource(R.drawable.biz_fan_weather_yin);
                break;
            case "多云":
                imageView.setImageResource(R.drawable.biz_fan_weather_duoyun);
                break;
            case "小雨":
                imageView.setImageResource(R.drawable.biz_fan_weather_xiaoyu);
                break;
            case "中雨":
                imageView.setImageResource(R.drawable.biz_fan_weather_zhongyu);
                break;
            case "大雨":
                imageView.setImageResource(R.drawable.biz_fan_weather_dayu);
                break;
            case "暴雨":
                imageView.setImageResource(R.drawable.biz_fan_weather_baoyu);
                break;
            case "大暴雨":
                imageView.setImageResource(R.drawable.biz_fan_weather_dabaoyu);
                break;
            case "特大暴雨":
                imageView.setImageResource(R.drawable.biz_fan_weather_tedabaoyu);
                break;
            case "阵雨":
                imageView.setImageResource(R.drawable.biz_fan_weather_zhenyu);
                break;
            case "雷阵雨":
                imageView.setImageResource(R.drawable.biz_fan_weather_leizhenyu);
                break;
            case "雷阵雨冰雹":
                imageView.setImageResource(R.drawable.biz_fan_weather_leizhenyubinbao);
                break;
            case "雨夹雪":
                imageView.setImageResource(R.drawable.biz_fan_weather_yujiaxue);
                break;
            case "沙尘暴":
                imageView.setImageResource(R.drawable.biz_fan_weather_shachenbao);
                break;
            case "雾":
                imageView.setImageResource(R.drawable.biz_fan_weather_wu);
                break;
            case "小雪":
                imageView.setImageResource(R.drawable.biz_fan_weather_xiaoxue);
                break;
            case "中雪":
                imageView.setImageResource(R.drawable.biz_fan_weather_zhongxue);
                break;
            case "大雪":
                imageView.setImageResource(R.drawable.biz_fan_weather_daxue);
                break;
            case "阵雪":
                imageView.setImageResource(R.drawable.biz_fan_weather_zhenxue);
                break;
            case "暴雪":
                imageView.setImageResource(R.drawable.biz_fan_weather_baoxue);
                break;
            case "扬沙":
                imageView.setImageResource(R.drawable.biz_fan_weather_yangsha);
                break;
            case "浮沉":
                imageView.setImageResource(R.drawable.biz_fan_weather_fuchen);
                break;
        }
    }

    //无天气信息调用
    void updateNoCity(){
        SharedPreferences sharedPreferences = getSharedPreferences("config" , MODE_PRIVATE);
        String city = sharedPreferences.getString("city","N/A");
        city_nameTv.setText(city + "天气");
        cityTv.setText(" | "+city);
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        now_wenduTv.setText("N/A");

        Toast.makeText(MainActivity0237.this,"没有找到...",Toast.LENGTH_SHORT).show();
    }

    //截取scrollview
    public Bitmap getActivityPic(){
        ScrollView scrollView = (ScrollView)findViewById(R.id.parent_scrollview);
        int h = 0;
        Bitmap bitmap = null;
        // 获取listView实际高度
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
            scrollView.getChildAt(i).setBackgroundColor(
                    Color.parseColor("#b1b1be"));//颜色设置和布局里的一样！
        }
        // 创建对应大小的bitmap
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        return bitmap;
    }

    //保存图片
    public void saveBitmapForSdCard(String path, Bitmap mBitmap) {
        //创建file对象
        File f = new File(path);
        try {
            //创建
            f.createNewFile();
        } catch (IOException e) {

        }

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
            if(fOut != null){
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //直接调用系统的分享功能，shareTitle弹出的activity标题
    public void shareMsg(String shareTitle, String msgTitle, String msgText, String imgPath){
        Intent intent = new Intent(Intent.ACTION_SEND);
        if(imgPath == null || imgPath.equals("")){
            intent.setType("text/plain");//纯文本
        }else {
            File f = new File(imgPath);
            if( f != null && f.exists() && f.isFile()){
                intent.setType("image/jpg");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM,u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT,msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT,msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent,shareTitle));
    }

    //设置五天的天气
    public void setWeatherType(TodayWeather todayWeather,int typeCount,String type){
        switch (typeCount){
            case 0:
                todayWeather.setType(type);
                break;
            case 1:
                todayWeather.setNightType(type);
                break;
            case 2:
                todayWeather.setType2(type);
                break;
            case 3:
                todayWeather.setNightType2(type);
                break;
            case 4:
                todayWeather.setType3(type);
                break;
            case 5:
                todayWeather.setNightType3(type);
                break;
            case 6:
                todayWeather.setType4(type);
                break;
            case 7:
                todayWeather.setNightType4(type);
                break;
            case 8:
                todayWeather.setType5(type);
                break;
            case 9:
                todayWeather.setNightType5(type);
                break;
        }
    }
    public void setWeatherFengli(TodayWeather todayWeather,int fengliCount,String fengli){

        if(fengliCount != 0)
            fengli = fengli.replace("级","");

        switch (fengliCount){
            case 0:
                todayWeather.setFengli(fengli);
                break;
            case 1:
                todayWeather.setFengli1(fengli);
                break;
            case 2:
                todayWeather.setNightFengli1(fengli);
                break;
            case 3:
                todayWeather.setFengli2(fengli);
                break;
            case 5:
                todayWeather.setFengli3(fengli);
                break;
            case 7:
                todayWeather.setFengli4(fengli);
                break;
            case 9:
                todayWeather.setFengli5(fengli);
                break;
        }
    }
    public void setWeatherLow(TodayWeather todayWeather,int count,String str){
        switch (count){
            case 0:
                todayWeather.setLow(str);
                break;
            case 1:
                todayWeather.setLow2(str);
                break;
            case 2:
                todayWeather.setLow3(str);
                break;
            case 3:
                todayWeather.setLow4(str);
                break;
            case 4:
                todayWeather.setLow5(str);
                break;
        }
    }
    public void setWeatherHigh(TodayWeather todayWeather,int count,String str){
        switch (count){
            case 0:
                todayWeather.setHigh(str);
                break;
            case 1:
                todayWeather.setHigh2(str);
                break;
            case 2:
                todayWeather.setHigh3(str);
                break;
            case 3:
                todayWeather.setHigh4(str);
                break;
            case 4:
                todayWeather.setHigh5(str);
                break;
        }
    }

    public void setWeatherDate(TodayWeather todayWeather,int count,String str){

        //用正则表达式将日期和星期截取出来
        String day = str.replaceAll("(.*?)日.*","$1");
        String week = str.replaceAll("(?is).*?日(.*?)","$1");
        //计算月份
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH)+1;
        int year = c.get(Calendar.YEAR);
        int iday = Integer.valueOf(day);
        int iday2 = c.get(Calendar.DAY_OF_MONTH);

        switch (count){
            case 0:
            {
                String s = String.format("%02d/%02d",month,iday);
                todayWeather.setDate(s);
                todayWeather.setWeek(week);
            }
                break;
            case 1:
            {
                month = getMonth(year,month,iday,iday2);
                String s = String.format("%02d/%02d",month,iday);
                todayWeather.setDate2(s);
            }
                break;
            case 2:
            {
                month = getMonth(year,month,iday,iday2);
                String s = String.format("%02d/%02d",month,iday);
                todayWeather.setDate3(s);
                todayWeather.setWeek3(week);
            }
                break;
            case 3:
            {
                month = getMonth(year,month,iday,iday2);
                String s = String.format("%02d/%02d",month,iday);
                todayWeather.setDate4(s);
                todayWeather.setWeek4(week);
            }
                break;
            case 4:
            {
                month = getMonth(year,month,iday,iday2);
                String s = String.format("%02d/%02d",month,iday);
                todayWeather.setDate5(s);
                todayWeather.setWeek5(week);
            }
                break;

        }
    }
    public int getMonth(int year,int month,int day,int day2){
        int monthDay[] = {0,31,28,31,30,31,30,31,31,30,31,30,31};
        if((year % 400 == 0 ) || (year % 100 != 0 && year % 4 == 0)){
            monthDay[2] = 29;
        }

        if(day >= day2){
            return month;
        }else{
            return ( month % 12 + 1);
        }


    }

    public void setSuggestion(TodayWeather todayWeather,int count,String str,int type){

        switch (count){
            case 0:
            {
                if(type == 1){
                    todayWeather.setSg_Sporting(str+"晨练");
                }else {
                    todayWeather.setSg_SportingDetail(str);
                }
            }
                break;
            case 2:
            {
                if(type == 1){
                    todayWeather.setSg_Cloth(str);
                }else {
                    todayWeather.setSg_ClothDetail(str);
                }
            }
                break;
            case 6:
            {
                if(type == 1){
                    todayWeather.setSg_Spf("紫外线"+str);
                }else {
                    todayWeather.setSg_SpfDetail(str);
                }
            }
                break;
            case 7:
            {
                if(type == 1){
                    todayWeather.setSg_Car(str+"洗车");
                }else {
                    todayWeather.setSg_CarDetail(str);
                }
            }
                break;
            case 10:
            {
                if(type == 1){
                    todayWeather.setSg_Umbrella(str);
                }else {
                    todayWeather.setSg_UmbrellaDetail(str);
                }
            }
                break;
        }
    }

    public void setWeatherText(String type,String night,TextView tv,String fengli){
        if(type == night){
            tv.setText(type+" | "+fengli);
        }else {
            tv.setText(type+"转"+night+" | "+fengli);
        }
    }

    //把initStart放入这里，使得初次打开和返回都调用刷新
    public void onStart(){
        super.onStart();
        if(iGPSflag == 1)
            initStart();
        iGPSflag=1;
    }

    //定位功能
    public void setGPSData(){
        //initGPS();
        mlocationClient = new AMapLocationClient(MainActivity0237.this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        //定位成功回调信息，设置相关消息
                        gpsCity = aMapLocation.getCity();
                        gpsCity = gpsCity.replace("市","");
                        gpsCity = gpsCity.replace("县","");

                        updateGPS(gpsCity);
                        Toast.makeText(MainActivity0237.this,"定位成功！",Toast.LENGTH_SHORT).show();
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                                /*Log.e("AmapError","location Error, ErrCode:"
                                        + aMapLocation.getErrorCode() + ", errInfo:"
                                        + aMapLocation.getErrorInfo());
                                        */
                        gpsErroText(aMapLocation.getErrorCode());

                    }
                    mlocationClient.onDestroy();
                }
            }
        });
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置定位间隔,单位毫秒,默认为2000ms
        //mLocationOption.setInterval(2000);
        //获取一次定位结果：
        mLocationOption.setOnceLocation(true);
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(10000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        //启动定位
        mlocationClient.startLocation();
    }
    void updateGPS(String cityName){
        List<City> list = app.getmCityList();

        //c#语法遍历list，耗时过长，有待改进
        for (City city : list) {
            if(city.getCity().compareTo(cityName) == 0){

                SharedPreferences setting = getSharedPreferences("config",MODE_PRIVATE);
                SharedPreferences.Editor editor = setting.edit();
                editor.putString("city",city.getCity());
                editor.putString("cityNum",city.getNumber());
                editor.commit();
                break;
            }
        }
        initStart();
    }
    void gpsErroText(int type){
        switch (type){
            case 0:
                break;
            case 2:
            case 9:
                Toast.makeText(MainActivity0237.this,"请重新定位！",Toast.LENGTH_LONG).show();
                break;
            case 3:
            case 4:
            case 5:
                Toast.makeText(MainActivity0237.this,"请检查网络！",Toast.LENGTH_LONG).show();
                break;
            case 12:
                getPermission();
                break;
            case 14:
                Toast.makeText(MainActivity0237.this,"失败，GPS状态差！",Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(MainActivity0237.this,"定位失败！",Toast.LENGTH_LONG).show();
                break;
        }
    }

    //询问定位权限
    public void getPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        }
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        if(requestCode == WRITE_COARSE_LOCATION_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                setGPSData();
            }
        }


    }


}



