package amodule.article.view.richtext;

import android.text.Layout;
import android.text.style.AlignmentSpan;

/**
 * PackageName : amodule.article.view.richtext
 * Created by MrTrying on 2017/6/22 21:41.
 * E_mail : ztanzeyu@gmail.com
 */

public class RichCenterAlignmentSpan implements AlignmentSpan {
    @Override
    public Layout.Alignment getAlignment() {
        return Layout.Alignment.ALIGN_CENTER;
    }
}
