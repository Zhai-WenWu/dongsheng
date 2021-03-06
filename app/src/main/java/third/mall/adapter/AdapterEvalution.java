package third.mall.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import third.mall.activity.PublishEvalutionMultiActivity;
import third.mall.activity.PublishEvalutionSingleActivity;
import third.mall.view.CommodEvalutionItem;

/**
 * PackageName : third.mall.adapter
 * Created by MrTrying on 2017/8/8 17:08.
 * E_mail : ztanzeyu@gmail.com
 */

public class AdapterEvalution<T extends Map<String,String>> extends BaseAdapter{
    /**上下文*/
    private Activity activity;
    /**数据*/
    private List<Map<String,String>> data = new ArrayList<>();
    /**订单号*/
    private String orderId;

    int id,orderPosition;

    public AdapterEvalution(Activity activity, List<Map<String, String>> data,String orderId){
        this.activity = activity;
        this.data = data;
        this.orderId = orderId;
    }

    public void setIdAndPosition(int id,int orderPosition){
        this.id = id;
        this.orderPosition = orderPosition;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Map<String,String> getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = new CommodEvalutionItem(activity);
            viewHolder = new ViewHolder((CommodEvalutionItem) convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.setData(getItem(position));
        return convertView;
    }

    public class ViewHolder{
        private CommodEvalutionItem view;

        public ViewHolder(CommodEvalutionItem view){
            this.view = view;
        }

        /**
         * 设置数据
         * @param data 数据
         */
        public void setData(final Map<String,String> data){
            if(view != null){
                view.setData(data);
                //设置星星点击事件
                view.setOnRatePickedCallback(new CommodEvalutionItem.OnRatePickedCallback() {
                    @Override
                    public void onRatePicked(int rating) {
                        data.put("score",String.valueOf(rating));
                        XHClick.mapStat(view.getContext(), PublishEvalutionMultiActivity.STATISTICS_ID,"点击星星","");
                        AdapterEvalution.this.notifyDataSetChanged();
                    }
                });
                //设置评价按钮点击事件
                view.setOnEvalutionClickCallback(new CommodEvalutionItem.OnEvalutionClickCallback() {
                    @Override
                    public void onEvalutionClick(CommodEvalutionItem view, Map<String, String> data) {
                        Intent intent = new Intent(activity, PublishEvalutionSingleActivity.class);
                        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_ORDER_ID,orderId);
                        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_ID,id);
                        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_POSITION,orderPosition);
                        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_PRODUCT_CODE,data.get("product_code"));
                        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_PRODUCT_IMAGE,data.get("product_img"));
                        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_SCORE,data.get("score"));
                        activity.startActivityForResult(intent,PublishEvalutionMultiActivity.REQUEST_CODE_NEED_REFRESH);
                    }
                });
            }
        }

    }
}
