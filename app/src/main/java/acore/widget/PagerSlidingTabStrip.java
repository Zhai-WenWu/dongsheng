/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package acore.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;

public class PagerSlidingTabStrip extends HorizontalScrollView {
    private static final String TAG = "PagerSlidingTabStrip";

    public static final int DEF_VALUE_TAB_TEXT_ALPHA = 150;
    private static final int[] ANDROID_ATTRS = new int[]{android.R.attr.textColorPrimary, android.R.attr.padding,
            android.R.attr.paddingLeft, android.R.attr.paddingRight, android.R.attr.layout_weight, android.R.attr.layout_height};

    private LinearLayout mTabsContainer;
    private LinearLayout.LayoutParams mTabLayoutParams;

    private final PagerAdapterObserver mAdapterObserver = new PagerAdapterObserver();
    private OnTabReselectedListener mTabReselectedListener = null;
    public OnPageChangeListener mDelegatePageListener;
    private ViewPager mPager;

    private int mTabCount;

    private int mTabColumn = -1;

    private int mCurrentPosition = 0;
    private float mCurrentPositionOffset = 0f;
    private int mDoubleClickInterval = 1000;

    private Paint mRectPaint;
    private Paint mDividerPaint;

    private int mWidth = LayoutParams.WRAP_CONTENT;
    private int mHeight = LayoutParams.MATCH_PARENT;

    private int mTabWidth = 0;
    private int mTabHeight = 0;

    private int mIndicatorColor;
    private int mIndicatorHeight = 2;
    private int mIndicatorPadding = 0;

    private int mUnderlineHeight = 0;
    private int mUnderlineColor;

    private int mDividerWidth = 0;
    private int mDividerPadding = 0;
    private int mDividerColor;

    private int mTabPadding = 0;
    private int mTabTextSize = 14;
    private int mTabSelectedTextSize = -1;
    private ColorStateList mTabTextColor = null;

    private int mPaddingLeft = 50;
    private int mPaddingRight = 0;

    private boolean isExpandTabs = false;
    private boolean isCustomTabs;
    private boolean isPaddingMiddle = false;
    private boolean isTabTextAllCaps = true;
    private boolean isIndicatorWidthFollowText = false;

    private Typeface mTabTextTypeface = null;
    private int mTabTextTypefaceStyle = Typeface.NORMAL;

    private int mScrollOffset;
    private int mLastScrollX = 0;

    private int mTabBackgroundResId = R.drawable.psts_background_tab;
    private int mTabInnerBackground = 0;
    private int mTop, mLeft, mRight, mBottom;
    private int mTextPaddingLeftRight, mTextPaddingTopBottom, mTabItemIntervalSize,
            mTabItemMarginTopBottom, mTabItemStartLeftMargin, mTabItemEndRightMargin;
    private ArrayList<Integer> mTabBackgroundResIds;

    public PagerSlidingTabStrip(Context context) {
        this(context, null);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFillViewport(true);
        setWillNotDraw(false);
        mTabsContainer = new LinearLayout(context);
        mTabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        mTabsContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        addView(mTabsContainer);

        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Style.FILL);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mScrollOffset, dm);
        mIndicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIndicatorHeight, dm);
        mIndicatorPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIndicatorPadding, dm);
        mUnderlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mUnderlineHeight, dm);
        mDividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDividerPadding, dm);
        mTabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mTabPadding, dm);
        mDividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDividerWidth, dm);
        mTabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTabTextSize, dm);
        mTabSelectedTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTabSelectedTextSize, dm);

        mDividerPaint = new Paint();
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setStrokeWidth(mDividerWidth);

        // get system attrs for container
        TypedArray a = context.obtainStyledAttributes(attrs, ANDROID_ATTRS);
        int textPrimaryColor = a.getColor(0, getResources().getColor(android.R.color.black));
        mUnderlineColor = textPrimaryColor;
        mDividerColor = textPrimaryColor;
        mIndicatorColor = textPrimaryColor;
        int padding = a.getDimensionPixelSize(1, 0);
        mPaddingLeft = padding > 0 ? padding : a.getDimensionPixelSize(2, 0);
        mPaddingRight = padding > 0 ? padding : a.getDimensionPixelSize(3, 0);
        mWidth = a.getDimensionPixelSize(4, mWidth);
        mHeight = a.getDimensionPixelSize(5, mHeight);
        a.recycle();

        String tabTextTypefaceName = "sans-serif";
        // Use Roboto Medium as the default typeface from API 21 onwards
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			tabTextTypefaceName = "sans-serif-medium";
//			mTabTextTypefaceStyle = Typeface.NORMAL;
//		}

        // get custom attrs for tabs and container
        a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);
        mTabColumn = a.getInt(R.styleable.PagerSlidingTabStrip_pstsTabColumn, mTabColumn);
        mIndicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor, mIndicatorColor);
        mIndicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight,
                mIndicatorHeight);
        mIndicatorPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorPaddingLeftRight,
                mIndicatorPadding);
        mUnderlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsUnderlineColor, mUnderlineColor);
        mUnderlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight,
                mUnderlineHeight);
        mDividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsDividerColor, mDividerColor);
        mDividerWidth = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerWidth, mDividerWidth);
        mDividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerPadding, mDividerPadding);
        isExpandTabs = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand, isExpandTabs);
        mScrollOffset = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsScrollOffset, mScrollOffset);
        isPaddingMiddle = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsPaddingMiddle, isPaddingMiddle);
        mTabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight, mTabPadding);
        mTabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground, mTabBackgroundResId);
        mTabInnerBackground = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabItemInnerBackground, mTabInnerBackground);
        mTabTextSize = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabTextSize, mTabTextSize);
        mTabSelectedTextSize = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabSelectedTextSize, mTabTextSize);
        mTabTextColor = a.hasValue(R.styleable.PagerSlidingTabStrip_pstsTabTextColor)
                ? a.getColorStateList(R.styleable.PagerSlidingTabStrip_pstsTabTextColor) : null;
        mTabTextTypefaceStyle = a.getInt(R.styleable.PagerSlidingTabStrip_pstsTabTextStyle, mTabTextTypefaceStyle);
        isTabTextAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTabTextAllCaps, isTabTextAllCaps);
        isIndicatorWidthFollowText = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsIndicatorWidthFollowText, isIndicatorWidthFollowText);
        mTextPaddingLeftRight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabTextPaddingLeftRight, mTextPaddingLeftRight);
        mTextPaddingTopBottom = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabTextPaddingTopBottom, mTextPaddingTopBottom);
        mTabItemIntervalSize = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabItemIntervalSize, mTabItemIntervalSize);
        mTabItemMarginTopBottom = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabItemMarginTopBottom, mTabItemMarginTopBottom);
        mTabItemStartLeftMargin = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabStartLeftMargin, mTabItemStartLeftMargin);
        mTabItemEndRightMargin = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabEndRightMargin, mTabItemEndRightMargin);
        int tabTextAlpha = a.getInt(R.styleable.PagerSlidingTabStrip_pstsTabTextAlpha, DEF_VALUE_TAB_TEXT_ALPHA);
        String fontFamily = a.getString(R.styleable.PagerSlidingTabStrip_pstsTabTextFontFamily);
        a.recycle();

        // Tab text color selector
        if (mTabTextColor == null) {
            mTabTextColor = createColorStateList(textPrimaryColor, textPrimaryColor, Color.argb(tabTextAlpha,
                    Color.red(textPrimaryColor), Color.green(textPrimaryColor), Color.blue(textPrimaryColor)));
        }

        // Tab text typeface and style
        if (fontFamily != null) {
            tabTextTypefaceName = fontFamily;
        }
        mTabTextTypeface = Typeface.create(tabTextTypefaceName, mTabTextTypefaceStyle);

        // Bottom padding for the tabs container parent view to show indicator
        // and underline
        setTabsContainerParentViewPaddings();

        // Configure tab's container LayoutParams for either equal divided space
        // or just wrap tabs
        mTabLayoutParams = isExpandTabs ? new LinearLayout.LayoutParams(0, mHeight, 1.0f)
                : new LinearLayout.LayoutParams(mWidth, mHeight);
        if (mTabColumn > 0) {
            WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(displayMetrics);
            int width = dm.widthPixels / mTabColumn;
            mTabLayoutParams = new LinearLayout.LayoutParams(width, mHeight);
            mTabWidth = width;
        }
    }

    private void setTabsContainerParentViewPaddings() {
        int bottomMargin = mIndicatorHeight >= mUnderlineHeight ? mIndicatorHeight : mUnderlineHeight;
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), bottomMargin);
    }

    public void setViewPager(ViewPager pager) {
        this.mPager = pager;
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        isCustomTabs = pager.getAdapter() instanceof CustomTabProvider;
        pager.getAdapter().registerDataSetObserver(mAdapterObserver);
        mAdapterObserver.setAttached(true);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mTabsContainer.removeAllViews();
        if (isCustomTabs) {
            ((CustomTabProvider) mPager.getAdapter()).onRemoveAllTabView();
        }
        mTabCount = mPager.getAdapter().getCount();
        View tabView;
        for (int i = 0; i < mTabCount; i++) {
            if (isCustomTabs) {
                tabView = ((CustomTabProvider) mPager.getAdapter()).getCustomTabView(this, i);
            } else {
                tabView = LayoutInflater.from(getContext()).inflate(R.layout.psts_tab, this, false);
            }
            CharSequence title = mPager.getAdapter().getPageTitle(i);
            addTab(i, title, tabView);
        }

        updateTabStyles();
        requestLayout();
    }

    private void addTab(final int position, CharSequence title, final View tabView) {
        TextView textView = (TextView) tabView.findViewById(R.id.psts_tab_title);
        if (textView != null) {
            if (title != null)
                textView.setText(title);
        }
        textView.setPadding(mTextPaddingLeftRight, mTextPaddingTopBottom, mTextPaddingLeftRight, mTextPaddingTopBottom);
        tabView.setFocusable(true);
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPager.getCurrentItem() != position) {
                    View tab = mTabsContainer.getChildAt(mPager.getCurrentItem());
                    unSelect(tab);
                    mPager.setCurrentItem(position, false);
                } else if (mTabReselectedListener != null) {
                    mTabReselectedListener.onTabReselected(position);
                }
            }
        });
        tabView.setOnTouchListener(new OnTouchListener() {
            int clickCount = 0;
            long firsttime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (clickCount == 0) {
                            clickCount++;
                            firsttime = System.currentTimeMillis();
                        } else if (clickCount == 1) {
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - firsttime <= mDoubleClickInterval) {
                                if (mOnItemDoubleClickListener != null) {
                                    mOnItemDoubleClickListener.onItemDoubleClick(tabView, position);
                                }
                            }
                            clickCount = 0;
                            firsttime = 0;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mWidth, mHeight);
        if (mTabItemMarginTopBottom > 0) {
            params.topMargin = mTabItemMarginTopBottom;
            params.bottomMargin = mTabItemMarginTopBottom;
        }
        if (mTabWidth > 0)
            params.width = mTabWidth;
        if (mTabHeight > 0)
            params.height = mTabHeight;
        if (position == 0 && mTabItemStartLeftMargin > 0) {
            params.leftMargin = mTabItemStartLeftMargin;
        } else if (position == mTabCount - 1 && mTabItemEndRightMargin > 0) {
            params.rightMargin = mTabItemEndRightMargin;
        } else if (mTabItemIntervalSize > 0) {
            params.leftMargin = mTabItemIntervalSize;
        }
        mTabsContainer.addView(tabView, position, params);
    }

    private void updateTabStyles() {
        for (int i = 0; i < mTabCount; i++) {
            View v = mTabsContainer.getChildAt(i);
            if (mTabBackgroundResIds != null) {
                if (mTabBackgroundResIds.size() >= mTabCount)
                    v.setBackgroundResource(mTabBackgroundResIds.get(i));
            } else {
                v.setBackgroundResource(mTabBackgroundResId);
            }
            v.setPadding(mTabPadding, v.getPaddingTop(), mTabPadding, v.getPaddingBottom());
            TextView tab_title = (TextView) v.findViewById(R.id.psts_tab_title);
            if (tab_title != null) {
                tab_title.setTextColor(mTabTextColor);
                tab_title.setTypeface(mTabTextTypeface, mTabTextTypefaceStyle);
                tab_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
                tab_title.setPadding(mLeft > 0 ? mLeft : mTextPaddingLeftRight, mTop > 0 ? mTop : mTextPaddingTopBottom,
                        mRight > 0 ? mRight : mTextPaddingLeftRight, mBottom > 0 ? mBottom : mTextPaddingTopBottom);
                tab_title.setBackgroundResource(mTabInnerBackground);
                // setAllCaps() is only available from API 14, so the upper case
                // is made manually if we are on a
                // pre-ICS-build
                if (isTabTextAllCaps) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        tab_title.setAllCaps(true);
                    } else {
                        tab_title.setText(
                                tab_title.getText().toString().toUpperCase(getResources().getConfiguration().locale));
                    }
                }
            }
        }
    }

    public void updateTabLayoutParams() {
        if (mTabColumn > 0) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(displayMetrics);
            int width = dm.widthPixels / mTabColumn;
            mTabLayoutParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public void scrollToChild(int position, int offset) {
        if (mTabCount == 0) {
            return;
        }

        int newScrollX = mTabsContainer.getChildAt(position).getLeft() - mTabsContainer.getChildAt(0).getLeft() + offset;
        if (position > 0 || offset > 0) {
            // Half screen offset.
            // - Either tabs start at the middle of the view scrolling straight
            // away
            // - Or tabs start at the begging (no padding) scrolling when
            // indicator gets
            // to the middle of the view width
            newScrollX -= mScrollOffset;
            Pair<Float, Float> lines = getIndicatorCoordinates();
            newScrollX += ((lines.second - lines.first) / 2);
        }

        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }
    }

    private Pair<Float, Float> getDrawIndicatorCoordinates() {
        // default: line below current tab
        View currentTab = mTabsContainer.getChildAt(mCurrentPosition);
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();
        if (mTabColumn > 0) {
            TextView text = (TextView) currentTab.findViewById(R.id.psts_tab_title);
            lineRight = text.getRight() + lineLeft;
            lineLeft += text.getLeft();
        }
        // if there is an offset, start interpolating left and right coordinates
        // between current and next tab
        if (mCurrentPositionOffset > 0f && mCurrentPosition < mTabCount - 1) {
            View nextTab = mTabsContainer.getChildAt(mCurrentPosition + 1);
            float nextTabLeft = nextTab.getLeft();// final
            float nextTabRight = nextTab.getRight();
            if (mTabColumn > 0) {
                TextView text = (TextView) nextTab.findViewById(R.id.psts_tab_title);
                nextTabRight = text.getRight() + nextTabLeft;
                nextTabLeft += text.getLeft();
            }
//			Log.d(TAG, "onDraw lineLeft = " + lineLeft + ",lineRight = " + lineRight);
//			Log.d(TAG, "onDraw nextTabLeft = " + nextTabLeft + ",nextTabRight = " + nextTabRight);
            lineLeft = (mCurrentPositionOffset * nextTabLeft + (1f - mCurrentPositionOffset) * lineLeft);
            lineRight = (mCurrentPositionOffset * nextTabRight + (1f - mCurrentPositionOffset) * lineRight);
        }

        return new Pair<Float, Float>(lineLeft, lineRight);
    }

    private Pair<Float, Float> getIndicatorCoordinates() {
        // default: line below current tab
        View currentTab = mTabsContainer.getChildAt(mCurrentPosition);
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();
        // if there is an offset, start interpolating left and right coordinates
        // between current and next tab
        if (mCurrentPositionOffset > 0f && mCurrentPosition < mTabCount - 1) {
            View nextTab = mTabsContainer.getChildAt(mCurrentPosition + 1);
            float nextTabLeft = nextTab.getLeft();// final
            float nextTabRight = nextTab.getRight();
            lineLeft = (mCurrentPositionOffset * nextTabLeft + (1f - mCurrentPositionOffset) * lineLeft);
            lineRight = (mCurrentPositionOffset * nextTabRight + (1f - mCurrentPositionOffset) * lineRight);
        }

        return new Pair<Float, Float>(lineLeft, lineRight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isPaddingMiddle || mPaddingLeft > 0 || mPaddingRight > 0) {
            int width;
            if (isPaddingMiddle) {
                width = getWidth();
            } else {
                // Account for manually set padding for offsetting tab start and
                // end positions.
                width = getWidth() - mPaddingLeft - mPaddingRight;
            }

            // Make sure tabContainer is bigger than the HorizontalScrollView to
            // be able to scroll
            mTabsContainer.setMinimumWidth(width);
            // Clipping padding to false to see the tabs while we pass them
            // swiping
            setClipToPadding(false);
        }

        if (mTabsContainer.getChildCount() > 0) {
            mTabsContainer.getChildAt(0).getViewTreeObserver().addOnGlobalLayoutListener(firstTabGlobalLayoutListener);
        }

        super.onLayout(changed, l, t, r, b);
    }

    private OnGlobalLayoutListener firstTabGlobalLayoutListener = new OnGlobalLayoutListener() {

        @Override
        public void onGlobalLayout() {
            View view = mTabsContainer.getChildAt(0);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                removeGlobalLayoutListenerPreJB();
            } else {
                removeGlobalLayoutListenerJB();
            }

            if (isPaddingMiddle) {
                int mHalfWidthFirstTab = view.getWidth() / 2;
                mPaddingLeft = mPaddingRight = getWidth() / 2 - mHalfWidthFirstTab;
            }

            setPadding(mPaddingLeft, getPaddingTop(), mPaddingRight, getPaddingBottom());
            if (mScrollOffset == 0)
                mScrollOffset = getWidth() / 2 - mPaddingLeft - view.getLeft();
            mCurrentPosition = mPager.getCurrentItem();
            mCurrentPositionOffset = 0f;
            scrollToChild(mCurrentPosition, 0);
            updateSelection(mCurrentPosition);
        }

        @SuppressWarnings("deprecation")
        private void removeGlobalLayoutListenerPreJB() {
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        private void removeGlobalLayoutListenerJB() {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode() || mTabCount == 0) {
            return;
        }

        final int height = getHeight();
        // draw divider
        if (mDividerWidth > 0) {
            mDividerPaint.setStrokeWidth(mDividerWidth);
            mDividerPaint.setColor(mDividerColor);
            for (int i = 0; i < mTabCount - 1; i++) {
                View tab = mTabsContainer.getChildAt(i);
                canvas.drawLine(tab.getRight(), mDividerPadding, tab.getRight(), height - mDividerPadding,
                        mDividerPaint);
            }
        }

        // draw underline
        if (mUnderlineHeight > 0) {
            mRectPaint.setColor(mUnderlineColor);
            canvas.drawRect(mPaddingLeft, height - mUnderlineHeight, mTabsContainer.getWidth() + mPaddingRight, height,
                    mRectPaint);
        }

        // draw indicator line
        if (mIndicatorHeight > 0) {
            mRectPaint.setColor(mIndicatorColor);
            Pair<Float, Float> lines = getDrawIndicatorCoordinates();
            if (isIndicatorWidthFollowText) {
                canvas.drawRect(lines.first + mPaddingLeft + mIndicatorPadding + mTabPadding, height - mIndicatorHeight,
                        lines.second + mPaddingLeft - mIndicatorPadding - mTabPadding, height, mRectPaint);
            } else {
                canvas.drawRect(lines.first + mPaddingLeft + mIndicatorPadding, height - mIndicatorHeight,
                        lines.second + mPaddingLeft - mIndicatorPadding, height, mRectPaint);
            }
        }
    }

    public void setOnTabReselectedListener(OnTabReselectedListener tabReselectedListener) {
        this.mTabReselectedListener = tabReselectedListener;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mDelegatePageListener = listener;
    }

    public void updateSelection(int position) {
        for (int i = 0; i < mTabCount; ++i) {
            View tv = mTabsContainer.getChildAt(i);
            final boolean selected = i == position;
            if (selected) {
                select(tv);
            } else {
                unSelect(tv);
            }
        }
    }

    public void unSelect(View tab) {
        if (tab != null) {
            tab.setSelected(false);
            TextView tab_title = (TextView) tab.findViewById(R.id.psts_tab_title);
            if (tab_title != null) {
                tab_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
            }
            if (isCustomTabs)
                ((CustomTabProvider) mPager.getAdapter()).tabUnselected(tab);
        }
    }

    public void select(View tab) {
        if (tab != null) {
            tab.setSelected(true);
            TextView tab_title = (TextView) tab.findViewById(R.id.psts_tab_title);
            if (tab_title != null) {
                if (mTabSelectedTextSize != 0) {
                    tab_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabSelectedTextSize);
                }
            }
            if (isCustomTabs)
                ((CustomTabProvider) mPager.getAdapter()).tabSelected(tab);
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {

        private boolean attached = false;

        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        public void setAttached(boolean attached) {
            this.attached = attached;
        }

        public boolean isAttached() {
            return attached;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mPager != null) {
            if (!mAdapterObserver.isAttached()) {
                mPager.getAdapter().registerDataSetObserver(mAdapterObserver);
                mAdapterObserver.setAttached(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPager != null) {
            if (mAdapterObserver.isAttached()) {
                mPager.getAdapter().unregisterDataSetObserver(mAdapterObserver);
                mAdapterObserver.setAttached(false);
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPosition = savedState.currentPosition;
        if (mCurrentPosition != 0 && mTabsContainer.getChildCount() > 0) {
            unSelect(mTabsContainer.getChildAt(0));
            select(mTabsContainer.getChildAt(mCurrentPosition));
        }
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = mCurrentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private OnItemDoubleClickListener mOnItemDoubleClickListener;

    public interface OnItemDoubleClickListener {
        public void onItemDoubleClick(View v, int position);
    }

    public void setOnItemDoubleClickListener(OnItemDoubleClickListener listener) {
        this.mOnItemDoubleClickListener = listener;
    }

    public int getIndicatorColor() {
        return this.mIndicatorColor;
    }

    public int getIndicatorHeight() {
        return mIndicatorHeight;
    }

    public int getUnderlineColor() {
        return mUnderlineColor;
    }

    public int getDividerColor() {
        return mDividerColor;
    }

    public int getDividerWidth() {
        return mDividerWidth;
    }

    public int getUnderlineHeight() {
        return mUnderlineHeight;
    }

    public int getDividerPadding() {
        return mDividerPadding;
    }

    public int getScrollOffset() {
        return mScrollOffset;
    }

    public boolean getShouldExpand() {
        return isExpandTabs;
    }

    public int getTextSize() {
        return mTabTextSize;
    }

    public boolean isTextAllCaps() {
        return isTabTextAllCaps;
    }

    public ColorStateList getTextColor() {
        return mTabTextColor;
    }

    public int getTabBackground() {
        return mTabBackgroundResId;
    }

    public int getTabPaddingLeftRight() {
        return mTabPadding;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorColorResource(int resId) {
        this.mIndicatorColor = getResources().getColor(resId);
        invalidate();
    }

    public void setIndicatorHeight(int indicatorLineHeightPx) {
        this.mIndicatorHeight = indicatorLineHeightPx;
        invalidate();
    }

    public void setUnderlineColor(int underlineColor) {
        this.mUnderlineColor = underlineColor;
        invalidate();
    }

    public void setUnderlineColorResource(int resId) {
        this.mUnderlineColor = getResources().getColor(resId);
        invalidate();
    }

    public void setDividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
        invalidate();
    }

    public void setDividerColorResource(int resId) {
        this.mDividerColor = getResources().getColor(resId);
        invalidate();
    }

    public void setDividerWidth(int dividerWidthPx) {
        this.mDividerWidth = dividerWidthPx;
        invalidate();
    }

    public void setUnderlineHeight(int underlineHeightPx) {
        this.mUnderlineHeight = underlineHeightPx;
        invalidate();
    }

    public void setDividerPadding(int dividerPaddingPx) {
        this.mDividerPadding = dividerPaddingPx;
        invalidate();
    }

    public void setScrollOffset(int scrollOffsetPx) {
        this.mScrollOffset = scrollOffsetPx;
        invalidate();
    }

    public void setShouldExpand(boolean shouldExpand) {
        this.isExpandTabs = shouldExpand;
        if (mPager != null) {
            requestLayout();
        }
    }

    public void setAllCaps(boolean textAllCaps) {
        this.isTabTextAllCaps = textAllCaps;
    }

    public void setTextSize(int textSizePx) {
        this.mTabTextSize = textSizePx;
        updateTabStyles();
    }

    public void setTextColorResource(int resId) {
        setTextColor(getResources().getColor(resId));
    }

    public void setTextColor(int textColor) {
        setTextColor(createColorStateList(textColor));
    }

    public void setTextColorStateListResource(int resId) {
        setTextColor(getResources().getColorStateList(resId));
    }

    public void setTextColor(ColorStateList colorStateList) {
        this.mTabTextColor = colorStateList;
        updateTabStyles();
    }

    private ColorStateList createColorStateList(int color_state_default) {
        return new ColorStateList(new int[][]{new int[]{} // default
        }, new int[]{color_state_default // default
        });
    }

    private ColorStateList createColorStateList(int color_state_pressed, int color_state_selected,
                                                int color_state_default) {
        return new ColorStateList(new int[][]{new int[]{android.R.attr.state_pressed}, // pressed
                new int[]{android.R.attr.state_selected}, // enabled
                new int[]{} // default
        }, new int[]{color_state_pressed, color_state_selected, color_state_default});
    }

    public void setTypeface(Typeface typeface, int style) {
        this.mTabTextTypeface = typeface;
        this.mTabTextTypefaceStyle = style;
        updateTabStyles();
    }

    public void setTabBackground(int resId) {
        this.mTabBackgroundResId = resId;
    }

    public void setTabBackground(ArrayList<Integer> resIds) {
        this.mTabBackgroundResIds = resIds;
    }

    public void setTabTextPaddingLeftRight(int resId) {
        this.mTextPaddingLeftRight = getResources().getDimensionPixelSize(resId);
    }

    public void setTabTextPaddingTopBottom(int resId) {
        this.mTextPaddingTopBottom = getResources().getDimensionPixelSize(resId);
    }

    public void setTabItemIntervalSize(int resId) {
        this.mTabItemIntervalSize = getResources().getDimensionPixelSize(resId);
    }

    public void setTabItemMarginTopBottom(int resId) {
        this.mTabItemMarginTopBottom = getResources().getDimensionPixelSize(resId);
    }

    public void setTabStartLeftMargin(int resId) {
        this.mTabItemStartLeftMargin = getResources().getDimensionPixelSize(resId);
    }

    public void setTabEndRightMargin(int resId) {
        this.mTabItemEndRightMargin = getResources().getDimensionPixelSize(resId);
    }

    public void setTabInnerBackground(int resId) {
        this.mTabInnerBackground = resId;
    }

    public void setPagerSlidingHeight(int height) {
        this.mHeight = height;
    }

    public void setTabWidth(int width) {
        this.mTabWidth = width;
    }

    public void setTabHeight(int height) {
        this.mTabHeight = height;
    }

    public void setTabInnerTextPadding(int left, int top, int right, int bottom) {
        this.mLeft = left;
        this.mTop = top;
        this.mRight = right;
        this.mBottom = bottom;
    }

    public void setTabPaddingLeftRight(int paddingPx) {
        this.mTabPadding = paddingPx;
        updateTabStyles();
    }

    public interface CustomTabProvider {
        void onRemoveAllTabView();

        View getCustomTabView(ViewGroup parent, int position);

        void tabSelected(View tab);

        void tabUnselected(View tab);
    }

    public interface OnTabReselectedListener {
        void onTabReselected(int position);
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(int mCurrentPosition) {
        this.mCurrentPosition = mCurrentPosition;
    }

    public float getmCurrentPositionOffset() {
        return mCurrentPositionOffset;
    }

    public void setmCurrentPositionOffset(float mCurrentPositionOffset) {
        this.mCurrentPositionOffset = mCurrentPositionOffset;
    }

    public int getmTabCount() {
        return mTabCount;
    }

    public void setmTabCount(int mTabCount) {
        this.mTabCount = mTabCount;
    }

    public LinearLayout getmTabsContainer() {
        return mTabsContainer;
    }

    public void setmTabsContainer(LinearLayout mTabsContainer) {
        this.mTabsContainer = mTabsContainer;
    }

    public OnPageChangeListener getmDelegatePageListener() {
        return mDelegatePageListener;
    }

    public void setmDelegatePageListener(OnPageChangeListener mDelegatePageListener) {
        this.mDelegatePageListener = mDelegatePageListener;
    }

    public void setTabColumn(int column) {
        this.mTabColumn = column;
    }

    public int getTabColumn() {
        return mTabColumn;
    }

    public void setDoubleClickInterval(int intervalTime) {
        this.mDoubleClickInterval = intervalTime;
    }

    public int getDoubleClickInterval() {
        return mDoubleClickInterval;
    }

    /**
     * 设置监听
     */
    public void setListener() {
        mPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //PagerSlidingTabStrip的滑动
                PagerSlidingTabStrip.this.setCurrentPosition(position);
                PagerSlidingTabStrip.this.setmCurrentPositionOffset(positionOffset);
                int offset = PagerSlidingTabStrip.this.getmTabCount() > 0 ? (int) (positionOffset * PagerSlidingTabStrip.this.getmTabsContainer().getChildAt(position).getWidth()) : 0;
                PagerSlidingTabStrip.this.scrollToChild(position, offset);
                PagerSlidingTabStrip.this.invalidate();
                if (PagerSlidingTabStrip.this.mDelegatePageListener != null) {
                    PagerSlidingTabStrip.this.mDelegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(int position) {
                PagerSlidingTabStrip.this.updateSelection(position);
                if (PagerSlidingTabStrip.this.mDelegatePageListener != null) {
                    PagerSlidingTabStrip.this.mDelegatePageListener.onPageSelected(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    PagerSlidingTabStrip.this.scrollToChild(mPager.getCurrentItem(), 0);
                }
                // Full tabTextAlpha for current item
                View currentTab = PagerSlidingTabStrip.this.getmTabsContainer().getChildAt(mPager.getCurrentItem());
                PagerSlidingTabStrip.this.select(currentTab);
                // Half transparent for prev item
                if (mPager.getCurrentItem() - 1 >= 0) {
                    View prevTab = PagerSlidingTabStrip.this.getmTabsContainer().getChildAt(mPager.getCurrentItem() - 1);
                    PagerSlidingTabStrip.this.unSelect(prevTab);
                }

                // Half transparent for next item
                if (mPager.getCurrentItem() + 1 <= mPager.getAdapter().getCount() - 1) {
                    View nextTab = PagerSlidingTabStrip.this.getmTabsContainer().getChildAt(mPager.getCurrentItem() + 1);
                    PagerSlidingTabStrip.this.unSelect(nextTab);
                }

                if (PagerSlidingTabStrip.this.mDelegatePageListener != null) {
                    PagerSlidingTabStrip.this.mDelegatePageListener.onPageScrollStateChanged(state);
                }
            }
        });
    }
}
