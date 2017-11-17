package amodule.home.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import amodule._common.utility.WidgetUtility;
import amodule._common.widget.baseview.BaseRecyclerItem;

/**
 * Created by sll on 2017/11/14.
 */

public class RecyclerItem3 extends BaseRecyclerItem {

    private ImageView mImageView1;
    private TextView mTextView1;
    private TextView mTextView2;
    public RecyclerItem3(Context context) {
        super(context, R.layout.recyclerview_item3);
    }

    public RecyclerItem3(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.recyclerview_item3);
    }

    public RecyclerItem3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.recyclerview_item3);
    }

    @Override
    protected void initView() {
        mImageView1 = (ImageView) findViewById(R.id.imageview1);
        mTextView1 = (TextView) findViewById(R.id.textview1);
        mTextView2 = (TextView) findViewById(R.id.textview2);
    }

    @Override
    protected void onDataReady(Map<String, String> data) {
        setViewImage(mImageView1, data.get("img"));
        WidgetUtility.setTextToView(mTextView1, data.get("text1"));
        WidgetUtility.setTextToView(mTextView2, data.get("text2"));
    }
}
