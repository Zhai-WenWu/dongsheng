package acore.widget;

import android.support.v4.view.ViewPager;
import android.view.View;

public class DefaultTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        page.setTranslationX(page.getWidth() * -position);
        float yPosition = position * page.getHeight();
        page.setTranslationY(yPosition);
    }
}
