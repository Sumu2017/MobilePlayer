package com.yml.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yml.mobileplayer.R;
import com.yml.mobileplayer.bean.SearchBean;

import org.xutils.x;

import java.util.List;

public class SearchAdapter extends BaseAdapter {

    private Context mContext;
    private List<SearchBean.ItemData> mSearchItems;

    public SearchAdapter(Context context, List<SearchBean.ItemData> searchItems) {
        mContext = context;
        mSearchItems = searchItems;
    }

    public void refreshData(List<SearchBean.ItemData> searchItems){
        if (!mSearchItems.isEmpty()&&mSearchItems.size()>0) {
           // mMediaItems.clear();
            //mMediaItems.addAll(mediaItems);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mSearchItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mSearchItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if (convertView==null){
            holder=new ViewHolder();
            convertView=View.inflate(mContext, R.layout.item_net_video_pager,null);
            holder.iv_icon=convertView.findViewById(R.id.iv_icon);
            holder.tv_name=convertView.findViewById(R.id.tv_name);
            holder.tv_desc=convertView.findViewById(R.id.tv_desc);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        SearchBean.ItemData itemData = mSearchItems.get(position);
        holder.tv_name.setText(itemData.getItemTitle());
        holder.tv_desc.setText(itemData.getKeywords());
        x.image().bind(holder.iv_icon,itemData.getItemImage().getImgUrl1());
        return convertView;
    }

    static class ViewHolder{
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_desc;
    }
}
