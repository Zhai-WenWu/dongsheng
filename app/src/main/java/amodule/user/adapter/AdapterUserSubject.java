package amodule.user.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.quan.adapter.AdapterCircle;
import amodule.quan.view.NormalContentView;
import amodule.quan.view.NormarlContentItemImageVideoView;

/**
 * Created by XiangHa on 2016/9/20.
 */
public class AdapterUserSubject extends AdapterSimple {

    private Activity mContext;
    private List<Map<String, String>> mData;
    private String mTongjiId;

    private NormalContentView.DeleteSubjectCallBack mDeleteCallback;

    public AdapterUserSubject(Activity con, View parent, List<Map<String, String>> data, int resource, String[] from, int[] to, String tongjiId, NormalContentView.DeleteSubjectCallBack callBack) {
        super(parent, data, resource, from, to);
        mContext = con;
        mData = data;
        mTongjiId = tongjiId;
        mDeleteCallback = callBack;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NormalContentViewHolder normalContentViewHolder = null;
        if (convertView == null || !(convertView.getTag() instanceof AdapterCircle.NormalContentViewHolder)) {
            normalContentViewHolder = new NormalContentViewHolder(new NormalContentView(mContext));
            convertView = normalContentViewHolder.view;
            convertView.setTag(normalContentViewHolder);
        } else {
            normalContentViewHolder = (NormalContentViewHolder) convertView.getTag();
        }
        normalContentViewHolder.setData(mData.get(position), position);
        return convertView;
    }

    public class NormalContentViewHolder {
        NormalContentView view;

        public NormalContentViewHolder(NormalContentView view) {
            this.view = view;
        }

        public void setData(final Map<String, String> map, int position) {
            view.setIsClickUser(false);
            view.initView(map, position);
            view.setStatisIDNew(mTongjiId,"美食贴");
            view.setDeleteSubjectCallBack(mDeleteCallback,position);
            view.setOnTitleTopStateCallBack(new NormalContentView.OnTitleTopStateCallBack() {

                @Override
                public void onTitleTopStateCallBack(boolean isok,boolean isConfirm,int position,Object data) {
                    if(isok && isConfirm && isShowTopHint()) {
                        final DialogManager dialogManager = new DialogManager(mContext);
                        dialogManager.createDialog(new ViewManager(dialogManager)
                                .setView(new TitleView(mContext).setText("置顶成功"))
                                .setView(new MessageView(mContext).setText("非会员72小时失效，会员永久有效哦~立刻开通会员？"))
                                .setView(new HButtonView(mContext)
                                        .setNegativeText("取消", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialogManager.cancel();
                                            }
                                        })
                                        .setPositiveText("开通会员", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialogManager.cancel();
                                                AppCommon.openUrl(mContext, StringManager.getVipUrl(true) + "&vipFrom=个人主页置顶弹框", true);
                                            }
                                        }))).show();
                    }else{
                        Tools.showToast(mContext, String.valueOf(data));
                    }
                }
            }, position);
            view.setVideoClickCallBack(new NormarlContentItemImageVideoView.VideoClickCallBack() {
                @Override
                public void videoImageOnClick(int position) {
                    videoClickCallBack.videoImageOnClick(position);
                }
            });
        }
    }

    boolean isShow = true;
    private boolean isShowTopHint(){
//        //Log.i("FRJ","isShowTopHint() isShow:" + isShow);
        if(isShow){
            if(AppCommon.isVip(LoginManager.userInfo.get("vip"))){
                isShow = false;
            }else {
                String topState = String.valueOf(FileManager.loadShared(mContext, FileManager.xmlFile_appInfo, "topState"));
                if (!TextUtils.isEmpty(topState)) {
                    long currentS = System.currentTimeMillis();
                    long oldS = Long.valueOf(topState);
                    if (currentS - oldS < 72 * 60 * 60 * 1000) {
                        isShow = false;
                    }
                }
            }
        }
        if(isShow){
//            //Log.i("FRJ","saveShare");
            FileManager.saveShared(mContext,FileManager.xmlFile_appInfo,"topState",String.valueOf(System.currentTimeMillis()));
        }
        return isShow;
    }
    //对view进行
    private NormarlContentItemImageVideoView.VideoClickCallBack videoClickCallBack;
    public void setVideoClickCallBack(NormarlContentItemImageVideoView.VideoClickCallBack videoClickCallBack){
        this.videoClickCallBack =videoClickCallBack;
    }
}
