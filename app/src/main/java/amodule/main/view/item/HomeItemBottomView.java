package amodule.main.view.item;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sll on 2017/6/22.
 */

public class HomeItemBottomView extends LinearLayout {

    private TextView mTextView1;
    private TextView mTextView2;

    private ArrayList<Map<String, String>> mDatas;
    public HomeItemBottomView(Context context) {
        this(context,null);
    }

    public HomeItemBottomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HomeItemBottomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.home_item_bottomview, this, true);
        mTextView1 = (TextView) findViewById(R.id.TextView1);
        mTextView2 = (TextView) findViewById(R.id.TextView2);
    }

    public void setData(ArrayList<Map<String, String>> datas) {
        if (datas == null || datas.isEmpty()) {
            setVisibility(View.GONE);
            return;
        }
        mDatas = datas;
        bindView();
    }

    private void bindView() {
        for (int i = 0; i < mDatas.size(); i ++) {
            Map<String, String> data = mDatas.get(i);
            if (data != null && !data.isEmpty()) {
                switch (i) {
                    case 0:
                        String text1 = data.get("");
                        if (!TextUtils.isEmpty(text1)) {
                            mTextView1.setText(text1);
                            mTextView1.setVisibility(View.VISIBLE);
                        } else
                            mTextView1.setVisibility(View.GONE);
                        break;
                    case 1:
                        String text2 = data.get("");
                        if (!TextUtils.isEmpty(text2)) {
                            mTextView2.setText(text2);
                            mTextView2.setVisibility(View.VISIBLE);
                        } else
                            mTextView2.setVisibility(View.GONE);
                        break;
                }

            }
        }
        setVisibility((mTextView1.getVisibility() == View.VISIBLE || mTextView2.getVisibility() == View.VISIBLE) ? View.VISIBLE : View.GONE);
    }
}
