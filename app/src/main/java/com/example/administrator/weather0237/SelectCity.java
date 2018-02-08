package com.example.administrator.weather0237;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.administrator.weather0237.Search.SideBar;
import com.example.administrator.weather0237.Search.SortAdapter;
import com.example.administrator.weather0237.app.MyApplication;
import com.example.administrator.weather0237.cn.edu.wzu.wujiajie.bean.City;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Administrator on 2016/11/29.
 */
public class SelectCity extends Activity {
    private ImageView mBackBtn;
    private SearchView searchView;
    private ListView listView;
    private TextView dialog,tvText;

    private SideBar sideBar;
    private SortAdapter adapter;

    private List<City> cityList = new ArrayList<City>();

    private City nowCity;
    private String oldCity,oldCityNum;
    private MyApplication app = (MyApplication) MyApplication.getInstance();

    private AlertDialog mdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.select_city);
        mBackBtn = (ImageView)findViewById(R.id.title_back);
        searchView = (SearchView)findViewById(R.id.searchView);
        listView = (ListView) findViewById(R.id.listView_city);

        dialog = (TextView)findViewById(R.id.dialog);
        tvText = (TextView)findViewById(R.id.title_name);

        sideBar = (SideBar)findViewById(R.id.sidebar);

        //获取当前城市
        SharedPreferences setting = getSharedPreferences("config" , MODE_PRIVATE);
        oldCity = setting.getString("city","北京");
        oldCityNum = setting.getString("cityNum","101010100");
        tvText.setText("当前城市："+ oldCity);

        ///通过app读取数据库
        cityList = app.getmCityList();

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //还原sharedpreference中存储的数据
                SharedPreferences setting = getSharedPreferences("config",MODE_PRIVATE);
                SharedPreferences.Editor editor = setting.edit();
                editor.putString("city",oldCity);
                editor.putString("cityNum",oldCityNum);
                editor.commit();
                finish();
            }
        });

        sideBar.setTextView(dialog);
        initEvents();
        setAdapter();

        //询问是否返回刷新的dialog(必须在这里new，否则finish里的dismiss就会出错，或者用isShowing判断)
        mdialog = new AlertDialog.Builder(SelectCity.this)
                .setTitle("提示")
                .setMessage("是否选定城市并返回？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mdialog.dismiss();
                    }
                }).create();

    }

    private void initEvents() {
        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    listView.setSelection(position + 1);
                }
            }
        });

        //ListView的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(position > 0 && position < cityList.size()+1) {
                    tvText.setText("当前城市：" + ((City) adapter.getItem(position - 1)).getCity());
                    //if (position > -1 && position < cityList.size())
                        //Toast.makeText(getApplication(), ((City) adapter.getItem(position - 1)).getCity(), Toast.LENGTH_SHORT).show();

                    nowCity = (City)adapter.getItem(position-1);

                    //更新sharedpreference中存储的数据
                    SharedPreferences setting = getSharedPreferences("config",MODE_PRIVATE);
                    SharedPreferences.Editor editor = setting.edit();
                    editor.putString("city",nowCity.getCity());
                    editor.putString("cityNum",nowCity.getNumber());
                    editor.commit();

                    //点击item就隐藏输入键盘，否则返回会出现dialog一闪而过
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if ( imm.isActive( ) ) {
                        imm.hideSoftInputFromWindow( view.getApplicationWindowToken( ) , 0 );
                    }

                    mdialog.show();

                }
            }
        });

        //滚动监听关闭软键盘
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:// 空闲状态
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:// 滚动状态关闭软键盘
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸后滚动关闭软键盘
                        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        //搜索框内容改变监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
               filterData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);//输入框改变时，ListView也跟着改变
                return true;
            }
        });
    }

    private void setAdapter() {
        //SourceDateList = filledData(getResources().getStringArray(R.array.provinces));
        Collections.sort(cityList, new Comparator<City>() {
            @Override
            public int compare(City o1, City o2) {
                return o1.getFirstPY().compareTo(o2.getFirstPY());
            }
        });
        adapter = new SortAdapter(this,cityList );
        listView.addHeaderView(initHeadView());
        listView.setAdapter(adapter);
    }

    //根据输入检索城市
    private void filterData(String filterStr) {
        List<City> mSortList = new ArrayList<>();
        if (TextUtils.isEmpty(filterStr)) {
            mSortList = cityList;
        } else {
            mSortList.clear();
            for (City sortModel : cityList) {

                //输入是拼音(indexof返回string首次出现位置)，输入是缩写，输入是中文，判断
                if (sortModel.getAllFirstPY().toUpperCase().indexOf(filterStr.toString().toUpperCase()) != -1 ||
                        sortModel.getAllPY().toUpperCase().startsWith(filterStr.toString().toUpperCase()) ||
                        sortModel.getCity().startsWith(filterStr.toString())) {
                    mSortList.add(sortModel);
                }
            }
        }
        // 根据a-z进行排序
        Collections.sort(mSortList, new Comparator<City>() {
            @Override
            public int compare(City o1, City o2) {
                return o1.getFirstPY().compareTo(o2.getFirstPY());
            }
        });
        adapter.updateListView(mSortList);
    }

    private View initHeadView() {
        View headView = getLayoutInflater().inflate(R.layout.support_simple_spinner_dropdown_item, null);
        //下面为添加热门城市，暂时没有成功，等待以后实现
        //GridView mGvCity = (GridView) headView.findViewById(R.id.gv_hot_city);
        //CityAdapter adapter = new CityAdapter(getApplicationContext(), R.layout.gridview_item, cityList);
       // mGvCity.setAdapter(adapter);
        return headView;
    }

    public void onDestroy(){
        super.onDestroy();
        mdialog.dismiss();
    }


}
