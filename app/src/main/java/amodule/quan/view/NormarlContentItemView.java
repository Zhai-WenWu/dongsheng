package amodule.quan.view;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

import acore.logic.XHClick;
import amodule.quan.tool.NormarlContentData;

/**
 * item 基础父类
 * 原则：不出来具体view,具体数据，遇到上述东西回调子类自己去处理
 */

public abstract class NormarlContentItemView extends CircleItemBaseRelativeLayout {
    public final int typeUser = 0;
    public final int typeSubject = 1;
    public final int typeCircle = 2;
    public final int typeAdHint= 3;
    public Activity context;
    public static final String STATISTICS_ID = "a_quan_click";
    public boolean isRobsof = false;
    public NormarlViewOnClickCallBack normarlViewOnClickCallBack;
    public NormalContentView.OnAdHintListener mAdHintClickCallback;
    public NormarlContentData normarlContentData= new NormarlContentData();
    public NormarlContentItemView(Activity context) {
        super(context);
        this.context = context;
    }

    public void setNormarlContentData(NormarlContentData normarlContentDatas){
        this.normarlContentData= normarlContentDatas;
    }
    /**
     * 添加回调
     * @param normarlViewOnClickCallBack
     */
    public void setNormarlViewOnClickCallBack(NormarlViewOnClickCallBack normarlViewOnClickCallBack){
        this.normarlViewOnClickCallBack= normarlViewOnClickCallBack;
    }

    public void setAdHintClickCallback(NormalContentView.OnAdHintListener listener){
        mAdHintClickCallback = listener;
    }
//    public void setStisticData(String statisID,String statisKey,String moduleName,String circleName){
//        this.statisID= statisID;
//        this.moduleName= moduleName;
//        this.statisKey=statisKey;
//        this.circleName=circleName;
//    }
    /**
     * 初始化view
     */
    protected abstract void initView();

    /**
     * 设置view的数据
     */
    public abstract void setViewData(Map<String,String> map,int position);

    /**
     * 设置view的状态---是否上传，还是标准数据
     * @param state true表示当前正常，上传成功的贴子 false 真在 上传中
     */
    public abstract void setShowUpload(boolean state);

    /**
     * view 被电击后，回调给view自己处理逻辑
     * @param type typeUser，typeSubject，typeCircle
     */
    public abstract void onClickCallback(int type,String statisValue);

    public abstract void onAdClickCallback(View view,String eventID);



    /**
     * 处理点击事件
     */
    public void setListener(final View view, final int type, final String statisValue) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type) {
                    case typeUser:
                        if (!TextUtils.isEmpty(normarlContentData.getModuleName())) {
                            if ("a_quan_homepage".equals(normarlContentData.getStatisID()))
                                XHClick.mapStat(context, normarlContentData.getStatisID(), normarlContentData.getModuleName(), "点击头像");
                            if (!TextUtils.isEmpty(normarlContentData.getCircleName())) {
                                XHClick.mapStat(context, STATISTICS_ID, normarlContentData.getCircleName(), normarlContentData.getModuleName() + "_用户头像");
                            }
                        }
                        onClickCallback(typeUser,statisValue);
                        XHClick.mapStat(context, normarlContentData.getStatisID(), normarlContentData.getStatisKey(), statisValue);
                        break;
                    case typeSubject:
                        if (!TextUtils.isEmpty(normarlContentData.getModuleName())) {
                            if ("a_quan_homepage".equals(normarlContentData.getStatisID()))
                                XHClick.mapStat(context, normarlContentData.getStatisID(), normarlContentData.getModuleName(), "点击内容");
                            if (!TextUtils.isEmpty(normarlContentData.getCircleName())) {
                                XHClick.mapStat(context, STATISTICS_ID, normarlContentData.getCircleName(), normarlContentData.getModuleName() + "_贴子内容");
                            }
                        }
                        onClickCallback(typeSubject,statisValue);
                        XHClick.mapStat(context, normarlContentData.getStatisID(), normarlContentData.getStatisKey(), statisValue);
                        break;
                    case typeCircle:// 点击去圈子
                        if (!TextUtils.isEmpty(normarlContentData.getModuleName())) {
                            if ("a_quan_homepage".equals(normarlContentData.getStatisID()))
                                XHClick.mapStat(context, normarlContentData.getStatisID(), normarlContentData.getModuleName(), "点击圈子");
                            if (!TextUtils.isEmpty(normarlContentData.getCircleName())) {
                                XHClick.mapStat(context, STATISTICS_ID, normarlContentData.getCircleName(), normarlContentData.getModuleName() + "_圈子");
                            }
                        }
                        onClickCallback(typeSubject,statisValue);
                        XHClick.mapStat(context, normarlContentData.getStatisID(), normarlContentData.getStatisKey(), statisValue);
                        break;
                    case typeAdHint:
                        onAdClickCallback(view,normarlContentData.getStatisID());
                        break;
                }
            }
        });

    }

    /**
     * map转json
     *
     * @param maps
     * @return
     */
    public JSONObject MapToJson(Map<String, String> maps) {

        Iterator<Map.Entry<String, String>> enty = maps.entrySet().iterator();
        JSONObject jsonObject = new JSONObject();
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
    public interface NormarlViewOnClickCallBack{
        public void onClickViewIndex(int type,String statisValue);
    }
}
