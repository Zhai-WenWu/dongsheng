package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * Created by sll on 2017/5/26.
 */

public class ReportItem extends RelativeLayout {

    private int mPosition;
    private String mValue;
    private String mKey;

    private TextView mDesc;
    public ReportItem(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.report_item, this, true);
        initView();
    }

    public ReportItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.report_item, this, true);
        initView();
    }

    public ReportItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.report_item, this, true);
        initView();

    }

    private void initView() {
        mDesc = (TextView) findViewById(R.id.desc);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClick != null)
                    mItemClick.onItemClick();
            }
        });
    }

    public void setData(String key, String desc, int pos) {
        mDesc.setText(desc);
        this.mPosition = pos;
        this.mValue = desc;
        this.mKey = key;
    }

    public String getInfo() {
        return mValue;
    }

    public String getKey() {
        return mKey;
    }

    public interface OnItemClickListener {
        public abstract void onItemClick();
    }

    private OnItemClickListener mItemClick;
    public void setOnItemClickListener(OnItemClickListener clickListener) {
        mItemClick = clickListener;
    }

}
