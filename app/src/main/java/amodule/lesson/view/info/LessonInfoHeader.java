package amodule.lesson.view.info;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule._common.delegate.IBindMap;
import amodule._common.utility.WidgetUtility;
import aplug.basic.LoadImage;

/**
 * Description :
 * PackageName : amodule.vip.view
 * Created by tanze on 2018/3/29 17:11.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonInfoHeader extends RelativeLayout implements IBindMap {

    private LayoutInflater mInflater;

    private ImageView mBackImageView;
    private TextView mTitle, mDescription;

    private LinearLayout mScoreLayout, mLessonNumLayout, mUpdateLayout;
    private View mInfoLine_1, mInfoLine_2;
    private TextView mScoreText, mScoreSuffixText;
    private TextView mLessonNumText, mLessonNumSuffixText;
    private TextView mUpdateText, mUpdateSuffixText;

    private LinearLayout mLearnedUserLayout;
    private TextView mLearnedDescText;

    private int imageHeight;

    public LessonInfoHeader(Context context) {
        this(context, null);
    }

    public LessonInfoHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LessonInfoHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeUI();
    }

    private void initializeUI() {
        mInflater = LayoutInflater.from(getContext());
        mInflater.inflate(R.layout.lesson_info_header, this);
        RelativeLayout imageLayout = (RelativeLayout) findViewById(R.id.image_layout);
        //设置图片高度
        imageHeight = (int) (ToolsDevice.getWindowPx(getContext()).widthPixels * 500 / 750f);
        imageLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imageHeight));
        mBackImageView = (ImageView) findViewById(R.id.background);
        mTitle = (TextView) findViewById(R.id.title);
        mDescription = (TextView) findViewById(R.id.description);

        mScoreLayout = (LinearLayout) findViewById(R.id.score_layout);
        mScoreText = (TextView) findViewById(R.id.score);
        mScoreSuffixText = (TextView) findViewById(R.id.score_suffix);

        mInfoLine_1 = findViewById(R.id.line);

        mLessonNumLayout = (LinearLayout) findViewById(R.id.lesson_num_layout);
        mLessonNumText = (TextView) findViewById(R.id.lesson_num);
        mLessonNumSuffixText = (TextView) findViewById(R.id.lesson_num_suffix);

        mInfoLine_2 = findViewById(R.id.line2);

        mUpdateLayout = (LinearLayout) findViewById(R.id.update_layout);
        mUpdateText = (TextView) findViewById(R.id.update_time);
        mUpdateSuffixText = (TextView) findViewById(R.id.update_time_suffix);

        mLearnedUserLayout = (LinearLayout) findViewById(R.id.learned_user);
        mLearnedDescText = (TextView) findViewById(R.id.learned_desc);
        setVisibility(VISIBLE);
    }

    @Override
    public void setData(Map<String, String> data) {
        Log.d("tzy", "setData: " + data);
        if(data == null || data.isEmpty()){
            return;
        }
        showImage(data.get("img"));
        WidgetUtility.setTextToView(mTitle,data.get("name"));
        WidgetUtility.setTextToView(mDescription,data.get("desc"));
        boolean isShowDescriptionLayout = mTitle.getVisibility() == VISIBLE || mDescription.getVisibility() == VISIBLE;
        findViewById(R.id.description_layout).setVisibility(isShowDescriptionLayout?VISIBLE:GONE);

        showScore(StringManager.getFirstMap(data.get("score")));

        showLessonDesc(StringManager.getFirstMap(data.get("lessonDesc")));

        showUpdateDesc(StringManager.getFirstMap(data.get("updateDesc")));

        isShowLine(mScoreLayout,mLessonNumLayout,mInfoLine_1);
        isShowLine(mLessonNumLayout,mUpdateLayout,mInfoLine_2);
        //显示已学部分
        Map<String,String> learnedDescmap = StringManager.getFirstMap(data.get("learnedDesc"));
        WidgetUtility.setTextToView(mLearnedDescText,getStringValue(learnedDescmap));
        showLearnedUser(StringManager.getListMapByJson(data.get("learnedUser")));
    }

    private void showImage(String imageUrl) {
        if(TextUtils.isEmpty(imageUrl)){
            return;
        }
        LoadImage.with(getContext()).load(imageUrl).build().into(mBackImageView);
    }

    private void showScore(Map<String, String> data) {
        WidgetUtility.setTextToView(mScoreText,data.get("text1"));
        WidgetUtility.setTextToView(mScoreSuffixText,data.get("text2"));
        boolean isShow = mScoreText.getVisibility() == VISIBLE || mScoreSuffixText.getVisibility() == VISIBLE;
        mScoreLayout.setVisibility(isShow?VISIBLE:GONE);
    }

    private void showLessonDesc(Map<String, String> data) {
        WidgetUtility.setTextToView(mLessonNumText,data.get("text1"));
        WidgetUtility.setTextToView(mLessonNumSuffixText,data.get("text2"));
        boolean isShow = mLessonNumText.getVisibility() == VISIBLE || mLessonNumSuffixText.getVisibility() == VISIBLE;
        mLessonNumLayout.setVisibility(isShow?VISIBLE:GONE);
    }

    private void showUpdateDesc(Map<String, String> data) {
        WidgetUtility.setTextToView(mUpdateText,data.get("text1"));
        WidgetUtility.setTextToView(mUpdateSuffixText,data.get("text2"));
        boolean isShow = mUpdateText.getVisibility() == VISIBLE || mUpdateSuffixText.getVisibility() == VISIBLE;
        mUpdateLayout.setVisibility(isShow?VISIBLE:GONE);
    }

    private void isShowLine(@NonNull LinearLayout layoutBefore,@NonNull LinearLayout layoutAfter, @NonNull View line) {
        boolean isShow = layoutBefore.getVisibility() == VISIBLE && layoutAfter.getVisibility() == VISIBLE;
        line.setVisibility(isShow?VISIBLE:GONE);
    }

    private void showLearnedUser(List<Map<String, String>> array) {
        if(array == null || array.isEmpty()){
            findViewById(R.id.learned_layout).setVisibility(GONE);
            return;
        }
        if(mLearnedUserLayout != null && mLearnedUserLayout.getChildCount() > 0){
            mLearnedUserLayout.removeAllViews();
        }
        int maxWidth = ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(), R.dimen.dp_40)
                - Tools.getMeasureWidth(mLearnedDescText);
        final int maxLength = maxWidth / Tools.getDimen(getContext(), R.dimen.dp_30);
        for (int index = 0; index < array.size() && index < maxLength; index++) {
            Map<String, String> value = array.get(index);
            View view = createView(value.get("img"));
            if (view != null) {
                mLearnedUserLayout.addView(view);
            }
        }
        if(mLearnedUserLayout.getChildCount() > 0){
            findViewById(R.id.learned_layout).setVisibility(VISIBLE);
        }
    }

    @Nullable
    private View createView(@NonNull String imgUrl) {
        if (TextUtils.isEmpty(imgUrl))
            return null;
        @SuppressLint("InflateParams")
        View view = mInflater.inflate(R.layout.item_learned_user, null, true);
        ImageView image = (ImageView) view.findViewById(R.id.user_img);
        LoadImage.with(getContext())
                .load(imgUrl)
                .setPlaceholderId(R.drawable.z_me_head)
                .setErrorId(R.drawable.z_me_head)
                .setImageRound(500)
                .build()
                .into(image);
        return view;
    }

    private String getStringValue(Map<String,String> data){
        if(data == null || data.isEmpty()){
            return "";
        }
        String text1 = data.get("text1");
        String text2 = data.get("text2");
        String result = appendNonNull("", text1);
        result = appendNonNull(result, text2);
        return result;
    }

    private String appendNonNull(String result, String appendStr) {
        if(!TextUtils.isEmpty(appendStr)){
            return result + appendStr;
        }
        return result;
    }

    public int getImageHeight() {
        return imageHeight;
    }
}
