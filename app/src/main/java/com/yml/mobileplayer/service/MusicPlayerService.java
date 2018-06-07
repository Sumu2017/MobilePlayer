package com.yml.mobileplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.yml.mobileplayer.IMusicPlayerService;
import com.yml.mobileplayer.R;
import com.yml.mobileplayer.activity.AudioPlayActivity;
import com.yml.mobileplayer.bean.Lyric;
import com.yml.mobileplayer.bean.MediaItem;
import com.yml.mobileplayer.utils.CacheUtils;
import com.yml.mobileplayer.utils.LyricUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private IMusicPlayerService.Stub mStub=new IMusicPlayerService.Stub() {
        MusicPlayerService mMusicPlayerService=MusicPlayerService.this;

        @Override
        public void openAudio(int position) throws RemoteException {
            mMusicPlayerService.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            mMusicPlayerService.start();
        }

        @Override
        public void pause() throws RemoteException {
            mMusicPlayerService.pause();
        }

        @Override
        public void stop() throws RemoteException {
            mMusicPlayerService.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return mMusicPlayerService.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return mMusicPlayerService.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException {
            return mMusicPlayerService.getArtist();
        }

        @Override
        public String getName() throws RemoteException {
            return mMusicPlayerService.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return mMusicPlayerService.getAudioPath();
        }

        @Override
        public void next() throws RemoteException {
            mMusicPlayerService.next();
        }

        @Override
        public void pre() throws RemoteException {
            mMusicPlayerService.pre();
        }

        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            mMusicPlayerService.setPlayMode(playMode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return mMusicPlayerService.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mMusicPlayerService.isPlaying();
        }

        @Override
        public void seekTo(int seek) throws RemoteException {
            mMusicPlayerService.seekTo(seek);
        }

        @Override
        public List<Lyric> getLyrics() throws RemoteException {
            return mMusicPlayerService.getLyrics();
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return mMusicPlayerService.getAudioSessionId();
        }
    };
    private ArrayList<MediaItem> mMediaItems;
    private int mPosition=-1;
    private MediaPlayer mMediaPlayer;
    private MediaItem mMediaItem;
    public static final String UPDATA_AUDIO_NAME_ARTIST="com.sumu.updata_audio_name_artist";
    private NotificationManager mManager;
    public static final int REPEATE_NORMAL=0x01;//默认播放，顺序播放
    public static final int REPEATE_SINGLE=0x02;//单曲循环
    public static final int REPEATE_ALL=0x03;//全部循环
    private int mCurrentMode=REPEATE_NORMAL;//当前播放模式
    private List<Lyric> mLyrics;//当前歌曲的歌词列表
    private int mAudioSessionId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getDataFromLocal();
        mCurrentMode=CacheUtils.getPlaymode(this,"playMode");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void openAudio(int position){
        mPosition=position;
        if (mMediaItems!=null&&mMediaItems.size()>0){
            mMediaItem = mMediaItems.get(position);
            if (mMediaPlayer!=null){
                mMediaPlayer.reset();
            }
            initMediaPlayer();
        }else {
            Toast.makeText(this,"没有音频或者数据未准备好",Toast.LENGTH_SHORT).show();
        }
    }

    private void  initNotificationManager(){
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent=new Intent(this, AudioPlayActivity.class);
        intent.putExtra("position",1000);
        intent.putExtra("notification",true);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification=new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("321音乐")
                .setContentText("正在播放："+getName())
                .setContentIntent(pendingIntent)
                .build();
        notification.flags=Notification.FLAG_ONGOING_EVENT;//点击不消失
        mManager.notify(0x01,notification);
    }

    private void initMediaPlayer(){
        mMediaPlayer=new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        if (mMediaItem!=null) {
            try {
                mMediaPlayer.setDataSource(mMediaItem.getData());
                mMediaPlayer.prepareAsync();
                if (mCurrentMode==REPEATE_SINGLE){
                    mMediaPlayer.setLooping(true);
                }else {
                    mMediaPlayer.setLooping(false);
                }
                readLyrics();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void readLyrics(){
        if (mLyrics!=null){
            mLyrics.clear();
            mLyrics=null;
        }
        String audioPath = getAudioPath();
        int index = audioPath.lastIndexOf(".");
        if (index!=-1) {
            String lyrPath = audioPath.substring(0, index) + ".lrc";
            mLyrics = LyricUtils.readLyricFile(new File(lyrPath));
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        start();
        sendUpdataNameAndArtist();
    }

    private void sendUpdataNameAndArtist() {
        Intent intent=new Intent(UPDATA_AUDIO_NAME_ARTIST);
        sendBroadcast(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        next();
        return true;
    }


    /**
     * 播放
     */
    public void start(){
        if (mMediaPlayer!=null){
            mMediaPlayer.start();
            initNotificationManager();
        }
    }

    /**
     * 暂停
     */
    public void pause(){
        if (mMediaPlayer!=null){
            mMediaPlayer.pause();
            mManager.cancel(0x01);
        }
    }

    /**
     * 停止
     */
    public void stop(){
        if (mMediaPlayer!=null){
            mMediaPlayer.stop();
        }
    }

    /**
     * 获取当前进度
     */
    public int getCurrentPosition(){
        int currentPosition=0;
        if (mMediaPlayer!=null){
           currentPosition=mMediaPlayer.getCurrentPosition();
        }
        return currentPosition;
    }

    /**
     * 获取总时长
     * @return
     */
    private int getDuration(){
        int duration=0;
        if (mMediaPlayer!=null){
            duration=mMediaPlayer.getDuration();
        }
        return duration;
    }

    /**
     * 得到艺术家
     * @return
     */
    private String getArtist(){
        if (mMediaItem!=null){
            return mMediaItem.getArtist();
        }
        return "";
    }

    /**
     * 获取歌曲名字
     * @return
     */
    private String getName(){
        if (mMediaItem!=null){
            return mMediaItem.getName();
        }
        return "";
    }

    /**
     * 获取歌曲路径
     * @return
     */
    private String getAudioPath(){
        if (mMediaItem!=null){
            return mMediaItem.getData();
        }
        return "";
    }

    private void next(){
        int position=mPosition;
        if (mCurrentMode==REPEATE_NORMAL){
            position++;
            if (position<mMediaItems.size()){
                openAudio(position);
            }else {
                openAudio(mMediaItems.size()-1);
            }
//        }else if (mCurrentMode==REPEATE_SINGLE){
//            openAudio(position);
        }else if (mCurrentMode==REPEATE_ALL||mCurrentMode==REPEATE_SINGLE){
            position++;
            if (position>mMediaItems.size()-1){
                position=0;
            }
            openAudio(position);
        }
    }

    private void pre(){
        int position=mPosition;
        if (mCurrentMode==REPEATE_NORMAL){
            position--;
            if (position>=0){
                openAudio(position);
            }else {
                openAudio(0);
            }
//        }else if (mCurrentMode==REPEATE_SINGLE){
//            openAudio(position);
        }else if (mCurrentMode==REPEATE_ALL||mCurrentMode==REPEATE_SINGLE){
            position--;
            if (position<0){
                position=mMediaItems.size()-1;
            }
            openAudio(position);
        }
    }

    /**
     * 设置播放模式
     */
    private void setPlayMode(int playMode){
        this.mCurrentMode=playMode;
        CacheUtils.putPlaymode(this,"playMode",mCurrentMode);
        if (mCurrentMode==REPEATE_SINGLE){
            mMediaPlayer.setLooping(true);
        }else {
            mMediaPlayer.setLooping(false);
        }
    }

    /**
     * 获取播放模式
     * @return
     */
    private int getPlayMode(){
        return mCurrentMode;
    }

    private boolean isPlaying(){
       if (mMediaPlayer!=null){
           return mMediaPlayer.isPlaying();
       }
       return false;
    }

    private void seekTo(int seek){
        if (mMediaPlayer!=null){
            mMediaPlayer.seekTo(seek);
        }
    }

    public List<Lyric> getLyrics() {
        return mLyrics;
    }

    public void getDataFromLocal() {
        mMediaItems=new ArrayList<>();
        new Thread() {
            @Override
            public void run() {
                super.run();
                ContentResolver resolver = getContentResolver();
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
                        mMediaItems.add(mediaItem);
                    }
                    cursor.close();
                }
            }
        }.start();
    }


    public int getAudioSessionId() {
        if (mMediaPlayer!=null){
            return mMediaPlayer.getAudioSessionId();
        }
        return -1;
    }
}
