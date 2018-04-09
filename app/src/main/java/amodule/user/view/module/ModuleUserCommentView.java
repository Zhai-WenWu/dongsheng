package amodule.user.view.module;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.widget.ProperRatingBar;
import amodule._common.utility.WidgetUtility;

import static acore.tools.Tools.parseIntOfThrow;


/**
 * 用户信息
 */
public class ModuleUserCommentView extends ModuleBaseView{

    static final int LAYOUT_ID = R.layout.module_user_comment_view;

    private ImageView auther_userImg;
    private TextView auther_name,right_text;
    private ProperRatingBar mRatingBar;
    private String userUrl="";//用户点击跳转url
    public ModuleUserCommentView(Context context) {
        super(context,LAYOUT_ID);
    }

    public ModuleUserCommentView(Context context, AttributeSet attrs) {
        super(context, attrs, LAYOUT_ID);
    }

    public ModuleUserCommentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr,LAYOUT_ID);
    }
    @Override
    public void initUI() {
        setMODULE_TAG("A2");
        auther_userImg= (ImageView) findViewById(R.id.auther_userImg);
        auther_name= (TextView) findViewById(R.id.auther_name);
        right_text= (TextView) findViewById(R.id.right_text);
        mRatingBar = (ProperRatingBar) findViewById(R.id.rating_bar);
    }
    @Override
    public void initData(Map<String, String> map) {
        //用户信息
        if(map.containsKey("userView")&&!TextUtils.isEmpty(map.get("userView"))) {
            Map<String,String> mapUser= StringManager.getFirstMap(map.get("userView"));
            if (mapUser != null && !mapUser.isEmpty()) {
                auther_name.setText(mapUser.get("nickName"));
                findViewById(R.id.cusType).setVisibility(mapUser.containsKey("iconGourmet") && "2".equals(mapUser.get("iconGourmet")) ? VISIBLE : GONE);
                if (mapUser.containsKey("img") && !TextUtils.isEmpty(mapUser.get("img")))
                    setViewImage(auther_userImg, mapUser.get("img"));
                else {
                    auther_userImg.setVisibility(INVISIBLE);
                }
                userUrl = mapUser.containsKey("url")&& !TextUtils.isEmpty(mapUser.get("url")) ? mapUser.get("url") : "";
            }
            //url点击跳转
        }
        WidgetUtility.setTextToView(right_text,map.get("rightTxt"));
        String starCountValue = map.get("starCount");
        int defaultValue = 5;
        int starCount = parseIntOfThrow(starCountValue, defaultValue);
        mRatingBar.setRating(starCount);
        setListener();
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        super.setOnLongClickListener(l);
        findViewById(R.id.auther_userImg).setOnLongClickListener(l);
        findViewById(R.id.auther_name).setOnLongClickListener(l);
    }

    @Override
    public void setListener() {
        auther_userImg.setOnClickListener(UserOnClickListener);
        auther_name.setOnClickListener(UserOnClickListener);
    }

    /**
     * 用户位置点击监听
     */
    private OnClickListener UserOnClickListener= v -> {
        if(!TextUtils.isEmpty(userUrl)){
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),userUrl,false);
            if(TextUtils.isEmpty(getStatisticId())&&mContext!=null) XHClick.mapStat(mContext,getStatisticId(),"点击头像","");
        }
    };
}
