package amodule.user.view.module;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.tools.Tools;
import acore.widget.TagTextView;
import acore.widget.TextViewShow;
import acore.widget.multifunction.JinghuaStyleBuilder;
import acore.widget.multifunction.VipStyleBuilder;
import acore.widget.multifunction.view.MultifunctionTextView;
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
    private TextViewShow mTagTextView;
    private TextView mTextDesc,mTextLessonDesc;

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
        mTagTextView = (TextViewShow) findViewById(R.id.text1);
        mTextDesc = (TextView) findViewById(R.id.text2);
        mTextLessonDesc = (TextView) findViewById(R.id.text2);
    }

    @Override
    public void initData(Map<String, String> map) {
//        if(!map.containsKey("styleData")|| TextUtils.isEmpty(map.get("styleData"))){
//            setVisibility(GONE);
//            return;
//        }
        //在特殊处理
        showTagTextView(map);
        WidgetUtility.setTextToView(mTextDesc,map.get("text2"),false);
        WidgetUtility.setTextToView(mTextLessonDesc,map.get("text3"),false);
//        setViewImage(mImageView,map.get());
        setVisibility(VISIBLE);
    }

    private void showTagTextView(Map<String, String> map) {
        VipStyleBuilder vipStyleBuilder =  null;
        switch (map.get("iconVip")){
            case "1":
                vipStyleBuilder = new VipStyleBuilder(getContext(), "试看", R.drawable.bg_round4_ff533c);
                break;
            case "2":
                vipStyleBuilder = new VipStyleBuilder(getContext(), "VIP", R.drawable.bg_round4_ff533c);
                break;
            case "3":
                vipStyleBuilder = new VipStyleBuilder(getContext(), "上新", R.drawable.bg_round4_ff533c);
                break;
                default:break;
        }
        if(vipStyleBuilder != null){
            vipStyleBuilder.setTextColor("#FFFFFF");
            MultifunctionTextView.MultifunctionText multifunctionText = new MultifunctionTextView.MultifunctionText();
            String titleValue = map.get("text1");
            multifunctionText.addStyle(vipStyleBuilder.getText() + " " + (TextUtils.isEmpty(titleValue)?"":titleValue),
                    vipStyleBuilder.build());
            mTagTextView.setText(multifunctionText);
        }else{
            WidgetUtility.setTextToView(mTagTextView,map.get("text1"));
        }
    }

    @Override
    public void setListener() {

    }
}
