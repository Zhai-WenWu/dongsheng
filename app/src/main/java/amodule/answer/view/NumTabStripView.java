package amodule.answer.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * Created by sll on 2017/7/31.
 */

public class NumTabStripView extends RelativeLayout{

    private int mPosition;

    private TextView mTitleText;
    private TextView mNumText;

    public NumTabStripView(Context context) {
        this(context, null);
    }

    public NumTabStripView(Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public NumTabStripView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.tab_strip_numlayout, this, true);
        mNumText = (TextView) findViewById(R.id.num);
        mTitleText = (TextView) findViewById(R.id.title);
    }

    public void setData(int num, String title) {
        if (num <= 0)
            mNumText.setVisibility(View.GONE);
        else if (num > 99) {
            mNumText.setText("99+");
            mNumText.setVisibility(View.VISIBLE);
        }
        mTitleText.setText(title);
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

    public void onTabClick() {
        mNumText.setVisibility(View.GONE);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
    }

    public void hideNum() {
        if (mNumText != null && mNumText.getVisibility() == View.VISIBLE)
            mNumText.setVisibility(View.GONE);
    }
}
