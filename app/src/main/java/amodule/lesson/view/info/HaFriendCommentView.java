package amodule.lesson.view.info;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Description : //TODO
 * PackageName : amodule.vip.view
 * Created by tanze on 2018/3/30 10:53.
 * e_mail : ztanzeyu@gmail.com
 */
public class HaFriendCommentView extends LessonParentLayout {
    public HaFriendCommentView(Context context) {
        super(context);
    }

    public HaFriendCommentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HaFriendCommentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean showInnerNextItem() {
        return false;
    }
}
