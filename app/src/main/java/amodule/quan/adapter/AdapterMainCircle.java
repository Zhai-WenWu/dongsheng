package amodule.quan.adapter;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.quan.tool.HomeAdvertControl;
import amodule.quan.tool.QuanAdvertControl;
import amodule.quan.view.NormalContentView;
import amodule.quan.view.NormarlContentItemImageVideoView;
import amodule.quan.view.RecommendFriendView;
import amodule.quan.view.UserRankView;
import third.ad.tools.AdConfigTools;
import xh.basic.tool.UtilString;

public class AdapterMainCircle extends RvBaseAdapter<Map<String, String>> {
    public static final String STYLE_NORMAL = "1";
    public static final String STYLE_ROB_SOFA = "5";
    public static final String STYLE_FRIEND_RECOMMEND = "6";

    public static final String DATATYPE_SUBJECT = "1";
    public static final String DATATYPE_USER = "2";
    public static final String DATATYPE_CIRCLE = "3";

    public static final int VIEWTYPE_USER = 1;
    public static final int VIEWTYPE_NORMAL = 2;
    public static final int VIEWTYPE_ROB_SOFA = 3;
    public static final int VIEWTYPE_FRIEND_RECOMMEND = 4;

    private String moduleName = "";
    private String circleName = "";
    private Activity mActivity;
    private List<Map<String, String>> mRecCutomerArray = null;
    private boolean statis_isHome = false;//统计是否是首页
    private String Cid;
    private String stiaticKey = "";
    private RecommendFriendView.RecommendCutomerCallBack mRecommendCutomerCallBack;
    public int index_user = 0;
    private String showIndex = "quan";
    private int currentPlayPosition = -1;
    private QuanAdvertControl quanAdvertControl;

    public AdapterMainCircle(Activity context, @Nullable List<Map<String, String>> data) {
        super(context, data);
        this.mActivity = context;
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_USER:
                return new UserViewHolder(new UserRankView(mContext));
            case VIEWTYPE_NORMAL:
            case VIEWTYPE_ROB_SOFA:
                return new NormalContentViewHolder(new NormalContentView(mActivity));
            case VIEWTYPE_FRIEND_RECOMMEND:
                return new RecommendFriendViewHolder(new RecommendFriendView(mContext));
            default:
                return new RvBaseViewHolder<Map<String, String>>(new View(mContext)) {
                    @Override
                    public void bindData(int position, @Nullable Map<String, String> data) {
                    }
                };
        }
    }

    @Override
    public int getItemViewType(int position) {
        Map<String, String> map = getItem(position);
        String dataTypeStr = map.get("dataType");
        String styleStr = map.get("style");
        if (DATATYPE_USER.equals(dataTypeStr)) {
            return VIEWTYPE_USER;
        } else if (DATATYPE_SUBJECT.equals(dataTypeStr)) {
            switch (styleStr) {
                case STYLE_NORMAL:
                    return VIEWTYPE_NORMAL;
                case STYLE_ROB_SOFA:
                    return VIEWTYPE_ROB_SOFA;
                case STYLE_FRIEND_RECOMMEND:
                    return VIEWTYPE_FRIEND_RECOMMEND;
            }
        }
        return 0;
    }

    public void setQuanAdvertControl(QuanAdvertControl quanAdvertControls) {
        this.quanAdvertControl = quanAdvertControls;
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

    /**
     * user的ViewHolder
     */
    public class UserViewHolder extends RvBaseViewHolder<Map<String, String>> {
        UserRankView view;

        public UserViewHolder(UserRankView view) {
            super(view);
            this.view = view;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (view != null) {
                view.initView(data);
            }
        }
    }

    public class RecommendFriendViewHolder extends RvBaseViewHolder<Map<String, String>> {
        RecommendFriendView view;

        public RecommendFriendViewHolder(RecommendFriendView view) {
            super(view);
            this.view = view;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (view != null) {
                view.setUserIndexCallback(new RecommendFriendView.UserIndexCallback() {
                    @Override
                    public int getUserIndex() {
                        return index_user;
                    }

                    @Override
                    public void plusUserIndex() {
                        index_user++;
                    }
                });
                view.initView(data);
                view.setStaticID(stiaticKey);
                if (mRecCutomerArray != null) {
                    view.setRecCutomerArray(mRecCutomerArray);
                }
                view.setRecommendCutomerCallBack(mRecommendCutomerCallBack);
            }
        }
    }

    public class NormalContentViewHolder extends RvBaseViewHolder<Map<String, String>> {
        NormalContentView view;

        public NormalContentViewHolder(NormalContentView view) {
            super(view);
            this.view = view;
        }

        @Override
        public void bindData(int position, @Nullable final Map<String, String> data) {
            if (view != null) {
                //添加美食贴统计code
                if (!data.containsKey("isPromotion"))
                    XHClick.saveCode(view.getContext(), data.get("code"));
                //设置是否显示抢沙发的title需要在initView前面
                view.setIsRobsof(AdapterMainCircle.STYLE_ROB_SOFA == data.get("style"));
                //设置当前的position
//                view.setTag(NormalContentView.POSITION,position);
                view.setNeedRefresh(currentPlayPosition != position);

                //设置点击统计回调
                view.setmOnItemClickStatictis(new NormalContentView.OnItemClickStatictis() {
                    @Override
                    public void onStatictis(String onClickSite) {
                        if (data.containsKey("showMid") && data.containsKey("showCid")) {
                            if (showIndex.equals("home"))
                                HomeAdvertControl.getInstance().advertStatisticRequest(view.getContext(), data, onClickSite);
                            else
                                AdConfigTools.getInstance().postTongjiQuan(view.getContext(), data, onClickSite, "click");
                        }
                    }
                });
                //设置广告回调
                view.setOnAdCallback(new NormalContentView.OnAdCallback() {
                    @Override
                    public void onAdShow(View view) {
                        if (data.containsKey("isPromotion")) {
                            if (!"2".equals(data.get("isShow"))) {
                                quanAdvertControl.onAdBind(data.get("controlTag"), Integer.valueOf(data.get("indexInData")), view,
                                        data.get("promotionIndex"));
                                data.put("isShow", "2");
                            }
                        }
                    }

                    @Override
                    public void onAdClick(View view) {

                        quanAdvertControl.onAdClick(data.get("controlTag"), view, Integer.valueOf(data.get("indexInData")),
                                data.get("promotionIndex"));
                    }
                });
                view.setOnAdHintCallback(new NormalContentView.OnAdHintListener() {

                    @Override
                    public void onAdHintListener(View view, String eventID) {
                        if (data.containsKey("controlTag") && !TextUtils.isEmpty(data.get("controlTag"))) {
                            AppCommon.onAdHintClick(mActivity, quanAdvertControl.getXhAllAdControl(data.get("controlTag")), Integer.valueOf(data.get("indexInData")), data.get("promotionIndex")
                                    , eventID, "第" + data.get("promotionIndex") + "位广告按钮");
                        }
                    }
                });
                //先写回调在执行展示代码
                view.setModuleName(moduleName);
                view.initView(data, position);
                view.setCircleName(circleName);
                if (!TextUtils.isEmpty(stiaticKey))
                    view.setStiaticKey(stiaticKey);
                setOnClickList(data);
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

    public void setVideoClickCallBack(NormarlContentItemImageVideoView.VideoClickCallBack videoClickCallBack) {
        this.videoClickCallBack = videoClickCallBack;
    }
}
