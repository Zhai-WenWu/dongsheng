package amodule.user.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.Map;

import amodule.user.view.module.ModuleBaseView;

/**
 * Created by Administrator on 2017/11/1.
 */

public class FavoriteItemBaseView extends RelativeLayout{
    public Context mContext;
    public FavoriteItemBaseView(Context context, int layoutId) {
        super(context);
        mContext= context;
        initLayout(layoutId);
    }
    public FavoriteItemBaseView(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs);
        mContext= context;
        initLayout(layoutId);
    }
    public FavoriteItemBaseView(Context context, AttributeSet attrs, int defStyleAttr,int layoutId) {
        super(context, attrs, defStyleAttr);
        mContext= context;
        initLayout(layoutId);
    }
    /**
     * layoutId 进行处理。子View不进行处理
     * @param layoutId
     */
    private void initLayout(int layoutId){
        LayoutInflater.from(mContext).inflate(layoutId,this,true);
    }
    /**
     * 设置数据
     * @param map 数据必须是map（必须调用）
     */
    public void initData(Map<String,String> map){
        Log.i("xianghaTag","FavoriteItemBaseView::");
        RelativeLayout root_rela = (RelativeLayout) findViewById(R.id.root_rela);
        int size= root_rela.getChildCount();
        for(int i=0;i<size;i++){
            ((ModuleBaseView)root_rela.getChildAt(i)).initData(map);
        }
    }
}
