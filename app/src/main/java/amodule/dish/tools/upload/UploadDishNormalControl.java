package amodule.dish.tools.upload;

import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.helper.UploadHelper;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.ScrollviewDish;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.tools.UploadDishControl;
import amodule.dish.tools.UploadDishSpeechTools;
import amodule.dish.view.UploadDish.DishMainView;
import amodule.main.Main;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;
import xh.windowview.XhDialog;

/**
 * Created by Fang Ruijiao on 2016/10/25.
 */

public class UploadDishNormalControl extends UploadDishParrentControl implements View.OnClickListener{

    /** 发菜谱页面统计ID */
    public static final String STATISTICS_ID = "a_write_recipes";

    private DishMainView mDishMainView;
    public ScrollviewDish mScrollView;

    private UploadDishControl mUploadDishControl;

    public UploadDishNormalControl(UploadDishActivity act){
        super(act,R.layout.a_upload_dish_new_normall_layout,"发菜谱");
    }

    /**
     * 初始化View
     * 传菜谱界面主控制_构造 绑定点击事件 建立定时存草稿timer
     */
    @Override
    protected void initView() {
        //初始化的时候添加用户提示dialog
        mAct.loadManager.loadOver(uploadDishData==null? UtilInternet.REQ_FAILD:UtilInternet.REQ_OK_STRING, 1, true);
        if(uploadDishData==null) return;
        if(uploadDishData.getUploadTimeCode() <= 0){
            uploadDishData.setUploadTimeCode(System.currentTimeMillis());
        }
        uploadDishData.setVideType(false);
        View view = mAct.findViewById(R.id.a_dish_view);
        TextView draft_Tv = (TextView) mAct.findViewById(R.id.rightText);
        TextView delete_Tt = (TextView) mAct.findViewById(R.id.delete_btn);
        if(isUploadDishEdit){delete_Tt.setText("删除本草稿");}
        draft_Tv.setText("存草稿");
        draft_Tv.setVisibility(View.VISIBLE);
        mDishMainView = new DishMainView(mAct, view,uploadDishData);
        draft_Tv.setOnClickListener(this);
        mScrollView = (ScrollviewDish)mAct.findViewById(R.id.scrollView);
        mUploadDishControl = UploadDishControl.getInstance();
        View btn_back = mAct.findViewById(R.id.ll_back);
        btn_back.setClickable(true);
        btn_back.setOnClickListener(mAct.getBackBtnAction());
        btn_back.setVisibility(View.VISIBLE);
        mAct.findViewById(R.id.submit_btn).setOnClickListener(this);
        delete_Tt.setOnClickListener(this);
    }

    /**
     * 从数据库读取草稿数据并回调initView
     */
    @Override
    protected void initDraftDishView(final int id) {
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {
                initView();
                if(mDishMainView!= null)
                    mDishMainView.onResultBack(UploadDishActivity.DISH_DRAFT_IN, new Intent().putExtra("DISH_DRAFT_IN", true));
            };
        };
        new Thread(new Runnable() {

            @Override
            public void run() {
                UploadDishSqlite mDishSqlite=new UploadDishSqlite(mAct.getApplicationContext());
                uploadDishData = mDishSqlite.selectById(id);
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    /**
     * 获取网络菜谱数据并回调initView
     */
    @Override
    protected void initNetDishView(String dishCode) {
        ReqInternet.in().doGet(StringManager.api_getDishInfo + "?code=" + dishCode+"&isNew=1", new InternetCallback(mAct) {

            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
                    if (list.size() > 0) {
                        Map<String, String> theDish = list.get(0);
                        uploadDishData=new UploadDishData();
                        uploadDishData.setCode(theDish.get("code"));
                        // 设置title
                        uploadDishData.setName(theDish.get("name"));
                        // 大图
                        uploadDishData.setCover(theDish.get("img"));

                        ArrayList<Map<String, String>> newList = UtilString.getListMapByJson(theDish.get("burden"));
                        JSONArray burdenArray = new JSONArray();
                        JSONArray foodArray = new JSONArray();
                        for (int i = 0; i < newList.size(); i++) {
                            Map<String, String> map = newList.get(i);
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("name", map.get("name"));
                                jsonObject.put("number", map.get("content"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (map.get("type").equals("1")){
                                foodArray.put(jsonObject);
                            }else{
                                burdenArray.put(jsonObject);
                            }
                        }
                        // 设置食材
                        uploadDishData.setFood(foodArray.toString());
                        // 设置配料数据
                        uploadDishData.setBurden(burdenArray.toString());
                        newList = UtilString.getListMapByJson(theDish.get("makes"));
                        JSONArray makeArray = new JSONArray();
                        for (int i = 0; i < newList.size(); i++) {
                            Map<String, String> map = newList.get(i);
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("makesImg", map.get("img"));
                                jsonObject.put("makesInfo", map.get("info"));
                                jsonObject.put("makesStep", map.get("num"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            makeArray.put(jsonObject);
                        }
                        // 设置做法数据
                        uploadDishData.setMakes(makeArray.toString());
                        // 配置小贴士
                        uploadDishData.setTips(theDish.get("remark"));
                        // 设置故事数据
                        uploadDishData.setStory(theDish.get("info"));
                        uploadDishData.setReadyTime(theDish.get("readyTime"));
                        uploadDishData.setCookTime(theDish.get("cookTime"));
                        uploadDishData.setTaste(theDish.get("tagIds"));
                        uploadDishData.setDiff(theDish.get("diff"));
                        uploadDishData.setExclusive(theDish.get("exclusive"));
                        uploadDishData.setVideType(false);

                        initView();
                    }
                    else {
                        Tools.showToast(mAct, "抱歉，未找到相应菜谱");
                        mAct.finish();
                    }
                }
            }
        });
    }


    /**
     * 存草稿
     * @param subjectType 存储菜谱的不同状态(标示菜谱的状态： 草稿、发布中、编辑中、后台发布 )
     * 调用数据层存菜谱草稿数据
     */
    @Override
    public int onSaveDraft(String subjectType) {
        if(mDishMainView == null) return -1;
        if(!mDishMainView.checkDishDataIsEmpty()) return -3;
        uploadDishData = mDishMainView.getDishData();
        uploadDishData.setDishType(subjectType);
        uploadDishData.setVideType(false);
        UploadDishSqlite mDishSqlite=new UploadDishSqlite(mAct.getApplicationContext());
        //如果是数据库已有字段,更新
        if(uploadDishData.getId() > 0){
            mDishSqlite.update(uploadDishData.getId(), uploadDishData);
        }else {uploadDishData.setId(mDishSqlite.insert(uploadDishData));}
        return uploadDishData.getId();
    }

    /**
     * 点击发菜谱
     */
    @Override
    protected synchronized void onClickUploadDish() {
        XHClick.mapStat(mAct, STATISTICS_ID, "立即发布", "");
        if (ifCanUpload) {
            ifCanUpload = false;
            if(LoginManager.isLogin()){
                //只有新菜谱才存草稿
                if(TextUtils.isEmpty(uploadDishData.getCode()))
                    onSaveDraft(UploadDishData.UPLOAD_DRAF);
                else uploadDishData = mDishMainView.getDishData();
                String failedStr = checkUploadDishData();
                if(!TextUtils.isEmpty(failedStr)) {
                    Tools.showToast(mAct, failedStr);
                    ifCanUpload=true;
                    return;
                }
                //关闭类似草稿之类的页面
                Main.colse_level=5;
                //结束定时存储草稿的操作
                timer.cancel();
                if(TextUtils.isEmpty(uploadDishData.getCode()))
                    onSaveDraft(UploadDishData.UPLOAD_ING);
                //开始发布页面逻辑处理
                UploadHelper.UploadCallback uploadCallback = mUploadDishControl.getUploadCallback();
                if(uploadCallback==null){
                    Intent intentMyDish = new Intent(mAct, FriendHome.class);
                    intentMyDish.putExtra("code",LoginManager.userInfo.get("code"));
                    intentMyDish.putExtra("index",1);
                    mAct.startActivity(intentMyDish);
                }else{
                    uploadCallback.uploading(uploadDishData.getId());
                }
                mAct.finish();
                mUploadDishControl.startUpload(uploadDishData);
                XHClick.track(mAct,"发菜谱");
            }else {
                Intent it = new Intent(mAct, LoginByAccout.class);
                mAct.startActivity(it);
            }
            ifCanUpload = true;
        } else {
            Tools.showToast(mAct, "菜谱正在发布，请勿重复点击！");
        }
    }

    /**
     * 数据校验
     * 正常则返回空字符串，否则返回错误信息
     */
    private String checkUploadDishData() {
        boolean dateIsOk = true;
        String resultToastInfo = "";
        boolean isUploading = mUploadDishControl.ifUploading(uploadDishData.getUploadTimeCode());// 设置上传菜谱code
        if (isUploading) {
            resultToastInfo = "菜谱正在发布，请稍等！";
            XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "当前有菜谱正在发布，请稍等！", 0);
            dateIsOk = false;
        } else {
            ArrayList<Map<String, String>> food = UtilString.getListMapByJson(uploadDishData.getFood());
            if (TextUtils.isEmpty(uploadDishData.getCover().toString().trim())) {
                resultToastInfo = "菜谱效果图不能为空";
                XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "菜谱效果图不能为空", 0);
                dateIsOk = false;
            }else if (TextUtils.isEmpty(uploadDishData.getName()) || uploadDishData.getName().length() <= 0) {
                resultToastInfo = "菜谱名字不能为空";
                XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "菜谱名字不能为空", 0);
                dateIsOk = false;
            }else if(!uploadDishData.getCheckGreement()){
                resultToastInfo = "同意原创内容发布协议后才能提交哦";
                XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "没有确认香哈协议", 0);
                dateIsOk = false;
            }else{
                if (food.size() < 1) {
                    resultToastInfo = "食材不足1项";
                    XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "食材不足1项", 0);
                    dateIsOk = false;
                }else{
                    if(mDishMainView.getCurrentCreatingNum() > 0 || !mDishMainView.getImgIsCreateOk()){
                        resultToastInfo = "正在加载图片，请稍等";
                        dateIsOk = false;
                    }else{
                        ArrayList<Map<String, String>> makes = UtilString.getListMapByJson(uploadDishData.getMakes());
                        if (makes.size() >= 3) {
                            for (int i = 0; i < makes.size(); i++) {
                                String makesInfo = makes.get(i).get("makesInfo").trim();
                                if (TextUtils.isEmpty(makesInfo)) {
                                    resultToastInfo = "步骤" + "没有写步骤说明哦";
                                    XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "请完善步骤说明", 0);
                                    dateIsOk = false;
                                    break;
                                }
                            }
                        } else {
                            resultToastInfo = "步骤太少,最少3步哦";
                            XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "步骤太少,最少3步哦", 0);
                            dateIsOk = false;
                        }
                    }
                }
            }
        }
        if(dateIsOk) XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "校验成功", 100);
        return resultToastInfo;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mDishMainView != null) mDishMainView.onResultBack(requestCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        UploadDishSpeechTools.createUploadDishSpeechTools().onDestroy();
    }

    @Override
    public void onPause() {
        timer.cancel();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submit_btn:
                onClickUploadDish();
                break;
            case R.id.rightText:
                int value = onSaveDraft(UploadDishData.UPLOAD_DRAF);
                if (value > 0)
                    Tools.showToast(mAct.getApplicationContext(), "已保存该菜谱为草稿");
                else if(value == -3)
                    Tools.showToast(mAct.getApplicationContext(), "还没有编写内容哦");
                break;
            case R.id.delete_btn:
                final XhDialog vsDialog = new XhDialog(mAct);
                vsDialog.setTitle("是否删除这个草稿呢？")
                        .setSureButton("删除", new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "点击删除这个草稿", "");
                                int id=uploadDishData.getId();
                                if (id > 0) {
                                    UploadDishSqlite mDishSqlite=new UploadDishSqlite(mAct.getApplicationContext());
                                    mDishSqlite.deleteById(id);
                                }
                                mAct.finish();
                            }
                        })
                        .setCanselButton("取消", new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                vsDialog.cancel();
                            }
                        });
                vsDialog.show();
                break;
        }
    }
}
