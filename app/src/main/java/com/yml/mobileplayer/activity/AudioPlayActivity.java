package com.yml.mobileplayer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yml.mobileplayer.IMusicPlayerService;
import com.yml.mobileplayer.R;
import com.yml.mobileplayer.service.MusicPlayerService;
import com.yml.mobileplayer.utils.Utils;
import com.yml.mobileplayer.view.BaseVisualizerView;
import com.yml.mobileplayer.view.ShowLyricView;

public class AudioPlayActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static final int UPDATA_DATA_FOR_UI = 0x01;
    public static final int UPDATA_LYRIC = 0x02;
    private UpdataNameAndArtistReceiver mUpdataNameAndArtistReceiver;
    private Utils mUtils;
    private boolean mNotification;//是否是从状态栏过来的
    private int mCurrentMode;

    public static void startAudioPlayActivity(Context context, int position) {
        Intent intent = new Intent(context, AudioPlayActivity.class);
        intent.putExtra("position", position);
        context.startActivity(intent);
    }

    private IMusicPlayerService mIMusicPlayerService;
    private int mPosition;
    private RelativeLayout mRlTop;
    private ImageView mIvIcon;
    private BaseVisualizerView mBaseVisualizerView;
    private TextView mTvArtist;
    private TextView mTvName;
    private LinearLayout mLlBottom;
    private TextView mTvTime;
    private SeekBar mSeekbarAudio;
    private Button mBtnAudioPlaymode;
    private Button mBtnAudioPre;
    private Button mBtnAudioStartPause;
    private Button mBtnAudioNext;
    private Button mBtnLyrc;
    private ShowLyricView mShowLyricView;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATA_DATA_FOR_UI:
                    updataFromService();
                    mHandler.removeMessages(UPDATA_DATA_FOR_UI);
                    sendEmptyMessageDelayed(UPDATA_DATA_FOR_UI, 1000);
                    break;
                case UPDATA_LYRIC:
                    upLyric();
                    mHandler.removeMessages(UPDATA_LYRIC);
                    sendEmptyMessageDelayed(UPDATA_LYRIC, 100);
                    break;
            }
        }
    };

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2018-05-30 22:25:39 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        mRlTop = (RelativeLayout) findViewById(R.id.rl_top);
        mIvIcon = (ImageView) findViewById(R.id.iv_icon);
        mBaseVisualizerView = (BaseVisualizerView) findViewById(R.id.baseVisualizerView);
        mTvArtist = (TextView) findViewById(R.id.tv_artist);
        mTvName = (TextView) findViewById(R.id.tv_name);
        mLlBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mSeekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        mBtnAudioPlaymode = (Button) findViewById(R.id.btn_audio_playmode);
        mBtnAudioPre = (Button) findViewById(R.id.btn_audio_pre);
        mBtnAudioStartPause = (Button) findViewById(R.id.btn_audio_start_pause);
        mBtnAudioNext = (Button) findViewById(R.id.btn_audio_next);
        mBtnLyrc = (Button) findViewById(R.id.btn_lyrc);
        mShowLyricView = (ShowLyricView) findViewById(R.id.showLyricView);

        mBtnAudioPlaymode.setOnClickListener(this);
        mBtnAudioPre.setOnClickListener(this);
        mBtnAudioStartPause.setOnClickListener(this);
        mBtnAudioNext.setOnClickListener(this);
        mBtnLyrc.setOnClickListener(this);

        mSeekbarAudio.setOnSeekBarChangeListener(this);
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2018-05-30 22:25:39 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnAudioPlaymode) {
            // Handle clicks for mBtnAudioPlaymode
            changeMode();
        } else if (v == mBtnAudioPre) {
            // Handle clicks for mBtnAudioPre
            if (mIMusicPlayerService != null) {
                try {
                    mIMusicPlayerService.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == mBtnAudioStartPause) {
            // Handle clicks for mBtnAudioStartPause
            try {
                if (mIMusicPlayerService == null) return;
                if (mIMusicPlayerService.isPlaying()) {
                    mIMusicPlayerService.pause();
                    mBtnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                } else {
                    mIMusicPlayerService.start();
                    mBtnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == mBtnAudioNext) {
            // Handle clicks for mBtnAudioNext
            if (mIMusicPlayerService != null) {
                try {
                    mIMusicPlayerService.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == mBtnLyrc) {
            // Handle clicks for mBtnLyrc
        }
    }

    /**
     * 修改播放模式
     */
    private void changeMode() {
        if (mIMusicPlayerService != null) {
            try {
                mCurrentMode = mIMusicPlayerService.getPlayMode();
                if (mCurrentMode == MusicPlayerService.REPEATE_NORMAL) {
                    mCurrentMode = MusicPlayerService.REPEATE_SINGLE;
                } else if (mCurrentMode == MusicPlayerService.REPEATE_SINGLE) {
                    mCurrentMode = MusicPlayerService.REPEATE_ALL;
                } else if (mCurrentMode == MusicPlayerService.REPEATE_ALL) {
                    mCurrentMode = MusicPlayerService.REPEATE_NORMAL;
                } else {
                    mCurrentMode = MusicPlayerService.REPEATE_NORMAL;
                }
                mIMusicPlayerService.setPlayMode(mCurrentMode);
                upDataPlayModeButtonStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void upDataPlayModeButtonStatus() throws RemoteException {
        mCurrentMode = mIMusicPlayerService.getPlayMode();
        if (mCurrentMode == MusicPlayerService.REPEATE_NORMAL) {
            mBtnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
        } else if (mCurrentMode == MusicPlayerService.REPEATE_SINGLE) {
            mBtnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
        } else if (mCurrentMode == MusicPlayerService.REPEATE_ALL) {
            mBtnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
        } else {
            mBtnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        findViews();
        initData();
        initBindService();
    }

    private void initData() {
        mUtils = new Utils();
        mPosition = getIntent().getIntExtra("position", -1);
        mNotification = getIntent().getBooleanExtra("notification", false);
        mUpdataNameAndArtistReceiver = new UpdataNameAndArtistReceiver();
        IntentFilter intentFilter = new IntentFilter(MusicPlayerService.UPDATA_AUDIO_NAME_ARTIST);
        registerReceiver(mUpdataNameAndArtistReceiver, intentFilter);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (mIMusicPlayerService != null) {
                try {
                    mIMusicPlayerService.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class UpdataNameAndArtistReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.sendEmptyMessage(UPDATA_DATA_FOR_UI);
            mHandler.sendEmptyMessage(UPDATA_LYRIC);
            setupVisualizerFxAndUi();
            initLyric();
        }
    }

    private void updataFromService() {
        if (mIMusicPlayerService != null) {
            try {
                mTvName.setText(mIMusicPlayerService.getName());
                mTvArtist.setText(mIMusicPlayerService.getArtist());
                mTvTime.setText(mUtils.stringForTime(mIMusicPlayerService.getCurrentPosition()) + "/" + mUtils.stringForTime(mIMusicPlayerService.getDuration()));
                mSeekbarAudio.setMax(mIMusicPlayerService.getDuration());
                mSeekbarAudio.setProgress(mIMusicPlayerService.getCurrentPosition());
                upDataPlayModeButtonStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化歌词
     */
    private void initLyric() {
        if (mIMusicPlayerService != null) {
            try {
                mShowLyricView.setLyrics(mIMusicPlayerService.getLyrics());
                mShowLyricView.setIndex(0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新歌词
     */
    private void upLyric() {
        if (mIMusicPlayerService != null) {
            try {
                mShowLyricView.setCurrentPosition(mIMusicPlayerService.getCurrentPosition());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private Visualizer mVisualizer;

    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
     */
    private void setupVisualizerFxAndUi() {

        try {
            int audioSessionid = mIMusicPlayerService.getAudioSessionId();
            mVisualizer = new Visualizer(audioSessionid);
            // 参数内必须是2的位数
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            // 设置允许波形表示，并且捕获它
            mBaseVisualizerView.setVisualizer(mVisualizer);
            mVisualizer.setEnabled(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void initBindService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIMusicPlayerService = IMusicPlayerService.Stub.asInterface(service);
            if (mIMusicPlayerService != null) {
                try {
                    if (!mNotification) {
                        mIMusicPlayerService.openAudio(mPosition);
                    }
                    mHandler.sendEmptyMessage(UPDATA_DATA_FOR_UI);
                    mHandler.sendEmptyMessage(UPDATA_LYRIC);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mIMusicPlayerService != null) {
                try {
                    mIMusicPlayerService.stop();
                    mIMusicPlayerService = null;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIMusicPlayerService != null) {
            unbindService(mConnection);
        }
        if (mUpdataNameAndArtistReceiver != null) {
            unregisterReceiver(mUpdataNameAndArtistReceiver);
        }
        mHandler.removeCallbacksAndMessages(null);
    }
}
