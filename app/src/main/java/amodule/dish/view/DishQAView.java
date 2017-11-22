package amodule.dish.view;

import android.content.Context;
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

    private void setData(ArrayList<Map<String,String>> list){

//        setViewImage(auther_userImg,list.get(0).get("img"));
        ArrayList<Map<String,String>> listQA = StringManager.getListMapByJson(list.get(0).get(""));
        if(listQA!=null&&listQA.size()>0){
            for(int i=0;i<listQA.size();i++){
                Map<String,String> mapQA= listQA.get(i);
                View qaItem=LayoutInflater.from(context).inflate(R.layout.view_dish_qa_item,null);
                TextView content_one= (TextView) qaItem.findViewById(R.id.content_one);
                TextView content_two= (TextView) qaItem.findViewById(R.id.content_two);

            }
        }
        this.findViewById(R.id.qa_more_linaer).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}
