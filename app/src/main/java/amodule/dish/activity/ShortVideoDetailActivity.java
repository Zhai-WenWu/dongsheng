package amodule.dish.activity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
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
import amodule.dish.adapter.RvVericalVideoItemAdapter;
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

    private DataController mDataController;
    private AtomicBoolean mLoading;

    private String mUserCode;
    private String mSourcePage;
    private String topicCode;
    private ArrayList<Map<String,String>> mapArrayList= new ArrayList<>();
    private int nowPosition = 0;
    private int mCurrentPosition = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFormat(PixelFormat.TRANSPARENT);
        setContentView(R.layout.layout_shortvideo_detail_activity);
        recyclerView= (RecyclerView) findViewById(R.id.recyclerView);
        new PagerSnapHelper().attachToRecyclerView(recyclerView);
        mGuidanceLayout = (ConstraintLayout) findViewById(R.id.guidance_layout);
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
                            rvVericalVideoItemAdapter.startCurVideoView();
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
        rvVericalVideoItemAdapter= new RvVericalVideoItemAdapter(this,mapArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(rvVericalVideoItemAdapter);
//        mAdapter = new ShortVideoDetailPagerAdapter(this, getSupportFragmentManager());
//        mViewPager.setAdapter(mAdapter);
        mDataController = new DataController();
        mLoading = new AtomicBoolean(false);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkShowGuidance();
        rvVericalVideoItemAdapter.rumeseVideoView();
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
        rvVericalVideoItemAdapter.pauseVideoView();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        private ArrayList<String> mCurrentPageCodes;

        private ArrayList<String> mCodes;
        private Map<String, ArrayList<String>> mCacheCodes;

        public DataController() {
            mCodes = new ArrayList<>();
            mCacheCodes = new HashMap<>();
            mCurrentPageCodes = new ArrayList<>();
        }

        public void executeNextOption() {
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
            String s = code.substring(0, 1);
            ArrayList<String> codes = mCacheCodes.get(s);
            if (codes == null) {
                codes = new ArrayList<>();
                codes.add(code);
                mCacheCodes.put(s, codes);
                mCodes.add(code);
            } else {
                if (!codes.contains(code)) {
                    mCodes.add(code);
                    codes.add(code);
                    if (codes.size() > COUNT_CODES_CACHE)
                        codes.remove(0);
                }
            }
        }

        private void loadVideoDetail(ArrayList<String> codes,boolean loadMore) {
            if (codes == null || codes.isEmpty())
                return;
            mNextPageStartPosition += codes.size();
            StringBuffer buffer = new StringBuffer("code=");
            for (int i = 0; i < codes.size(); i++) {
                buffer.append(codes.get(i));
                if (i != codes.size() - 1) {
                    buffer.append(",");
                }
            }
            String params = buffer.append("&type=RAW").toString();
            ReqEncyptInternet.in().doEncypt(StringManager.api_getVideoInfo, params, new InternetCallback() {
                @Override
                public void loaded(int flag, String s, Object o) {
                    if (flag >= ReqInternet.REQ_OK_STRING) {
                        String listStr = StringManager.getFirstMap(o).get("list");
                        ArrayList<Map<String, String>> datas = StringManager.getListMapByJson(listStr);
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

}
