package com.yml.mobileplayer.pager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yml.mobileplayer.R;
import com.yml.mobileplayer.activity.SystemVideoPlayerActivity;
import com.yml.mobileplayer.adapter.NetVideoAdapter;
import com.yml.mobileplayer.base.BasePager;
import com.yml.mobileplayer.bean.MediaItem;
import com.yml.mobileplayer.utils.CacheUtils;
import com.yml.mobileplayer.utils.Constants;
import com.yml.mobileplayer.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

public class NetVideoPager extends BasePager {
    private static final String mTAG="NetVideoPager";
    private static final String NET_VIDEO_CACHE="net_video_cache";
    public NetVideoPager(Context context) {
        super(context);
    }
    @ViewInject(R.id.lv_video)
    private ListView mLvVideo;
    @ViewInject(R.id.tv_no_net)
    private TextView mTvNoNet;
    @ViewInject(R.id.pb_loading)
    private ProgressBar mPbLoading;

    private List<MediaItem> mMediaItems;
    private NetVideoAdapter mNetVideoAdapter;



    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.net_video_pager, null);
        x.view().inject(this,view);
        mTvNoNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataFromServer();
            }
        });
        return view;
    }



    @Override
    public void initData() {
        super.initData();
        mMediaItems=new ArrayList<>();
        mNetVideoAdapter=new NetVideoAdapter(mContext,mMediaItems);
        mLvVideo.setAdapter(mNetVideoAdapter);
        mLvVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SystemVideoPlayerActivity.startSystemVideoPlayerActivity(mContext, (ArrayList<MediaItem>) mMediaItems,null,position);
            }
        });
        String cache = CacheUtils.getString(mContext, NET_VIDEO_CACHE);
        if (!TextUtils.isEmpty(cache)){
            mMediaItems.addAll(parseData(cache));
            showData();
        }
        getDataFromServer();
    }

    private void getDataFromServer() {
        mPbLoading.setVisibility(View.VISIBLE);
        RequestParams params=new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                CacheUtils.putString(mContext,NET_VIDEO_CACHE,result);
                List<MediaItem> mediaItems = parseData(result);
                mMediaItems.clear();
                mMediaItems.addAll(mediaItems);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.d("onError",ex);

            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.d("onCancelled",cex);
            }

            @Override
            public void onFinished() {
                LogUtil.d("onFinished");
                showData();
            }
        });
    }

    private void showData() {
        if (!mMediaItems.isEmpty()&&mMediaItems.size()>0){
            mTvNoNet.setVisibility(View.GONE);
            mNetVideoAdapter.refreshData(mMediaItems);
        }else {
            mTvNoNet.setVisibility(View.VISIBLE);
        }
        mPbLoading.setVisibility(View.GONE);
    }

    /**
     * 解析数据
     * @param result
     */
    private List<MediaItem> parseData(String result) {
        List<MediaItem> mediaItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            if(jsonArray!= null && jsonArray.length() >0){

                for (int i=0;i<jsonArray.length();i++){

                    JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);

                    if(jsonObjectItem != null){

                        MediaItem mediaItem = new MediaItem();

                        String movieName = jsonObjectItem.optString("movieName");//name
                        mediaItem.setName(movieName);

                        String videoTitle = jsonObjectItem.optString("videoTitle");//desc
                        mediaItem.setDesc(videoTitle);

                        String imageUrl = jsonObjectItem.optString("coverImg");//imageUrl
                        mediaItem.setImageUrl(imageUrl);

                        String hightUrl = jsonObjectItem.optString("hightUrl");//data
                        mediaItem.setData(hightUrl);

                        //把数据添加到集合
                        mediaItems.add(mediaItem);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mediaItems;
    }
}
