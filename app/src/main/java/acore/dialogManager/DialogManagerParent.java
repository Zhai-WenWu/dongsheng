package acore.dialogManager;

/**
 * Created by Fang Ruijiao on 2017/5/4.
 */

public abstract class DialogManagerParent {

    public abstract void isShow(OnDialogManagerCallback callback);
    public abstract void show();

    public abstract void cancel();


    public interface OnDialogManagerCallback{
        public void onShow();
        public void onGone();
    }

}
