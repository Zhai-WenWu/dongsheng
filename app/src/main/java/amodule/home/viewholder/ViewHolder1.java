package amodule.home.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xiangha.R;

import java.util.Map;

import amodule._common.utility.WidgetUtility;

/**
 * Created by sll on 2017/11/14.
 */

public class ViewHolder1 extends XHBaseRvViewHolder {

    public View mItemView;
    private LinearLayout mLinearLayout;
    private ImageView mImageView1;
    private TextView mTextView1;
    private TextView mTextView3;
    private TextView mTextView4;
    public ViewHolder1(@NonNull View itemView,View parent) {
        super(itemView,parent);
        mItemView = itemView;
        if (mItemView == null)
            return;
        int[] wh = computeItemWH(326, 456, mItemView.getResources().getDimensionPixelSize(R.dimen.dp_50), 2);
        mItemView.setLayoutParams(new RelativeLayout.LayoutParams(wh[0], wh[1]));
        mItemView.invalidate();
        mLinearLayout = (LinearLayout) mItemView.findViewById(R.id.linearlayout1);
        mImageView1 = (ImageView) mItemView.findViewById(R.id.imageview1);
        mTextView1 = (TextView) mItemView.findViewById(R.id.textview1);
        mTextView3 = (TextView) mItemView.findViewById(R.id.textview3);
        mTextView4 = (TextView) mItemView.findViewById(R.id.textview4);
    }

    @Override
    public void bindData(int position, @Nullable Map<String, String> data) {
        super.bindData(position, data);
        if (mItemView == null || data == null || data.isEmpty())
            return;
        String img = data.get("img");
        setViewImage(mImageView1, img);
        String t1 = data.get("text1");
        WidgetUtility.setTextToView(mTextView1, t1);
        String t3 = data.get("text3");
        WidgetUtility.setTextToView(mTextView3, t3);
        String t4 = data.get("iconText");
        WidgetUtility.setTextToView(mTextView4, t4);
        boolean empty1 = TextUtils.isEmpty(t1);
        boolean empty3 = TextUtils.isEmpty(t3);
        boolean empty4 = TextUtils.isEmpty(t4);
        mLinearLayout.setBackgroundResource((empty1 && empty3 && empty4) ? 0 : R.drawable.bg_home_horizontal_gradation);
        String img2 = data.get("img2");
        if (!TextUtils.isEmpty(img2) && !TextUtils.equals(img, img2))
            Glide.with(mItemView.getContext()).load(img2).diskCacheStrategy(DiskCacheStrategy.SOURCE).preload();
    }
}
