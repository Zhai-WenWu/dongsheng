package acore.widget.banner;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Description :
 * PackageName : com.mrtrying.carddemo
 * Created by mrtrying on 2018/12/10 10:40.
 * e_mail : ztanzeyu@gmail.com
 */
public class CardPageTransformer implements ViewPager.PageTransformer {
    private final int mScaleOffset,mTranslationOffset;

    public CardPageTransformer(int scaleOffset, int translationOffset) {
        mScaleOffset = scaleOffset;
        mTranslationOffset = translationOffset;
    }

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setScaleX(1);
            view.setScaleY(1);
        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        } else if (position <= 1) { // (0,1]
            // Fade the page out.

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position + mTranslationOffset * position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = (view.getWidth() - mScaleOffset * position) / (float) (view.getWidth());
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setScaleX(0);
            view.setScaleY(0);
        }
    }
}
