package amodule.dish.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.TextViewShow;
import amodule.quan.activity.ShowSubject;
import amodule.user.activity.FriendHome;
import aplug.basic.LoadImage;
import xh.basic.tool.UtilString;

/**
 * 精彩评论标签
 */

public class CommentDishShow extends DishBaseView{
    private LinearLayout ll_pinglun;
    private final int viewUser = 1;
    private final int viewSubject = 2;
    private boolean isHasVideo= false;
    private String tongjiId="a_menu_detail_normal";
    public CommentDishShow(Context context) {
        super(context, R.layout.view_dish_header_commend);
    }

    public CommentDishShow(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_header_commend);
    }

    public CommentDishShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_header_commend);
    }

    @Override
    public void init() {
        super.init();
        ll_pinglun= (LinearLayout) findViewById(R.id.ll_pinglun);
    }

    /**
     * 设置数据
     * @param lists
     */
    public void setData(ArrayList<Map<String,String>> lists){
        if(lists.size() > 0){
            Map<String, String> map = lists.get(0);
            final String subjectCode = map.get("subjectCode");
            ArrayList<Map<String, String>> floorList = UtilString.getListMapByJson(map.get("floorList"));
            ll_pinglun.setVisibility(View.VISIBLE);
            findViewById(R.id.tv_pinglunHint).setVisibility(View.VISIBLE);
            LayoutInflater inflater =LayoutInflater.from(context);
            for(Map<String, String> mm : floorList){
                String customerNickName = mm.get("customerNickName");
                String customerCode = mm.get("customerCode");
                String customerImg = mm.get("customerImg");
                String content = mm.get("content");
                String addTime = mm.get("addTime");
                String num = mm.get("num");
                View v = inflater.inflate(R.layout.a_dish_detail_item_pinlun, null);
                ll_pinglun.addView(v);
                TextView tv_sub_user_name = (TextView)v.findViewById(R.id.tv_sub_user_name);
                final ImageView userHeard = (ImageView)v.findViewById(R.id.iv_sub_user_heard);
                TextViewShow tv_sub_content = (TextViewShow)v.findViewById(R.id.tv_sub_content);
                TextView tv_sub_timeShow = (TextView)v.findViewById(R.id.tv_sub_timeShow);
                TextView tv_sub_num = (TextView)v.findViewById(R.id.tv_sub_num);
                BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = LoadImage.with(context)
                        .load(customerImg)
                        .setSaveType(FileManager.save_cache)
                        .setImageRound(ToolsDevice.dp2px(context, 500))
                        .build();
                if(requestBuilder != null){
                    requestBuilder.into(userHeard);
                }
                tv_sub_user_name.setText(customerNickName);
                tv_sub_content.setText(content);
                tv_sub_timeShow.setText(addTime);
                tv_sub_num.setText(num);

                // 点击事件
                setClickEvent(v, viewSubject, subjectCode);
                setClickEvent(tv_sub_content, viewSubject, subjectCode);
                setClickEvent(tv_sub_timeShow, viewSubject, subjectCode);
                setClickEvent(tv_sub_user_name, viewUser, customerCode);
                setClickEvent(userHeard, viewUser, customerCode);
            }
        }else{
            ll_pinglun.setVisibility(View.GONE);
            findViewById(R.id.tv_pinglunHint).setVisibility(View.GONE);
        }

    }

    /**
     * 设置基础数据
     * @param isHas
     * @param tongjiId
     */
    public void setStiatic(boolean isHas,String tongjiId){
        this.isHasVideo= isHas;
        this.tongjiId= tongjiId;

    }
    /**
     * 点击事件的方法
     * @param view 点击的view
     * @param type 事件类型
     */
    private void setClickEvent(View view,final int type,final String code) {
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (type) {
                    case viewUser:
                        Tools.showToast(context,":::;viewUser");
                        if(isHasVideo) XHClick.mapStat(context,tongjiId ,"评论区","");
                        Intent it = new Intent(context, FriendHome.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("code", code);
                        it.putExtras(bundle);
                        context.startActivity(it);
                        break;
                    case viewSubject:
                        Tools.showToast(context,":::;viewSubject");
                        if(isHasVideo) XHClick.mapStat(context,tongjiId ,"评论区","");
                        Intent intent = new Intent(context, ShowSubject.class);
                        intent.putExtra("code", code);
                        context.startActivity(intent);
                        break;
                }
            }
        });
    }
}
