package amodule.user.view.module;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import amodule._common.utility.WidgetUtility;

/**
 * Description : //TODO
 * PackageName : amodule.user.view.module
 * Created by tanzeyu on 2018/4/2 11:41.
 * e_mail : ztanzeyu@gmail.com
 */
public class ModuleLessonGuessLike extends ModuleBaseView {

    static final int LAYOUT_ID = R.layout.module_lesson_guess_like;

    private ImageView mImageView;
    private TextView mTextTitle,mTextDesc,mTextLessonDesc;

    public ModuleLessonGuessLike(Context context) {
        super(context, LAYOUT_ID);
    }

    public ModuleLessonGuessLike(Context context, AttributeSet attrs) {
        super(context, attrs, LAYOUT_ID);
    }

    public ModuleLessonGuessLike(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, LAYOUT_ID);
    }

    @Override
    public void initUI() {
        setMODULE_TAG("B6");
        mImageView = (ImageView) findViewById(R.id.image);
        mTextTitle = (TextView) findViewById(R.id.text1);
        mTextDesc = (TextView) findViewById(R.id.text2);
        mTextLessonDesc = (TextView) findViewById(R.id.text3);
    }

    @Override
    public void initData(Map<String, String> map) {
        Map<String,String> styleData = StringManager.getFirstMap(map.get("styleData"));
        if(styleData.isEmpty()){
            mImageView.setVisibility(INVISIBLE);
        }else{
            setViewImage(mImageView,styleData.get("img"));
        }
        WidgetUtility.setTextToView(mTextTitle,map.get("text1"),false);
        WidgetUtility.setTextToView(mTextDesc,map.get("text2"),false);
        WidgetUtility.setTextToView(mTextLessonDesc,map.get("text2"),false);
    }

    @Override
    public void setListener() {

    }
}
