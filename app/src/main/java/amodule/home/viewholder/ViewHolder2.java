package amodule.home.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.textservice.TextInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import amodule._common.utility.WidgetUtility;

/**
 * Created by sll on 2017/11/14.
 */

public class ViewHolder2 extends XHBaseRvViewHolder {

    private final String ICONTYPE_VIDEO_PLAY = "1";

    private View mItemView;
    private ImageView mImageView1;
    private ImageView mIcon;
    private View mShadow;
    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    private LinearLayout mLinearlayout1;

    public ViewHolder2(@NonNull View itemView) {
        super(itemView);
        mItemView = itemView;
        if (mItemView == null)
            return;
        mLinearlayout1 = (LinearLayout) mItemView.findViewById(R.id.linearlayout1);
        RelativeLayout imageLayout = (RelativeLayout) mItemView.findViewById(R.id.imageview_layout);
        mImageView1 = (ImageView) mItemView.findViewById(R.id.imageview1);
        mShadow = mItemView.findViewById(R.id.shadow);
        mIcon = (ImageView) mItemView.findViewById(R.id.icon);
        mTextView1 = (TextView) mItemView.findViewById(R.id.textview1);
        mTextView2 = (TextView) mItemView.findViewById(R.id.textview2);
        mTextView3 = (TextView) mItemView.findViewById(R.id.textview3);
        int[] wh = computeItemWH(326, 326, mItemView.getContext().getResources().getDimensionPixelSize(R.dimen.dp_50), 2);
        mItemView.setLayoutParams(new RelativeLayout.LayoutParams(wh[0], RelativeLayout.LayoutParams.WRAP_CONTENT));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mImageView1.getLayoutParams();
        params.width = wh[0];
        params.height = wh[1];
        imageLayout.setLayoutParams(params);
        mItemView.invalidate();
    }

    @Override
    public void bindData(int position, @Nullable Map<String, String> data) {
        super.bindData(position, data);
        if (mItemView == null || data == null || data.isEmpty())
            return;
        setViewImage(mImageView1, data.get("img"));
        showIcon(data.get("icon"));
        WidgetUtility.setTextToView(mTextView1, data.get("text1"));
        String t2 = data.get("text2");
        String t3 = data.get("text3");
        boolean gone = TextUtils.isEmpty(t2) && TextUtils.isEmpty(t3);
        if (mLinearlayout1 != null) {
            mLinearlayout1.setVisibility(gone ? View.GONE : View.VISIBLE);
            if (gone)
                return;
        }
        WidgetUtility.setTextToView(mTextView2, t2);
        if (null != mTextView3) {
            if (!TextUtils.isEmpty(t3)) {
                Spannable spanStrikethrough = new SpannableString(t3);
                StrikethroughSpan stSpan = new StrikethroughSpan();  //设置删除线样式
                spanStrikethrough.setSpan(stSpan, 0, t3.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                WidgetUtility.setTextToView(mTextView3, spanStrikethrough);
            } else {
                mTextView3.setVisibility(View.GONE);
            }
        }
    }

    private void showIcon(String iconType){
        hideAllIcon();
        switch(iconType){
            case ICONTYPE_VIDEO_PLAY:
                showVideoPlayIcon();
                break;
                default:

                    break;
        }
    }

    private void showVideoPlayIcon(){
        mShadow.setVisibility(View.VISIBLE);
        mIcon.setVisibility(View.VISIBLE);
    }

    private void hideAllIcon(){
        mShadow.setVisibility(View.GONE);
        mIcon.setVisibility(View.GONE);
    }
}
