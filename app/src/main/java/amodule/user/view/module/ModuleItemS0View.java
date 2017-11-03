package amodule.user.view.module;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
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
    private String statisticId="";
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
        View view = LayoutInflater.from(mContext).inflate(layoutId,null,true);
        addView(view);
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        part_0= (RelativeLayout) findViewById(R.id.rela_part_1);
        part_1= (RelativeLayout) findViewById(R.id.rela_part_2);
        part_2= (RelativeLayout) findViewById(R.id.rela_part_3);
    }
    /**
     * 设置数据
     * @param map 数据必须是map（必须调用）
     */
    public void initData(Map<String,String> map){

        //已知布局中有三个数据
        Map<String,String> mapA= StringManager.getFirstMap(map.get("A"));
        Map<String,String> mapB= StringManager.getFirstMap(map.get("B"));
//        Map<String,String> mapC= StringManager.getFirstMap(map.get("C"));
        part_0.getChildAt(0).setTag("A1");
        part_1.getChildAt(0).setTag("B1");
        part_1.getChildAt(1).setTag("B2");
        part_1.getChildAt(2).setTag("B3");
        part_1.getChildAt(3).setTag("B4");
        handlerViewShow(part_0,mapA);
        handlerViewShow(part_1,mapB);

    }
    private void handlerViewShow(RelativeLayout part,Map<String,String> map){
        int part_0_size=part.getChildCount();
        Log.i("xianghaTag","part_0_size:::"+part_0_size);
        for(int i=0;i<part_0_size;i++){
            View viewStub= part.getChildAt(i);
            if(viewStub==null){
                Log.i("xianghaTag","viewStub:::");
            }
            if( viewStub!=null&&map.get("style").equals(viewStub.getTag()) ){
                viewStub.setVisibility(View.VISIBLE);
                viewStub.invalidate();
                ModuleBaseView  baseView= (ModuleBaseView) findViewWithTag(viewStub.getTag());
                baseView.setStatisticId(getStatisticId());
                baseView.initData(map);
            }else viewStub.setVisibility(GONE);
        }
    }
    public String getStatisticId() {
        return statisticId;
    }

    public void setStatisticId(String statisticId) {
        this.statisticId = statisticId;
    }
}
