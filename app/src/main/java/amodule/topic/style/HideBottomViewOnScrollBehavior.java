/*
 * Copyright (c) 2018 mrtrying
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package amodule.topic.style;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.CoordinatorLayout.Behavior;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;

/**
 * Description :
 * PackageName : com.mrtrying.architecture.common.behavior
 * Created by mrtrying on 2018/8/27 18:22.
 * e_mail : ztanzeyu@gmail.com
 */
public class HideBottomViewOnScrollBehavior<V extends View> extends Behavior<V> {
    protected static final int ENTER_ANIMATION_DURATION = 225;
    protected static final int EXIT_ANIMATION_DURATION = 175;
    private static final int STATE_SCROLLED_DOWN = 1;
    private static final int STATE_SCROLLED_UP = 2;
    private int height = 0;
    private int currentState = STATE_SCROLLED_UP;
    private ViewPropertyAnimator currentAnimator;

    public HideBottomViewOnScrollBehavior() {
    }

    public HideBottomViewOnScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        this.height = child.getMeasuredHeight() + layoutParams.bottomMargin;
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == 2;
    }

    public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (this.currentState != STATE_SCROLLED_DOWN && dyConsumed > 0) {
            this.slideDown(child);
        } else if (this.currentState != STATE_SCROLLED_UP && dyConsumed < 0) {
            this.slideUp(child);
        }

    }

    protected void slideUp(V child) {
        if (this.currentAnimator != null) {
            this.currentAnimator.cancel();
            child.clearAnimation();
        }

        this.currentState = STATE_SCROLLED_UP;
        this.animateChildTo(child, 0, ENTER_ANIMATION_DURATION, new LinearOutSlowInInterpolator());
    }

    protected void slideDown(V child) {
        if (this.currentAnimator != null) {
            this.currentAnimator.cancel();
            child.clearAnimation();
        }

        this.currentState = STATE_SCROLLED_DOWN;
        this.animateChildTo(child, this.height, EXIT_ANIMATION_DURATION, new LinearOutSlowInInterpolator());
    }

    private void animateChildTo(V child, int targetY, long duration, TimeInterpolator interpolator) {
        this.currentAnimator = child.animate()
                .translationY((float) targetY)
                .setInterpolator(interpolator)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        HideBottomViewOnScrollBehavior.this.currentAnimator = null;
                    }
                });
    }
}

