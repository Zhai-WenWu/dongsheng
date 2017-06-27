package amodule.dish.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import acore.override.view.ItemBaseView;

/**
 * Created by Administrator on 2016/8/15.
 */

public class DishVideoImageView extends ItemBaseView {
    private ImageView imageview_rela;
    private TextView time_tv;
    public DishVideoImageView(Context context) {
        super(context, R.layout.view_dish_video);
    }

    public DishVideoImageView(Context context, AttributeSet attrs) {
        super(context, attrs,  R.layout.view_dish_video);
    }

    public DishVideoImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr,  R.layout.view_dish_video);
    }

    @Override
    public void init() {
        super.init();
        imageview_rela= (ImageView) findViewById(R.id.imageview_rela);
        time_tv= (TextView) findViewById(R.id.time);
    }

    /**
     * 设置数据
     * @param img
     * @param time
     */
    public View setData(String img,String time){
        setViewImage(imageview_rela,img);
        if(!TextUtils.isEmpty(time)){
            time_tv.setVisibility(View.VISIBLE);
            time_tv.setText(time);
        }else time_tv.setVisibility(View.GONE);
        return this;
    }

    public View setDataNoTime(String img){
        setViewImage(imageview_rela,img);
        findViewById(R.id.play_layout_1).setVisibility(GONE);
        findViewById(R.id.play_layout_2).setVisibility(VISIBLE);
        return this;
    }
}
