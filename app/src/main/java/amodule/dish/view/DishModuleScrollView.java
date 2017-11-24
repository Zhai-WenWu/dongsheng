package amodule.dish.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xiangha.R;
import java.util.ArrayList;
import java.util.Map;
import acore.override.view.ItemBaseView;

/**
 * 模块化；横滑
 */
public class DishModuleScrollView extends ItemBaseView{
    private TextView module_tv;
    private HorizontalScrollView horizontal_scroll;
    private LinearLayout horizontal_scroll_linear;
    public DishModuleScrollView(Context context) {
        super(context, R.layout.dish_module_scroll_view);
    }

    public DishModuleScrollView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.dish_module_scroll_view);
    }

    public DishModuleScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.dish_module_scroll_view);
    }

    @Override
    public void init() {
        super.init();
        module_tv= (TextView) findViewById(R.id.module_tv);
        horizontal_scroll= (HorizontalScrollView) findViewById(R.id.horizontal_scroll);
        horizontal_scroll_linear= (LinearLayout) findViewById(R.id.horizontal_scroll_linear);
    }
    public void setData(ArrayList<Map<String,String>> listMaps){
        if(listMaps==null||listMaps.size()<=0)return;
        int size= listMaps.size();
        for(int i=0;i<size;i++){
            View view=LayoutInflater.from(context).inflate(R.layout.dish_module_skill,null);
            ImageView img_skill= (ImageView) view.findViewById(R.id.img_skill);
            TextView text1= (TextView) view.findViewById(R.id.text1);
            setViewImage(img_skill,listMaps.get(i),"img");
            text1.setText(listMaps.get(i).get("text"));
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            horizontal_scroll_linear.addView(view);
        }
    }
}
