package amodule.comment.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.multifunction.CommentBuilder;
import acore.widget.multifunction.view.MultifunctionTextView;
import amodule.user.activity.FriendHome;
import aplug.basic.LoadImage;

/**
 * Created by Fang Ruijiao on 2017/5/25.
 */

public class ViewCommentItem extends LinearLayout {

    private Context mContext;
    private LayoutInflater layoutInflater;
    private ImageView userIcon,userType,userVip,commentPraise;
    private TextView userName,commentTime,commentDelete,commentPraiseNum,replayContentShow;

    private LinearLayout commentContent,commentReplay;

    private Map<String,String> dataMap;
    private Map<String, String> cusstomMap;

    public ViewCommentItem(Context context) {
        super(context);
        initView(context);
    }

    public ViewCommentItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context){
        mContext = context;
        layoutInflater = LayoutInflater.from(context);
        layoutInflater.inflate(R.layout.a_comment_item_combination,this);

        userIcon = (ImageView) findViewById(R.id.commend_user_icon);
        userType = (ImageView) findViewById(R.id.commend_user_userType);
        userName = (TextView) findViewById(R.id.comment_user_name);
        userVip = (ImageView) findViewById(R.id.comment_user_vip);
        commentTime = (TextView) findViewById(R.id.comment_time);
        commentDelete = (TextView) findViewById(R.id.comment_delete);
        commentPraise = (ImageView) findViewById(R.id.comment_praise);
        commentPraiseNum = (TextView) findViewById(R.id.comment_praise_num);

        commentContent = (LinearLayout) findViewById(R.id.comment_content);
        commentReplay = (LinearLayout) findViewById(R.id.comment_item_replay_cotent);
    }

    public void setData(Map<String,String> map){
        dataMap = map;
        initUserInfo();
        initContent();
        initReplay();
        initOther();
    }

    private void initUserInfo(){
        String custome = dataMap.get("custome");
        ArrayList<Map<String, String>> customeArray = StringManager.getListMapByJson(custome);
        if(customeArray.size() > 0){
            cusstomMap = customeArray.get(0);
            setUserImage(userIcon,cusstomMap.get("header_img"));
            AppCommon.setUserTypeImage(Integer.valueOf(cusstomMap.get("is_gourmet")), userType);
            userName.setText(cusstomMap.get("nick_name"));
            userName.setTextColor(Color.parseColor(cusstomMap.get("name_color")));
            AppCommon.setVip((Activity) mContext,userVip,cusstomMap.get("is_member"));
        }
    }

    private void initContent(){
        String content = dataMap.get("content");
        ArrayList<Map<String, String>> contentArray = StringManager.getListMapByJson(content);
        for(Map<String, String> contentMap:contentArray) {
            initCotentView(contentMap);
        }
    }

    private void initCotentView(Map<String, String> contentMap){
        View view = layoutInflater.inflate(R.layout.a_comment_item_content,null);
        TextView contentText = (TextView) view.findViewById(R.id.commend_cotent_text);
        contentText.setText(contentMap.get("text"));
        String imgs = contentMap.get("imgs");
        ArrayList<Map<String, String>> contentArray = StringManager.getListMapByJson(imgs);
        switch (contentArray.size()){
            case 1:
                ImageView imageView1 = (ImageView) view.findViewById(R.id.commend_cotent_img1);
                setImg(imageView1,contentArray.get(0).get(""));
                break;
            case 2:
                ImageView imageView2 = (ImageView) view.findViewById(R.id.commend_cotent_img2);
                setImg(imageView2,contentArray.get(0).get(""));
                break;
            case 3:
                ImageView imageView3 = (ImageView) view.findViewById(R.id.commend_cotent_img3);
                setImg(imageView3,contentArray.get(3).get(""));
                break;
        }
        commentContent.addView(view);
    }

    private OnCommentItenListener mListener;
    public void setCommentItemListener(OnCommentItenListener listener){
        mListener = listener;
    }

    private void initReplay(){
        String replay = dataMap.get("replay");
        String replay_num = dataMap.get("replay_num");
        addReplayView(replay);
        if(!TextUtils.isEmpty(replay_num) && Integer.parseInt(replay_num) > 0){
            replayContentShow = (TextView) findViewById(R.id.comment_item_replay_cotent_show);
            replayContentShow.setVisibility(View.VISIBLE);
            replayContentShow.setText("展现" + replay_num + "条回复 >");
            replayContentShow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    replayContentShow.setVisibility(View.GONE);
                    if(mListener != null){
                        mListener.onShowAllReplayClick(dataMap.get("comment_id"));;
                    }
                }
            });
        }else{
            replayContentShow.setVisibility(View.GONE);
        }
    }

    public void addReplayView(String replay){
        ArrayList<Map<String, String>> replayArray = StringManager.getListMapByJson(replay);
        View view;
        MultifunctionTextView replayTv;
        for(final Map<String, String> replayMap:replayArray) {
            view = layoutInflater.inflate(R.layout.a_comment_item_replay_cotent,null);
            replayTv = (MultifunctionTextView) view.findViewById(R.id.comment_item_replay_item_tv);
            String content = replayMap.get("content");
            String uName = replayMap.get("uname");
            final String ucode = replayMap.get("ucode");
            final String is_author = replayMap.get("is_author");
            String replay_uname = replayMap.get("replay_uname");
            final String replay_ucode = replayMap.get("replay_ucode");
            final String is_replay_author = replayMap.get("is_replay_author");

            MultifunctionTextView.MultifunctionText multifunctionText = new MultifunctionTextView.MultifunctionText();
            CommentBuilder uNameBuilder = new CommentBuilder(uName).setTextColor("#bcbcbc");
            uNameBuilder.parse(new CommentBuilder.CommentClickCallback() {
                @Override
                public void onCommentClick(View v, String userCode) {
                    Intent intent = new Intent(mContext, FriendHome.class);
                    intent.putExtra("code", ucode);
                    mContext.startActivity(intent);
                }
            });
            multifunctionText.addStyle(uNameBuilder.getContent(), uNameBuilder.build());
            if("2".equals(is_author)) {
                CommentBuilder authorBuilder = new CommentBuilder("作者").setTextColor("#ffffff").setBackgroudColor("#cccccc");
                authorBuilder.parse(null);
                multifunctionText.addStyle(authorBuilder.getContent(), authorBuilder.build());
            }
            CommentBuilder replayNameBuilder = new CommentBuilder(replay_uname).setTextColor("#bcbcbc");
            replayNameBuilder.parse(new CommentBuilder.CommentClickCallback() {
                @Override
                public void onCommentClick(View v, String userCode) {
                    Intent intent = new Intent(mContext, FriendHome.class);
                    intent.putExtra("code", replay_ucode);
                    mContext.startActivity(intent);
                }
            });
            multifunctionText.addStyle(replayNameBuilder.getContent(), replayNameBuilder.build());
            if("2".equals(is_replay_author)) {
                CommentBuilder authorBuilder = new CommentBuilder("作者").setTextColor("#ffffff").setBackgroudColor("#cccccc");
                authorBuilder.parse(null);
                multifunctionText.addStyle(authorBuilder.getContent(), authorBuilder.build());
            }
            CommentBuilder contentBuilder = new CommentBuilder(": " + content).setTextColor("#535353");
            contentBuilder.parse(null);
            multifunctionText.addStyle(contentBuilder.getContent(), contentBuilder.build());
            replayTv.setText(multifunctionText);
            replayTv.setRightClicker(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onReportClick(dataMap.get("comment_id"),replayMap.get("replay_id"));
                }
            });
            commentReplay.addView(view);
        }
    }

    private void initOther(){
        commentTime.setText(dataMap.get("create_time"));
        commentPraiseNum.setText(dataMap.get("fabulous_num"));
        commentPraise.setImageResource("2".equals(dataMap.get("is_fabulous")) ? R.drawable.i_comment_praise_ok : R.drawable.i_comment_praise);
        commentPraise.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPraiseClick(dataMap.get("comment_id"));
            }
        });
        String is_del_report = dataMap.get("is_del_report");
        final boolean isDelete = "2".equals(is_del_report);
        commentDelete.setText(isDelete ? "删除":"举报");
        commentDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDeleteOrReportClick(dataMap.get("comment_id"), isDelete);
            }
        });
    }

    private void setImg(ImageView imageView, String url){
        Glide.with(mContext).load(url) .into(imageView);
    }

    public void setUserImage(final ImageView v, String value) {
        v.setVisibility(View.VISIBLE);
        v.setScaleType(ImageView.ScaleType.CENTER_CROP);
        v.setImageResource(R.drawable.bg_round_grey_e0e0e0_50);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mContext)
                .load(value)
                .setImageRound(ToolsDevice.dp2px(mContext, 500))
                .build();
        if(bitmapRequest != null)
            bitmapRequest.into(v);
    }

    public interface OnCommentItenListener{
        public void onShowAllReplayClick(String comment_id);
        public void onReportClick(String comment_id,String replay_id);
        public void onDeleteOrReportClick(String comment_id,boolean isDelete);
        public void onPraiseClick(String comment_id);
    }


}
