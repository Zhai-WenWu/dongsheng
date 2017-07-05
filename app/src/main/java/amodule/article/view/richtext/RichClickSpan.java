package amodule.article.view.richtext;

import android.text.style.ClickableSpan;
import android.view.View;

/**
 * PackageName : amodule.article.view.richtext
 * Created by MrTrying on 2017/7/5 22:04.
 * E_mail : ztanzeyu@gmail.com
 */

public class RichClickSpan extends ClickableSpan {
    private ClickCallback clickCallback;
    public RichClickSpan(ClickCallback callback){
        this.clickCallback = callback;
    }
    @Override
    public void onClick(View widget) {
        clickCallback.onClick(widget);
    }

    public interface ClickCallback{
        public void onClick(View widget);
    }
}
