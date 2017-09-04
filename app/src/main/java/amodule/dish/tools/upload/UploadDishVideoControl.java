package amodule.dish.tools.upload;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.AgreementManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.activity.upload.UploadDishMakeOptionActivity;
import amodule.dish.adapter.AdapterDishNewMake;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.tools.MediaReqDataContorl;
import amodule.dish.tools.MediaStorageControl;
import amodule.dish.tools.UploadDishContentControl;
import amodule.dish.tools.UploadDishSpeechTools;
import amodule.dish.video.View.MediaListViewSurfaceVideoView;
import amodule.dish.video.activity.MediaHandleActivity;
import amodule.dish.video.activity.MediaPaperActivity;
import amodule.dish.view.UploadDish.DishIngredientView;
import amodule.dish.view.UploadDish.DishOtherControl;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;
import aplug.recordervideo.activity.ChooseVideoActivity;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilString;
import xh.windowview.XhDialog;

/**
 * Created by Fang Ruijiao on 2016/10/25.
 */
public class UploadDishVideoControl extends UploadDishParrentControl implements View.OnClickListener{
    private ListView mListView;
    private AdapterDishNewMake adapterDishNewMake;
    private ArrayList<Map<String,String>> mArrayList;

    private DishIngredientView mDishIngredientView;
    private DishOtherControl mDishOtherControl;
    private AgreementManager mAgreementManager;

    private ImageView dishImageIv;
    private EditText mDishNameEt,mDishIdeaEt,mDishTipsEt;
    private TextView mVideAllTime,mVideoAllTimeHint;
    private float mAllVideoTime = 0;

    /**效果图图片地址*/
    private String imgUrl = "";
    //用于正在加载图片，此时点上传按钮，会提示效果图为空问题
    private boolean imgIsCreateOk = true;

    private MediaReqDataContorl mediaReqDataContorl;

    private MediaListViewSurfaceVideoView viewSurfaceVideoView;
    private LinearLayout currentVideoViewParentLayout;

    public final static String tongjiId = "a_write_dishvideo";
    //步骤描述最多多少字
    public final static int MAKE_INFO_MAX_TEXT = 25;
    //视频长度最长多少秒
    public final static int MAKE_VIDEO_MAX_S = 15;
    //视频每秒几个字
    public final static int MAKE_VIDEO_S_TEXT = 3;

    public UploadDishVideoControl(UploadDishActivity act){
        super(act,R.layout.a_upload_dish_new_video_layout,"视频菜谱");
        initSpeech();
    }

    @Override
    protected void initView() {
        if(mArrayList == null || mArrayList.size() == 0){
            mArrayList = new ArrayList<>();
            Map<String,String> map;
            for(int i = 1; i < 4; i ++){
                map = new HashMap<>();
                map.put("makesStep",String.valueOf(i));
                map.put("makesInfo","");
                map.put("videoInfo","");
                map.put("videoPlayState","1");
                mArrayList.add(map);
            }
        }else{
            frfrashData();
        }
        mListView = (ListView) mAct.findViewById(R.id.a_dish_upload_new_video_layout_listview);
        View headView = inflater.inflate(R.layout.a_upload_dish_new_item_header,null);
        View footView = inflater.inflate(R.layout.a_upload_dish_new_item_footer,null);
        mListView.addHeaderView(headView);
        mListView.addFooterView(footView);
        adapterDishNewMake = new AdapterDishNewMake(mAct,mListView,mArrayList,0,null,null);
        mListView.setAdapter(adapterDishNewMake);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(viewSurfaceVideoView != null && viewSurfaceVideoView.isPlay()){
                    stopVideo();
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
        });
        adapterDishNewMake.setOnStarListener(new AdapterDishNewMake.OnStarListener(){
            @Override
            public void OnClickListener(int index) {
                if(viewSurfaceVideoView == null){
                    viewSurfaceVideoView = new MediaListViewSurfaceVideoView(mAct);
                }
                if(currentVideoViewParentLayout != null && currentVideoViewParentLayout.getChildCount() > 0){
                    currentVideoViewParentLayout.removeView(viewSurfaceVideoView);
                }
                onPause();
                int firstVisiPosi = mListView.getFirstVisiblePosition();
                //要获得listview的第n个View,则需要n减去第一个可见View的位置。+1是因为有header
                View parentView = mListView.getChildAt(index - firstVisiPosi + 1);
                currentVideoViewParentLayout = (LinearLayout) parentView.findViewById(R.id.video_makes_parent);
                currentVideoViewParentLayout.addView(viewSurfaceVideoView);
                Map<String,String> makeMap = mArrayList.get(index);
                String videoInfo = makeMap.get("videoInfo");
                viewSurfaceVideoView.setInfo(videoInfo);
                viewSurfaceVideoView.onClickView();
                viewSurfaceVideoView.setPlayCallBack(new MediaListViewSurfaceVideoView.ViewPlayCallBack() {
                    @Override
                    public void playState(boolean state) {
                        if(state){
                            stopVideo();
                        }
                    }
                });
            }
        });
        adapterDishNewMake.setOnCutListener(new AdapterDishNewMake.OnCutListener() {
            @Override
            public void OnCut(int index) {
                onSaveDraft(UploadDishData.UPLOAD_DRAF);
                XHClick.mapStat(mAct, UploadDishVideoControl.tongjiId,"视频编辑","裁剪");
                Intent it = new Intent(mAct, MediaPaperActivity.class);
                it.putExtra("id",uploadDishData.getId());
                it.putExtra("position",index);
                it.putExtra("mediaJson", UploadDishContentControl.getDishMakeData(mArrayList));
                mAct.startActivityForResult(it, UploadDishActivity.DISH_MAKE_ITEM_OPTION);
            }
        });
        initHeadView();
        initFootView();
        initData();
    }

    private void stopVideo(){
        viewSurfaceVideoView.stopVideo();
        onResume();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //片头片尾下载
        mediaReqDataContorl = new MediaReqDataContorl(mAct);
        mediaReqDataContorl.start();
        if(uploadDishData.getUploadTimeCode() <= 0){
            uploadDishData.setUploadTimeCode(System.currentTimeMillis());
        }
        imgUrl = uploadDishData.getCover();
        setImageView(dishImageIv,imgUrl);
        String name=TextUtils.isEmpty(uploadDishData.getRemoveName())?uploadDishData.getName():uploadDishData.getName().replace(uploadDishData.getRemoveName(), "");
        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(uploadDishData.getActivityId())){
            int first = name.indexOf("#");
            int last = name.indexOf("#",1);
            if(first == 0 && last > first){
                mDishNameEt.setTag(R.id.dish_upload_number, 10 + (last - first + 1));
            }
        }
        changeViewData(name, mDishNameEt);
        changeViewData(uploadDishData.getStory(), mDishIdeaEt);
        changeViewData(uploadDishData.getTips(), mDishTipsEt);
        setAllVideoTime(false);
    }

    private void setAllVideoTime(boolean isRefush){
        try {
            mAllVideoTime = 0;
            for (Map<String, String> map : mArrayList) {
                String vidieInfo = map.get("videoInfo");
                if (!TextUtils.isEmpty(vidieInfo)) {
                    JSONObject jsonObject = new JSONObject(vidieInfo);
                    float allTime = Float.parseFloat(jsonObject.getString("cutTime"));
                    mAllVideoTime += allTime;
                    isRefush = true;
                }
            }
            if(isRefush)refushTimeHint();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void initDraftDishView(final int id) {
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {
                mArrayList = new ArrayList<>();
                mArrayList.addAll(UtilString.getListMapByJson(uploadDishData.getMakes()));
                initView();
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

    private void frfrashData(){
        try {
            for (Map<String, String> map : mArrayList) {
                String vidieInfo = map.get("videoInfo");
                if (!TextUtils.isEmpty(vidieInfo)) {
                    JSONObject jsonObject = new JSONObject(vidieInfo);
                    String dideoPath = jsonObject.getString("path");
                    if(!new File(dideoPath).exists()){
                        map.put("videoInfo","");
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取网络菜谱数据并回调initView
     */
    @Override
    protected void initNetDishView(final String dishCode) {
        ReqInternet.in().doGet(StringManager.api_getDishInfoNew + "?code=" + dishCode+"&isNew=1&pg=1", new InternetCallback(mAct) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    ArrayList<Map<String, String>> list = UtilString.getListMapByJson(returnObj);
                    if (list.size() > 0) {
                        Map<String, String> theDish = list.get(0);
                        list = UtilString.getListMapByJson(theDish.get("data"));
                        if (list.size() > 0) {
                            theDish = list.get(0);
                            uploadDishData = new UploadDishData();
                            uploadDishData.setCode(theDish.get("code"));
                            // 设置title
                            uploadDishData.setName(theDish.get("name"));
                            // 大图
                            uploadDishData.setCover(theDish.get("img"));

                            //取主料/辅料数据
                            ArrayList<Map<String, String>> newList = UtilString.getListMapByJson(theDish.get("burden"));
                            JSONArray burdenArray = new JSONArray();
                            JSONArray foodArray = new JSONArray();
                            if(newList.size() > 0) {
                                Map<String, String> map = newList.get(0);
                                //主料
                                if (map.get("type").equals("1")) {
                                    foodArray = addFoodOrFu(map);
                                }else{
                                    burdenArray = addFoodOrFu(map);
                                }
                            }
                            if(newList.size() > 1) {
                                Map<String, String> map = newList.get(1);
                                //主料
                                if (map.get("type").equals("1")) {
                                    foodArray = addFoodOrFu(map);
                                }else{
                                    burdenArray = addFoodOrFu(map);
                                }
                            }
                            // 设置食材
                            uploadDishData.setFood(foodArray.toString());
                            // 设置配料数据
                            uploadDishData.setBurden(burdenArray.toString());
                            //设置步骤信息
                            mArrayList = new ArrayList<>();
                            ArrayList<Map<String, String>> addition = UtilString.getListMapByJson(theDish.get("addition"));
                            if (addition.size() > 0) {
                                mArrayList.addAll(addition);
                            } else {
                                // 设置做法数据
                                uploadDishData.setMakes(theDish.get("makes"));
                            }
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
                    }
                    else {
                        Tools.showToast(mAct, "抱歉，未找到相应菜谱");
                        mAct.finish();
                    }
                }
            }
        });
    }

    private JSONArray addFoodOrFu(Map<String, String> map){
        JSONArray foodArray = new JSONArray();
        JSONObject jsonObject;
        ArrayList<Map<String, String>> foodList = UtilString.getListMapByJson(map.get("data"));
        Map<String, String> foodMap;
        try {
            for (int foodIndex = 0; foodIndex < foodList.size(); foodIndex++) {
                foodMap = foodList.get(foodIndex);
                jsonObject = new JSONObject();
                jsonObject.put("name", foodMap.get("name"));
                jsonObject.put("number", foodMap.get("content"));
                foodArray.put(jsonObject);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return foodArray;
    }

    /**
     * 设置界面上的添加按钮与显示内容的显示状态
     * @param content : 内容
     * @param et : 存放内容的TextView
     */
    private void changeViewData(String content, EditText et) {
        if (!TextUtils.isEmpty(content) && content.length() > 0) {
            et.setText(content);
        }
    }

    private void initSpeech(){
        UploadDishSpeechTools speechTools = UploadDishSpeechTools.createUploadDishSpeechTools();
        speechTools.initSpeech(mAct);
//		TextView btn = (TextView)mUploadDishActivity.findViewById(R.id.dish_up_speech_btn);
//		speechTools.setStartButton(btn);
    }

    private void initHeadView(){
        View titleInfo = mAct.findViewById(R.id.dish_video_up_title_info);
        titleInfo.setVisibility(View.VISIBLE);
        titleInfo.setOnClickListener(this);
        //积分规则
        mAct.findViewById(R.id.a_dish_score_hint).setVisibility(View.GONE);
//        mAct.findViewById(R.id.a_dish_score_hint_close).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                mAct.findViewById(R.id.a_dish_score_hint).setVisibility(View.GONE);
//            }
//        });
        dishImageIv = (ImageView)mAct.findViewById(R.id.dishImage_iv);
        dishImageIv.setOnClickListener(this);
        mDishNameEt= (EditText) mAct.findViewById(R.id.a_dish_upload_title);
        mDishNameEt.setTag(R.id.dish_upload_number, 10);
        mDishNameEt.setTag(R.id.dish_upload_hint, "菜谱名不能超过10个字哦");
        View dish_head_layout = mAct.findViewById(R.id.a_dish_head_layout);
        mDishIdeaEt = (EditText) dish_head_layout.findViewById(R.id.a_dish_upload_item_speech_et);
        mDishIdeaEt.setTag(R.id.dish_upload_number, 800);
        mDishIdeaEt.setTag(R.id.dish_upload_hint, "心得不能超过800个字哦");
        mDishIdeaEt.setMaxLines(5);
        /**禁止输入特殊符号*/
        UploadDishContentControl.addDishNameChangeListener(mAct,mDishNameEt);
        initSpeechView(dish_head_layout,mDishNameEt,false);
        initSpeechView(dish_head_layout,mDishIdeaEt,true);
        //食材和辅料控制构造
        View dish_ingredientView = mAct.findViewById(R.id.a_dish_ingredient_view);
        mDishIngredientView = new DishIngredientView(mAct, dish_ingredientView, uploadDishData,true);
    }

    private void initFootView(){
        mAct.findViewById(R.id.a_dish_upload_new_video_allTime_layout).setVisibility(View.VISIBLE);
        mVideAllTime = (TextView)mAct.findViewById(R.id.a_dish_upload_new_video_allTime);
        mVideoAllTimeHint = (TextView)mAct.findViewById(R.id.a_dish_upload_new_video_allTime_hint);
        //烹饪小技巧
        View dish_tip_layout = mAct.findViewById(R.id.a_dish_tip_layout);
        mDishTipsEt = (EditText) dish_tip_layout.findViewById(R.id.a_dish_upload_item_speech_et);
        initSpeechView(dish_tip_layout,mDishTipsEt,true);
        LinearLayout dish_otherView = (LinearLayout) mAct.findViewById(R.id.dish_other_content);
        mDishOtherControl = new DishOtherControl(mAct, dish_otherView, uploadDishData,false);
        mAgreementManager = new AgreementManager(mAct,StringManager.api_agreementOriginal);
        mAct.findViewById(R.id.tv_addMake).setOnClickListener(this);
        mAct.findViewById(R.id.tv_trimMake).setOnClickListener(this);
        Button submit_btn = (Button) mAct.findViewById(R.id.submit_btn);
        submit_btn.setText("下一步，合成视频");
        submit_btn.setOnClickListener(this);
        mAct.findViewById(R.id.delete_btn).setOnClickListener(this);
        TextView draftNum = (TextView)mAct.findViewById(R.id.a_dish_upload_go_draft_btn);
        UploadDishSqlite sqlite = new UploadDishSqlite(mAct);
        draftNum.setText("草稿箱  (" + sqlite.getAllDraftSize() + ")");
        draftNum.setOnClickListener(mAct);
    }

    private void initSpeechView(View parentView,final EditText et,boolean isCheckNum){
        if(parentView != null){
            parentView.findViewById(R.id.a_dish_upload_item_speech_iv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "语音", "直接点击语音按钮");
                    et.requestFocus();
                    UploadDishSpeechTools.createUploadDishSpeechTools().startSpeech(et);
                }
            });
        }
        if(isCheckNum)
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int maxLength = -1;
                    String hint = "";
                    if(et.getTag(R.id.dish_upload_number) != null){
                        maxLength = Integer.parseInt(String.valueOf(et.getTag(R.id.dish_upload_number)));
                        hint = String.valueOf(et.getTag(R.id.dish_upload_hint));
                    }
                    if(maxLength > 0 && s.length() > maxLength){
                        CharSequence text = s.subSequence(0, maxLength);
                        et.setText(text);
                        et.setSelection(text.length());
                        Tools.showToast(mAct, hint);
                    }
                }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                }
            });
    }

    @Override
    public int onSaveDraft(String dishType) {
        if(!checkDishDataIsEmpty()) return -3;
        XHClick.mapStat(mAct, tongjiId,"存草稿","");
        uploadDishData.setDishType(dishType);
        UploadDishSqlite mDishSqlite=new UploadDishSqlite(mAct.getApplicationContext());
        //如果是数据库已有字段,更新
        if(uploadDishData.getId() > 0){
            mDishSqlite.update(uploadDishData.getId(), uploadDishData);
        }else {uploadDishData.setId(mDishSqlite.insert(uploadDishData));}
        return uploadDishData.getId();
    }

    public boolean checkDishDataIsEmpty(){
        setDishData();
        return !(uploadDishData.getName().length() == 0 && uploadDishData.getCover().length() == 0 &&
                uploadDishData.getBurden().length() < 3 && uploadDishData.getFood().length() < 3 &&
                uploadDishData.getMakes().length() < 3 && uploadDishData.getTips().length() == 0);
    }

    @Override
    protected synchronized void onClickUploadDish() {
//        XHClick.mapStat(mAct,UploadDishNewActivity.STATISTICS_ID, "下一步", "");
        if (ifCanUpload) {
            ifCanUpload = false;
            if(LoginManager.isLogin()){
                onSaveDraft(UploadDishData.UPLOAD_DRAF);
                String failedStr = checkUploadDishData();
                if(TextUtils.isEmpty(failedStr)) {
                    XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "校验成功", 100);
                }else{
                    Tools.showToast(mAct, failedStr);
                    ifCanUpload=true;
                    return;
                }
                XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "下一步，合成视频", "继续");
                if(new MediaStorageControl(mAct).checkStorage(uploadDishData.getId())){
                    ifCanUpload = true;
                    return;
                }
                //结束定时存储草稿的操作
                timer.cancel();
                XHClick.track(mAct,"下一步，合成视频");
//                MediaHandleControl.delAllMediaHandlerData(uploadDishData.getId());
                Intent handleIntent = new Intent(mAct, MediaHandleActivity.class);
                handleIntent.putExtra("uploadDishData",uploadDishData);
                mAct.startActivity(handleIntent);
            }else {
                Intent it = new Intent(mAct, LoginByAccout.class);
                mAct.startActivity(it);
            }
            ifCanUpload = true;
        } else {
            Tools.showToast(mAct, "菜谱正在发布，请勿重复点击！");
        }
    }

    private void setDishData(){
        uploadDishData.setCover(imgUrl);
        uploadDishData.setName(StringManager.getUploadString(mDishNameEt.getText()));//菜谱名字
        uploadDishData.setStory(StringManager.getUploadString(mDishIdeaEt.getText()));//心得
        uploadDishData.setFood(mDishIngredientView.getFoodData());//食材
        uploadDishData.setBurden(mDishIngredientView.getIngredientData());//辅料
        uploadDishData.setMakes(UploadDishContentControl.getDishMakeData(mArrayList));//获取做法步骤数据，包括文字和图片
        uploadDishData.setTips(StringManager.getUploadString(mDishTipsEt.getText()));// 小贴士
        uploadDishData.setAddTime(Tools.getAssignTime("yyyy-MM-dd HH:mm:ss", 0));//添加时间
        uploadDishData.setReadyTime(mDishOtherControl.getReadyTime()); //准备时间
        uploadDishData.setCookTime(mDishOtherControl.getCookTime()); //烹饪时间
        uploadDishData.setTaste(mDishOtherControl.getTaste()); //口味
        uploadDishData.setDiff(mDishOtherControl.getDiff()); //难度
        uploadDishData.setExclusive("2"); //是否独家
        uploadDishData.setCheckGreement(mAgreementManager.getIsChecked());
        uploadDishData.setVideType(true);
    }



    /**
     * 数据校验
     * 正常则返回空字符串，否则返回错误信息
     */
    private String checkUploadDishData() {
//        boolean isUploading = mUploadDishControl.ifUploading(uploadDishData.getUploadTimeCode());// 设置上传菜谱code
//        if (isUploading) {
//            resultToastInfo = "菜谱正在发布，请稍等！";
//            XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "当前有菜谱正在发布，请稍等！", 0);
//            dateIsOk = false;
//        } else {
        if (TextUtils.isEmpty(uploadDishData.getCover().toString().trim())) {
            XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "菜谱效果图不能为空", 0);
            return "菜谱效果图不能为空";
        }
        if (TextUtils.isEmpty(uploadDishData.getName()) || uploadDishData.getName().length() <= 0) {
            XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "菜谱名字不能为空", 0);
            return "菜谱名字不能为空";
        }
        if(!uploadDishData.getCheckGreement()){
            XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "没有确认香哈协议", 0);
            return "同意原创内容发布协议后才能提交哦";
        }
        //主料检查
        String chekFoodInfo = checkFood();
        if (!TextUtils.isEmpty(chekFoodInfo)) {
            XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", chekFoodInfo, 0);
            return chekFoodInfo;
        }
        //辅料检查
        String chekIngredInfo = checkIngredientData();
        if (!TextUtils.isEmpty(chekIngredInfo)) {
            XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", chekIngredInfo, 0);
            return chekIngredInfo;
        }
        if (!imgIsCreateOk) {
            return "正在加载图片，请稍等";
        }
        if (!mediaReqDataContorl.start()) {
            XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "下一步，合成视频", "弹出升级弹框");
            return "准备中，请稍后…";
        }

        ArrayList<Map<String, String>> makes = UtilString.getListMapByJson(uploadDishData.getMakes());
        if (makes.size() >= 3) {
            int index = 0;
            for(Map<String, String> map : makes) {
                index ++;
                String makesInfo = map.get("makesInfo"); //视频步骤可以不写内容，只写空格
                String videoInfo = map.get("videoInfo").trim();
                if (TextUtils.isEmpty(makesInfo)) {
                    XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "请完善步骤说明", 0);
                    return "步骤" + index + "没有写步骤说明哦";
                }
                if (TextUtils.isEmpty(videoInfo)) {
                    XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "请选择步骤视频", 0);
                    return "请选择步骤视频";
                }
                float videoTime;
                String cutTime = "0.0";
                try {
                    JSONObject jsonObject = new JSONObject(videoInfo);
                    cutTime = jsonObject.getString("cutTime");
                }catch (Exception e){
                    e.printStackTrace();
                }
                videoTime = Float.parseFloat(cutTime);
                if(videoTime > MAKE_VIDEO_MAX_S){
                    XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "第" + index + "步视频超过15秒", 0);
                    return "第" + index + "步视频超过15秒";
                }
                int makeInfoLength = makesInfo.length();
                if(makeInfoLength > MAKE_INFO_MAX_TEXT){
                    XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "第" + index + "步描述超过25个字", 0);
                    return "第" + index + "步描述超过25个字";
                }
                if(videoTime * 3 < makeInfoLength){
                    XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "第" + index + "步描述过长", 0);
                    return "第" + index + "步描述过长";
                }
            }
            if (mAllVideoTime < 30) {
                XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "总步骤最短30秒，请添加步骤", 0);
                XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "下一步，合成视频", "提示时间过短");
                return "总步骤最短30秒，请添加步骤";
            } else if (mAllVideoTime > 6 * 60) {
                XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "总步骤最长6分钟，请裁剪", 0);
                XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "下一步，合成视频", "提示时间过长");
                return "总步骤最长6分钟，请裁剪";
            }
        } else {
            XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "步骤太少,最少3步哦", 0);
            return  "步骤太少,最少3步哦";
        }
        return "";
    }

    private String checkFood(){
        String resultToastInfo = "";
        String food = uploadDishData.getFood();
        ArrayList<Map<String, String>> foodArray = UtilString.getListMapByJson(food);
        if (foodArray.size() < 1) {
            resultToastInfo = "食材不足1项";
            XHClick.onEventValue(mAct, "uploadDishClick", "uploadDishClick", "食材不足1项", 0);
        }else{
            for(Map<String, String> map : foodArray){
                if(TextUtils.isEmpty(map.get("number"))){
                    resultToastInfo = "食材用量不能为空";
                    break;
                }
            }
        }
        return resultToastInfo;
    }

    private String checkIngredientData(){
        String resultToastInfo = "";
        String burden = uploadDishData.getBurden();
        ArrayList<Map<String, String>> burdenArray = UtilString.getListMapByJson(burden);
        if (burdenArray.size() > 0) {
            for(Map<String, String> map : burdenArray){
                if(TextUtils.isEmpty(map.get("number"))){
                    resultToastInfo = "辅料用量不能为空";
                    break;
                }
            }
        }
        return resultToastInfo;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UploadDishActivity.DISH_CHOOSE_SINGLE_IMG:
                ArrayList<String> resultList = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
                if(resultList.size() > 0){
                    imgUrl = resultList.get(0);
                    setImageView(dishImageIv, imgUrl);
                }
                break;
            case UploadDishActivity.DISH_ADD_MAKE:
                int position = data.getIntExtra(ChooseVideoActivity.EXTRA_RUSULT_POSITION,-1);
                final String resultTime = data.getStringExtra(ChooseVideoActivity.EXTRA_RUSULT_TIME);
                final String resultPath = data.getStringExtra(ChooseVideoActivity.EXTRA_RUSULT_Path);
                final String imgPath = data.getStringExtra(ChooseVideoActivity.EXTRA_RUSULT_IMG_Path);
                if(!TextUtils.isEmpty(resultTime)){
                    JSONObject jsonObj = new JSONObject();
                    try {
                        jsonObj.put("path",resultPath);
                        jsonObj.put("imgPath",imgPath);
                        jsonObj.put("allTime",String.valueOf(resultTime));
                        jsonObj.put("startTime","0");
                        jsonObj.put("endTime","0");
                        jsonObj.put("cutTime",String.valueOf(resultTime));
                    } catch(JSONException e){
                        e.printStackTrace();
                    }
                    if(position == -1){
                        Map<String,String> map = new HashMap<>();
                        map.put("makesStep",String.valueOf(position));
                        map.put("makesInfo","");
                        map.put("videoTime",resultTime);
                        map.put("makesImg",imgPath);
                        map.put("videoInfo",jsonObj.toString());
                        mArrayList.add(map);
                    }else{
                        Map<String,String> map = mArrayList.get(position);
                        try { //替换视频，需要把原来的视频长度减掉
                            String vidieInfo = map.get("videoInfo");
                            if (!TextUtils.isEmpty(vidieInfo)) {
                                JSONObject jsonObject = new JSONObject(vidieInfo);
                                float cutTime = Float.parseFloat(jsonObject.getString("cutTime"));
                                mAllVideoTime -= cutTime;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        map.put("videoTime",resultTime);
                        map.put("makesImg",imgPath);
                        map.put("videoInfo",jsonObj.toString());
                    }
                    mAllVideoTime = mAllVideoTime + Float.parseFloat(resultTime);
                    refushTimeHint();
                    adapterDishNewMake.notifyDataSetChanged();
                }
                break;
            case UploadDishActivity.DISH_MAKE_ITEM_OPTION:
                mArrayList.clear();
                String optionJson = data.getStringExtra(UploadDishMakeOptionActivity.MAKE_ITEM_OPTION_DATA);
                mAllVideoTime = Float.parseFloat(!TextUtils.isEmpty(data.getStringExtra("allTime"))?
                        data.getStringExtra("allTime"):"0");
                refushTimeHint();
                mArrayList.addAll(UtilString.getListMapByJson(optionJson));
                adapterDishNewMake.notifyDataSetChanged();
                if(mAllVideoTime == 0)
                    setAllVideoTime(true);
                break;
        }
    }

    private void refushTimeHint(){
        mVideoAllTimeHint.setVisibility(View.VISIBLE);
        int minu = (int) (mAllVideoTime / 60);
        int secon = (int)(mAllVideoTime - (minu * 60));
        String minuStr = String.valueOf(minu);
        String seconStr = String.valueOf(secon);
        if(minu < 10){
            minuStr = "0" + minu;
        }
        if(secon < 10){
            seconStr = "0" + secon; ;
        }
        if(mAllVideoTime < 30){
            mVideoAllTimeHint.setText("(合成后不能低于30s)");
        }else{
            if(mAllVideoTime > 6 * 60){
                mVideoAllTimeHint.setText("(裁剪后不能超过6分钟)");
            }else{
                mVideoAllTimeHint.setVisibility(View.GONE);
            }
        }
        mVideAllTime.setText("视频总时长" + minuStr + ":" + seconStr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rule_tv: //查看积分规则
                if (LoginManager.isLogin()) {
                    String url = StringManager.api_integralInfo + "?code="+ LoginManager.userInfo.get("code");
                    AppCommon.openUrl(mAct, url, true);
                } else {
                    Tools.showToast(mAct, "登录后即可查看积分规则");
                    mAct.startActivity(new Intent(mAct, LoginByAccout.class));
                }
                break;
            case R.id.dishImage_iv://新方法开启图片选择
                Intent intent = new Intent();
                intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_SINGLE);
                intent.setClass(mAct, ImageSelectorActivity.class);
                mAct.startActivityForResult(intent, UploadDishActivity.DISH_CHOOSE_SINGLE_IMG);
                break;
            case R.id.tv_addMake: //添加步骤
                if(mArrayList.size() >= 60){
                    Toast.makeText(mAct,"最多60步哦!",Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String,String> map = new HashMap<>();
                map.put("makesStep",String.valueOf(mArrayList.size() + 1));
                map.put("makesInfo","");
                map.put("videoInfo","");
                mArrayList.add(map);
                adapterDishNewMake.notifyDataSetChanged();
                break;
            case R.id.tv_trimMake: //调整步骤
                XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "步骤操作", "点击调整步骤按钮");
                Intent it = new Intent(mAct,UploadDishMakeOptionActivity.class);
                it.putExtra("makesJson", UploadDishContentControl.getDishMakeData(mArrayList));
                it.putExtra("isVideoMake",true);
                it.putExtra("tongjiId",tongjiId);
                mAct.startActivityForResult(it, UploadDishActivity.DISH_MAKE_ITEM_OPTION);
                break;
            case R.id.submit_btn:
                onClickUploadDish();
                break;
            case R.id.dish_video_up_title_info: //视频帮助信息
                XHClick.mapStat(mAct, tongjiId,"说明页面入口按钮","");
                AppCommon.openUrl(mAct,"http://appweb.xiangha.com/deal/helpDishVideo.html",true);
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

    /**
     * 界面上所有的imageView操作;
     * @param imageView
     * @param imgUrl
     */
    private void setImageView(final ImageView imageView, final String imgUrl) {
        // 根据类型和位序来设置图片
        if (imgUrl != null && imgUrl.length() > 0) {
            if (imgUrl.indexOf("http") == 0) {
                BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(imageView.getContext())
                        .load(imgUrl)
                        .build();
                if(bitmapRequest != null)
                    bitmapRequest.into(getTarget(imageView,0,0));
            } else {
                dealLocalImg(imageView);
            }
        }
    }

    private void dealLocalImg(final ImageView imageView){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bitmap bmp = (Bitmap)msg.obj;
                if(bmp != null){
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageBitmap(bmp);
                }else
                    imgUrl = "";
                imgIsCreateOk = true;
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                imgIsCreateOk = false;
                Bitmap bmp = UtilImage.imgPathToBitmap(imgUrl, ToolsDevice.dp2px(mAct, 800),
                        ToolsDevice.dp2px(mAct, 400), false, null);
                Message msg = new Message();
                msg.obj = bmp;
                handler.sendMessage(msg);

            }
        }).start();
    }

    private SubBitmapTarget getTarget(final ImageView v, final int width_dp, final int height_dp){
        return new SubBitmapTarget(){
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = v;
                if (img != null && bitmap != null) {
                    UtilImage.setImgViewByWH(img, bitmap, ToolsDevice.dp2px(mAct, width_dp), height_dp, false);
                }
            }};
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(viewSurfaceVideoView != null)
            viewSurfaceVideoView.onDestory();
    }

}
