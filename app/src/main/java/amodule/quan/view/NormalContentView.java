package amodule.quan.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.StringManager;
import amodule.quan.tool.NormarlContentData;
import com.xh.windowview.BottomDialog;

import static third.ad.scrollerAd.XHScrollerAdParent.ADKEY_GDT;
import static third.ad.scrollerAd.XHScrollerAdParent.ID_AD_ICON_GDT;

/**
 * 正常贴子内容
 */
@SuppressLint("ClickableViewAccessibility")
public class NormalContentView extends CircleItemBaseRelativeLayout {
    public static final int POSITION = 911;

    private Activity context;
    private Map<String, String> map;
    private String moduleName;
    private String circleName;
    private ArrayList<String> onClickData = new ArrayList<>();

    private OnAdCallback mOnAdCallback;
    private OnAdHintListener mAdHintCallback;
    private OnItemClickStatictis mOnItemClickStatictis;
    public static final String INDEX_OVERALL = "overall";// 整体
    public static final String INDEX_USER = "user";// 用户
    public static final String INDEX_TIME = "time";// 时间
    public static final String INDEX_QUANNAME = "quanName";// 圈子名称
    public static final String INDEX_CONTENT = "content";// 评论
    public static final String INDEX_LIKE = "like";// 赞

    private String statisID = "a_quan_homepage";
    private String statisKey = "贴子";
    private boolean needRefresh = false;
    public boolean isRobsof = false;
    private NormalContentItemUserView normalContentItemUserView;
    private NormarlContentItemImageVideoView normarlContentItemImageVideoView;
    private NormarlContentItemfootView normarlContentItemfootView;
    private NormarlContentData normarlContentData;
    private int position;
    private  BottomDialog bottomDialog;
    private boolean isOnClickUser=true;//用户头像是否可点击
    private int position_now=-1;
    public NormalContentView(Activity context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.circle_invitationcontent, this, true);
        normarlContentData= new NormarlContentData();
        this.context = context;
    }
    /**
     * 设置统计的key 和id
     * @param statisIDs
     * @param statisKey
     */
    public void setStatisIDNew(String statisIDs,String statisKey){
        this.statisID=statisIDs;
        this.statisKey=statisKey;
        normarlContentData.setStatisID(statisID);
        normarlContentData.setStatisKey(statisKey);
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
        normarlContentData.setModuleName(moduleName);
    }
    public void setStiaticKey(String stiaticKey) {
        this.statisID = stiaticKey;
        normarlContentData.setStatisID(statisID);
    }
    public void setIsRobsof(boolean isRobsof) {
        this.isRobsof=isRobsof;
        statisKey = isRobsof ? "抢沙发" : "贴子";
        normarlContentData.setStatisKey(statisKey);
    }
    public void setCircleName(String circleName) {
        this.circleName = circleName;
        normarlContentData.setCircleName(circleName);
    }

    /**
     * 是否可点击头像---在initView方法之前调用
     * @param isOnClickUser
     */
    public void setIsClickUser(boolean isOnClickUser){
        this.isOnClickUser=isOnClickUser;
    }
    /**
     * 设置点击事件
     *
     * @param onClickData
     */
    public void setOnClickData(ArrayList<String> onClickData) {
        this.onClickData = onClickData;
    }

    /**
     * 初始化view
     *
     * @param maps
     */
    public void initView(Map<String, String> maps,int nowPosition) {
        this.map = maps;
        this.position_now = nowPosition;
        //标准user
        if(normalContentItemUserView==null){
            normalContentItemUserView = new NormalContentItemUserView(context,this);
            normalContentItemUserView.setIsOnClickUser(isOnClickUser);
            normalContentItemUserView.initView();
        }
        normalContentItemUserView.setIsRobof(isRobsof);
        normalContentItemUserView.setNormarlContentData(normarlContentData);
        //中间view
        if(normarlContentItemImageVideoView==null){
            normarlContentItemImageVideoView= new NormarlContentItemImageVideoView(context,this);
            normarlContentItemImageVideoView.initView();
        }

        normarlContentItemImageVideoView.setNormarlContentData(normarlContentData);
        //处理断点击数据
        handleonClickData();
        //底部view
        if(normarlContentItemfootView==null){
            normarlContentItemfootView= new NormarlContentItemfootView(context,this);
            normarlContentItemfootView.initView();
        }
        normarlContentItemImageVideoView.setNormarlContentData(normarlContentData);
        normarlContentItemfootView.setFootViewCallback(new NormarlContentItemfootView.FootViewCallback() {
            @Override
            public void onClicklike(String site) {
                setIndexStatictis(site);
            }

            @Override
            public void onClickview(String site) {
                String isSafa = "";
                if (map.containsKey("isSafa"))
                    isSafa = map.get("isSafa");
                if ("6".equals(map.get("style")))
                    isSafa = "qiang";
                if (onClickData.size() >= 2) {
                    onClickData.add(1, "subjectInfo.app?code=" + map.get("code") + "&isSafa=" + isSafa);
                } else {
                    onClickData.add("subjectInfo.app?code=" + map.get("code") + "&isSafa=" + isSafa);
                }
            }
        });

        //设置userview 数据
        normalContentItemUserView.setViewData(map,nowPosition);
        normalContentItemUserView.setNormarlViewOnClickCallBack(new NormarlContentItemView.NormarlViewOnClickCallBack() {
            @Override
            public void onClickViewIndex(int type, String statisValue) {
                getOnclick(normalContentItemUserView,type,statisValue);
            }
        });
        normalContentItemUserView.setAdHintClickCallback(new OnAdHintListener() {
            @Override
            public void onAdHintListener(View view, String eventID) {
                setOnAdHintClick(view,eventID);
            }
        });
        //设置img view数据
        normarlContentItemImageVideoView.setViewData(map,nowPosition);
        normarlContentItemImageVideoView.setNormarlViewOnClickCallBack(new NormarlContentItemView.NormarlViewOnClickCallBack() {
            @Override
            public void onClickViewIndex(int type, String statisValue) {
                getOnclick(normarlContentItemImageVideoView,type,statisValue);
            }
        });

        normarlContentItemImageVideoView.setVideoClicCallBack(new NormarlContentItemImageVideoView.VideoClickCallBack() {
            @Override
            public void videoImageOnClick(int position) {
                if(videoClickCallBack!=null)
                    videoClickCallBack.videoImageOnClick(position);
            }
        });

        //foot view
        normarlContentItemfootView.setViewData(map,nowPosition);
        normarlContentItemfootView.setNormarlViewOnClickCallBack(new NormarlContentItemView.NormarlViewOnClickCallBack() {
            @Override
            public void onClickViewIndex(int type, String statisValue) {
                getOnclick(normarlContentItemfootView,type,statisValue);
            }
        });

        if ((map.containsKey("code") && !TextUtils.isEmpty(map.get("code")) || map.containsKey("url") && !TextUtils.isEmpty(map.get("url")))
                && (!map.containsKey("uploadState") || "3000".equals(map.get("uploadState")))) {// 初始化只要正常贴和上传中状态
            setShowUpload(true);
        } else {
            setShowUpload(false);
        }
        setShowIndex();

        View view = findViewById(ID_AD_ICON_GDT);
        if(view != null){
            view.setVisibility(ADKEY_GDT.equals(map.get("adType"))?VISIBLE:GONE);
        }
    }

    /**
     * 点击统计
     * @param v
     * @param type
     * @param statisValue
     */
    private void getOnclick(View v,int type,String statisValue){
        if (map.containsKey("isPromotion")) {
            setOnClick(v);
        } else if ("评论".equals(statisValue)) {
            goNextIndex(type, "&isReplayFloorOwner=1");
        } else
            goNextIndex(type);
        setIndexStatictis(statisValue);
    }
    /** 重新开始videoView */
    public void startVideoView(){
        normarlContentItemImageVideoView.startVideoView();
    }

    /** 暂停videoView */
    public void stopVideoView(){
        normarlContentItemImageVideoView.stopVideoView();
    }

    /**
     * 设置贴子当前状态
     *
     * @param state true表示当前正常，上传成功的贴子 false 真在 上传中
     */
    public void setShowUpload(boolean state) {
        normalContentItemUserView.setShowUpload(state);
        normarlContentItemImageVideoView.setShowUpload(state);
        normarlContentItemfootView.setShowUpload(state);
    }

    /**
     * 设置code数据
     *
     * @param code
     */
    public void setCode(String code) {
        map.put("code", code);
    }

    private void handleonClickData() {
        try {
            onClickData.clear();
            onClickData.add("userIndex.app?code=" + StringManager.getListMapByJson(map.get("customer")).get(0).get("code"));
            String isSafa = "";
            if (map.containsKey("isSafa"))
                isSafa = map.get("isSafa");
            if (map.containsKey("style") && map.get("style").equals("6"))
                isSafa = "qiang";
            if (map.containsKey("url") && !TextUtils.isEmpty(map.get("url"))) {
                onClickData.add(map.get("url"));
            } else {
                onClickData.add("subjectInfo.app?code=" + map.get("code")  + "&isSafa=" + isSafa);
            }
            onClickData.add("circleHome?cid=" + map.get("cid"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void goNextIndex(int index) {
        goNextIndex(index, "");
    }

    /**
     * 用户点击位置
     *
     * @param index
     */
    private void goNextIndex(int index, String suffix) {
        if (onClickData.size() >= index + 1) {
            if (TextUtils.isEmpty(suffix)) {
                AppCommon.openUrl(context, onClickData.get(index), true);
            } else {
                AppCommon.openUrl(context, onClickData.get(index) + suffix, true);
            }
        } else {
            if (onClickData.size() > 0) {
                if (TextUtils.isEmpty(suffix)) {
                    AppCommon.openUrl(context, onClickData.get(onClickData.size() - 1), true);
                } else {
                    AppCommon.openUrl(context, onClickData.get(onClickData.size() - 1) + suffix, true);
                }
            }
        }
    }
    /**
     * 点击统计
*
     * @param index ：：点击位置
     */
    private void setIndexStatictis(String index) {
        if (mOnItemClickStatictis != null) {
            mOnItemClickStatictis.onStatictis(index);
        }
    }

    /**
     * 广告点击
     *
     * @param v
     */
    public void setOnClick(View v) {
        if (mOnAdCallback != null) {
            mOnAdCallback.onAdClick(v);
        }
    }

    public void setOnAdHintClick(View v,String evendId){
        if(mAdHintCallback != null) mAdHintCallback.onAdHintListener(v,evendId);
    }

    /** 展示统计 */
    private void setShowIndex() {
        if (mOnAdCallback != null) {
            mOnAdCallback.onAdShow(this);
        }
    }


    /** 广告回调 */
    public interface OnAdCallback {
        public void onAdShow(View view);
        public void onAdClick(View view);
    }

    /** 点击统计 */
    public interface OnItemClickStatictis {
        public void onStatictis(String index);
    }

    public void setOnAdCallback(OnAdCallback onAdCallback) {
        this.mOnAdCallback = onAdCallback;
    }

    public void setOnAdHintCallback(OnAdHintListener listener){
        mAdHintCallback = listener;
    }
    public interface OnAdHintListener{
        public void onAdHintListener(View view,String eventID);
    }


    public void setmOnItemClickStatictis(OnItemClickStatictis mOnItemClickStatictis) {
        this.mOnItemClickStatictis = mOnItemClickStatictis;
    }
    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        this.needRefresh = needRefresh;
        if(normarlContentItemImageVideoView!=null){
            normarlContentItemImageVideoView.setNeedRefresh(this.needRefresh);
        }

    }
    /**
     * 删除回调
     */
    public void setOnTitleTopStateCallBack(final OnTitleTopStateCallBack callBack, int position){
        this.position=position;
        //title回调
        normalContentItemUserView.setOnTitleTopStateCallBack(new NormalContentItemUserView.OnTitleTopStateCallBack() {
            @Override
            public void onTitleTopStateCallBack(boolean isOk,boolean isConfirm,int position,Object data) {
                callBack.onTitleTopStateCallBack(isOk,isConfirm,position,data);
            }
        },position);
        if(normarlContentItemImageVideoView!=null)
            normarlContentItemImageVideoView.setNeedRefresh(needRefresh);
    }
    /**
     * 删除回调
     */
    public interface OnTitleTopStateCallBack{
        public void onTitleTopStateCallBack(boolean isOk,boolean isConfirm,int position,Object data);
    }

    /**
     * 删除回调
     */
    public void setDeleteSubjectCallBack(final DeleteSubjectCallBack deleteSubjectCallBack, int position){
        this.position=position;
        //title回调
        normalContentItemUserView.setTitleDeleteSubjectCallBacks(new NormalContentItemUserView.TitleDeleteSubjectCallBack() {
            @Override
            public void deleteSubjectPosition(int position) {
                deleteSubjectCallBack.deleteSubjectPosition(position);
            }
        },position);
        if(normarlContentItemImageVideoView!=null)
            normarlContentItemImageVideoView.setNeedRefresh(needRefresh);
    }
    /**
     * 删除回调
     */
    public interface DeleteSubjectCallBack{
        public void deleteSubjectPosition(int position);
    }
    public NormarlContentItemImageVideoView.VideoClickCallBack videoClickCallBack;
    public void setVideoClickCallBack(NormarlContentItemImageVideoView.VideoClickCallBack videoClickCallBack){
        this.videoClickCallBack=videoClickCallBack;
    }

    /**
     * 获取当前position
     * @return
     */
    public int getPositionNow(){
        return position_now;
    }
}
