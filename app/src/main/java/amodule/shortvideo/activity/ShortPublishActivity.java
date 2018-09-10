package amodule.shortvideo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.xiangha.R;

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import amodule.article.db.UploadArticleData;
import amodule.article.db.UploadVideoSQLite;
import amodule.search.view.MultiTagView;
import amodule.shortvideo.tools.ShortVideoPublishBean;
import amodule.shortvideo.tools.ShortVideoPublishManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.location.LocationHelper;

/**
 * 视频发布页面
 */
public class ShortPublishActivity extends BaseActivity implements View.OnClickListener{

    private EditText edit_text;
    private ImageView video_cover,location_img,topic_img;
    private RelativeLayout publish_layout;
    private TextView location_tv,topic_tv;
    private MultiTagView hot_table;
    private String videoPath,imgPath,otherPath;
    private String location_state= "1";//定位状态 1-正在定位，2-定位成功，3-定位失败
    private boolean isShowLocation= true;//是否显示定位信息
    private ArrayList<Map<String,String>> topicList = new ArrayList<>();
    private ShortVideoPublishBean shortVideoPublishBean= new ShortVideoPublishBean();
    private String extraDataJson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("", 2, 0, 0, R.layout.a_short_video_publish);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            videoPath = (String) bundle.get("videoPath");
            imgPath = (String) bundle.get("imgPath");
            otherPath = (String) bundle.get("otherPath");
            extraDataJson = (String) bundle.get("extraDataJson");
        }
        handleExtraData();
        initView();
        initData();
    }
    private void handleExtraData(){
        if((TextUtils.isEmpty(videoPath)||TextUtils.isEmpty(imgPath))&&TextUtils.isEmpty(extraDataJson)){
            this.finish();
            return;
        }
        if(!TextUtils.isEmpty(videoPath)){
            shortVideoPublishBean.setVideoPath(videoPath);
        }
        if(!TextUtils.isEmpty(imgPath)){
            shortVideoPublishBean.setImagePath(imgPath);
        }
        if(!TextUtils.isEmpty(otherPath)){

        }
        if(!TextUtils.isEmpty(extraDataJson)){
            shortVideoPublishBean.jsonToBean(extraDataJson);
        }
    }

    private void initView() {
        ((TextView)findViewById(R.id.title)).setText("发布");
        findViewById(R.id.rightImgBtn2).setVisibility(View.GONE);
        findViewById(R.id.rightImgBtn4).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.rightText)).setText("存草稿");
        findViewById(R.id.rightText).setVisibility(View.VISIBLE);
        findViewById(R.id.rightText).setOnClickListener(this);
        edit_text=findViewById(R.id.edit_text);
        video_cover=findViewById(R.id.video_cover);
        location_tv=findViewById(R.id.location_tv);
        location_img=findViewById(R.id.location_img);
        topic_img=findViewById(R.id.topic_img);
        topic_tv=findViewById(R.id.topic_tv);
        publish_layout=findViewById(R.id.publish_layout);
        publish_layout.setOnClickListener(this);
        hot_table=findViewById(R.id.hot_table);
        findViewById(R.id.topic_more_linear).setOnClickListener(this);
        findViewById(R.id.topic_back).setOnClickListener(this);
        findViewById(R.id.topic_delete).setOnClickListener(this);
        findViewById(R.id.video_duration_layout).setOnClickListener(this);
        findViewById(R.id.video_cover).setOnClickListener(this);
        findViewById(R.id.location_tv).setOnClickListener(this);
    }
    private void initData() {
        initUIData();
        handleLocation();
        getTopicData();
    }

    public void initUIData(){
        if(!TextUtils.isEmpty(shortVideoPublishBean.getName())){
            edit_text.setText(shortVideoPublishBean.getName());
        }
    }

    /**
     * 处理定位信息
     */
    private void handleLocation(){
        locationMap = new LinkedHashMap<String, String>();
        LocationHelper.getInstance().registerLocationListener(locationCallBack);
        location_state="1";
        handleLocationMsg("");
    }

    /**
     * 获取话题数据
     */
    public void getTopicData(){
        requestTopic();
        hot_table.setlineNum(3);
        hot_table.setSelectState(true);

    }
    /**
     * 请求话题数据
     */
    private void requestTopic(){
        String url = StringManager.API_SHORTVIDEO_TOPICRECOM;
        String params = "source=1";
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if(flag>= ReqInternet.REQ_OK_STRING){
                    ArrayList<Map<String,String>>  mapArrayList = StringManager.getListMapByJson(msg);
                    int size= mapArrayList.size();
                    if(!mapArrayList.isEmpty()){
                        for(int i=0;i<size;i++){
                            Map<String,String> map=mapArrayList.get(i);
                            map.put("hot",mapArrayList.get(i).get("name"));
                            topicList.add(map);
                        }
                        hot_table.addTags(topicList, new MultiTagView.MutilTagViewCallBack() {
                            @Override
                            public void onClick(int tagIndexr) {
                                topic_tv.setText(topicList.get(tagIndexr).get("name"));
                            }
                        });
                    }

                }
            }
        });
    }

    /**
     * 校验数据
     */
    private boolean checkData(){
        return false;
    }
    /**
     * 保存数据---存储草稿
     */
    private void saveData(){
        if(checkData()){return;}
        String title= edit_text.getText().toString();
        shortVideoPublishBean.setName(title);
        UploadArticleData uploadArticleData = new UploadArticleData();
        uploadArticleData.setTitle(title);
        uploadArticleData.setImg(imgPath);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(videoPath);
        uploadArticleData.setVideos(jsonArray.toString());
        uploadArticleData.setExtraDataJson(shortVideoPublishBean.toJsonString());
        UploadVideoSQLite uploadVideoSQLite = new UploadVideoSQLite(this);
        int id=uploadVideoSQLite.insert(uploadArticleData);
        shortVideoPublishBean.setId(String.valueOf(id));
    }

    /**
     * 开始发布
     */
    public void startPublish(){
        ShortVideoPublishManager.getInstance().setShortVideoPublishBean(shortVideoPublishBean);
        ShortVideoPublishManager.getInstance().startUpload();
    }

    /**
     * 删除选中的话题
     */
    private void deleteSelectTopic(){

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.publish_layout://发布
                startPublish();
                break;
            case R.id.rightText://草稿
                saveData();
                break;
            case R.id.topic_more_linear://更多话题
                break;
            case R.id.topic_delete://删除话题
                break;
            case R.id.video_duration_layout://预览
            case R.id.video_cover:
                break;
            case R.id.location_tv://定位
                break;
            default:
                break;
        }
    }

//***************************定位start*******************************************
    private LinkedHashMap<String,String> locationMap;
    public void unregisterLocationListener() {
        LocationHelper.getInstance().unregisterLocationListener(locationCallBack);
    }
    private LocationHelper.LocationListener locationCallBack = new LocationHelper.LocationListener() {
        @Override
        public void onSuccess(AMapLocation value) {
            unregisterLocationListener();
            locationMap.put("country", value.getCountry());
            locationMap.put("countryCode", "");
            locationMap.put("province", value.getProvince());
            locationMap.put("city", value.getCity());
            locationMap.put("district", value.getDistrict());
            locationMap.put("lat", "" + value.getLatitude());
            locationMap.put("lng", "" + value.getLongitude());
            String showText;
            if(value.getProvince().equals(value.getCity())){
                showText = value.getCity() + " " + value.getDistrict();
            }else{
                showText = value.getProvince() + " " + value.getCity();
            }
            location_state="2";
            handleLocationMsg(showText);
        }

        @Override
        public void onFailed() {
            unregisterLocationListener();
            location_state = "3";
            handleLocationMsg("定位失败");
        }
    };
    /**
     * 显示请求字符串
     * @param str
     */
    private void handleLocationMsg(String str) {
        try {
            if(!isShowLocation){//当前使用
                location_img.setSelected(false);
                location_tv.setTextColor(Color.parseColor("#999999"));
                location_tv.setText("不显示我的定位");
                return;
            }
            if("2".equals(location_state)) {//定位成功
                if (str == null || str.equals("") || str.equals("null")) {
                    location_img.setSelected(false);
                    location_tv.setTextColor(Color.parseColor("#999999"));
                    location_tv.setText("定位失败");
                    location_state = "3";
                }else{
                    location_img.setSelected(true);
                    location_tv.setTextColor(Color.parseColor("#3e3e3e"));
                    location_tv.setText(str);
                }
            }else if("3".equals(location_state)){//定位失败
                location_img.setSelected(false);
                location_tv.setTextColor(Color.parseColor("#999999"));
                location_tv.setText("定位失败");
            }else if("1".equals(location_state)){
                location_img.setSelected(false);
                location_tv.setTextColor(Color.parseColor("#999999"));
                location_tv.setText("正在定位");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //***************************定位start*******************************************
}
