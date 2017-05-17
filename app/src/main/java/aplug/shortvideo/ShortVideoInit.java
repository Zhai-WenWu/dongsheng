package aplug.shortvideo;

import android.content.Context;

import java.io.File;

import acore.tools.FileManager;

/**
 * PackageName : aplug.shortvideo
 * Created by MrTrying on 2016/9/20 17:00.
 * E_mail : ztanzeyu@gmail.com
 */

public class ShortVideoInit {

    public static String path_short= FileManager.getSDDir() + "shortvideo/";
    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context) {
        // 设置拍摄视频缓存路径
//        String dcim = FileManager.getSDDir() + "shortvideo/";
//        VCamera.setVideoCachePath(dcim);
//        // 开启log输出,ffmpeg输出到logcat
//        VCamera.setDebugMode(true);
//        // 初始化拍摄SDK，必须
//        VCamera.initialize(context);
        //清除过期视频
        clearExpireVideo();
    }

    /** 清除过期视频 */
    public static void clearExpireVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File cachePath = new File(path_short + "cache/");
                File[] cacheFiles = cachePath.listFiles();
                if(cacheFiles == null){
                    return;
                }
                //计算成分钟，避免溢出
                final long currentTime = System.currentTimeMillis() / 1000 / 60;
                final long difference = 14 * 24 * 60;
                for (File file : cacheFiles) {
                    long lastModified = file.lastModified() / 1000 / 60;
                    if (currentTime - lastModified >= difference) {
                        delete(file);
                    }
                }
            }
        }).start();
    }

    /**
     * 删除文件加中所有文件以及目录
     * @param file
     */
    private static void delete(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }
}
