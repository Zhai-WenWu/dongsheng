package acore.widget;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.LinkedList;
import java.util.List;

public class SoftKeyboardManager implements ViewTreeObserver.OnGlobalLayoutListener {

    public interface SoftKeyboardStateListener {
        void onSoftKeyboardOpened(int keyboardHeightInPx, boolean rootChanged);

        void onSoftKeyboardClosed();
    }

    private final List<SoftKeyboardStateListener> listeners = new LinkedList<>();
    private final View rootView;
    private int lastSoftKeyboardHeightInPx;
    private int rootHeight;
    private boolean isSoftKeyboardOpened;

    public SoftKeyboardManager(View rootView) {
        this(rootView, false);
    }

    public SoftKeyboardManager(View rootView, boolean isSoftKeyboardOpened) {
        this.rootView = rootView;
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        final Rect r = new Rect();
        //r will be populated with the coordinates of your view that area still visible.
        rootView.getWindowVisibleDisplayFrame(r);
        boolean rootChanged = false;
        int rootH = rootView.getRootView().getHeight();
        if (rootHeight == 0) {
            rootHeight = rootH;
        }
        int heightDiff = rootH - (r.bottom - r.top);
        if (rootHeight - rootH != 0) {
            heightDiff = rootHeight - rootH;
            rootHeight = rootH;
            rootChanged = true;
        }
        if (rootChanged) {
            innerOnGlobalLayout(heightDiff, rootChanged);
            return;
        }
        innerOnGlobalLayout(heightDiff, rootChanged);
    }

    private void innerOnGlobalLayout(int heightDiff, boolean rootChanged) {
        if (!isSoftKeyboardOpened && heightDiff > 500) { // if more than 100 pixels， its probably a keyboard...
            isSoftKeyboardOpened = true;
            notifyOnSoftKeyboardOpened(heightDiff, rootChanged);
        } else if (isSoftKeyboardOpened && heightDiff < 500) {
            isSoftKeyboardOpened = false;
            notifyOnSoftKeyboardClosed();
        }
    }

    public void setIsSoftKeyboardOpened(boolean isSoftKeyboardOpened) {
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
    }

    public boolean isSoftKeyboardOpened() {
        return isSoftKeyboardOpened;
    }

    /**
     * Default value is zero (0)
     *
     * @return last saved keyboard height in px
     */
    public int getLastSoftKeyboardHeightInPx() {
        return lastSoftKeyboardHeightInPx;
    }

    public void addSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.add(listener);
    }

    public void removeSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyOnSoftKeyboardOpened(int keyboardHeightInPx, boolean rootChanged) {
        this.lastSoftKeyboardHeightInPx = keyboardHeightInPx;

        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardOpened(keyboardHeightInPx, rootChanged);
            }
        }
    }

    private void notifyOnSoftKeyboardClosed() {
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardClosed();
            }
        }
    }
}
