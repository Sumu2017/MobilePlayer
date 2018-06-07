package com.yml.mobileplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yml.mobileplayer.R;
import com.yml.mobileplayer.bean.MediaItem;

import org.xutils.x;

import java.util.List;

public class NetVideoAdapter extends BaseAdapter {

    private Context mContext;
    private List<MediaItem> mMediaItems;

    public NetVideoAdapter(Context context, List<MediaItem> mediaItems) {
        mContext = context;
        mMediaItems = mediaItems;
    }

    public void refreshData(List<MediaItem> mediaItems){
        if (!mediaItems.isEmpty()&&mediaItems.size()>0) {
           // mMediaItems.clear();
            //mMediaItems.addAll(mediaItems);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mMediaItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mMediaItems.get(position);
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
        MediaItem mediaItem = mMediaItems.get(position);
        holder.tv_name.setText(mediaItem.getName());
        holder.tv_desc.setText(mediaItem.getDesc());
        x.image().bind(holder.iv_icon,mediaItem.getImageUrl());
        return convertView;
    }

    static class ViewHolder{
        private ImageView iv_icon;
        private TextView tv_name;
        private TextView tv_desc;
    }
}
