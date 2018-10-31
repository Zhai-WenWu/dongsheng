package amodule.user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.MessageTipController;
import acore.logic.XHClick;
import acore.notification.controller.NotificationSettingController;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.FileManager;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule._common.utility.WidgetUtility;
import amodule.answer.activity.QAMsgListActivity;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.adapter.AdapterMainMsg;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import aplug.feedback.activity.Feedback;
import third.qiyu.QiYvHelper;
import xh.basic.internet.UtilInternet;

import static acore.notification.controller.NotificationSettingController.pushSetMessage;
import static acore.notification.controller.NotificationSettingController.push_show_message;
import static acore.tools.ObserverManager.NOTIFY_LOGIN;
import static acore.tools.ObserverManager.NOTIFY_LOGOUT;
import static acore.tools.ObserverManager.NOTIFY_MESSAGE_REFRESH;

public class MyMessage extends BaseAppCompatActivity implements OnClickListener, IObserver {
    public static final String KEY = "MyMessage";
    private DownRefreshList listMessage;
    private TextView feekback_msg_num, msg_title_sort;

    private AdapterMainMsg adapter;
    private ArrayList<Map<String, String>> listDataMessage = new ArrayList<>();

    private String pageTime = "";
    private int currentPage = 0, everyPage = 0;
    private boolean clickFlag = true;

    private RelativeLayout noLoginLayout;
    private TextView mMyQANum;
    private TextView mQiYvNum;
    private Handler handller=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.a_common_message);
        init();
        XHClick.track(this, "浏览消息列表页");
        ObserverManager.getInstance().registerObserver(this, NOTIFY_LOGIN, NOTIFY_LOGOUT, NOTIFY_MESSAGE_REFRESH);
        handller = new Handler(Looper.getMainLooper());
        handller.postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationSettingController.showNotification(push_show_message,pushSetMessage);
            }
        },1000*3);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 初始化七鱼客服未读消息数
     */
    private void initQiYvNum() {
        MessageTipController.newInstance().loadQiyuUnreadCount(count -> setQiYvNum());
    }

    /** 设置消息数的显示 */
    private void setQiYvNum() {
        if (mQiYvNum != null) {
            int qiyuMsg = MessageTipController.newInstance().getQiyvMessage();
            String qiyuNumValue = qiyuMsg > 0 ? String.valueOf(qiyuMsg) : "";
            WidgetUtility.setTextToView(mQiYvNum, qiyuNumValue);
        }
    }

    private void setFeekbackMsg() {
        if (feekback_msg_num != null) {
            int feekbackMsg = MessageTipController.newInstance().getFeekbackMessage();
            String feekbackNumValue = feekbackMsg > 0 ? String.valueOf(feekbackMsg) : "";
            WidgetUtility.setTextToView(feekback_msg_num, feekbackNumValue);
        }
    }

    private void setQAMsgNum() {
        if (mMyQANum != null) {
            int qaMsg = MessageTipController.newInstance().getMyQAMessage();
            String myQANumValue = qaMsg > 0 ? String.valueOf(qaMsg) : "";
            WidgetUtility.setTextToView(mMyQANum, myQANumValue);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(handller!=null) {
            handller.removeCallbacksAndMessages(null);
            handller=null;
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unRegisterObserver(this);
    }

    /** 外面调用的刷新 */
    public void onRefresh() {
        // 标示用户登录啦
        if (LoginManager.isLogin()) {
            noLoginLayout.setVisibility(View.GONE);
            if (ToolsDevice.getNetActiveState(this)) {
                setRefresh();
                refresh();
            }
            MessageTipController.newInstance().setQuanMessage(0);
        } else {
            noLoginLayout.setVisibility(View.VISIBLE);
        }
        findViewById(R.id.tv_noData).setVisibility(View.GONE);
    }

    public void login(View view) {
        startActivity(new Intent(this, LoginByAccout.class));
    }

    private void init() {
        noLoginLayout = (RelativeLayout) findViewById(R.id.no_login_rela);
        noLoginLayout.setVisibility(LoginManager.isLogin() ? View.GONE : View.VISIBLE);

        // title初始化
        TextView title = (TextView) findViewById(R.id.msg_title_tv);
        title.setText("消息");
        msg_title_sort = (TextView) findViewById(R.id.msg_title_sort);
        msg_title_sort.setText("未读");
        msg_title_sort.setVisibility(View.VISIBLE);
        findViewById(R.id.leftText).setVisibility(View.INVISIBLE);
        findViewById(R.id.leftImgBtn).setVisibility(View.VISIBLE);

        // 结果显示
        listMessage = (DownRefreshList) findViewById(R.id.lv_message);
        listMessage.setVisibility(View.GONE);

        initHeader();

        title.setOnClickListener(this);
        msg_title_sort.setOnClickListener(this);
        noLoginLayout.setOnClickListener(this);
        findViewById(R.id.no_admin_linear).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);

        if (LoginManager.isLogin()) {
            initQiYvNum();
        }

        setFeekbackMsg();
        setQAMsgNum();
        adapter = new AdapterMainMsg(this, listMessage, listDataMessage, 0, null, null);
        loadManager.setLoading(listMessage, adapter, true,
                v -> {
//                    if (isCreated)
                        load();
//                    else {
//                        isCreated = true;
//                        refresh();
//                    }
                },
                v -> refresh());
    }
    private  void initHeader(){
        RelativeLayout headerView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.a_common_message_header, null);
        RelativeLayout headerSecretary = (RelativeLayout) headerView.findViewById(R.id.secretary);
        RelativeLayout headerMyQA = (RelativeLayout) headerView.findViewById(R.id.my_qa);
        mMyQANum = (TextView) headerMyQA.findViewById(R.id.qa_msg_num);
        RelativeLayout headerQY = (RelativeLayout) headerView.findViewById(R.id.qiyv);
        mQiYvNum = (TextView) headerQY.findViewById(R.id.qiyv_msg_num);
        feekback_msg_num = (TextView) headerView.findViewById(R.id.feekback_msg_num);
        listMessage.addHeaderView(headerView, null, false);

        headerSecretary.setOnClickListener(this);
        headerMyQA.setOnClickListener(this);
        headerQY.setOnClickListener(this);
    }

    /** 刷新view */
    public void setRefresh() {
        listMessage.onRefreshStart();
    }

    private void startFeekback() {
        XHClick.mapStat(this, "a_message", "点击香哈小秘书", "");
        startActivity(new Intent(this, Feedback.class));
    }

    public void refresh(){
        currentPage = 0;
        everyPage = 0;
        pageTime = "";
        MessageTipController.newInstance().setQuanMessage(0);
        load();
    }

    /** 加载数据 */
    private void load() {
        currentPage++;
        String getUrl = StringManager.api_message + "?type=" + (clickFlag ? "all" : "asc") + "&page=" + currentPage;
        if(!TextUtils.isEmpty(pageTime))
            getUrl += "&pageTime=" + pageTime;
        loadManager.loading(listMessage,listDataMessage.isEmpty());
        ReqInternet.in().doGet(getUrl, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                int loadCount = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    if (currentPage == 1) listDataMessage.clear();
                    ArrayList<Map<String, String>> listReturn = StringManager.getListMapByJson(returnObj);
                    for (int i = 0; i < listReturn.size(); i++) {
                        Map<String, String> map = listReturn.get(i);
                        map.put("addTimeShow", map.get("addTimeShow"));
                        pageTime = map.get("pageTime");
                        if (map.get("msgType").equals("1")) {
                            map.put("adminName", "");
                            map.put("admin", "");
                            // 点赞图标显示时,不显示评论内容;
                            if (map.get("type") != null && map.get("type").equals("3")) {
                                map.put("content", "");
                                map.put("isLike", "ico" + R.drawable.z_quan_home_body_ico_good_active);
                            } else
                                map.put("isLike", "");
                            // 主题图片显示时,不显示主题标题
                            if (map.get("img") != null && map.get("img").length() > 5) {
                                map.put("title", "hide");
                            } else
                                map.put("img", "hide");
                            String info_url = map.get("url");
                            if (!TextUtils.isEmpty(info_url)) {
                                Map<String, String> info_map = StringManager.getMapByString(info_url.substring(info_url.indexOf("subjectInfo.php?") + 16),
                                        "&", "=");
                                map.put("subjectCode", info_map.get("code"));
                                map.put("floorNum", info_map.get("floorNum"));
                            }
                            // 添加回复人回复信息;
                            if (listReturn.get(i).containsKey("customer")) {
                                Map<String, String> customer = StringManager.getFirstMap(listReturn.get(i).get("customer"));
                                map.put("nickName", customer.get("nickName"));
                                map.put("nickImg", customer.get("img"));
                                map.put("nickCode", customer.get("code"));
                                map.put("isGourmet", customer.get("isGourmet"));
                                if (customer.containsKey("url") && !TextUtils.isEmpty(customer.get("url")))
                                    map.put("customerUrl", customer.get("url"));
                            }
                        } else if (map.get("msgType").equals("2")) {
                            map.put("nickName", "");
                            map.put("nickImg", "id" + R.drawable.z_me_ico_mypage);
                            map.put("isLike", "");
                            map.put("admin", "官方");
                            map.put("adminName", "管理员");
                        } else if (map.get("msgType").equals("3")) {
                            map.put("admin", "");
                            map.put("adminName", "");
                            map.put("isLike", "");
                            if (listReturn.get(i).containsKey("customer")) {
                                Map<String, String> customer = StringManager.getFirstMap(listReturn.get(i).get("customer"));
                                map.put("nickName", customer.get("nickName"));
                                map.put("nickImg", customer.get("img"));
                                map.put("nickCode", customer.get("code"));
                                map.put("isGourmet", customer.get("isGourmet"));
                                if (customer.containsKey("url") && !TextUtils.isEmpty(customer.get("url")))
                                    map.put("customerUrl", customer.get("url"));
                            }
                        }
                        if (!map.get("content").equals(""))
                            map.put("content", map.get("content") + " ");
                        if (map.get("state").equals("1"))
                            map.put("bgColor", "#FFFDE3");
                        listDataMessage.add(map);
                    }
                    loadCount = listReturn.size();
                    // 标示用户登录啦
                    noLoginLayout.setVisibility(LoginManager.isLogin() ? View.GONE : View.VISIBLE);
                    findViewById(R.id.tv_noData).setVisibility(currentPage == 1 && loadCount == 0 && LoginManager.isLogin() ? View.VISIBLE:View.GONE);
                }
                listMessage.setVisibility(View.VISIBLE);
                if (everyPage == 0)
                    everyPage = loadCount;
                loadManager.loadOver(flag,listMessage,loadCount);
                if (currentPage == 1)
                    adapter.notifyDataSetInvalidated();
                else
                    adapter.notifyDataSetChanged();
                // 如果总数据为空,显示没有消息
                listMessage.onRefreshComplete();
            }
        });
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        switch (name) {
            case NOTIFY_LOGIN:
            case NOTIFY_LOGOUT:
                onRefresh();
                break;
            case NOTIFY_MESSAGE_REFRESH:
                setFeekbackMsg();
                setQAMsgNum();
                setQiYvNum();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.secretary:
                startFeekback();
                break;
            case R.id.my_qa:
                if (mMyQANum != null && mMyQANum.getVisibility() == View.VISIBLE) {
                    MessageTipController.newInstance().setMyQAMessage(0);
                    mMyQANum.setText("");
                    mMyQANum.setVisibility(View.GONE);
                }
                startActivity(new Intent(MyMessage.this, QAMsgListActivity.class));
                XHClick.mapStat(MyMessage.this, "a_message", "点击我问我答", "");
                break;
            case R.id.qiyv:
                MessageTipController.newInstance().setQiyvMessage(0);
                setQiYvNum();
                Map<String, String> customMap = new HashMap<>();
                customMap.put("pageTitle", "消息列表页");
                QiYvHelper.getInstance().startServiceAcitivity(MyMessage.this, null, null, customMap);
                break;
            case R.id.msg_title_tv:
                //7.29新添加统计
                XHClick.mapStat(MyMessage.this, "a_switch_message", "消息中心", "");
                //回到顶部
                AppCommon.scorllToIndex(listMessage,0);
                break;
            case R.id.msg_title_sort:
                switchMessageType();
                break;
            case R.id.no_admin_linear:
                startFeekback();
                break;
            case R.id.back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private void switchMessageType() {
        XHClick.mapStat(MyMessage.this, "a_message", "点击未读按钮", "");
        clickFlag = !clickFlag;
        msg_title_sort.setText(clickFlag ? "未读" : "全部");
        refresh();
    }
}
