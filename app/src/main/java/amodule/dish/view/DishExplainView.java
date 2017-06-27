package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.view.ItemBaseView;
import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;
import amodule.user.activity.login.LoginByAccout;
import aplug.feedback.activity.Feedback;


/**
 * 小贴士
 */

public class DishExplainView extends ItemBaseView {
    public static String DISH_STYLE_EXP="dish_style_EXP";
    public static int  DISH_STYLE_EXP_INDEX=2;
    private Map<String,String> mapData;
    private Activity activity;
    public DishExplainView(Context context) {
        super(context, R.layout.view_dish_explain);
    }

    public DishExplainView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_explain);
    }

    public DishExplainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_explain);
    }

    @Override
    public void init() {
        super.init();
    }

    public void setData(final Map<String,String> maps, Activity activitys){
        this.activity=activitys;
        this.mapData= maps;
        TextView explain_content_tv= (TextView) findViewById(R.id.explain_content_tv);
        if(maps.containsKey("remark")&& !TextUtils.isEmpty(maps.get("remark"))){
            explain_content_tv.setText(maps.get("remark"));
            findViewById(R.id.tv_explain).setVisibility(View.VISIBLE);
            explain_content_tv.setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.tv_explain).setVisibility(View.GONE);
            explain_content_tv.setVisibility(View.GONE);
        }
        TextView subjectfloorNum_tv= (TextView) findViewById(R.id.subjectfloorNum_tv);
        if(maps.containsKey("commentNum")&&!TextUtils.isEmpty(maps.get("commentNum"))&&Integer.parseInt(maps.get("commentNum"))>0){
            subjectfloorNum_tv.setText("去评论("+maps.get("commentNum")+"条)");
        }else subjectfloorNum_tv.setText("去评论");

        //举报
        findViewById(R.id.explain_report).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(activity, DetailDish.tongjiId, "菜谱区域的点击", "举报这道菜普");
                Intent intent = new Intent(activity, Feedback.class);
                intent.putExtra("feekUrl","http://www.xiangha.com/caipu/"+maps.get("code")+".html");
                activity.startActivity(intent);
            }
        });
        //评论
        findViewById(R.id.subjectfloorNum_tv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(activity, DetailDish.tongjiId, "菜谱区域的点击", "去评论的按钮");
               String url= "subjectInfo.app?code=" + mapData.get("subjectCode") + "&title=" + mapData.get("name")+"&isReplayFloorOwner=true";
                AppCommon.openUrl(activity,url,true);
            }
        });
        //晒菜
        findViewById(R.id.gouploadsubject_tv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(activity, DetailDish.tongjiId, "菜谱区域的点击", "晒我做的这道菜的按钮");
                doUpload();

            }
        });

    }
    // 上传我的做法
    public void doUpload() {
        // 若用户还没登陆则先登陆
        if (!LoginManager.isLogin()) {
            Tools.showToast(context, "请先登录");
            Intent intent = new Intent(context, LoginByAccout.class);
            context.startActivity(intent);
            return;
        }
        String cid = "";
        String json = FileManager.readFile(FileManager.getDataDir() + FileManager.file_indexModuleAndRecCircle);
        ArrayList<Map<String, String>> data = StringManager.getListMapByJson(json);
        if (data.size() > 0) {
            String circleName = data.get(0).get("dish2quan");
            CircleSqlite sqlite = new CircleSqlite(context);
            CircleData circleData = sqlite.select(CircleSqlite.CircleDB.db_name, circleName);
            cid = circleData.getCid();
        }
        Intent intent = new Intent();
        if (!TextUtils.isEmpty(cid)) {
            intent.putExtra("cid", cid);
        }
        intent.putExtra("title", mapData.get("name"));
        intent.putExtra("dishCode", mapData.get("code"));
        intent.putExtra("subjectCode", mapData.get("subjectCode"));
        intent.putExtra("skip", true);
        intent.setClass(context, UploadSubjectNew.class);
//        context.startActivity(intent);
        activity.startActivityForResult(intent,10000);
    }
}
