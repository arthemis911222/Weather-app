package com.example.administrator.weather0237.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.weather0237.cn.edu.wzu.wujiajie.bean.City;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/12/1.
 */
public class CityDB {
    public static final String CITY_DB_NAME = "city2.db";
    private static final String CITY_TABLE_NAME = "city";
    private SQLiteDatabase db;

    public CityDB(Context context, String path){
        //db = SQLiteDatabase.openOrCreateDatabase(path,null);
        db = context.openOrCreateDatabase(CITY_DB_NAME,Context.MODE_PRIVATE,null);
    }
    public List<City> getAllCity(){
        List<City> list = new ArrayList<City>();
        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME,null);//这是一个sql语句，from后面要有空格，否则查询错误！！！
        while(c.moveToNext()){
            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            City item = new City(province,city,number,firstPY,allPY,
                    allFirstPY);
            list.add(item);
        }
        return list;
    }

}
