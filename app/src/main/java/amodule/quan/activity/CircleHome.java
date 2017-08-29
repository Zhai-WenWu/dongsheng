package amodule.quan.activity;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.xianghatest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.XHApplication;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.override.data.UploadData;
import acore.override.helper.UploadHelper.UploadCallback;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.PagerSlidingTabStrip;
import acore.widget.PagerSlidingTabStrip.OnItemDoubleClickListener;
import amodule.main.view.CommonBottomView;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;
import amodule.quan.db.CircleSqlite.CircleDB;
import amodule.quan.db.PlateData;
import amodule.quan.db.SubjectData;
import amodule.quan.db.SubjectSqlite;
import amodule.quan.fragment.CircleFragment;
import amodule.quan.tool.UploadSubjectControl;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.share.BarShare;
import third.share.ShareTools;

/**
 * 圈子首页 该界面 launchMode 为 singleTask
 *
 * @author Eva
 */
public class CircleHome extends BaseAppCompatActivity implements OnClickListener {
    /** 当前subjectData不存在于本地集合中的返回值 */
    private final int NO_EXIST = -1;
    /** 该页面的tab控件 */
    private PagerSlidingTabStrip mTabs;
    private ViewPager mViewPager;
    /** 用户头像可显示的最大值 */
    private TextView mTitle;

    private FragmentManager mFragmentManager;
    /** 板块信息集合 */
    private ArrayList<PlateData> mPlateDataArray = new ArrayList<>();
    /** 发贴是管理SubjectData的集合 */
    public ArrayList<SubjectData> mSubjectDataArray = new ArrayList<>();
    /** 圈子的cid */
    private String cid = "";
    private String name = "";
    private String img = "";
    private String desc = "";
    private String code = "";

    private boolean mLoadrOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sufureView页面闪烁
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        initActivity("", 2, 0, R.layout.c_view_bar_title_circle_home, R.layout.a_circle_detail);

        initView();
        Intent intent = this.getIntent();
        // 获取圈子cid
        cid = intent.getStringExtra("cid");
        // 设置回调
        UploadSubjectControl.getInstance().setUploadCallback(mUploadCallback);
        SubjectSqlite subjectSqlite = SubjectSqlite.getInstance(this);
        batchUpdateSubjectDataArray(subjectSqlite.selectByCidState(cid, SubjectData.UPLOAD_ING), SubjectData.UPLOAD_ING);
        batchUpdateSubjectDataArray(subjectSqlite.selectByCidState(cid, SubjectData.UPLOAD_FAIL), SubjectData.UPLOAD_FAIL);
        if (control != null && mCommonBottomView != null) {
            control.refreshIconOnClickListener(CommonBottomView.BOTTOM_CENTER, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    XHClick.mapStat(CircleHome.this, "a_quan_zi_index", "本圈子发贴按钮", "");
                    XHClick.mapStat(CircleHome.this, "a_down420", "发贴按钮", "");
                    Intent uploadSubject = new Intent(CircleHome.this, UploadSubjectNew.class);
                    uploadSubject.putExtra("cid", cid);
                    CircleSqlite sqlite = new CircleSqlite(CircleHome.this);
                    CircleData circleData = sqlite.select(CircleDB.db_cid, cid);
                    if (circleData != null && "2".equals(circleData.getSkip())) {
                        uploadSubject.putExtra("skip", true);
                    }
                    startActivity(uploadSubject);
                }
            });
        }
    }

    public void batchUpdateSubjectDataArray(ArrayList<SubjectData> array, int state) {
        for (SubjectData data : array) {
            updateSubjectDataArray(data, state);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLoadrOver) {
            return;
        }
        mLoadrOver = true;
        initData();
    }

    /** 初始化数据 */
    private void initData() {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        loadManager.setLoading(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getCirclerInfo();
            }
        });

    }

    private void initView() {
        mTitle = (TextView) findViewById(R.id.title);

        mTabs = (PagerSlidingTabStrip) findViewById(R.id.circle_tab);
        mTabs.setSelected(false);
        mTabs.setmDelegatePageListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                //圈子内版块切换
                if (!TextUtils.isEmpty(name) && mPlateDataArray != null && arg0 < mPlateDataArray.size()) {
                    XHClick.mapStat(CircleHome.this, "a_quan_switch", name, mPlateDataArray.get(arg0).getName());
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        mTabs.setOnItemDoubleClickListener(new OnItemDoubleClickListener() {
            @Override
            public void onItemDoubleClick(View v, int position) {
                if (mViewPager.getCurrentItem() == position) {
                    refreshCurrentFragment();
                }
            }
        });
        mViewPager = (ViewPager) findViewById(R.id.circle_viewpager);
        mViewPager.setOffscreenPageLimit(3);

        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.circle_share).setOnClickListener(this);
        findViewById(R.id.title).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
    }

    private void getCirclerInfo() {
        String url = StringManager.api_circleGetInfo + "?cid=" + cid;
        ReqInternet.in().doGet(url, new InternetCallback(XHApplication.in()) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    List<Map<String, String>> returnData = StringManager.getListMapByJson(msg);
                    if (returnData.size() > 0) {
                        Map<String, String> circleInfo = returnData.get(0);
                        setCirclerInfo(circleInfo);
                    }
                } else {
                    // 失败处理

                }
                loadManager.loadOver(flag, 1, true);
            }
        });
    }

    /**
     * 设置circle数据
     *
     * @param circleInfo
     */
    private void setCirclerInfo(Map<String, String> circleInfo) {
        img = circleInfo.get("img");
        name = circleInfo.get("name");
        mTitle.setText(name);
        // 设置circleInfo的请求api、圈子的cid参数、需要显示的circleInfo
        desc = circleInfo.get("desc") == null ? "" : circleInfo.get("desc");
        code = circleInfo.get("code");
        // 设置模块信息
        initPlate(circleInfo.get("module"));
    }

    /**
     * 初始化板块
     *
     * @param moduleStr
     */
    private void initPlate(String moduleStr) {
        if (!TextUtils.isEmpty(moduleStr)) {
            // 清空模块数据
            if (mPlateDataArray != null) {
                mPlateDataArray.clear();
            }
            List<Map<String, String>> modules = StringManager.getListMapByJson(moduleStr);
            final int length = modules.size();
            for (int index = 0; index < length; index++) {
                Map<String, String> module = modules.get(index);
                PlateData plateData = new PlateData();
                plateData.setCid(cid);
                plateData.setMid(module.get("mId"));
                plateData.setName(module.get("name"));
                plateData.setLocation(module.get("isLocation"));
                plateData.setPosition(index);
                mPlateDataArray.add(plateData);
                break;
            }
        }
        if (mPlateDataArray.size() == 0) {
            Tools.showToast(this, "初始化错误");
            finish();
            return;
        } else if (mPlateDataArray.size() <= 5) {
            mTabs.setTabColumn(mPlateDataArray.size());
            mTabs.updateTabLayoutParams();
        }
        mAdapterPager = new AdapterCircleHomePager(mFragmentManager, mPlateDataArray);
        mViewPager.setAdapter(mAdapterPager);
        mTabs.setViewPager(mViewPager);
        mTabs.setListener();

        mViewPager.setVisibility(View.VISIBLE);
        if (mPlateDataArray.size() == 1) {
            mTabs.setVisibility(View.GONE);
        } else {
            mTabs.setVisibility(View.VISIBLE);
        }
    }

    /** FragmentPagerAdapter */
    public class AdapterCircleHomePager extends FragmentPagerAdapter {
        private ArrayList<PlateData> mPlates;

        public AdapterCircleHomePager(FragmentManager fm, ArrayList<PlateData> titles) {
            super(fm);
            this.mPlates = titles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPlates.get(position).getName();
        }

        @Override
        public Fragment getItem(int position) {
            PlateData plateData = mPlates.get(position);
            CircleFragment fragment = CircleFragment.newInstance(plateData);
            Bundle bundle = fragment.getArguments();
            bundle.putString(CircleFragment.CIRCLENAME, name);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return mPlates.size();
        }
    }

    /** 上传回调 */
    private UploadCallback mUploadCallback = new UploadCallback() {
        @Override
        public void uploading(int id) {
            SubjectSqlite sqlite = SubjectSqlite.getInstance(CircleHome.this);
            SubjectData subjectData = sqlite.selectById(id);
            if (subjectData == null) {
                return;
            }
            if (cid.equals(subjectData.getCid())) {
                updateSubjectDataArray(subjectData, SubjectData.UPLOAD_ING);
                // 通知Fragment刷新
                updateFragment(subjectData, true);
            }
        }

        @Override
        public void uploadOver(final UploadData uploadData, final int flag, Object msg) {
            SubjectData subjectData = (SubjectData) uploadData;
            if (flag >= ReqInternet.REQ_OK_STRING) {
                // 发布成功
                // 修改数据状态
                updateSubjectDataArray(subjectData, SubjectData.UPLOAD_SUCCESS);
                //发布成功删除本地视频
                if(subjectData != null
                        && !TextUtils.isEmpty(subjectData.getVideo())
                        && !TextUtils.isEmpty(subjectData.getVideoLocalPath())){
                    File videoFile = new File(subjectData.getVideoLocalPath());
                    Log.d("video delete","video delete " + String.valueOf(videoFile.delete()));
                }
            } else {
                // 发布失败
                // 修改数据状态
                Log.i("shortVideo","subjectData::"+subjectData.getVideoLocalPath());
                updateSubjectDataArray(subjectData, SubjectData.UPLOAD_FAIL);
            }
            // 通知Fragment刷新
            updateFragment(subjectData, false);
        }


    };

    /**
     * 根据对应的subjectData数据获取fragment
     *
     * @param subjectData
     *
     * @return
     */
    private CircleFragment getRefreshFragmentByMid(SubjectData subjectData) {
        String mid = subjectData.getMid();
        if (mFragmentManager == null) {
            return null;
        }
        List<Fragment> fragmentArray = mFragmentManager.getFragments();
        if (fragmentArray != null && fragmentArray.size() > 0) {
            int length = fragmentArray.size();
            for (int index = 0; index < length; index++) {
                CircleFragment refreshFragment = (CircleFragment) fragmentArray.get(index);
                if (TextUtils.isEmpty(mid) && refreshFragment!=null && refreshFragment.getmPlateData()!=null&&refreshFragment.getmPlateData().getPosition() == 0) {
                    return refreshFragment;
                } else if (refreshFragment!=null && refreshFragment.getmPlateData()!=null&&mid.equals(refreshFragment.getmPlateData().getMid())) {
                    return refreshFragment;
                }
            }
        }
        return null;
    }

    /**
     * 通知fragment更新数据和界面
     *
     * @param subjectData
     */
    private void updateFragment(SubjectData subjectData, boolean isUploading) {
        CircleFragment refreshFragment = getRefreshFragmentByMid(subjectData);
        if (refreshFragment != null) {
            if (isUploading) {
                mViewPager.setCurrentItem(mAdapterPager.getItemPosition(refreshFragment));
                refreshFragment.refresh();
            }
            //
            refreshFragment.updateCircleHeader(mSubjectDataArray);
        }
    }

    private AdapterCircleHomePager mAdapterPager;

    /**
     * 更新mSubjectDataArray的数据 如果数据不存在则添加，存在则更新数据
     *
     * @param subjectData
     * @param state
     */
    private void updateSubjectDataArray(SubjectData subjectData, int state) {
        subjectData.setUploadState(state);
        int index = contains(subjectData);
        if (NO_EXIST == index) {
            mSubjectDataArray.add(subjectData);
        } else {
            mSubjectDataArray.remove(index);
            mSubjectDataArray.add(index, subjectData);
        }
    }

    /**
     * 判断集合中是否有该数据
     *
     * @param subjectData
     *
     * @return 该数据在集合中的index，返回-1则是不存在
     */
    public int contains(SubjectData subjectData) {
        int id = subjectData.getId();
        return contains(id);
    }

    private int contains(int id) {
        int length = mSubjectDataArray.size();
        for (int index = 0; index < length; index++) {
            if (id == mSubjectDataArray.get(index).getId()) {
                return index;
            }
        }
        return NO_EXIST;
    }

    /**
     * 根据SubjectData 的id移除发送失败的数据
     *
     * @param id
     */
    public void removeFailedSubjec(int id) {
        int index = contains(id);
//		//删除数据库
//		SubjectSqlite sqlite = new SubjectSqlite(this);
//		sqlite.deleteById(id);
        //移除相应数据
        if (NO_EXIST != id && index < mSubjectDataArray.size()) {
            mSubjectDataArray.remove(index);
        }
    }

    /**
     * 移除所有对应板块的发送成功的数据
     *
     * @param mid
     */
    public void removeAllSuccessSubject(String mid) {
        for (int index = 0; index < mSubjectDataArray.size(); index++) {
            SubjectData data = mSubjectDataArray.get(index);
            if (data == null || (SubjectData.UPLOAD_SUCCESS == data.getUploadState()
                    && (mid == null || TextUtils.isEmpty(data.getMid()) || data.getMid().equals(mid)))) {
                mSubjectDataArray.remove(index--);
            }
        }
    }

    /** 给界面设置新的intent */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //启动模式为singleTask，
        String newCid = intent.getStringExtra("cid");
        if (!cid.equals(newCid) && newCid != null) {
            // 界面销毁时将上传回调注销
            UploadSubjectControl.getInstance().setUploadCallback(null);
            this.finish();
            Intent circle = new Intent(this, CircleHome.class);
            circle.putExtra("cid", newCid);
            startActivity(circle);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.circle_share:
                XHClick.mapStat(this, "a_share400", "生活圈", "圈子首页");
                XHClick.mapStat(this, "a_invitation", "美食圈邀请", "");
                XHClick.mapStat(this, "a_quan_zi_index", "分享", "");
                barShare = new BarShare(this, "圈子首页", "生活圈");
                String clickUrl = StringManager.api_circleShare + "/" + code;
                barShare.setShare(ShareTools.IMG_TYPE_WEB, name, desc, img, clickUrl);
                barShare.openShare();
                break;
            case R.id.back:
                System.out.println("back");
                XHClick.mapStat(this, "a_quan_zi_index", "返回", "");
                this.onBackPressed();
                break;
            case R.id.title:
                System.out.println("title");
                refreshCurrentFragment();
                break;
            default:
                break;
        }
    }

    private void refreshCurrentFragment() {
        int current = mViewPager.getCurrentItem();
        //调用页面的刷新方法
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for(Fragment fragment:fragments){
            if (fragment instanceof CircleFragment) {
                CircleFragment circleMainFragment = (CircleFragment) fragment;
                if(circleMainFragment.getmPlateData()!=null&&circleMainFragment.getmPlateData().getPosition() == current){
                    circleMainFragment.returnListTop();
                    circleMainFragment.refresh();
                }
            }
        }
    }

    @Override
    public void finish() {
        // 界面销毁时将上传回调注销
        UploadSubjectControl.getInstance().setUploadCallback(null);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
}
