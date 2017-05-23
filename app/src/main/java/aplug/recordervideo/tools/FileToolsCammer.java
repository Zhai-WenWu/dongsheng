package aplug.recordervideo.tools;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import aplug.recordervideo.activity.RecorderActivity;
import aplug.recordervideo.db.RecorderVideoData;
import aplug.recordervideo.view.RecorderVideoPreviewView;

import static aplug.recordervideo.db.RecorderVideoSqlite.getInstans;

/**
 * Created by Fang Ruijiao on 2016/10/24.
 */
public class FileToolsCammer {

    private static boolean isLoading = false;
    public static String VIDEO_CATCH = "videoCatch/";

    public static void loadCammerAllData(final OnCammerFileListener listener, final boolean isDelete, final boolean isReload){
        if(isLoading) return;
        isLoading = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Map<String,String>> orderArrayList = new ArrayList<>();
                final ArrayList<Map<String,String>> olddArrayList = new ArrayList<>();
                if(isReload){
                    doLoadFiles(true);
                }
                ArrayList<Map<String,String>> videoList = getInstans().getAllIngDataInDB();
                File videoFile;
                for(Map<String,String> map : videoList){
                    String videoPath = map.get(RecorderVideoData.video_path);
                    if(TextUtils.isEmpty(videoPath)){
                        getInstans().deleteById(map.get(RecorderVideoData.video_id));
                        continue;
                    }
                    videoFile = new File(videoPath);
                    if(videoFile.exists()){
                        olddArrayList.add(map);
                        String imgPath = map.get(RecorderVideoData.video_img_path);
                        File imgFile = new File(imgPath);
                        if(!imgFile.exists()){
                            FileToolsCammer.getBitmapByImgPath(videoPath);
                        }
                    }else{
                        getInstans().deleteById(map.get(RecorderVideoData.video_id));
                    }
                }
                order(olddArrayList,orderArrayList);
                Map<String,String> map;
                for(int i = 0; i < orderArrayList.size(); i++){
                    map = orderArrayList.get(i);
                    map.put(RecorderVideoData.video_state, RecorderVideoPreviewView.DEFAULT);
                    map.put(RecorderVideoData.video_isDelete,String.valueOf(isDelete));
                }
                listener.loadOver(orderArrayList);
                isLoading = false;
            }
        }).start();
    }

    private static void doLoadFiles(boolean isReload){
        if(isReload || getInstans().getDataSize() == 0) {
            ArrayList<Map<String, String>> olddArrayList = new ArrayList<>();
            selectFils(olddArrayList);
//            Log.i("FRJ", "select size:" + olddArrayList.size());
            getInstans().resetAll(olddArrayList);
        }
    }

    /**
     * 按照时间分割排序
     */
    private static void order(ArrayList<Map<String,String>> olddArrayList,ArrayList<Map<String,String>> orderArrayList){
        int size = 0;
        Map<String,String> map1,map2,map3;
        for(int i = 0; i < olddArrayList.size(); i++ ){
            map1  = olddArrayList.get(i);
            long videoAddTime1 = Long.parseLong(map1.get(RecorderVideoData.video_add_time));
            if(i == 0){
                map1.put(RecorderVideoData.video_time, Tools.getFormatedDateTime("MM月dd日HH时",videoAddTime1));
                orderArrayList.add(map1);
                size ++;
            }
            if(i + 1 < olddArrayList.size()){
                map2  = olddArrayList.get(i + 1);
                map2.put(RecorderVideoData.video_time,"");
                long videoAddTime2 = Long.parseLong(map2.get(RecorderVideoData.video_add_time));
                //间隔在6小时内
                if(videoAddTime1 - videoAddTime2 <  6 * 60 * 60 * 1000){
                    orderArrayList.add(map2);
                    size ++;
                }else{
                    if(size % 2 == 1){
                        map3 = new HashMap<>();
                        map3.put(RecorderVideoData.video_add_time,"");
                        map3.put(RecorderVideoData.video_time,"");
                        orderArrayList.add(map3);
                        size ++;
                    }
                    map2.put(RecorderVideoData.video_time,Tools.getFormatedDateTime("MM月dd日HH时",videoAddTime2));
                    orderArrayList.add(map2);
                    size ++;
                }
            }
            map2 = null;
            map3 = null;
        }
    }

    private static void selectFils(ArrayList<Map<String,String>> olddArrayList){
        //读取本地文件，加载数据
        File cacheFile = new File(RecorderActivity.parentPath);
        File[] dirAboutList = cacheFile.listFiles();
        if(dirAboutList == null || dirAboutList.length == 0) return;
        List<File> arrayList = new ArrayList<>();
        for(File file : dirAboutList){
            if(file.length() > 1024){ //文件大小要大于1K
                String fileName=file.getName();
                int index = fileName.lastIndexOf(".") + 1;
                if(index < fileName.length()){
                    String prefix=fileName.substring(index);
                    if("mp4".equals(prefix) || "MP4".equals(prefix))
                        arrayList.add(file);
                }
            }
        }
        SortFile comp = new SortFile();
        Collections.sort(arrayList,comp);

        for (File file : arrayList) {
            String videoPath = file.getAbsolutePath();
            Map<String,String> map = new HashMap<>();
            map = data(videoPath);
            olddArrayList.add(0, map);

//                Map<String, String> map = RecorderVideoSqlite.getInstans().selectIdByPath(videoPath);
//                if(map.size() < 4){
//                    map = data(videoPath);
//                }else if(!new File(map.get(RecorderVideoData.video_img_path)).exists()){
//                    String bitmapName = StringManager.toMD5(videoPath,false);
//                    String bitmapPath = FileManager.getSDDir() + VIDEO_CATCH + bitmapName + ".jpg";
//                    map.put(RecorderVideoData.video_img_path,bitmapPath);
//                    if(!new File(bitmapPath).exists()){
//                        Bitmap bitmap = getBitmapByImgPath(videoPath);
//                        if(bitmap == null || bitmap.getWidth() / 16.0 * 9 != bitmap.getHeight()){
//                            map = new HashMap<>();
//                        }
//                    }
//                }
//                if(map.size() > 4) {
//                    map.put(RecorderVideoData.video_time, "");
//                    olddArrayList.add(0, map);
//                }
        }
    }

    public static Map<String, String> data(String videoPath){
        File file = new File(videoPath);
        Map<String, String> map = new HashMap<>();
        if(!file.exists()) return map;

        map.put(RecorderVideoData.video_add_time,"" + file.lastModified());
        float longTime = ToolsCammer.getLongTime(videoPath);
        String bitmapName = StringManager.toMD5(videoPath,false);
        String bitmapPath = FileManager.getSDDir() + VIDEO_CATCH + bitmapName + ".jpg";
        if(!new File(bitmapPath).exists()){
            Bitmap bitmap = getBitmapByImgPath(videoPath);
            if(bitmap == null || bitmap.getWidth() / 16.0 * 9 != bitmap.getHeight()){
                return new HashMap<>();
            }
        }
        int minu = (int) (longTime / 60);
        int secon = (int)(longTime - minu);
        if(minu == 0 && secon == 0)secon = 1;
        String minuStr = String.valueOf(minu);
        String seconStr = String.valueOf(secon);
        if(minu < 10){
            minuStr = "0" + minu;
        }
        if(secon < 10){
            seconStr = "0" + secon; ;
        }
        map.put(RecorderVideoData.video_long_time, String.valueOf(longTime));
        map.put(RecorderVideoData.video_show_time, minuStr + ":" + seconStr);
        map.put(RecorderVideoData.video_path,videoPath);
        map.put(RecorderVideoData.video_img_path,bitmapPath);
        return map;
    }

    public static Bitmap getBitmapByImgPath(String videoPath){
        if(TextUtils.isEmpty(videoPath)) return null;
        Bitmap bitmap = null;
        String bitmapName = StringManager.toMD5(videoPath,false);
        String imgPath = FileManager.getSDDir() + VIDEO_CATCH + bitmapName + ".jpg";
        if(new File(videoPath).exists()) {
            if (new File(imgPath).exists()) {
                bitmap = BitmapFactory.decodeFile(imgPath);
            } else {
                bitmap = ToolsCammer.getFrameAtTime(videoPath);
                FileManager.saveImgToCompletePath(bitmap, imgPath, Bitmap.CompressFormat.JPEG);
            }
        }
        return bitmap;
    }

    public static String getImgPath(String videoPath){
        if(TextUtils.isEmpty(videoPath)) return null;
        String imgPath = null;
        //如果有辞视频，但是没有次视频的封面图
        if(new File(videoPath).exists()) {
            String bitmapName = StringManager.toMD5(videoPath,false);
            imgPath = FileManager.getSDDir() + VIDEO_CATCH + bitmapName + ".jpg";
            if(!new File(imgPath).exists()) {
                Bitmap bitmap = ToolsCammer.getFrameAtTime(videoPath);
                FileManager.saveImgToCompletePath(bitmap, imgPath, Bitmap.CompressFormat.JPEG);
            }
        }
        return imgPath;
    }

    /**
     * 将拍下来的照片存放在SD卡中
     * @param data
     * @throws IOException
     */
    public static void saveToSDCard(Context con,byte[] data) throws IOException {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
        String filename = format.format(date) + ".jpg";
        File fileFolder = new File(FileManager.getSDDir() + VIDEO_CATCH);
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
            fileFolder.mkdir();
        }
        File jpgFile = new File(fileFolder, filename);
        FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流
        outputStream.write(data); // 写入sd卡中
        outputStream.close(); // 关闭输出流
        save2Xiangce(con,jpgFile.getAbsolutePath());
    }

    private static void save2Xiangce(Context con,String path){
        try {
            // 把文件插入到系统图库
            MediaStore.Images.Media.insertImage(con.getContentResolver(), path, "", "");
            // 最后通知图库更新
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                MediaScannerConnection.scanFile(con, new String[]{Environment.getExternalStorageDirectory().getPath()}, null, null);
            } else {
                con.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //删除过度文件
        FileManager.delDirectoryOrFile(path);
    }

    public interface OnCammerFileListener{
        public void loadOver(ArrayList<Map<String,String>> orderArrayLis);
    }

    public static String formatVideoTimeByMills(long mills) {
        if (mills <= 0)
            return "00:00";
        int hours = (int) (mills / (1000 * 60 * 60));
        int mins = (int) ((mills - hours * 60 * 60 * 1000) / (1000 * 60));
        int secos = (int) ((mills - hours * 60 * 60 * 1000 - mins * 60 * 1000) / 1000);
        StringBuffer buffer = new StringBuffer();
        if (hours >= 10)
            buffer.append(hours + ":");
        else if (hours > 0)
            buffer.append("0" + hours + ":");
        if (mins >= 10)
            buffer.append(mins + ":");
        else if (mins > 0)
            buffer.append("0" + mins + ":");
        else
            buffer.append("00:");
        if (secos >= 10)
            buffer.append(secos);
        else if (secos > 0)
            buffer.append("0" + secos);
        else
            buffer.append("00");
        return buffer.toString();
    }

    /**
     * 获取本地所有视频
     */
    public static ArrayList<Map<String, String>> getLocalMedias() {
        ArrayList<Map<String, String>> videos = new ArrayList<Map<String, String>>();
        Cursor cursor = XHApplication.in().getApplicationContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Map<String, String> map = new HashMap<String, String>();
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)); // id
                String title =cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM)); // 专辑
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)); // 艺术家
                String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)); // 显示名称
                String mimeType =cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)); // 路径
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)); // 时长
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)); // 大小
                String resolution =cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION));//分辨率
                map.put(MediaStore.Video.Media._ID, String.valueOf(id));
                map.put(MediaStore.Video.Media.TITLE, title);
                map.put(MediaStore.Video.Media.ALBUM, album);
                map.put(MediaStore.Video.Media.ARTIST, artist);
                map.put(MediaStore.Video.Media.DISPLAY_NAME, displayName);
                map.put(MediaStore.Video.Media.MIME_TYPE, mimeType);
                map.put(MediaStore.Video.Media.DATA, data);
                map.put(MediaStore.Video.Media.DURATION, String.valueOf(duration));
                map.put(MediaStore.Video.Media.SIZE, String.valueOf(size));
                map.put(MediaStore.Video.Media.RESOLUTION, resolution);
                videos.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            return videos;
        }
    }

}
