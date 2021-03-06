package amodule.home.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;
import amodule._common.utility.WidgetUtility;

/**
 * Created by sll on 2017/11/14.
 */

public class ViewHolder3 extends XHBaseRvViewHolder {

    public View mItemView;
    private ImageView mImageView1;
    private ImageView mLabelIcon;
    private TextView mTextView1;
    private TextView mTextView2;
    public ViewHolder3(@NonNull View itemView,View parent) {
        super(itemView,parent);
        mItemView = itemView;
        if (mItemView == null)
            return;
        int[] wh = computeItemWH(670, 380, mItemView.getContext().getResources().getDimensionPixelSize(R.dimen.dp_40), 1);
        mItemView.setLayoutParams(new RelativeLayout.LayoutParams(wh[0], wh[1]));
        mItemView.invalidate();
        mImageView1 = (ImageView) mItemView.findViewById(R.id.imageview1);
        mLabelIcon = (ImageView) mItemView.findViewById(R.id.icon_label);
        mTextView1 = (TextView) mItemView.findViewById(R.id.textview1);
        mTextView2 = (TextView) mItemView.findViewById(R.id.textview2);
    }

    @Override
    public void bindData(int position, @Nullable Map<String, String> data) {
        super.bindData(position, data);
        if (mItemView == null || data == null || data.isEmpty())
            return;
        setViewImage(mImageView1, data.get("img"));
        String icon = StringManager.getFirstMap(data.get("labelIcon")).get("iconUrl");
        setViewImage(mLabelIcon, icon, R.color.transparent, false);
        WidgetUtility.setTextToView(mTextView1, data.get("text1"));
        WidgetUtility.setTextToView(mTextView2,  "— " + data.get("text2") + " —");
    }
}
