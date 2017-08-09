package third.mall.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xiangha.R;

import acore.override.view.ItemBaseView;
import acore.tools.Tools;
import third.mall.activity.PublishEvalutionSingleActivity;

/**
 * PackageName : third.mall.view
 * Created by MrTrying on 2017/8/9 17:25.
 * E_mail : ztanzeyu@gmail.com
 */

public class CommodEvalutionImageItem extends ItemBaseView {
    public CommodEvalutionImageItem(Context context) {
        super(context, R.layout.item_commod_evalution_image);
        initialize();
    }

    public CommodEvalutionImageItem(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.item_commod_evalution_image);
        initialize();
    }

    public CommodEvalutionImageItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.item_commod_evalution_image);
        initialize();
    }

    private ImageView contentImage,removeImage;

    private void initialize(){
        contentImage = (ImageView) findViewById(R.id.image);
        removeImage = (ImageView) findViewById(R.id.remove);
    }

    public void setRemoveClick(OnClickListener listener){
        removeImage.setOnClickListener(listener);
    }

    public void setImage(String imagePath,final OnLoadImageFailed callback){
        Glide.with(getContext())
                .load(imagePath)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                        if(callback != null){
                            callback.onLoadFailed();
                        }
                        Tools.showToast(getContext(), "文件已损坏");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {

                        return false;
                    }
                })
                .error(R.drawable.i_nopic)
                .into(contentImage);
    }

    public interface OnLoadImageFailed{
        void onLoadFailed();
    }
}
