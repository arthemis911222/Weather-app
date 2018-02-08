package com.example.administrator.weather0237.Search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.administrator.weather0237.R;
import com.example.administrator.weather0237.cn.edu.wzu.wujiajie.bean.City;

import java.util.List;

/**
 * Created by Administrator on 2016/12/5.
 */
public class SortAdapter extends BaseAdapter implements SectionIndexer {
    private List<City> cityList = null;
    private Context mContext;

    public SortAdapter(Context mContext,List<City> cityList){
        this.mContext = mContext;
        this.cityList = cityList;
    }

    public void updateListView(List<City> cityList){
        this.cityList = cityList;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return this.cityList.size();
    }

    @Override
    public Object getItem(int position) {
        return cityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        final City mContent = cityList.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_select_city, null);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_city_name);
            convertView.setTag(viewHolder);
            viewHolder.tvLetter = (TextView) convertView.findViewById(R.id.tv_catagory);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        int section = getSectionForPosition(position);

        if (position == getPositionForSection(section)) {
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(mContent.getFirstPY());
        } else {
            viewHolder.tvLetter.setVisibility(View.GONE);
        }

        viewHolder.tvTitle.setText(this.cityList.get(position).getCity());

        return convertView;
    }

    final static class ViewHolder{
        TextView tvLetter;
        TextView tvTitle;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        for (int i = 0; i < getCount(); i++){
            String sortStr = cityList.get(i).getFirstPY();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if(firstChar == sectionIndex){
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        return cityList.get(position).getFirstPY().charAt(0);
    }
}
