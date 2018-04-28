package amodule.dish.view;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xiangha.R;

import acore.tools.Tools;

/**
 * 图片预览单个view
// * Created by XiangHa on 2016/8/16.
 */
public abstract class ImageMoreView implements View.OnClickListener {

    //当前contentLayot是否显示
    public static boolean IS_SHOW = true;

    /**
     * 获取ViewPager的item View
     * @return
     */
    public abstract View getImageMoreView();

    /**
     * 当ViewPage切换页时，设置当前页的点击事件
     */
    public abstract void setOnClick();

    /**
     * 设置viewpage曝光事件
     */
    public void onShow(){

    }

    /**
     * 当前页面被切走
     */
    public void switchNextPage(){}

    /**
     * 此页面被切换为当前页面
     */
    public void onPageChange(){}

//    /**
//     * 设置图片layout的高度
//     */
    public void setImageViewHeight(final Context mAct, final ScrollView scrollView, final TextView tv) {
        final WindowManager wm = (WindowManager) mAct.getSystemService(Context.WINDOW_SERVICE);
        final int screenH = wm.getDefaultDisplay().getHeight();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
        final int maxHeight = (int) (screenH * 0.4);
        int tvHeight = getTextViewHeight(mAct,tv);
        if(tvHeight > maxHeight){
            params.height = maxHeight;
        }
    }

    /**
     * 获取值得买每行的字数
     * @return
     */
    private int getTextViewHeight(Context context,TextView tv) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int tv_distance = (int) context.getResources().getDimension(R.dimen.dp_19);
        int distance = (int) context.getResources().getDimension(R.dimen.dp_20);

        int waith = wm.getDefaultDisplay().getWidth();
        int tv_waith = waith - distance * 2;
        int tv_pad = Tools.getDimen(context,R.dimen.dp_5);
        int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);

        int textCount = tv.getText().length();
        int line = (textCount + num - 1) / num;
        int tvHeight = line * (tv_distance + tv_pad) + Tools.getDimen(context, R.dimen.dp_28) + Tools.getDimen(context, R.dimen.dp_16) ;
//        //Log.i("FRJ","getTvHeight:" + tvHeight);
        return tvHeight;
    }

    /**
     * 下面这种实现方式，第一次设置会失败，再次刷新就好了
     */
//    private void setTvHeight(){
        //        ViewTreeObserver vto = tv.getViewTreeObserver();
//        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
////                //文字的高度 + scrollView的padding + 内容文字框上面的数字提示高度
//                int tvHeight = tv.getHeight() + Tools.getDimen(mAct, R.dimen.dp_28) + Tools.getDimen(mAct, R.dimen.dp_16) + Tools.getDimen(mAct,R.dimen.dp_5);
////                //Log.i("FRJ","tvContent:" + tv.getText());
////                //Log.i("FRJ","tvHeight:" + tvHeight);
////                //Log.i("FRJ","maxHeight:" + maxHeight);
//                if(tvHeight > maxHeight){
//                    params.height = maxHeight;
//                }
//                return true;
//            }
//        });
//    }

}
