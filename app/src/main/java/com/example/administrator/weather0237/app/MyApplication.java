package com.example.administrator.weather0237.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.example.administrator.weather0237.cn.edu.wzu.wujiajie.bean.City;
import com.example.administrator.weather0237.db.CityDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.*;

/**
 * Created by Administrator on 2016/12/1.
 */
public class MyApplication extends Application{
    private static final String TAG = "MyAPP";
    private List<City> mCityList;
    //private HashMap<String,List<City>> mCityHash;
    private CityDB mCityDB;

    private static Application mApplication;
    @Override
    public void onCreate() {
        super.onCreate();

        mApplication = this;

        mCityDB = openCityDB();

        //mCityList = new ArrayList<City>();
        //mCityList = mCityDB.getAllCity();
        initCityList();
    }


    private void initCityList(){
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCityList = mCityDB.getAllCity();
            }
        }).start();
    }

    public static Application getInstance(){
        return mApplication;
    }

    private CityDB openCityDB(){
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases"
                + File.separator;

        File dbFile = new File(path);//设置目录参数
        File db = new File(dbFile+"/"+CityDB.CITY_DB_NAME);

        if( !db.exists()){
            //Log.i("MyApp","db is not exists");
            try{
                InputStream is = getAssets().open("city.db");

                //创建文件夹和文件
                dbFile.mkdirs();//新建目录
                String filename = CityDB.CITY_DB_NAME;
                File cacheFile = new File(dbFile,filename);//设置参数
                try {
                    cacheFile.createNewFile();//生成文件
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //

                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while((len = is.read(buffer)) != -1){
                    fos.write(buffer,0,len);
                    fos.flush();
                }
                fos.close();
                is.close();
            }catch (IOException e){
                e.printStackTrace();
                System.exit(0);
            }
        }

        return new CityDB(this,path + CityDB.CITY_DB_NAME);
    }

    public List<City> getmCityList() {
        return mCityList;
    }

}
