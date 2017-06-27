package amodule.quan.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.XHClick;
import amodule.dish.activity.DetailDish;
import amodule.quan.activity.ShowSubject;
import amodule.search.avtivity.HomeSearch;
import third.mall.tool.ToolView;

/**
 * PackageName : amodule.quan.view
 * Created by MrTrying on 2016/9/28 20:01.
 * E_mail : ztanzeyu@gmail.com
 */

public class SubjectHeaderMore extends RelativeLayout implements View.OnClickListener{
    private TextView moreText;
    String title="";
    String dishCode = "";
    String dishName = "";
    public SubjectHeaderMore(Context context) {
        this(context,null);
    }

    public SubjectHeaderMore(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SubjectHeaderMore(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.subject_header_more,this);
        moreText = (TextView) findViewById(R.id.head_subject_more);
        setOnClickListener(this);
    }

    /**
     * 设置数据
     * @param title
     * @param type
     */
    public void setData(String title,String type){
        this.title = title;
        //设置类型
        setType(type);
        //设置text
        String des_title = handlerTitle(title);
        if (!TextUtils.isEmpty(des_title))
            moreText.setText("查看" + des_title + "的做法>>");
        else
            setVisibility(View.GONE);
    }

    public void setDishInfo(String code,String name){
        this.dishCode =code;
        this.dishName = name;
    }

    /**
     * 处理不同的type的类型
     *
     * @param type
     */
    private void setType(String type) {
        setVisibility(View.GONE);
        if ("3".equals(type) || "5".equals(type)) {//菜谱贴子
            setVisibility(View.VISIBLE);
        } else if ("2".equals(type)) {
            setVisibility(View.VISIBLE);
        }
    }

    /**
     * 处理title
     * @param title
     * @return
     */
    private String handlerTitle(String title) {
        int num = getTextNum();
        if (!TextUtils.isEmpty(title)) {
            if (title.length() > num - 6)
                return title.substring(0, num - 6) + "...";
            else return title;
        } else return null;
    }

    /**
     * 获取当前字数
     *
     * @return
     */
    private int getTextNum() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_16);
        int distance = (int) this.getResources().getDimension(R.dimen.dp_15);

        int waith = wm.getDefaultDisplay().getWidth();
        int tv_waith = waith - distance * 4;
        int tv_pad = ToolView.dip2px(getContext(), 1.0f);
        int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);
        return num;
    }

    public void setMoreTextClick(OnClickListener listener){
        if(moreText != null && listener != null){
            moreText.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
        XHClick.mapStat(getContext(), BarSubjectFloorOwnerNew.tongjiId, "搜索链接点击量", "");
        Intent intent;
        if (dishCode != null) {
            intent = new Intent(getContext(), DetailDish.class);
            if(TextUtils.isEmpty(dishCode)
                    || TextUtils.isEmpty(dishName)){
                return;
            }
            intent.putExtra("code", dishCode);
            intent.putExtra("name", dishName);
        } else {
            intent = new Intent(getContext(), HomeSearch.class);
            intent.putExtra("from", "美食贴更多");
            intent.putExtra("s", title);
            intent.putExtra("type", "4".equals(ShowSubject.types) ? "zhishi" : "caipu");
        }
        getContext().startActivity(intent);
    }
}
