package third.mall.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.xianghatest.R;

import java.util.ArrayList;

import acore.tools.Tools;

/**
 * PackageName : third.mall.view
 * Created by MrTrying on 2017/8/10 13:12.
 * E_mail : ztanzeyu@gmail.com
 */

public class EvalutionImageLayout extends LinearLayout {

    public EvalutionImageLayout(Context context) {
        super(context);
        initialize();
    }

    public EvalutionImageLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public EvalutionImageLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    ArrayList<String> imageArray = new ArrayList<>();
    private OnHierarchyChangeCallback onHierarchyChangeCallback;

    private int itemWidth = -1;
    private int itemHeight=-1;

    private void initialize(){
        setOrientation(HORIZONTAL);
        setGravity(Gravity.BOTTOM);
        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if(onHierarchyChangeCallback != null)
                    onHierarchyChangeCallback.onChildViewAdded(parent, child);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                if(onHierarchyChangeCallback != null)
                    onHierarchyChangeCallback.onChildViewRemoved(parent, child);
            }
        });
    }

    /**
     * 设置宽度，通过宽度计算高度
     * @param width
     */
    public void setViewSize(int width){
        this.itemWidth = width;
        this.itemHeight = width - Tools.getDimen(getContext(),R.dimen.dp_6);
    }

    /**
     * 更新UI以及数据集合
     * @param images 图片集合
     */
    public void updateImage(ArrayList<String> images) {
        images.addAll(0,imageArray);
        imageArray.clear();
        //替换数据
        for (int index = 0, length = images.size(); index < length; index++) {
            CommodEvalutionImageItem item;
            String imagePath = images.get(index);
            if(index < getChildCount()){
                //有image了
                item = (CommodEvalutionImageItem) getChildAt(index);
            }else {
                //没有image
                item = new CommodEvalutionImageItem(getContext());
                if(itemWidth != -1 && itemHeight != -1){
                    item.setLayoutParams(new LayoutParams(itemWidth,itemHeight));
                }
                addView(item);
            }
            item.setTag(R.id.image_path, imagePath);
            imageArray.add(imagePath);
            setImage(imagePath, item);
        }
        //移除多余view
        for (int index = getChildCount() - 1; index >= images.size(); index--)
            removeViewAt(index);
    }

    private void setImage(final String imagePath, final CommodEvalutionImageItem item) {
        item.setImage(imagePath, new CommodEvalutionImageItem.OnLoadImageFailed() {
            @Override
            public void onLoadFailed() {
                imageArray.remove(imagePath);
                removeView(item);

            }
        });
        item.setRemoveClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageArray.remove(imagePath);
                removeView(item);
            }
        });
    }

    public ArrayList<String> getImageArray() {
        return imageArray;
    }

    public void setImageArray(ArrayList<String> imageArray) {
        this.imageArray = imageArray;
    }

    public OnHierarchyChangeCallback getOnHierarchyChangeCallback() {
        return onHierarchyChangeCallback;
    }

    public void setOnHierarchyChangeCallback(OnHierarchyChangeCallback onHierarchyChangeCallback) {
        this.onHierarchyChangeCallback = onHierarchyChangeCallback;
    }

    public interface OnHierarchyChangeCallback{
        void onChildViewAdded(View parent, View child);
        void onChildViewRemoved(View parent, View child);
    }

}
