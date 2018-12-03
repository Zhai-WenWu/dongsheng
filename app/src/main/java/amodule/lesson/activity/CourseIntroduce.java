package amodule.lesson.activity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.xiangha.R;

import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.Tools;

/**
 * Description :
 * PackageName : amodule.lesson.activity
 * Created by mrtrying on 2018/12/3 15:26.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseIntroduce extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("",2,0,0,R.layout.a_course_introduce);
        initTitle();
    }

    private void initTitle() {
        if (Tools.isShowTitle()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int topbarHeight = Tools.getDimen(this, R.dimen.topbar_height);
            final int statusBarHeight = Tools.getStatusBarHeight(this);
        }
    }

}
