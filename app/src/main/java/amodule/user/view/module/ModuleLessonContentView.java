package amodule.user.view.module;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.widget.TagTextView;
import amodule._common.utility.WidgetUtility;

/**
 * Description : //TODO
 * PackageName : amodule.user.view.module
 * Created by tanzeyu on 2018/4/2 11:33.
 * e_mail : ztanzeyu@gmail.com
 */
public class ModuleLessonContentView extends ModuleBaseView {

    static final int LAYOUT_ID = R.layout.module_lesson_content_view;

    private ImageView mImageView;
    private TagTextView mTagTextView;
    private TextView mTextDesc;

    public ModuleLessonContentView(Context context) {
        super(context, LAYOUT_ID);
    }

    public ModuleLessonContentView(Context context, AttributeSet attrs) {
        super(context, attrs, LAYOUT_ID);
    }

    public ModuleLessonContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, LAYOUT_ID);
    }

    @Override
    public void initUI() {
        setMODULE_TAG("B5");
        mImageView = (ImageView) findViewById(R.id.image);
        mTagTextView = (TagTextView) findViewById(R.id.text1);
        mTextDesc = (TextView) findViewById(R.id.text2);
    }

    @Override
    public void initData(Map<String, String> map) {
        if(!map.containsKey("styleData")|| TextUtils.isEmpty(map.get("styleData"))){
            setVisibility(GONE);
            return;
        }
        //在特殊处理

        WidgetUtility.setTextToView(mTagTextView,map.get("text1"),false);
        WidgetUtility.setTextToView(mTextDesc,map.get("text2"),false);
//        setViewImage(mImageView,map.get());
        setVisibility(VISIBLE);
    }

    @Override
    public void setListener() {

    }
}
