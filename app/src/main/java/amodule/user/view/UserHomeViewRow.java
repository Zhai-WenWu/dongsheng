package amodule.user.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;
import java.util.Map;

import acore.tools.ToolsDevice;

/**
 * Description :
 * PackageName : amodule.user.view
 * Created by tanzeyu on 2018/9/13 15:49.
 * e_mail : ztanzeyu@gmail.com
 */
public class UserHomeViewRow extends LinearLayout {
    public static final int MAX_SIZE = 3;
    private CreateViewCallback mCreateViewCallback;
    final int RLM = RelativeLayout.LayoutParams.MATCH_PARENT;

    public UserHomeViewRow(Context context) {
        this(context, null);
    }

    public UserHomeViewRow(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserHomeViewRow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView();
    }

    private void initializeView() {
        setOrientation(HORIZONTAL);
//        setWeightSum(MAX_SIZE);
        //124*165
        final int dividerSize = 2;
        int itemWidth = (ToolsDevice.getWindowPx(getContext()).widthPixels - dividerSize * (MAX_SIZE - 1)) / MAX_SIZE;
        int itemHeight = (int) (itemWidth * 165 / 124f);
        LayoutParams layoutParams = new LayoutParams(itemWidth + dividerSize, itemHeight);
        for (int i = 0; i < MAX_SIZE; i++) {
            RelativeLayout itemLayout = new RelativeLayout(getContext());
            itemLayout.setPadding(0,0,i != MAX_SIZE - 1 ? dividerSize : 0,0);
            addView(itemLayout, layoutParams);
        }
        setPadding(0,0,0,dividerSize);
    }

    int position;

    public void setData(List<Map<String, String>> data, int position) {
        this.position = position;
        if (data == null || data.isEmpty()) {
            setVisibility(GONE);
            return;
        }

        for (int i = 0; i < getChildCount(); i++) {
            RelativeLayout itemLayout = (RelativeLayout) getChildAt(i);
            if (i < data.size()) {
                View view = null;
                if (itemLayout.getChildCount() <= 0) {
                    //填充view
                    if (mCreateViewCallback != null) {
                        view = mCreateViewCallback.createView();
                    }
                    if (view != null) {
                        itemLayout.addView(view, RLM, RLM);
                    }
                } else {
                    view = itemLayout.getChildAt(0);
                }
                //有view，bindData
                if (mCreateViewCallback != null) {
                    mCreateViewCallback.bindData(view, data.get(i));
                }
                itemLayout.setVisibility(VISIBLE);
            } else {
                itemLayout.setVisibility(INVISIBLE);
            }
        }
    }

    public void setCreateViewCallback(CreateViewCallback createViewCallback) {
        mCreateViewCallback = createViewCallback;
    }

    public int getRowPosition() {
        return position;
    }

    public interface CreateViewCallback {
        View createView();

        void bindData(View view, Map<String, String> data);
    }
}
