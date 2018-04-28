package amodule.dish.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xiangha.R;
import java.util.Map;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;


/**
 * 临时停留页面
 */
public class DishTestActivity extends BaseAppCompatActivity{
    private String code,img;
    private Bundle bundle;
    public static long startTime=0;
    private RelativeLayout dishVidioLayout;
    private boolean isfinish=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 声明使用自定义标题
        setContentView(R.layout.a_dish_test);
        setCommonStyle();
        dishVidioLayout= (RelativeLayout) findViewById(R.id.video_layout);

        String colors = Tools.getColorStr(this, R.color.common_top_bg);
        Tools.setStatusBarColor(this, Color.parseColor(colors));
        //处理广告
        startTime= System.currentTimeMillis();
        bundle = getIntent().getExtras();
        code = bundle.getString("code");
        img = bundle.getString("img");
        loadManager.setLoading(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reqTopInfo();
            }
        });
//        AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1.0f);
//        //设置动画持续时长
//        alphaAnimation.setDuration(100);
//        //设置动画结束之后的状态是否是动画的最终状态，true，表示是保持动画结束时的最终状态
//        alphaAnimation.setFillAfter(true);
//        rl.startAnimation(alphaAnimation);
        if(!TextUtils.isEmpty(img))setImg(img);
    }
    /**
     * 请求topInfo数据---第一请求，有权限请求
     */
    public void reqTopInfo() {
        loadManager.showProgressBar();
        String params = "dishCode=" + code;
        ReqEncyptInternet.in().doEncypt(StringManager.API_GETDISHTYPE, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String url, Object object) {
                loadManager.hideProgressBar();
                Log.i("xianghaTag","isfinish::"+isfinish);
                if(isfinish){
                    return;
                }
                if(flag>= ReqInternet.REQ_OK_STRING){
                    Map<String,String> map= StringManager.getFirstMap(object);
                    if(!map.containsKey("type")|| TextUtils.isEmpty(map.get("type"))){
                        loadManager.showLoadFaildBar();
                        return;
                    }
                    Intent intent = new Intent();
                    if("1".equals(map.get("type"))){
                        intent.setClass(DishTestActivity.this,DetailDish.class);
                    }else if("2".equals(map.get("type"))){
                        intent.setClass(DishTestActivity.this,DetailDishWeb.class);
                    }
                    intent.putExtras(bundle);
                    DishTestActivity.this.startActivity(intent);
                    DishTestActivity.this.finish();
                }else loadManager.showLoadFaildBar();
                long endTime= System.currentTimeMillis();
                Log.i("xianghaTag","时间：："+(endTime-startTime));

            }
        });
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
        isfinish=true;
    }
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        // 设置切换动画，从右边进入，左边退出
//        overridePendingTransition(R.anim.in_from_right, 0);
        overridePendingTransition(0, 0);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        // 设置切换动画，从右边进入，左边退出
        overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
        super.finish();
        isfinish=true;
        overridePendingTransition(0, 0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        long endTime= System.currentTimeMillis();
        Log.i("xianghaTag","test::onWindowFocusChanged时间：："+(endTime-startTime));
    }
    /**
     * 展示顶图view,是大图还是视频
     * @param img          》图片链接
     */
    public void setImg(final String img) {
        Log.i("wyl","img:___:::"+img);
        int waith = ToolsDevice.getWindowPx(this).widthPixels *5/6;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams params_rela = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,waith);
        final ImageViewVideo imvv = new ImageViewVideo(this);
        imvv.parseItemImg(ImageView.ScaleType.CENTER_CROP, img, false, false, R.drawable.i_nopic, FileManager.save_cache);
        imvv.setLayoutParams(params);
        dishVidioLayout.removeAllViews();
        dishVidioLayout.setLayoutParams(params_rela);
        dishVidioLayout.addView(imvv);
    }
}
