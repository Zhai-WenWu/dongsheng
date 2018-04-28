package amodule.dish.video.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.upload.UploadDishMakeOptionActivity;
import amodule.dish.db.UploadDishData;
import amodule.dish.db.UploadDishSqlite;
import amodule.dish.video.View.MediaPaperItemViewNew;
import amodule.dish.video.View.RangeSeekBarView;
import amodule.dish.video.bean.MediaPaperBean;
import xh.basic.tool.UtilString;

/**
 * 视频截取页面
 */
public class MediaPaperActivity extends BaseActivity implements View.OnClickListener{
    private ViewPager viewpager;
    private ArrayList<MediaPaperItemViewNew> MediaPaperItemViewNews = new ArrayList<>();
    private int position = 0;
    private int select_position = 0;//当前选择的位置
    private TextView all_time,time_set;
    private String mediaJson;
    private ArrayList<MediaPaperBean> mediaPaperBeans=new ArrayList<>();
    private ArrayList<Map<String,String>> lists;
    private int id=-1;//当前草稿箱id
    private UploadDishSqlite sqlite;
    private TextView pager_time,pager_index_1,pager_index_2;
    private int media_waith=0;
    private int media_height=0;
    private RangeSeekBarView rangeSeekBarView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            position = bundle.getInt("position", 0);
            mediaJson= bundle.getString("mediaJson");
            id= bundle.getInt("id",-1);
        }
        sqlite = new UploadDishSqlite(this);
       //YLKLog.i("zhangyujian","id::"+id);
        initView();
        initTitle();
        initData();
    }

    private void initTitle() {
        if (Tools.isShowTitle()) {
            int topbarHeight = Tools.getDimen(this, R.dimen.topbar_height);
            int height = topbarHeight + Tools.getStatusBarHeight(this);
            RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_all_rela);
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            bar_title.setLayoutParams(layout);
            bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
        }
    }
    private void initView() {
        initActivity("", 2, 0, 0, R.layout.a_media_paper);
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        findViewById(R.id.tv_finish).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        all_time= (TextView) findViewById(R.id.all_time);
        time_set= (TextView) findViewById(R.id.time_set);

        pager_time= (TextView) findViewById(R.id.pager_time);
        pager_index_1= (TextView) findViewById(R.id.pager_index_1);
        pager_index_2= (TextView) findViewById(R.id.pager_index_2);

        media_waith= ToolsDevice.getWindowPx(this).widthPixels- Tools.getDimen(this,R.dimen.dp_40);
        media_height= media_waith/16*9;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,media_height);
        layoutParams.addRule(RelativeLayout.BELOW,R.id.linear_time);
        viewpager.setLayoutParams(layoutParams);

        rangeSeekBarView= (RangeSeekBarView) findViewById(R.id.rangseekbarview);


    }

    /**
     * 处理数据来源，并对数据进行转化
     */
    private void initData() {
        select_position = position;
        try {
            JSONArray jsonArray= new JSONArray(mediaJson);
           //YLKLog.i("zhangyujian","length:::"+jsonArray.length());
            lists= UtilString.getListMapByJson(jsonArray);
            for(int i=0;i<lists.size();i++){
               //YLKLog.i("zhangyujian","temp:::"+lists.get(i).get("videoInfo"));
                String strTemp=lists.get(i).get("videoInfo");
                if(!TextUtils.isEmpty(strTemp)){
                    JSONObject jsonObject= new JSONObject(lists.get(i).get("videoInfo"));
                    MediaPaperBean bean= new MediaPaperBean();
                    bean.jsonToBean(jsonObject);
                    bean.setIndex(i);
                    mediaPaperBeans.add(bean);
                }
            }
            int size = mediaPaperBeans.size();
           //YLKLog.i("zhangyujian","size:::"+size);
            for (int i = 0; i < size; i++) {
               //YLKLog.i("time",mediaPaperBeans.get(i).toString());
                MediaPaperItemViewNew MediaPaperItemViewNew = new MediaPaperItemViewNew(this, mediaPaperBeans.get(i),i,size);
                MediaPaperItemViewNew.setChangeTimeCallBack(new MediaPaperItemViewNew.VideoTimeCallBack() {
                    @Override
                    public void changeVideoTime(float time) {
                        rangeSeekBarView.setProgressVideo(time);
                    }

                    @Override
                    public void changeVideoIndex(boolean state) {
                        if(state)viewpager.setCurrentItem(select_position+1);
                        else viewpager.setCurrentItem(select_position-1);
                    }
                });
                MediaPaperItemViewNews.add(MediaPaperItemViewNew);
            }
            viewpager.setAdapter(new MyViewPagerAdapter(MediaPaperItemViewNews));
            viewpager.setCurrentItem(select_position);

            setTimeData();
            setAllTime();
            setListener();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 监听
     */
    private void setListener() {
        viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if(select_position>position)XHClick.mapStat(MediaPaperActivity.this,"a_video_clip","下一个","");
                else XHClick.mapStat(MediaPaperActivity.this,"a_video_clip","上一个","");
                select_position = position;
                int size = MediaPaperItemViewNews.size();
                for (int i = 0; i < size; i++) {
                    if (position != i) {
                        MediaPaperItemViewNews.get(i).onPause();
                    } else {
                        MediaPaperItemViewNews.get(i).onResume();
                    }
                }

                rangeSeekBarView.setProgressVideo(0);
                setTimeData();
                saveSqliteData();

            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        rangeSeekBarView.setNumberCallBack(new RangeSeekBarView.NumberCallBack() {
            @Override
            public void getstartAndEndValue(float startValue, float endValue, boolean isTouchState, int position) {
                DecimalFormat df   = new DecimalFormat("######0.0");

                MediaPaperBean mediaBean=mediaPaperBeans.get(position);
                BigDecimal b_start  =   new BigDecimal(startValue);
                startValue   =  b_start.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();

                BigDecimal b_end  =   new BigDecimal(endValue);
                endValue   =  b_end.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
               //YLKLog.i("zhangyujian",":startValue:"+startValue+":::"+endValue);
                if(startValue!=mediaBean.getStartTime()||endValue!=mediaBean.getEndTime()){
                    mediaBean.setStartTime(startValue);
                    mediaBean.setEndTime(endValue);
                    pager_time.setText(String.valueOf(df.format(mediaBean.getCutTime()))+"秒");
                    setAllTime();
                    if(isTouchState){
                        MediaPaperItemViewNews.get(position).startVideo();
                    }
                }
            }
        });
    }

    /**
     * 处理时间数据和页面位置
     */
    private void setTimeData(){
        DecimalFormat    df   = new DecimalFormat("######0.0");
        MediaPaperBean bean= mediaPaperBeans.get(select_position);
        pager_time.setText(String.valueOf(df.format(bean.getCutTime()))+"秒");
        pager_index_1.setText(String.valueOf(select_position+1));
        pager_index_2.setText(String.valueOf("/"+mediaPaperBeans.size()));

        rangeSeekBarView.postDelayed(new Runnable() {
            @Override
            public void run() {
                rangeSeekBarView.setTimeParams(mediaPaperBeans.get(select_position).getAllTime(),select_position
                        ,mediaPaperBeans.get(select_position).getStartTime(),mediaPaperBeans.get(select_position).getEndTime());
            }
        },100);


    }
    @Override
    protected void onResume() {
        super.onResume();
        if(MediaPaperItemViewNews.size()>0&&MediaPaperItemViewNews.size()>select_position)
            MediaPaperItemViewNews.get(select_position).onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        int size = MediaPaperItemViewNews.size();
        for (int i = 0; i < size; i++) {
            MediaPaperItemViewNews.get(i).onPause();
        }
    }
    @Override
    protected void onDestroy() {
        int size = MediaPaperItemViewNews.size();
        for (int i = 0; i < size; i++) {
            MediaPaperItemViewNews.get(i).onDestory();
        }
        super.onDestroy();
    }

    /**
     * 对总时间进行处理
     */
    private void setAllTime(){
        int size = mediaPaperBeans.size();
        float num_index = 0;
        for (int i = 0; i < size; i++) {
            num_index+=mediaPaperBeans.get(i).getCutTime();
        }

        if(num_index<=30){
            time_set.setText("(不能小于30s)");
            time_set.setVisibility(View.VISIBLE);
        }else if(num_index>360){
            time_set.setText("(不能超过6分钟)");
            time_set.setVisibility(View.VISIBLE);
        }else{
            time_set.setVisibility(View.INVISIBLE);
        }
        int temp = (int) num_index;
        String str;
        if(temp<=60){
            if(temp>=10)str="00:"+temp;
            else str="00:0"+temp;
        }else{
            int num= temp/60;
            int num_s= temp%60;
            if(num>=10)str=num+":";
            else str="0"+num+":";
            if(num_s>=10)str+=String.valueOf(num_s);
            else str+="0"+num_s;
        }
        all_time.setText("步骤总时长:"+str);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_finish:
                XHClick.mapStat(MediaPaperActivity.this,"a_video_clip","完成","");
//                MediaControl.getInstance().startPaperMedia();
//                MediaPaperActivity.this.startActivity(new Intent(MediaPaperActivity.this,TestActivity.class));
//                MediaHandleContorl mediaHandleContorl=new MediaHandleContorl(MediaPaperActivity.this);
//                mediaHandleContorl.startVideo();
                findViewById(R.id.tv_finish).setFocusable(false);
                dataForResult();
                findViewById(R.id.tv_finish).setFocusable(true);
                this.finish();
                break;
            case R.id.back:
//                showDialog();
                saveSqliteData();
                dataForResult();
                MediaPaperActivity.this.finish();
//                selectByIdData();
                break;
        }
    }

    /**
     * setResult返回裁剪数据
     */
    private void dataForResult(){
        Intent intent = new Intent();
        intent.putExtra(UploadDishMakeOptionActivity.MAKE_ITEM_OPTION_DATA,toJsonArray().toString());
        int size = mediaPaperBeans.size();
        float num_index = 0;
        for (int i = 0; i < size; i++) {
            num_index+=mediaPaperBeans.get(i).getCutTime();
        }
        DecimalFormat df   = new DecimalFormat("######0.0");
        intent.putExtra("allTime",df.format(num_index));
        setResult(RESULT_OK,intent);
    }
    /**
     * 对数据封装成json
     * @return
     */
    private JSONArray toJsonArray(){
        try {
            JSONArray jsonArray= new JSONArray();
            int size = lists.size();
            for (int i=0;i<size;i++) {
                JSONObject jsonObjectAll= new JSONObject();
                jsonObjectAll.put("makesStep",lists.get(i).get("makesStep"));
               //YLKLog.i("time",lists.get(i).get("makesStep")+"：：：step位置");
                jsonObjectAll.put("makesInfo",lists.get(i).get("makesInfo"));
                jsonObjectAll.put("videoInfo","");
                int beans= mediaPaperBeans.size();
                for(int j=0;j<beans;j++){
                    if(i==mediaPaperBeans.get(j).getIndex()){
                       //YLKLog.i("time",mediaPaperBeans.get(j).getIndex()+"：：：位置");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject=mediaPaperBeans.get(j).beanToJson();
                        jsonObjectAll.put("videoInfo",jsonObject);
                    }
                }
                jsonArray.put(jsonObjectAll);
            }
            return jsonArray;
        }catch (Exception e){

        }
        return null;
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<MediaPaperItemViewNew> mListViews;

        public MyViewPagerAdapter(List<MediaPaperItemViewNew> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            MediaPaperItemViewNew MediaPaperItemViewNew = mListViews.get(position);
//            MediaPaperItemViewNew.onResume();
            container.addView(MediaPaperItemViewNew, 0);
            return mListViews.get(position);
        }
        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    /**
     * 保持数据到草稿箱
     */
    private void saveSqliteData(){
        if(id>-1){
            sqlite.update(id, UploadDishData.ds_makes, toJsonArray().toString());
        }
    }

    private void selectByIdData(){
       //YLKLog.i("zhangyujian","id::"+id);
        UploadDishData data=sqlite.selectById(id);
        data.getMakes();
       //YLKLog.i("zhangyujian","makes::"+data.getMakes());
    }
}
