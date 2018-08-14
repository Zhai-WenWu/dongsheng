package amodule.dish.activity;

import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.broadcast.ConnectionChangeReceiver;
import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.IObserver;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule._common.conf.GlobalVariableConfig;
import amodule.dish.adapter.RvVericalVideoItemAdapter;
import amodule.dish.helper.ParticularPositionEnableSnapHelper;
import amodule.dish.video.module.ShareModule;
import amodule.dish.video.module.ShortVideoDetailModule;
import amodule.topic.model.AddressModel;
import amodule.topic.model.CustomerModel;
import amodule.topic.model.ImageModel;
import amodule.topic.model.TopicModel;
import amodule.topic.model.VideoModel;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

public class ShortVideoDetailActivity extends AppCompatActivity implements IObserver {

    public static final String STATISTIC_ID = "a_NewShortVideoDetail";

    private final int UP_SCROLL = 1;
    private final int DOWN_SCROLL = 2;

    private ConstraintLayout mGuidanceLayout;

    private RecyclerView recyclerView;
    private RvVericalVideoItemAdapter rvVericalVideoItemAdapter;
    private ParticularPositionEnableSnapHelper mPagerSnapHelper;

    private DataController mDataController;
    private AtomicBoolean mOnResuming;
    private boolean mFirstPlayStarted;
    boolean mCanDispatchTouch = true;

    private String mUserCode;
    private String mSourcePage;
    private String topicCode;
    private ArrayList<ShortVideoDetailModule> mDatas = new ArrayList<>();

    private int mScreenWidth;
    private float mPointerX = -1f;
    public static Map<String,String> favoriteLocalStates= new HashMap<>();//收藏状态集合 1--否，2--是

    private ConnectionChangeReceiver mReceiver;
    private DialogManager mNetStateTipDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFormat(PixelFormat.TRANSPARENT);
        setContentView(R.layout.layout_shortvideo_detail_activity);
        if ("null".equals(ToolsDevice.getNetWorkSimpleType(XHApplication.in()))) {
            Tools.showToast(this,"网络异常，请检查网络");
            finish();
            return;
        }
        init();
        addListener();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
//            String json = bundle.getString("json");
//            json = Uri.decode(json);
//            if (json != null) {
//                // TODO: 2018/8/7 组装数据 直接加载
//            } else {
                String code = bundle.getString("code");
                if (TextUtils.isEmpty(code)) {
                    finish();
                    return;
                }
                mUserCode = bundle.getString("userCode");
                mSourcePage = bundle.getString("sourcePage");
                topicCode = bundle.getString("topicCode");
                mDataController.start(code);
//            }
        }
        ObserverManager.getInstance().registerObserver(this, ObserverManager.NOTIFY_SHARE);
        registerConnectionReceiver();
    }

    private void addListener() {
        mGuidanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGuidanceLayout.setVisibility(View.GONE);
            }
        });
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                int pos = recyclerView.getChildAdapterPosition(view);
                if (pos == 0 && !mFirstPlayStarted) {
                    mFirstPlayStarted = true;
                    RvVericalVideoItemAdapter.ItemViewHolder viewHolder = (RvVericalVideoItemAdapter.ItemViewHolder)recyclerView.getChildViewHolder(view);
                    rvVericalVideoItemAdapter.setCurrentViewHolder(viewHolder);
                    String netState = ToolsDevice.getNetWorkSimpleType(XHApplication.in());
                    switch (netState) {
                        case "wifi":
                            viewHolder.startVideo();
                            break;
                        case "null":
                            Tools.showToast(ShortVideoDetailActivity.this,"加载失败，请重试");
                            break;
                        default:
                            if (canShowTipDialog()) {
                                showNetworkTip();
                            } else {
                                viewHolder.startVideo();
                            }
                            break;
                    }
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {

            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE){
                    LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int pos = llm.findLastCompletelyVisibleItemPosition();
                    RvVericalVideoItemAdapter.ItemViewHolder currentHolder = (RvVericalVideoItemAdapter.ItemViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
                    RvVericalVideoItemAdapter.ItemViewHolder adapterLastHolder = rvVericalVideoItemAdapter.getCurrentViewHolder();
                    if (currentHolder == adapterLastHolder || adapterLastHolder == null || currentHolder == null)
                        return;
                    rvVericalVideoItemAdapter.setCurrentViewHolder(currentHolder);
                    int lastState = adapterLastHolder.getPlayState();
                    switch (lastState) {
                        case GSYVideoPlayer.CURRENT_STATE_PLAYING:
                            adapterLastHolder.pauseVideo();
                            break;
                        case GSYVideoPlayer.CURRENT_STATE_PLAYING_BUFFERING_START:
                        case GSYVideoPlayer.CURRENT_STATE_PREPAREING:
                        case GSYVideoPlayer.CURRENT_STATE_AUTO_COMPLETE:
                        case GSYVideoPlayer.CURRENT_STATE_ERROR:
                            adapterLastHolder.stopVideo();
                            break;
                        default:
                            adapterLastHolder.stopVideo();
                            break;
                    }
                    int currState = currentHolder.getPlayState();
                    switch (currState) {
                        case GSYVideoPlayer.CURRENT_STATE_PAUSE:
                            currentHolder.resumeVideo();
                            break;
                        default:
                            currentHolder.startVideo();
                            break;
                    }
                    int orientationScroll = 0;
                    if(currentHolder.getAdapterPosition() > adapterLastHolder.getAdapterPosition()){
                        orientationScroll = DOWN_SCROLL;
                    } else if (currentHolder.getAdapterPosition() < adapterLastHolder.getAdapterPosition()) {
                        orientationScroll = UP_SCROLL;
                    }
                    //处理请求下一页
                    if (currentHolder.getAdapterPosition() >= mDatas.size() - 1) {
                        mDataController.executeNextOption();
                    }
                    if (orientationScroll !=0)
                        XHClick.mapStat(ShortVideoDetailActivity.this, STATISTIC_ID, "视频", orientationScroll == DOWN_SCROLL ? "上滑（下一条）" : "下滑（上一条）");
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void init() {
        recyclerView= findViewById(R.id.recyclerView);
        mPagerSnapHelper = new ParticularPositionEnableSnapHelper();
        mPagerSnapHelper.attachToRecyclerView(recyclerView);
        mGuidanceLayout = findViewById(R.id.guidance_layout);
        rvVericalVideoItemAdapter= new RvVericalVideoItemAdapter(this,mDatas);
        recyclerView.setItemViewCacheSize(3);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(rvVericalVideoItemAdapter);
        mDataController = new DataController();
        mOnResuming = new AtomicBoolean(false);
        mScreenWidth = ToolsDevice.getWindowPx(this).widthPixels;
    }

    private void registerConnectionReceiver() {
        mReceiver = new ConnectionChangeReceiver(new ConnectionChangeReceiver.ConnectionChangeListener() {
            @Override
            public void disconnect() {
                Tools.showToast(ShortVideoDetailActivity.this,"加载失败，请重试");
            }

            @Override
            public void wifi() {
                if (mNetStateTipDialog != null && mNetStateTipDialog.isShowing()) {
                    mNetStateTipDialog.cancel();
                    if (rvVericalVideoItemAdapter != null) {
                        RvVericalVideoItemAdapter.ItemViewHolder currentHolder = rvVericalVideoItemAdapter.getCurrentViewHolder();
                        if (currentHolder != null) {
                            int playState = currentHolder.getPlayState();
                            switch (playState) {
                                case GSYVideoPlayer.CURRENT_STATE_PAUSE:
                                    currentHolder.resumeVideo();
                                    break;
                                default:
                                    currentHolder.startVideo();
                                    break;
                            }
                        }
                    }
                }
            }

            @Override
            public void mobile() {
                if (canShowTipDialog()) {
                    showNetworkTip();
                }
            }
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mPointerX == -1) {
                    mPointerX = ev.getX();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float currentX = ev.getX();
                if (mPointerX - currentX > mScreenWidth / 5 && recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    mCanDispatchTouch = false;
                    RvVericalVideoItemAdapter.ItemViewHolder currHolder = rvVericalVideoItemAdapter.getCurrentViewHolder();
                    if (currHolder != null) {
                        mPagerSnapHelper.particularTargetSnapPositionEnable(currHolder.getAdapterPosition());
                    }
                    gotoUser();
                    resetPointerX();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                mCanDispatchTouch = true;
                resetPointerX();
                break;
        }
        if (!mCanDispatchTouch) {
            return true;
        }
        mPagerSnapHelper.invalidParticularTargetSnapPosition();
        return super.dispatchTouchEvent(ev);
    }

    private void resetPointerX() {
        mPointerX = -1f;
    }

    public void gotoUser() {
        if (rvVericalVideoItemAdapter != null) {
            rvVericalVideoItemAdapter.notifyGotoUser();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mOnResuming.set(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOnResuming.set(true);
        checkShowGuidance();
        rvVericalVideoItemAdapter.onResume();
    }

    private void checkShowGuidance() {
        String show = (String) FileManager.loadShared(this, FileManager.xhmKey_shortVideoGuidanceShow, "show");
        if (TextUtils.equals(show, "2"))
            return;
        mGuidanceLayout.setVisibility(View.VISIBLE);
        FileManager.saveShared(this, FileManager.xhmKey_shortVideoGuidanceShow, "show", "2");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOnResuming.set(false);
        rvVericalVideoItemAdapter.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mOnResuming.set(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOnResuming.set(false);
        rvVericalVideoItemAdapter.onDestroy();
        ObserverManager.getInstance().unRegisterObserver(this);
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void notify(String name, Object sender, Object data) {
        if (name == null)
            return;
        switch (name) {
            case ObserverManager.NOTIFY_SHARE:
                if (data != null) {
                    Map<String, String> dataMap = (Map<String, String>) data;
                    String videoCode = dataMap.get("callbackParams");
                    if (!TextUtils.isEmpty(videoCode)) {
                        for (int i = 0; i < mDatas.size(); i ++) {
                            ShortVideoDetailModule module = mDatas.get(i);
                            if (module != null && TextUtils.equals(module.getCode(), videoCode)) {
                                try {
                                    module.setShareNum(String.valueOf(Integer.parseInt(module.getShareNum()) + 1));
                                    // TODO: 2018/8/9 有漏洞 不能直接notify item，
                                    rvVericalVideoItemAdapter.notifyItemChanged(i);
                                } catch (Exception e) {}
                            }
                        }
                    }
                }
                break;
        }
    }

    private class DataController {
        private static final int COUNT_EACH_PAGE = 10;
        private int mNextPageStartPosition;
        private ArrayList<String> mCodes;

        private AtomicBoolean mCodesLoading;
        private AtomicBoolean mLoadCodesEnable;
        private AtomicBoolean mDetailLoading;
        private AtomicBoolean mLoadDetailEnable;

        public DataController() {
            mCodes = new ArrayList<>();
            mCodesLoading = new AtomicBoolean(false);
            mLoadCodesEnable = new AtomicBoolean(true);
            mDetailLoading = new AtomicBoolean(false);
            mLoadDetailEnable = new AtomicBoolean(true);
        }

        public void executeNextOption() {
            ArrayList<String> nextPageCodes = getNextPageCodes();
            if (!nextPageCodes.isEmpty()) {
                loadVideoDetail(nextPageCodes, true);
                if (nextPageCodes.size() < COUNT_EACH_PAGE) {
                    loadVideoCodes(nextPageCodes.get(nextPageCodes.size() - 1), null, null, false);
                }
            } else {
                loadVideoCodes(mCodes.get(mNextPageStartPosition - 1), null, null, true);
            }
        }

        private ArrayList<String> getNextPageCodes() {
            int lastPosition = mCodes.size() - 1;
            ArrayList<String> ret = new ArrayList<>();
            for (int i = 0; i < COUNT_EACH_PAGE; i ++) {
                int getPos = mNextPageStartPosition + i;
                if (getPos > lastPosition)
                    break;
                ret.add(mCodes.get(getPos));
            }
            return ret;
        }

        public void start(String code) {
            mCodes.add(code);
            loadVideoDetail(mCodes, false);
            loadVideoCodes(code, null, null, true);
        }

        /**
         * 加载详情
         * @param codes
         * @param loadMore
         */
        private void loadVideoDetail(ArrayList<String> codes,boolean loadMore) {
            if (mDetailLoading.get() || codes == null || codes.isEmpty())
                return;
            mDetailLoading.set(true);
            mNextPageStartPosition += codes.size();
            StringBuffer buffer = new StringBuffer("codes=");
            for (int i = 0; i < codes.size(); i++) {
                buffer.append(codes.get(i));
                if (i != codes.size() - 1) {
                    buffer.append(",");
                }
            }
            String params = buffer.toString();
            ReqEncyptInternet.in().doEncypt(StringManager.api_getVideoInfo, params, new InternetCallback() {
                @Override
                public void loaded(int flag, String s, Object o) {
                    mDetailLoading.set(false);
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        int insertPosStart = mDatas.size();
                        ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(o);
                        for (int i = 0; i < datas.size() && datas.size() > 0; i ++) {
                            Map<String, String> itemMap = datas.get(i);
                            ShortVideoDetailModule module = new ShortVideoDetailModule();
                            module.setCode(itemMap.get("code"));
                            module.setName(itemMap.get("name"));
                            module.setEssence("2".equals(itemMap.get("isEssence")));
                            module.setFav("2".equals(itemMap.get("isFav")));
                            module.setLike("2".equals(itemMap.get("isLike")));
                            module.setFavNum(itemMap.get("favNum"));
                            module.setCommentNum(itemMap.get("commentNum"));
                            module.setLikeNum(itemMap.get("likeNum"));
                            module.setShareNum(itemMap.get("shareNum"));
                            module.setClickNum(itemMap.get("clickNum"));
                            Map<String, String> videoMap = StringManager.getFirstMap(itemMap.get("video"));
                            VideoModel videoModel = new VideoModel();
                            videoModel.setAutoPlay("2".equals(videoMap.get("isAuto")));
                            videoModel.setVideoTime(videoMap.get("time"));
                            videoModel.setPlayableTime(videoMap.get("playableTime"));
                            videoModel.setVideoW(videoMap.get("width"));
                            videoModel.setVideoH(videoMap.get("height"));
                            videoModel.setVideoUrlMap(StringManager.getFirstMap(videoMap.get("videoUrl")));
                            videoModel.setVideoImg(videoMap.get("videoImg"));
                            videoModel.setVideoGif(videoMap.get("videoGif"));
                            module.setVideoModel(videoModel);
                            Map<String, String> imageMap = StringManager.getFirstMap(itemMap.get("image"));
                            ImageModel imageModel = new ImageModel();
                            imageModel.setImageW(imageMap.get("width"));
                            imageModel.setImageH(imageMap.get("height"));
                            imageModel.setImageUrl(imageMap.get("url"));
                            module.setImageModel(imageModel);
                            Map<String, String> customerMap = StringManager.getFirstMap(itemMap.get("customer"));
                            CustomerModel customerModel = new CustomerModel();
                            customerModel.setUserCode(customerMap.get("code"));
                            customerModel.setNickName(customerMap.get("nickName"));
                            customerModel.setHeaderImg(customerMap.get("img"));
                            customerModel.setFollow("2".equals(customerMap.get("isFollow")));
                            customerModel.setGotoUrl(customerMap.get("url"));
                            module.setCustomerModel(customerModel);
                            Map<String, String> topicMap = StringManager.getFirstMap(itemMap.get("topic"));
                            TopicModel topicModel = new TopicModel();
                            topicModel.setCode(topicMap.get("code"));
                            topicModel.setTitle(topicMap.get("title"));
                            topicModel.setColor(topicMap.get("color"));
                            topicModel.setBgColor(topicMap.get("bgColor"));
                            topicModel.setGotoUrl(topicMap.get("url"));
                            module.setTopicModel(topicModel);
                            Map<String, String> addressMap = StringManager.getFirstMap(itemMap.get("address"));
                            AddressModel addressModel = new AddressModel();
                            addressModel.setCode(addressMap.get("code"));
                            addressModel.setAddress(addressMap.get("title"));
                            addressModel.setColor(addressMap.get("color"));
                            addressModel.setBgColor(addressMap.get("bgColor"));
                            addressModel.setGotoUrl(addressMap.get("url"));
                            module.setAddressModel(addressModel);
                            Map<String, String> shareMap = StringManager.getFirstMap(itemMap.get("share"));
                            ShareModule shareModule = new ShareModule();
                            shareModule.setUrl(shareMap.get("url"));
                            shareModule.setContent(shareMap.get("content"));
                            shareModule.setTitle(shareMap.get("title"));
                            shareModule.setImg(shareMap.get("img"));
                            module.setShareModule(shareModule);
                            mDatas.add(module);
                        }
                        if (mDatas.size() != insertPosStart) {
                            rvVericalVideoItemAdapter.notifyItemRangeInserted(insertPosStart, datas.size());
                        }
                    } else {
                        if (!loadMore) {
                            // TODO: 2018/4/17 第一次的网络请求失败
                        } else {
                            // TODO: 2018/4/19 加载更多时加载失败
                        }
                    }
                }
            });
        }

        /**
         *加载ids
         * @param lastCode
         * @param successRun
         * @param failedRun
         * @param needLoadDetail
         */
        private void loadVideoCodes(String lastCode, Runnable successRun, Runnable failedRun, boolean needLoadDetail) {
            if (!mLoadCodesEnable.get() || mCodesLoading.get() || lastCode == null || lastCode.isEmpty()) {
                // TODO: 2018/8/9 正在加载中 或者 数据错误
                return;
            }
            mCodesLoading.set(true);
            StringBuffer sb = new StringBuffer();
            sb.append("code=").append(lastCode).append("&");
            sb.append("sourcePage=").append(mSourcePage).append("&");
            sb.append("userCode=").append(TextUtils.isEmpty(mUserCode) ? "" : mUserCode);
            if(!TextUtils.isEmpty(topicCode)){
                sb.append("&").append("topicCode=").append(topicCode);
            }
            ReqEncyptInternet.in().doEncypt(StringManager.API_SHORT_VIDEOCODES, sb.toString(), new InternetCallback() {
                @Override
                public void loaded(int flag, String s, Object o) {
                    mCodesLoading.set(false);
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(StringManager.getFirstMap(o).get("list"));
                        for (int i = 0; i < maps.size(); i++) {
                            String code = maps.get(i).get("");
                            mCodes.add(code);
                        }
                        if (maps.size() <= 0) {
                            mLoadCodesEnable.set(false);
                        }
                        if (successRun != null)
                            successRun.run();

                        if (needLoadDetail) {
                            loadVideoDetail(getNextPageCodes(), true);
                        }
                    } else {
                        mLoadCodesEnable.set(false);
                        if (failedRun != null)
                            failedRun.run();
                    }
                }
            });
        }
    }

    private void showNetworkTip() {
        if (mNetStateTipDialog != null && mNetStateTipDialog.isShowing()) {
            return;
        }
        if (rvVericalVideoItemAdapter != null) {
            RvVericalVideoItemAdapter.ItemViewHolder currentHolder = rvVericalVideoItemAdapter.getCurrentViewHolder();
            if (currentHolder != null) {
                int playState = currentHolder.getPlayState();
                switch (playState) {
                    case GSYVideoPlayer.CURRENT_STATE_PLAYING:
                        currentHolder.pauseVideo();
                        break;
                    case GSYVideoPlayer.CURRENT_STATE_PLAYING_BUFFERING_START:
                    case GSYVideoPlayer.CURRENT_STATE_PREPAREING:
                    case GSYVideoPlayer.CURRENT_STATE_AUTO_COMPLETE:
                    case GSYVideoPlayer.CURRENT_STATE_ERROR:
                        currentHolder.stopVideo();
                        break;
                    default:
                        currentHolder.stopVideo();
                        break;
                }
            }
        }
        if (mNetStateTipDialog == null) {
            mNetStateTipDialog = new DialogManager(this);
            ViewManager viewManager = new ViewManager(mNetStateTipDialog);
            viewManager.setView(new TitleMessageView(this).setText("非wifi环境，是否使用流量继续观看视频？"))
                    .setView(new HButtonView(this).setPositiveText("继续播放", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mNetStateTipDialog.cancel();
                            if (rvVericalVideoItemAdapter != null) {
                                RvVericalVideoItemAdapter.ItemViewHolder currentHolder = rvVericalVideoItemAdapter.getCurrentViewHolder();
                                if (currentHolder != null) {
                                    int playState = currentHolder.getPlayState();
                                    switch (playState) {
                                        case GSYVideoPlayer.CURRENT_STATE_PAUSE:
                                            currentHolder.resumeVideo();
                                            break;
                                        default:
                                            currentHolder.startVideo();
                                            break;
                                    }
                                }
                            }
                            GlobalVariableConfig.shortVideoDetail_netStateTip_dialogEnable = false;
                        }
                    }).setNegativeText("退出播放", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mNetStateTipDialog.cancel();
                            ShortVideoDetailActivity.this.finish();
                        }
                    }));
            mNetStateTipDialog.setCancelable(false);
            mNetStateTipDialog.createDialog(viewManager);
        }
        mNetStateTipDialog.show();
    }

    private boolean canShowTipDialog() {
        return GlobalVariableConfig.shortVideoDetail_netStateTip_dialogEnable;
    }
}
