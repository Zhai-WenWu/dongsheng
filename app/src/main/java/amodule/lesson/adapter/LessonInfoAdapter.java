package amodule.lesson.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.lesson.view.info.ItemImage;
import amodule.lesson.view.info.ItemTitle;

/**
 * Description :
 * PackageName : amodule.vip.adapter
 * Created by tanze on 2018/3/29 17:08.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonInfoAdapter extends RvBaseAdapter<Map<String, String>> {

    public static final String KEY_VIEW_TYPE = "VIEW_TYPE";
    public static final int VIEW_TYPE_TITLE = 1;
    public static final int VIEW_TYPE_IMAGE = 2;

    private ItemImage.OnClickMoreCallbcak mMoreCallbcak;

    private LayoutInflater mLayoutInflater;

    private int mImageWidth = 0;

    public LessonInfoAdapter(Context context, @Nullable List<Map<String, String>> data) {
        super(context, data);
        mImageWidth = ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_40);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_TITLE:
                ItemTitle itemTitle = (ItemTitle) mLayoutInflater.inflate(R.layout.item_lesson_title_layout,parent,false);
                return new TitleViewHolder(itemTitle);
            case VIEW_TYPE_IMAGE:
                ItemImage itemImage = (ItemImage) mLayoutInflater.inflate(R.layout.item_lesson_image_layout,parent,false);
                return new ImageViewHolder(itemImage);
            default:
                return new DefaultViewHolder(new View(mContext));
        }
    }

    @Override
    public int getItemViewType(int position) {
        int itemType = 0;
        if (mData != null && mData.size() > position) {
            try {
                itemType = Integer.parseInt(mData.get(position).get(KEY_VIEW_TYPE));
            } catch (Exception ignored) {
            }
        }
        return itemType;
    }

    public void setMoreCallbcak(ItemImage.OnClickMoreCallbcak moreCallbcak) {
        mMoreCallbcak = moreCallbcak;
    }

    class TitleViewHolder extends RvBaseViewHolder<Map<String, String>> {

        private ItemTitle mTitle;

        public TitleViewHolder(@NonNull ItemTitle title) {
            super(title);
            this.mTitle = title;
//            mTitle.showTopPadding();
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            mTitle.setData(data);
        }
    }

    class ImageViewHolder extends RvBaseViewHolder<Map<String, String>> {
        private ItemImage mImage;

        public ImageViewHolder(@NonNull ItemImage image) {
            super(image);
            this.mImage = image;
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            mImage.setData(data);
            mImage.setImageWidth(mImageWidth);
            if(mMoreCallbcak != null){
                mImage.setClickMoreCallbcak(mMoreCallbcak);
            }
        }
    }

    /**默认*/
    class DefaultViewHolder extends RvBaseViewHolder<Map<String, String>>{

        public DefaultViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            //do nothing
        }
    }
}
