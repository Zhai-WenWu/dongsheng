package amodule.dish.helper;

import android.support.annotation.Nullable;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ParticularPositionEnableSnapHelper extends PagerSnapHelper {

    private int mParticularTargetSnapPosition = -1;

    /**
     * Assigning the target snap position. Calling {@link #invalidParticularTargetSnapPosition()} to release the particular target snap position.
     * @param particularTargetSnapPosition the position of particular target snap
     */
    public void particularTargetSnapPositionEnable(int particularTargetSnapPosition) {
        mParticularTargetSnapPosition = particularTargetSnapPosition;
    }

    /**
     * Making the particular target snap position invalided.
     * @see #particularTargetSnapPositionEnable(int)
     */
    public void invalidParticularTargetSnapPosition() {
        mParticularTargetSnapPosition = -1;
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        return super.findSnapView(layoutManager);
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        if (mParticularTargetSnapPosition != -1)
            return mParticularTargetSnapPosition;
        return super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
    }
}
