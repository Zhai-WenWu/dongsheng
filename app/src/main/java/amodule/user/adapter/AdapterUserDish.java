package amodule.user.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.activity.upload.UploadDishListActivity;
import amodule.dish.db.UploadDishData;
import xh.windowview.XhDialog;

/**
 * Created by Fang Ruijiao on 2016/9/21.
 */
public class AdapterUserDish extends AdapterSimple {
    private List<Map<String,String>> data;
    private BaseActivity mAct;
    public int viewHeight = 0;
    private int mResource;

    private OnDeleteDishClick mDeleteListener;
    private String mTongjiId;

    public AdapterUserDish(BaseActivity mAct, View parent, List<Map<String,String>> data, int resource, String[] from, int[] to, OnDeleteDishClick deleteListener,String tongjiId) {
        super(parent, data, resource, from, to);
        this.data = data;
        this.mAct = mAct;
        mResource = resource;
        mDeleteListener = deleteListener;
        mTongjiId = tongjiId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        final Map<String,String> map = data.get(position);
        //为解决锤子手机崩溃问题
        if(view == null || view.findViewById(R.id.iv_video_img) == null){
            view = LayoutInflater.from(mAct).inflate(mResource,null);
            setViewImage((ImageView) view.findViewById(R.id.iv_video_img),map.get("img"));
            setViewText((TextView) view.findViewById(R.id.a_user_home_dish_name),map.get("nickName"));
        }
        setOnClick(view.findViewById(R.id.iv_video_img),position,"图片");
        setOnClick(view.findViewById(R.id.a_user_home_dish_name),position,"标题");
        setOnClick(view.findViewById(R.id.a_user_home_dish_layout),position,"");
        View iv_delete = view.findViewById(R.id.a_user_home_dish_delete);
        String state = getState(map);
        if ("1".equals(state)) { //可以删除
            iv_delete.setVisibility(View.VISIBLE);
            iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final XhDialog dialog = new XhDialog(mAct);
                    dialog.setTitle("真的要删除这个菜谱么?").
                            setSureButton("删除", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDeleteListener.onDelete(position);
                                    dialog.cancel();
                                }
                            }).setCanselButton("取消", new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                }
            });
        } else {
            iv_delete.setVisibility(View.GONE);
            iv_delete.setOnClickListener(null);
        }
        return view;
    }

    public void setOnClick(View view, final int position, final String tongjiName){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(tongjiName))
                    XHClick.mapStat(mAct, mTongjiId, "菜谱", tongjiName);
                onItemClick(position);
            }
        });
    }

    public void onItemClick(int position){
        // 去掉两个头部位置;
        Map<String, String> mapInfo = data.get(position);
        if (mapInfo != null) {
            //视频菜谱
            if("2".equals(mapInfo.get("hasVideo"))){
                String draft = mapInfo.get("draft");
                //上传中、上传失败、暂停状态，点击都进入上传列表页面
                if(UploadDishData.UPLOAD_ING.equals(draft) || UploadDishData.UPLOAD_ING_BACK.equals(draft) || UploadDishData.UPLOAD_FAIL.equals(draft) || UploadDishData.UPLOAD_PAUSE.equals(draft)){
                    // 看菜谱
                    Intent intent = new Intent(mAct, UploadDishListActivity.class);
                    intent.putExtra("draftId",Integer.parseInt(mapInfo.get("id")));
                    mAct.startActivity(intent);
                }else if("5".equals(mapInfo.get("dishState")) && "1".equals(mapInfo.get("videoState"))){ //正在转码
                    Toast.makeText(mAct,"正在转码，请稍后",Toast.LENGTH_SHORT).show();
                }else{
                    // 看菜谱
                    Intent intent = new Intent(mAct, DetailDish.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("code", mapInfo.get("code"));
                    bundle.putString("name", mapInfo.get("name"));
                    bundle.putString("img", mapInfo.get("img"));
                    if (LoginManager.userInfo.get("code") != null && LoginManager.userInfo.get("code").equals(mapInfo.get("userCode")))
                        bundle.putString("state", mapInfo.get("dishState"));
                    intent.putExtras(bundle);
                    mAct.startActivity(intent);
                }
            }else{
                // 去草稿
                if (mapInfo.get("draft").equals(UploadDishData.UPLOAD_ING)) {
                    Tools.showToast(mAct, "正在发布");
                }else if (mapInfo.get("draft").equals(UploadDishData.UPLOAD_FAIL)) {
                    Intent intent = new Intent(mAct, UploadDishActivity.class);
                    int currentIdDB = Integer.parseInt(mapInfo.get("currentIdDB"));
                    intent.putExtra("id",currentIdDB);
                    intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_DRAFT);
                    mAct.startActivity(intent);
                }
                else {
                    // 看菜谱
                    Intent intent = new Intent(mAct, DetailDish.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("code", mapInfo.get("code"));
                    bundle.putString("name", mapInfo.get("name"));
                    bundle.putString("img", mapInfo.get("img"));
                    if (LoginManager.userInfo.get("code") != null && LoginManager.userInfo.get("code").equals(mapInfo.get("userCode")))
                        bundle.putString("state", mapInfo.get("dishState"));
                    intent.putExtras(bundle);
                    mAct.startActivity(intent);
                }
            }
        }
    }

    private String getState(final Map<String, String> map){
        if(map.get("draft").equals(UploadDishData.UPLOAD_ING) || map.get("draft").equals(UploadDishData.UPLOAD_FAIL)){
            return map.get("draft");
        }
        return map.get("isDelete");
    }

    public interface OnDeleteDishClick{
        public void onDelete(int index);
    }
}
