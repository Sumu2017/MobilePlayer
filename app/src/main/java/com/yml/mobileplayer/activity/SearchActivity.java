package com.yml.mobileplayer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.yml.mobileplayer.MyApplication;
import com.yml.mobileplayer.R;
import com.yml.mobileplayer.adapter.SearchAdapter;
import com.yml.mobileplayer.bean.SearchBean;
import com.yml.mobileplayer.utils.Constants;
import com.yml.mobileplayer.utils.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SearchActivity extends Activity implements View.OnClickListener {
    private static final String mTAG = "SearchActivity";
    private EditText mEtInput;
    private ImageView mIvVoice;
    private TextView mTvSearch;
    private ListView mListview;
    private ProgressBar mProgressBar;
    private TextView mTvNodata;
    private SpeechRecognizer mAsr;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private String mUrl;
    private List<SearchBean.ItemData> mSearchResults;
    private SearchAdapter mSearchAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViews();
        initData();
    }

    private void initData() {
        mSearchResults = new ArrayList<>();
        mSearchAdapter=new SearchAdapter(this,mSearchResults);
        mListview.setAdapter(mSearchAdapter);
    }

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-06-04 20:34:39 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        mEtInput = (EditText) findViewById(R.id.et_input);
        mIvVoice = (ImageView) findViewById(R.id.iv_voice);
        mTvSearch = (TextView) findViewById(R.id.tv_search);
        mListview = (ListView) findViewById(R.id.listview);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTvNodata = (TextView) findViewById(R.id.tv_nodata);
        mIvVoice.setOnClickListener(this);
        mTvSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_voice:
                showDialog();
                break;

            case R.id.tv_search:
                searchText();
                break;
        }
    }

    private void searchText() {
        String searchContent = mEtInput.getText().toString().trim();
        if (!TextUtils.isEmpty(searchContent)) {
            try {
                searchContent = URLEncoder.encode(searchContent, "UTF-8");
                mUrl = Constants.SEARCH_URL + searchContent;
                getDataFromServer();
                mSearchResults.clear();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void getDataFromServer() {
        mProgressBar.setVisibility(View.VISIBLE);
        RequestParams params = new RequestParams(mUrl);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(mTAG, "onSuccess");
                parseData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d(mTAG, "onError");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d(mTAG, "onCancelled");
            }

            @Override
            public void onFinished() {
                Log.d(mTAG, "onFinished");
                refreshUI();
            }
        });
    }

    private void parseData(String result) {
        SearchBean searchBean = MyApplication.getGson().fromJson(result, SearchBean.class);
        mSearchResults.addAll(searchBean.getItems());
    }

    private void refreshUI(){
        mProgressBar.setVisibility(View.GONE);
        if (mSearchResults!=null&&!mSearchResults.isEmpty()){
            mSearchAdapter.refreshData(mSearchResults);
            mTvNodata.setVisibility(View.GONE);
        }else {
            mTvNodata.setVisibility(View.VISIBLE);
        }
    }

    private void showDialog() {
        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener());
        //2.设置accent、 language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");//中文
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");//普通话
        //若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解
        //结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener());
        //4.显示dialog，接收语音输入
        mDialog.show();
    }

    class MyRecognizerDialogListener implements RecognizerDialogListener {

        /**
         * @param recognizerResult
         * @param b                是否说话结束
         */
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result = recognizerResult.getResultString();
            String text = JsonParser.parseIatResult(result);

            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text);

            StringBuffer resultBuffer = new StringBuffer();//拼成一句
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }

            mEtInput.setText(resultBuffer.toString());
            mEtInput.setSelection(mEtInput.length());

        }

        /**
         * 出错了
         *
         * @param speechError
         */
        @Override
        public void onError(SpeechError speechError) {

        }
    }


    class MyInitListener implements InitListener {

        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                Toast.makeText(SearchActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
