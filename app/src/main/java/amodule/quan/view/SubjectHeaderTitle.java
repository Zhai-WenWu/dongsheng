package amodule.quan.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.tools.Tools;
import acore.widget.TextViewShow;
import acore.widget.multifunction.JinghuaStyleBuilder;
import acore.widget.multifunction.view.MultifunctionTextView;

/**
 * PackageName : amodule.quan.view
 * Created by MrTrying on 2016/9/28 18:54.
 * E_mail : ztanzeyu@gmail.com
 */

public class SubjectHeaderTitle extends RelativeLayout {
    private TextViewShow sb_header_tv_title;

    public SubjectHeaderTitle(Context context) {
        this(context,null);
    }

    public SubjectHeaderTitle(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SubjectHeaderTitle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.subject_header_title,this);
        initView();
    }

    private void initView() {
        sb_header_tv_title = (TextViewShow) findViewById(R.id.sb_header_tv_title);
    }

    /**
     *
     * @param title
     * @param type
     * @param isJingHua
     */
    public void setTitle(String title,String type, String isJingHua){
        JinghuaStyleBuilder jinghuaBuilder = null;
        if ("3".equals(type) || "5".equals(type)) {//菜谱帖子
            jinghuaBuilder = new JinghuaStyleBuilder(getContext(), "菜谱", R.drawable.round_red);
        } else if ("4".equals(type)) {//知识帖子
            jinghuaBuilder = new JinghuaStyleBuilder(getContext(), "知识", R.drawable.round_red);
        } else if ("2".equals(isJingHua)) {//精华帖子
            jinghuaBuilder = new JinghuaStyleBuilder(getContext(), "精华", R.drawable.round_red);
        }

        if (jinghuaBuilder != null) {
            String color = Tools.getColorStr(getContext(), R.color.comment_color);
            jinghuaBuilder.setTextColor(color);
            MultifunctionTextView.MultifunctionText multifunctionText = new MultifunctionTextView.MultifunctionText();
            multifunctionText.addStyle(jinghuaBuilder.getText() + " " + title, jinghuaBuilder.build());
            sb_header_tv_title.setText(multifunctionText);
        } else {
            setViewText(sb_header_tv_title, title);
        }
    }

    public void setTitleOnClick(OnClickListener listener){
        if(listener != null){
            sb_header_tv_title.setOnClickListener(listener);
        }
    }

    public void setTitleRightClick(String content,OnClickListener rightClick){
        if(rightClick != null && !TextUtils.isEmpty(content)){
            sb_header_tv_title.setRightClicker(content,rightClick);
        }
    }

    /**
     * 处理文字是否显示
     *
     * @param v
     * @param text
     */
    public void setViewText(TextView v, String text) {
        if (text == null || text.length() == 0 || text.equals("hide"))
            v.setVisibility(View.GONE);
        else {
            v.setVisibility(View.VISIBLE);
            v.setText(text);
        }
    }

}
