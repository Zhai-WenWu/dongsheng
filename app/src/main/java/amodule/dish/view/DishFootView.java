package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.tools.ADDishContorl;
import third.ad.scrollerAd.XHAllAdControl;
import third.ad.scrollerAd.XHScrollerAdParent;
import third.ad.tools.AdPlayIdConfig;

/**
 * Created by Administrator on 2016/9/21.
 */

public class DishFootView extends LinearLayout {
    private Context context;
    private LinearLayout foot_linear, foot_commend, foot_menu;
    private DishExplainView explainView;
    //    private DishShareShow shareShow;
    private Activity activity;
    private ArrayList<Map<String, String>> list_wonderful = new ArrayList<>();
    private boolean startWonderful = false;//是否开启加载页面
    private ADDishContorl adDishContorl;
    private XHAllAdControl xhAllAdControl;
    private int wonderful_index = 0;//加载到第几个位置

    public DishFootView(Context context) {
        super(context);
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);
    }

    public DishFootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);
    }

    public DishFootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);
    }

    public void initView(Activity activitys) {
        this.activity = activitys;
        LinearLayout.LayoutParams linear_layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        foot_menu = new LinearLayout(context);
        foot_menu.setLayoutParams(linear_layoutParams);
        foot_menu.setOrientation(LinearLayout.VERTICAL);
        foot_linear = new LinearLayout(context);
        foot_linear.setLayoutParams(linear_layoutParams);
        foot_linear.setOrientation(LinearLayout.VERTICAL);

        foot_commend = new LinearLayout(context);
        foot_commend.setLayoutParams(linear_layoutParams);
        foot_commend.setOrientation(LinearLayout.VERTICAL);
        this.addView(foot_menu);
        this.addView(foot_commend);
        this.addView(foot_linear);
        //小贴士
        explainView = new DishExplainView(context);
        foot_menu.addView(explainView);
        //分享
//        shareShow= new DishShareShow(context);
//        foot_menu.addView(shareShow);
//        //广告位
//        DishMenuRecommend dishMenuRecommend = new DishMenuRecommend(activity);
//        dishMenuRecommend.setAD(activity);
//        foot_menu.addView(dishMenuRecommend);

        DishAdDataView dishAdDataView = new DishAdDataView(activity);
        dishAdDataView.getRequest(activity, foot_menu);
    }

    public void reset(){
        foot_linear.removeAllViews();
        foot_commend.removeAllViews();
    }

    /**
     * 设置小贴士数据
     *
     * @param stringStringMap
     * @param permissionMap
     */
    public void setDataExplain(Map<String, String> stringStringMap, Map<String, String> permissionMap) {
        explainView.setData(stringStringMap, activity,permissionMap);
    }

//    /**
//     * 设置分享的数据
//     * @param map
//     */
//    public void setDataShare(Map<String, String> map){
//        shareShow.setData(map);
//    }

    /**
     * 菜谱数据解析
     *
     * @param list
     * @param permissionMap
     */
    public void analyzeMenuData(ArrayList<Map<String, String>> list, Map<String, String> permissionMap) {
        for (int i = 0, size = list.size(); i < size; i++) {
            list.get(i).put("style", DishMenuRecommend.DISH_STYLE_MENU);
        }
        for (int i = 0, size = list.size(); i < size; i++) {
            DishMenuRecommend dishMenuRecommend = new DishMenuRecommend(context);
            dishMenuRecommend.setData(list.get(i), activity);
            foot_menu.addView(dishMenuRecommend);
        }
    }

    /**
     * 相关推荐
     *
     * @param list
     * @param permissionMap
     */
    public void analyzeRelatedData(final ArrayList<Map<String, String>> list, Map<String, String> permissionMap) {
        for (int i = 0, size = list.size(); i < size; i++) {
            list.get(i).put("style", DishCommendView_New.DISH_STYLE_COMMEND);
        }
        ArrayList<String> adList = new ArrayList<>();
        adList.add(AdPlayIdConfig.DISH_COMMEND);
        xhAllAdControl = new XHAllAdControl(adList,
                new XHAllAdControl.XHBackIdsDataCallBack() {
                    @Override
                    public void callBack(Map<String, String> map) {
//                        Log.i("tzy","list = " + list.toString());
                        //转化数据，ADData -> NormalData
                        String data = map.get(AdPlayIdConfig.DISH_COMMEND);
                        map = StringManager.getFirstMap(data);
                        if (null != map && map.size() > 0) {
                            if (XHScrollerAdParent.ADKEY_BANNER.equals(map.get("type"))) {
                                map.put("imgUrl", map.get("imgUrl3"));
                            }
                            if (!TextUtils.isEmpty(map.get("imgUrl"))) {
                                map.put("isAD", "2");
                                map.put("adPosition", "0");
                                map.put("img", map.get("imgUrl"));
                                map.put("burdens", map.get("desc"));
                                map.put("source", "1".equals(map.get("adType")) ? "香哈" : "广告");
                                map.put("allClick", String.valueOf(Tools.getRandom(0, 4000) + 6000));
                                map.put("favorites", "hide");
                                list.add(1, map);
                            }
                        }
                        //添加item
                        for (int i = 0, size = list.size(); i < size; i++) {
                            addDishCommendView(list,i);
                        }
                    }
                }, activity, "result_recommend");
    }

    /**
     * 添加推荐item
     * @param list
     * @param i
     */
    private void addDishCommendView(ArrayList<Map<String, String>> list,int i){
        DishCommendView_New dishCommendView = new DishCommendView_New(context);
        dishCommendView.setmADCallback(new DishCommendView_New.ADCallback() {
            @Override
            public void onAdBind(int index, View view, String listIndex) {
                xhAllAdControl.onAdBind(index, view, listIndex);
            }

            @Override
            public void onAdClick(int index, String listIndex) {
                xhAllAdControl.onAdClick(index,listIndex);
            }
        });
        list.get(i).put("position", String.valueOf(i));
        list.get(i).put("size", String.valueOf(list.size() - 1));
        dishCommendView.setData(list.get(i), activity);
        foot_commend.addView(dishCommendView);
    }

    /**
     * 精彩推荐
     *
     * @param list
     * @param permissionMap
     */
    public void analyzeWonderfulData(ArrayList<Map<String, String>> list, ADDishContorl adDishContorls, Map<String, String> permissionMap) {
        this.adDishContorl = adDishContorls;
        list = adDishContorl.getAdList(list);
        for (int i = 0, size = list.size(); i < size; i++) {
            list.get(i).put("style", DishWonderfulView.DISH_STYLE_WONDERFUL);
            list.get(i).put("position", String.valueOf(i));
        }
        list_wonderful = list;
        addWonderfulView(wonderful_index, 2);
    }

    /**
     * 加载列表数据--包含头不包含尾
     *
     * @param start--开始角标位
     * @param size         加载数量
     */
    public synchronized void addWonderfulView(int start, int size) {
        startWonderful = true;
        int length = size;
        if (list_wonderful.size() <= start) {
            return;
        } else if (list_wonderful.size() > start && list_wonderful.size() <= start + size) {
            length = list_wonderful.size();
        } else if (list_wonderful.size() > start + size) {
            length = size + start;
        }
        wonderful_index = length;
        for (int i = start; i < length; i++) {
            DishWonderfulView dishWonderfulView = new DishWonderfulView(context);
            dishWonderfulView.setData(list_wonderful.get(i), activity, adDishContorl);
            foot_linear.addView(dishWonderfulView);
            if (i == list_wonderful.size() - 1) {
                startWonderful = false;
            }
        }
//        loadManager.loadOver(50, 1, true);
//        adapter.notifyDataSetChanged();
    }

    /**
     * 获取加载的位置
     *
     * @return
     */
    public int getWonderful_index() {
        return wonderful_index;
    }

    /**
     * 获取当前状态
     *
     * @return
     */
    public boolean getstartWonderfulState() {
        return startWonderful;
    }


    public interface DishFootCallBack {
        /**
         * 加载完成回调
         */
        public void loadOverWonderful();

        /**
         * 加载中状态的变化
         *
         * @param startWonderful
         */
        public void startWonderfulState(boolean startWonderful);
    }


    public void onListViewScroll() {

        if (foot_linear == null)
            return;
        Log.e("foot_linear", "" + foot_linear.getChildCount());
        for (int i = foot_linear.getChildCount() - 1 ; i >= 0 ; i--) {
            View wonderfulView = foot_linear.getChildAt(i);
            if (wonderfulView != null && wonderfulView instanceof DishWonderfulView) {
                int[] viewLocation = new int[2];
                wonderfulView.getLocationOnScreen(viewLocation);
                if ((viewLocation[1] > Tools.getStatusBarHeight(context)
                        && viewLocation[1] < Tools.getScreenHeight())) {
                    ((DishWonderfulView) wonderfulView).onListScroll();
                    break;
                }
            }
        }

        Log.e("foot_commend", "" + foot_commend.getChildCount());
        for (int i = 0; i < foot_commend.getChildCount(); i++) {
            View commandView = foot_commend.getChildAt(i);
            if (commandView != null && commandView instanceof DishCommendView_New) {
                int[] viewLocation = new int[2];
                commandView.getLocationOnScreen(viewLocation);
                if ((viewLocation[1] > Tools.getStatusBarHeight(context)
                        && viewLocation[1] < Tools.getScreenHeight() - ToolsDevice.dp2px(context, 49 + 95))) {
                    ((DishCommendView_New) commandView).onListScroll();
                }
            }
        }

        Log.e("foot_menu", "" + foot_menu.getChildCount());
        for (int i = 0; i < foot_menu.getChildCount(); i++) {
            View dishAdDataView = foot_menu.getChildAt(i);
            if (dishAdDataView != null && dishAdDataView instanceof DishAdDataView) {
                int[] viewLocation = new int[2];
                dishAdDataView.getLocationOnScreen(viewLocation);
                if ((viewLocation[1] > Tools.getStatusBarHeight(context)
                        && viewLocation[1] < Tools.getScreenHeight() - ToolsDevice.dp2px(context, 57))) {
                    ((DishAdDataView) dishAdDataView).onListScroll();
                }
            }
        }
    }
}
