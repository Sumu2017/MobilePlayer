package com.yml.mobileplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import io.vov.vitamio.widget.VideoView;



public class MyVitamioVideoView extends VideoView {
    public MyVitamioVideoView(Context context) {
        super(context);
    }

    public MyVitamioVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVitamioVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }

    public void setVideoSize(int width, int height){
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width=width;
        params.height=height;
        setLayoutParams(params);
    }
}
