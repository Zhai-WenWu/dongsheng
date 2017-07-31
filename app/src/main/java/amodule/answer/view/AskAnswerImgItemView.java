package amodule.answer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import java.io.File;
import java.util.Map;

import acore.tools.Tools;
import aplug.basic.LoadImage;

/**
 * Created by sll on 2017/7/18.
 */

public class AskAnswerImgItemView extends RelativeLayout {

    private Context mContext;

    private ImageView mImg;
    private ImageView mVideoBtn;
    private ImageView mDelImg;
    private RelativeLayout mRootView;

    private Map<String, String> mDataMap;
    private int mPosition;

    public AskAnswerImgItemView(Context context) {
        super(context);
        initView(context);
    }

    public AskAnswerImgItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AskAnswerImgItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        if (mContext == null)
            return;
        LayoutInflater.from(mContext).inflate(R.layout.ask_answer_img_itemview, this, true);
        mRootView = (RelativeLayout) findViewById(R.id.ask_itemview);
        mImg = (ImageView) findViewById(R.id.img);
        mVideoBtn = (ImageView) findViewById(R.id.video_btn);
        mDelImg = (ImageView) findViewById(R.id.del_img);
        addListener();
    }

    private void addListener() {
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ask_itemview:
                        if (mOnItemClickListener != null)
                            mOnItemClickListener.onClick(AskAnswerImgItemView.this);
                        break;
                    case R.id.del_img:
                        if (mOnDelClickListener != null)
                            mOnDelClickListener.onClick(mDelImg);
                        break;
                }
            }
        };
        mRootView.setOnClickListener(listener);
        mDelImg.setOnClickListener(listener);
    }

    public void setData(Map<String, String> dataMap, int position, int itemWidth, int itemHeight) {
        if (dataMap == null || dataMap.isEmpty())
            return;
        mDataMap = dataMap;
        mPosition = position;
        mVideoBtn.setVisibility(!TextUtils.isEmpty(dataMap.get("video")) ? View.VISIBLE : View.GONE);
        loadSource(TextUtils.isEmpty(dataMap.get("video")) ? dataMap.get("img") : dataMap.get("thumImg"));
        this.setLayoutParams(new LayoutParams(itemWidth, itemHeight));
        this.invalidate();
    }

    private void loadSource(String imageUrl) {
        if (imageUrl.endsWith(".gif")) {
            if (imageUrl.startsWith("http")) {
                LoadImage.with(getContext())
                        .load(imageUrl)
                        .build()
                        .listener(bitmapRequestListener)
                        .into(mImg);

            } else {
                File file = new File(imageUrl);
                Glide.with(getContext())
                        .load(file)
                        .asBitmap()
                        .listener(fileRequestListener)
                        .into(mImg);
            }
        } else {
            if (imageUrl.startsWith("http")) {
                LoadImage.with(getContext())
                        .load(imageUrl)
                        .build()
                        .listener(bitmapRequestListener)
                        .into(mImg);
            } else {
                File file = new File(imageUrl);
                Glide.with(getContext())
                        .load(file)
                        .asBitmap()
                        .listener(fileRequestListener)
                        .into(mImg);
            }
        }
    }

    public Map<String, String> getData() {
        return mDataMap;
    }

    private RequestListener<GlideUrl, Bitmap> bitmapRequestListener = new RequestListener<GlideUrl, Bitmap>() {
        @Override
        public boolean onException(Exception e, GlideUrl glideUrl, Target<Bitmap> target, boolean b) {
            if (mOnLoadExce != null)
                mOnLoadExce.onLoadException();
            Tools.showToast(getContext(), "文件已损坏");
            return true;
        }

        @Override
        public boolean onResourceReady(Bitmap bitmap, GlideUrl glideUrl, Target<Bitmap> target, boolean b, boolean b1) {
            return false;
        }
    };
    private RequestListener<File, Bitmap> fileRequestListener = new RequestListener<File, Bitmap>() {
        @Override
        public boolean onException(Exception e, File file, Target<Bitmap> target, boolean b) {
            if (mOnLoadExce != null)
                mOnLoadExce.onLoadException();
            Tools.showToast(getContext(), "文件已损坏");
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap bitmap, File file, Target<Bitmap> target, boolean b, boolean b1) {
            return false;
        }
    };

    public boolean isVideo() {
        return mDataMap == null ? false : !TextUtils.isEmpty(mDataMap.get("videoPath"));
    }

    public int getPosition() {
        return mPosition;
    }

    private OnClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnClickListener listener) {
        mOnItemClickListener = listener;
    }

    private OnClickListener mOnDelClickListener;
    public void setOnDelClickListener(OnClickListener listener) {
        mOnDelClickListener = listener;
    }

    public interface OnLoadExceptionListener {
        void onLoadException();
    }

    private OnLoadExceptionListener mOnLoadExce;
    public void setOnLoadExceptionListener (OnLoadExceptionListener onLoadExceptionListener) {
        mOnLoadExce = onLoadExceptionListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }
}
