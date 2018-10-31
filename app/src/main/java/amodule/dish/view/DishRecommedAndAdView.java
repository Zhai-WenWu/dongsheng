package amodule.dish.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.view.ItemBaseView;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.dish.activity.DetailDish;
import amodule.quan.activity.FollowSubject;
import amodule.quan.activity.ShowSubject;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.FriendHome;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.LoadImage;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

import static amodule.dish.activity.DetailDishWeb.tongjiId;
import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * Created by Administrator on 2017/11/16.
 */

public class DishRecommedAndAdView extends ItemBaseView implements View.OnClickListener{
    private LinearLayout userDishLayout;
    private RelativeLayout mRecomentLayout;
    private TextView mRecommentNum,mRelevantTv;
    private String code,mDishName;
    public DishRecommedAndAdView(Context context) {
        super(context, R.layout.a_dish_detail_recommend);
    }

    public DishRecommedAndAdView(Context context, AttributeSet attrs) {
        super(context, attrs,  R.layout.a_dish_detail_recommend);
    }

    public DishRecommedAndAdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr,  R.layout.a_dish_detail_recommend);
    }

    @Override
    public void init() {
        super.init();
        mRecomentLayout = (RelativeLayout)findViewById(R.id.a_dish_detail_new_xiangguan);
        userDishLayout = (LinearLayout)findViewById(R.id.a_dish_detail_new_xiangguan_scroll_linear);

        mRecommentNum = (TextView)findViewById(R.id.a_dish_detail_new_tv_num);
        mRelevantTv = (TextView)findViewById(R.id.a_dish_detail_new_relevantTv);

        mRecomentLayout.setOnClickListener(this);
        mRelevantTv.setOnClickListener(this);
        mRecommentNum.setOnClickListener(this);
    }

    /**
     * 设置基础数据
     * @param code
     * @param dishName
     */
    public void initData(String code ,String dishName){
        this.code= code;
        this.mDishName= dishName;
    }
    /**
     * 处理用户相关推荐数据
     */
    public void initUserDish(ArrayList<Map<String, String>> arrayList){
//        mRelevantTv.setVisibility(View.VISIBLE);
        userDishLayout.removeAllViews();
        if(arrayList.size() > 0) {
            findViewById(R.id.qa_line).setVisibility(View.VISIBLE);
            Map<String, String> TieMap = arrayList.get(0);
            mRecommentNum.setText(TieMap.get("totalNum") + "个作品");
            LayoutInflater inflater = LayoutInflater.from(context);
            arrayList = getListMapByJson(TieMap.get("list"));
            if (arrayList.size() > 0) {
                findViewById(R.id.a_dish_detail_new_xiangguan_scroll).setVisibility(View.VISIBLE);
                mRecomentLayout.setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.a_dish_detail_new_xiangguan_scroll).setVisibility(View.GONE);
                return;
            }
            View view;
            int index = 0, size = arrayList.size() - 1;
            for (final Map<String, String> map : arrayList) {
                view = inflater.inflate(R.layout.a_dish_detail_new_footer_item, null);
                ImageView dishImg = (ImageView) view.findViewById(R.id.a_dish_detail_show_img);
                ImageView userImg = (ImageView) view.findViewById(R.id.a_dish_detail_user_icon);
                final ImageView zanImg = (ImageView) view.findViewById(R.id.a_dish_detail_zan);
                TextView userName = (TextView) view.findViewById(R.id.a_dish_detail_user_name);
                TextView dishTime = (TextView) view.findViewById(R.id.a_dish_detail_time);
                final TextView zanNumber = (TextView) view.findViewById(R.id.a_dish_detail_zan_numer);

                String customer = map.get("customer");
                ArrayList<Map<String, String>> customerArray = StringManager.getListMapByJson(customer);
                if(customerArray.size() > 0) {
                    Map<String, String> customerMap = customerArray.get(0);
                    userName.setText(customerMap.get("nickName"));
                    setViewImage(userImg, customerMap.get("img"), 500);
                    String userCode = customerMap.get("customerCode");
                    setGotoFriendHome(userImg, userCode);
                    setGotoFriendHome(userName, userCode);
                }

                final String zanNumberStr = map.get("likeNum");
                final String subjectCode = map.get("tieCode");
                setViewImage(dishImg, map.get("img"), 0);
                final boolean isLike = "2".equals(map.get("isLike"));
                zanImg.setImageResource(isLike ? R.drawable.z_menu_praiseselected : R.drawable.z_menu_praisenomal);
                dishTime.setText(map.get("timeShow"));
                zanNumber.setText(zanNumberStr);
                OnClickListener viewOnClickListener= new View.OnClickListener(){
                        boolean newIsLike = isLike;
                        @Override
                        public void onClick(View v) {
                            if(newIsLike){
                                Tools.showToast(context,"已点过赞");
                            }else {
                                XHClick.mapStat(context, DetailDish.tongjiId_detail, "哈友相关作品", "点赞按钮点击量");
                                if(!LoginManager.isLogin()){//未登录，直接去登录
                                    Intent intent = new Intent(context, LoginByAccout.class);
                                    context.startActivity(intent);
                                    return;
                                }
                                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                                map.put("subjectCode", subjectCode);
                                map.put("type", "likeList");
                                ReqEncyptInternet.in().doEncypt(StringManager.api_quanSetSubject, map, new InternetCallback() {
                                    @Override
                                    public void loaded(int flag, String s, Object o) {
                                        if (flag >= ReqInternet.REQ_OK_STRING) {
                                            try {
                                                newIsLike = true;
                                                zanImg.setImageResource(R.drawable.i_good_activity);
                                                int zanNum = Integer.parseInt(zanNumberStr);
                                                zanNumber.setText(String.valueOf(++zanNum));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                };
                zanImg.setOnClickListener(viewOnClickListener);
                view.findViewById(R.id.dish_zan_linear).setOnClickListener(viewOnClickListener);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(context, DetailDish.tongjiId_detail, "哈友相关作品", "相关作品帖子点击量");
                        Intent it = new Intent(context, ShowSubject.class);
                        it.putExtra("code", subjectCode);
                        context.startActivity(it);
                    }
                });
                View itemParent = view.findViewById(R.id.dish_footer_item);
                if (index == 0) {
                    int dp20 = Tools.getDimen(context, R.dimen.dp_20);
                    itemParent.setPadding(dp20, 0, 0, 0);
                } else if (index == size) {
                    int dp20 = Tools.getDimen(context, R.dimen.dp_20);
                    int dp8 = Tools.getDimen(context, R.dimen.dp_8);
                    itemParent.setPadding(dp8, 0, dp20, 0);
                }
                index++;
                userDishLayout.addView(view);
            }
        }else{
            userDishLayout.removeAllViews();
            findViewById(R.id.qa_line).setVisibility(View.GONE);
            mRecomentLayout.setVisibility(View.GONE);
            findViewById(R.id.a_dish_detail_new_xiangguan_scroll).setVisibility(View.GONE);
        }
    }
    public void setViewImage(final ImageView v, final String value, int roundImgPixels) {
        if (value == null) return;
        // 异步请求网络图片
        if (value.indexOf("http") == 0) {
            if (value.length() < 10)
                return;
            v.setImageResource(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon);
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            v.setTag(TAG_ID, value);
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(context)
                    .load(value)
                    .setImageRound(roundImgPixels)
                    .setPlaceholderId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                    .setErrorId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                    .setSaveType(FileManager.save_cache)
                    .build();
            if (bitmapRequest != null) {
                bitmapRequest.into(new SubBitmapTarget() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                        ImageView img = null;
                        if (v.getTag(TAG_ID).equals(value))
                            img = v;
                        if (img != null && bitmap != null) {
                            // 图片圆角和宽高适应
                            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            UtilImage.setImgViewByWH(v, bitmap, 0, 0, false);
                        }
                    }
                });
            }
        }
    }
    private void setGotoFriendHome(View view, final String userCode){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(context, DetailDish.tongjiId_detail, "哈友相关作品", "相关作品帖子用户头像点击量");
                Intent intent = new Intent(context, FriendHome.class);
                intent.putExtra("code",userCode);
                context.startActivity(intent);
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.a_dish_detail_new_xiangguan: //点击相关作品
                Intent intent = new Intent(context, FollowSubject.class);
                intent.putExtra("dishCode",code);
                intent.putExtra("title",mDishName);
                context.startActivity(intent);
                break;
            case R.id.a_dish_detail_new_relevantTv: //晒我做的这道菜
                Intent showIntent = new Intent(context, UploadSubjectNew.class);
                showIntent.putExtra("dishCode",code);
                showIntent.putExtra("name",mDishName);
                showIntent.putExtra("skip", true);
                showIntent.putExtra("cid", "1");
                context.startActivity(showIntent);
                XHClick.mapStat(context, tongjiId, "晒我做的这道菜", "晒我做的这道菜点击量");
                break;
            case R.id.a_dish_detail_new_tv_num:
                XHClick.mapStat(context, tongjiId, "哈友相关作品", "更多作品点击量");
                Intent intentn = new Intent(context, FollowSubject.class);
                intentn.putExtra("dishCode",code);
                intentn.putExtra("title",mDishName);
                context.startActivity(intentn);
                break;
        }
    }


}
