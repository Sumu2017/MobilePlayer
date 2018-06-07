// IMusicPlayerService.aidl
package com.yml.mobileplayer;
import com.yml.mobileplayer.bean.Lyric;

// Declare any non-default types here with import statements

interface IMusicPlayerService {
      void  openAudio(int position);
   /**
        * 播放
        */
      void start();

       /**
        * 暂停
        */
      void pause();

       /**
        * 停止
        */
      void stop();

       /**
        * 获取当前进度
        */
      int getCurrentPosition();

       /**
        * 获取总时长
        * @return
        */
        int getDuration();

       /**
        * 得到艺术家
        * @return
        */
        String getArtist();

       /**
        * 获取歌曲名字
        * @return
        */
        String getName();
       /**
        * 获取歌曲路径
        * @return
        */
        String getAudioPath();

        void next();

        void pre();

       /**
        * 设置播放模式
        */
        void setPlayMode(int playMode);

       /**
        * 获取播放模式
        * @return
        */
        int getPlayMode();


        boolean isPlaying();

        void seekTo(int seek);


        List<Lyric> getLyrics();

        int getAudioSessionId();
}
