package amodule.lesson.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;

import acore.logic.stat.intefaces.OnClickListenerStat;
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
        LayoutInflater.from(context).inflate(LAYOUT_ID,this);
        mFavIcon = findViewById(R.id.fav_icon);
        switchFavStatus(false);
        mFavText = findViewById(R.id.fav_text);
        mVipButton = findViewById(R.id.vip_button);
        mUploadButton = findViewById(R.id.upload_button);

        OnClickListener favClick = new OnClickListenerStat(getContext(),MOUDLE_NAME,"收藏") {
            @Override
            public void onClicked(View v) {
                switchFavStatus(!mFavIcon.isSelected());
            }
        };
        mFavIcon.setOnClickListener(favClick);
        mFavText.setOnClickListener(favClick);

        mVipButton.setOnClickListener(new OnClickListenerStat(MOUDLE_NAME) {
            @Override
            public void onClicked(View v) {
                //TODO
                Toast.makeText(context, "VIP Button", Toast.LENGTH_SHORT).show();
            }
        });
        mUploadButton.setOnClickListener(new OnClickListenerStat(getContext(),MOUDLE_NAME,"上传作品") {
            @Override
            public void onClicked(View v) {
                //TODO
                Toast.makeText(context, "上传作品", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchFavStatus(boolean b) {
        mFavIcon.setSelected(b);
    }


}
