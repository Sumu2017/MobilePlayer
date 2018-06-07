package com.yml.mobileplayer.pager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yml.mobileplayer.R;
import com.yml.mobileplayer.adapter.VideoAdapter;
import com.yml.mobileplayer.activity.MainActivity;
import com.yml.mobileplayer.activity.SystemVideoPlayerActivity;
import com.yml.mobileplayer.base.BasePager;
import com.yml.mobileplayer.bean.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class VideoPager extends BasePager implements AdapterView.OnItemClickListener {

    private static final int DATA_LOADING_FINISH=0x01;
    private ListView mLvVideo;
    private TextView mTvNoVideo;
    private ProgressBar mPbLoading;

    private List<MediaItem> mMediaItems;
    private VideoAdapter mVideoAdapter;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DATA_LOADING_FINISH:
                    if (!mMediaItems.isEmpty()&&mMediaItems.size()>0){
                        mTvNoVideo.setVisibility(View.GONE);
                        mVideoAdapter.refreshData(mMediaItems);
                    }else {
                        mTvNoVideo.setVisibility(View.VISIBLE);
                    }
                    mPbLoading.setVisibility(View.GONE);
                    break;
            }
        }
    };

    public VideoPager(MainActivity context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.video_pager, null);
        mLvVideo = view.findViewById(R.id.lv_video);
        mTvNoVideo = view.findViewById(R.id.tv_no_video);
        mPbLoading = view.findViewById(R.id.pb_loading);
        mTvNoVideo.setText("没有视频文件...");
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        mMediaItems=new ArrayList<>();
        mVideoAdapter=new VideoAdapter(mContext,mMediaItems);
        mLvVideo.setAdapter(mVideoAdapter);
        mLvVideo.setOnItemClickListener(this);
        getDataFromLocal();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SystemVideoPlayerActivity.startSystemVideoPlayerActivity(mContext,(ArrayList<MediaItem>) mMediaItems,null,position);
    }


    public void getDataFromLocal() {
        mPbLoading.setVisibility(View.VISIBLE);

        new Thread() {
            @Override
            public void run() {
                super.run();
                ContentResolver resolver = mContext.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String obj[] = {MediaStore.Video.Media.DISPLAY_NAME,//视频名称
                        MediaStore.Video.Media.DURATION,//视频时长
                        MediaStore.Video.Media.SIZE,//视频大小
                        MediaStore.Video.Media.DATA,//视频地址
                };
                Cursor cursor = resolver.query(uri, obj, null, null, null);
                if (cursor!=null){
                    while (cursor.moveToNext()){
                        MediaItem mediaItem=new MediaItem();
                        mediaItem.setName(cursor.getString(0));
                        mediaItem.setDuration(cursor.getInt(1));
                        mediaItem.setSize(cursor.getLong(2));
                        mediaItem.setData(cursor.getString(3));
                        mMediaItems.add(mediaItem);
                    }
                    cursor.close();
                }
                Message.obtain(mHandler,DATA_LOADING_FINISH).sendToTarget();
            }
        }.start();
    }


}
