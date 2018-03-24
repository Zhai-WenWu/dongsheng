package amodule.quan.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import third.share.BarShare;
import xh.basic.tool.UtilString;

/**
 * 底部view
 */
public class NormarlContentItemfootView extends NormarlContentItemView{
    private View view;
    private ImageView like_img,click_img;
    private TextView click_tv, comment_tv, like_tv,tv_distance;
    private LinearLayout click_linear, comment_linear, like_linear;
    private Map<String,String> map;
    private FootViewCallback footViewCallback;

    public NormarlContentItemfootView(Activity context,View view) {
        super(context);
        this.view= view;
    }

    public void setFootViewCallback(FootViewCallback footViewCallback){
        this.footViewCallback=footViewCallback;
    }
    @Override
    protected void initView() {
        tv_distance = (TextView)view. findViewById(R.id.tv_distance);
        // 底部显示
        click_linear = (LinearLayout) view.findViewById(R.id.click_linear);
        comment_linear = (LinearLayout)view. findViewById(R.id.comment_linear);
        like_linear = (LinearLayout)view. findViewById(R.id.like_linear);
        click_tv = (TextView)view. findViewById(R.id.click_tv);
        comment_tv = (TextView)view. findViewById(R.id.comment_tv);
        like_tv = (TextView)view. findViewById(R.id.like_tv);
        like_img = (ImageView)view. findViewById(R.id.like_img);
        click_img= (ImageView) view.findViewById(R.id.click_img);
    }

    @Override
    public void setViewData(Map<String, String> maps,int position) {
        this.map= maps;
        if (map.containsKey("city") && !TextUtils.isEmpty(map.get("city"))) {
            view.findViewById(R.id.distance_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.im_distance).setVisibility(View.VISIBLE);
            setViewText(tv_distance, map.get("city"));
        } else {
            view.findViewById(R.id.distance_layout).setVisibility(View.GONE);
        }
        //广告处理
        if (map.get("isPromotion") != null && map.get("isPromotion").equals("1")) {
            view.findViewById(R.id.distance_layout).setVisibility(View.GONE);
            view.findViewById(R.id.im_distance).setVisibility(View.GONE);

            if("ad".equals(map.get("adStyle")) && "1".equals(map.get("hideAdTag"))){
                view.findViewById(R.id.ad_tag).setVisibility(View.INVISIBLE);
            }else{
                view.findViewById(R.id.ad_tag).setVisibility(View.VISIBLE);
            }
        }else{
            view.findViewById(R.id.ad_tag).setVisibility(View.GONE);
        }
        // *****************************对底部的处理start************************************
        if(map.containsKey("selfVideo") && !TextUtils.isEmpty(map.get("selfVideo"))){//视频
            click_tv.setText("分享");
            click_img.setImageResource(R.drawable.circle_item_share);
        }else {
            click_tv.setText((map.containsKey("click") && !TextUtils.isEmpty(map.get("click")) && !"0".equals(map.get("click"))) ? map.get("click") : "浏览");
            LinearLayout.LayoutParams layoutParams= new LinearLayout.LayoutParams(Tools.getDimen(context,R.dimen.dp_19),Tools.getDimen(context,R.dimen.dp_13));
            layoutParams.gravity= Gravity.CENTER_VERTICAL;
            click_img.setImageResource(R.drawable.circle_browse);
            click_img.setLayoutParams(layoutParams);
        }
        comment_tv.setText((map.containsKey("commentNum") && !TextUtils.isEmpty(map.get("commentNum"))&&!"0".equals(map.get("commentNum"))) ? map.get("commentNum") : "评论");
        like_tv.setText((map.containsKey("likeNum") && !TextUtils.isEmpty(map.get("likeNum"))&&!"0".equals(map.get("likeNum"))) ? map.get("likeNum") : "点赞");
        if (map.containsKey("isLike") && "2".equals(map.get("isLike"))) {
            like_img.setBackgroundResource(R.drawable.z_quan_home_body_ico_good_active);
        } else like_img.setBackgroundResource(R.drawable.z_quan_home_body_ico_good);
        if(map.get("isPromotion") != null && map.get("isPromotion").equals("1")){
            view.findViewById(R.id.bootom_linear).setVisibility(View.GONE);
            view.findViewById(R.id.bootom_view).setVisibility(View.VISIBLE);
        }else{
            view.findViewById(R.id.bootom_linear).setVisibility(View.VISIBLE);
            view.findViewById(R.id.bootom_view).setVisibility(View.GONE);
        }
        // *****************************对底部的处理end**************************************
    }

    @Override
    public void setShowUpload(boolean state) {
        if(state){
            if(map.containsKey("selfVideo") && !TextUtils.isEmpty(map.get("selfVideo"))) {//视频
                click_linear.setOnClickListener(new OnClickListener() {//分享
                    @Override
                    public void onClick(View v) {
                        BarShare barShare = new BarShare(context, "美食贴列表", "视频");
                        String mTitle = map.get("title");
                        String mClickUrl = StringManager.wwwUrl + "quan/" + map.get("code") + ".html";
                        String mContent = "看了这段视频，我和我的小伙伴都惊呆了！播放戳这里 ";
                        String shareImg=UtilString.getListMapByJson(map.get("selfVideo")).get(0).get("sImgUrl");
                        String type = BarShare.IMG_TYPE_WEB;
                        if (!TextUtils.isEmpty(shareImg)) {
                            barShare.setShare(type, mTitle, mContent, shareImg, mClickUrl);
                        } else {
                            Resources res = getResources();
                            Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.share_launcher);
                            barShare.setShare(mTitle, mContent, bmp, mClickUrl);
                        }
                        barShare.openShare();
                    }
                });
            }else{
                setListener(click_linear, typeSubject, "浏览");
            }

            setListener(comment_linear, typeSubject,  "评论");
            //点赞
            like_linear.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!LoginManager.isLogin()) {
                        Intent intent = new Intent(context, LoginByAccout.class);
                        context.startActivity(intent);
                        return;
                    }
                    setOnClickLike();
                    if(footViewCallback!=null)footViewCallback.onClicklike("like");
//                    setIndexStatictis(INDEX_LIKE);
                    if(normarlContentData!=null)
                     XHClick.mapStat(context, normarlContentData.getStatisID(), normarlContentData.getStatisKey(), "点赞");
                     XHClick.track(getContext(), "美食贴点赞");
                }
            });
//			//点击评论
            if(footViewCallback!=null)footViewCallback.onClickview("content");
//
        }
    }

    @Override
    public void onClickCallback(int type,String statisValue) {
        if(normarlViewOnClickCallBack!=null)
            normarlViewOnClickCallBack.onClickViewIndex(type,statisValue);
    }

    @Override
    public void onAdClickCallback(View view,String eventId) {
        if (mAdHintClickCallback != null)
            mAdHintClickCallback.onAdHintListener(view,eventId);
    }

    /**
     * 处理点赞效果
     */
    private void setOnClickLike() {
        if (map.containsKey("isLike") && "1".equals(map.get("isLike"))) {//未点赞
            // 请求网络;
            like_linear.setClickable(false);
            ReqInternet.in().doPost(StringManager.api_quanSetSubject, "type=likeList&subjectCode=" + map.get("code") + "&floorId=0&isLike" + map.get("isLike"),
                    new InternetCallback() {
                        @Override
                        public void loaded(int flag, String url, Object returnObj) {
                            like_linear.setClickable(true);
                            if (flag >= aplug.basic.ReqInternet.REQ_OK_STRING) {

                                like_img.setBackgroundResource(R.drawable.z_quan_home_body_ico_good_active);
                                map.put("isLike", "2");
                                int likenum = 0;
                                if (map.containsKey("likeNum") && !TextUtils.isEmpty(map.get("likeNum")))
                                    likenum = Integer.parseInt(map.get("likeNum"));
                                map.put("likeNum", String.valueOf(++likenum));
                                like_tv.setText((map.containsKey("likeNum") && !TextUtils.isEmpty(map.get("likeNum"))) ? map.get("likeNum") : "点赞");
                            }
                        }
                    });
        } else if (map.containsKey("isLike") && "2".equals(map.get("isLike"))) {//已点赞
            Tools.showToast(context, "您已经赞过了，谢谢！");
        }
    }
    public interface FootViewCallback{
        public void onClicklike(String clickSite);
        public void onClickview(String clickSite);
    }
}
