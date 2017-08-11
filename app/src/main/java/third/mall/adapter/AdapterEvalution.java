package third.mall.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import third.mall.view.CommodEvalutionItem;

/**
 * PackageName : third.mall.adapter
 * Created by MrTrying on 2017/8/8 17:08.
 * E_mail : ztanzeyu@gmail.com
 */

public class AdapterEvalution<T extends Map<String,String>> extends BaseAdapter{

    private Context context;

    private List<Map<String,String>> data = new ArrayList<>();

    public AdapterEvalution(Context context, List<Map<String, String>> data){
        this.context = context;
        this.data = data;
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
            convertView = new CommodEvalutionItem(context);
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
                        AdapterEvalution.this.notifyDataSetChanged();
                    }
                });
            }
        }

    }
}
