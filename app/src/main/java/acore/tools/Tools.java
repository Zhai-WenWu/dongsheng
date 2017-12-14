package acore.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import acore.logic.VersionOp;
import acore.logic.XHClick;
import acore.override.XHApplication;
import aplug.recordervideo.tools.SortComparator;
import third.mall.tool.ToolView;
import xh.basic.tool.UtilLog;

import static acore.override.XHApplication.ONLINE_PACKAGE_NAME;

@SuppressLint("SimpleDateFormat")
public class Tools {

    /**
     * 是否是debug
     * @param context
     * @return
     */
    public static boolean isDebug(Context context){
        String versoinName = VersionOp.getVerName(context);
        String[] temp = versoinName.split("\\.");
        String currentPackName = ToolsDevice.getPackageName(context);
        return temp.length != 3 || !ONLINE_PACKAGE_NAME.equals(currentPackName);
    }

    /**
     * 是否开启请求返回值提示
     * @param context
     * @return
     */
    public static boolean isOpenRequestTip(Context context){
        String isOpen = FileManager.loadShared(context,FileManager.xmlFile_appInfo,FileManager.xmlKey_request_tip).toString();
        return "2".equals(isOpen);
    }

    /**
     * 获取指定格式的时间(注意ms数不能溢出)
     *
     * @param formatStr 形如"yyyy年MM月dd日HH:mm:ss"
     * @param overTime  0为当前时间,其他根据值加减时间ms
     * @return 时间
     */
    public static String getAssignTime(String formatStr, long overTime) {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(formatStr);
        java.util.Date curDate = new java.util.Date(System.currentTimeMillis() + overTime);
        return formatter.format(curDate);
    }

    /**
     * 将毫秒转换成日期
     *
     * @param pattern  ：指定格式
     * @param dateTime ：毫秒
     * @return
     */
    public static String getFormatedDateTime(String pattern, long dateTime) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);
        return sDateFormat.format(new Date(dateTime));
    }

    // 处理文本首段缩进
    public static StringBuilder getSegmentedStr(String info) {
        String[] infos = info.split("\n");
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < infos.length; i++)
            if (i == infos.length - 1)
                text.append("\t\t\t\t").append(infos[i]);
            else
                text.append("\t\t\t\t").append(infos[i]).append("\n");
        return text;
    }

    /**
     * 时间格式为yyyy-MM-dd HH:mm:ss
     *
     * @param lastTime    上一条记录时间
     * @param currentTime 当前记录时间
     * @return 时间的正确显示字符串
     * @throws ParseException
     */
    public static String dealTime(String lastTime, String currentTime, String dateFormat, String time) throws ParseException {
        if (time.equals("hide"))
            return time;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date currentDate = simpleDateFormat.parse(currentTime);
        if (lastTime != null) {
            Date lastDate = simpleDateFormat.parse(lastTime);
            long timeDifference = currentDate.getTime() - lastDate.getTime();
            if (timeDifference <= 15 * 60 * 1000)
                return "hide";
        }
        return time;
    }

    /**
     * 将数组转换为JSON格式的数据。
     *
     * @param strs 数据源
     * @return JSON格式的数据
     */
    public static String changeArrayDateToJson(String[] strs) {
        try {
            JSONArray array = new JSONArray();
            int length = strs.length;
            for (String name : strs) {
                JSONObject stoneObject = new JSONObject();
                stoneObject.put("", name);
                array.put(stoneObject);
            }
            return array.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getTargetHeight(View view) {
        if (view == null) return 0;
        try {
            Method m = view.getClass().getDeclaredMethod("onMeasure", int.class, int.class);
            m.setAccessible(true);
            m.invoke(view, MeasureSpec.makeMeasureSpec(((View) view.getParent()).getMeasuredWidth(), MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        } catch (Exception e) {
            UtilLog.reportError("获取View的onMeasre方法", e);
        }
        return view.getMeasuredHeight();
    }


    //获取view的measure高度
    public static int getMeasureHeight(View view) {
        if (view == null) return 0;
        view = setViewMeasure(view);
        return view.getMeasuredHeight();
    }

    //获取view的measure宽度
    public static int getMeasureWidth(View view) {
        if (view == null) return 0;
        view = setViewMeasure(view);
        return view.getMeasuredWidth();
    }

    //获取view的onMeasure方法
    private static View setViewMeasure(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        return view;
    }

    //通过反射获取view的onMeasure方法
    @SuppressWarnings("unused")
    private static View setViewMeasureByReflex(View view) {
        try {
            Method m = view.getClass().getDeclaredMethod("onMeasure", int.class, int.class);
            m.setAccessible(true);
            m.invoke(view, MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        } catch (Exception e) {
            UtilLog.reportError("获取View的onMeasre方法", e);
        }
        return view;
    }

    /**
     * @param type year 年 , month 月, date 日, day 星期, hour 时 , minute 分 , second 秒
     * @return 0 没有对应类型 -1 type为null
     */
    @SuppressWarnings("deprecation")
    public static int getDate(String type) {
        Date date = new Date();
        if (type != null) {
            if (type.equals("year"))        // 年
                return date.getYear() + 1900;
            else if (type.equals("month"))    // 月
                return date.getMonth() + 1;
            else if (type.equals("date"))        // 日
                return date.getDate();
            else if (type.equals("day"))        // 星期
                return date.getDay();
            else if (type.equals("hour"))        // 时
                return date.getHours();
            else if (type.equals("minute"))    // 分
                return date.getMinutes();
            else if (type.equals("second"))    // 秒
                return date.getSeconds();
            else
                return 0;
        } else
            return -1;
    }


    /**
     * @param context
     * @param id      dimens文件中的id(仅适用于dp)
     * @return dimen 对应分辨率的dp或者sp值
     */
    public static int getDimen(Context context, int id) {
        float dimen = 0;
        String string = context.getResources().getString(id).replace("dip", "");
        dimen = Float.parseFloat(string);
        return ToolsDevice.dp2px(context, dimen);
    }

    /**
     * @param context
     * @param id      dimens文件中的id(仅适用于dp)
     * @return dimen sp值
     */
    public static Float getDimenSp(Context context, int id) {
        float dimen = 0;
        String string = context.getResources().getString(id).replace("sp", "");
        dimen = Float.parseFloat(string);
        return dimen;
    }


    public static String map2Json(Map<String, String> map) {
        JSONObject jsonObject = new JSONObject();
        for (Entry<String, String> entry : map.entrySet()) {
            try {
                String value = "";
                if (entry.getValue() != null && !entry.getValue().equals("null"))
                    value = entry.getValue();
                jsonObject.put(entry.getKey(), value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject.toString();
    }

    /**
     * @param context   上下文
     * @param returnObj 需要弹出的内容,如果是空的内容,就不弹.
     */
    public static void showToast(Context context, String returnObj) {
        if (context == null)
            return;
        showToast(context, returnObj, -1);
    }

    /**
     * @param context   上下文
     * @param returnObj 需要弹出的内容,如果是空的内容,就不弹.
     */
    public static void showToast(Context context, String returnObj, int gravity) {
        if (context != null && !TextUtils.isEmpty(returnObj) && !"[]".equals(returnObj)) {
            Toast toast = Toast.makeText(context, returnObj, Toast.LENGTH_SHORT);
            if (gravity != -1)
                toast.setGravity(gravity, 0, 0);
            toast.show();
        }
    }

    public static JSONArray list2JsonArray(List<Map<String, String>> data){
        JSONArray jsonArray = new JSONArray();
        for (Map<String, String> map : data) {
            JSONObject jsonObject = new JSONObject();
            try {
                for (Entry<String, String> entry : map.entrySet()) {
                    jsonObject.put(entry.getKey(), entry.getValue());
                }
            } catch (JSONException e) {
                UtilLog.reportError("JSON生成异常", e);
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public static String list2Json(List<Map<String, String>> data) {
        //添加JS回调代码
        return list2JsonArray(data).toString();
    }

    /**
     * @param context
     * @return 获取手机内存和CPU频率以及系统版本
     */
    public static boolean getPhoneInformation(Context context) {
        //添加手机的判断
        int version = android.os.Build.VERSION.SDK_INT;
        String availMemory = "";
//		if (context != null) {
//			availMemory = ToolsDevice.getAvailMemory(context);//获取手机当前可用内存
//		}
        availMemory = ToolsDevice.getTotalMemory();//获取系统总内存
        int parseInt = 0;
        if (!TextUtils.isEmpty(availMemory)) {
            parseInt = Integer.parseInt(availMemory);
        }
//		String curCpuFreq = CpuManager.getCurCpuFreq();
        int maxCpuFreq = CPUTool.getMaxCpuFreq();
//		int minCpuFreq = CPUTool.getMinCpuFreq();
        return version >= 17 && parseInt >= 1000 && maxCpuFreq > 1000000;
    }

    /**
     * 将imageview转成bitmap
     *
     * @param imageView
     * @return
     */
    public static Bitmap getBitmap(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        BitmapDrawable bd = (BitmapDrawable) drawable;
        return bd.getBitmap();
    }

    public static int getRandom(int star, int end) {
        return (int) (star + (Math.random() * (end + 1 - star)));
    }

    /**
     * 算出真实的useracode
     *
     * @param user
     * @return
     */
    public static String getUserCode(String user) {
        int usedata = Integer.parseInt(user);
        String data = "";
        if (usedata % 10 == 0)
            data = 8 + "";
        else
            data = user.substring(user.length() - 1, user.length());
        String u = data + user + user.substring(0, 1);
        long code = Long.parseLong(u) * 89 + 312;
        return code + "";
    }

    /**
     * 获得当前进程的名字
     *
     * @param context
     * @return 进程号
     */
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public static String timeFormat(long timeMillis, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date(timeMillis));
    }

    public static String formatPhotoDate(long time) {
        return timeFormat(time, "yyyy-MM-dd");
    }

    public static String formatPhotoDate(String path) {
        String retStr = "1970-01-01";
        if (TextUtils.isEmpty(path))
            return retStr;
        File file = new File(path);
        if (file.exists()) {
            long time = file.lastModified();
            retStr = formatPhotoDate(time);
        }
        return retStr;
    }

    public static String InputStream2String(InputStream is) {
        String str = "";
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int count = -1;
            while ((count = is.read(data, 0, 1024)) != -1) {
                os.write(data, 0, count);
            }
            data = null;
            str = new String(os.toByteArray());
        } catch (IOException ignored) {

        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    public static String getColorStr(Context context, int resId) {
        if(null == context){
            return "#00FFFFFE";
        }
        return context.getResources().getString(resId);
    }

    /**
     * //透明状态栏
     * getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
     * //透明导航栏
     * getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
     * <p>
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取导航栏高度
     *
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = 0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
//			Log.show("高度："+resourceId);
//			CMLog.show("高度："+context.getResources().getDimensionPixelSize(resourceId) +"");
            return context.getResources().getDimensionPixelSize(resourceId);
        } else
            return 0;
    }

    /**
     * 是否可以自定义状态栏
     *
     * @return
     */
    public static boolean isShowTitle() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * 直接改变view的颜色
     * @param activity
     * @param statusColor
     */
    public static void setStatusBarColor(Activity activity, int statusColor) {
        Window window = activity.getWindow();
        ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //First translucent status bar.
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //After LOLLIPOP not translucent status bar
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                //Then call setStatusBarColor.
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(statusColor);
                //set child View not fill the system window
                View mChildView = mContentView.getChildAt(0);
                if (mChildView != null) {
                    ViewCompat.setFitsSystemWindows(mChildView, true);
                }
            } else {
                ViewGroup mDecorView = (ViewGroup) window.getDecorView();
                if (mDecorView.getTag() != null && mDecorView.getTag() instanceof Boolean && (Boolean) mDecorView.getTag()) {
                    //if has add fake status bar view
                    View mStatusBarView = mDecorView.getChildAt(0);
                    if (mStatusBarView != null) {
                        mStatusBarView.setBackgroundColor(statusColor);
                    }
                } else {
                    int statusBarHeight = Tools.getStatusBarHeight(activity);
                    //add margin
                    View mContentChild = mContentView.getChildAt(0);
                    if (mContentChild != null) {
                        ViewCompat.setFitsSystemWindows(mContentChild, false);
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContentChild.getLayoutParams();
                        lp.topMargin += statusBarHeight;
                        mContentChild.setLayoutParams(lp);
                    }
                    //add fake status bar view
                    View mStatusBarView = new View(activity);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
                    layoutParams.gravity = Gravity.TOP;
                    mStatusBarView.setLayoutParams(layoutParams);
                    mStatusBarView.setBackgroundColor(statusColor);
                    mDecorView.addView(mStatusBarView, 0);
                    mDecorView.setTag(true);
                }
            }
        }
    }

    public static boolean isSupportSpeciaVideoSize(boolean isFacingFont) {
        boolean flag = false;
        try {
            Camera.Parameters mParameters;
            Camera mCamera;
            if (isFacingFont) {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                mCamera = Camera.open();
            }
            if (mCamera != null) {
                mParameters = mCamera.getParameters();
                List<Camera.Size> list = mParameters.getSupportedPreviewSizes();
                if (list != null && list.size() > 0) {
                    Comparator comp = new SortComparator();
                    Collections.sort(list, comp);
                    for (Camera.Size size : list) {
                        if (size.height / 9.0f * 16 == size.width) {
                            flag = true;
                            break;
                        }
                    }
                }
                mCamera.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    // 获取SD卡的可用空间大小
    public static long getSDCardAvailableSize() {
        if (isSDCardMounted()) {
            StatFs fs = new StatFs(getSDCardBaseDir());
            long count = fs.getAvailableBlocks();
            long size = fs.getBlockSize();
            return count * size / 1024 / 1024;
        }
        return 0;
    }

    // 判断SD卡是否被挂载
    public static boolean isSDCardMounted() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    // 获取SD卡的根目录
    public static String getSDCardBaseDir() {
        if (isSDCardMounted()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return null;
    }


    //判断应用是否在前台
    public static boolean isForward(Context context) {
        if (context == null)
            return false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        ActivityManager.RunningAppProcessInfo processInfo = appProcesses.get(0);
        return processInfo != null
                && processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && processInfo.processName.equals(packageName);
    }


    /**
     * 将字节转换为KB,MB,GB
     *
     * @param size
     * @return
     */
    public static String getPrintSize(long size) {
        long temp;

        //如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            temp = size % 1024;
            size = size / 1024;
        }
        //如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
        //因为还没有到达要使用另一个单位的时候
        //接下去以此类推
        if (size < 1024) {
            return String.valueOf(size) + "KB";
        } else {
            temp = size % 1024;
            size = size / 1024;
        }

        if (size < 1024) {
            return String.valueOf(size) + "."
                    + String.valueOf((int) (temp * 10 / 1024)) + "MB";
        } else {
            //否则如果要以GB为单位的，先除于1024再作同样的处理
            temp = size % 1024;
            size = size / 1024;
            return String.valueOf(size) + "."
                    + String.valueOf((int) (temp * 10 / 1024)) + "GB";
        }
    }

    /**
     * 对字符串md5加密
     *
     * @param str
     * @return
     */
    public static String getMD5(String str){
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            return str;
        }
    }


    public static int getPhoneWidth() {
        WindowManager wm = (WindowManager) XHApplication.in()
                .getSystemService(Context.WINDOW_SERVICE);

        return wm.getDefaultDisplay().getWidth();
    }

    public static int getPhoneHeight() {
        WindowManager wm = (WindowManager) XHApplication.in()
                .getSystemService(Context.WINDOW_SERVICE);

        return wm.getDefaultDisplay().getHeight();
    }


    public static boolean isFileExists(String filePath){

        if(TextUtils.isEmpty(filePath))
            return  false;
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    public static int getScreenHeight(){
        return  ToolsDevice.getWindowPx(XHApplication.in()).heightPixels;
    }

    /**
     *
     * @param context
     * @param tvWidth : textView的宽
     * @param tvSize
     * @return
     */
    public static int getTextNumbers (Context context,int tvWidth,int tvSize) {
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        int tv_distance = (int) context.getResources().getDimension(R.dimen.dp_18);
//        int waith = wm.getDefaultDisplay().getWidth();
//        int tv_waith = waith - margin;
        int tv_pad = ToolView.dip2px(context, 1.0f);
        /* 判断是否等于0 */
        return tvSize + tv_pad > 0 ? (tvWidth + tv_pad) / (tvSize + tv_pad) : 0;
    }

    /**
     * 程序是否在前台运行
     * @return
     */
    public static boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device
        ActivityManager activityManager = (ActivityManager) XHApplication.in().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = XHApplication.in().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    public static void getApiSurTime(String twoLevel,long oldTime,long newTime){
        long backTimeMin = (newTime - oldTime) / 1000 / 60;
        long surTime;
        if(oldTime == 0){
            surTime = 1;
        }else if(backTimeMin < 6){
            surTime = backTimeMin / 2 + 1;
        }else if(backTimeMin > 60){
            surTime = 8;
        }else{
            if(backTimeMin < 11){
                surTime = 4;
            }else if(backTimeMin < 31){
                surTime = 5;
            }else if(backTimeMin < 46){
                surTime = 6;
            }else{
                surTime = 7;
            }
        }
        XHClick.mapStat(XHApplication.in(),"apiSurTime",twoLevel,String.valueOf(surTime));
    }
    /**
     * map转json
     *
     * @param maps
     * @return
     */
    public static JSONObject MapToJson(Map<String, String> maps) {

        JSONObject jsonObject = new JSONObject();
        if(maps==null||maps.size()<=0)return jsonObject;

        Iterator<Map.Entry<String, String>> enty = maps.entrySet().iterator();
        try {
            while (enty.hasNext()) {
                Map.Entry<String, String> entry = enty.next();
                jsonObject.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static String getTimeDifferent(long nowTime,long time){
        String timeShow="";
        if(nowTime<=0||time<=0){
            return "问题时间";
        }
        long timeDifferent=nowTime-time;
        if(timeDifferent<60){//秒
            timeShow="刚刚";
        }else if(timeDifferent<60*60){//分钟
            int num= (int) (timeDifferent/(60));
            timeShow=num+"分钟前";
        }else if(timeDifferent<60*60*24){//小时
            int num= (int) (timeDifferent/(60*60));
            timeShow=num+"小时前";
        }else if(timeDifferent<60*60*48){//昨天
            timeShow="昨天";
        }else if(timeDifferent<60*60*24*30){//几天
            int num= (int) (timeDifferent/(60*60*24));
            timeShow=num+"天前";
        }else if(timeDifferent<60*60*24*30*12){//几月
            int num= (int) (timeDifferent/(60*60*24*30));
            timeShow=num+"月前";
        }else {
            timeShow="很久前";
        }
        return timeShow;
    }

    public static void setMute(Context context){
        AudioManager audioManager = (AudioManager)context.getSystemService("audio");
        int mCurrSoundNum = audioManager.getStreamVolume(3);
        audioManager.setStreamVolume(3, 0, 0);
    }

    /**
     * 获取Context所在进程的名称
     *
     * @param context
     * @return
     */
    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 插入文本到剪切板
     * @param context
     * @param str
     */
    public static void inputToClipboard(Context context,String str){
        if(context == null || TextUtils.isEmpty(str))
            return;
        final ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("simple text copy", str);
        manager.setPrimaryClip(clip);
    }
    /*获取Context所在进程的名称

    pkgName 包名
    *
    * */
    public static boolean isPkgInstalled(String pkgName,Context context) {
        PackageInfo packageInfo = null;
        try {

            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {

            packageInfo = null;
            e.printStackTrace();
        }
        return packageInfo != null;
        }
}
