package amodule.dish.view;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Map;

import acore.override.view.ItemBaseView;

/**
 * 模块化；横滑
 */
public class DishModuleScrollView extends ItemBaseView{
    public DishModuleScrollView(Context context, int layoutId) {
        super(context, layoutId);
    }

    public DishModuleScrollView(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs, layoutId);
    }

    public DishModuleScrollView(Context context, AttributeSet attrs, int defStyleAttr, int layoutId) {
        super(context, attrs, defStyleAttr, layoutId);
    }

    @Override
    public void init() {
        super.init();
    }
    public void setData(ArrayList<Map<String,String>> listMaps){

    }
}
