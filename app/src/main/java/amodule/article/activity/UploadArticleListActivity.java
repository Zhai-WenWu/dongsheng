package amodule.article.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.ToolsDevice;
import amodule.article.upload.ArticleUploadListPool;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.video.activity.MediaPreviewActivity;
import amodule.dish.view.CommonDialog;
import amodule.main.Main;
import amodule.upload.UploadListControl;
import amodule.upload.UploadListPool;
import amodule.upload.bean.UploadItemData;
import amodule.upload.bean.UploadPoolData;
import amodule.upload.callback.UploadListUICallBack;
import amodule.user.activity.FriendHome;
import aplug.basic.ReqInternet;

/**
 * 文章上传列表页
 * Created by Fang Ruijiao on 2017/5/23.
 */
public class UploadArticleListActivity extends BaseActivity {
    private ListView mListview;
    private int draftId;
    private String dishName;
    private UploadListPool listPool;
    private UploadPoolData uploadPoolData = new UploadPoolData();
    private ArrayList<Map<String, String>> arrayList = new ArrayList<>();
    private AdapterSimple mAdpater;
    private LinearLayout rl_allstart;
    private LinearLayout rl_allstop;
    private TextView tv_upload_statis;

    //获取到的视频第一帧
    private List<String> bitmapPaths;
    //正在获取第一帧图片的路径
    private HashMap<String, Bitmap> bitmaps;
    private TextView tv_title;
    private TextView tv_cancel_upload;
    private ImageView iv_back;
    private boolean isStopUpload;

    private static final String TAG = "a_videodish_uploadlist";

    //通过传入额外数据，获取步骤视频路径，大图路劲
    private String timesStamp;
    private String coverPath;
    private String finalVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("上传列表", 4, 0, R.layout.c_view_bar_title_uploadlist,
                R.layout.a_dish_upload_list);
        //保持高亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
        initData();
        getData();
        setAdapter();
    }


    @Override
    protected void onResume() {
        super.onResume();
        closePreActivity();
    }


    private void initData() {
        Intent intent = getIntent();
        draftId = intent.getIntExtra("draftId", 0);
        timesStamp = intent.getStringExtra("time");
        coverPath = intent.getStringExtra("coverPath");
        finalVideoPath = intent.getStringExtra("finalVideoPath");
        bitmapPaths = new ArrayList<String>();
        bitmaps = new HashMap<String, Bitmap>();
        isStopUpload = false;
    }

    public void getData() {
        if (draftId < 1) {
            Toast.makeText(this, "数据异常", Toast.LENGTH_SHORT).show();
            super.finish();
            return;
        }

        listPool = UploadListControl.getUploadListControlInstance()
                .add(ArticleUploadListPool.class,
                        draftId,coverPath,finalVideoPath,timesStamp, generateUiCallback());
        uploadPoolData = listPool.getUploadPoolData();

        if(uploadPoolData.getUploadArticleData() == null){
            finish();
            return;
        }
        dishName = uploadPoolData.getTitle();
        arrayList = uploadPoolData.getListData();
        tv_title.setText(dishName);

        if ("wifi".equals(ToolsDevice.getNetWorkType(this))) {
            allStartOrPause(true);
        } else {
            allStartOrPause(false);
        }

        loadManager.changeMoreBtn(ReqInternet.REQ_OK_STRING, -1, -1, 1, true);
        loadManager.setLoading(mListview, mAdpater, false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
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
                if (flag) {
                    isStopUpload = false;
                    gotoFriendHome();
                } else {
                    isStopUpload = true;
                }
            }
        };
    }

    private void initView() {
        tv_title = (TextView) findViewById(R.id.title);
        tv_cancel_upload = (TextView) findViewById(R.id.tv_cancel_upload);
        tv_upload_statis = (TextView) findViewById(R.id.tv_upload_statis);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        mListview = (ListView) findViewById(R.id.list_upload);
        mListview.addHeaderView(getHeaderView());
        mListview.addFooterView(getFooterView());
        addListener();

    }

    private void closePreActivity() {
        if (MediaPreviewActivity.mediaPreWeakRe != null) {
            Activity mediaPreAct = MediaPreviewActivity.mediaPreWeakRe.get();
            MediaPreviewActivity.mediaPreWeakRe = null;
            if (mediaPreAct != null)
                mediaPreAct.finish();
        }

        if(UploadDishActivity.uploaDishWeakRef !=null){
            Activity dishActivity =UploadDishActivity.uploaDishWeakRef.get();
            UploadDishActivity.uploaDishWeakRef = null;
            if (dishActivity != null)
                dishActivity.finish();
        }
    }


    private void addListener() {
        tv_cancel_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String showInfo = "确定取消上传视频吗？";
                String btnMsg1 = "确定";
                String btnMsg2 = "取消";

                final CommonDialog dialog = new CommonDialog(UploadArticleListActivity.this);
                dialog.setMessage(showInfo).setSureButton(btnMsg1, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        listPool.cancelUpload();
                        FriendHome.isRefresh = true;
                        isStopUpload = true;

                        Intent intent = new Intent();
                        intent.setClass(UploadArticleListActivity.this,UploadDishActivity.class);
                        intent.putExtra(UploadDishActivity.DISH_TYPE_KEY,UploadDishActivity.DISH_TYPE_VIDEO);
                        intent.putExtra("id",draftId);
                        intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_DRAFT);
                        startActivity(intent);
                        finish();

                    }
                });
                dialog.setCanselButton(btnMsg2, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
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
                    int newPosition = position;
                    if(String.valueOf(UploadItemData.TYPE_VIDEO).equals(arrayList.get(position).get("type")))
                        newPosition = 0;
                    view.findViewById(R.id.iv_cover_dish).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.iv_cover_dish_last).setVisibility(View.GONE);
                    Glide.with(UploadArticleListActivity.this).load(arrayList.get(newPosition).get("path"))
                            .into(((ImageView) view.findViewById(R.id.iv_cover_dish)));
                }
                ((ProgressBar) view.findViewById(R.id.pb_progress)).setProgress(Integer.parseInt(itemMap.get("progress")));
                ((ProgressBar) view.findViewById(R.id.pb_progress_pause)).setProgress(Integer.parseInt(itemMap.get("progress")));
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (UploadItemData.STATE_FAILD == Integer.valueOf(itemMap.get("state"))) {
                            listPool.oneStartOrStop(Integer.valueOf(itemMap.get("pos")),
                                    Integer.valueOf(itemMap.get("index")), UploadListPool.TYPE_START);
                            XHClick.mapStat(UploadArticleListActivity.this, TAG, "点击重试", "");
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
        View view = LayoutInflater.from(UploadArticleListActivity.this)
                .inflate(R.layout.c_upload_list_header_item, null);
        rl_allstart = (LinearLayout) view.findViewById(R.id.ll_allstart);
        rl_allstop = (LinearLayout) view.findViewById(R.id.ll_allstop);
        rl_allstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allStartOrPause(true);
                isStopUpload = false;
            }
        });
        rl_allstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allStartOrPause(false);
                isStopUpload = true;
            }
        });
        return view;
    }

    public View getFooterView() {
        return LayoutInflater.from(UploadArticleListActivity.this)
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
            Toast.makeText(XHApplication.in().getApplicationContext(),
                    "视频会在后台继续上传", Toast.LENGTH_SHORT).show();
        }
    }

    private void gotoFriendHome() {
        Main.colse_level = 5;
        if (FriendHome.isAlive) {
            FriendHome.isRefresh = true;
        } else {
            Intent intent = new Intent();
            intent.putExtra("code", LoginManager.userInfo.get("code"));
            intent.putExtra("index", 1);
            intent.setClass(this, FriendHome.class);
            FriendHome.isRefresh = true;
            startActivity(intent);
        }
        finish();
    }


    private void allStartOrPause(boolean isAllStart) {
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        listPool.unBindUI();
    }
}
