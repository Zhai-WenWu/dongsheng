package amodule.home.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import amodule._common.utility.WidgetUtility;
import amodule._common.widget.baseview.BaseRecyclerItem;

/**
 * Created by sll on 2017/11/14.
 */

public class RecyclerItem1 extends BaseRecyclerItem {

    private LinearLayout mLinearLayout;
    private ImageView mImageView1;
    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    private TextView mTextView4;
    public RecyclerItem1(Context context) {
        super(context, R.layout.recyclerview_item1);
    }

    public RecyclerItem1(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.recyclerview_item1);
    }

    public RecyclerItem1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.recyclerview_item1);
    }

    @Override
    protected void initView() {
        int[] wh = computeItemWH(326, 406, getContext().getResources().getDimensionPixelSize(R.dimen.dp_50), 2);
        setLayoutParams(new RelativeLayout.LayoutParams(wh[0], wh[1]));
        invalidate();
        mLinearLayout = (LinearLayout) findViewById(R.id.linearlayout1);
        mImageView1 = (ImageView) findViewById(R.id.imageview1);
        mTextView1 = (TextView) findViewById(R.id.textview1);
        mTextView2 = (TextView) findViewById(R.id.textview2);
        mTextView3 = (TextView) findViewById(R.id.textview3);
        mTextView4 = (TextView) findViewById(R.id.textview4);
    }


    @Override
    protected void onDataReady(Map<String, String> data) {
        setViewImage(mImageView1, data.get("img"));
        String t1 = data.get("text1");
        WidgetUtility.setTextToView(mTextView1, t1);
        String t2 = data.get("text2");
        WidgetUtility.setTextToView(mTextView2, t2);
        String t3 = data.get("text3");
        WidgetUtility.setTextToView(mTextView3, t3);
        String t4 = data.get("iconText");
        WidgetUtility.setTextToView(mTextView4, t4);
        boolean empty1 = TextUtils.isEmpty(t1);
        boolean empty2 = TextUtils.isEmpty(t2);
        boolean empty3 = TextUtils.isEmpty(t3);
        boolean empty4 = TextUtils.isEmpty(t4);
        mLinearLayout.setBackgroundResource((empty1 && empty2 && empty3 && empty4) ? 0 : R.drawable.bg_home_horizontal_gradation);
    }
}
