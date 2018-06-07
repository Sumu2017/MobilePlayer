package com.yml.mobileplayer.pager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yml.mobileplayer.MyApplication;
import com.yml.mobileplayer.R;
import com.yml.mobileplayer.adapter.NetAudioAdapter;
import com.yml.mobileplayer.base.BasePager;
import com.yml.mobileplayer.bean.NetAudioPageBean;
import com.yml.mobileplayer.utils.CacheUtils;
import com.yml.mobileplayer.utils.Constants;
import com.yml.mobileplayer.utils.LogUtil;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

public class NetAudioPager extends BasePager {
    private static final String mTAG="NetAudioPager";
    private static final String NET_INFO_CACHE="net_info_cache";
    public NetAudioPager(Context context) {
        super(context);
    }
    @ViewInject(R.id.lv_video)
    private ListView mLvVideo;
    @ViewInject(R.id.tv_no_net)
    private TextView mTvNoNet;
    @ViewInject(R.id.pb_loading)
    private ProgressBar mPbLoading;

    private List<NetAudioPageBean.ListBean> mDatas;
    private NetAudioAdapter mNetAudioAdapter;



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
        mDatas=new ArrayList<>();
        mNetAudioAdapter=new NetAudioAdapter(mContext,mDatas);
        mLvVideo.setAdapter(mNetAudioAdapter);
        mLvVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        String cache = CacheUtils.getString(mContext, NET_INFO_CACHE);
        if (!TextUtils.isEmpty(cache)){
            mDatas.addAll(parseData(cache));
            showData();
        }
        getDataFromServer();
    }

    private void getDataFromServer() {
        mPbLoading.setVisibility(View.VISIBLE);
        RequestParams params=new RequestParams(Constants.ALL_RES_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                CacheUtils.putString(mContext,NET_INFO_CACHE,result);
                List<NetAudioPageBean.ListBean> datas = parseData(result);
                mDatas.clear();
                mDatas.addAll(datas);
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
        if (!mDatas.isEmpty()&&mDatas.size()>0){
            mTvNoNet.setVisibility(View.GONE);
            mNetAudioAdapter.refreshData(mDatas);
        }else {
            mTvNoNet.setVisibility(View.VISIBLE);
        }
        mPbLoading.setVisibility(View.GONE);
    }

    /**
     * 解析数据
     * @param result
     */
    private List<NetAudioPageBean.ListBean> parseData(String result) {
        List<NetAudioPageBean.ListBean> datas = new ArrayList<>();
        NetAudioPageBean netAudioPageBean=MyApplication.getGson().fromJson(result,NetAudioPageBean.class);
        if (netAudioPageBean!=null){
            if (!netAudioPageBean.getList().isEmpty()){
                datas=netAudioPageBean.getList();
            }
        }
        return datas;
    }
}
