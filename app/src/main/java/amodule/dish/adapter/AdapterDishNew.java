package amodule.dish.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import amodule.dish.view.DishStepView;

/**
 * 新的菜谱详情页步骤图
 */

public class AdapterDishNew extends AdapterSimple {

    private Context context ;
    private ArrayList<Map<String,String>> data;
    private ItemOnClickCallBack clickCallBack;
    private Activity activity;
    public AdapterDishNew(View parent, List<? extends Map<String, ?>> data) {
        super(parent, data, 0, null, null);
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
        StepViewHolder stepViewHolder = null;
        if(convertView == null|| !(convertView.getTag() instanceof StepViewHolder)){
            stepViewHolder = new StepViewHolder(new DishStepView(context));
            convertView = stepViewHolder.stepView;
            convertView.setTag(stepViewHolder);
        }else{
            stepViewHolder = (StepViewHolder) convertView.getTag();
        }
        stepViewHolder.setData(map,position);
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

    public interface ItemOnClickCallBack{
        public void onClickPosition(int position);
    }

}
