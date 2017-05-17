package amodule.quan.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.adapter.AdapterSimple;
import amodule.quan.tool.HomeAdvertControl;
import amodule.quan.tool.QuanAdvertControl;
import amodule.quan.view.NormalContentView;
import amodule.quan.view.NormarlContentItemImageVideoView;
import amodule.quan.view.RecommendFriendView;
import amodule.quan.view.UserRankView;
import third.ad.tools.AdConfigTools;
import xh.basic.tool.UtilString;

public class AdapterCircle extends AdapterSimple {
    public static final int STYLE_NORMAL = 1;
    public static final int STYLE_ROB_SOFA = 5;
    public static final int STYLE_FRIEND_RECOMMEND = 6;

    public static final int DATATYPE_SUBJECT = 1;
    public static final int DATATYPE_USER = 2;
    public static final int DATATYPE_CIRCLE = 3;
    private String moduleName = "";
    private String circleName = "";
    private Activity mContext;
    private List<Map<String, String>> mData = null;
    private List<Map<String, String>> mRecCutomerArray = null;
    private boolean statis_isHome = false;//统计是否是首页
    private String Cid;
    private String stiaticKey = "";
    private RecommendFriendView.RecommendCutomerCallBack mRecommendCutomerCallBack;
    public int index_user=0;
    private String showIndex="quan";
    private int currentPlayPosition = -1;
    private QuanAdvertControl quanAdvertControl;
    public AdapterCircle(Activity context, View parent, List<? extends Map<String, ?>> data, String Cid) {
        super(parent, data, 0, null, null);
        this.mData = (List<Map<String, String>>) data;
        this.mContext = context;
        this.Cid = Cid;
    }
    @SuppressWarnings("unchecked")
    public AdapterCircle(Activity context, View parent, List<? extends Map<String, ?>> data, String Cid,String showIndex) {
        super(parent, data, 0, null, null);
        this.mData = (List<Map<String, String>>) data;
        this.mContext = context;
        this.Cid = Cid;
        this.showIndex= showIndex;
    }
    public void setQuanAdvertControl(QuanAdvertControl quanAdvertControls){
        this.quanAdvertControl=quanAdvertControls;
    }

    public String getCircleName() {
        return circleName;
    }

    public void setStiaticKey(String stiaticKey) {
        this.stiaticKey = stiaticKey;
    }

    public void setCircleName(String circleName) {
        this.circleName = circleName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setModuleName(String moduleName, boolean isHome) {
        this.moduleName = moduleName;
        this.statis_isHome = isHome;
    }

    public void setmRecCutomerArray(List<Map<String, String>> recCutomerArray) {
        if (mRecCutomerArray == null) {
            this.mRecCutomerArray = recCutomerArray;
        } else {
            mRecCutomerArray.addAll(recCutomerArray);
        }
    }

    public void clearRecCutomerArray() {
        if (mRecCutomerArray != null) {
            mRecCutomerArray.clear();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, String> map = mData.get(position);
        int dataType = Integer.valueOf(map.get("dataType"));
        //判断数据类型是否是user数据列表
        if (DATATYPE_USER == dataType) {
            UserViewHolder holder = null;
            if (convertView == null) {
                holder = new UserViewHolder(new UserRankView(mContext));
                convertView = holder.view;
                convertView.setTag(holder);
            } else {
                holder = (UserViewHolder) convertView.getTag();
            }
            holder.setData(map);
        } else if (DATATYPE_SUBJECT == dataType) {
            final Integer subjectStyle = Integer.valueOf(map.get("style"));
            switch (subjectStyle) {
                case AdapterCircle.STYLE_FRIEND_RECOMMEND:
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
                    recommendFriendView.setStaticID(stiaticKey);
                    if (mRecCutomerArray != null) {
                        recommendFriendView.setRecCutomerArray(mRecCutomerArray);
                    }
                    recommendFriendView.setRecommendCutomerCallBack(mRecommendCutomerCallBack);
                    convertView = recommendFriendView;
                    break;
                case AdapterCircle.STYLE_ROB_SOFA:
                case AdapterCircle.STYLE_NORMAL:
                    NormalContentViewHolder normalContentViewHolder = null;
                    if (convertView == null
                            || !(convertView.getTag() instanceof NormalContentViewHolder)) {
                        normalContentViewHolder = new NormalContentViewHolder(new NormalContentView(mContext));
                        convertView = normalContentViewHolder.view;
                        convertView.setTag(normalContentViewHolder);
                    } else {
                        normalContentViewHolder = (NormalContentViewHolder) convertView.getTag();
                    }
                    normalContentViewHolder.setData(map, position, subjectStyle);
                    break;
            }
        }
        return convertView;
    }

    /**
     * user的ViewHolder
     */
    public class UserViewHolder {
        UserRankView view;

        public UserViewHolder(UserRankView view) {
            this.view = view;
        }

        public void setData(Map<String, String> map) {
            if (view != null) {
                view.initView(map);
            }
        }
    }

    public class RecommendFriendViewHolder {
        RecommendFriendView view;

        public RecommendFriendViewHolder(RecommendFriendView view) {
            this.view = view;
        }

        public void setData(Map<String, String> map) {
            if (view != null) {
                view.initView(map);
            }
        }
    }

    public class NormalContentViewHolder {
        NormalContentView view;

        public NormalContentViewHolder(NormalContentView view) {
            this.view = view;
        }

        public void setData(final Map<String, String> map, final int position, int style) {
            if (view != null) {
                //添加美食帖统计code
                if (!map.containsKey("isPromotion"))
                    XHClick.saveCode(view.getContext(), map.get("code"));
                //设置是否显示抢沙发的title需要在initView前面
                view.setIsRobsof(AdapterCircle.STYLE_ROB_SOFA == style);
                //设置当前的position
//                view.setTag(NormalContentView.POSITION,position);
                view.setNeedRefresh(currentPlayPosition != position);

                //设置点击统计回调
                view.setmOnItemClickStatictis(new NormalContentView.OnItemClickStatictis() {
                    @Override
                    public void onStatictis(String onClickSite) {
                        if (map.containsKey("showMid") && map.containsKey("showCid")) {
                            if (showIndex.equals("home"))
                                HomeAdvertControl.getInstance().advertStatisticRequest(view.getContext(), map, onClickSite);
                            else
                                AdConfigTools.getInstance().postTongjiQuan(view.getContext(), map, onClickSite, "click");
                        }
                    }
                });
                //设置广告回调
                view.setOnAdCallback(new NormalContentView.OnAdCallback() {
                    @Override
                    public void onAdShow(View view) {
                        if (map.containsKey("isPromotion")) {
                            if (!"2".equals(map.get("isShow"))) {
                                quanAdvertControl.getXhAllAdControl().onAdBind(Integer.valueOf(map.get("indexInData")), view,
                                        map.get("promotionIndex"));
                                map.put("isShow", "2");
                            }
                        }
                    }

                    @Override
                    public void onAdClick(View view) {

                        quanAdvertControl.getXhAllAdControl().onAdClick(view,Integer.valueOf(map.get("indexInData")),
                                map.get("promotionIndex"));
                    }
                });
                view.setOnAdHintCallback(new NormalContentView.OnAdHintListener() {

                    @Override
                    public void onAdHintListener(View view, String eventID) {

                        AppCommon.onAdHintClick(mContext,quanAdvertControl.getXhAllAdControl(),Integer.valueOf(map.get("indexInData")), map.get("promotionIndex")
                                ,eventID,"第" + map.get("promotionIndex") + "位广告按钮");
                    }
                });
                //先写回调在执行展示代码
                view.setModuleName(moduleName);
                view.initView(map, Cid,position);
                view.setCircleName(circleName);
                if (!TextUtils.isEmpty(stiaticKey))
                    view.setStiaticKey(stiaticKey);
                setOnClickList(map);
                //视频被点击回调
                view.setVideoClickCallBack(new NormarlContentItemImageVideoView.VideoClickCallBack() {
                    @Override
                    public void videoImageOnClick(int position) {
                        Log.i("zhangyujian", "position::"+position);//被点击
                        videoClickCallBack.videoImageOnClick(position);
                    }
                });
            }
        }

        /**
         * 对数据是否是广告进行判断并进行数据配置上
         *
         * @param map
         */
        private void setOnClickList(Map<String, String> map) {
            if (map.containsKey("showCid") && map.containsKey("showMid")) {
                ArrayList<String> list = new ArrayList<String>();
                ArrayList<Map<String, String>> customers = UtilString.getListMapByJson(map.get("customer"));
                if (customers.get(0).containsKey("url"))
                    list.add(customers.get(0).get("url"));
                if (map.containsKey("url"))
                    list.add(map.get("url"));
                view.setOnClickData(list);
            }
        }
    }

    public void setmRecommendCutomerCallBack(RecommendFriendView.RecommendCutomerCallBack callback) {
        this.mRecommendCutomerCallBack = callback;
    }

    public int getCurrentPlayPosition() {
        return currentPlayPosition;
    }

    public void setCurrentPlayPosition(int currentPlayPosition) {
        this.currentPlayPosition = currentPlayPosition;
    }
    //对view进行
    private NormarlContentItemImageVideoView.VideoClickCallBack videoClickCallBack;
    public void setVideoClickCallBack(NormarlContentItemImageVideoView.VideoClickCallBack videoClickCallBack){
        this.videoClickCallBack =videoClickCallBack;
    }
}
