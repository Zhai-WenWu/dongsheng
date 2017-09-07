package acore.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;

/**
 * Created by XiangHa on 2016/9/22.
 */
public class TextViewLimitLine extends TextView implements View.OnClickListener{

    private Context mCon;
    private ArrayList<OnClickListener> arrayList;
    private int initLine = 5;

    private boolean isShowMore = false; //是否大于初始行数
    private int textLine = 0;
    private boolean currentIsShowMore = false;

    public TextViewLimitLine(Context context) {
        super(context);
        mCon = context;
        init();
    }

    public TextViewLimitLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCon = context;
        TypedArray a = mCon.obtainStyledAttributes(attrs, R.styleable.TextViewLimitLine);
        initLine = a.getInteger(0,5);
        a.recycle(); //记得回收
        init();
    }

    private void init(){
        arrayList = new ArrayList<>();
        setEnabled(true);
        setOnClickListener(this);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

        if(text != null){
            textLine = getLineCount();
            boolean isNeedRefrash = textLine > initLine;
            if(isNeedRefrash) {
                currentIsShowMore = true;
                setMaxLines(initLine);
                isShowMore = true;
            }else{
                currentIsShowMore = false;
                isShowMore = false;
                setMaxLines(500);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(isShowMore){
            if(currentIsShowMore){
                setMaxLines(500);
            }else{
                setMaxLines(initLine);
            }
            currentIsShowMore = !currentIsShowMore;
        }
        for(OnClickListener listener : arrayList){
            listener.onClick(v,isShowMore);
        }
    }

    public void addOnClick(OnClickListener listener){
        arrayList.add(listener);
    }

    public interface OnClickListener{
        public void onClick(View v,boolean isNeedRefrash) ;
    }
}
