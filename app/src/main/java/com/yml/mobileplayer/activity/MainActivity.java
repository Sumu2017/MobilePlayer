package com.yml.mobileplayer.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yml.mobileplayer.Fragment.BaseFragment;
import com.yml.mobileplayer.R;
import com.yml.mobileplayer.base.BasePager;
import com.yml.mobileplayer.pager.AudioPager;
import com.yml.mobileplayer.pager.NetAudioPager;
import com.yml.mobileplayer.pager.NetVideoPager;
import com.yml.mobileplayer.pager.VideoPager;

import java.util.ArrayList;
import java.util.List;

import cn.jzvd.JZVideoPlayer;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends FragmentActivity implements EasyPermissions.PermissionCallbacks{
    private FrameLayout fl_main_content;
    private RadioGroup rg_bottom_tab;
    private List<BasePager> mBasePagers;
    private TextView mTvSearch;

    private int position=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fl_main_content=findViewById(R.id.fl_main_content);
        rg_bottom_tab=findViewById(R.id.rg_bottom_tab);
        mTvSearch=findViewById(R.id.tv_search);
        mBasePagers=new ArrayList<>();
        mBasePagers.add(new VideoPager(this));
        mBasePagers.add(new AudioPager(this));
        mBasePagers.add(new NetVideoPager(this));
        mBasePagers.add(new NetAudioPager(this));
        rg_bottom_tab.setOnCheckedChangeListener(new MyCheckedChangeListener());
        EasyPermissions.requestPermissions(this,"允许读取手机文件",0, Manifest.permission.READ_EXTERNAL_STORAGE);

        mTvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //申请成功时调用
        rg_bottom_tab.check(R.id.rb_video);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        //申请失败时调用
        finish();
    }

    private class MyCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                case R.id.rb_video:
                    position=0;
                    break;
                case R.id.rb_audio:
                    position=1;
                    break;
                case R.id.rb_net_video:
                    position=2;
                    break;
                case R.id.rb_net_audio:
                    position=3;
                    break;
            }
            
            setFragment();
        }
    }

    private void setFragment() {
        final FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        BasePager basePager = getBasePager();
        transaction.replace(R.id.fl_main_content, BaseFragment.newInstance(basePager));
        transaction.commit();
    }

    private BasePager getBasePager() {
        BasePager basePager = mBasePagers.get(position);
        if (basePager!=null&&!basePager.isInit()){
            basePager.initData();
        }
        return basePager;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 是否已经退出
     */
    private boolean isExit = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode ==KeyEvent.KEYCODE_BACK){
            if(position != 0){//不是第一页面
                position = 0;
                rg_bottom_tab.check(R.id.rb_video);//首页
                return true;
            }else  if(!isExit){
                isExit = true;
                Toast.makeText(MainActivity.this,"再按一次推出",Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit  = false;
                    }
                },2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (JZVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }
    @Override
    protected void onPause() {
        super.onPause();
        JZVideoPlayer.releaseAllVideos();
    }
}
