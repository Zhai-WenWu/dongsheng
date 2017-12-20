package amodule.home.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Arrays;
import java.util.List;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import amodule._common.utility.WidgetUtility;

/**
 * Description :
 * PackageName : amodule.home.view
 * Created by MrTrying on 2017/11/14 11:14.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeFuncNavView1 extends LinearLayout {
    public HomeFuncNavView1(Context context) {
        this(context, null);
    }

    public HomeFuncNavView1(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeFuncNavView1(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();
    }

    protected List<Integer> navIds = Arrays.asList(R.id.nav_1, R.id.nav_2, R.id.nav_3, R.id.nav_4);
    protected List<Integer> lineIds = Arrays.asList(R.id.line_1, R.id.line_2, R.id.line_3);

    private void initialize() {
        //填充UI
        LayoutInflater.from(getContext()).inflate(R.layout.widget_func_nav_1_layout, this, true);

        initData();
    }

    protected void initData() {
        int[] iconArray = {R.drawable.home_fanc_nav_1, R.drawable.home_fanc_nav_2, R.drawable.home_fanc_nav_3, R.drawable.home_fanc_nav_4};
        String[] textArray = {"菜谱分类", "VIP名厨课", "视频菜谱", "商城"};
        String[] urls = {
                "xiangha://welcome?fenlei.app",
                "xiangha://welcome?url=https://appweb.xiangha.com/recom/xiangHaSchool",
                "xiangha://welcome?HomeSecond.app?type=video",
                "xiangha://welcome?xhds.home.app",
        };
        for (int index = 0; index < navIds.size(); index++) {
            View navView = findViewById(navIds.get(index));
            setResToView(navView, textArray[index], iconArray[index]);
            String url = urls[index];
            navView.setOnClickListener(v -> AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, true));
        }
        setVisibility(VISIBLE);
    }

    private void setResToView(View itemView, String text, int icon) {
        TextView textView = (TextView) itemView.findViewById(R.id.text_1);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.icon);

        WidgetUtility.setTextToView(textView, text, false);
        WidgetUtility.setResToImage(imageView, icon, false);
    }

    protected void setNavItemVisibility(int id, boolean isShow) {
        if (id <= 0) return;
        findViewById(id).setVisibility(isShow ? VISIBLE : GONE);
        final int index = navIds.indexOf(id);
        if (index < 0) {
            return;
        }
        int lineid = lineIds.get(index < lineIds.size() ? index : lineIds.size() - 1);
        findViewById(lineid).setVisibility(isShow ? VISIBLE : GONE);
    }
}
