package amodule.dish.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.ImageViewVideo;
import amodule.main.view.item.BaseLinearItemView;

public class DishRelatedRecommendItemView extends BaseLinearItemView implements View.OnClickListener{

    private DishGridDialog.OnItemClickCallback mClickCallback;

    private View mTopLine;
    private ImageViewVideo mImageViewVideo;
    private TextView mDishNameText;
    private TextView mDishDescText;
    private TextView mDishUserNameText;
    private TextView mDishTextView1;
    private TextView mDishTextView2;

    private boolean mIsVideo;

    public DishRelatedRecommendItemView(Context context) {
        super(context);
        initView(context);
    }

    public DishRelatedRecommendItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DishRelatedRecommendItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.dish_related_recommend_item, this);
        mTopLine = findViewById(R.id.v_top_line);
        mImageViewVideo = (ImageViewVideo) findViewById(R.id.img);
        mDishNameText = (TextView) findViewById(R.id.tv_caipu_name);
        mDishDescText = (TextView) findViewById(R.id.desc);
        mDishUserNameText = (TextView) findViewById(R.id.user_name);
        mDishTextView1 = (TextView) findViewById(R.id.text1);
        mDishTextView2 = (TextView) findViewById(R.id.text2);
        setOnClickListener(this);
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null || mDataMap.isEmpty())
            return;
        mTopLine.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        setText(mDishNameText, getValueByKey("name"));
        setText(mDishDescText, getValueByKey("burdens"));
        Map<String, String> customerMap = StringManager.getFirstMap(mDataMap.get("customer"));
        setText(mDishUserNameText, customerMap.get("nickName"));
        setText(mDishTextView1, handleNumber(getValueByKey("allClick")) + "浏览");
        setText(mDishTextView2, handleNumber(getValueByKey("favorites")) + "收藏");
        mIsVideo = TextUtils.equals(getValueByKey("hasVideo"), "2");
        mImageViewVideo.playImgWH = Tools.getDimen(getContext(), R.dimen.dp_34);
        mImageViewVideo.parseItemImg(getValueByKey("img"), mIsVideo, true);
    }

    private String getValueByKey(String key) {
        return mDataMap.get(key);
    }

    private void setText(TextView textView, String text) {
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        String url = getValueByKey("link");
        if (!TextUtils.isEmpty(url))
            AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, true);
        if (mClickCallback != null)
            mClickCallback.onItemClick(v, mPosition, mDataMap);
    }

    public void setOnClickCallback(DishGridDialog.OnItemClickCallback callback) {
        mClickCallback = callback;
    }
}
