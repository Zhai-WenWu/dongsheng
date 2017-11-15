package amodule.home.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import amodule._common.widget.baseview.BaseRecyclerItem;

/**
 * Created by sll on 2017/11/14.
 */

public class RecyclerItem1 extends BaseRecyclerItem {

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
        mImageView1 = (ImageView) findViewById(R.id.imageview1);
        mTextView1 = (TextView) findViewById(R.id.textview1);
        mTextView2 = (TextView) findViewById(R.id.textview2);
        mTextView3 = (TextView) findViewById(R.id.textview3);
        mTextView4 = (TextView) findViewById(R.id.textview4);
    }

    @Override
    protected void onDataReady(Map<String, String> data) {
        String image = data.get("img");
        setViewImage(mImageView1, image);
        String t1 = data.get("text1");
        mTextView1.setText(t1);
        mTextView1.setVisibility(TextUtils.isEmpty(t1) ? View.GONE : View.VISIBLE);
        String t2 = data.get("text2");
        mTextView2.setText(t2);
        mTextView2.setVisibility(TextUtils.isEmpty(t2) ? View.GONE : View.VISIBLE);
        String t3 = data.get("text3");
        mTextView3.setText(t3);
        mTextView3.setVisibility(TextUtils.isEmpty(t3) ? View.GONE : View.VISIBLE);
        String t4 = data.get("iconText");
        mTextView4.setText(t4);
        mTextView4.setVisibility(TextUtils.isEmpty(t4) ? View.GONE : View.VISIBLE);
    }
}
