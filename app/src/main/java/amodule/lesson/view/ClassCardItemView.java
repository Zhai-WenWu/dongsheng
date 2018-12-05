package amodule.lesson.view;

import android.content.Context;
import android.util.AttributeSet;

import com.xiangha.R;

import java.util.Map;

import acore.override.view.ItemBaseView;
import amodule.dish.view.DishSkillView;

class ClassCardItemView extends ItemBaseView {
    public ClassCardItemView(Context context) {
        super(context, R.layout.class_card_scroll_view);
    }

    public ClassCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.class_card_scroll_view);
    }

    public ClassCardItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.class_card_scroll_view);
    }

    public void setData(Map<String, String> data, int position) {
    }
}
