package amodule.quan.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.activity.FriendHome;
import amodule.user.view.FollowView;
import aplug.basic.LoadImage;
import aplug.basic.ReqInternet;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * PackageName : amodule.quan.view
 * Created by MrTrying on 2016/9/29 09:38.
 * E_mail : ztanzeyu@gmail.com
 */

public class SubjectHeaderUser extends RelativeLayout implements View.OnClickListener{
    private FollowView head_subject_tv_choose;
    private TextView sb_header_tv_user_name;
    private TextView sb_header_tv_sub_timeShow;
    private ImageView sb_header_textview_lv;
    private ImageView sb_header_textview_vip;
    private ImageView sb_header_iv_user;
    private ImageView sb_header_iv_userType;

    private String userCode = "";
    private String folState="";

    public SubjectHeaderUser(Context act) {
        this(act,null);
    }

    public SubjectHeaderUser(Context act, AttributeSet attrs) {
        this(act, attrs,0);
    }

    public SubjectHeaderUser(Context act, AttributeSet attrs, int defStyleAttr) {
        super(act, attrs, defStyleAttr);
        LayoutInflater.from(act).inflate(R.layout.subject_header_user,this);
        initView();
    }

    private void initView() {
        sb_header_tv_user_name = (TextView) findViewById(R.id.sb_header_tv_user_name);
        sb_header_tv_sub_timeShow = (TextView) findViewById(R.id.sb_header_tv_sub_timeShow);
        head_subject_tv_choose = (FollowView) findViewById(R.id.head_subject_tv_choose);
        sb_header_textview_lv = (ImageView) findViewById(R.id.i_user_lv);
        sb_header_textview_vip = (ImageView) findViewById(R.id.i_user_vip);
        sb_header_iv_user = (ImageView) findViewById(R.id.sb_header_iv_user);
        sb_header_iv_userType = (ImageView) findViewById(R.id.sb_header_iv_userType);

        sb_header_iv_user.setOnClickListener(this);
    }

    public void setFollowState(@NonNull String folState){
        this.folState = folState;
    }

    public void setData(Activity act, String folState , Map<String,String> data){
        setFollowState(folState);
        setCustomerData(act,StringManager.getFirstMap(data.get("customer")));
        sb_header_tv_sub_timeShow.setText(data.get("timeShow"));
        //关注数据： 2：未关注，3：已关注
        head_subject_tv_choose.FOLLOW = "3";
        head_subject_tv_choose.FOLLOW_NOT = "2";
//        head_subject_tv_choose.setData(StringManager.api_setUserData, "type=follow&p1=" + userCode, "folState", folState);
        head_subject_tv_choose.setCallback(new FollowView.FollowCallback() {
            @Override
            public void onCallback(int flag, String url, Object returnObj) {
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    XHClick.mapStat(getContext(), BarSubjectFloorOwnerNew.tongjiId, "帖子部分点击量", "关注点击量");
                }
            }
        });
        setCustomerData(act,StringManager.getFirstMap(data.get("customer")));
    }

    // 设置楼主相关
    private void setCustomerData(Activity act,Map<String, String> customer_map) {
        if(customer_map == null){
            return;
        }
        userCode = customer_map.get("code");
        head_subject_tv_choose.setData(StringManager.api_setUserData, "type=follow&p1=" + userCode, "folState", folState);
        sb_header_tv_user_name.setText(customer_map.get("nickName"));
        sb_header_tv_user_name.setOnClickListener(this);
//        customer_map.put("color","#333333");
        if(!TextUtils.isEmpty(customer_map.get("color"))){
            sb_header_tv_user_name.setTextColor(Color.parseColor(customer_map.get("color")));
        }
        setImg(customer_map.get("imgShow"), sb_header_iv_user, ToolsDevice.dp2px(getContext(), 500));
        int iconNum = 0;
        //楼主等级是0 的不显示
        if (customer_map.containsKey("exp")) {
            boolean isShowLv = AppCommon.setLvImage(Integer.parseInt(customer_map.get("exp")), sb_header_textview_lv);
            if(isShowLv) iconNum++;
        }
        boolean isVip = AppCommon.setVip(act,sb_header_textview_vip,customer_map.get("vip"));
        if(isVip) iconNum++;
        if(iconNum>0){
            sb_header_tv_user_name.setMaxWidth(ToolsDevice.dp2px(getContext(),160 - 18 * iconNum));
        }
        //设置加v
        if (customer_map.get("isGourmet") != null)
            AppCommon.setUserTypeImage(Integer.parseInt(customer_map.get("isGourmet")), sb_header_iv_userType);

        if (LoginManager.isLogin()
                && userCode.equals(LoginManager.userInfo.get("code"))){
            head_subject_tv_choose.setVisibility(View.GONE);
        } else {
            head_subject_tv_choose.setVisibility(View.VISIBLE);
        }

    }

    // 设置图片
    private void setImg(String img_url, final ImageView imageView, int roundImgPixels) {
        imageView.setClickable(true);
        if (img_url != null && img_url.length() < 10)
            return;
        imageView.setTag(BarSubjectFloorOwnerNew.TAG_ID, img_url);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(getContext())
                .load(img_url)
                .setImageRound(roundImgPixels)
                .build();
        if (bitmapRequest != null)
            bitmapRequest.into(getTarget(imageView, img_url, roundImgPixels));
        imageView.setVisibility(View.VISIBLE);
    }

    private SubBitmapTarget getTarget(final ImageView img_view, final String url, final int roundImgPixels) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = null;
                if (img_view.getTag(BarSubjectFloorOwnerNew.TAG_ID).equals(url))
                    img = img_view;
                if (img != null && bitmap != null) {
                    // 图片圆角和宽高适应
                    img_view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    if (roundImgPixels == 0)
                        UtilImage.setImgViewByWH(img_view, bitmap, ToolsDevice.getWindowPx(getContext()).widthPixels - Tools.getDimen(getContext(), R.dimen.dp_10) * 2, 0, false);
                    else
                        UtilImage.setImgViewByWH(img_view, bitmap, 0, 0, false);
                }
            }
        };
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 点击打开楼主首页
            case R.id.sb_header_iv_user:
            case R.id.sb_header_tv_user_name:
                if(TextUtils.isEmpty(userCode)){
                    return;
                }
                XHClick.mapStat(getContext(), BarSubjectFloorOwnerNew.tongjiId, "帖子部分点击量", "头像／昵称点击量");
                //打开用户中心
                Intent intent = new Intent(getContext(), FriendHome.class);
                intent.putExtra("code", userCode);
                getContext().startActivity(intent);
                break;
        }
    }
}
