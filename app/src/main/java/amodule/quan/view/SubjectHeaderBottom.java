package amodule.quan.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.user.activity.FriendHome;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import xh.basic.tool.UtilImage;

/**
 * PackageName : amodule.quan.view
 * Created by MrTrying on 2016/9/28 16:45.
 * E_mail : ztanzeyu@gmail.com
 */

public class SubjectHeaderBottom extends LinearLayout {
    private LinearLayout sb_header_ll_tongjizantouxiang;
    private RelativeLayout sb_header_tv_tongjizantouxiang;
    private TextView sb_header_tv_likeNum_round;
    private TextView sb_header_tv_likeNum;
    private TextView sb_header_tv_commentNum;
    private TextView sb_header_tv_clickNum;
    private ImageView imageViewMy;//隐藏的我的点赞的头像
    private ImageView zan_num_sanjiao;

    private ArrayList<Map<String, String>> listMapByJson;//点赞人头像集合

    private int likeNum = 0;

    private int replyNum = 0;

    private int countNum;//点赞人的头像显示的数量

    public SubjectHeaderBottom(Context context) {
        this(context,null);
    }

    public SubjectHeaderBottom(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SubjectHeaderBottom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.subject_header_bottom,this);
        initView();
        listMapByJson = new ArrayList<>();
    }

    private void initView() {
        sb_header_ll_tongjizantouxiang = (LinearLayout) findViewById(R.id.sb_header_ll_tongjizantouxiang);
        sb_header_tv_tongjizantouxiang = (RelativeLayout) findViewById(R.id.sb_header_tv_tongjizantouxiang);
        sb_header_tv_likeNum_round = (TextView) findViewById(R.id.sb_header_tv_likeNum_round);
        sb_header_tv_likeNum = (TextView) findViewById(R.id.sb_header_tv_likeNum);
        sb_header_tv_commentNum = (TextView) findViewById(R.id.sb_header_tv_commentNum);
        sb_header_tv_clickNum = (TextView) findViewById(R.id.sb_header_tv_clickNum);
        zan_num_sanjiao = (ImageView) findViewById(R.id.zan_num_sanjiao);
        imageViewMy = (ImageView) sb_header_tv_tongjizantouxiang.getChildAt(0);
    }

    /**
     *
     * @param likeNum 点赞
     * @param replyNum 回复
     * @param clickNum 浏览
     */
    public void setTopData(int likeNum,int replyNum,int clickNum){
        this.likeNum = likeNum;
        this.replyNum = replyNum;
        //设置点赞数
        sb_header_tv_likeNum.setText(likeNum + "赞");
        sb_header_tv_likeNum_round.setText(String.valueOf(likeNum));
        //设置评论
        sb_header_tv_commentNum.setText(replyNum + "评论");
        //设置浏览
        sb_header_tv_clickNum.setText(clickNum + "浏览");
    }

    /**
     * 设置点赞用户头像
      * @param listMapByJson
     */
    public void setLikesShow(ArrayList<Map<String, String>> listMapByJson) {
        //每次加载或刷新的时候都隐藏自己的头像
        imageViewMy.setVisibility(View.GONE);
        int dp_33 = Tools.getDimen(getContext(), R.dimen.dp_33);
        int wid = ToolsDevice.getWindowPx(getContext()).widthPixels - dp_33 * 2;
        int widTu = dp_33;
        countNum = wid / widTu;
        if (countNum >= 10) {
            countNum = 10;
        }
        if (listMapByJson.size() != 0) {
            for (int i = 0; i < (listMapByJson.size() <= countNum ? listMapByJson.size() : countNum); i++) {
                String img_url = listMapByJson.get(i).get("imgShow");
                final String code = listMapByJson.get(i).get("code");
                ImageView imageView = (ImageView) sb_header_tv_tongjizantouxiang.getChildAt(1 + i);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XHClick.mapStat(getContext(), BarSubjectFloorOwnerNew.tongjiId, "点赞头像列点击量", "");
                        openUserHome(code);
                    }
                });
                setImg(img_url, imageView, ToolsDevice.dp2px(getContext(), 500));
            }
            sb_header_tv_tongjizantouxiang.setVisibility(View.VISIBLE);
            zan_num_sanjiao.setVisibility(View.VISIBLE);
            sb_header_ll_tongjizantouxiang.setVisibility(View.VISIBLE);
        } else {
            sb_header_tv_tongjizantouxiang.setVisibility(View.GONE);
            zan_num_sanjiao.setVisibility(View.GONE);
            sb_header_ll_tongjizantouxiang.setVisibility(View.GONE);
        }
    }

    /**
     * 点赞成功执行
     * @param returnObj
     */
    public void likeOver(Object returnObj) {
        //returnObject 是一个{"tytle",2}
        if (listMapByJson.size() != 0 && listMapByJson.size() >= countNum) {
            sb_header_tv_tongjizantouxiang.getChildAt(countNum).setVisibility(View.GONE);
        }

        imageViewMy.setVisibility(View.VISIBLE);
        String string = LoginManager.userInfo.get("img");
        setImg(string, imageViewMy, ToolsDevice.dp2px(getContext(), 500));

        sb_header_tv_tongjizantouxiang.setVisibility(View.VISIBLE);
        zan_num_sanjiao.setVisibility(View.VISIBLE);
        sb_header_ll_tongjizantouxiang.setVisibility(View.VISIBLE);

        likeNum++;
        sb_header_tv_likeNum.setText(likeNum + "赞");
        sb_header_tv_likeNum_round.setText(likeNum + "");

        imageViewMy.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FriendHome.class);
                intent.putExtra("code", LoginManager.userInfo.get("code"));
                getContext().startActivity(intent);
            }
        });
    }

    /**回复成功*/
    public void replyOver() {
        replyNum++;
        sb_header_tv_commentNum.setText(replyNum + "评论");
    }

    /**
     * 设置图片
     * @param img_url
     * @param imageView
     * @param roundImgPixels
     */
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

    /**
     *
     * @param img_view
     * @param url
     * @param roundImgPixels
     * @return
     */
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

    /**
     * 打开user首页
     * @param code
     */
    private void openUserHome(final String code) {
        Intent intent = new Intent(getContext(), FriendHome.class);
        Bundle bundle = new Bundle();
        bundle.putString("code", code);
        intent.putExtras(bundle);
        getContext().startActivity(intent);
    }
}