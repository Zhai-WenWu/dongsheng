package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.json.JSONArray;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:37.
 * E_mail : ztanzeyu@gmail.com
 */

public abstract class BaseView extends RelativeLayout {
    protected OnClickImageListener mOnClickImageListener;
    protected OnRemoveCallback mOnRemoveCallback;

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
    public abstract String getOutputData();

    public interface OnClickImageListener{
        public void onClick(View v,String url);
    }
    public interface OnRemoveCallback{
        public void onRemove(BaseView view);
    }

    public OnClickImageListener getmOnClickImageListener() {
        return mOnClickImageListener;
    }

    public void setmOnClickImageListener(OnClickImageListener mOnClickImageListener) {
        this.mOnClickImageListener = mOnClickImageListener;
    }

    public OnRemoveCallback getmOnRemoveCallback() {
        return mOnRemoveCallback;
    }

    public void setmOnRemoveCallback(OnRemoveCallback mOnRemoveCallback) {
        this.mOnRemoveCallback = mOnRemoveCallback;
    }
}
