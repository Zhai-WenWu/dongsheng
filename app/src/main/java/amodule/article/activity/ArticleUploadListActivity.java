package amodule.article.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.broadcast.ConnectionChangeReceiver;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.article.activity.edit.ArticleEidtActivity;
import amodule.article.activity.edit.EditParentActivity;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.article.db.UploadArticleData;
import amodule.article.upload.ArticleUploadListPool;
import amodule.main.Main;
import amodule.upload.UploadListControl;
import amodule.upload.UploadListPool;
import amodule.upload.bean.UploadItemData;
import amodule.upload.bean.UploadPoolData;
import amodule.upload.callback.UploadListUICallBack;
import amodule.user.Broadcast.UploadStateChangeBroadcasterReceiver;
import amodule.user.activity.FriendHome;
import aplug.basic.ReqInternet;

/**
 * 文章上传列表页
 * Created by XiangHa on 2017/5/23.
 */
public class ArticleUploadListActivity extends BaseActivity {
    private ListView mListview;
    private int draftId;
    private UploadListPool listPool;
    private UploadPoolData uploadPoolData = new UploadPoolData();
    private ArrayList<Map<String, String>> arrayList = new ArrayList<>();
    private AdapterSimple mAdpater;
    private LinearLayout rl_allstart;
    private LinearLayout rl_allstop;
    private TextView tv_upload_statis;

    private TextView tv_title;
    private TextView tv_cancel_upload;
    private ImageView iv_back;
    private boolean isStopUpload;

    private static final String TAG = "a_videodish_uploadlist";

    //通过传入额外数据，获取步骤视频路径，大图路劲
    private String timesStamp;
    private String coverPath;
    private String finalVideoPath;

    private int dataType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("上传列表", 4, 0, R.layout.c_view_bar_title_uploadlist,R.layout.a_dish_upload_list);
        dataType = getIntent().getIntExtra("dataType",0);
        Log.i("articleUpload","ArticleUploadListActivity dataType:" + dataType);
        if(dataType != EditParentActivity.DATA_TYPE_ARTICLE && dataType != EditParentActivity.DATA_TYPE_VIDEO){
            Tools.showToast(this,"发布数据类型为空");
            finish();
        }
        //保持高亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
        initData();
        getData();
        setAdapter();
        registnetworkListener();
    }


    private void initData() {
        Intent intent = getIntent();
        draftId = intent.getIntExtra("draftId", 0);
        timesStamp = intent.getStringExtra("time");
        coverPath = intent.getStringExtra("coverPath");
        finalVideoPath = intent.getStringExtra("finalVideoPath");
        Log.i("articleUpload","initData() draftId:" + draftId + "    timesStamp:" + timesStamp + "    coverPath:" + coverPath + "    finalVideoPath:" + finalVideoPath);
        isStopUpload = false;
    }

    public void getData() {
        if (draftId < 1) {
            Toast.makeText(this, "数据异常", Toast.LENGTH_SHORT).show();
            super.finish();
            return;
        }

        listPool = UploadListControl.getUploadListControlInstance()
                .add(dataType,ArticleUploadListPool.class,
                        draftId,coverPath,finalVideoPath,timesStamp, generateUiCallback());
        uploadPoolData = listPool.getUploadPoolData();

        UploadArticleData articleData = uploadPoolData.getUploadArticleData();
        if(articleData == null){
            finish();
            return;
        }
        boolean isSecondEdit = !TextUtils.isEmpty(articleData.getCode());

        if (listPool instanceof ArticleUploadListPool) {
            ((ArticleUploadListPool)listPool).setIsSecondEdit(isSecondEdit);
        }
        String dishName = uploadPoolData.getTitle();
        arrayList = uploadPoolData.getListData();
        if(!TextUtils.isEmpty(dishName)) {
            tv_title.setText(dishName.length() > 7 ? dishName.substring(0,6) + "..." : dishName);
        }

        boolean isAutoUpload = getIntent().getBooleanExtra("isAutoUpload",false);
        if(isAutoUpload) {
            if ("wifi".equals(ToolsDevice.getNetWorkType(this))) {
                allStartOrPause(true);
            } else {
                allStartOrPause(false);
                hintNetWork();
            }
        }else{
            allStartOrPause(false);
        }

        loadManager.loading(mListview,true);
        loadManager.setLoading(mListview, mAdpater, false, v -> { });
        loadManager.hideProgressBar();
    }

    private UploadListUICallBack generateUiCallback() {
        return new UploadListUICallBack() {
            @Override
            public void changeState() {
//                Log.i("articleUpload","改变ui回调 changeState()");
                refreshUploadView();
            }

            @Override
            public void changeState(int pos, int index, UploadItemData data) {
//                Log.i("articleUpload","改变ui回调 changeState() " + pos + "   index:" + index);
            }
            @Override
            public void uploadOver(boolean flag, String responseStr) {
                Log.i("articleUpload","改变ui回调 uploadOver flag:" + flag + "   responseStr:" + responseStr);
                refreshUploadView();
                isStopUpload = !flag;
//                gotoFriendHome();
            }
        };
    }

    private void initView() {
        findViewById(R.id.all_content).setBackgroundColor(Color.parseColor("#ffffff"));
        tv_title = (TextView) findViewById(R.id.title);
        tv_cancel_upload = (TextView) findViewById(R.id.tv_cancel_upload);
        tv_upload_statis = (TextView) findViewById(R.id.tv_upload_statis);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        mListview = (ListView) findViewById(R.id.list_upload);
        mListview.addHeaderView(getHeaderView());
        mListview.addFooterView(getFooterView());
        addListener();

    }

    private void addListener() {
        tv_cancel_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String showInfo = "确定取消上传文章吗？";
                if(dataType == EditParentActivity.DATA_TYPE_VIDEO)
                    showInfo = "确定取消上传视频吗？";
                String btnMsg1 = "确定";
                String btnMsg2 = "取消";

                final DialogManager dialogManager = new DialogManager(ArticleUploadListActivity.this);
                dialogManager.createDialog(new ViewManager(dialogManager)
                        .setView(new TitleMessageView(ArticleUploadListActivity.this).setText(showInfo))
                        .setView(new HButtonView(ArticleUploadListActivity.this)
                                .setPositiveText(btnMsg1, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogManager.cancel();
                                        listPool.cancelUpload();
                                        if (FriendHome.isAlive) {
                                            Intent broadIntent = new Intent();
                                            broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
                                            String type = "";
                                            if (dataType == EditParentActivity.DATA_TYPE_ARTICLE)
                                                type = "2";
                                            else if (dataType == EditParentActivity.DATA_TYPE_VIDEO)
                                                type = "1";
                                            if (!TextUtils.isEmpty(type))
                                                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, type);
                                            Main.allMain.sendBroadcast(broadIntent);
                                        }
                                        isStopUpload = true;

                                        Intent intent = new Intent();
                                        if(dataType == EditParentActivity.DATA_TYPE_ARTICLE){
                                            intent.setClass(ArticleUploadListActivity.this, ArticleEidtActivity.class);
                                        }else if(dataType == EditParentActivity.DATA_TYPE_VIDEO){
                                            intent.setClass(ArticleUploadListActivity.this, VideoEditActivity.class);
                                        }
                                        intent.putExtra("draftId",draftId);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .setNegativeText(btnMsg2, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogManager.cancel();
                                    }
                                }))).show();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
    }

    private void setAdapter() {
        mAdpater = new AdapterSimple(mListview, arrayList,
                R.layout.c_upload_dishvideo_item,
                new String[]{"makeStep", "stateInfo", "totleLength"},
                new int[]{R.id.tv_tilte, R.id.tv_upload_state, R.id.tv_sizs}) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final Map<String, String> itemMap = arrayList.get(position);
                switch (Integer.valueOf(itemMap.get("state"))) {
                    case UploadItemData.STATE_RUNNING:
                        view.findViewById(R.id.rl_upload_info).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.tv_upload_state).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.tv_sizs).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.pb_progress).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.pb_progress_pause).setVisibility(View.GONE);
                        view.findViewById(R.id.ll_upload_success_item).setVisibility(View.GONE);
                        view.findViewById(R.id.ll_upload_fail_item).setVisibility(View.GONE);
                        break;
                    case UploadItemData.STATE_FAILD:
                        view.findViewById(R.id.rl_upload_info).setVisibility(View.GONE);
                        view.findViewById(R.id.tv_sizs).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.ll_upload_success_item).setVisibility(View.GONE);
                        view.findViewById(R.id.ll_upload_fail_item).setVisibility(View.VISIBLE);
                        break;
                    case UploadItemData.STATE_SUCCESS:
                        view.findViewById(R.id.rl_upload_info).setVisibility(View.GONE);
                        view.findViewById(R.id.tv_sizs).setVisibility(View.GONE);
                        view.findViewById(R.id.ll_upload_success_item).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.ll_upload_fail_item).setVisibility(View.GONE);
                        break;
                    case UploadItemData.STATE_PAUSE:
                        view.findViewById(R.id.rl_upload_info).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.tv_sizs).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.pb_progress).setVisibility(View.GONE);
                        view.findViewById(R.id.pb_progress_pause).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.tv_sizs).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.ll_upload_success_item).setVisibility(View.GONE);
                        view.findViewById(R.id.ll_upload_fail_item).setVisibility(View.GONE);
                        break;
                    case UploadItemData.STATE_WAITING:
                        view.findViewById(R.id.rl_upload_info).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.tv_sizs).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.pb_progress).setVisibility(View.GONE);
                        view.findViewById(R.id.pb_progress_pause).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.tv_sizs).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.ll_upload_success_item).setVisibility(View.GONE);
                        view.findViewById(R.id.ll_upload_fail_item).setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
                ((TextView) view.findViewById(R.id.tv_upload_state)).setText(itemMap.get("stateInfo"));
                if (position == arrayList.size() - 1) {
                    view.findViewById(R.id.iv_cover_dish).setVisibility(View.GONE);
                    view.findViewById(R.id.iv_cover_dish_last).setVisibility(View.VISIBLE);
                }else{
                    String imgPath = arrayList.get(position).get("path");
                    if(String.valueOf(UploadItemData.TYPE_VIDEO).equals(arrayList.get(position).get("type")))
                        imgPath = arrayList.get(position).get("videoImage");
                    ImageView ivCoverDish = (ImageView) view.findViewById(R.id.iv_cover_dish);
                    ivCoverDish.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.iv_cover_dish_last).setVisibility(View.GONE);
                    Glide.with(ArticleUploadListActivity.this).load(imgPath)
                            .override(Tools.getDimen(ArticleUploadListActivity.this,R.dimen.dp_123)
                                    ,Tools.getDimen(ArticleUploadListActivity.this,R.dimen.dp_69))
                            .into(ivCoverDish);
                }
                ((ProgressBar) view.findViewById(R.id.pb_progress)).setProgress(Integer.parseInt(itemMap.get("progress")));
                ((ProgressBar) view.findViewById(R.id.pb_progress_pause)).setProgress(Integer.parseInt(itemMap.get("progress")));
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (UploadItemData.STATE_FAILD == Integer.valueOf(itemMap.get("state"))) {
                            listPool.oneStartOrStop(Integer.valueOf(itemMap.get("pos")),
                                    Integer.valueOf(itemMap.get("index")), UploadListPool.TYPE_START);
                            XHClick.mapStat(ArticleUploadListActivity.this, TAG, "点击重试", "");
                        }
                    }
                });
                return view;
            }

            @Override
            public void setViewImage(ImageView v, String value) {

            }
        };
        mAdpater.scaleType = ImageView.ScaleType.CENTER_CROP;
        mListview.setAdapter(mAdpater);
    }

    private View getHeaderView() {
        View view = LayoutInflater.from(ArticleUploadListActivity.this).inflate(R.layout.c_upload_list_header_item, null);
        rl_allstart = (LinearLayout) view.findViewById(R.id.ll_allstart);
        rl_allstop = (LinearLayout) view.findViewById(R.id.ll_allstop);
        rl_allstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("wifi".equals(ToolsDevice.getNetWorkType(ArticleUploadListActivity.this))) {
                    allStartOrPause(true);
                } else {
                    hintNetWork();
                }

            }
        });
        rl_allstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allStartOrPause(false);
            }
        });
        return view;
    }

    public View getFooterView() {
        return LayoutInflater.from(ArticleUploadListActivity.this)
                .inflate(R.layout.c_upload_footer_item, null);
    }

    private void setStatisInfo() {
        int success = 0;
        int fail = 0;
        int left = 0;
        for (UploadItemData itemData : uploadPoolData.getTotalDataList()) {
            if (itemData.getState() == UploadItemData.STATE_FAILD) {
                fail++;
            } else if (itemData.getState() == UploadItemData.STATE_SUCCESS) {
                success++;
            } else {
                left++;
            }
            tv_upload_statis.setText("已上传" + success + "，剩余" + left + "，失败" + fail);
        }
    }

    private void refreshUploadView() {
        if (uploadPoolData == null || mAdpater == null) {
            return;
        }
        arrayList = uploadPoolData.getListData();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdpater.notifyDataSetChanged();
                setStatisInfo();
            }
        });
    }


    @Override
    public void onBackPressed() {
        goBack();
        super.onBackPressed();
    }



    private void goBack() {
        gotoFriendHome();
        if (!isStopUpload) {
            if(dataType == EditParentActivity.DATA_TYPE_ARTICLE){
                Toast.makeText(XHApplication.in().getApplicationContext(),"文章会在后台继续上传", Toast.LENGTH_SHORT).show();
            }else if(dataType == EditParentActivity.DATA_TYPE_VIDEO){
                Toast.makeText(XHApplication.in().getApplicationContext(),"视频会在后台继续上传", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void gotoFriendHome() {
        Log.i("articleUpload","gotoFriendHome() FriendHome.isAlive:" + FriendHome.isAlive + "   code:" + LoginManager.userInfo.get("code"));
        Main.colse_level = 5;
        if (FriendHome.isAlive) {
            Intent broadIntent = new Intent();
            broadIntent.setAction(UploadStateChangeBroadcasterReceiver.ACTION);
            String type = "";
            if (this.dataType == EditParentActivity.DATA_TYPE_ARTICLE)
                type = "2";
            else if (this.dataType == EditParentActivity.DATA_TYPE_VIDEO)
                type = "1";
            if (!TextUtils.isEmpty(type))
                broadIntent.putExtra(UploadStateChangeBroadcasterReceiver.DATA_TYPE, type);
            Main.allMain.sendBroadcast(broadIntent);
        } else {
            Intent intent = new Intent();
            intent.putExtra("code", LoginManager.userInfo.get("code"));
            if(dataType == EditParentActivity.DATA_TYPE_ARTICLE)
                intent.putExtra("index", 3);
            else if(dataType == EditParentActivity.DATA_TYPE_VIDEO)
                intent.putExtra("index", 2);
            intent.setClass(this, FriendHome.class);
            startActivity(intent);
        }
        finish();
    }

    private void allStartOrPause(boolean isAllStart) {
        Log.i("articleUpload","allStartOrPause() isAllStart:" + isAllStart);
        isStopUpload = !isAllStart;
        if (isAllStart) {
            listPool.allStartOrStop(UploadListPool.TYPE_START);
            rl_allstart.setVisibility(View.GONE);
            rl_allstop.setVisibility(View.VISIBLE);
        } else {
            listPool.allStartOrStop(UploadListPool.TYPE_PAUSE);
            rl_allstart.setVisibility(View.VISIBLE);
            rl_allstop.setVisibility(View.GONE);
        }
    }

    private ConnectionChangeReceiver connectionChangeReceiver;
    private void registnetworkListener(){
        connectionChangeReceiver = new ConnectionChangeReceiver(new ConnectionChangeReceiver.ConnectionChangeListener() {
            @Override
            public void disconnect() {}

            @Override
            public void wifi() {}

            @Override
            public void mobile() {
                if(!isStopUpload){
                    allStartOrPause(false);
                    hintNetWork();
                }
            }
        });
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionChangeReceiver,filter);
    }

    public void unregistnetworkListener(){
        if(connectionChangeReceiver != null){
            unregisterReceiver(connectionChangeReceiver);
        }
    }

    private void hintNetWork(){
        final DialogManager dialogManager = new DialogManager(ArticleUploadListActivity.this);
        dialogManager.createDialog(new ViewManager(dialogManager)
                .setView(new TitleMessageView(ArticleUploadListActivity.this).setText("当前不是WiFi环境，是否继续上传？"))
                .setView(new HButtonView(ArticleUploadListActivity.this)
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listPool != null)
            listPool.unBindUI();
       unregistnetworkListener();
    }
}
