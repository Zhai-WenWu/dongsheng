package amodule.search.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import amodule.search.bean.Tag;


public class MultiTagView extends LinearLayout {
    private final int DEFAULT_TAG_PADDING = 10;
    private final int DEFAULT_TAG_MARGIN = 9;
    private final int DEFAULT_TAG_PADDING_TOP = 3;
    private final int DEFAULT_LAYOUT_MARGIN_TOP = 9;
    private final int DEFAULT_TAG_HEIGHT =24;
    private final int MAX_ELEM_IN_ROW = 5;

    private int mEditTextWidth;
    private int tempWidth = 0;
    private LinearLayout mLayoutItem;
    private Context mContext;
    private int mTotalWidth;
    private ArrayList<Tag> tags;

    private int parentMargin;
    private String[] tagColors;
    private String buttonAddColor;
    private String buttonAddClickColor;
    private String tagTextColor;
    private String tagClickColor;
    private int tagPading;
    private int tagMargin;
    private int tagPaddingTop;
    private int tagPaddingBottom;
    private int tagPaddingRight;
    private int tagPaddingLeft;
    private int tagMarginTop;
    private int tagHeight;
    private Drawable deleteDrawable;
    private boolean tagClickable = true;
    private boolean showAddButton;
    private int elementsInRow;
    FrameLayout frameLayout;

    private MutilTagViewCallBack callback;

    public MultiTagView(Context context) {
        this(context, null);
    }

    public MultiTagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiTagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        mContext = context;

        if (attrs == null) return;
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiTagView, defStyleAttr, 0);
        if (a == null) return;

        setParentMargin(a.getDimensionPixelSize(R.styleable.MultiTagView_parentMargin, 12));
        setButtonAddColor(a.getString(R.styleable.MultiTagView_buttonAddColor));
        setButtonAddClickColor(a.getString(R.styleable.MultiTagView_buttonAddClickColor));
        setTagTextColor(a.getString(R.styleable.MultiTagView_tagTextColor));
        setTagClickColor(a.getString(R.styleable.MultiTagView_tagClickColor));
//        setTagPading(a.getDimensionPixelSize(R.styleable.MultiTagView_tagPadding, DEFAULT_TAG_PADDING));
        setTagPaddingTop(a.getDimensionPixelSize(R.styleable.MultiTagView_tagPadding, DEFAULT_TAG_PADDING_TOP));
        setTagPaddingBottom(a.getDimensionPixelSize(R.styleable.MultiTagView_tagPadding, DEFAULT_TAG_PADDING_TOP));
        setTagPaddingRight(a.getDimensionPixelSize(R.styleable.MultiTagView_tagPadding, DEFAULT_TAG_PADDING));
        setTagPaddingLeft(a.getDimensionPixelSize(R.styleable.MultiTagView_tagPadding, DEFAULT_TAG_PADDING));
        setTagMarginTop(a.getDimensionPixelSize(R.styleable.MultiTagView_tagHeight, DEFAULT_TAG_HEIGHT));
        setDeleteDrawable(a.getDrawable(R.styleable.MultiTagView_deleteDrawable));
        init();
        a.recycle();
    }

    private void init() {
        int parentPadding = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        mTotalWidth = getDeviceWidth() - parentPadding * 2;
        tags = new ArrayList<>();
        mLayoutItem = new LinearLayout(mContext);
        mLayoutItem.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(mLayoutItem);
        tagClickable = true;
    }


    public boolean isaddView= true;
    private void addTag(final Tag tag, final int tagTndex) {
        if(MultiTagView.this.getChildCount()>=lineNum+1||!isaddView){
            return;
        }
        final TextView button = new TextView(mContext);
        button.setText(tag.content);
        button.setTextColor(getResources().getColor(android.R.color.black));
        button.setTextSize(14);
//        StateRoundRectDrawable drawable = new StateRoundRectDrawable(Color.parseColor("#000000"), Color.parseColor("#BAA8A8"));
        StateRoundRectDrawable drawable = new StateRoundRectDrawable(Color.parseColor("#f7f7f7"), Color.parseColor("#BAA8A8"));
        drawable.setDefautRadius(dip2px(2));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            button.setBackground(drawable);
        } else {
            button.setBackgroundDrawable(drawable);
        }
        button.setPadding(dip2px(DEFAULT_TAG_PADDING), dip2px(DEFAULT_TAG_PADDING_TOP),
                dip2px(DEFAULT_TAG_PADDING), dip2px(DEFAULT_TAG_PADDING_TOP));
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tag == null)
                    return;
                callback.onClick(tagTndex);
                if(isSelect) {
                    int layoutCount = MultiTagView.this.getChildCount();
                    for (int j = 0; j < layoutCount; j++) {
                        if (MultiTagView.this.getChildAt(j) instanceof LinearLayout) {
                            LinearLayout temp = (LinearLayout) MultiTagView.this.getChildAt(j);
                            int count = temp.getChildCount();
                            if (count > 0) {
                                for (int i = 0; i < count; i++) {
                                    if (temp.getChildAt(i) instanceof FrameLayout) {
                                        ((FrameLayout) temp.getChildAt(i)).getChildAt(0).setBackgroundColor(Color.parseColor("#f7f7f7"));
                                    }
                                }
                            }
                        }
                    }
                    button.setBackgroundColor(Color.parseColor("#ff6d23"));
                }

            }
        });
        button.setEnabled(tagClickable);

        int btnWidth = (int) (2 * dip2px(DEFAULT_TAG_PADDING) + button.getPaint().measureText(button.getText().toString()));
        LayoutParams layoutParams = new LayoutParams(btnWidth, dip2px(DEFAULT_TAG_HEIGHT));
        frameLayout = new FrameLayout(mContext);
        frameLayout.setLayoutParams(layoutParams);
        frameLayout.addView(button);
        layoutParams.rightMargin = dip2px(DEFAULT_TAG_MARGIN);
        tempWidth += dip2px(DEFAULT_TAG_MARGIN) + btnWidth; //add tag width
        //the last tag margin right DEFAULT_BUTTON_MARGIN, don't forget
        elementsInRow++;
        if (tempWidth - dip2px(DEFAULT_TAG_MARGIN) > mTotalWidth) {  //if out of screen, add a new layout
//        if (tempWidth - dip2px(DEFAULT_TAG_MARGIN) > mTotalWidth || elementsInRow > MAX_ELEM_IN_ROW) {  //if out of screen, add a new layout
            mLayoutItem = new LinearLayout(mContext);
            LayoutParams lParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lParams.topMargin = dip2px(DEFAULT_LAYOUT_MARGIN_TOP);
            mLayoutItem.setLayoutParams(lParams);
            addView(mLayoutItem);
            if(MultiTagView.this.getChildCount()>=lineNum+1){
                removeView(mLayoutItem);
                isaddView=false;
                return;
            }
            tempWidth = dip2px(DEFAULT_TAG_MARGIN) + btnWidth;
            elementsInRow = 1;
        }
        mLayoutItem.addView(frameLayout, layoutParams);
    }

    private void refresh() {
        removeAllViews();
        mLayoutItem = new LinearLayout(mContext);
        mLayoutItem.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(mLayoutItem);
        tempWidth = 0;
        elementsInRow = 0;
        int i = 0;
        for (Tag tag : tags) {
            addTag(tag,i++);
        }
    }

    public void addTag(String s) {
        Tag tag = new Tag(tags.size(), s);
        tags.add(tag);
        refresh();
    }

//    public void addTags(ArrayList<String> arrayList) {
//        for (String s : arrayList) {
//            Tag tag = new Tag(tags.size(), s);
//            tags.add(tag);
//        }
//        refresh();
//    }


    public void addTags(ArrayList<Map<String, String>> hotWords,MutilTagViewCallBack callback) {
        this.callback = callback;
        for (Map<String, String> m : hotWords) {
            Tag tag = new Tag(hotWords.size(), m.get("hot"));
            tags.add(tag);
        }
        refresh();
    }


    public void removeAllTagView() {
        tags.clear();
        refresh();
    }

    public void clearTags(){
        tags.clear();
    }

    public void removeTagAt(int i) {
        tags.remove(i);
        refresh();
    }

    public void setShowAddButton(boolean show) {
        showAddButton = show;
        refresh();
    }

    public void setTagClickable(boolean able) {
        tagClickable = able;
        refresh();

    }

    public ArrayList<String> getTags() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (Tag tag : tags) {
            arrayList.add(tag.content);
        }
        return arrayList;
    }

    private int getDeviceWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    private int dip2px(float dipValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public int getTagHeight() {
        return tagHeight;
    }

    public void setTagHeight(int tagHeight) {
        this.tagHeight = tagHeight;
    }

    public int getTagMarginTop() {
        return tagMarginTop;
    }

    public void setTagMarginTop(int tagMarginTop) {
        this.tagMarginTop = tagMarginTop;
    }

    public int getTagPaddingTop() {
        return tagPaddingTop;
    }

    public void setTagPaddingTop(int tagPaddingTop) {
        this.tagPaddingTop = tagPaddingTop;
    }

    public int getTagMargin() {
        return tagMargin;
    }

    public void setTagMargin(int tagMargin) {
        this.tagMargin = tagMargin;
    }

    public int getTagPading() {
        return tagPading;
    }

    public void setTagPading(int tagPading) {
        this.tagPading = tagPading;
    }

    public String getTagClickColor() {
        return tagClickColor;
    }

    public void setTagClickColor(String tagClickColor) {
        this.tagClickColor = tagClickColor;
    }

    public String getTagTextColor() {
        return tagTextColor;
    }

    public void setTagTextColor(String tagTextColor) {
        this.tagTextColor = tagTextColor;
    }

    public String getButtonAddClickColor() {
        return buttonAddClickColor;
    }

    public void setButtonAddClickColor(String buttonAddClickColor) {
        this.buttonAddClickColor = buttonAddClickColor;
    }

    public String getButtonAddColor() {
        return buttonAddColor;
    }

    public void setButtonAddColor(String buttonAddColor) {
        this.buttonAddColor = buttonAddColor;
    }

    public String[] getTagColors() {
        return tagColors;
    }

    public void setTagColors(String[] tagColors) {
        this.tagColors = tagColors;
    }

    public int getParentMargin() {
        return parentMargin;
    }

    public void setParentMargin(int parentMargin) {
        this.parentMargin = parentMargin;
    }

    public int getTagPaddingBottom() {
        return tagPaddingBottom;
    }

    public void setTagPaddingBottom(int tagPaddingBottom) {
        this.tagPaddingBottom = tagPaddingBottom;
    }

    public int getTagPaddingRight() {
        return tagPaddingRight;
    }

    public void setTagPaddingRight(int tagPaddingRight) {
        this.tagPaddingRight = tagPaddingRight;
    }

    public int getTagPaddingLeft() {
        return tagPaddingLeft;
    }

    public void setTagPaddingLeft(int tagPaddingLeft) {
        this.tagPaddingLeft = tagPaddingLeft;
    }

    public Drawable getDeleteDrawable() {
        return deleteDrawable;
    }

    public void setDeleteDrawable(Drawable deleteDrawable) {
        this.deleteDrawable = deleteDrawable;
    }



    public interface MutilTagViewCallBack {
        void onClick(int tagIndexr);
    }
    public int lineNum= 100;

    /**
     * 设置显示行数
     * @param lineNum
     */
    public void setlineNum(int lineNum ){
        this.lineNum=lineNum;
    }
    private boolean isSelect= false;

    /**
     * 是否选中状态
     * @param isSelect
     */
    public void setSelectState(boolean isSelect){
        this.isSelect = isSelect;
    }

}
