package acore.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;

import acore.tools.Tools;


/**
 * 标题滑块
 * 样式为社区页面的标题样式
 */
public class TitleSlide extends LinearLayout{
    private Context context;
    private LinearLayout linear_choose;
    private int index=-1;//初始角标位
    private slideIndexCallBack callBack;
    public TitleSlide(Context context) {
        super(context);
        this.context= context;
        initView();
    }

    public TitleSlide(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context= context;
        initView();
    }

    public TitleSlide(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context= context;
        initView();
    }

    private void initView(){
        LayoutInflater.from(context).inflate(R.layout.view_title_slide,this,true);
        linear_choose= (LinearLayout) findViewById(R.id.linear_choose);

    }

    /**
     * 设置数据和回调
     * @param lists
     * @param callBack
     */
    public void setData(ArrayList<String>lists,slideIndexCallBack callBack){
        this.callBack= callBack;
        for (int i=0,size= lists.size();i<size;i++){
            createTextView(i,lists.get(i));
        }
    }

    /**
     * 设置到指定位置
     * @param index
     */
    public void setIndex(int index){
        setTitleBackgroupNew(index);
    }

    /**
     * 创建textview
     */
    private void createTextView(int flag,String name){
        final TextView tv = new TextView(context);
        LinearLayout.LayoutParams  layoutParams=new LinearLayout.LayoutParams(Tools.getDimen(context,R.dimen.dp_60),
                Tools.getDimen(context,R.dimen.dp_25));
        tv.setLayoutParams(layoutParams);
        String color = Tools.getColorStr(context,R.color.comment_color);
        tv.setTextColor(Color.parseColor(color));
        tv.setTextSize(Tools.getDimenSp(context,R.dimen.sp_14));
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundResource(R.drawable.bg_main_circle_title_withe);
        tv.setTag(flag);
        tv.setText(name);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitleBackgroupNew(Integer.parseInt(String.valueOf(tv.getTag())));
            }
        });
        linear_choose.addView(tv);
    }
    /**
     * 改变标题的背景颜色
     * @param now_Index
     */
    private void setTitleBackgroupNew(int now_Index){
        if(index==now_Index){//点击的是当前位置
            callBack.getIndex(index);
        }else{//点击不是当前位置
            for (int i=0;i<linear_choose.getChildCount();i++){
                TextView tv_now= (TextView) linear_choose.getChildAt(i);
                if(i==now_Index){
                    index=now_Index;
                    callBack.getIndex(index);
                    tv_now.setBackgroundResource(R.drawable.bg_main_circle_title_withe);
                    String color = Tools.getColorStr(context,R.color.comment_color);
                    tv_now.setTextColor(Color.parseColor(color));
                }else{
                    tv_now.setBackgroundColor(Color.parseColor("#00ffffff"));
                    tv_now.setTextColor(Color.parseColor("#fefefe"));

                }
            }
        }
    }
    public interface  slideIndexCallBack{
        public void getIndex(int index);
    }
}
