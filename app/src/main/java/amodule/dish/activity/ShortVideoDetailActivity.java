package amodule.dish.activity;

import android.graphics.PixelFormat;
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

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import amodule.dish.adapter.RvVericalVideoItemAdapter;
import amodule.dish.helper.ParticularPositionEnableSnapHelper;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

public class ShortVideoDetailActivity extends AppCompatActivity {

    public static final String STATISTIC_ID = "a_NewShortVideoDetail";

    private final int UP_SCROLL = 1;
    private final int DOWN_SCROLL = 2;

    private ConstraintLayout mGuidanceLayout;

    private RecyclerView recyclerView;
    private RvVericalVideoItemAdapter rvVericalVideoItemAdapter;
    private ParticularPositionEnableSnapHelper mPagerSnapHelper;

    private DataController mDataController;
    private AtomicBoolean mLoading;
    private AtomicBoolean mOnResuming;
    boolean mCanDispatchTouch = true;

    private String mUserCode;
    private String mSourcePage;
    private String topicCode;
    private ArrayList<Map<String,String>> mapArrayList= new ArrayList<>();
    private int nowPosition = 0;
    private int mCurrentPosition = 0;

    private int mScreenWidth;
    private float mPointerX = -1f;
    public static Map<String,String> favoriteLocalStates= new HashMap<>();//收藏状态集合 1--否，2--是
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFormat(PixelFormat.TRANSPARENT);
        setContentView(R.layout.layout_shortvideo_detail_activity);
        init();
        addListener();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String code = bundle.getString("code");
            if (TextUtils.isEmpty(code)) {
                finish();
                return;
            }
            mUserCode = bundle.getString("userCode");
            mSourcePage = bundle.getString("sourcePage");
            topicCode = bundle.getString("topicCode");
            mDataController.start(code);
        }
    }

    private void addListener() {
        mGuidanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGuidanceLayout.setVisibility(View.GONE);
            }
        });
//        mAdapter.setOnPlayPauseListener(new ShortVideoDetailFragment.OnPlayPauseClickListener() {
//            @Override
//            public void onClick(boolean isPlay) {
//                // TODO: 2018/4/19 处理暂停/播放按钮的点击事件
//            }
//        });
//        mAdapter.setOnSeekBarTrackingTouchListener(new ShortVideoDetailFragment.OnSeekBarTrackingTouchListener() {
//            @Override
//            public void onStartTrackingTouch(int position) {
//                // TODO: 2018/4/19 处理开始触摸进度条的行为
//            }
//
//            @Override
//            public void onStopTrackingTouch(int position) {
//                // TODO: 2018/4/19 处理触摸完毕后的行为
//            }
//        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE){
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
                    int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
                    int orientationScroll = 0;
                    if(lastPosition>=0&&nowPosition!= lastPosition){
                        if (nowPosition > lastPosition) {
                            orientationScroll = DOWN_SCROLL;
                        } else if (nowPosition < lastPosition) {
                            orientationScroll = UP_SCROLL;
                        }
                        nowPosition= lastPosition;
                    }
                    int visibleItemPosition= linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    if(visibleItemPosition>=0 && mCurrentPosition!=visibleItemPosition){
                        rvVericalVideoItemAdapter.stopCurVideoView();
                        mCurrentPosition = visibleItemPosition;
                        View holderView= recyclerView.findViewWithTag(mCurrentPosition);
                        if(holderView!=null){
                            RvVericalVideoItemAdapter.ItemViewHolder itemViewHolder= (RvVericalVideoItemAdapter.ItemViewHolder) recyclerView.getChildViewHolder(holderView);
                            rvVericalVideoItemAdapter.setCurViewHolder(itemViewHolder);
                            if (mOnResuming.get()) {
                                rvVericalVideoItemAdapter.startCurVideoView();
                            }
                        }
                    }
                    //处理请求下一页
                    if (nowPosition >= mapArrayList.size() - 1) {
                        mDataController.executeNextOption();
                    }
                    XHClick.mapStat(ShortVideoDetailActivity.this, STATISTIC_ID, "视频", orientationScroll == DOWN_SCROLL ? "上滑（下一条）" : "下滑（上一条）");
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    /**
     * 第一次视频播放
     */
    public void startVideoOne(){
        if(mCurrentPosition==0){
            View holderView= recyclerView.findViewWithTag(mCurrentPosition);
            if(holderView!=null){
                RvVericalVideoItemAdapter.ItemViewHolder itemViewHolder= (RvVericalVideoItemAdapter.ItemViewHolder) recyclerView.getChildViewHolder(holderView);
                rvVericalVideoItemAdapter.setCurViewHolder(itemViewHolder);
                rvVericalVideoItemAdapter.startCurVideoView();
            }
        }
    }

    private void init() {
        recyclerView= findViewById(R.id.recyclerView);
        mPagerSnapHelper = new ParticularPositionEnableSnapHelper();
        mPagerSnapHelper.attachToRecyclerView(recyclerView);
        mGuidanceLayout = findViewById(R.id.guidance_layout);
        rvVericalVideoItemAdapter= new RvVericalVideoItemAdapter(this,mapArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(rvVericalVideoItemAdapter);
//        mAdapter = new ShortVideoDetailPagerAdapter(this, getSupportFragmentManager());
//        mViewPager.setAdapter(mAdapter);
        mDataController = new DataController();
        mLoading = new AtomicBoolean(false);
        mOnResuming = new AtomicBoolean(false);
        mScreenWidth = ToolsDevice.getWindowPx(this).widthPixels;
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
                    mPagerSnapHelper.particularTargetSnapPositionEnable(mCurrentPosition);
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
        rvVericalVideoItemAdapter.rumeseVideoView();
        handleItemDataChange();
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
        rvVericalVideoItemAdapter.pauseVideoView();
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
        rvVericalVideoItemAdapter.stopCurVideoView();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class DataController {
        private static final int COUNT_EACH_PAGE = 10;
        private static final int COUNT_CODES_CACHE = 50;
        private int mNextPageStartPosition;
        private ArrayList<String> mCodes;

        public DataController() {
            mCodes = new ArrayList<>();
        }

        private void executeNextOption() {
            innerExecuteNextOption(false);
        }

        private ArrayList<String> getNextPageCodes() {
            int codesSize = mCodes.size();
            int lastPosition = codesSize - 1;
            if (mNextPageStartPosition > lastPosition) {
                return null;
            } else {
                ArrayList<String> ret = new ArrayList<>();
                for (int i = 0; i < COUNT_EACH_PAGE; i ++) {
                    int getPos = mNextPageStartPosition + i;
                    if (getPos > lastPosition)
                        break;
                    ret.add(mCodes.get(getPos));
                }
                return ret;
            }
        }

        public void start(String code) {
            addCode(code);
            loadVideoDetail(mCodes, false);
        }

        private boolean checkPrepareNext() {
            return mCodes.size() - mNextPageStartPosition < COUNT_EACH_PAGE;
        }

        private void addCode(String code) {
            mCodes.add(code);
        }

        /**
         * 加载详情
         * @param codes
         * @param loadMore
         */
        private void loadVideoDetail(ArrayList<String> codes,boolean loadMore) {
            if (codes == null || codes.isEmpty())
                return;
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
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(o);
                        mapArrayList.addAll(datas);
                        rvVericalVideoItemAdapter.notifyDataSetChanged();
//                        mAdapter.setData(datas);
                        if(mapArrayList.size()>0){
                            startVideoOne();
                        }
                        if (checkPrepareNext())
                            innerExecuteNextOption(true);

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
         */
        private void loadVideoCodes(String lastCode, Runnable successRun, Runnable failedRun) {
            if (mLoading.get() || lastCode == null || lastCode.isEmpty())
                return;
            mLoading.set(true);
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
                    mLoading.set(false);
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        ArrayList<Map<String, String>> maps = StringManager.getListMapByJson(StringManager.getFirstMap(o).get("list"));
                        for (int i = 0; i < maps.size(); i++) {
                            String code = maps.get(i).get("");
                            addCode(code);
                        }
                        if (successRun != null)
                            successRun.run();
                    } else {
                        if (failedRun != null)
                            failedRun.run();
                    }
                }
            });
        }

        /**
         *
         * @param fromInner
         */
        private void innerExecuteNextOption(boolean fromInner) {
            ArrayList<String> nextPageCodes = getNextPageCodes();
            int lastPos = mCodes.size() - 1;
            if (nextPageCodes == null || nextPageCodes.isEmpty()) {
                loadVideoCodes(mCodes.get(lastPos), new Runnable() {
                    @Override
                    public void run() {
                        if (lastPos >= mCodes.size() - 1)
                            return;
                        if (mNextPageStartPosition - 1 == lastPos)
                            loadVideoDetail(getNextPageCodes(), true);
                    }
                }, null);
            } else if (/*nextPageCodes.size() < COUNT_EACH_PAGE && */!fromInner) {
                loadVideoDetail(nextPageCodes, true);
            }
        }
    }

    /**
     * 刷新当前item数据
     */
    private void handleItemDataChange(){
        if(mCurrentPosition>=0 && mapArrayList.size()>mCurrentPosition){
            View holderView= recyclerView.findViewWithTag(mCurrentPosition);
            if(holderView!=null){
                RvVericalVideoItemAdapter.ItemViewHolder itemViewHolder= (RvVericalVideoItemAdapter.ItemViewHolder) recyclerView.getChildViewHolder(holderView);
                itemViewHolder.bindData(mCurrentPosition,mapArrayList.get(mCurrentPosition));
            }
        }
    }

}
