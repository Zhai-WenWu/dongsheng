package amodule.answer.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.Tools;

/**
 * Created by sll on 2017/7/18.
 */

public class AskAnswerImgController {
    private Context mContext;
    private ViewGroup mParentView;

    private ArrayList<Map<String, String>> mDatas = new ArrayList<Map<String, String>>();
    private final int mImgSize = 3;
    private final int mVideoSize = 1;

    private int mItemWidth;
    private int mItemHeight;

    public AskAnswerImgController(Context context, ViewGroup parentView, int itemWidth, int itemHeight) {
        this.mContext = context;
        this.mParentView = parentView;
        this.mItemWidth = itemWidth;
        this.mItemHeight = itemHeight;
    }

    private View.OnClickListener mOnVideoClickListener;
    public void setOnVideoClickListener(View.OnClickListener listener) {
        mOnVideoClickListener = listener;
    }

    private View.OnClickListener mOnPhotoClickListener;
    public void setOnPhotoClickListener(View.OnClickListener listener) {
        mOnPhotoClickListener = listener;
    }

    public boolean checkCondition(boolean isVideo) {
        if (mDatas.isEmpty())
            return true;
        if (!TextUtils.isEmpty(mDatas.get(0).get("video"))) {
            if (isVideo) {
                if (mDatas.size() < mVideoSize)
                    return true;
                else {
                    Tools.showToast(mContext, "最多可选取" + mVideoSize + "个视频");
                    return false;
                }
            } else {
                Tools.showToast(mContext, "视频和图片不能同时选择");
                return false;
            }
        } else {
            if (!isVideo) {
                if (mDatas.size() < mImgSize)
                    return true;
                else {
                    Tools.showToast(mContext, "最多可选取" + mImgSize + "张图片");
                    return false;
                }
            } else {
                Tools.showToast(mContext, "视频和图片不能同时选择");
                return false;
            }
        }
    }

    public void addData(final Map<String, String> dataMap) {
        if (mContext == null || mParentView == null || dataMap == null || dataMap.isEmpty())
            return;
        if (mItemWidth <= 0 || mItemHeight <= 0) {
            throw new IllegalArgumentException("The item width and height must >0.");
        }
        int childCount = mParentView.getChildCount();
        mDatas.add(childCount, dataMap);
        final AskAnswerImgItemView itemView = new AskAnswerImgItemView(mContext);
        itemView.setData(dataMap, childCount, mItemWidth, mItemHeight);
        itemView.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemView.isVideo()) {
                    if (mOnVideoClickListener != null)
                        mOnVideoClickListener.onClick(itemView);
                } else {
                    if (mOnPhotoClickListener != null)
                        mOnPhotoClickListener.onClick(itemView);
                }
            }
        });
        itemView.setOnDelClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               remove(dataMap, itemView);
            }
        });
        itemView.setOnLoadExceptionListener(new AskAnswerImgItemView.OnLoadExceptionListener() {
            @Override
            public void onLoadException() {
                remove(dataMap, itemView);
            }
        });
        mParentView.addView(itemView);
    }

    private void remove(Map<String, String> dataMap, View itemView) {
        mDatas.remove(dataMap);
        mParentView.removeView(itemView);
        mParentView.invalidate();
    }

    /**
     * 获取图片集合
     * @return
     */
    public ArrayList<Map<String, String>> getImgsArray() {
        ArrayList<Map<String, String>> imgs = new ArrayList<Map<String, String>>();
        for (Map<String, String> map : mDatas) {
            String imgPath = map.get("img");
            if (!TextUtils.isEmpty(imgPath))
                imgs.add(map);
            else
                break;
        }
        return imgs;
    }

    /**
     * 适用于只有视频文件
     * @return
     */
    public ArrayList<Map<String, String>> getVideosArray() {
        ArrayList<Map<String, String>> videos = new ArrayList<Map<String, String>>();
        for (Map<String, String> map : mDatas) {
            String videoPath = map.get("video");
            if (!TextUtils.isEmpty(videoPath))
                videos.add(map);
            else
                break;
        }
        return videos;
    }

    public int getVideoFixedSize() {
        return mVideoSize;
    }

    public int getImgFixedSize() {
        return mImgSize;
    }

    public ArrayList<Map<String, String>> getDatas() {
        return mDatas;
    }
}
