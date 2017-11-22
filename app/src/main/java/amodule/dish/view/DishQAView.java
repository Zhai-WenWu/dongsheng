package amodule.dish.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.view.ItemBaseView;
import acore.tools.StringManager;
import acore.tools.Tools;

/**
 * 菜谱详情页问答
 */
public class DishQAView extends ItemBaseView{
    private ImageView auther_userImg;
    private TextView text_user,text_answer,text_degree,text_time;
    private LinearLayout qa_content_linear;
    public DishQAView(Context context) {
        super(context, R.layout.view_dish_qa);
    }

    public DishQAView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.view_dish_qa);
    }

    public DishQAView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.view_dish_qa);
    }
    @Override
    public void init() {
        super.init();
        auther_userImg = (ImageView) findViewById(R.id.auther_userImg);
        text_user= (TextView) findViewById(R.id.text_user);
        text_answer= (TextView) findViewById(R.id.text_answer);
        text_degree= (TextView) findViewById(R.id.text_degree);
        text_time= (TextView) findViewById(R.id.text_time);
        qa_content_linear= (LinearLayout) findViewById(R.id.qa_content_linear);
    }

    public void setData(ArrayList<Map<String,String>> list){

//        setViewImage(auther_userImg,list.get(0).get("img"));
//        ArrayList<Map<String,String>> listQA = StringManager.getListMapByJson(list.get(0).get(""));
//        if(listQA!=null&&listQA.size()>0){
//            for(int i=0;i<listQA.size();i++){
//                Map<String,String> mapQA= listQA.get(i);
                View qaItem=LayoutInflater.from(context).inflate(R.layout.view_dish_qa_item,null);
                TextView content_one= (TextView) qaItem.findViewById(R.id.content_one);
                TextView content_two= (TextView) qaItem.findViewById(R.id.content_two);
                content_one.setText(getClickableSpan("张玉建的德胜门内德胜门内的释迦摩尼",true,null));
                content_one.setMovementMethod(LinkMovementMethod.getInstance());//必须设置否则无效
                qa_content_linear.addView(qaItem);
//            }
//        }
        this.findViewById(R.id.qa_more_linaer).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    private SpannableString getClickableSpan(String content,boolean isShowIm,Map<String,String> maps) {
        content="问 "+content+" 图";
        SpannableString spanableInfo = new SpannableString(content);
        //处理问图片
        Drawable d = getResources().getDrawable(R.drawable.dish_qa_icon);
        d.setBounds(1, 1, Tools.getDimen(context,R.dimen.dp_20), Tools.getDimen(context,R.dimen.dp_20));
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
        spanableInfo.setSpan( span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

       //处理图片数据
        Drawable bitmap = getResources().getDrawable(R.drawable.dish_qa_bitmap);
        bitmap.setBounds(1, 1, Tools.getDimen(context,R.dimen.dp_18), Tools.getDimen(context,R.dimen.dp_18));
        ImageSpan span_bitmap = new ImageSpan(bitmap, ImageSpan.ALIGN_BASELINE);

        spanableInfo.setSpan( span_bitmap, content.length()-1, content.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        View.OnClickListener bitSpanClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.showToast(context,"测试数据");
            }
        };
        spanableInfo.setSpan(new Clickable(bitSpanClick), content.length()-1, content.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanableInfo;
    }
    class Clickable extends ClickableSpan implements View.OnClickListener {
        private final View.OnClickListener mListener;

        public Clickable(View.OnClickListener mListener) {
            this.mListener = mListener;
        }
        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
    }
}
