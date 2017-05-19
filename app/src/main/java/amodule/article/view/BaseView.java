package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.json.JSONArray;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:37.
 * E_mail : ztanzeyu@gmail.com
 */

public abstract class BaseView extends RelativeLayout {
    public BaseView(Context context) {
        super(context);
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void init();
    public abstract JSONArray getOutputData();

}
