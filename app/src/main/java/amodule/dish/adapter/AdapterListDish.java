package amodule.dish.adapter;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.MessageView;
import com.xh.view.TitleView;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.TagTextView;
import amodule.dish.db.DataOperate;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import xh.basic.internet.UtilInternet;

/**
 * Title:AdapterListDish.java Copyright: Copyright (c) 2014~2017
 *
 * @author zeyu_t
 * @date 2014年10月14日
 */
public class AdapterListDish extends AdapterSimple {
    private List<? extends Map<String, ?>> data;
    private BaseActivity mAct;
    public int viewHeight = 0;
    private int height = 0 ;

    public AdapterListDish(BaseActivity mAct, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,
                           String type) {
        super(parent, data, resource, from, to);
        this.data = data;
        this.mAct = mAct;
        int screenWidth = ToolsDevice.getWindowPx(mAct).widthPixels;
        int dp_30 = Tools.getDimen(mAct, R.dimen.dp_30);
        height = (screenWidth - dp_30) / 3 * 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) data.get(position);
        parseFavIcon(view, map.get("isFav"));
        parseDelIcon(view, map.get("isDel"));
        parseJinIcon(view, map.get("level"));
        setOnclick(map, view, position);
        RelativeLayout dish_recom_item_root = (RelativeLayout) view.findViewById(R.id.dish_recom_item_root);
        TextView today=(TextView) view.findViewById(R.id.dish_recom_item_today);
        if(today.getVisibility()==View.VISIBLE){
            view.findViewById(R.id.dish_recom_item_today_layout).setVisibility(View.VISIBLE);
            dish_recom_item_root.getLayoutParams().height = height + Tools.getMeasureHeight(today);
        }else{
            view.findViewById(R.id.dish_recom_item_today_layout).setVisibility(View.GONE);
            dish_recom_item_root.getLayoutParams().height = height;
        }
        return view;
    }

    /**
     * 根据标示,设置赞的图标
     */
    private void parseDelIcon(View view, String isDel) {
        if ("hide".equals(isDel))
            view.findViewById(R.id.dish_recom_item_isDel).setVisibility(View.GONE);
        else
            view.findViewById(R.id.dish_recom_item_isDel).setVisibility(View.VISIBLE);
    }

    /**
     * 根据标示,设置收藏的图标
     */
    public void parseFavIcon(View view, String isFav) {
        if ("2".equals(isFav)) {
            ((ImageView) view.findViewById(R.id.dish_recom_item_isFav)).setImageResource(R.drawable.z_caipu_today_ico_fav_active);
        } else if ("1".equals(isFav)) {
            ((ImageView) view.findViewById(R.id.dish_recom_item_isFav)).setImageResource(R.drawable.z_caipu_today_ico_fav);
        } else if ("hide".equals(isFav)) {
            ((ImageView) view.findViewById(R.id.dish_recom_item_isFav)).setVisibility(View.GONE);
        }
    }
    public void parseJinIcon(View view, String leve){
        if("3".equals(leve)){
            view.findViewById(R.id.iv_itemIsGood).setVisibility(View.VISIBLE);
            ((TagTextView)view.findViewById(R.id.iv_itemIsGood)).setText("精华");
        }else{
            view.findViewById(R.id.iv_itemIsGood).setVisibility(View.GONE);
        }
    }
    // 绑定点击动作
    private void setOnclick(final Map<String, String> map, final View view, final int index) {
        view.findViewById(R.id.dish_recom_item_isFav).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (LoginManager.isLogin()) {
                    doFavorite(map);
                } else {
                    Intent intent = new Intent(mAct, LoginByAccout.class);
                    mAct.startActivity(intent);
                }
            }
        });
        view.findViewById(R.id.dish_recom_item_isDel).setOnClickListener(deleteClicker(map, index));
    }

    // 收藏响应
    public void doFavorite(final Map<String, String> map) {
        AppCommon.onFavoriteClick(mAct,"favorites", map.get("code"), new InternetCallback(mAct) {
            @Override
            public void loaded(int flag, String url, Object returnObj) {
                if (flag >= UtilInternet.REQ_OK_STRING)
                    parseFavClick(map);
            }
        });
    }

    /**
     * 收藏事件处理
     * @param map
     */
    private void parseFavClick(Map<String, String> map) {
        String str = map.get("favorites");
        int favorites = Integer.parseInt(str.substring(0, str.indexOf("收藏")));
        if ((map.get("isFav") + "").equals("2")) {
            map.put("favorites", (favorites - 1) + "收藏");
            map.put("isFav", "1");
        } else {
            map.put("favorites", (favorites + 1) + "收藏");
            map.put("isFav", "2");
        }
        notifyDataSetChanged();
    }

    // 删除单个响应
    private OnClickListener deleteClicker(final Map<String, String> map, final int index) {
        return new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final DialogManager dialogManager = new DialogManager(mAct);
                dialogManager.createDialog(new ViewManager(dialogManager)
                        .setView(new TitleView(mAct).setText("取消删除"))
                        .setView(new MessageView(mAct).setText("确定要删除离线菜谱？"))
                        .setView(new HButtonView(mAct)
                                .setNegativeText("取消", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogManager.cancel();
                                    }
                                })
                                .setPositiveText("确定", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DataOperate.deleteBuyBurden(mAct, map.get("code"));
                                        data.remove(index);
                                        notifyDataSetChanged();
                                        Tools.showToast(mAct, "删除成功");
                                        dialogManager.cancel();
                                    }
                                }))).show();
            }
        };
    }
}
