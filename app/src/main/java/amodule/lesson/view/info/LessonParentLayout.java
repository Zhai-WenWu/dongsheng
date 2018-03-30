package amodule.lesson.view.info;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import amodule._common.delegate.IBindMap;
import amodule.lesson.delegate.IShowNextItem;

/**
 * Description :
 * PackageName : amodule.vip.view
 * Created by tanze on 2018/3/30 10:37.
 * e_mail : ztanzeyu@gmail.com
 */
public abstract class LessonParentLayout extends LinearLayout implements IBindMap,IShowNextItem {

    protected List<Map<String,String>> mTopExtraData = new ArrayList<>();
    protected List<Map<String,String>> mBottomExtraData = new ArrayList<>();

    public LessonParentLayout(Context context) {
        super(context);
    }

    public LessonParentLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LessonParentLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setData(Map<String, String> data) {

    }

    protected void setTopExtraData(Map<String, String> data){

    }

    protected void setBottomExtraData(Map<String, String> data){

    }

    @Override
    public boolean showNextItem() {
        return false;
    }

    protected boolean showTopNextItem(){
        return false;
    }

    protected boolean showBottomNextItem(){
        return false;
    }

    protected abstract boolean showInnerNextItem();





}
