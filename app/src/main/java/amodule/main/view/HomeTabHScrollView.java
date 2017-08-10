package amodule.main.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.Tools;
import amodule.main.bean.HomeModuleBean;

/**
 * Created by sll on 2017/4/18.
 */

public class HomeTabHScrollView extends HorizontalScrollView {

    private LinearLayout mTabContainer;
    private ArrayList<Map<String, String>> mDatas;
    private HomeDataChangeCallBack mDataChangeCallBack;

    private String mSelectedType;

    private View mLastSelected;

    private HomeModuleBean mModuleBean;

    private boolean mIsDay;

    public HomeTabHScrollView(Context context) {
        super(context);
        addContentView();
    }

    public HomeTabHScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addContentView();
    }

    public HomeTabHScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addContentView();
    }

    private void addContentView() {
        this.setHorizontalScrollBarEnabled(false);
        mTabContainer = new LinearLayout(getContext());
        mTabContainer.setOrientation(LinearLayout.HORIZONTAL);
        mTabContainer.setBackgroundColor(Color.parseColor("#ffffff"));
        this.addView(mTabContainer);
    }

    public void setHomeModuleBean(HomeModuleBean bean) {
        mModuleBean = bean;
        if (bean != null && !TextUtils.isEmpty(bean.getTwoType()))
            mSelectedType = bean.getTwoType();
    }

    public void setData(ArrayList<Map<String, String>> listDatas) {
        if (mDatas != null)
            mDatas.clear();
        if (mTabContainer.getChildCount() > 0)
            mTabContainer.removeAllViews();
        mDatas = listDatas;
        if (mDatas == null || mDatas.size() < 1 || mTabContainer == null)
            return;
        if (mModuleBean != null && !TextUtils.isEmpty(mModuleBean.getType()) && "day".equals(mModuleBean.getType()))
            mIsDay = true;
        addItemView();
    }

    private void addItemView() {
        //尺寸的设置是临时数据，需要设计定规则
        if (mIsDay) {
            mTabContainer.setPadding(0, 0, 0, 0);
            int phoneWidth = Tools.getPhoneWidth();
            int imgWidthPx = 375;
            int imgHeightPx = 245;
            int width = phoneWidth / 3;
            int height = (imgHeightPx * width) / imgWidthPx;
            for (int i = 0; i < mDatas.size(); i++) {
                Map<String, String> map = mDatas.get(i);
                if (map != null && map.size() > 0) {
                    RelativeLayout container = new RelativeLayout(getContext());
                    container.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                    final ImageView imageView = new ImageView(getContext());
                    switch (i) {
                        case 0:
                            imageView.setImageResource(R.drawable.selector_hometabscroll_item_bg_morning);
                            break;
                        case 1:
                            imageView.setImageResource(R.drawable.selector_hometabscroll_item_bg_afternoon);
                            break;
                        case 2:
                            imageView.setImageResource(R.drawable.selector_hometabscroll_item_bg_evening);
                            break;
                    }

                    imageView.setTag(map);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    if (map.containsKey("two_type")) {
                        String two_type = map.get("two_type");
                        if (!TextUtils.isEmpty(two_type) && !TextUtils.isEmpty(mSelectedType) && two_type.equals(mSelectedType)) {
                            imageView.setSelected(true);
                            mLastSelected = imageView;
                            setStatistics(imageView);
                        }
                    }
                    imageView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (mLastSelected == imageView)
                                return;
                            if (mLastSelected != null)
                                mLastSelected.setSelected(false);
                            mLastSelected = imageView;
                            imageView.setSelected(true);
                            if (mDataChangeCallBack != null)
                                mDataChangeCallBack.indexChanged((Map<String, String>) imageView.getTag());
                            setStatistics(imageView);
                        }
                    });
                    RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(width, height);
                    container.addView(imageView, relativeLayoutParams);
                    mTabContainer.addView(container, layoutParams);
                }
            }
            if (mTabContainer.getChildCount() > 0) {
                if (getVisibility() != View.VISIBLE)
                    this.setVisibility(View.VISIBLE);
                if (mLastSelected == null) {
                    RelativeLayout relativeLayout = (RelativeLayout) mTabContainer.getChildAt(0);
                    ImageView imageView = (ImageView) relativeLayout.getChildAt(0);
                    mLastSelected = imageView;
                    mLastSelected.setSelected(true);
                    setStatistics(imageView);
                }
            }
        } else {
            int firstLeftMargin = getDimensionPixelSize(R.dimen.dp_20);
            int leftMargin = getDimensionPixelSize(R.dimen.dp_3);
            int lastRightMargin = getDimensionPixelSize(R.dimen.dp_20);
            for (int i = 0; i < mDatas.size(); i++) {
                Map<String, String> map = mDatas.get(i);
                if (map != null && map.size() > 0) {
                    final RelativeLayout container = new RelativeLayout(getContext());
                    container.setPadding(0, getDimensionPixelSize(R.dimen.dp_12), 0, getDimensionPixelSize(R.dimen.dp_20));
                    TextView textView = new TextView(getContext());
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    textView.setText(map.get("title"));
                    textView.setTextColor(getResources().getColorStateList(R.color.selector_hometabscroll_item_textcolor));
                    textView.setBackgroundResource(R.drawable.selector_hometabscroll_item_bg);
                    textView.setGravity(Gravity.CENTER);
                    textView.setPadding(getDimensionPixelSize(R.dimen.dp_13), getDimensionPixelSize(R.dimen.dp_5), getDimensionPixelSize(R.dimen.dp_13), getDimensionPixelSize(R.dimen.dp_5));
                    container.addView(textView);
                    container.setTag(map);
                    container.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (mLastSelected == v)
                                return;
                            if (mLastSelected != null)
                                mLastSelected.setSelected(false);
                            mLastSelected = v;
                            container.setSelected(true);
                            if (mDataChangeCallBack != null)
                                mDataChangeCallBack.indexChanged((Map<String, String>) container.getTag());
                            setStatistics(container);
                        }
                    });
                    if (map.containsKey("two_type")) {
                        String two_type = map.get("two_type");
                        if (!TextUtils.isEmpty(two_type) && !TextUtils.isEmpty(mSelectedType) && two_type.equals(mSelectedType)) {
                            container.setSelected(true);
                            mLastSelected = container;
                            setStatistics(container);
                        }
                    }
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    if (i == 0) {
                        layoutParams.leftMargin = firstLeftMargin;
                    } else {
                        layoutParams.leftMargin = leftMargin;
                        if (i == (mDatas.size() - 1))
                            layoutParams.rightMargin = lastRightMargin;
                    }
                    mTabContainer.addView(container, layoutParams);
                }
            }
            if (mTabContainer.getChildCount() > 0) {
                if (getVisibility() != View.VISIBLE)
                    this.setVisibility(View.VISIBLE);
                if (mLastSelected == null) {
                    RelativeLayout relativeLayout = (RelativeLayout) mTabContainer.getChildAt(0);
                    mLastSelected = relativeLayout;
                    mLastSelected.setSelected(true);
                    setStatistics(relativeLayout);
                }
            }
        }
    }

    public void setCallback(HomeDataChangeCallBack callback) {
        mDataChangeCallBack = callback;
    }

    private int getDimensionPixelSize(int id) {
        return getResources().getDimensionPixelSize(id);
    }
    public interface HomeDataChangeCallBack {
        /**
         * 被点击数据体
         * @param map
         */
        void indexChanged(Map<String, String> map);
    }

    /**
     * 设置二级分类的统计
     * @param tagView 被选中的带有tag信息的View
     */
    private void setStatistics(View tagView) {
        if (mModuleBean != null && tagView != null) {
            String type = mModuleBean.getType();
            if (!TextUtils.isEmpty(type)) {
                String eventId = "";
                String twoLevel = "";
                String threeLevel = "";
                switch (type) {
                    case "video":
                        eventId = "a_video";
                        twoLevel = "视频分类标签点击/切换量";
                        threeLevel = "点击" + (Integer.parseInt(((Map) tagView.getTag()).get("position").toString()) + 1) + "位置";
                        break;
                    case "day":
                        eventId = "a_meals_recommend";
                        twoLevel = "早中晚餐标签点击";
                        threeLevel = "点击" + ((Map) tagView.getTag()).get("title");
                        break;
                }
                XHClick.mapStat((Activity) getContext(), eventId, twoLevel, threeLevel);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ViewParent parent = getParent();
        if (parent == null)
            return super.dispatchTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                parent.requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                parent.requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
