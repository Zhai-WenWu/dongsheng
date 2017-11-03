package amodule.user.view.module;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.Map;

import acore.tools.StringManager;
import amodule.user.view.module.ModuleBaseView;

/**
 * Created by Administrator on 2017/11/1.
 */
public class ModuleItemS0View extends RelativeLayout{
    public Context mContext;
    private RelativeLayout part_0,part_1,part_2;
    public ModuleItemS0View(Context context) {
        super(context);
        mContext= context;
        initLayout(R.layout.module_array_s0);
    }
    public ModuleItemS0View(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext= context;
        initLayout(R.layout.module_array_s0);
    }
    public ModuleItemS0View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext= context;
        initLayout(R.layout.module_array_s0);
    }
    /**
     * layoutId 进行处理。子View不进行处理
     * @param layoutId
     */
    private void initLayout(int layoutId){
        //TODO 有问题
        View view = LayoutInflater.from(mContext).inflate(layoutId,null,true);
        addView(view);
        part_0= (RelativeLayout) findViewById(R.id.rela_part_1);
        part_1= (RelativeLayout) findViewById(R.id.rela_part_2);
        part_2= (RelativeLayout) findViewById(R.id.rela_part_3);

    }
    /**
     * 设置数据
     * @param map 数据必须是map（必须调用）
     */
    public void initData(Map<String,String> map){
        Log.i("xianghaTag","FavoriteItemBaseView::");

        //已知布局中有三个数据
        Map<String,String> mapA= StringManager.getFirstMap(map.get("A"));
        Map<String,String> mapB= StringManager.getFirstMap(map.get("B"));
        Map<String,String> mapC= StringManager.getFirstMap(map.get("C"));
        handlerViewShow(part_0,mapA);
        handlerViewShow(part_1,mapB);
        handlerViewShow(part_2,mapC);
    }
    private void handlerViewShow(RelativeLayout part,Map<String,String> map){
        int part_0_size=part.getChildCount();
        for(int i=0;i<part_0_size;i++){
            ModuleBaseView baseView= (ModuleBaseView) part.getChildAt(i);
            if( map.get("style").equals(baseView.MODULE_TAG) ){
                baseView.setVisibility(VISIBLE);
                baseView.initData(map);
            }else baseView.setVisibility(GONE);
        }
    }
}
