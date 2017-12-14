package amodule.home.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import amodule._common.utility.WidgetUtility;

/**
 * Created by sll on 2017/11/14.
 */

public class ViewHolder2 extends XHBaseRvViewHolder {

    public View mItemView;
    private ImageView mImageView1;
    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    public ViewHolder2(@NonNull View itemView) {
        super(itemView);
        mItemView = itemView;
        if (mItemView == null)
            return;
        mImageView1 = (ImageView) mItemView.findViewById(R.id.imageview1);
        mTextView1 = (TextView) mItemView.findViewById(R.id.textview1);
        mTextView2 = (TextView) mItemView.findViewById(R.id.textview2);
        mTextView3 = (TextView) mItemView.findViewById(R.id.textview3);
        int[] wh = computeItemWH(326, 326, mItemView.getContext().getResources().getDimensionPixelSize(R.dimen.dp_50), 2);
        mItemView.setLayoutParams(new RelativeLayout.LayoutParams(wh[0], RelativeLayout.LayoutParams.WRAP_CONTENT));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mImageView1.getLayoutParams();
        params.width = wh[0];
        params.height = wh[1];
        mImageView1.setLayoutParams(params);
        mItemView.invalidate();
    }

    @Override
    public void bindData(int position, @Nullable Map<String, String> data) {
        super.bindData(position, data);
        if (mItemView == null || data == null || data.isEmpty())
            return;
        setViewImage(mImageView1, data.get("img"));
        WidgetUtility.setTextToView(mTextView1, data.get("text1"));
        WidgetUtility.setTextToView(mTextView2, data.get("text2"));
        String t3 = data.get("text3");
        if(null != mTextView3){
            if(!TextUtils.isEmpty(t3)){
                Spannable spanStrikethrough = new SpannableString(t3);
                StrikethroughSpan stSpan = new StrikethroughSpan();  //设置删除线样式
                spanStrikethrough.setSpan(stSpan, 0, t3.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                WidgetUtility.setTextToView(mTextView3, spanStrikethrough);
            }else{
                mTextView3.setVisibility(View.GONE);
            }
        }
    }
}
