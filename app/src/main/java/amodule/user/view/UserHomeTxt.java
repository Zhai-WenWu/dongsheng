/**
 * 
 * @author intBird 20140213.
 * 
 */
package amodule.user.view;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.load.LoadManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadArticleSQLite;
import amodule.user.activity.FriendHome;
import amodule.user.adapter.AdapterUserTxt;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilLog;

/**
 * 我的页面：文章
 */
public class UserHomeTxt extends TabContentView {
	private View headView;
	private FriendHome mAct;
	private String userCode = "";
	private boolean isMyselft = false;
	private LoadManager loadManager;
	public ArrayList<Map<String, String>> datas;
    private ArrayList<Map<String, String>> mLocalDatas;
    private ArrayList<Map<String, String>> mNetDatas;
	public AdapterUserTxt adapter;
	private boolean mLocalDataReady;
	private boolean mNetDataReady;

	private int currentPage = 0, everyPage = 0;
	private boolean isRefresh = false;

	private TextView subjectNum;

	public UserHomeTxt(FriendHome act, String code) {
		view = View.inflate(act, R.layout.myself_subject, null);
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
			LinearLayout tabMainMyself = (LinearLayout) mAct.findViewById(R.id.a_user_home_title_tab);
			subjectNum = (TextView) tabMainMyself.getChildAt(0).findViewById(R.id.tab_data);
		}
	}

	private void init() {
		// 结果显示
		loadManager = mAct.loadManager;
		theListView = (DownRefreshList) view.findViewById(R.id.list_myself_subject);
		theListView.setDivider(null);
		datas = new ArrayList<>();
        mLocalDatas = new ArrayList<Map<String, String>>();
        mNetDatas = new ArrayList<Map<String, String>>();
		adapter = new AdapterUserTxt(mAct, theListView, datas, 0, null, null,new UserHomeItem.DeleteCallback() {
			@Override
			public void delete(int position) {
				String numS = String.valueOf(subjectNum.getText());
				if(!TextUtils.isEmpty(numS)){
					try{
						int num = Integer.parseInt(numS);
						subjectNum.setText(String.valueOf(--num));
					}catch (Exception e){
						e.printStackTrace();
					}
				}
				datas.remove(position);
				adapter.notifyDataSetChanged();
			}
		});
		adapter.scaleType = ScaleType.CENTER_CROP;
		adapter.isAnimate = true;
	}

	@Override
	public void initLoad() {
		currentPage = 0;
		isRefresh = true;
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
		}, datas.size() == 0, new LoadManager.ViewScrollCallBack() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
		});
	}

	/**
	 * 详情页
	 */
	private void goNextActivity(int position){
		String isSafa="";
		Map<String,String> map = datas.get(position);
		if (map.containsKey("isSafa"))
			isSafa = map.get("isSafa");
		if (map.containsKey("style") && map.get("style").equals("6"))
			isSafa = "qiang";
		AppCommon.openUrl(mAct, "subjectInfo.app?code=" + map.get("code") + "&isSafa=" + isSafa, true);
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
                    UploadArticleSQLite articleSQLite = new UploadArticleSQLite(mAct);
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
                                String video = articleData.getVideo();
                                String videoUrl = articleData.getVideoUrl();
                                String videoImgUrl = articleData.getVideoImgUrl();
                                String uploadType = articleData.getUploadType();
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

		String getUrl = url + "?code=" + userCode + "&page=" + currentPage;
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
                if (datas.size() == 0) {
                    view.findViewById(R.id.tv_noData).setVisibility(View.VISIBLE);
                } else {
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
