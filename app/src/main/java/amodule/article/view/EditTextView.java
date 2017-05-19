package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;

import org.json.JSONArray;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:42.
 * E_mail : ztanzeyu@gmail.com
 */

public class EditTextView extends BaseView {
    public EditTextView(Context context) {
        super(context);
    }

    public EditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
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
