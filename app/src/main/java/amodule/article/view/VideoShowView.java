package amodule.article.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import org.json.JSONArray;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:42.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoShowView extends BaseView implements View.OnClickListener {
    private ImageView coverImage;
    private ImageView deleteImage;
    private RelativeLayout videoLayout;

    private String coverImageUrl;
    private String videoUrl;

    public VideoShowView(Context context) {
        super(context);
    }

    public VideoShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.a_article_view_video, this);
        coverImage = (ImageView) findViewById(R.id.video_cover_image);
        deleteImage = (ImageView) findViewById(R.id.delete_image);

        coverImage.setOnClickListener(this);
        deleteImage.setOnClickListener(this);

    }

    /**
     * [video src="videoUrl" poster="coverUrl"][video]
     *
     * @return
     */
    @Override
    public String getOutputData() {
        return "[video src=\"" + videoUrl + "\" poster=\"" + coverImageUrl + "\"][video]";
    }

    public void setVideoData(String coverImageUrl, String videoUrl) {
        this.coverImageUrl = coverImageUrl;
        this.videoUrl = videoUrl;
        Glide.with(getContext())
                .load(coverImageUrl)
                .into(coverImage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_cover_image:
                if (null != mOnClickImageListener) {
                    mOnClickImageListener.onClick(v,videoUrl);
                }
                break;
            case R.id.delete_image:
                if (null != mOnRemoveCallback) {
                    mOnRemoveCallback.onRemove(this);
                }
                break;
        }
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
