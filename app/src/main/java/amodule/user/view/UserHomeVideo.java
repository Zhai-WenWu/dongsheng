/**
 * @author intBird 20140213.
 */
package amodule.user.view;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadVideoSQLite;
import amodule.dish.db.UploadDishData;
import amodule.shortvideo.tools.ShortVideoPublishManager;
import amodule.user.activity.FriendHome;
import amodule.user.adapter.AdapterUserVideo;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import third.aliyun.work.AliyunCommon;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilLog;

/**
 * 我的页面：视频
 */
public class UserHomeVideo extends TabContentView {
    private View headView;
    private FriendHome mAct;
    private String userCode = "";
    private boolean isMyselft = false;
    private LoadManager loadManager;
    public ArrayList<Map<String, String>> datas;
    private ArrayList<Map<String, String>> mLocalDatas;
    private Map<String, Map<String, String>> mSecondEditDatas;
    private ArrayList<Map<String, String>> mNetDatas;
    public AdapterUserVideo adapter;
    private boolean mLocalDataReady;
    private boolean mNetDataReady;

    private int currentPage = 0, everyPage = 0;
    private boolean isRefresh = false;
    private RelativeLayout mEmptyContainer;
    private LinearLayout mEmptyView;
    private Button mGotoBtn;

    private int mHeadViewHeight;

    public UserHomeVideo(FriendHome act, String code) {
        view = View.inflate(act, R.layout.myself_txt, null);
        this.mAct = act;
        userCode = code;
        if (!TextUtils.isEmpty(LoginManager.userInfo.get("code")) && LoginManager.userInfo.get("code").equals(userCode)) {
            isMyselft = true;
        }
        // 设定scrollLayout的高度
        scrollLayout = mAct.scrollLayout;
        // 滑动设置
        backLayout = mAct.backLayout;
        friend_info = mAct.friend_info;
        init();
    }

    @Override
    public void onResume(String tag) {
        super.onResume(tag);
        if (tag.equals("resume")) {
            initLoad();
            super.onResume("0");
        } else
            super.onResume(tag);
        theListView.setSelection(datas.isEmpty() ? 0 : 1);
    }

    private void init() {
        // 结果显示
        loadManager = mAct.loadManager;
        mEmptyContainer = view.findViewById(R.id.empty_container);
        mEmptyView = mEmptyContainer.findViewById(R.id.empty);
        mGotoBtn = mEmptyView.findViewById(R.id.goto_btn);
        mGotoBtn.setText("发视频");
        mGotoBtn.setOnClickListener(v -> {
            AliyunCommon.getInstance().startRecord(mAct);
//            if (LoginManager.isBindMobilePhone())
//                mAct.startActivity(new Intent(mAct, VideoEditActivity.class));
//            else
//                BaseLoginActivity.gotoBindPhoneNum(mAct);
        });
        theListView = view.findViewById(R.id.list_myself_subject);
        theListView.setDivider(null);
        datas = new ArrayList<>();
        mSecondEditDatas = new HashMap<>();
        mLocalDatas = new ArrayList<>();
        mNetDatas = new ArrayList<>();
        adapter = new AdapterUserVideo(mAct, theListView, datas, 0, null, null);
        adapter.scaleType = ScaleType.CENTER_CROP;
        adapter.isAnimate = true;
        adapter.setOnItemClickListener((itemView, dataMap) -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(itemView, dataMap);
            }
        });
        adapter.setOnDeleteClickCallback(this::showSureDelateDialog);
    }

    private void showSureDelateDialog(final Map<String, String> data) {
        final DialogManager dialogManager = new DialogManager(mAct);
        ViewManager viewManager = new ViewManager(dialogManager);
        viewManager.setView(new TitleMessageView(mAct).setText("确认删除？"))
                .setView(new HButtonView(mAct)
                        .setNegativeText("否", v -> dialogManager.cancel())
                        .setPositiveText("是", v -> {
                            dialogManager.cancel();
                            deleteData(data);
                        }));
        dialogManager.createDialog(viewManager).show();
    }

    private void deleteData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        final String id = data.get("id");
        String fromLocal = data.get("dataFrom");
        if ("1".equals(fromLocal)) {
            //delete local
            UploadVideoSQLite sqLite = new UploadVideoSQLite(mAct);
            boolean isDeleted = sqLite.deleteById(Tools.parseIntOfThrow(id, -1));
            Toast.makeText(mAct, isDeleted ? "删除成功" : "删除失败", Toast.LENGTH_SHORT).show();
            if (isDeleted) {
                datas.remove(data);
                adapter.notifyDataSetChanged();
            }
        } else if ("2".equals(fromLocal)) {
            //delete remote
        }
    }

    private void showDialog(final String text, final String url) {
        final DialogManager dialogManager = new DialogManager(mAct);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(mAct).setText("暂无发布\"" + text + "\"的权限，是否申请发布权限？"))
                .setView(new HButtonView(mAct)
                        .setNegativeText("否", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                                XHClick.mapStat(mAct, "a_post_button", text, "权限弹框 - 否");
                            }
                        })
                        .setPositiveText("是", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogManager.cancel();
                                XHClick.mapStat(mAct, "a_post_button", text, "权限弹框 - 是");
                                AppCommon.openUrl(mAct, url, true);
                            }
                        }))).show();
    }

    private UserHomeItem.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(UserHomeItem.OnItemClickListener clickListener) {
        mOnItemClickListener = clickListener;
    }

    @Override
    public void initLoad() {
        currentPage = 0;
        isRefresh = true;
//        theListView.setVisibility(View.GONE);
        if (theListView.getAdapter() == null) {
            headView = new View(mAct);
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
        }, datas.size() == 0);
    }

    private void setHeadViewHeight() {
        final int tabHost_h = Tools.getDimen(mAct, R.dimen.dp_51);
        final int bigImg_h = Tools.getDimen(mAct, R.dimen.dp_200) + Tools.getStatusBarHeight(mAct);
        final int userinfo_h = Tools.getTargetHeight(friend_info);
        try {
            if (friend_info.getText() == null || friend_info.getText().toString().equals(""))
                mHeadViewHeight = tabHost_h + bigImg_h;
            else
                mHeadViewHeight = tabHost_h + bigImg_h + userinfo_h;
            headView.setLayoutParams(new AbsListView.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, mHeadViewHeight));
            headView.requestLayout();
        } catch (Exception e) {
            UtilLog.reportError("MyselfSubject头部局异常", e);
        }

    }

    private void loadFromServer() {
        loadManager.changeMoreBtn(theListView, UtilInternet.REQ_OK_STRING, -1, -1, currentPage, datas.size() == 0);
        if (isRefresh) {
            mNetDataReady = false;
            if (isMyselft)
                mLocalDataReady = false;
        }
        if (isRefresh && isMyselft) {
            if (mLocalDatas != null)
                mLocalDatas.clear();
            if (mSecondEditDatas != null)
                mSecondEditDatas.clear();
            new Thread(() -> {
                UploadVideoSQLite articleSQLite = new UploadVideoSQLite(mAct);
                ArrayList<UploadArticleData> articleDatas = articleSQLite.getAllDrafData();
                if (articleDatas != null && articleDatas.size() > 0) {
                    for (UploadArticleData articleData : articleDatas) {
                        if (articleData != null) {
                            Map<String, String> data = new HashMap<String, String>();
                            String code = articleData.getCode();
                            String title = articleData.getTitle();
                            String classCode = articleData.getClassCode();
                            String content = articleData.getContent();
                            int isOriginal = articleData.getIsOriginal();
                            String repAddress = articleData.getRepAddress();
                            String img = articleData.getImg();
                            String imgs = articleData.getImgs();
                            String videos = articleData.getVideos();
                            String video = "";
                            String videoUrl = "";
                            String videoImgUrl = "";
                            ArrayList<Map<String, String>> videoArray = articleData.getVideoArray();
                            if (videoArray.size() > 0) {
                                video = videoArray.get(0).get("video");
                                videoUrl = videoArray.get(0).get("videoUrl");
                                videoImgUrl = videoArray.get(0).get("imageUrl");
                            }
                            String uploadType = articleData.getUploadType();
                            if (!TextUtils.isEmpty(uploadType) && UploadDishData.UPLOAD_ING_BACK.equals(uploadType))
                                continue;
                            int id = articleData.getId();
                            boolean hasMedia = articleSQLite.checkHasMedia(id);
                            data.put("hasMedia", hasMedia ? "2" : "1");
                            data.put("id", String.valueOf(id));
                            data.put("code", code);
                            data.put("title", title);
                            data.put("classCode", classCode);
                            data.put("content", content);
                            data.put("isOriginal", String.valueOf(isOriginal));
                            data.put("repAddress", repAddress);
                            data.put("imgPath", img);
                            data.put("video", video);
                            data.put("videoUrl", videoUrl);
                            data.put("videoImgUrl", videoImgUrl);
                            data.put("uploadType", uploadType);
                            data.put("imgs", imgs);
                            data.put("videos", videos);
                            data.put("isMe", "2");
                            data.put("dataFrom", String.valueOf(1));//dataFrom:数据来源，本地:1；网络:2,或者null、""、不存在该字段；
                            data.put("extraDataJson",articleData.getExtraDataJson());
                            if (!TextUtils.isEmpty(code)) {
                                mSecondEditDatas.put(code, data);
                                continue;
                            }
                            mLocalDatas.add(data);
                        }
                    }
                }
                onDataReady(false);
            }).start();
        }

        ReqEncyptInternet.in().doEncypt(StringManager.API_USERHOME_VIDEO, "code=" + userCode + "&page=" + currentPage, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                int loadCount = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    loadCount = parseInfo(returnObj);
                }
                if (everyPage == 0)
                    everyPage = loadCount;
                currentPage = loadManager.changeMoreBtn(theListView, flag, everyPage, loadCount, currentPage, datas.size() == 0);
                if (flag < UtilInternet.REQ_OK_STRING) {
                    Tools.showToast(mAct, returnObj.toString());
                    return;
                }
                setHeadViewHeight();
                onDataReady(true);
            }
        });
    }

    private void onDataReady(final boolean fromNet) {
        mAct.runOnUiThread(() -> {
            if (!fromNet) {
                mLocalDataReady = true;
            } else {
                mNetDataReady = true;
            }
            if ((!mLocalDataReady || !mNetDataReady) && isMyselft)
                return;
            if (isRefresh) {
                datas.clear();
                datas.addAll(mLocalDatas);
                isRefresh = false;
            }
            if (!mSecondEditDatas.isEmpty() && !mNetDatas.isEmpty()) {
                ListIterator<Map<String, String>> netDatas = mNetDatas.listIterator();
                while (netDatas.hasNext()) {
                    Map<String, String> netData = netDatas.next();
                    String code = netData.get("code");
                    if (!TextUtils.isEmpty(code) && mSecondEditDatas.size() > 0 && mSecondEditDatas.containsKey(code)) {
                        netDatas.set(mSecondEditDatas.get(code));
                        mSecondEditDatas.remove(code);
                    }
                }
            }
            datas.addAll(mNetDatas);
            if (datas.size() == 0 && isMyselft) {
                RelativeLayout.LayoutParams emptyParams = (RelativeLayout.LayoutParams) mEmptyView.getLayoutParams();
                emptyParams.topMargin = mHeadViewHeight;
                mEmptyContainer.setVisibility(View.VISIBLE);
            } else {
                mEmptyContainer.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                theListView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void deleteById(int id) {
        for (int i = 0; i < mLocalDatas.size(); i++) {
            Map<String, String> map = mLocalDatas.get(i);
            if (TextUtils.equals(map.get("id"), String.valueOf(id))) {
                datas.remove(map);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * 解析数据
     * @param returnObj 要解析的数据
     */
    private int parseInfo(Object returnObj) {
        if (mNetDatas != null && mNetDatas.size() > 0)
            mNetDatas.clear();
        int loadCount = 0;
        ArrayList<Map<String, String>> listMySelf = StringManager.getListMapByJson(returnObj);
        if (listMySelf != null && listMySelf.size() > 0) {
            for (int i = 0; i < listMySelf.size(); i++) {
                loadCount++;
                Map<String, String> map = listMySelf.get(i);
                boolean isFilter = false;
                if (isMyselft)
                    map.put("isMe", "2");
                else {
                    map.put("isMe", "1");
                    isFilter = filterOthersData(map);
                }
                if (!isFilter)
                    mNetDatas.add(map);
            }
        }
        return loadCount;
    }
}
