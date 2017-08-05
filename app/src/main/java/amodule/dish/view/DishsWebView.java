package amodule.dish.view;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 */
public class DishsWebView extends DishWebView{

    private int index = 0;
    private ArrayList<String> mData;
    private OnWebViewLoadDataCallback mCallBack;

    public DishsWebView(Context context) {
        super(context);
    }

    public DishsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DishsWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnLoadCallback(OnWebViewLoadDataCallback callback){
        mCallBack = callback;
    }

    @Override
    public void onLoadFinishCallback(String html) {
        super.onLoadFinishCallback(html);
        saveDishData();
        if(mData != null && index < mData.size())
            loadDishData(mData.get(index++));
        if(mCallBack != null) mCallBack.onLoadFinish();
    }

    /**
     * 加载多个信息，加载完一个，加载下一个
     * @param data
     */
    public void loadDishData(ArrayList<String> data){
        mData = data;
        if(mData.size() > 0){
            loadDishData(mData.get(index++));
        }
    }

    public interface OnWebViewLoadDataCallback{
        public void onLoadFinish();
    }
}
