package aplug.stickheaderlayout;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by sj on 15/11/22.
 */
public class HeaderScrollView extends ScrollView {
    public HeaderScrollView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
