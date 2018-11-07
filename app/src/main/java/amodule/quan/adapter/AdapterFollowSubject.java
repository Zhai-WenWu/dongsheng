package amodule.quan.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import amodule.quan.view.NormalContentView;
import amodule.quan.view.NormarlContentItemImageVideoView;
import amodule.quan.view.RecommendFriendView;
import third.ad.tools.AdConfigTools;
import xh.basic.tool.UtilString;

import static amodule.quan.adapter.AdapterCircle.STYLE_FRIEND_RECOMMEND;
import static amodule.quan.adapter.AdapterCircle.STYLE_ROB_SOFA;

/**
 * Created by XiangHa on 2017/7/19.
 */

public class AdapterFollowSubject extends AdapterSimple {
    public static final int STYLE_NORMAL = 1;
    private String moduleName = "";
    private String circleName = "";
    private Activity mContext;
    private List<Map<String, String>> mData = null;
    private List<Map<String, String>> mRecCutomerArray = null;
    private String stiaticKey = "";
    public int index_user = 0;
    private String showIndex = "quan";
    private int currentPlayPosition = -1;

    public AdapterFollowSubject(Activity context, View parent, List<? extends Map<String, ?>> data) {
        super(parent, data, 0, null, null);
        this.mData = (List<Map<String, String>>) data;
        this.mContext = context;
    }
    public void setStiaticData(String stiaticId){
        this.stiaticKey=stiaticId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, String> map = mData.get(position);
        final Integer subjectStyle = Integer.valueOf(map.get("style"));
        switch (subjectStyle) {
            case STYLE_FRIEND_RECOMMEND:
                RecommendFriendView recommendFriendView = new RecommendFriendView(mContext);
                recommendFriendView.setUserIndexCallback(new RecommendFriendView.UserIndexCallback() {
                    @Override
                    public int getUserIndex() {
                        return index_user;
                    }

                    @Override
                    public void plusUserIndex() {
                        index_user++;
                    }
                });
                recommendFriendView.initView(map);
                if(!TextUtils.isEmpty(stiaticKey))recommendFriendView.setStaticID(stiaticKey);
                if (mRecCutomerArray != null) {
                    recommendFriendView.setRecCutomerArray(mRecCutomerArray);
                }
                convertView = recommendFriendView;
                break;
            case STYLE_ROB_SOFA:
            case STYLE_NORMAL:
                NormalContentViewHolder normalContentViewHolder;
                if (convertView == null
                        || !(convertView.getTag() instanceof AdapterCircle.NormalContentViewHolder)) {
                    normalContentViewHolder = new NormalContentViewHolder(new NormalContentView(mContext));
                    convertView = normalContentViewHolder.view;
                    convertView.setTag(normalContentViewHolder);
                } else {
                    normalContentViewHolder = (NormalContentViewHolder) convertView.getTag();
                }
                normalContentViewHolder.setData(map, position, subjectStyle);
                break;
        }
        return convertView;
    }

    public class NormalContentViewHolder {
        NormalContentView view;

        public NormalContentViewHolder(NormalContentView view) {
            this.view = view;
        }

        public void setData(final Map<String, String> map, final int position, int style) {
            if (view != null) {
                //添加美食贴统计code
                if (!map.containsKey("isPromotion"))
                    XHClick.saveCode(view.getContext(), map.get("code"));
                //设置是否显示抢沙发的title需要在initView前面
                view.setIsRobsof(STYLE_ROB_SOFA == style);
                //设置当前的position
//                view.setTag(NormalContentView.POSITION,position);
                view.setNeedRefresh(currentPlayPosition != position);

                //设置点击统计回调
                view.setmOnItemClickStatictis(new NormalContentView.OnItemClickStatictis() {
                    @Override
                    public void onStatictis(String onClickSite) {
                        if (map.containsKey("showMid") && map.containsKey("showCid")) {
                            if (!showIndex.equals("home")){
//                                AdConfigTools.getInstance().postTongjiQuan(view.getContext(), map, onClickSite, "click");
                            }
                        }
                    }
                });
                //先写回调在执行展示代码
                view.setModuleName(moduleName);
                view.initView(map, position);
                view.setCircleName(circleName);
                if (!TextUtils.isEmpty(stiaticKey))
                    view.setStiaticKey(stiaticKey);
                setOnClickList(map);
                //视频被点击回调
                view.setVideoClickCallBack(new NormarlContentItemImageVideoView.VideoClickCallBack() {
                    @Override
                    public void videoImageOnClick(int position) {
                        Log.i("zhangyujian", "position::" + position);//被点击
                        videoClickCallBack.videoImageOnClick(position);
                    }
                });
            }
        }

        /**
         * 对数据是否是广告进行判断并进行数据配置上
         * @param map
         */
        private void setOnClickList(Map<String, String> map) {
            if (map.containsKey("showCid") && map.containsKey("showMid")) {
                ArrayList<String> list = new ArrayList<>();
                ArrayList<Map<String, String>> customers = UtilString.getListMapByJson(map.get("customer"));
                if (customers.get(0).containsKey("url"))
                    list.add(customers.get(0).get("url"));
                if (map.containsKey("url"))
                    list.add(map.get("url"));
                view.setOnClickData(list);
            }
        }
    }

    //对view进行
    private NormarlContentItemImageVideoView.VideoClickCallBack videoClickCallBack;

    public void setVideoClickCallBack(NormarlContentItemImageVideoView.VideoClickCallBack videoClickCallBack) {
        this.videoClickCallBack = videoClickCallBack;
    }
}

