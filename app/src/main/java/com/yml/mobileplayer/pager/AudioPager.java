package com.yml.mobileplayer.pager;

import android.content.ContentResolver;
import android.content.Context;
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
import com.yml.mobileplayer.activity.AudioPlayActivity;
import com.yml.mobileplayer.adapter.AudioAdapter;
import com.yml.mobileplayer.base.BasePager;
import com.yml.mobileplayer.bean.MediaItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AudioPager extends BasePager implements AdapterView.OnItemClickListener {

    private static final int DATA_LOADING_FINISH=0x01;
    private ListView mLvAudio;
    private TextView mTvNoAudio;
    private ProgressBar mPbLoading;

    private List<MediaItem> mMediaItems;
    private AudioAdapter mAudioAdapter;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DATA_LOADING_FINISH:
                    if (msg.obj!=null){
                        mMediaItems.addAll((Collection<? extends MediaItem>) msg.obj);
                    }
                    if (!mMediaItems.isEmpty()&&mMediaItems.size()>0){
                        mTvNoAudio.setVisibility(View.GONE);
                        mAudioAdapter.refreshData(mMediaItems);
                    }else {
                        mTvNoAudio.setVisibility(View.VISIBLE);
                    }
                    mPbLoading.setVisibility(View.GONE);
                    break;
            }
        }
    };

    public AudioPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.video_pager, null);
        mLvAudio = view.findViewById(R.id.lv_video);
        mTvNoAudio = view.findViewById(R.id.tv_no_video);
        mPbLoading = view.findViewById(R.id.pb_loading);
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        mMediaItems=new ArrayList<>();
        mAudioAdapter=new AudioAdapter(mContext,mMediaItems);
        mLvAudio.setAdapter(mAudioAdapter);
        mLvAudio.setOnItemClickListener(this);
        mTvNoAudio.setText("没有音频文件...");
        getDataFromLocal();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AudioPlayActivity.startAudioPlayActivity(mContext,position);
    }


    public void getDataFromLocal() {
        mPbLoading.setVisibility(View.VISIBLE);

        new Thread() {
            @Override
            public void run() {
                super.run();
                List<MediaItem> mediaItems=new ArrayList<>();
                ContentResolver resolver = mContext.getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String obj[] = {MediaStore.Audio.Media.DISPLAY_NAME,//视频名称
                        MediaStore.Audio.Media.DURATION,//视频时长
                        MediaStore.Audio.Media.SIZE,//视频大小
                        MediaStore.Audio.Media.DATA,//视频地址
                        MediaStore.Audio.Media.ARTIST,//歌曲的演唱者
                };
                Cursor cursor = resolver.query(uri, obj, null, null, null);
                if (cursor!=null){
                    while (cursor.moveToNext()){
                        MediaItem mediaItem=new MediaItem();
                        mediaItem.setName(cursor.getString(0));
                        mediaItem.setDuration(cursor.getInt(1));
                        mediaItem.setSize(cursor.getLong(2));
                        mediaItem.setData(cursor.getString(3));
                        mediaItem.setArtist(cursor.getString(4));
                        mediaItems.add(mediaItem);
                    }
                    cursor.close();
                }
                Message.obtain(mHandler,DATA_LOADING_FINISH,mediaItems).sendToTarget();
            }
        }.start();
    }
}
