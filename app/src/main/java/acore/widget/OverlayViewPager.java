package acore.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class OverlayViewPager extends ViewPager {

    public OverlayViewPager(@NonNull Context context) {
        super(context);
    }

    public OverlayViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param overlayPagerAdapter
     */
    public void init(Adapter overlayPagerAdapter) {
        init(overlayPagerAdapter, null);
    }

    public void init(Adapter overlayPagerAdapter, PageChangeListener pageChangeListener) {
        initView(overlayPagerAdapter, pageChangeListener);
    }

    public void initView(final Adapter overlayPagerAdapter, final PageChangeListener pageChangeListener) {
        if (overlayPagerAdapter == null || overlayPagerAdapter.getmData() == null)
            return;

        setOffscreenPageLimit(2);
        setAdapter(overlayPagerAdapter);
        setCurrentItem(getStartPageIndex(overlayPagerAdapter.getmData().size(), overlayPagerAdapter));
        setPageTransformer(true, CardPageTransformer.getBuild()//建造者模式
                .setViewType(PageTransformerConfig.LEFT)
                .setTranslationOffset(20)
                .setScaleOffset(10)
                .create(this));

        if (pageChangeListener != null) {
            addOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                @Override
                public void onPageSelected(int i) {
                    int i1 = i % overlayPagerAdapter.getmData().size();
                    pageChangeListener.pageChangeListener(i1, overlayPagerAdapter.getmData().get(i1));
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            });
        }
    }

    public int getStartPageIndex(int data, Adapter overlayPagerAdapter) {
        int index = overlayPagerAdapter.getCount() / 2;
        int remainder = index % data;
        index = index - remainder;
        return index;
    }

    interface PageChangeListener<T> {
        void pageChangeListener(int i1, T t);
    }

    /*------------------- Adapter -------------------*/

    public static abstract class Adapter<T> extends PagerAdapter {
        public List<T> mData = new ArrayList<>();

        public void setData(List<T> data) {
            mData = data;
        }

        public List<T> getmData() {
            return mData;
        }

        @Override
        public int getCount() {
            return mData.size();
        }


        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @Override
        public final Object instantiateItem(ViewGroup container, int position) {
            int i = position % mData.size();
            return overWriteInstantiateItem(container, i);
        }

        abstract public Object overWriteInstantiateItem(ViewGroup container, int position);

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // 将当前位置的View移除
            container.removeView((View) object);
        }

    }

    /*------------------- transformer -------------------*/

    public static class CardPageTransformer implements ViewPager.PageTransformer {
        private CardPageTransformer.Build mBuild;


        private CardPageTransformer(CardPageTransformer.Build build) {
            mBuild = build;
        }

        public static CardPageTransformer.Build getBuild() {
            return new CardPageTransformer.Build();
        }

        @SuppressLint("NewApi")
        public void transformPage(View page, float position) {
            if (mBuild.mOrientation == PageTransformerConfig.HORIZONTAL) {
                transformHorizontal(page, position);
            } else {
                transformHorizontal(page, position);
            }
        }

        /**
         * <p>水平方向</p>
         * <p>Horizontal</p>
         *
         * @param page
         * @param position
         */
        private void transformHorizontal(View page, float position) {
            if (position <= 0.0f) {//被滑动的那页  The page that was sliding
                page.setTranslationX(0f);

                //-----------------------动画 animation start

                // 动画类型不为none才执行动画  Animation type is not 'none'
                if (!mBuild.mAnimationType.contains(PageTransformerConfig.NONE)) {
                    //旋转 //Rotation
                    if (mBuild.mAnimationType.contains(PageTransformerConfig.ROTATION)) {
                        //旋转角度  Rotation angle   -45° * 0.1 = -4.5°
                        page.setRotation((mBuild.mRotation * Math.abs(position)));
                        //X轴偏移 xAxis offset li:  300/3 * -0.1 = -10
                        page.setTranslationX((page.getWidth() / 3 * position));
                    }

                    //透明度 alpha
                    if (mBuild.mAnimationType.contains(PageTransformerConfig.ALPHA)) {

                        //设置透明度  set alpha
                        page.setAlpha(mBuild.mAlpha - (mBuild.mAlpha * Math.abs(position)));
                    }
                }
                //-----------------------动画 animation end

                //打开点击事件
                page.setClickable(true);

            } else if (position <= mBuild.mMaxShowPage || mBuild.mViewPager == null) {    //只显示3张卡片

                setHorizontalTransformPager(page, position);
                //屏蔽点击事件
                page.setClickable(false);
            } else {
                page.setTranslationX(0f);
                page.setTranslationY(0f);
            }
        }

        /**
         * 设置禁止页面的样式
         * <p>set view type，set card style</p>
         *
         * @param page
         * @param position
         */
        private void setHorizontalTransformPager(View page, float position) {
            //缩放比例
            float scale = (page.getWidth() - mBuild.mScaleOffset * position) / (float) (page.getWidth());

            page.setScaleX(scale);
            page.setScaleY(scale);
            float proportion = 1.5f;
            //开始判断类型 //type
            switch (mBuild.mViewType) {
                //底部 bottom
                case PageTransformerConfig.BOTTOM:
                    page.setTranslationX((-page.getWidth() * position));
                    page.setTranslationY((mBuild.mTranslationOffset * proportion) * position);
                    break;
                //底左  bottom left
                case PageTransformerConfig.BOTTOM_LEFT:
                    page.setTranslationX((-page.getWidth() * position) + ((mBuild.mTranslationOffset * proportion) * position));
                    page.setTranslationY((mBuild.mTranslationOffset * proportion) * position);
                    break;
                //底右  bottom right
                case PageTransformerConfig.BOTTOM_RIGHT:
                    page.setTranslationX((-page.getWidth() * position) - ((mBuild.mTranslationOffset * proportion) * position));
                    page.setTranslationY((mBuild.mTranslationOffset * proportion) * position);

                    break;
                //右边  right
                case PageTransformerConfig.RIGHT:
                    page.setTranslationX((-page.getWidth() * position) - ((mBuild.mTranslationOffset * proportion) * position));
                    page.setTranslationY(0);
                    break;
                //左边 left
                case PageTransformerConfig.LEFT:
                    page.setTranslationX((-page.getWidth() * position) + ((mBuild.mTranslationOffset * proportion) * position));
                    page.setTranslationY(0);
                    break;
                //上面 top
                case PageTransformerConfig.TOP:
                    page.setTranslationX((-page.getWidth() * position));
                    page.setTranslationY(-((mBuild.mTranslationOffset * proportion) * position));
                    break;
                //上左 top left
                case PageTransformerConfig.TOP_LEFT:
                    page.setTranslationX((-page.getWidth() * position) + ((mBuild.mTranslationOffset * proportion) * position));
                    page.setTranslationY(-((mBuild.mTranslationOffset * proportion) * position));
                    break;
                //上右 top right
                case PageTransformerConfig.TOP_RIGHT:
                    page.setTranslationX((-page.getWidth() * position) - ((mBuild.mTranslationOffset * proportion) * position));
                    page.setTranslationY(-((mBuild.mTranslationOffset * proportion) * position));
                    break;


            }


        }

        /**
         * 建造者模式
         */
        public static class Build {

            /**
             * 缩放偏移量
             */
            private int mScaleOffset = 40;
            /**
             * 偏移量
             */
            private int mTranslationOffset = 40;
            /**
             * 旋转角度
             */
            private int mRotation = -45;
            /**
             * 透明度
             */
            private float mAlpha = 1f;
            /**
             * 视图类型
             */
            private int mViewType = PageTransformerConfig.BOTTOM;
            /**
             * 动画类型
             */
            private Set<Integer> mAnimationType = new TreeSet<>();
            /**
             * 方向
             */
            private int mOrientation = PageTransformerConfig.HORIZONTAL;
            /**
             * 默认显示的页数
             */
            private int mMaxShowPage = 5;
            /**
             * ViewPager
             */
            private ViewPager mViewPager;


            public int getOrientation() {
                return mOrientation;
            }

            public CardPageTransformer.Build setOrientation(@PageTransformerConfig.Orientation int mOrientation) {
                this.mOrientation = mOrientation;
                return this;
            }

            public CardPageTransformer.Build setScaleOffset(int mScaleOffset) {
                this.mScaleOffset = mScaleOffset;
                return this;
            }

            public CardPageTransformer.Build setTranslationOffset(int mTranslationOffset) {
                this.mTranslationOffset = mTranslationOffset;
                return this;
            }

            /**
             * 设置样式
             *
             * @param mViewType
             * @return
             */
            public CardPageTransformer.Build setViewType(@PageTransformerConfig.ViewType int mViewType) {
                this.mViewType = mViewType;
                return this;
            }

            /**
             * 动画类型（旋转、透明度）
             *
             * @param mAnimationType
             * @return
             */
            public CardPageTransformer.Build addAnimationType(@PageTransformerConfig.AnimationType int... mAnimationType) {
                for (int type : mAnimationType) {
                    this.mAnimationType.add(type);
                }
                return this;
            }

            /**
             * 完成创建
             *
             * @return
             */
            public ViewPager.PageTransformer create(ViewPager viewPager) {
                this.mViewPager = viewPager;
                this.mMaxShowPage = viewPager.getOffscreenPageLimit() - 1;
                return new CardPageTransformer(this);
            }

            public int getRotation() {
                return mRotation;
            }

            public float getAlpha() {
                return mAlpha;
            }

            public CardPageTransformer.Build setAlpha(float mAlpha) {
                this.mAlpha = mAlpha;
                return this;
            }

            /**
             * 旋转角度
             *
             * @param mRotation
             * @return
             */
            public CardPageTransformer.Build setRotation(int mRotation) {
                this.mRotation = mRotation;
                return this;
            }

        }
    }

    public static class PageTransformerConfig {
        /**
         * 方向
         */
        @IntDef({HORIZONTAL, VERTICAL})
        public @interface Orientation {
        }

        /**
         * 动画类型
         */
        @IntDef({NONE, ROTATION, ALPHA})
        public @interface AnimationType {
        }

        /**
         * 视图类型
         */
        @IntDef({BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT, TOP, TOP_LEFT, TOP_RIGHT, LEFT, RIGHT})
        public @interface ViewType {
        }

        /**
         * 没有动画
         */
        public static final int NONE = 99;
        /**
         * 旋转
         */
        public static final int ROTATION = 98;
        /**
         * 透明度
         */
        public static final int ALPHA = 97;
        /**
         * 水平方向
         */
        public static final int HORIZONTAL = -1;
        /**
         * 垂直方向
         */
        public static final int VERTICAL = -2;

        /**
         * 底部
         */
        public static final int BOTTOM = 1;
        /**
         * 底部 左边
         */
        public static final int BOTTOM_LEFT = 11;
        /**
         * 底部右边
         */
        public static final int BOTTOM_RIGHT = 12;
        /**
         * 上面
         */
        public static final int TOP = 2;
        /**
         * 上左
         */
        public static final int TOP_LEFT = 21;
        /**
         * 上右
         */
        public static final int TOP_RIGHT = 22;
        /**
         * 左边
         */
        public static final int LEFT = 3;
        /**
         * 右边
         */
        public static final int RIGHT = 4;
    }

}
