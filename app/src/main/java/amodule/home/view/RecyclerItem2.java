package amodule.home.view;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import amodule._common.utility.WidgetUtility;
import amodule._common.widget.baseview.BaseRecyclerItem;

/**
 * Created by sll on 2017/11/14.
 */

public class RecyclerItem2 extends BaseRecyclerItem {

    private ImageView mImageView1;
    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    public RecyclerItem2(Context context) {
        super(context, R.layout.recyclerview_item2);
    }

    public RecyclerItem2(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.recyclerview_item2);
    }

    public RecyclerItem2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.recyclerview_item2);
    }

    @Override
    protected void initView() {
        mImageView1 = (ImageView) findViewById(R.id.imageview1);
        mTextView1 = (TextView) findViewById(R.id.textview1);
        mTextView2 = (TextView) findViewById(R.id.textview2);
        mTextView3 = (TextView) findViewById(R.id.textview3);
        int[] wh = computeItemWH(326, 326, getContext().getResources().getDimensionPixelSize(R.dimen.dp_50), 2);
        setLayoutParams(new RelativeLayout.LayoutParams(wh[0], LayoutParams.WRAP_CONTENT));
        RelativeLayout.LayoutParams params = (LayoutParams) mImageView1.getLayoutParams();
        params.width = wh[0];
        params.height = wh[1];
        mImageView1.setLayoutParams(params);
        invalidate();
    }

    @Override
    protected void onDataReady(Map<String, String> data) {
        setViewImage(mImageView1, data.get("img"));
        WidgetUtility.setTextToView(mTextView1, data.get("text1"));
        WidgetUtility.setTextToView(mTextView2, "¥" + data.get("text2"));
        String t3 = "¥" + data.get("text3");
        Spannable spanStrikethrough = new SpannableString(t3);
        StrikethroughSpan stSpan = new StrikethroughSpan();  //设置删除线样式
        spanStrikethrough.setSpan(stSpan, 0, t3.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        WidgetUtility.setTextToView(mTextView3, spanStrikethrough);
    }
}
