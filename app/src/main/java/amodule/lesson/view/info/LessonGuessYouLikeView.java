package amodule.lesson.view.info;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Description :
 * PackageName : amodule.vip.view
 * Created by tanze on 2018/3/30 11:01.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonGuessYouLikeView extends LessonParentLayout {
    public LessonGuessYouLikeView(Context context) {
        super(context);
    }

    public LessonGuessYouLikeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LessonGuessYouLikeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean showInnerNextItem() {
        return false;
    }
}
