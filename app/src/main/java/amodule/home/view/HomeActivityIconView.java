package amodule.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Description : //TODO
 * PackageName : amodule.home
 * Created by MrTrying on 2017/11/13 15:17.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomeActivityIconView extends RelativeLayout{

    public HomeActivityIconView(Context context) {
        this(context,null);
    }

    public HomeActivityIconView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HomeActivityIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        initUI();
        initData();
    }

    //初始化UI
    private void initUI() {

    }

    //初始化数据
    private void initData() {

    }



    //获取数据
    private void loadData(){

    }

    //保存数据
    public void saveData(){

    }
}
