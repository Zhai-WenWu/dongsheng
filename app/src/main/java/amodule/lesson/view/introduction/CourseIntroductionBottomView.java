package amodule.lesson.view.introduction;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.tools.ColorUtil;
import acore.widget.TagTextView;

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2018/12/5 17:40.
 * e_mail : ztanzeyu@gmail.com
 */
public class CourseIntroductionBottomView extends RelativeLayout {
    final int LAYOUT_ID = R.layout.layout_course_bottom;
    final String MOUDLE_NAME = "底部view";
    private ImageView mFavIcon;
    private TextView mFavText;
    private TagTextView mVipButton;
    private LinearLayout mUploadButton;

    public CourseIntroductionBottomView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public CourseIntroductionBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public CourseIntroductionBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(LAYOUT_ID, this);
        mFavIcon = findViewById(R.id.fav_icon);
        switchFavStatus(false);
        mFavText = findViewById(R.id.fav_text);
        mVipButton = findViewById(R.id.vip_button);
        mUploadButton = findViewById(R.id.upload_button);

        OnClickListener favClick = new OnClickListenerStat(getContext(), MOUDLE_NAME, "收藏") {
            @Override
            public void onClicked(View v) {
                if (mFavClick != null) {
                    mFavClick.onClick(v);
                }
                switchFavStatus(!mFavIcon.isSelected());
            }
        };
        mFavIcon.setOnClickListener(favClick);
        mFavText.setOnClickListener(favClick);
    }

    private void switchFavStatus(boolean b) {
        mFavIcon.setSelected(b);
    }

    String vipUrl;
    public void setVipButtonData(Map<String, String> data) {
        if(data == null || data.isEmpty()){
            return;
        }
        vipUrl = data.get("url");
        if(!TextUtils.isEmpty(data.get("text"))){
            mVipButton.setText(data.get("text"));
        }
        if(!TextUtils.isEmpty(data.get("color"))){
            mVipButton.setTextColor(ColorUtil.parseColor(data.get("color"),Color.WHITE));
        }
        if(!TextUtils.isEmpty(data.get("bgColor"))){
            mVipButton.setBackgroundColor(ColorUtil.parseColor(data.get("bgColor"),ColorUtil.parseColor("#FA273B")));
        }
    }

    private OnClickListener mFavClick;

    public void setFavClickListener(OnClickListener listener) {
        mFavClick = listener;
    }

    public void showVipButton(boolean isVisibility) {
        mVipButton.setVisibility(isVisibility ? VISIBLE : GONE);
    }

    public void showUploadButton(boolean isVisibility) {
        mUploadButton.setVisibility(isVisibility ? VISIBLE : GONE);
    }

    public void setVIPClickListener(OnClickListener listener) {
        mVipButton.setOnClickListener(new OnClickListenerStat(MOUDLE_NAME) {
            @Override
            public void onClicked(View v) {
                AppCommon.openUrl(vipUrl,true);
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
    }

    public void setUploadClickListener(OnClickListener listener) {
        mUploadButton.setOnClickListener(new OnClickListenerStat(getContext(), MOUDLE_NAME, "上传作品") {
            @Override
            public void onClicked(View v) {
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
    }
}
