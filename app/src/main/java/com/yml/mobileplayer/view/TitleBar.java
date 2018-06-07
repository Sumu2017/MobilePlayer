package com.yml.mobileplayer.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yml.mobileplayer.R;

public class TitleBar extends LinearLayout implements View.OnClickListener {

    public TitleBar(Context context) {
        super(context);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private View tv_search,rl_game,iv_record;


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tv_search=findViewById(R.id.tv_search);
        rl_game=findViewById(R.id.rl_game);
        iv_record=findViewById(R.id.iv_record);
        tv_search.setOnClickListener(this);
        rl_game.setOnClickListener(this);
        iv_record.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_search:
                Toast.makeText(getContext(),"search",Toast.LENGTH_SHORT).show();
                break;
            case R.id.rl_game:
                Toast.makeText(getContext(),"game",Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_record:
                Toast.makeText(getContext(),"record",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
