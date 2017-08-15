package third.mall.adapter;

import android.app.Activity;
import android.content.Context;
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

    private Activity activity;

    private List<Map<String,String>> data = new ArrayList<>();

    private String orderId;

    public AdapterEvalution(Activity activity, List<Map<String, String>> data,String orderId){
        this.activity = activity;
        this.data = data;
        this.orderId = orderId;
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

        public void setData(final Map<String,String> data){
            if(view != null){
                view.setData(data);
                view.setOnRatePickedCallback(new CommodEvalutionItem.OnRatePickedCallback() {
                    @Override
                    public void onRatePicked(int rating) {
                        data.put("score",String.valueOf(rating));
                        XHClick.mapStat(view.getContext(), PublishEvalutionMultiActivity.STATISTICS_ID,"点击星星","");
                        AdapterEvalution.this.notifyDataSetChanged();
                    }
                });
                view.setOnEvalutionClickCallback(new CommodEvalutionItem.OnEvalutionClickCallback() {
                    @Override
                    public void onEvalutionClick(CommodEvalutionItem view, Map<String, String> data) {
                        Intent intent = new Intent(activity, PublishEvalutionSingleActivity.class);
                        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_ORDER_ID,orderId);
                        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_PRODUCT_ID,data.get("product_code"));
                        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_PRODUCT_IMAGE,data.get("product_img"));
                        intent.putExtra(PublishEvalutionSingleActivity.EXTRAS_SCORE,Integer.parseInt(data.get("score")));
                        activity.startActivityForResult(intent,PublishEvalutionMultiActivity.REQUEST_CODE_NEED_REFRESH);
                    }
                });
            }
        }

    }
}
