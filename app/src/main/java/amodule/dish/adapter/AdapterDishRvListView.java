package amodule.dish.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.dish.view.DishStepView;

/**
 * 菜谱详情页面Adapter
 */
public class AdapterDishRvListView extends RvBaseAdapter<Map<String,String>>{
    private ItemOnClickCallBack clickCallBack;
    private boolean isShowDistance = false;
    public AdapterDishRvListView(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StepViewHolder(new DishStepView(mContext));
    }

    public void setClickCallBack(ItemOnClickCallBack itemOnClickCallBack){
        this.clickCallBack= itemOnClickCallBack;
    }
    public void setShowDistance(boolean isShowDistance){
        this.isShowDistance= isShowDistance;
    }
    @Override
    public int getItemViewType(int position) {
        return 0;
    }
    public class StepViewHolder extends RvBaseViewHolder<Map<String,String>>{
        private DishStepView stepView;

        public StepViewHolder(DishStepView dishStepView) {
            super(dishStepView);
            this.stepView=dishStepView;
        }
        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            stepView.setTag(position);
            stepView.setData(data, new DishStepView.StepViewCallBack() {
                @Override
                public void getHeight(String height) {
                }
                @Override
                public void onClick() {
                    clickCallBack.onClickPosition(position);
                }
                @Override
                public void onGifPlayClick() {
                    clickCallBack.onGifClickPosition(position);
                }
            },position);

            if(isShowDistance)stepView.isDistance(position == mData.size()-1);
        }
    }
    public void refreshView(int postion){
    }
    public interface ItemOnClickCallBack{
        public void onClickPosition(int position);
        public void onGifClickPosition(int position);
    }
}
