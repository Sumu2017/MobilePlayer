package com.yml.mobileplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.yml.mobileplayer.bean.Lyric;
import com.yml.mobileplayer.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

public class ShowLyricView extends AppCompatTextView {


    private int mWidth;
    private int mHeight;
    private int mLyricHeight;
    private int mIndex=0;//歌曲播放的位置
    private int mCurrentPosition;
    private long mTimePoint;
    private long mSleepTime;

    public ShowLyricView(Context context) {
        this(context,null);
    }

    public ShowLyricView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ShowLyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
        initPaint();
    }
    private List<Lyric> mLyrics;
    private Paint mHighlightPaint;
    private Paint mWhilePaint;

    private void initPaint(){
        mHighlightPaint=new Paint();
        mHighlightPaint.setTextSize(DensityUtil.dip2px(getContext(),16));
        mHighlightPaint.setColor(Color.BLUE);
        mHighlightPaint.setAntiAlias(true);
        mHighlightPaint.setTextAlign(Paint.Align.CENTER);

        mWhilePaint=new Paint();
        mWhilePaint.setTextSize(DensityUtil.dip2px(getContext(),16));
        mWhilePaint.setColor(Color.WHITE);
        mWhilePaint.setAntiAlias(true);
        mWhilePaint.setTextAlign(Paint.Align.CENTER);

    }

    private void initData() {
        mLyricHeight=DensityUtil.dip2px(getContext(),20);
        mLyrics=new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLyricView(canvas);
    }

    private void drawLyricView(Canvas canvas) {
        if (mLyrics!=null&&!mLyrics.isEmpty()){

            float plush=0;

            if (mSleepTime==0){
                plush=0;
            }else {
                //移动的距离
                float delta=(mLyricHeight*1.0f/mSleepTime)*(mCurrentPosition-mTimePoint);
                plush=delta;
            }

            canvas.translate(0,-plush);

            //中间高亮那句
            String highlightContent = mLyrics.get(mIndex).getContent();
            canvas.drawText(highlightContent,mWidth/2,mHeight/2,mHighlightPaint);
            //高亮前面部分
            for (int i=mIndex-1;i>=0;i--){
                int lyricY=mHeight/2-mLyricHeight*(mIndex-i);
                if (lyricY<0){
                    break;
                }
                String normalContent = mLyrics.get(i).getContent();
                canvas.drawText(normalContent,mWidth/2,lyricY,mWhilePaint);
            }
            //高亮后面部分
            for (int i=mIndex+1;i<mLyrics.size();i++){
                int lyricY=mHeight/2+mLyricHeight*(i-mIndex);
                if (lyricY<0){
                    break;
                }
                String normalContent = mLyrics.get(i).getContent();
                canvas.drawText(normalContent,mWidth/2,lyricY,mWhilePaint);
            }
        }else {
            //没有歌词
            canvas.drawText("没有找到歌词。。。",mWidth/2,mHeight/2,mHighlightPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    public void setCurrentPosition(int currentPosition) {
        mCurrentPosition = currentPosition;
        if (mLyrics!=null&&!mLyrics.isEmpty()){
            for (int i=1;i<mLyrics.size();i++){
                //将当前时间与每一个歌词时间戳进行对比
                if (mCurrentPosition<mLyrics.get(i).getTimePoint()){
                    if (mCurrentPosition>=mLyrics.get(i-1).getTimePoint()){
                        mIndex=i-1;
                        mTimePoint = mLyrics.get(mIndex).getTimePoint();
                        mSleepTime = mLyrics.get(mIndex).getSleepTime();
                        break;
                    }
                }
            }
        }
        invalidate();
    }

    /**
     * 设置歌词数据
     * @param lyrics
     */
    public void setLyrics(List<Lyric> lyrics) {
        mLyrics = lyrics;
    }

    public void setIndex(int index) {
        mIndex = index;
    }
}
