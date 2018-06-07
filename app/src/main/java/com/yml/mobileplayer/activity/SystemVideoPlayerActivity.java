package com.yml.mobileplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yml.mobileplayer.R;
import com.yml.mobileplayer.bean.MediaItem;
import com.yml.mobileplayer.utils.Utils;
import com.yml.mobileplayer.view.MyVideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SystemVideoPlayerActivity extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, View.OnClickListener {

    private static final String mTAG = "SystemVideoPlayer";
    private int mPosition;
    private int mCurrentVoice;
    private int mMaxVoice;
    private Uri mUri;


    public static void startSystemVideoPlayerActivity(Context context, ArrayList<MediaItem> mediaItems, Uri uri,int position) {
        Intent intent = new Intent(context, SystemVideoPlayerActivity.class);
        intent.putParcelableArrayListExtra("mediaItems", mediaItems);
        intent.putExtra("position", position);
        intent.setData(uri);
        context.startActivity(intent);
    }


    private static final int UPDATA_SEEKBAR_AND_TIEM = 0x01;
    private static final int UPDATA_TIME = 0x02;
    private static final int HIDE_MEDIA_CONTROLLER = 0x03;
    private static final int DEFAULT_VIDEO_TYPE = 0x04;
    private static final int FULLSCREEN_VIDEO_TYPE = 0x05;
    private static final int UPDATA_NET_SPEED = 0x06;
    private MediaItem mMediaItem;
    private List<MediaItem> mMediaItems;
    private MyVideoView mVvContent;
    private TextView mTvVideoName;
    private ImageView mIvBattery;
    private TextView mTvSystemTime;
    private Button mBtnVoice;
    private SeekBar mSeekbarVoice;
    private Button mBtnSwitchPlayer;
    private TextView mTvCurrentTime;
    private SeekBar mSeekbarVideo;
    private TextView mTvTotalTime;
    private Button mBtnExit;
    private Button mBtnPre;
    private Button mBtnStartPause;
    private Button mBtnNext;
    private Button mBtnSwitchScreenFull;
    private LinearLayout mLlBuffer;
    private LinearLayout mLlLoading;
    private RelativeLayout mRelaMediaController;
    private TextView mTvBufferNetspeed, mTvLaodingNetspeed;
    private Utils mUtils;
    private BatteryReceiver mBatteryReceiver;
    private boolean mIsMediaControllerShow = false;
    private GestureDetector mGestureDetector;
    private boolean isFullScreen = false;//是否是全屏播放
    private int mVideoWidth, mVideoHeight;//视屏原大小；
    private int mScreenWidth, mScreenHeight;//手机屏幕大小；
    private AudioManager mAudioManager;
    private boolean isMute = false;//是否是静音

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATA_SEEKBAR_AND_TIEM:
                    updataVideoBarAndTime();
                    mHandler.removeMessages(UPDATA_SEEKBAR_AND_TIEM);
                    sendEmptyMessageDelayed(UPDATA_SEEKBAR_AND_TIEM, 1000);
                    break;
                case UPDATA_TIME:
                    mTvSystemTime.setText(getSystemTime());
                    mHandler.removeMessages(UPDATA_TIME);
                    sendEmptyMessageDelayed(UPDATA_TIME, 1000);
                    break;
                case HIDE_MEDIA_CONTROLLER:
                    showHideMediaController(false);
                    break;
                case UPDATA_NET_SPEED:
                    updataNetSpeed();
                    mHandler.removeMessages(UPDATA_NET_SPEED);
                    sendEmptyMessageDelayed(UPDATA_NET_SPEED, 2000);
                    break;
            }
        }
    };

    /**
     * 更新网速
     */
    private void updataNetSpeed() {
        String netSpeed = mUtils.getNetSpeed(this);
        mTvBufferNetspeed.setText("正在缓冲：" + netSpeed);
        mTvLaodingNetspeed.setText("正在玩命加载：" + netSpeed);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_video_player);
        findViews();
        initData();
        //vv_content.setMediaController(new MediaController(this));
    }

    private void initData() {
        mUtils = new Utils();
        mUri = getIntent().getData();
        if (mUri != null) {
            mMediaItem = new MediaItem();
            mMediaItem.setName(mUri.toString());
            mMediaItem.setData(mUri.toString());
            updataMediaInfo(mMediaItem);
        } else {
            mMediaItems = getIntent().getParcelableArrayListExtra("mediaItems");
            if (mMediaItems != null && mMediaItems.size() > 0) {
                mPosition = getIntent().getIntExtra("position", -1);
                mMediaItem = mMediaItems.get(mPosition);
                updataMediaInfo(mMediaItem);
            }
        }
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;

        //注册电量广播
        mBatteryReceiver = new BatteryReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryReceiver, intentFilter);

        mHandler.sendEmptyMessage(UPDATA_TIME);

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mIsMediaControllerShow) {
                    showHideMediaController(false);
                } else {
                    showHideMediaController(true);
                }
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isFullScreen) {
                    setVideoType(DEFAULT_VIDEO_TYPE);
                } else {
                    setVideoType(FULLSCREEN_VIDEO_TYPE);
                }
                return super.onDoubleTap(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                //长按
                startAndPause();
            }
        });

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mCurrentVoice = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mMaxVoice = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mSeekbarVoice.setMax(mMaxVoice);
        mSeekbarVoice.setProgress(mCurrentVoice);
    }

    /**
     * 设置全屏或者默认
     */
    private void setVideoType(int type) {
        switch (type) {
            case DEFAULT_VIDEO_TYPE:
                int height = mScreenHeight;
                int width = mScreenWidth;
                if (mVideoWidth > 0 && mVideoHeight > 0) {
                    if (mVideoWidth * height < width * mVideoHeight) {
                        //Log.i("@@@", "image too wide, correcting");
                        width = height * mVideoWidth / mVideoHeight;
                    } else if (mVideoWidth * height > width * mVideoHeight) {
                        //Log.i("@@@", "image too tall, correcting");
                        height = width * mVideoHeight / mVideoWidth;
                    }
                }
                mVvContent.setVideoSize(width, height);
                isFullScreen = false;
                mBtnSwitchScreenFull.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);
                break;
            case FULLSCREEN_VIDEO_TYPE:
                mVvContent.setVideoSize(mScreenWidth, mScreenHeight);
                isFullScreen = true;
                mBtnSwitchScreenFull.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
                break;
        }
    }

    private float startY, endY, startX;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                sendOrRemoveHideMediaControllerMessage(false);
                startX = event.getX();
                startY = event.getY();
                mCurrentVoice = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                break;
            case MotionEvent.ACTION_MOVE:
                endY = event.getY();
                float distanceY = startY - endY;

                if (startX > mScreenWidth / 2) {
                    //触碰的是右半边，改变音量

                    //需要改变的声音大小，移动距离/总高度*总音量
                    int delta = (int) ((distanceY / mScreenHeight) * mMaxVoice);
                    //最终音量，当前音量+改变的音量大小
                    //最终音量不能小于0，也不能超过最大音量
                    int volum = Math.min(Math.max(mCurrentVoice + delta, 0), mMaxVoice);

                    if (volum == 0) {
                        isMute = true;
                    } else {
                        isMute = false;
                    }
                    updataVoice(volum, isMute);
                }else{
                    //触碰的是左半边，改变亮度
                    //左边屏幕-调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(10);
                    }
                    if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(-10);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                sendOrRemoveHideMediaControllerMessage(true);
                break;
        }
        return super.onTouchEvent(event);
    }

    /*
     *
     * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
     */
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0.1) {
            lp.screenBrightness = (float) 0.1;
        }
        getWindow().setAttributes(lp);
    }

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);//0~100
            setBattery(level);
        }
    }


    private void setBattery(int level) {
        if (level <= 0) {
            mIvBattery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            mIvBattery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            mIvBattery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            mIvBattery.setImageResource(R.drawable.ic_battery_40);
            mIvBattery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            mIvBattery.setImageResource(R.drawable.ic_battery_80);
        } else {
            mIvBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    private String getSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNextMedia();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //1.播放的视频格式不支持--跳转到万能播放器继续播放
        startVitamioPlayer();
        //2.播放网络视频的时候，网络中断---1.如果网络确实断了，可以提示用于网络断了；2.网络断断续续的，重新播放
        //3.播放的时候本地文件中间有空白---下载做完成
        Toast.makeText(this,"onError",Toast.LENGTH_SHORT).show();
        return true;
    }

    private void startVitamioPlayer() {
        if (mVvContent != null) {
            mVvContent.stopPlayback();
        }
        VitamioVideoPlayerActivity.startVitamioVideoPlayerActivity(this, (ArrayList<MediaItem>) mMediaItems, mUri, mPosition);
        finish();//关闭页面
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mMediaItem != null) {
            mLlLoading.setVisibility(View.GONE);
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            setVideoType(DEFAULT_VIDEO_TYPE);
            showHideMediaController(false);
            mp.start();
            isPlay(true);
            mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    int secondaryProgress = mSeekbarVideo.getMax() * percent / 100;
                    Log.d(mTAG, "secondaryProgress=" + secondaryProgress + " percent=" + percent);
                    mSeekbarVideo.setSecondaryProgress(secondaryProgress);
                }
            });
            mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START://正在缓冲
                            mLlBuffer.setVisibility(View.VISIBLE);
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END://缓冲结束
                            mLlBuffer.setVisibility(View.GONE);
                            break;
                    }
                    return true;
                }
            });
        }
    }

    /**
     * 更新进度和时间
     */
    private void updataVideoBarAndTime() {
        if (mVvContent != null) {
            int totalTime = mVvContent.getDuration();
            int currentTime = mVvContent.getCurrentPosition();
            mSeekbarVideo.setMax(totalTime);
            mSeekbarVideo.setProgress(currentTime);
            mTvCurrentTime.setText(mUtils.stringForTime(currentTime));
            mTvTotalTime.setText(mUtils.stringForTime(totalTime));
        }
    }


    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-05-26 11:09:18 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        mVvContent = findViewById(R.id.vv_content);
        mTvVideoName = (TextView) findViewById(R.id.tv_video_name);
        mIvBattery = (ImageView) findViewById(R.id.iv_battery);
        mTvSystemTime = (TextView) findViewById(R.id.tv_system_time);
        mBtnVoice = (Button) findViewById(R.id.btn_voice);
        mSeekbarVoice = (SeekBar) findViewById(R.id.seekbar_voice);
        mBtnSwitchPlayer = (Button) findViewById(R.id.btn_switch_player);
        mTvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        mSeekbarVideo = (SeekBar) findViewById(R.id.seekbar_video);
        mTvTotalTime = (TextView) findViewById(R.id.tv_total_time);
        mBtnExit = (Button) findViewById(R.id.btn_exit);
        mBtnPre = (Button) findViewById(R.id.btn_pre);
        mBtnStartPause = (Button) findViewById(R.id.btn_start_pause);
        mBtnNext = (Button) findViewById(R.id.btn_next);
        mBtnSwitchScreenFull = (Button) findViewById(R.id.btn_switch_screen_full);
        mRelaMediaController = findViewById(R.id.rela_media_controller);
        mLlBuffer = findViewById(R.id.ll_buffer);
        mLlLoading = findViewById(R.id.ll_loading);
        mTvBufferNetspeed = findViewById(R.id.tv_buffer_netspeed);
        mTvLaodingNetspeed = findViewById(R.id.tv_laoding_netspeed);
        mBtnVoice.setOnClickListener(this);
        mBtnSwitchPlayer.setOnClickListener(this);
        mBtnExit.setOnClickListener(this);
        mBtnPre.setOnClickListener(this);
        mBtnStartPause.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnSwitchScreenFull.setOnClickListener(this);
        mSeekbarVideo.setOnSeekBarChangeListener(new VideoSeekBarListener());
        mSeekbarVoice.setOnSeekBarChangeListener(new VoiceSeekBarListener());

        mVvContent.setOnCompletionListener(this);
        mVvContent.setOnErrorListener(this);
        mVvContent.setOnPreparedListener(this);
    }


    /**
     * 显示隐藏播放控件
     *
     * @param show
     */
    private void showHideMediaController(boolean show) {
        if (show) {
            mRelaMediaController.setVisibility(View.VISIBLE);
            mIsMediaControllerShow = true;
            sendOrRemoveHideMediaControllerMessage(true);
        } else {
            mRelaMediaController.setVisibility(View.GONE);
            mIsMediaControllerShow = false;
        }
    }

    /**
     * 发送或者移除隐藏MediaController消息
     *
     * @param send
     */
    private void sendOrRemoveHideMediaControllerMessage(boolean send) {
        if (send) {
            mHandler.sendEmptyMessageDelayed(HIDE_MEDIA_CONTROLLER, 5000);
        } else {
            mHandler.removeMessages(HIDE_MEDIA_CONTROLLER);
        }
    }


    private class VideoSeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mVvContent.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            sendOrRemoveHideMediaControllerMessage(false);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendOrRemoveHideMediaControllerMessage(true);
        }
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2018-05-26 11:09:18 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnVoice) {
            // Handle clicks for mBtnVoice
            isMute = !isMute;
            updataVoice(mCurrentVoice, isMute);
        } else if (v == mBtnSwitchPlayer) {
            // Handle clicks for mBtnSwitchPlayer
            showSwichPlayerDialog();
        } else if (v == mBtnExit) {
            // Handle clicks for mBtnExit
            finish();
        } else if (v == mBtnPre) {
            // Handle clicks for mBtnPre
            playPreMedia();
        } else if (v == mBtnStartPause) {
            // Handle clicks for mBtnStartPause
            startAndPause();
        } else if (v == mBtnNext) {
            // Handle clicks for mBtnNext
            playNextMedia();
        } else if (v == mBtnSwitchScreenFull) {
            // Handle clicks for mBtnSwitchScreenFull
            if (isFullScreen) {
                setVideoType(DEFAULT_VIDEO_TYPE);
            } else {
                setVideoType(FULLSCREEN_VIDEO_TYPE);
            }
        }
        sendOrRemoveHideMediaControllerMessage(false);
        sendOrRemoveHideMediaControllerMessage(true);
    }

    /**
     * 切换播放器
     */
    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("系统播放器提示");
        builder.setMessage("当您播放视频，有声音没有画面的时候，请切换万能播放器播放");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVitamioPlayer();
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    /**
     * 播放或者暂停
     */
    private void startAndPause() {
        if (mVvContent.isPlaying()) {
            mVvContent.pause();
            isPlay(false);
        } else {
            mVvContent.start();
            isPlay(true);
        }
    }

    /**
     * 播放上一个视频
     */
    private void playPreMedia() {
        if (mMediaItems != null && mMediaItems.size() > 0) {
            mPosition--;
            if (mPosition >= 0) {
                mMediaItem = mMediaItems.get(mPosition);
                updataMediaInfo(mMediaItem);
            }
        }
    }

    /**
     * 播放下一个视频
     */
    private void playNextMedia() {
        if (mMediaItems != null && mMediaItems.size() > 0) {
            mPosition++;
            if (mPosition < mMediaItems.size()) {
                mMediaItem = mMediaItems.get(mPosition);
                updataMediaInfo(mMediaItem);
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void updataMediaInfo(MediaItem mediaItem) {
        if (mediaItem != null) {
            mTvVideoName.setText(mediaItem.getName());
            mVvContent.setVideoURI(Uri.parse(mediaItem.getData()));
            mLlLoading.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessage(UPDATA_NET_SPEED);
        }
        setBtnPreAndNextEnable();
    }


    /**
     * 设置播放的上下按钮是否可以点击
     */
    private void setBtnPreAndNextEnable() {
        mBtnPre.setEnabled(true);
        mBtnNext.setEnabled(true);
        if (mMediaItems != null && mMediaItems.size() > 1) {
            if (mPosition == 0) {
                //当前位置属于第一个，那上一个置灰
                mBtnPre.setEnabled(false);
            } else if (mPosition == mMediaItems.size() - 1) {
                //当前位置属于最后一个，那下一个置灰
                mBtnNext.setEnabled(false);
            }/*else{
                mBtnPre.setEnabled(true);
                mBtnNext.setEnabled(true);
            }*/
        } else {
            //列表是空的或者长度只有一个的情况
            mBtnPre.setEnabled(false);
            mBtnNext.setEnabled(false);
        }
    }

    private void isPlay(boolean isPlay) {
        if (isPlay) {
            mBtnStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
            mHandler.sendEmptyMessage(UPDATA_SEEKBAR_AND_TIEM);
        } else {
            mBtnStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
            //mHandler.removeMessages(UPDATA_SEEKBAR_AND_TIEM);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mCurrentVoice--;
            updataVoice(mCurrentVoice, false);
            sendOrRemoveHideMediaControllerMessage(false);
            sendOrRemoveHideMediaControllerMessage(true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mCurrentVoice++;
            updataVoice(mCurrentVoice, false);
            sendOrRemoveHideMediaControllerMessage(false);
            sendOrRemoveHideMediaControllerMessage(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBatteryReceiver != null) {
            unregisterReceiver(mBatteryReceiver);
        }
        mHandler.removeCallbacksAndMessages(null);
    }


    private class VoiceSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress == 0) {
                    isMute = true;
                } else {
                    isMute = false;
                }
                updataVoice(progress, isMute);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            sendOrRemoveHideMediaControllerMessage(false);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendOrRemoveHideMediaControllerMessage(true);
        }
    }

    /**
     * 修改声音大小
     *
     * @param currentVoice 当前音量
     * @param isMute       是否静音
     */
    private void updataVoice(int currentVoice, boolean isMute) {
        if (isMute) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            mSeekbarVoice.setProgress(0);
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVoice, 0);
            mSeekbarVoice.setProgress(currentVoice);
            mCurrentVoice = currentVoice;
        }
    }
}
