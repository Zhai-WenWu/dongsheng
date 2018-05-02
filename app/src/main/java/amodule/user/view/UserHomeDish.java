/**
 * @author intBird 20140213.
 */
package amodule.user.view;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.adapter.AdapterSimple;
import acore.override.data.UploadData;
import acore.override.helper.UploadHelper;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.tools.UploadDishControl;
import amodule.main.Main;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.activity.FriendHome;
import amodule.user.activity.MyDraft;
import amodule.user.adapter.AdapterUserDish;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;

import static xh.basic.tool.UtilString.getListMapByJson;

public class UserHomeDish extends TabContentView {
    private View headLayout,headView;;
    private int draftSize;
    private TextView headDraftTv;
    private FriendHome mAct;
    private LoadManager loadManager;
    private AdapterSimple adapter;
    private ArrayList<Map<String, String>> listDataMyDish;

    private int currentPage = 0, everyPage = 0;
    private String userCode = "";
    private int imgHeight;
    private boolean isBlankSpace = true;
    private boolean isMyselft = false;
    private String tongjiId = "a_user";
    private ArrayList<Map<String,String>> backArray;

    public UserHomeDish(FriendHome act, String code) {
        view = View.inflate(act, R.layout.a_my_dish, null);
        userCode = code;
        if (!TextUtils.isEmpty(LoginManager.userInfo.get("code")) && LoginManager.userInfo.get("code").equals(userCode)) {
            isMyselft = true;
            tongjiId = "a_my";
        }
        this.mAct = act;
        // 设定scrollLayout的高度
        scrollLayout = mAct.scrollLayout;
        // 滑动设置
        backLayout = mAct.backLayout;
        friend_info = mAct.friend_info;
        init();
        initUploadDishCallback();
    }

    @Override
    public void onResume(String tag) {
        // 界面被第二次以后从外部激活时要刷新
        if (tag.equals("resume")) {
            initLoad();
            super.onResume("0");
        } else
            super.onResume(tag);
        theListView.setSelection(listDataMyDish.isEmpty() ? 0 : 1);
    }

    private void init() {
        imgHeight = (int) (ToolsDevice.getWindowPx(mAct).widthPixels * 450 / 750f);
        // 结果显示
        loadManager = mAct.loadManager;
        theListView = (DownRefreshList) view.findViewById(R.id.lv_myDish);
        listDataMyDish = new ArrayList<>();
        adapter = new AdapterUserDish(mAct, theListView, listDataMyDish, R.layout.a_user_home_item_dish,
                new String[]{"name", "allClick", "isFine", "isGood", "isExclusive", "nickName",
                        "userimg", "isGourmet", "isDel", "timeShowV43", "isUpdate", "isFail"},
                new int[]{R.id.a_user_home_dish_name, R.id.a_user_home_dish_taste, R.id.a_user_home_dish_jing,
                        R.id.a_user_home_dish_you, R.id.a_user_home_dish_sole, R.id.a_user_home_dish_author_name,
                        R.id.a_user_home_dish_author_image, R.id.a_user_home_dish_author_gourmet, R.id.a_user_home_dish_delete,
                        R.id.a_user_home_dish_time, R.id.a_user_home_dish_uploading, R.id.a_user_home_dish_fail}, new AdapterUserDish.OnDeleteDishClick() {

            @Override
            public void onDelete(final int index) {
                XHClick.mapStat(mAct, "a_my", "菜谱", "删除菜谱");
                String code = listDataMyDish.get(index).get("code");
                ReqInternet.in().doPost(StringManager.api_deleteDish, "code=" + code,new InternetCallback() {
                    @Override
                    public void loaded(int flag, String url, Object returnObj) {
                        if (flag >= UtilInternet.REQ_OK_STRING) {
                            listDataMyDish.remove(index);
                            adapter.notifyDataSetChanged();
                            if (FriendHome.isAlive) {
                                Intent broadIntent = new Intent();
                                broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
                                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, "0");
                                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.ACTION_DEL, "2");
                                Main.allMain.sendBroadcast(broadIntent);
                            }
                        }
                    }
                });
            }
        }, tongjiId);
        adapter.roundImgPixels = Tools.getDimen(mAct, R.dimen.dp_400);
        adapter.playImgWH = Tools.getDimen(mAct, R.dimen.dp_45);
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                int id = view.getId();
                switch (id) {
                    //处理美食家icon
                    case R.id.a_user_home_dish_author_gourmet:
                        view.setVisibility(data != null && "2".equals(data.toString()) ? View.VISIBLE : View.GONE);
                        return true;
                    case R.id.a_user_home_dish_name:
                        if (data == null || "".equals(data.toString())) {
                            view.setVisibility(View.INVISIBLE);
                        } else {
                            view.setVisibility(View.VISIBLE);
                            TextView tv = (TextView) view;
                            tv.setText(data.toString());
                        }
                        return true;
                    case R.id.iv_video_img:
                        if (imgHeight != 0) {
                            view.getLayoutParams().height = imgHeight;
                        }
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void initLoad() {
        currentPage = 0;
//        if(isBlankSpace) theListView.setVisibility(View.GONE);
        if (theListView.getAdapter() == null) {
            headView = LayoutInflater.from(mAct).inflate(R.layout.a_user_home_dish_item, null);
            headLayout = headView.findViewById(R.id.a_user_home_head_layout);
            if (isMyselft) {
                headLayout.findViewById(R.id.a_user_home_dish_draft_layout).setVisibility(View.VISIBLE);
                headDraftTv = (TextView) headLayout.findViewById(R.id.a_user_home_dish_draft_name);
                headDraftTv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Main.colse_level == 5){
                            Main.colse_level = 6;
                            MyDraft.oldCloseLevel = 5;
                        }
                        XHClick.mapStat(mAct, "a_my", "菜谱", "草稿箱");
                        Intent intent = new Intent(mAct, MyDraft.class);
                        mAct.startActivity(intent);
                    }
                });
            }
            Log.i("tzy","initLoad");
        }
        if (isMyselft) {
            UploadDishSqlite sqlite = new UploadDishSqlite(mAct);
            draftSize = sqlite.getAllDraftSize();
            headDraftTv.setText("草稿箱（" + draftSize + "）");
            if(draftSize > 0) {
                headLayout.findViewById(R.id.a_user_home_dish_draft_layout).setVisibility(View.VISIBLE);
            }else{
                headLayout.findViewById(R.id.a_user_home_dish_draft_layout).setVisibility(View.GONE);
            }
        }
        if (theListView.getAdapter() == null) {
            setHeadViewHeight();
        }

        loadManager.setLoading(theListView, adapter, scrollLayout, backLayout, headView, new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage++;
                loadFromServer();
            }
        }, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAct.doReload();
            }
        }, isBlankSpace);
        if (!isBlankSpace) mAct.loadManager.hideProgressBar();
    }

    private void setHeadViewHeight() {
        int tabHost_h = Tools.getDimen(mAct, R.dimen.dp_51);
        int draf_h = 0;
        if (isMyselft && draftSize > 0)
            draf_h = Tools.getDimen(mAct, R.dimen.dp_65);
        int bigImg_h = Tools.getDimen(mAct, R.dimen.dp_200) + Tools.getStatusBarHeight(mAct);
        int userinfo_h = Tools.getTargetHeight(friend_info);
//        Log.i("tzy","userinfo_h = " + userinfo_h);
//        Log.i("tzy","draf_h = " + draf_h);
        try {
            if (friend_info.getText() == null || friend_info.getText().toString().equals("")) {
//                headLayout.setLayoutParams(new AbsListView.LayoutParams(
//                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
//                        tabHost_h + draf_h + bigImg_h));
                headLayout.getLayoutParams().width= ViewGroup.LayoutParams.MATCH_PARENT;
                headLayout.getLayoutParams().height=tabHost_h + draf_h + bigImg_h;
            }else {
//                headLayout.setLayoutParams(new AbsListView.LayoutParams(
//                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
//                        tabHost_h + draf_h + bigImg_h + userinfo_h));
                headLayout.getLayoutParams().width= ViewGroup.LayoutParams.MATCH_PARENT;
                headLayout.getLayoutParams().height=tabHost_h + draf_h + bigImg_h + userinfo_h;
            }
            headLayout.requestLayout();
//            Log.i("tzy","height = " + headLayout.getLayoutParams().height);
        } catch (Exception e) {
            UtilLog.reportError("MyselfSubject头部局异常", e);
        }
    }

    private void loadFromServer() {
        loadManager.changeMoreBtn(theListView, UtilInternet.REQ_OK_STRING, -1, -1, currentPage, isBlankSpace);
        String getUrl = StringManager.api_getDishByCode + "?code=" + userCode + "&page=" + currentPage;
        ReqInternet.in().doGet(getUrl, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                int loadPage = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    if (currentPage == 1) {
                        listDataMyDish.clear();
                        if(isMyselft) loadFormLocal();
                    }
                    ArrayList<Map<String, String>> returnList = getListMapByJson(returnObj);
                    for (int i = 0; i < returnList.size(); i++) {
                        Map<String, String> map = returnList.get(i);
                        map.put("isUpdate", "");
                        String state = map.get("dishState");
                        map.put("isFail", "");
                        if ("6".equals(state)) { //审核通过才显示多少浏览多少收藏
                            StringBuffer buffer = new StringBuffer();
                            buffer.append(map.get("allClick"));
                            buffer.append("浏览  ");
                            buffer.append(map.get("favorites"));
                            buffer.append("收藏");
                            map.put("allClick", buffer.toString());
                        } else if ("5".equals(state)) {
                            map.put("allClick", "等待审核");
                        } else if ("2".equals(state)) {
                            map.put("allClick", "");
                            map.put("isFail", "审核失败");
                        } else {
                            map.put("allClick", "");
                        }
                        map.put("isDel", "hide");
                        if (!map.containsKey("hasVideo")) {
                            map.put("hasVideo", "1");
                        }
                        String videoState = map.get("videoState");
                        //如果是视频菜谱，并且是转码中
                        if ("2".equals(map.get("hasVideo")) && "1".equals(videoState)) {
                            map.put("allClick", "上传成功,转码中");
                        }
                        if (!map.containsKey("level")) {
                            map.put("level", "1");
                        }
                        map.put("draft", UploadDishData.UPLOAD_SUCCESS);
                        map.put("isGood", map.get("level").equals("2") ? "优质" : "hide");
                        map.put("isFine", map.get("level").equals("3") ? "精华" : "hide");
                        map.put("isExclusive", map.get("isExclusive").equals("2") ? "独家" : "hide");
                        ArrayList<Map<String, String>> customerArray = UtilString.getListMapByJson(map.get("customers"));
                        if (customerArray.size() > 0) {
                            Map<String, String> customer = customerArray.get(0);
                            map.put("isGourmet", customer.get("isGourmet"));
                            map.put("nickName", customer.get("nickName"));
                            map.put("userimg", customer.get("img"));
                            map.put("userCode", userCode);
                        }
                        if(backArray != null){
                            for(Map<String,String> backMap : backArray) {
                                String dishCode = backMap.get("code");
                                if (map.get("code").equals(dishCode)) {
                                    String uploadBackDraf = backMap.get("draft");
                                    String draftId = backMap.get("id");
                                    map.put("id", draftId);
                                    map.put("allClick", "");
                                    map.put("draft", UploadDishData.UPLOAD_ING_BACK);
                                    if (UploadDishData.UPLOAD_ING.equals(uploadBackDraf) || UploadDishData.UPLOAD_ING_BACK.equals(uploadBackDraf)) {
                                        map.put("isUpdate", "上传中  查看详情");
                                        map.put("isFail", "");
                                    } else if (UploadDishData.UPLOAD_PAUSE.equals(uploadBackDraf)) {
                                        map.put("isUpdate", "");
                                        map.put("isFail", "暂停上传");
                                    } else {
                                        map.put("isFail", "上传失败  查看详情");
                                        map.put("isUpdate", "");
                                    }
                                }
                            }
                        }
						listDataMyDish.add(map);
                    }
                    loadPage = returnList.size();
                    isBlankSpace = false;
                    Log.i("tzy","request");
                    setHeadViewHeight();
                    adapter.notifyDataSetChanged();
                }
                if (everyPage == 0)
                    everyPage = loadPage;
                currentPage = loadManager.changeMoreBtn(theListView, flag, everyPage, loadPage, currentPage, isBlankSpace);
                // 如果总数据为空,显示没有菜谱
                if (flag >= UtilInternet.REQ_OK_STRING && listDataMyDish.size() == 0 && draftSize == 0) {
                    LinearLayout tabMainMyself = (LinearLayout) mAct.findViewById(R.id.a_user_home_title_tab);
                    TextView empty = (TextView) view.findViewById(R.id.tv_noData);
                    RelativeLayout.LayoutParams emptyParams = (RelativeLayout.LayoutParams) empty.getLayoutParams();
                    emptyParams.topMargin = tabMainMyself.getTop() + tabMainMyself.getHeight();
                    empty.setVisibility(View.VISIBLE);
                }
                // 如果总数据为空,显示没有菜谱
//				if (flag >= UtilInternet.REQ_OK_STRING && listDataMyDish.size() == 0) {
//					view.findViewById(R.id.tv_noData).setVisibility(View.VISIBLE);
//				}
                // 否则显示结果
                else {
                    view.findViewById(R.id.tv_noData).setVisibility(View.GONE);
                    theListView.setVisibility(View.VISIBLE);
                }
                theListView.onRefreshComplete();
            }
        });
    }

    /**
     * 读取正在发布的贴子
     */
    private void loadFormLocal() {
        UploadDishSqlite sqlite = new UploadDishSqlite(mAct);
        backArray = new ArrayList<>();
        Map<String,String> backMap = sqlite.getUploadingBack();
        if(backMap != null) backArray.add(backMap);
        ArrayList<Map<String, String>> listDrafts = sqlite.getAllDataInDB();
        for (int i = 0; i < listDrafts.size(); i++) {
            Map<String, String> map = listDrafts.get(i);
            //Log.i("FRJ","loadFormLocal map.get(draft:" + map.get("draft"));
//			if (map!=null && (map.get("draft").equals(UploadDishData.UPLOAD_ING) || map.get("draft").equals(UploadDishData.UPLOAD_FAIL))){
            if (map != null && (map.get("draft").equals(UploadDishData.UPLOAD_ING) || map.get("draft").equals(UploadDishData.UPLOAD_FAIL) || map.get("draft").equals(UploadDishData.UPLOAD_PAUSE))) {
//				"name", "allClick","isFine","isGood","isExclusive","nickName",
//						"userimg","isGourmet","isDel","dishTime"
                if(!TextUtils.isEmpty(map.get("code"))){
                    backArray.add(map);
                    continue;
                }
                map.put("isFine", "");
                map.put("isGood", "");
                map.put("isExclusive", "");
                map.put("nickName", LoginManager.userInfo.get("nickName"));
                map.put("userimg", LoginManager.userInfo.get("img"));
                map.put("isDel", "");
                map.put("timeShowV43", "今天");

                String draft = map.get("draft");
                map.put("draft", draft);
                if (UploadDishData.UPLOAD_ING.equals(draft)) {
                    if (!TextUtils.isEmpty(map.get("videoType")) && "2".equals(map.get("videoType")))
                        map.put("isUpdate", "上传中  查看详情");
                    else
                        map.put("isUpdate", "上传中");
                    map.put("isFail", "");

                } else if (UploadDishData.UPLOAD_PAUSE.equals(draft)) {
                    map.put("isUpdate", "");
                    map.put("isFail", "暂停上传");
                } else {
                    if (!TextUtils.isEmpty(map.get("videoType")) && "2".equals(map.get("videoType")))
                        map.put("isFail", "上传失败  查看详情");
                    else
                        map.put("isFail", "上传失败");
                    map.put("isUpdate", "");
                }
                map.put("id", map.get("id"));
                map.put("currentIdDB", map.get("id"));
                map.put("hasVideo", "1");
                if ("2".equals(map.get("videoType"))) {
                    map.put("hasVideo", "2");
                }
				listDataMyDish.add(map);
            }
        }
    }

    private void initUploadDishCallback() {
        UploadDishControl.getInstance().setUploadCallback(new UploadHelper.UploadCallback() {
            @Override
            public void uploading(int id) {
                initLoad();
            }
            @Override
            public void uploadOver(UploadData uploadData, int flag, Object msg) {
                //发布成功
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    initLoad();
                }
                //发布失败
                else {
                    UploadDishData uploadDishData = (UploadDishData) uploadData;
                    initLoad();
                    Intent intent = new Intent();
                    intent.setClass(mAct, UploadDishActivity.class);
                    intent.putExtra("id", uploadDishData.getId());
                    intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_DRAFT);
                    mAct.startActivity(intent);
                }
            }
        });
    }

    @Override
    public void finish() {
        UploadDishControl.getInstance().setUploadCallback(null);
        super.finish();
    }
}
