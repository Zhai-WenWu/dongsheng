/**
 * 
 * @author intBird 20140213.
 * 
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
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.load.LoadManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import amodule.article.activity.edit.ArticleEidtActiivty;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadVideoSQLite;
import amodule.user.activity.FriendHome;
import amodule.user.adapter.AdapterUserVideo;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
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
    private ArrayList<Map<String, String>> mNetDatas;
    public AdapterUserVideo adapter;
    private boolean mLocalDataReady;
    private boolean mNetDataReady;

    private int currentPage = 0, everyPage = 0;
    private boolean isRefresh = false;

    private TextView subjectNum;

    private LinearLayout mTabMainMyself;
    private RelativeLayout mEmptyContainer;
    private LinearLayout mEmptyView;
    private Button mGotoBtn;

    public UserHomeVideo(FriendHome act, String code) {
        view = View.inflate(act, R.layout.myself_txt, null);
        this.mAct = act;
        userCode = code;
        if(!TextUtils.isEmpty(LoginManager.userInfo.get("code")) && LoginManager.userInfo.get("code").equals(userCode)){
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
        theListView.setSelection(1);
        if(subjectNum == null) {
            mTabMainMyself = (LinearLayout) mAct.findViewById(R.id.a_user_home_title_tab);
            subjectNum = (TextView) mTabMainMyself.getChildAt(0).findViewById(R.id.tab_data);
        }
    }

    private void init() {
        // 结果显示
        loadManager = mAct.loadManager;
        mEmptyContainer = (RelativeLayout) view.findViewById(R.id.empty_container);
        mEmptyView = (LinearLayout) mEmptyContainer.findViewById(R.id.empty);
        mGotoBtn = (Button) mEmptyView.findViewById(R.id.goto_btn);
        mGotoBtn.setText("发视频");
        mGotoBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAct.startActivity(new Intent(mAct, ArticleEidtActiivty.class));
            }
        });
        theListView = (DownRefreshList) view.findViewById(R.id.list_myself_subject);
        theListView.setDivider(null);
        datas = new ArrayList<>();
        mLocalDatas = new ArrayList<Map<String, String>>();
        mNetDatas = new ArrayList<Map<String, String>>();
        adapter = new AdapterUserVideo(mAct, theListView, datas, 0, null, null);
        adapter.scaleType = ScaleType.CENTER_CROP;
        adapter.isAnimate = true;
        adapter.setOnItemClickListener(new UserHomeItem.OnItemClickListener() {
            @Override
            public void onItemClick(UserHomeItem itemView, Map<String, String> dataMap) {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(itemView, dataMap);
            }
        });
    }

    private UserHomeItem.OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(UserHomeItem.OnItemClickListener clickListener) {
        mOnItemClickListener = clickListener;
    }

    @Override
    public void initLoad() {
        currentPage = 0;
        isRefresh = true;
        theListView.setVisibility(View.GONE);
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

    private void setHeadViewHeight(){
        final int tabHost_h = Tools.getDimen(mAct, R.dimen.dp_51);
        final int bigImg_h = Tools.getDimen(mAct, R.dimen.dp_200) + Tools.getStatusBarHeight(mAct);
        final int userinfo_h = Tools.getTargetHeight(friend_info);
        try {
            if (friend_info.getText() == null || friend_info.getText().toString().equals(""))
                headView.setLayoutParams(new AbsListView.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        tabHost_h + bigImg_h));
            else
                headView.setLayoutParams(new AbsListView.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        tabHost_h + bigImg_h + userinfo_h));
        } catch (Exception e) {
            UtilLog.reportError("MyselfSubject头部局异常", e);
        }

    }

    private void loadFromServer() {
        loadManager.changeMoreBtn(theListView,UtilInternet.REQ_OK_STRING, -1, -1, currentPage,datas.size() == 0);
        if (isRefresh) {
            mNetDataReady = false;
            if(isMyselft)
                mLocalDataReady = false;
        }
        if (isRefresh && isMyselft) {
            if (mLocalDatas != null)
                mLocalDatas.clear();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UploadVideoSQLite articleSQLite = new UploadVideoSQLite(mAct);
                    ArrayList<UploadArticleData> articleDatas = articleSQLite.getAllUploadIngData();
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
                                String video = "";
                                String videoUrl = "";
                                String videoImgUrl = "";
                                ArrayList<Map<String,String>> videoArray = articleData.getVideoArray();
                                if(videoArray.size() > 0){
                                    video = videoArray.get(0).get("video");
                                    videoUrl = videoArray.get(0).get("videoUrl");
                                    videoImgUrl = videoArray.get(0).get("imageUrl");
                                }
                                String uploadType = articleData.getUploadType();
                                int id = articleData.getId();
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
                                data.put("isMe", "2");
                                data.put("dataFrom", String.valueOf(1));//dataFrom:数据来源，本地:1；网络:2,或者null、""、不存在该字段；
                                mLocalDatas.add(data);
                            }
                        }
                    }
                    onDataReady(false);
                }
            }).start();
        }

        String getUrl = StringManager.API_USERHOME_VIDEO + "?code=" + userCode + "&page=" + currentPage;
        ReqEncyptInternet.in().doEncypt(getUrl, "", new InternetCallback(mAct) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                int loadCount = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    loadCount = parseInfo(returnObj);
                } else {
                    Tools.showToast(mAct, returnObj.toString());
                }
                if (everyPage == 0)
                    everyPage = loadCount;
                currentPage = loadManager.changeMoreBtn(theListView,flag, everyPage, loadCount, currentPage,datas.size() == 0);
                onDataReady(true);
                setHeadViewHeight();
                mAct.show();
            }
        });
    }

    private void onDataReady(final boolean fromNet) {
        mAct.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                datas.addAll(mNetDatas);
                if (datas.size() == 0 && isMyselft) {
                    RelativeLayout.LayoutParams emptyParams = (RelativeLayout.LayoutParams) mEmptyView.getLayoutParams();
                    emptyParams.topMargin = mTabMainMyself.getTop() + mTabMainMyself.getHeight();
                    mEmptyContainer.setVisibility(View.VISIBLE);
                } else {
                    mEmptyContainer.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    theListView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 解析数据
     * @param returnObj 要解析的数据
     */
    public int parseInfo(Object returnObj) {
        if (isRefresh) {
            if (mNetDatas != null)
                mNetDatas.clear();
        }
        int loadCount = 0;
        ArrayList<Map<String, String>> listMySelf = StringManager.getListMapByJson(returnObj);
        if(listMySelf != null && listMySelf.size() > 0) {
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
                if(!isFilter)
                    mNetDatas.add(map);
            }
        }
        return loadCount;
    }
}
