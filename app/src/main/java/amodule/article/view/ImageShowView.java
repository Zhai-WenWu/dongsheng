package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;

import org.json.JSONArray;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:41.
 * E_mail : ztanzeyu@gmail.com
 */

public class ImageShowView extends BaseView {
    public ImageShowView(Context context) {
        super(context);
    }

    public ImageShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init() {

    }

    @Override
    public JSONArray getOutputData() {
        return null;
    }
}
