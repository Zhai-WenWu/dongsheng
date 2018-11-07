package amodule.answer.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.popdialog.util.PushManager;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xh.window.FloatingWindow;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.broadcast.ConnectionChangeReceiver;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.answer.adapter.AskAnswerUploadAdapter;
import amodule.answer.db.AskAnswerSQLite;
import amodule.answer.model.AskAnswerModel;
import amodule.answer.upload.AskAnswerUploadListPool;
import amodule.main.Main;
import amodule.upload.UploadListControl;
import amodule.upload.UploadListPool;
import amodule.upload.bean.UploadItemData;
import amodule.upload.bean.UploadPoolData;
import amodule.upload.callback.UploadListUICallBack;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

/**
 * Created by sll on 2017/7/20.
 */

public class AskAnswerUploadListActivity extends BaseActivity {

    private ListView mListView;
    private LinearLayout mAllStart;
    private LinearLayout mAllStop;
    private TextView mUploadStatics;

    private TextView mTitle;
    private TextView mCancelUpload;
    private ImageView mBack;

    private ConnectionChangeReceiver mConnectionChangeReceiver;
    private UploadListPool mListPool;
    private UploadPoolData mUploadPoolData = new UploadPoolData();
    private ArrayList<Map<String, String>> mArrayList = new ArrayList<>();
    private AskAnswerUploadAdapter mAdapter;
    private int mDraftId;
    private String mTimesStamp;
    private String mCoverPath;
    private String mFinalVideoPath;

    private int mHeaderViewCount;
    private boolean mIsStopUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initActivity("上传列表", 4, 0, R.layout.c_view_bar_title_uploadlist, R.layout.a_dish_upload_list);
        initView();
        initData();
        getData();
        registnetworkListener();
    }

    private void initData() {
        Intent intent = getIntent();
        mDraftId = intent.getIntExtra("draftId", 0);
        mTimesStamp = intent.getStringExtra("time");
        mCoverPath = intent.getStringExtra("coverPath");
        mFinalVideoPath = intent.getStringExtra("finalVideoPath");
    }

    private void registnetworkListener() {
        mConnectionChangeReceiver = new ConnectionChangeReceiver(new ConnectionChangeReceiver.ConnectionChangeListener() {
            @Override
            public void disconnect() {
                allStartOrPause(false);
                Toast.makeText(AskAnswerUploadListActivity.this, "网络异常，请检查网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void wifi() {
            }

            @Override
            public void mobile() {
                if (!mIsStopUpload) {
                    allStartOrPause(false);
                    hintNetWork();
                }
            }
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionChangeReceiver, filter);
    }

    private void allStartOrPause(boolean isAllStart) {
        mIsStopUpload = !isAllStart;
        mListPool.allStartOrStop(isAllStart ? UploadListPool.TYPE_START : UploadListPool.TYPE_PAUSE);
        mAllStart.setVisibility(isAllStart ? View.GONE : View.VISIBLE);
        mAllStop.setVisibility(isAllStart ? View.VISIBLE : View.GONE);
        XHClick.mapStat(this, "a_answer_upload", isAllStart ? "全部开始" : "全部暂停", "");
    }

    private void hintNetWork() {
        final DialogManager dialogManager = new DialogManager(AskAnswerUploadListActivity.this);
        dialogManager.createDialog(new ViewManager(dialogManager)
        .setView(new TitleMessageView(AskAnswerUploadListActivity.this)
        .setText("当前不是WiFi环境，是否继续上传？"))
        .setView(new HButtonView(AskAnswerUploadListActivity.this)
        .setNegativeText("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogManager.cancel();
            }
        })
        .setPositiveText("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogManager.cancel();
                allStartOrPause(true);
            }
        }))).show();
    }

    private void getData() {
        if (mDraftId < 1) {
            Toast.makeText(this, "数据异常", Toast.LENGTH_SHORT).show();
            super.finish();
            return;
        }

        mListPool = UploadListControl.getUploadListControlInstance()
                .add(AskAnswerUploadListPool.class,
                        mDraftId, mCoverPath, mFinalVideoPath, mTimesStamp, generateUiCallback());
        ((AskAnswerUploadListPool) mListPool).setUploadOverListener(new AskAnswerUploadListPool.UploadOverListener() {
            @Override
            public void onUploadOver(boolean flag, String response) {
                XHClick.mapStat(AskAnswerUploadListActivity.this, "a_answer_upload", "上传状态", flag ? "成功" : "上传失败");
                if (flag) {
                    AskAnswerSQLite sqLite = new AskAnswerSQLite(XHApplication.in().getApplicationContext());
                    sqLite.deleteData(mUploadPoolData.getDraftId());
                }
                if (Tools.isAppOnForeground() && flag) {
                    if (!PushManager.isNotificationEnabled(AskAnswerUploadListActivity.this)) {
                        getIsTip();
                    }
                    Main.colse_level = 1;
                    Intent intent = new Intent();
                    intent.putExtra("response", response);
                    setResult(RESULT_OK, intent);
                    AskAnswerUploadListActivity.this.finish();
                }
            }
        });
        mUploadPoolData = mListPool.getUploadPoolData();

        AskAnswerModel model = mUploadPoolData.getUploadAskAnswerData();
        if (model == null) {
            finish();
            return;
        }

        mArrayList = mUploadPoolData.getListData();
        mAdapter = new AskAnswerUploadAdapter(this);
        mTitle.setText(AskAnswerModel.TYPE_ANSWER_AGAIN.equals(model.getmType()) ? "追答" : "我答");

        boolean isAutoUpload = getIntent().getBooleanExtra("isAutoUpload", false);
        if (isAutoUpload) {
            if ("wifi".equals(ToolsDevice.getNetWorkType(this))) {
                allStartOrPause(true);
            } else if ("null".equals(ToolsDevice.getNetWorkType(this))) {
                allStartOrPause(false);
            } else {
                allStartOrPause(false);
                hintNetWork();
            }
        } else {
            allStartOrPause(false);
        }

        loadManager.loading(mListView,true);
        loadManager.setLoading(mListView, mAdapter, false, v -> { });
        loadManager.hideProgressBar();
    }

    private UploadListUICallBack generateUiCallback() {
        return new UploadListUICallBack() {
            @Override
            public void changeState() {
                refreshUploadView();
            }

            @Override
            public void changeState(int pos, int index, UploadItemData data) {
            }

            @Override
            public void uploadOver(boolean flag, String responseStr) {
                refreshUploadView();
                mIsStopUpload = true;
            }
        };
    }

    private void refreshUploadView() {
        if (mUploadPoolData == null || mAdapter == null) {
            return;
        }
        mArrayList = mUploadPoolData.getListData();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.setData(mArrayList);
                setStatisInfo();
            }
        });
    }

    private void setStatisInfo() {
        int success = 0;
        int fail = 0;
        int left = 0;
        for (UploadItemData itemData : mUploadPoolData.getTotalDataList()) {
            if (itemData.getState() == UploadItemData.STATE_FAILD) {
                fail++;
            } else if (itemData.getState() == UploadItemData.STATE_SUCCESS) {
                success++;
            } else {
                left++;
            }
            mUploadStatics.setText("已上传" + success + "，剩余" + left + "，失败" + fail);
        }
    }

    private void initView() {
        findViewById(R.id.all_content).setBackgroundColor(Color.parseColor("#ffffff"));
        mTitle = (TextView) findViewById(R.id.title);
        mCancelUpload = (TextView) findViewById(R.id.tv_cancel_upload);
        mUploadStatics = (TextView) findViewById(R.id.tv_upload_statis);
        mBack = (ImageView) findViewById(R.id.iv_back);
        mListView = (ListView) findViewById(R.id.list_upload);
        mListView.addHeaderView(getHeaderView());
        mHeaderViewCount++;
        mListView.addFooterView(getFooterView());
        addListener();
    }

    private View getFooterView() {
        return LayoutInflater.from(this)
                .inflate(R.layout.c_upload_footer_item, null);
    }

    private View getHeaderView() {
        View view = LayoutInflater.from(this).inflate(R.layout.c_upload_list_header_item, null);
        mAllStart = (LinearLayout) view.findViewById(R.id.ll_allstart);
        mAllStop = (LinearLayout) view.findViewById(R.id.ll_allstop);
        mAllStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("wifi".equals(ToolsDevice.getNetWorkType(AskAnswerUploadListActivity.this))) {
                    allStartOrPause(true);
                } else if ("null".equals(ToolsDevice.getNetWorkType(AskAnswerUploadListActivity.this))) {
                    Toast.makeText(AskAnswerUploadListActivity.this, "网络异常，请检查网络", Toast.LENGTH_SHORT).show();
                } else {
                    hintNetWork();
                }

            }
        });
        mAllStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allStartOrPause(false);
            }
        });
        return view;
    }

    private void addListener() {
        mCancelUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> itemMap = mAdapter.getItem(position - mHeaderViewCount);
                if (itemMap == null || itemMap.isEmpty())
                    return;
                if (UploadItemData.STATE_FAILD == Integer.valueOf(itemMap.get("state"))) {
                    mListPool.oneStartOrStop(Integer.valueOf(itemMap.get("pos")),
                            Integer.valueOf(itemMap.get("index")), UploadListPool.TYPE_START);
                    XHClick.mapStat(AskAnswerUploadListActivity.this, "a_answer_upload", "点击重试", "");
                }
            }
        });
    }

    private void showCancelDialog() {
        final DialogManager dialogManager = new DialogManager(AskAnswerUploadListActivity.this);
        dialogManager.createDialog(new ViewManager(dialogManager)
        .setView(new TitleMessageView(AskAnswerUploadListActivity.this)
        .setText("确定取消上传吗？"))
        .setView(new HButtonView(AskAnswerUploadListActivity.this)
        .setNegativeText("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogManager.cancel();
            }
        })
        .setPositiveText("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogManager.cancel();
                mListPool.cancelUpload();
                mAllStart.setVisibility(View.VISIBLE);
                mAllStop.setVisibility(View.GONE);
                mIsStopUpload = true;
                XHClick.mapStat(AskAnswerUploadListActivity.this, "a_answer_upload", "取消上传", "");
                AskAnswerUploadListActivity.this.finish();
            }
        }))).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mListPool != null) {
            allStartOrPause(false);
            mListPool.cancelUpload();
        }
    }

    private void getIsTip() {
        ReqEncyptInternet.in().doEncypt(StringManager.API_QA_ISTIP, "type=2", new InternetCallback() {
            @Override
            public void loaded(int i, String s, Object obj) {
                {
                    if (i >= UtilInternet.REQ_OK_STRING) {
                        Map<String, String> map = StringManager.getFirstMap(obj);
                        if (map != null && !map.isEmpty() && "2".equals(map.get("isTip"))) {
                            final FloatingWindow window = FloatingWindow.getInstance(XHApplication.in().getApplicationContext());
                            window.setView(new TitleMessageView(XHApplication.in().getApplicationContext())
                                    .setText("开启推送通知，用户的追问或评价结果将在第一时间通知你，是否开启？"))
                                    .setView(new HButtonView(XHApplication.in().getApplicationContext())
                                    .setPositiveText("是", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            window.cancelFloatingWindow();
                                            PushManager.requestPermission(AskAnswerUploadListActivity.this);
                                            XHClick.mapStat(AskAnswerUploadListActivity.this, "a_ask_push", "作者推送", "是");
                                        }
                                    })
                                    .setNegativeText("否", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            window.cancelFloatingWindow();
                                            XHClick.mapStat(AskAnswerUploadListActivity.this, "a_ask_push", "作者推送", "否");
                                        }
                                    })).setCancelable(true).showFloatingWindow();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnectionChangeReceiver != null)
            unregisterReceiver(mConnectionChangeReceiver);
    }
}
