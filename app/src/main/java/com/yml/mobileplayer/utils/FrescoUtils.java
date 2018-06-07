package com.yml.mobileplayer.utils;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

public class FrescoUtils {

    //对Hierarchy进行设置，如各种状态下显示的图片
    public static void setHierarchay(GenericDraweeHierarchy hierarchy) {
        if (hierarchy != null) {
            //重新加载显示的图片
           // hierarchy.setRetryImage(retryImage);
            //加载失败显示的图片
          //  hierarchy.setFailureImage(failureImage, ScalingUtils.ScaleType.CENTER_CROP);
            //加载完成前显示的占位图
          //  hierarchy.setPlaceholderImage(placeholderImage, ScalingUtils.ScaleType.CENTER_CROP);
            //设置加载成功后图片的缩放模式
          //  hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);

            //显示加载进度条，使用自带的new ProgressBarDrawable()
            //默认会显示在图片的底部，可以设置进度条的颜色。
            //hierarchy.setProgressBarImage(new ProgressBarDrawable());
        }
    }

    /**
     * 加载图片核心方法
     *
     * @param simpleDraweeView              图片加载控件
     * @param uri                           图片加载地址

     */
    public static void loadImage(SimpleDraweeView simpleDraweeView, Uri uri) {
        //设置Hierarchy
        setHierarchay(simpleDraweeView.getHierarchy());
        //构建并获取ImageRequest
        ImageRequest imageRequest = getImageRequest(uri, simpleDraweeView);
        //构建并获取Controller
        DraweeController draweeController = getController(imageRequest, simpleDraweeView.getController());
        //开始加载
        simpleDraweeView.setController(draweeController);
    }

    /**
     * 构建、获取ImageRequest
     * @param uri 加载路径
     * @param simpleDraweeView 加载的图片控件
     * @return ImageRequest
     */
    public static ImageRequest getImageRequest(Uri uri, SimpleDraweeView simpleDraweeView) {

        int width;
        int height;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            width = simpleDraweeView.getWidth();
            height = simpleDraweeView.getHeight();
        } else {
            width = simpleDraweeView.getMaxWidth();
            height = simpleDraweeView.getMaxHeight();
        }

        //根据请求路径生成ImageRequest的构造者
        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        //调整解码图片的大小
//        if (width > 0 && height > 0) {
//            builder.setResizeOptions(new ResizeOptions(width, height));
//        }
        //设置是否开启渐进式加载，仅支持JPEG图片
        builder.setProgressiveRenderingEnabled(true);
        return builder.build();
    }

    public static DraweeController getController(ImageRequest request, @Nullable DraweeController oldController) {
        PipelineDraweeControllerBuilder builder = Fresco.newDraweeControllerBuilder();
        builder.setImageRequest(request);//设置图片请求
        builder.setTapToRetryEnabled(false);//设置是否允许加载失败时点击再次加载
        builder.setAutoPlayAnimations(true);//设置是否允许动画图自动播放
        builder.setOldController(oldController);
        return builder.build();
    }
}
