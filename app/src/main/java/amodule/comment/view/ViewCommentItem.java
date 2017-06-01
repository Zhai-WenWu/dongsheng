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
import acore.logic.LoginManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;
import acore.widget.multifunction.CommentBuilder;
import acore.widget.multifunction.view.MultifunctionTextView;
import amodule.dish.activity.MoreImageShow;
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

    private String comment_id;
    private String gotoCommentId,gotoReplayId;

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
        replayContentShow = (TextView) findViewById(R.id.comment_item_replay_cotent_show);

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

    public void setGotoItem(String gotoCommentId,String gotoReplayId){
        this.gotoCommentId = gotoCommentId;
        this.gotoReplayId = gotoReplayId;
    }

    private void initUserInfo(){
        String custome = dataMap.get("customer");
        ArrayList<Map<String, String>> customeArray = StringManager.getListMapByJson(custome);
        if(customeArray.size() > 0){
            cusstomMap = customeArray.get(0);
            setUserImage(userIcon,cusstomMap.get("header_img"));
            userIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    goFriendHome(cusstomMap.get("ucode"));
                }
            });
            if(!TextUtils.isEmpty(cusstomMap.get("is_gourmet")) && !"null".equals(cusstomMap.get("is_gourmet"))){
                AppCommon.setUserTypeImage(Integer.valueOf(cusstomMap.get("is_gourmet")), userType);
            }
            String nickName = cusstomMap.get("nick_name");
            if(TextUtils.isEmpty(nickName)) nickName = "";
            userName.setText(nickName.length() < 6 ? nickName : nickName.subSequence(0,5) + "...");
            userName.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    goFriendHome(cusstomMap.get("ucode"));
                }
            });
            if(!TextUtils.isEmpty(cusstomMap.get("name_color")))
                userName.setTextColor(Color.parseColor(cusstomMap.get("name_color")));
            AppCommon.setVip((Activity) mContext,userVip,cusstomMap.get("is_member"));
        }
    }

    private void initContent(){
        commentContent.removeAllViews();
        comment_id = dataMap.get("comment_id");
        String content = dataMap.get("content");
        ArrayList<Map<String, String>> contentArray = StringManager.getListMapByJson(content);
        for(Map<String, String> contentMap:contentArray) {
            initCotentView(contentMap);
        }
    }

    private boolean isShowContentClick = false;
    private void initCotentView(final Map<String, String> contentMap){
        View view = layoutInflater.inflate(R.layout.a_comment_item_content,null);
        final String text = contentMap.get("text");
        final MultifunctionTextView contentText = (MultifunctionTextView) view.findViewById(R.id.commend_cotent_text);
        if(TextUtils.isEmpty(gotoReplayId) && !TextUtils.isEmpty(gotoCommentId) && gotoCommentId.equals(comment_id)){
            contentText.setBackgroundColor(Color.parseColor("#fffae3"));
            if(mListener != null)mListener.onContentReplayClick(comment_id,cusstomMap.get("ucode"), cusstomMap.get("nick_name"));
        }else{
            contentText.setBackgroundColor(Color.parseColor("#00fffae3"));
        }
        if(TextUtils.isEmpty(text)){
            contentText.setVisibility(View.GONE);
        }else {
            contentText.setVisibility(View.VISIBLE);
            contentText.setNormBackColor(mContext.getResources().getColor(R.color.common_bg));
            int maxNum = 100;
            if (TextUtils.isEmpty(text) || text.length() <= maxNum) {
                contentText.setText(text);
            } else {
                String newText = text.substring(0, maxNum);
                MultifunctionTextView.MultifunctionText multifunctionText = new MultifunctionTextView.MultifunctionText();
                CommentBuilder textBuilder = new CommentBuilder(newText).setTextColor("#535353");
                textBuilder.parse(null);
                multifunctionText.addStyle(textBuilder.getContent(), textBuilder.build());
                CommentBuilder showBuilder = new CommentBuilder("...>").setTextColor("#bcbcbc");
                showBuilder.parse(new CommentBuilder.CommentClickCallback() {
                    @Override
                    public void onCommentClick(View v, String userCode) {
                        isShowContentClick = true;
                        contentText.setText(text);
                        contentText.setCopyText(text);
                    }
                });
                multifunctionText.addStyle(showBuilder.getContent(), showBuilder.build());
                contentText.setText(multifunctionText);
                contentText.setCopyText(newText + "...>");
            }
            contentText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null && !isShowContentClick)
                        mListener.onContentReplayClick(dataMap.get("comment_id"), cusstomMap.get("ucode"), cusstomMap.get("nick_name"));
                    isShowContentClick = false;
                }
            });
            String ucode = cusstomMap.get("ucode");
            final boolean isReport = TextUtils.isEmpty(ucode) || !ucode.equals(LoginManager.userInfo.get("code"));
            contentText.setRightClicker(isReport ? "举报" : "删除", new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        if (isReport)
                            mListener.onReportCommentClick(dataMap.get("comment_id"),cusstomMap.get("ucode"),cusstomMap.get("nick_name"));
                        else
                            mListener.onDeleteCommentClick(dataMap.get("comment_id"));
                    }
                }
            });
        }
        final String imgs = contentMap.get("imgs");
        ArrayList<Map<String, String>> contentArray = StringManager.getListMapByJson(imgs);
        switch (contentArray.size()){
            case 3:
                ImageView imageView3 = (ImageView) view.findViewById(R.id.commend_cotent_img3);
                setImg(imageView3,contentArray.get(2).get(""));
                imageView3.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImg(imgs,2);
                    }
                });
            case 2:
                ImageView imageView2 = (ImageView) view.findViewById(R.id.commend_cotent_img2);
                setImg(imageView2,contentArray.get(1).get(""));
                imageView2.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImg(imgs,1);
                    }
                });
            case 1:
                ImageView imageView1 = (ImageView) view.findViewById(R.id.commend_cotent_img1);
                setImg(imageView1,contentArray.get(0).get(""));
                imageView1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showImg(imgs,0);
                    }
                });
                break;
            default:
                view.findViewById(R.id.comment_content_imglayout).setVisibility(View.GONE);
                break;
        }
        commentContent.addView(view);
    }

    private void showImg(String imgs,int index){
        ArrayList<Map<String, String>> contentArray = StringManager.getListMapByJson(imgs);
        for(int i = 0; i < contentArray.size(); i++){
            Map<String,String> map = contentArray.get(i);
            map.put("img", map.get(""));
            map.put("info", "");
            map.put("num", "" + (i+1));
        }
        Intent intent = new Intent(mContext, MoreImageShow.class);
        intent.putExtra("data",contentArray);
        intent.putExtra("index", index);
        intent.putExtra("isShowAd", false);
        mContext.startActivity(intent);
    }

    private OnCommentItenListener mListener;
    public void setCommentItemListener(OnCommentItenListener listener){
        mListener = listener;
    }

    private void initReplay(){
        String replay = dataMap.get("replay");
        String replay_num = dataMap.get("replay_num");
        commentReplay.removeAllViews();
        addReplayView(replay);
        if(!TextUtils.isEmpty(replay_num) && Integer.parseInt(replay_num) > 0){
            replayContentShow = (TextView) findViewById(R.id.comment_item_replay_cotent_show);
            replayContentShow.setVisibility(View.VISIBLE);
            replayContentShow.setText("展现" + replay_num + "条回复 >");
            replayContentShow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    replayContentShow.setVisibility(View.GONE);
                    if(mListener != null) mListener.onShowAllReplayClick(dataMap.get("comment_id"));;

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
            replayTv.setNormBackColor(Color.parseColor("#efefef"));
            String content = replayMap.get("content");
            String uName = replayMap.get("uname");
            final String ucode = replayMap.get("ucode");
            final String is_author = replayMap.get("is_author");
            final String replay_uname = replayMap.get("replay_uname");
            final String replay_ucode = replayMap.get("replay_ucode");
            final String is_replay_author = replayMap.get("is_replay_author");

            String authoCode = null;

            if(comment_id.equals(gotoCommentId) && replayMap.get("replay_id").equals(gotoReplayId)){
                view.setBackgroundColor(Color.parseColor("#fffae3"));
                if(mListener != null) mListener.onContentReplayClick(dataMap.get("comment_id"),replay_ucode,replay_uname);
            }else{
                view.setBackgroundColor(Color.parseColor("#00fffae3"));
            }
            MultifunctionTextView.MultifunctionText multifunctionText = new MultifunctionTextView.MultifunctionText();
            CommentBuilder uNameBuilder = new CommentBuilder(uName).setTextColor("#bcbcbc");
            uNameBuilder.parse(new CommentBuilder.CommentClickCallback() {
                @Override
                public void onCommentClick(View v, String userCode) {
                    goFriendHome(ucode);
                }
            });
            multifunctionText.addStyle(uNameBuilder.getContent(), uNameBuilder.build());
            if("2".equals(is_author)) {
                authoCode = ucode;
                CommentBuilder authorBuilder = new CommentBuilder("作者").setTextColor("#590e04");
                authorBuilder.parse(null);
                multifunctionText.addStyle(authorBuilder.getContent(), authorBuilder.build());
            }
            if(!TextUtils.isEmpty(replay_uname)) {
                CommentBuilder replayHintBuilder = new CommentBuilder(" 回复 ").setTextColor("#535353");
                replayHintBuilder.parse(null);
                multifunctionText.addStyle(replayHintBuilder.getContent(), replayHintBuilder.build());

                CommentBuilder replayNameBuilder = new CommentBuilder(replay_uname).setTextColor("#bcbcbc");
                replayNameBuilder.parse(new CommentBuilder.CommentClickCallback() {
                    @Override
                    public void onCommentClick(View v, String userCode) {
                        goFriendHome(replay_ucode);
                    }
                });
                multifunctionText.addStyle(replayNameBuilder.getContent(), replayNameBuilder.build());
                if ("2".equals(is_replay_author)) {
                    authoCode = replay_ucode;
                    CommentBuilder authorBuilder = new CommentBuilder("作者").setTextColor("#590e04");
                    authorBuilder.parse(null);
                    multifunctionText.addStyle(authorBuilder.getContent(), authorBuilder.build());
                }
            }
            CommentBuilder contentBuilder = new CommentBuilder(" : " + content).setTextColor("#535353");
            contentBuilder.parse(new CommentBuilder.CommentClickCallback() {
                @Override
                public void onCommentClick(View v, String userCode) {
                    if(mListener != null) mListener.onContentReplayClick(dataMap.get("comment_id"),replay_ucode,replay_uname);
                }
            });
            multifunctionText.addStyle(contentBuilder.getContent(), contentBuilder.build());

            replayTv.setText(multifunctionText);
            replayTv.setCopyText(content);
            final boolean isReport = TextUtils.isEmpty(authoCode) || !authoCode.equals(LoginManager.userInfo.get("code"));
            replayTv.setRightClicker(isReport ? "举报" : "删除" ,new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null) {
                        if(isReport)
                            mListener.onReportReplayClick(dataMap.get("comment_id"),replayMap.get("replay_id"),replay_ucode,replay_uname);
                        else
                            mListener.onDeleteReplayClick(dataMap.get("comment_id"),replayMap.get("replay_id"));
                    }
                }
            });
            commentReplay.addView(view);
        }
    }

    private void goFriendHome(String code){
        Intent intent = new Intent(mContext, FriendHome.class);
        intent.putExtra("code", code);
        mContext.startActivity(intent);
    }

    private void initOther(){
        commentTime.setText(dataMap.get("create_time"));
        commentPraiseNum.setText(dataMap.get("fabulous_num"));
        commentPraise.setImageResource("2".equals(dataMap.get("is_fabulous")) ? R.drawable.i_comment_praise_ok : R.drawable.i_comment_praise);
        commentPraise.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) mListener.onPraiseClick(dataMap.get("comment_id"));
                commentPraise.setImageResource(R.drawable.i_comment_praise_ok);
            }
        });
        String is_del_report = dataMap.get("is_del_report");
        final boolean isDelete = "2".equals(is_del_report);
        View commentReplay = findViewById(R.id.comment_replay);
        commentReplay.setVisibility(isDelete ? View.GONE : View.VISIBLE);
        commentReplay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isDelete && mListener != null)
                    mListener.onContentReplayClick(dataMap.get("comment_id"),cusstomMap.get("ucode"),cusstomMap.get("nick_name"));
            }
        });
        commentDelete.setText(isDelete ? "删除":"举报");
        commentDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    if (isDelete)
                        mListener.onDeleteCommentClick(dataMap.get("comment_id"));
                    else
                        mListener.onReportCommentClick(dataMap.get("comment_id"),cusstomMap.get("ucode"),cusstomMap.get("nick_name"));
                }
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
        public void onReportCommentClick(String comment_id,String comment_user_code,String comment_user_name);
        public void onDeleteCommentClick(String comment_id);
        public void onReportReplayClick(String comment_id, String replay_id,String replay_user_code,String replay_user_name);
        public void onDeleteReplayClick(String comment_id, String replay_id);
        public void onPraiseClick(String comment_id);
        public void onContentReplayClick(String comment_id,String replay_user_code, String replay_user_name);
    }


}
