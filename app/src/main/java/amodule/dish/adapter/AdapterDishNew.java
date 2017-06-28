package amodule.dish.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import amodule.dish.view.DishCommendView;
import amodule.dish.view.DishExplainView;
import amodule.dish.view.DishMenuRecommend;
import amodule.dish.view.DishShareShow;
import amodule.dish.view.DishStepView;
import amodule.dish.view.DishWonderfulView;

/**
 * 新的菜谱详情页步骤图
 */

public class AdapterDishNew extends AdapterSimple{

    private Context context ;
    private ArrayList<Map<String,String>> data;
    private ItemOnClickCallBack clickCallBack;
    private Activity activity;
    public AdapterDishNew(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(parent, data, resource, from, to);
        this.context= parent.getContext();
        this.data= (ArrayList<Map<String, String>>) data;
    }

    public void setClickCallBack(ItemOnClickCallBack itemOnClickCallBack){
        this.clickCallBack= itemOnClickCallBack;
    }
    public void setActivity(Activity activity){
        this.activity= activity;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String,String> map= (Map<String, String>) data.get(position);
        String style= map.get("style");
        if(style.equals(DishStepView.DISH_STYLE_STEP)){//步骤
            StepViewHolder stepViewHolder = null;
            if(convertView == null
                    || !(convertView.getTag() instanceof StepViewHolder)){
                stepViewHolder = new StepViewHolder(new DishStepView(context));
                convertView = stepViewHolder.stepView;
                convertView.setTag(stepViewHolder);
            }else{
                stepViewHolder = (StepViewHolder) convertView.getTag();
            }
            stepViewHolder.setData(map,position);
        }else if(style.equals(DishExplainView.DISH_STYLE_EXP)){//小贴士
            DishExplainView explainView= new DishExplainView(context);
            //TODO 适应以前代码
            explainView.setData(map,activity, new HashMap<String, String>());
            convertView= explainView;

        }else if(style.equals(DishShareShow.DISH_STYLE_SHARE)){//分享
            DishShareShow shareShow= new DishShareShow(context);
            shareShow.setData(map);
            convertView=shareShow;

        }else if(style.equals(DishMenuRecommend.DISH_STYLE_MENU)){//菜谱推荐
            DishMenuRecommend menuRecommend= new DishMenuRecommend(context);
            menuRecommend.setData(map,activity);
            convertView= menuRecommend;
        }else if(style.equals(DishCommendView.DISH_STYLE_COMMEND)){//相关推荐
            CommendViewHolder commendViewHolder = null;
            if(convertView == null|| !(convertView.getTag() instanceof CommendViewHolder)){
                commendViewHolder = new CommendViewHolder(new DishCommendView(context));
                convertView = commendViewHolder.commendView;
                convertView.setTag(commendViewHolder);
            }else{
                commendViewHolder = (CommendViewHolder) convertView.getTag();
            }
            commendViewHolder.setData(map,position);
        }else if(style.equals(DishWonderfulView.DISH_STYLE_WONDERFUL)){//精彩推荐
            WonderfulViewHolder wonderfulViewHolder=null;
            if(convertView == null|| !(convertView.getTag() instanceof WonderfulViewHolder)){
                wonderfulViewHolder= new WonderfulViewHolder(new DishWonderfulView(context));
                convertView= wonderfulViewHolder.wonderfulView;
                convertView.setTag(wonderfulViewHolder);
            }else{
                wonderfulViewHolder= (WonderfulViewHolder)convertView.getTag();
            }
            wonderfulViewHolder.setData(map,position);
        }
        return convertView;
    }
    public class StepViewHolder{
        private DishStepView stepView;

        public StepViewHolder(DishStepView dishStepView) {
            this.stepView=dishStepView;
        }

        public void setData(Map<String,String> map, final int position){
            stepView.setData(map, new DishStepView.StepViewCallBack() {
                @Override
                public void getHeight(String height) {
                    data.get(position).put("height",height);
                }

                @Override
                public void onClick() {
                    clickCallBack.onClickPosition(position);
                }
            },position);
        }
    }

    public class CommendViewHolder{
        private DishCommendView commendView;

        public CommendViewHolder(DishCommendView dishCommendView) {
            this.commendView=dishCommendView;
        }

        public void setData(Map<String,String> map, final int position){
            commendView.setData(map,activity);
        }
    }

    public class WonderfulViewHolder{
        private DishWonderfulView wonderfulView;
        public WonderfulViewHolder(DishWonderfulView dishWonderfulView){
            this.wonderfulView= dishWonderfulView;
        }
        public void setData(Map<String,String> map, final int position){
//            wonderfulView.setData(map,activity);
        }
    }
    public interface ItemOnClickCallBack{
        public void onClickPosition(int position);
    }

}
