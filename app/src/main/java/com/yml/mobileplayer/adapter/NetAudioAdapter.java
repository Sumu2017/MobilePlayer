package com.yml.mobileplayer.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.yml.mobileplayer.R;
import com.yml.mobileplayer.bean.NetAudioPageBean;
import com.yml.mobileplayer.utils.FrescoUtils;
import com.yml.mobileplayer.utils.Utils;

import org.xutils.common.util.DensityUtil;
import org.xutils.x;

import java.util.List;

import cn.jzvd.JZVideoPlayerStandard;

public class NetAudioAdapter extends BaseAdapter {

    private Context mContext;
    private List<NetAudioPageBean.ListBean> mDatas;

    private static final int TYPE_VIDEO = 0;//视频
    private static final int TYPE_GIT = 1;//git
    private static final int TYPE_IMAGE = 2;//image
    private static final int TYPE_TEXT = 3;//文本内容

    private Utils mUtils;

    public NetAudioAdapter(Context context, List<NetAudioPageBean.ListBean> datas) {
        mContext = context;
        mDatas = datas;
        mUtils=new Utils();
    }

    public void refreshData(List<NetAudioPageBean.ListBean> datas) {
        if (!datas.isEmpty() && datas.size() > 0) {
            // mMediaItems.clear();
            //mMediaItems.addAll(mediaItems);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        int result = TYPE_TEXT;
        NetAudioPageBean.ListBean data = mDatas.get(position);
        if (data.getType().equals("video")) {
            result = TYPE_VIDEO;
        } else if (data.getType().equals("gif")) {
            result = TYPE_GIT;
        } else if (data.getType().equals("image")) {
            result = TYPE_IMAGE;
        } else if (data.getType().equals("text")) {
            result = TYPE_TEXT;
        }
        return result;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    int screenWidth=DensityUtil.getScreenWidth()-DensityUtil.dip2px(20);
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.net_audio_item_video, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        NetAudioPageBean.ListBean data = mDatas.get(position);
        int type = getItemViewType(position);
        if (type == TYPE_IMAGE) {
            //设置图片类型主内容
            holder.sdv_iv_git_content.setVisibility(View.VISIBLE);
            holder.rl_video.setVisibility(View.GONE);
            NetAudioPageBean.ListBean.ImageBean dataImage = data.getImage();
            ViewGroup.LayoutParams params = holder.sdv_iv_git_content.getLayoutParams();
            int  height = dataImage.getHeight()<= DensityUtil.getScreenHeight()*0.75?dataImage.getHeight(): (int) (DensityUtil.getScreenHeight() * 0.75);
            params.width = screenWidth;
            params.height = height;
            holder.sdv_iv_git_content.setLayoutParams(params);
            FrescoUtils.loadImage(holder.sdv_iv_git_content, Uri.parse(dataImage.getDownload_url().get(0)));
            //holder.sdv_iv_git_content.setImageURI(dataImage.getDownload_url().get(0));
        } else if (type == TYPE_GIT) {
            //设置GIF类型主内容
            holder.sdv_iv_git_content.setVisibility(View.VISIBLE);
            holder.rl_video.setVisibility(View.GONE);
            NetAudioPageBean.ListBean.GifBean gifBean = data.getGif();
            ViewGroup.LayoutParams params = holder.sdv_iv_git_content.getLayoutParams();
            int scale= (int) (screenWidth/(gifBean.getWidth()*1.0f));

            params.width = screenWidth;
            params.height = gifBean.getHeight()*scale;
            holder.sdv_iv_git_content.setLayoutParams(params);
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(gifBean.getImages().get(0))
                    .setAutoPlayAnimations(true)
                    .build();
            holder.sdv_iv_git_content.setController(controller);
        }else if (type==TYPE_TEXT){
            holder.sdv_iv_git_content.setVisibility(View.GONE);
            holder.rl_video.setVisibility(View.GONE);
        }else if (type==TYPE_VIDEO){
            holder.sdv_iv_git_content.setVisibility(View.GONE);
            holder.rl_video.setVisibility(View.VISIBLE);
            NetAudioPageBean.ListBean.VideoBean video = data.getVideo();
            ViewGroup.LayoutParams params = holder.videoplayer.getLayoutParams();
            int scale= (int) (screenWidth/(video.getWidth()*1.0f));

            params.width = screenWidth;
            params.height = video.getHeight()*scale;
            holder.sdv_iv_git_content.setLayoutParams(params);

            holder.videoplayer.setUp(data.getVideo().getVideo().get(0),JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL,
                    "");
            x.image().bind(holder.videoplayer.thumbImageView,data.getVideo().getThumbnail().get(0));
            holder.tv_play_number.setText(video.getPlaycount()+"次播放");
            holder.tv_duration.setText(mUtils.stringForTime(video.getDuration()* 1000));
        }
        //设置个人发表人信息模块
        NetAudioPageBean.ListBean.UBean dataU = data.getU();
        holder.head_icon.setImageURI(dataU.getHeader().get(0));
        holder.tv_name.setText(dataU.getName());
        holder.tv_time.setText(data.getPasstime());
        //设置文字内容
        holder.tv_content.setText(data.getText());
        //设置标签
        List<NetAudioPageBean.ListBean.TagsBean> tags = data.getTags();
        if (tags!=null&&!tags.isEmpty()){
            holder.ll_tag.setVisibility(View.VISIBLE);
            StringBuilder builder=new StringBuilder();
            for (int i=0;i<tags.size();i++){
                builder.append(tags.get(i).getName()+" ");
            }
            holder.tv_tag.setText(builder.toString());
        }else {
            holder.ll_tag.setVisibility(View.GONE);
        }
        //设置点赞、踩、分享、评论数、
        holder.tv_shenhe_ding_number.setText(data.getUp());
        holder.tv_shenhe_cai_number.setText(data.getDown()+"");
        holder.tv_posts_number.setText(data.getForward()+"");
        holder.tv_download_number.setText(data.getComment()+"");

        List<NetAudioPageBean.ListBean.TopCommentsBean> top_comments = data.getTop_comments();
        if (top_comments!=null&&!top_comments.isEmpty()){
            holder.ll_top_comments.setVisibility(View.VISIBLE);
            if (top_comments.size()>=2){
                holder.ll_top_comment_two.setVisibility(View.VISIBLE);
                holder.tv_name_two.setText(top_comments.get(1).getU().getName()+":");
                holder.tv_content_two.setText(top_comments.get(1).getContent());
            }else {
                holder.ll_top_comment_two.setVisibility(View.GONE);
            }
            holder.tv_name_one.setText(top_comments.get(0).getU().getName()+":");
            holder.tv_content_one.setText(top_comments.get(0).getContent());
        }else {
            holder.ll_top_comments.setVisibility(View.GONE);
        }
        return convertView;
    }


    static class ViewHolder {
        //个人信息模块
        SimpleDraweeView head_icon;
        TextView tv_name;
        TextView tv_time;
        ImageView iv_right_more;
        //内容区域
        TextView tv_content;
        SimpleDraweeView sdv_iv_git_content;

        RelativeLayout rl_video;
        JZVideoPlayerStandard videoplayer;
        TextView tv_play_number;
        TextView tv_duration;
        //底部区域
        LinearLayout ll_tag;
        TextView tv_tag;

        TextView tv_shenhe_ding_number;
        TextView tv_shenhe_cai_number;
        TextView tv_posts_number;
        TextView tv_download_number;

        //评论模块
        LinearLayout ll_top_comments;

        LinearLayout ll_top_comment_one;
        TextView tv_name_one;
        TextView tv_content_one;

        LinearLayout ll_top_comment_two;
        TextView tv_name_two;
        TextView tv_content_two;

        public ViewHolder(View convertView) {
            head_icon = convertView.findViewById(R.id.head_icon);
            tv_name = convertView.findViewById(R.id.tv_name);
            tv_time = convertView.findViewById(R.id.tv_time);
            iv_right_more = convertView.findViewById(R.id.iv_right_more);

            tv_content = convertView.findViewById(R.id.tv_content);
            sdv_iv_git_content = convertView.findViewById(R.id.sdv_iv_git_content);

            rl_video=convertView.findViewById(R.id.rl_video);
            videoplayer=convertView.findViewById(R.id.videoplayer);
            tv_play_number=convertView.findViewById(R.id.tv_play_number);
            tv_duration=convertView.findViewById(R.id.tv_duration);


            ll_tag = convertView.findViewById(R.id.ll_tag);
            tv_tag = convertView.findViewById(R.id.tv_tag);

            tv_shenhe_ding_number = convertView.findViewById(R.id.tv_shenhe_ding_number);
            tv_shenhe_cai_number = convertView.findViewById(R.id.tv_shenhe_cai_number);
            tv_posts_number = convertView.findViewById(R.id.tv_posts_number);
            tv_download_number = convertView.findViewById(R.id.tv_download_number);

            ll_top_comments = convertView.findViewById(R.id.ll_top_comments);

            ll_top_comment_one = convertView.findViewById(R.id.ll_top_comment_one);
            tv_name_one = convertView.findViewById(R.id.tv_name_one);
            tv_content_one = convertView.findViewById(R.id.tv_content_one);

            ll_top_comment_two = convertView.findViewById(R.id.ll_top_comment_two);
            tv_name_two = convertView.findViewById(R.id.tv_name_two);
            tv_content_two = convertView.findViewById(R.id.tv_content_two);
        }
    }
}
