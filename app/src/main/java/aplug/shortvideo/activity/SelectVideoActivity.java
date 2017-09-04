package aplug.shortvideo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import acore.widget.GridViewWithHeaderAndFooter;
import aplug.shortvideo.ShortVideoInit;
import aplug.shortvideo.adapter.SelectVideoAdapter;
import aplug.shortvideo.view.VideoPreviewView;

/**
 * PackageName : aplug.shortvideo.activity.record
 * Created by MrTrying on 2016/9/21 20:02.
 * E_mail : ztanzeyu@gmail.com
 */
public class SelectVideoActivity extends BaseActivity implements View.OnClickListener {
    public static final int DATA_LOAD_OVER = 1;
    public static final String EXTRAS_CAN_EDIT = "can_edit";
    public static final String EXTRAS_VIDEO_PATH = "video_path";
    public static final String EXTRAS_IMAGE_PATH = "image_path";
    public static final String SUFFIX_IMAGE = ".jpg";
    public static final String SUFFIX_VIDEO = ".mp4";
    public static final String SUFFIX_VIDEO_2 = ".MP4";
    /** 表格 */
    private GridViewWithHeaderAndFooter gridView;

    private TextView mComplate;

    private SelectVideoAdapter mAdapter;

    private List<Map<String, String>> mData = new ArrayList<>();

    private boolean isDelete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.short_recorder_select_activity);
        //初始化状态栏
        initTitles();
        //初始化UI
        initView();

        initData();
    }

    private void initData() {
        mAdapter = new SelectVideoAdapter(this, mData);
        mAdapter.setmOnSelectListener(new VideoPreviewView.OnSelectListener() {
            @Override
            public void onSelect(int position,View view, Map<String, String> data) {
                XHClick.mapStat(SelectVideoActivity.this,"a_select_shortvideo","预览","");
                data.put(VideoPreviewView.STATUS,VideoPreviewView.SELECTED);
                setStatus(position,VideoPreviewView.UNSELECTED);
            }
        });
        mAdapter.setmOnReselectListener(new VideoPreviewView.OnReselectListener() {
            @Override
            public void onReselect(int position, View view, Map<String, String> data) {
                XHClick.mapStat(SelectVideoActivity.this,"a_select_shortvideo","选择","");
                Intent intent = new Intent();
                intent.putExtra(EXTRAS_VIDEO_PATH,data.get(VideoPreviewView.VIDEO_PATH));
                intent.putExtra(EXTRAS_IMAGE_PATH,data.get(VideoPreviewView.IMAGE_PATH));
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        mAdapter.setmOnUnselectListener(new VideoPreviewView.OnUnselectListener() {
            @Override
            public void onUnselect(int position, View view, Map<String, String> data) {

            }
        });
        mAdapter.setmOnDeleteListener(new VideoPreviewView.OnDeleteListener() {
            @Override
            public void onDelete(int position, View view, Map<String, String> data) {
                XHClick.mapStat(SelectVideoActivity.this,"a_select_shortvideo","删除","");
                Log.i("zhangyujian","::::"+data.get(VideoPreviewView.VIDEO_PATH));
//                MediaHandleControl.deleteFile(data.get(VideoPreviewView.VIDEO_PATH));
                mData.remove(data);
                handler.sendEmptyMessage(DATA_LOAD_OVER);
            }
        });

        View footer = LayoutInflater.from(this).inflate(R.layout.short_video_footer, null);
        gridView.addFooterView(footer);
        gridView.addHeaderView(new View(this));
        gridView.setAdapter(mAdapter);

        //读取本地文件，加载数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                File cacheFile = new File(ShortVideoInit.path_short);
                File[] files = cacheFile.listFiles();
                for (File path : files) {
                    if (path.isDirectory()) {
                        File[] dirAboutList = path.listFiles();
                        for (File dir : dirAboutList) {
                            if(dir.listFiles().length == 0){
//                                FileUtils.deleteDir(dir);
                                continue;
                            }
                            String imagePath = "";
                            String videoPath = "";
                            for (File file : dir.listFiles()) {
                                if (file.getName().endsWith(SUFFIX_IMAGE)) {
                                    imagePath = file.getAbsolutePath();
                                } else if (file.getName().endsWith(SUFFIX_VIDEO) || file.getName().endsWith(SUFFIX_VIDEO_2)) {
                                    videoPath = file.getAbsolutePath();
                                }
                            }
                            if (!TextUtils.isEmpty(imagePath)
                                    && !TextUtils.isEmpty(videoPath)
                                    && (imagePath.replace(SUFFIX_IMAGE, "").equals(videoPath.replace(SUFFIX_VIDEO, ""))
                                        || imagePath.replace(SUFFIX_IMAGE, "").equals(videoPath.replace(SUFFIX_VIDEO_2, "")))) {
                                Map<String, String> map = new HashMap<>();
                                map.put(VideoPreviewView.IMAGE_PATH, imagePath);
                                map.put(VideoPreviewView.VIDEO_PATH, videoPath);
                                map.put(VideoPreviewView.STATUS, VideoPreviewView.DEFAULT);
                                map.put(VideoPreviewView.IS_DELETE, "false");
                                mData.add(0, map);
                            }
                        }
                    }
                }
                handler.sendEmptyMessage(DATA_LOAD_OVER);
            }
        }).start();


    }

    /**初始化title*/
    private void initTitles() {
        if (Tools.isShowTitle()) {
            int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
            int height = dp_45 + Tools.getStatusBarHeight(this);

            RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
        }
    }

    /**初始化UI*/
    private void initView() {
        gridView = (GridViewWithHeaderAndFooter) findViewById(R.id.gridView);
        mComplate = (TextView) findViewById(R.id.complate);

        mComplate.setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);

        Intent intent = getIntent();
        boolean canEdit = intent.getBooleanExtra(EXTRAS_CAN_EDIT,true);
        mComplate.setVisibility(canEdit?View.VISIBLE:View.GONE);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DATA_LOAD_OVER:
                    //刷新数据
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    /**
     * @param position 不改变的index
     * @param status 需要重置的状态
     */
    public void setStatus(final int position,final String status){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int length = mData.size();
                for (int index = 0; index < length; index++) {
                    if(index != position){
                        mData.get(index).put(VideoPreviewView.STATUS, status);
                    }
                }
                handler.sendEmptyMessage(DATA_LOAD_OVER);
            }
        }).start();
    }

    /**
     * 编辑切换
     * @param flag
     */
    public void isDelete(final boolean flag) {
        mComplate.setText(flag ? "完成" : "编辑");
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int length = mData.size();
                for (int index = 0; index < length; index++) {
                    mData.get(index).put(VideoPreviewView.STATUS, VideoPreviewView.DEFAULT);
                    mData.get(index).put(VideoPreviewView.IS_DELETE, String.valueOf(flag));
                }
                handler.sendEmptyMessage(DATA_LOAD_OVER);
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.back:
                XHClick.mapStat(SelectVideoActivity.this,"a_select_shortvideo","返回","");
                onBackPressed();
                break;
            case R.id.complate:
                XHClick.mapStat(SelectVideoActivity.this,"a_select_shortvideo","编辑","");
                isDelete = !isDelete;
                isDelete(isDelete);
                break;
        }
    }
}
