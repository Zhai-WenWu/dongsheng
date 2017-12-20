package amodule.lesson.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule._common.delegate.StatisticCallback;
import amodule._common.helper.WidgetDataHelper;
import amodule.lesson.view.HorizontalRecyclerView;

/**
 * Description :
 * PackageName : amodule.lesson.adapter
 * Created by mrtrying on 2017/12/19 11:26:54.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonHomeAdapter extends RvBaseAdapter<Map<String,String>> {
    public LessonHomeAdapter(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LessonViewHolder(new HorizontalRecyclerView(mContext,viewType));
    }

    @Override
    public int getItemViewType(int position) {
        try{
            String styleValue = mData.get(position).get("style");
            return Integer.parseInt(styleValue);
        }catch (Exception e){
            return 0;
        }
    }

    class LessonViewHolder extends RvBaseViewHolder<Map<String,String>>{
        HorizontalRecyclerView view;

        public LessonViewHolder(@NonNull HorizontalRecyclerView itemView) {
            super(itemView);
            this.view = itemView;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            final String ID = LoginManager.isVIP() || LoginManager.isTempVip() ? "vip_gourmand" : "nonvip_gourmand";
            String title = StringManager.getFirstMap(data.get(WidgetDataHelper.KEY_PARAMETER)).get("title");
            final String titleTwoLevel = StringManager.getFirstMap(title).get("text1");
            view.setStatictusData(ID,titleTwoLevel,"");
            view.setData(data);
            view.setStatisticCallback((id, twoLevel, threeLevel, position1) -> {
                if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)){
                    XHClick.mapStat(getContext(),id,twoLevel,threeLevel);
                }
            });
        }
    }
}
