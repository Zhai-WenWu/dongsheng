package amodule.dish.video.control;

import java.util.ArrayList;

import acore.tools.FileManager;
import amodule.dish.video.bean.MediaBean;

/**
 * 视频业务控制---单例模式
 */

public class MediaControl {
    public static String viewPath_cancel= FileManager.getSDDir()+"video/";//缓存路径
    public static String path_img="path_img";//缓存路径
    public static String path_ts= "path_ts";//准备合成视频全部ts文件目录
    public static String path_code= "path_code";//片头片尾处理后的数据存储位置
    public static String path_paper="path_paper";//全部裁剪好的视频目录
    public static String path_paper_text="path_paper_text";//全部裁剪好加字幕的视频目录
    public static String path_sucess="path_sucess";//合成视频的路径
    public static String path_sucess_ts="path_sucess_ts";//合成视频的路径ts
    public static String path_audio="path_audio";//音频路径
    public static String path_pcm="path_pcm";//音频pcm路径
    public static String path_aac="path_aac";//音频aac路径
    public static String path_voide=viewPath_cancel+"xianghaVideo";//片头片尾，背景文件存发路径
    //-------------------------路径viewPath_cancel为缓存路径，其他合成路径为前期有当前草稿箱id,为路径标示。

    //片头命名为：xiangha_start.mp4 片尾命名：xiangha_end.mp4 背景音乐命名：xiangha_bgm.mp3 黑体路径：back.ttf
    public ArrayList<MediaBean> MediaBeans= new ArrayList<>();//数据集合
    private static  MediaControl mediaControl;
    private  MediaControl(){}
    public static MediaControl getInstance(){
        if(mediaControl==null){
            synchronized (MediaControl.class){
                mediaControl= new MediaControl();
            }
        }
        return mediaControl;
    }

    /**
     * 清空全部数据
     */
    public void release(){
    }
    /**
     * 添加数据
     * @param mediaBean
     */
    public void addMediaBean(MediaBean mediaBean){
        MediaBeans.add(mediaBean);
    }


}
