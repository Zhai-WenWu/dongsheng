package amodule.lesson.controler.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.widget.rvlistview.RvListView;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Description :
 * PackageName : amodule.lesson.controler.view
 * Created by mrtrying on 2017/12/19 11:23:38.
 * e_mail : ztanzeyu@gmail.com
 */
public class LessonHomeViewController implements View.OnClickListener{

    private Activity mActivity;

    private View mHeaderView;

    private PtrClassicFrameLayout mRefreshLayout;
    private RvListView mRvListView;

    private LessonHomeHeaderControler mHeaderControler;

    public LessonHomeViewController(Activity activity) {
        this.mActivity = activity;
        mHeaderView = LayoutInflater.from(mActivity).inflate(R.layout.a_lesson_header_layout, null, true);
    }

    public void onCreate(){
        initUI();
    }

    private void initUI() {
        TextView title = (TextView) mActivity.findViewById(R.id.title);
        title.setText("VIP名厨课");
        mActivity.findViewById(R.id.back).setOnClickListener(this);
        //创建headerData控制器
        mHeaderControler = new LessonHomeHeaderControler(mHeaderView);

        mRefreshLayout = (PtrClassicFrameLayout) mActivity.findViewById(R.id.refresh_list_view_frame);
        mRefreshLayout.disableWhenHorizontalMove(true);
        mRvListView = (RvListView) mActivity.findViewById(R.id.rvListview);
        mRvListView.addHeaderView(mHeaderView);
    }

    public void setHeaderData(List<Map<String, String>> data){
        if (data == null || data.isEmpty()) {
            return;
        }
        //TODO ceshi
        data.remove(1);
        data.remove(1);
        mHeaderControler.setData(data);
    }

    //回到第一个位置
    public void returnListTop() {
        if (mRvListView != null) {
            mRvListView.scrollToPosition(0);
        }
    }

    public void refreshComplete() {
        if (null != mRefreshLayout)
            mRefreshLayout.refreshComplete();
    }

    public void autoRefresh() {
        if (null != mRefreshLayout)
            mRefreshLayout.autoRefresh();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                if(null == mActivity){
                    mActivity.onBackPressed();
                }
            break;
            default:break;
        }
    }

    /*--------------------------------------------- Get&Set ---------------------------------------------*/

    public RvListView getRvListView() {
        return mRvListView;
    }

    public PtrClassicFrameLayout getRefreshLayout() {
        return mRefreshLayout;
    }


}
