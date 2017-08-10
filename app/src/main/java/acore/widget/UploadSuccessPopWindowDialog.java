package acore.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.tools.ToolsDevice;
import third.share.ShareTools;

/**
 * Created by ：fei_teng on 2016/11/7 21:07.
 */

public class UploadSuccessPopWindowDialog {

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mView;
    private ArrayList<Map<String, String>> mData = new ArrayList<Map<String, String>>();
    private String[] mSharePlatforms;
    private String mType, mTitle, mClickUrl, mContent, mImgUrl, mFrom, mParent;
    private String mDishName;
    private String mDishPath;
    private LinearLayout ll_more_info;
    private ImageView dish_cover;
    private UploadSuccessDialogCallback callback;


    public UploadSuccessPopWindowDialog(Context context, String dishName, String dishPath,UploadSuccessDialogCallback callback) {
        mContext = context;
        mDishName = dishName;
        mDishPath = dishPath;
        this.callback = callback;
//        initData();
        init();
    }

//    private void init() {
//        LayoutInflater inflater = LayoutInflater.from(mContext);
//        mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//        mView = inflater.inflate(R.layout.d_popwindow_upload_success, null);
//        initShareView();
//        TextView tv_dish_name = (TextView) mView.findViewById(R.id.tv_dish_name);
//        tv_dish_name.setText(mDishName);
//        mView.setOnClickListener(onCloseListener);
//        mView.findViewById(R.id.d_popwindow_close).setOnClickListener(onCloseListener);
//
//        ImageView dish_cover = (ImageView) mView.findViewById(R.id.iv_dish_cover);
//        Glide.with(mContext).load(mDishPath).into(dish_cover);
//
//        mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
////        //设置window的type
//        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
//        mLayoutParams.format = PixelFormat.RGBA_8888;
//        //设置浮动窗口不可聚焦
//        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        //位置
//        mLayoutParams.gravity = Gravity.TOP;
//
//    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        mView = inflater.inflate(R.layout.d_popwindow_upload_fail, null);
        TextView tv_dish_name = (TextView) mView.findViewById(R.id.tv_dish_name);
        tv_dish_name.setText(mDishName);
        mView.setOnClickListener(onCloseListener);
        mView.findViewById(R.id.d_popwindow_close).setVisibility(View.INVISIBLE);

         dish_cover = (ImageView) mView.findViewById(R.id.iv_dish_cover);
        Glide.with(XHApplication.in()).load(mDishPath).priority(Priority.IMMEDIATE)
                .error(R.drawable.mall_recommed_product_backgroup)
                .placeholder(R.drawable.mall_recommed_product_backgroup).crossFade()
                .into(dish_cover);

        ll_more_info = (LinearLayout) mView.findViewById(R.id.ll_more_info);
        ll_more_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClick();
                closePopWindowDialog();
            }
        });

        mView.findViewById(R.id.d_popwindow_img).setBackgroundResource(R.drawable.icon_dishvideo_upload_sucess);
        ((TextView) mView.findViewById(R.id.d_popwindow_title)).setTextColor(Color.parseColor("#00c847"));
        ((TextView) mView.findViewById(R.id.d_popwindow_title)).setText("发布成功");
        ((TextView) mView.findViewById(R.id.tv_info)).setText("我知道了");
        mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//        //设置window的type
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //位置
        mLayoutParams.gravity = Gravity.CENTER;

    }


    private void initShareView() {
        GridView mGridView = (GridView) mView.findViewById(R.id.d_popwindow_share_gridview);

        SimpleAdapter adapter = new SimpleAdapter(mContext, mData, R.layout.d_popwindow_share_item,
                new String[]{"img", "name"},
                new int[]{R.id.share_logo, R.id.share_name});
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String platfrom = mSharePlatforms[position];
                ShareTools barShare = ShareTools.getBarShare(mContext);
                barShare.showSharePlatform(mTitle, mContent, mType, mImgUrl, mClickUrl, platfrom, mFrom, mParent);
                closePopWindowDialog();
            }
        });
    }

    private void initData() {
        String[] mNames;
        int[] mLogos;
        if (ToolsDevice.isAppInPhone(mContext, "com.tencent.mm") == 0) {
            mNames = new String[]{"QQ空间", "QQ", "新浪微博", "信息", "复制链接"};
            mLogos = new int[]{
                    R.drawable.logo_qzone, R.drawable.logo_qq,
                    R.drawable.logo_sina_weibo, R.drawable.logo_short_message,
                    R.drawable.logo_copy
            };
            mSharePlatforms = new String[]{
                    ShareTools.QQ_ZONE, ShareTools.QQ_NAME,
                    ShareTools.SINA_NAME, ShareTools.SHORT_MESSAGE,
                    ShareTools.LINK_COPY};
        } else {
            mNames = new String[]{"微信好友", "微信朋友圈", "QQ空间", "QQ", "新浪微博", "信息", "复制链接"};
            mLogos = new int[]{R.drawable.logo_wechat, R.drawable.logo_wechat_moments,
                    R.drawable.logo_qzone, R.drawable.logo_qq,
                    R.drawable.logo_sina_weibo, R.drawable.logo_short_message,
                    R.drawable.logo_copy
            };
            mSharePlatforms = new String[]{
                    ShareTools.WEI_XIN, ShareTools.WEI_QUAN,
                    ShareTools.QQ_ZONE, ShareTools.QQ_NAME,
                    ShareTools.SINA_NAME, ShareTools.SHORT_MESSAGE,
                    ShareTools.LINK_COPY};
        }
        for (int i = 0; i < mNames.length; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", mNames[i]);
            map.put("img", "" + mLogos[i]);
            mData.add(map);
        }
    }

    public void show(String type, String title, String clickUrl, String content, String imgUrl, String from, String parent) {
        mType = type;
        mTitle = title;
        mClickUrl = clickUrl;
        mContent = content;
        mImgUrl = imgUrl;
        mFrom = from;
        mParent = parent;


        mWindowManager.addView(mView, mLayoutParams);


    }

    public void show() {
        mWindowManager.addView(mView, mLayoutParams);
    }

//	public void show() {
//		mWindowManager.addView(mView, mLayoutParams);
//	}

    private View.OnClickListener onCloseListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            closePopWindowDialog();
        }
    };

    public void closePopWindowDialog() {
        if (mWindowManager != null) {
            if (mView != null)
                mWindowManager.removeView(mView);
            mWindowManager = null;
        }
    }

    /**
     * 获取当前分享Dialog是否还在显示
     *
     * @return true:显示 fase：不显示
     */
    public boolean isHasShow() {
        return mWindowManager != null;
    }

    public void onPause() {
        mView.setVisibility(View.GONE);
    }

    public void onResume() {
        mView.setVisibility(View.VISIBLE);
    }

    public interface UploadSuccessDialogCallback {
        void onClick();
    }

}
