package aplug.recordervideo.activity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bartoszlipinski.recyclerviewheader.RecyclerViewHeader;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import aplug.recordervideo.adapter.AdapterChooseVideo;
import aplug.recordervideo.db.RecorderVideoData;
import aplug.recordervideo.db.RecorderVideoSqlite;
import aplug.recordervideo.tools.FileToolsCammer;
import aplug.recordervideo.view.RecorderVideoPreviewView;
import aplug.shortvideo.view.VideoPreviewView;

import static aplug.recordervideo.db.RecorderVideoSqlite.getInstans;

/**
 * Created by XiangHa on 2016/10/14.
 */
public class ChooseVideoActivity extends BaseActivity implements View.OnClickListener{
    public static final int DATA_LOAD_OVER = 1;
    public static final int DATA_REFRASH = 2;

    public static final String EXTRA_RUSULT_POSITION = "position";
    public static final String EXTRA_RUSULT_TIME = "rusultTime";
    public static final String EXTRA_RUSULT_Path = "rusultPath";
    public static final String EXTRA_RUSULT_IMG_Path = "imgPath";

    private RecyclerView mRecyclerView;
    private ArrayList<Map<String,String>> arrayList = new ArrayList<>();
    private AdapterChooseVideo adapter;

    private TextView mComplate,mRefresh;
    private boolean isDelete = false;
    private int position;
    private boolean isCanEdit = false,isLoading = false;

    public static boolean isRefrushData = false;

    private static final String tongjiId = "a_dishvideo_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //防止页面黑一下
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        initActivity("",2,0,0,R.layout.a_recorder_video_choose);
        position = getIntent().getIntExtra("postion",-1);
        isCanEdit = getIntent().getBooleanExtra("isCanEdit",false);
        initTitle();
        init();
    }

    private void initTitle() {
        if(Tools.isShowTitle()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if(Tools.isShowTitle()) {
            int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
            int height = dp_45 + Tools.getStatusBarHeight(this);

            RelativeLayout bar_title = (RelativeLayout)findViewById(R.id.title_all_rela);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
        }
    }
    private void init(){
        findViewById(R.id.back).setOnClickListener(this);
        // 设置加载
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentLoad(false,isRefrushData);
            }
        });
        mRefresh = (TextView) findViewById(R.id.refresh);
        mComplate = (TextView) findViewById(R.id.complate);
        mRefresh.setOnClickListener(this);
        mComplate.setOnClickListener(this);
        mRecyclerView = (RecyclerView)findViewById(R.id.a_video_choose_rcv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        RecyclerViewHeader recyclerViewHeader = (RecyclerViewHeader) findViewById(R.id.a_video_choose_rcv_head);
        recyclerViewHeader.attachTo(mRecyclerView, true);

        adapter = new AdapterChooseVideo(this, arrayList,!isCanEdit);
        if(isCanEdit){
            mComplate.setVisibility(View.VISIBLE);
            findViewById(R.id.dish_video_choose_hint).setVisibility(View.GONE);
        }else{
            mRefresh.setVisibility(View.VISIBLE);
            adapter.setOnChooseListener(new AdapterChooseVideo.OnChooseAdaperListener() {
                @Override
                public void onClick(int index,String longTime) {
                    float time = Float.parseFloat(longTime);
                    if(time < 3){
                        Toast.makeText(ChooseVideoActivity.this,"支持选取最短3秒",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(EXTRA_RUSULT_POSITION,position);
                    bundle.putString(EXTRA_RUSULT_TIME,longTime);
                    bundle.putString(EXTRA_RUSULT_Path,arrayList.get(index).get(RecorderVideoData.video_path));
                    bundle.putString(EXTRA_RUSULT_IMG_Path,arrayList.get(index).get(RecorderVideoData.video_img_path));
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    ChooseVideoActivity.this.finish();
                }
            });
        }
        adapter.setOnSelectListener(new AdapterChooseVideo.OnSelectAdaperListener() {
            @Override
            public void onSelect(int position) {
                Map<String,String> map = arrayList.get(position);
                map.put(RecorderVideoData.video_state,RecorderVideoPreviewView.SELECTED);
                setStatus(position);
            }
        });
        adapter.setOnDeleteListener(new AdapterChooseVideo.OnDeleteListener() {
            @Override
            public void onDelete(final int position, final String videoPath) {
                final DialogManager dialogManager = new DialogManager(ChooseVideoActivity.this);
                dialogManager.createDialog(new ViewManager(dialogManager)
                        .setView(new TitleView(ChooseVideoActivity.this).setText("确定删除选中视频吗？"))
                        .setView(new MessageView(ChooseVideoActivity.this).setText("手机相册中的视频将会同时被删除"))
                        .setView(new HButtonView(ChooseVideoActivity.this)
                                .setNegativeText("取消", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogManager.cancel();
                                        XHClick.mapStat(ChooseVideoActivity.this, tongjiId,"删除","取消删除");
                                    }
                                })
                                .setPositiveText("删除", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogManager.cancel();
                                        XHClick.mapStat(ChooseVideoActivity.this, tongjiId,"删除","确定删除");
                                        if(isDelete){
                                            String id = arrayList.get(position).get(RecorderVideoData.video_id);
                                            RecorderVideoSqlite.getInstans().deleteById(id);
                                            handler.sendEmptyMessage(DATA_REFRASH);
                                        }else{
                                            Toast.makeText(ChooseVideoActivity.this,"删除失败！",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }))).show();
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DATA_LOAD_OVER:
                    //刷新数据
                    loadManager.hideProgressBar();
                    adapter.notifyDataSetChanged();
                    //为了解决尽量黑屏问题
                    mRecyclerView.scrollBy(0,1);
                    if(arrayList.size() == 0){
                        findViewById(R.id.ecorder_video_choose_data_layout).setVisibility(View.GONE);
                        findViewById(R.id.dish_video_choose_hint).setVisibility(View.GONE);
                        findViewById(R.id.recorder_video_choose_hint_layout).setVisibility(View.VISIBLE);
                    }else{
                        findViewById(R.id.ecorder_video_choose_data_layout).setVisibility(View.VISIBLE);
                        if(!isCanEdit) findViewById(R.id.dish_video_choose_hint).setVisibility(View.VISIBLE);
                        findViewById(R.id.recorder_video_choose_hint_layout).setVisibility(View.GONE);
                    }
                    break;
                case DATA_REFRASH:
                    arrayList.clear();
                    contentLoad(true,true);
                    break;
            }
        }
    };

    private void contentLoad(final boolean isDelete,boolean isReload){
        isLoading = true;
        if(isReload)isRefrushData = false;
        loadManager.showProgressBar();
        if(!isReload && getInstans().getDataSize() == 0) isReload = true;
        FileToolsCammer.loadCammerAllData(new FileToolsCammer.OnCammerFileListener() {
            @Override
            public void loadOver(ArrayList<Map<String, String>> orderArrayLis) {
                arrayList.addAll(orderArrayLis);
                handler.sendEmptyMessage(DATA_LOAD_OVER);
                isLoading = false;
            }
        },isDelete,isReload);
    }

    /**
     * @param position 不改变的index
     */
    public void setStatus(final int position){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int length = arrayList.size();
                for (int index = 0; index < length; index++) {
                    if(index != position){
                        Map<String,String> map = arrayList.get(index);
                        map.put(RecorderVideoData.video_state,RecorderVideoPreviewView.UNSELECTED);
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
        if(flag)findViewById(R.id.back).setVisibility(View.GONE);
        else findViewById(R.id.back).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int length = arrayList.size();
                Map<String,String> map;
                for (int index = 0; index < length; index++) {
                    map = arrayList.get(index);
                    map.put(RecorderVideoData.video_state,VideoPreviewView.DEFAULT);
                    map.put(RecorderVideoData.video_isDelete,String.valueOf(flag));
                }
                handler.sendEmptyMessage(DATA_LOAD_OVER);
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if(isDelete){
            isDelete = !isDelete;
            isDelete(isDelete);
        }else{
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                XHClick.mapStat(this, tongjiId,"拍摄按钮","");
                onBackPressed();
                break;
            case R.id.refresh:
                if(isLoading) return;
                XHClick.mapStat(this, tongjiId,"刷新","");
                arrayList.clear();
                contentLoad(false,true);
                break;
            case R.id.complate:
                if(isLoading) return;
                XHClick.mapStat(this, tongjiId,"编辑","");
                isDelete = !isDelete;
                isDelete(isDelete);
                break;
        }
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new ContextWrapper(newBase)
        {
            @Override
            public Object getSystemService(String name)
            {
                if (Context.AUDIO_SERVICE.equals(name))
                    return getApplicationContext().getSystemService(name);

                return super.getSystemService(name);
            }
        });
    }
}
