package amodule.dish.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.StringManager;

public class DishRelatedRecommendView extends LinearLayout {

    private LinearLayout mContentLayout;

    private DishGridDialog.OnItemClickCallback mOnItemClickCallback;
    public DishRelatedRecommendView(Context context) {
        super(context);
        initView(context);
    }

    public DishRelatedRecommendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DishRelatedRecommendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_dish_commend, this);
        mContentLayout = (LinearLayout) findViewById(R.id.content);
    }

    public void setData(ArrayList<Map<String, String>> list) {
        mContentLayout.removeAllViews();
        ArrayList<Map<String, String>> entity = StringManager.getListMapByJson(list.get(0).get("data"));
        int p = 0;
        for (Map<String, String> data : entity){
            DishRelatedRecommendItemView itemView = new DishRelatedRecommendItemView(getContext());
            if (mOnItemClickCallback != null)
                itemView.setOnClickCallback(mOnItemClickCallback);
            int temp = p;
            itemView.setData(data, temp);
            mContentLayout.addView(itemView);
            p ++;
        }
        setVisibility(entity.isEmpty() ? GONE : VISIBLE);
    }

    public void setOnItemClickCallback(DishGridDialog.OnItemClickCallback callback) {
        mOnItemClickCallback = callback;
    }

}
