package third.fanli;

import android.os.Bundle;
import android.view.View;

import com.xianghatest.R;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseFragmentActivity;

/**
 * Created by Fang Ruijiao on 2016/8/18.
 */

public class GoodsListActivity extends BaseFragmentActivity implements View.OnClickListener{

    private final String  tongjiId = "a_mail_fanli";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("天猫精品返现", 2, 0, R.layout.a_fanli_mall_title, R.layout.a_fanli_mall);
        init();
    }

    private void init(){
//        XHClick.mapStat(this, tongjiId, "返利商品列表", "浏览");
//        findViewById(R.id.a_fanli_mall_fanxian).setOnClickListener(this);
//
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        //获得fragment的实例
//        HhyjMainGoodsFragment hhyjMainGoodsFragment = new HhyjMainGoodsFragment();
//
//        /** 传入自定义的dialog布局 不传或者传 0 则为Android原生dialog
//          *注意：确保自定义dialog布局中的 negative（即 "再逛逛"） 事件id为 R.id.cancel
//          * positive（即 "去登录"） 事件id为 R.id.confirm
//         */
//        hhyjMainGoodsFragment.setDialogLayout(R.layout.a_fanli_mall_login_dialog);
//
//        /** 这个方法是控制是否打开简版的商品详情  默认是 false
//         * true: 是 即从列表打开SDK中的商品详情，然后再从商品详情打开淘宝的商品详情
//         * false: 否 即从列表直接打开淘宝商品详情
//         */
//        hhyjMainGoodsFragment.setOpenDetail(false);
//        //R.id.ll_content 承载fragment的占位控件
//        transaction.add(R.id.ll_content, hhyjMainGoodsFragment).commit();
//
//        // 在用此回调之前确保已经初始化SDK
//        HhyjChannelSDK.getInstance().setOnClickEventListence(new HhyjOnClickEventListence<CallBackMark>() {
//            @Override
//            public void ClickEvent(Context context, CallBackMark type) {
//                switch (type) {
//                    case HOME:
//                        //如果用自定义的商品详情页面的话 在详情页面
//                        //点击详情页面 返回首页按钮
//                        break;
//                    case GOLOGIN:
//                        XHClick.mapStat(GoodsListActivity.this, tongjiId, "商品点击", "未登录");
//                        //去登录的回调接口 收到这个回调可以跳转登录界面
//                        Intent it = new Intent(GoodsListActivity.this, UserLoginOptions.class);
//                        startActivity(it);
//                        break;
//                }
//            }
//        });
//        // 在用此回调之前确保已经初始化SDK
//        // 回调用户点击的商品信息
//        HhyjChannelSDK.getInstance().setHhyjOnClickDataListence(new HhyjOnClickEventListence<Intent>(){
//
//            @Override
//            public void ClickEvent(Context context, Intent data) {
//                XHClick.mapStat(GoodsListActivity.this, tongjiId, "商品点击", "进详情");
//                // 回调商品名称
////                if (data != null && data.hasExtra("goods_name")) {
////                    String name = data.getStringExtra("goods_name");
////                    Log.i("FRJ","click name:" + name);
////                }
////                // 回调商品id
////                if (data != null && data.hasExtra("goods_id")) {
////                    String id = data.getStringExtra("goods_id");
////                    Log.i("FRJ","click id:" + id);
////                }
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.a_fanli_mall_fanxian:
                AppCommon.openUrl(this,FanliTools.fanxianGuize,true);
                break;
        }
    }
}
