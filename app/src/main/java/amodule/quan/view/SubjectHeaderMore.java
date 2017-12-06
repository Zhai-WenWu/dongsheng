package amodule.quan.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.StringManager;
import amodule.dish.activity.DetailDish;
import amodule.quan.activity.ShowSubject;
import amodule.search.avtivity.HomeSearch;
import third.mall.tool.ToolView;

/**
 * PackageName : amodule.quan.view
 * Created by MrTrying on 2016/9/28 20:01.
 * E_mail : ztanzeyu@gmail.com
 */

public class SubjectHeaderMore extends RelativeLayout implements View.OnClickListener{
    private TextView moreText;
    String title="";
    String dishCode = "";
    String dishName = "";
    private String mClickNum;
    private String mFavNum;
    private String mInfo;
    private String mImg;
    private String mHasVideo;

    private Map<String, String> mFloorsInfo;
    private Map<String, String> mSubjectIfo;
    public SubjectHeaderMore(Context context) {
        this(context,null);
    }

    public SubjectHeaderMore(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SubjectHeaderMore(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.subject_header_more,this);
        moreText = (TextView) findViewById(R.id.head_subject_more);
        setOnClickListener(this);
    }

    /**
     * 设置数据
     * @param title
     * @param type
     */
    public void setData(String title,String type){
        this.title = title;
        //设置类型
        setType(type);
    }

    public void setDishInfo(Map<String, String> subjectInfo){
        mSubjectIfo = subjectInfo;
        Map<String, String> map = StringManager.getFirstMap(subjectInfo.get("dish"));
        this.dishCode =map.get("code");
        this.dishName = map.get("name");
        this.mClickNum = map.get("clickNum");
        this.mFavNum = map.get("favorites");
        this.mInfo = map.get("info");
        //设置text
        String des_title = handlerTitle(dishName);
        if (!TextUtils.isEmpty(des_title))
            moreText.setText("查看" + des_title + "的做法>>");
        else
            setVisibility(View.GONE);
    }

    public void setFloorInfo (Map<String, String> floorsInfo) {
        mFloorsInfo = floorsInfo;
        ArrayList<Map<String, String>> contents = StringManager.getListMapByJson(floorsInfo.get("content"));
        if (contents.isEmpty())
            return;
        for (Map<String, String> content : contents) {
            String img = content.get("img");
            if (TextUtils.isEmpty(img))
                continue;
            else {
                mImg = img;
                mHasVideo = content.get("hasVideo");
                break;
            }
        }
    }

    /**
     * 处理不同的type的类型
     *
     * @param type
     */
    private void setType(String type) {
        setVisibility(View.GONE);
        if ("3".equals(type) || "5".equals(type)) {//菜谱贴子
            setVisibility(View.VISIBLE);
        } else if ("2".equals(type)) {
            setVisibility(View.VISIBLE);
        }
    }

    /**
     * 处理title
     * @param title
     * @return
     */
    private String handlerTitle(String title) {
        int num = getTextNum();
        if (!TextUtils.isEmpty(title)) {
            if (title.length() > num - 6)
                return title.substring(0, num - 6) + "...";
            else return title;
        } else return null;
    }

    /**
     * 获取当前字数
     *
     * @return
     */
    private int getTextNum() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_16);
        int distance = (int) this.getResources().getDimension(R.dimen.dp_15);

        int waith = wm.getDefaultDisplay().getWidth();
        int tv_waith = waith - distance * 4;
        int tv_pad = ToolView.dip2px(getContext(), 1.0f);
        int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);
        return num;
    }

    public void setMoreTextClick(OnClickListener listener){
        if(moreText != null && listener != null){
            moreText.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
        XHClick.mapStat(getContext(), BarSubjectFloorOwnerNew.tongjiId, "搜索链接点击量", "");
        Intent intent;
        if (dishCode != null) {
            intent = new Intent(getContext(), DetailDish.class);
            if(TextUtils.isEmpty(dishCode)
                    || TextUtils.isEmpty(dishName)){
                return;
            }
            intent.putExtra("code", dishCode);
            intent.putExtra("name", dishName);
            intent.putExtra("img", mImg);
            intent.putExtra("dishInfo", getDishInfo());
        } else {
            intent = new Intent(getContext(), HomeSearch.class);
            intent.putExtra("from", "美食贴更多");
            intent.putExtra("s", title);
            intent.putExtra("type", "4".equals(ShowSubject.types) ? "zhishi" : "caipu");
        }
        getContext().startActivity(intent);
    }

    private String getDishInfo() {
        try{
            JSONObject dishInfoJson = new JSONObject();
            dishInfoJson.put("code",TextUtils.isEmpty(dishCode) ? "" : dishCode);
            dishInfoJson.put("name",TextUtils.isEmpty(dishName) ? "" : dishName);
            dishInfoJson.put("img",mImg);
            dishInfoJson.put("type",TextUtils.equals(mHasVideo, "2") ? "2" : "1");
            dishInfoJson.put("favorites",TextUtils.isEmpty(mFavNum) ? "" : mFavNum);
            dishInfoJson.put("allClick",TextUtils.isEmpty(mClickNum) ? "" : mClickNum);
            dishInfoJson.put("info", TextUtils.isEmpty(mInfo) ? "" : mInfo);
            JSONObject customerJson = new JSONObject();
            Map<String,String> userInfo = StringManager.getFirstMap(mFloorsInfo.get("customer"));
            customerJson.put("customerCode",userInfo.get("code"));
            customerJson.put("nickName",userInfo.get("nickName"));
            customerJson.put("info",TextUtils.isEmpty(userInfo.get("info")) ? "" : userInfo.get("info"));
            customerJson.put("img",userInfo.get("imgShow"));
            dishInfoJson.put("customer",customerJson);
            return Uri.encode(dishInfoJson.toString());
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
