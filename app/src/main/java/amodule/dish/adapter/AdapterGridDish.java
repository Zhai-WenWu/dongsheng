package amodule.dish.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule._common.utility.WidgetUtility;

/**
 * Description :
 * PackageName : amodule.dish.adapter
 * Created on 2017/12/1 16:30.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class AdapterGridDish extends RvBaseAdapter<Map<String, String>> {

    LayoutInflater mInflater;
    static int itemWidth, itemHieght;

    public AdapterGridDish(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
        mInflater = LayoutInflater.from(getContext());
        itemWidth = (ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_50)) / 2;
        itemHieght = (int) (itemWidth * 224 / 327f);
    }

    @Override
    public GridDishViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = mInflater.inflate(R.layout.a_dish_grid_item, null, true);
        return new GridDishViewHolder(item);
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    class GridDishViewHolder extends RvBaseViewHolder<Map<String, String>> {

        public GridDishViewHolder(@NonNull View itemView) {
            super(itemView);
            findViewById(R.id.cardview).getLayoutParams().height = itemHieght;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            WidgetUtility.setTextToView(findViewById(R.id.title),data.get("name"));
            WidgetUtility.setTextToView(findViewById(R.id.time_text),data.get("time"));
            Glide.with(getContext()).load(data.get("image"))
                    .placeholder(R.drawable.i_nopic)
                    .error(R.drawable.i_nopic)
                    .into((ImageView) findViewById(R.id.image));
            if(!LoginManager.isVIP())
                WidgetUtility.setResToImage(findViewById(R.id.icon),getIconRes(data.get("isShow")));
            findViewById(R.id.icon).setVisibility(LoginManager.isVIP()?View.GONE:View.VISIBLE);
            findViewById(R.id.shadow).setVisibility("2".equals(data.get("isCurrent"))?View.GONE:View.VISIBLE);
            findViewById(R.id.time_text).setVisibility("2".equals(data.get("isCurrent"))?View.GONE:View.VISIBLE);
        }

        //获取icon id
        private int getIconRes(String isShow){
            switch(isShow){
                case "1":
                    return R.drawable.icon_dish_try_see;
                case "2":
                    return R.drawable.vip;
                default:
                    return 0;
            }
        }
    }

}

