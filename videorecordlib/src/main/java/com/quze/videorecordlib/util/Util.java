package com.quze.videorecordlib.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * Description :
 * PackageName : com.quze.videorecordlib.util
 * Created by mrtrying on 2018/8/16 15:23.
 * e_mail : ztanzeyu@gmail.com
 */
public class Util {
    /**
     * 获得本地视频第一帧
     *
     * @param context
     */
    public static Bitmap getFirstVideoBitmap(Context context) {
        String[] mediaColumns = new String[]{MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE, MediaStore.Video.Media.MIME_TYPE
        };
        //首先检索SDcard上所有的video
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaColumns, null, null, null);
        String filePath = "";
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        }
        cursor.close();
        if (TextUtils.isEmpty(filePath)) {
            return null;
        } else {
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(filePath);// videoPath 本地视频的路径
            return media.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
