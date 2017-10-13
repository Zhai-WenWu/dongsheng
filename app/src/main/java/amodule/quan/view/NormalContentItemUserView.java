package amodule.quan.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.TextViewShow;
import amodule.quan.activity.CircleRobSofa;
import amodule.user.activity.login.LoginByAccout;
import amodule.user.view.UserIconView;
import aplug.basic.InternetCallback;
import third.mall.tool.ToolView;
import xh.basic.tool.UtilString;
import xh.windowview.BottomDialog;
import xh.windowview.XhDialog;

/**
 * NormarlContentView---用户相关
 */
public class NormalContentItemUserView extends NormarlContentItemView {
    private View view;//父类的view
    private Activity mAct;
    private ImageView auther_userImg, follow_img,normal_friend_select;
    private TextView auther_name, user_time, follow_tv,tv_recommend;
    private UserIconView userIconView;
    private ImageView userVip;
    private RelativeLayout follow_rela;
    public ArrayList<Map<String, String>> customers;
    private Map<String,String> map;
    private FrameLayout frame_send;
    private TextViewShow  quan_title_2;
    private final String noData = "       ";// 实现样式的空字符串
    private final String sendData = "      ";
    private BottomDialog bottomDialog;
    private int position;
    private boolean isOnClickUser= true;

    private View adHintView;

    public NormalContentItemUserView(Activity act,View view) {
        super(act);
        this.view= view;
        this.mAct=act;
    }

    /**
     * 头像是否可点击
     * @param isOnClickUser
     */
    public void setIsOnClickUser(boolean isOnClickUser){
        this.isOnClickUser = isOnClickUser;
    }

    @Override
    protected void initView() {
        adHintView = view.findViewById(R.id.ad_tag);
        auther_userImg = (ImageView)view. findViewById(R.id.auther_userImg);
        auther_name = (TextView)view. findViewById(R.id.auther_name);
        userIconView = (UserIconView)view. findViewById(R.id.usericonview);
        userVip = (ImageView) view. findViewById(R.id.i_user_vip);
        user_time = (TextView)view. findViewById(R.id.user_time);
        follow_img = (ImageView)view. findViewById(R.id.follow_img);
        follow_tv = (TextView)view. findViewById(R.id.follow_tv);
        normal_friend_select = (ImageView)view. findViewById(R.id.normal_friend_select);
        //关注控件
        follow_rela= (RelativeLayout)view. findViewById(R.id.follow_rela);

        // 发送相关
        frame_send = (FrameLayout)view. findViewById(R.id.frame_send);
        // 标题相关
        quan_title_2 = (TextViewShow)view. findViewById(R.id.quan_title_2);
        quan_title_2.setHaveCopyFunction(false);
        quan_title_2.setFaceWH(Tools.getDimen(mAct,R.dimen.dp_17));
        tv_recommend = (TextView)view. findViewById(R.id.tv_recommend);
    }

    public void setIsRobof(boolean isRobsof){
        this.isRobsof= isRobsof;
    }
    @Override
    public void setViewData(Map<String,String> maps,int position) {
        this.map= maps;
        String statisKey = isRobsof ? "抢沙发" : "贴子";

        view.findViewById(R.id.circle_robsof_title).setVisibility(isRobsof ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.circle_robsof_title).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = TextUtils.isEmpty(normarlContentData.getStatisID()) ? "a_quan_homepage" : normarlContentData.getStatisID();
                XHClick.mapStat(mAct, id, "抢沙发", "更多");
                Intent intent = new Intent(mAct, CircleRobSofa.class);
                intent.putExtra("cid", map.get("cid"));
                mAct.startActivity(intent);
            }
        });
        customers = UtilString.getListMapByJson(map.get("customer"));
        setViewImage(auther_userImg, customers.get(0).get("img"));
        setViewText(auther_name, customers.get(0).get("nickName"));
        try {
            auther_name.setTextColor(Color.parseColor(customers.get(0).get("color")));
        } catch (Exception e) {
            auther_name.setTextColor(Color.parseColor("#333333"));
        }
        if(customers.get(0).containsKey("isGourmet")&&"2".equals(customers.get(0).get("isGourmet"))){
            view.findViewById(R.id.cusType).setVisibility(View.VISIBLE);
        }else view.findViewById(R.id.cusType).setVisibility(View.GONE);

        String id = TextUtils.isEmpty(normarlContentData.getStatisID()) ? "a_quan_homepage" : normarlContentData.getStatisID();
        boolean isVip = AppCommon.setVip(mAct,userVip,customers.get(0).get("vip"),id,statisKey, AppCommon.VipFrom.POST_LIST);
        int showNum = userIconView.setData(customers.get(0).get("sex"), customers.get(0).get("lv"), "");
        if(isVip)showNum ++;
        auther_name.setMaxWidth(ToolsDevice.dp2px(mAct, (float) (190 - showNum * 19)));

        setViewText(user_time, map.get("timeShowV43"));

        //1---个人中心；查看别人的贴子，2--查看自己的贴子
        if(map.containsKey("isMe")&& !TextUtils.isEmpty(map.get("isMe"))){
            if("2".equals(map.get("isMe"))) {
                normal_friend_select.setVisibility(View.VISIBLE);
            }else normal_friend_select.setVisibility(View.GONE);
            view.findViewById(R.id.follow_rela).setVisibility(View.GONE);
        }else {
            normal_friend_select.setVisibility(View.GONE);
            view.findViewById(R.id.follow_rela).setVisibility(View.VISIBLE);
            //修改关注状态
            setFollowState(customers.get(0));
        }

        changeTitleView();

        if ("ad".equals(map.get("adStyle")) && "1".equals(map.get("hideAdTag"))) {
            view.findViewById(R.id.ad_tag).setVisibility(INVISIBLE);
        } else {
            view.findViewById(R.id.ad_tag).setVisibility(VISIBLE);
        }
    }

    /**
     * 改变titile
     */
    private void changeTitleView(){
        // *****************************对标题的处理start************************************
        // 推荐
        String color = Tools.getColorStr(mAct, R.color.comment_color);
        tv_recommend.setTextColor(Color.parseColor(color));
        // 置顶贴  0---不置顶，1为置顶
        if (map.get("isOverHead") != null && map.get("isOverHead").equals("1")) {
            setViewText(tv_recommend, "置顶");
            tv_recommend.setBackgroundResource(R.drawable.round_red);
            setTitleData(noData);
        }
        else if (map.get("style") != null && map.get("style").equals("3")) {
            setViewText(tv_recommend, "推荐");
            tv_recommend.setBackgroundResource(R.drawable.round_red);
            setTitleData(noData);
        }
        // 菜谱
        else if (map.get("type") != null && map.get("type").equals("5") || map.get("type") != null && map.get("type").equals("3")) {
            setViewText(tv_recommend, "菜谱");
            tv_recommend.setBackgroundResource(R.drawable.round_red);
            setTitleData(noData);
        }
        // 菜谱
        else if (map.get("isDish") != null && map.get("isDish").equals("3")) {
            setViewText(tv_recommend, "菜谱");
            tv_recommend.setBackgroundResource(R.drawable.round_red);
            setTitleData(noData);
        }
        // 精华贴
        else if (map.get("isEssence") != null && map.get("isEssence").equals("2")) {
            setViewText(tv_recommend, "精华");
            tv_recommend.setBackgroundResource(R.drawable.round_red);
            setTitleData(noData);
        }
        // 活动
        else if (map.get("style") != null && map.get("style").equals("4")) {
            setViewText(tv_recommend, "活动");
            tv_recommend.setBackgroundResource(R.drawable.round_red);
            setTitleData(noData);
        }
        // 知识
        else if (map.get("type") != null && map.get("type").equals("4")) {
            setViewText(tv_recommend, "知识");
            tv_recommend.setBackgroundResource(R.drawable.round_red);
            setTitleData(noData);
        }
        // 推广
//        else if (map.get("isPromotion") != null && map.get("isPromotion").equals("1")) {
//            setViewText(tv_recommend, "推广");
//            tv_recommend.setBackgroundResource(R.drawable.round_red);
//            setTitleData(noData);
//        }
        // 普通
        else {
            setViewText(tv_recommend, "");
            setTitleData("");
        }
        // *****************************对标题的处理end**************************************

    }
    @Override
    public void setShowUpload(boolean state) {
        if(state){
            if(frame_send.getVisibility() == View.VISIBLE){
                setTitleData("");
            }
            frame_send.setVisibility(View.GONE);
            if(isOnClickUser) {
                setListener(auther_userImg, typeUser, "用户头像");
                setListener(auther_name, typeUser, "用户昵称");
                setListener(userIconView, typeUser, "");
            }else{
                setListener(auther_userImg, -1, "用户头像");
                setListener(auther_name, -1, "用户昵称");
                setListener(userIconView, -1, "");
            }
            setListener(adHintView,typeAdHint,"广告提示");
            setListener(quan_title_2, typeSubject,  "贴子内容");
            follow_rela.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!LoginManager.isLogin()) {
                        Intent intent = new Intent(context, LoginByAccout.class);
                        context.startActivity(intent);
                        return;
                    }
                    Map<String,String> userMap = StringManager.getFirstMap(map.get("customer"));
                    if( userMap != null
                            && userMap.get("folState") != null
                            && userMap.get("code") != null
                            && userMap.get("folState").equals("2")) {
                        boolean isFollow = customers.get(0).containsKey("folState") && "2".equals(customers.get(0).get("folState"));
                        XHClick.mapStat(mAct, normarlContentData.getStatisID(), normarlContentData.getStatisKey(), isFollow ? "关注" : "已关注");
                        AppCommon.onAttentionClick(userMap.get("code"), "follow");
                        customers.get(0).put("folState", "3");
                        Tools.showToast(mAct, "已关注");
                        setFollowState(customers.get(0));
                        map.put("customer", MapToJson(customers.get(0)).toString());
                    }
                }
            });
            normal_friend_select.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //
                    showFirendSelect();
                }
            });
        }else{
            frame_send.setVisibility(View.VISIBLE);
            setTitleData(sendData);
        }
    }

    @Override
    public void onClickCallback(int type,String statisValue) {
        if(normarlViewOnClickCallBack!=null)
            normarlViewOnClickCallBack.onClickViewIndex(type,statisValue);
    }

    @Override
    public void onAdClickCallback(View v,String eventId){
        if(mAdHintClickCallback != null)
            mAdHintClickCallback.onAdHintListener(v,eventId);
    }

    /** 修改关注状态     */
    private void setFollowState(Map<String, String> cursterMap) {
        if (!TextUtils.isEmpty(normarlContentData.getModuleName()) && normarlContentData.getModuleName().equals("关注")) {
            follow_rela.setVisibility(View.GONE);
            return;
        } else {
            follow_rela.setVisibility(View.VISIBLE);
        }
        if (cursterMap.containsKey("folState") && "2".equals(cursterMap.get("folState"))) {//未关注
            follow_rela.setVisibility(View.VISIBLE);
            int dp_10 = Tools.getDimen(mAct, R.dimen.dp_10);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp_10, dp_10);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            follow_img.setLayoutParams(layoutParams);
            follow_img.setBackgroundResource(R.drawable.dish_follow_a);
            view.findViewById(R.id.follow_rela).setBackgroundResource(R.drawable.bg_circle_follow_5);
            follow_tv.setText("关注");
            String color = Tools.getColorStr(mAct, R.color.comment_color);
            follow_tv.setTextColor(Color.parseColor(color));
        } else if (cursterMap.containsKey("folState") && "3".equals(cursterMap.get("folState"))) {//已关注
            follow_rela.setVisibility(View.VISIBLE);
            int dp_12 = Tools.getDimen(mAct, R.dimen.dp_12);
            int dp_9 = Tools.getDimen(mAct, R.dimen.dp_9);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp_12, dp_9);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            layoutParams.setMargins(0, Tools.getDimen(mAct, R.dimen.dp_1), 0, 0);
            follow_img.setLayoutParams(layoutParams);
            follow_img.setBackgroundResource(R.drawable.circle_follow_user_right);
            follow_tv.setText("已关注");
            follow_tv.setTextColor(Color.parseColor("#999999"));
            view.findViewById(R.id.follow_rela).setBackgroundColor(Color.parseColor("#fffffe"));
        } else {
            follow_rela.setVisibility(View.GONE);
        }

    }

    private void setTitleData(String nodata) {
        String title = map.get("title");
        if (map.containsKey("content") && !TextUtils.isEmpty(map.get("content"))) {
            String content = map.get("content");
            content.trim();
            content = content.replaceAll("\\s+", " ");
            if (!TextUtils.isEmpty(content)) {
                if(!TextUtils.isEmpty(title))
                    title = "【" + title + "】";
                title += content;
            }
        }else if(!"".equals(nodata))
            nodata += "  ";
        int lineCount = setTextViewNum(0);
        String textValue = new String(title);
        if(textValue.length() > lineCount * 3){
            textValue = textValue.substring(0,lineCount * 3 - 3);
            textValue += "...";
        }
        textValue = nodata + textValue;
        quan_title_2.setVisibility(View.VISIBLE);
        quan_title_2.setText(textValue);
    }
    /**
     * 获取值得买每行的字数
     * @return
     */
    private int setTextViewNum(int distance_commend) {
        WindowManager wm = (WindowManager) mAct.getSystemService(Context.WINDOW_SERVICE);
        int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_17);
        int distance = (int) this.getResources().getDimension(R.dimen.dp_15);

        int waith = wm.getDefaultDisplay().getWidth();
        int tv_waith = waith - distance * 2 - distance_commend;
        int tv_pad = ToolView.dip2px(mAct, 1.0f);
        int num = (tv_waith + tv_pad) / (tv_distance + tv_pad);
        return num;
    }

    private void showFirendSelect(){
        //
        String topName="置顶";
        if(map.containsKey("isOverHead")&&"1".equals(map.get("isOverHead"))){
            topName= "取消置顶";
        }else {
            if(!map.containsKey("isOverHead"))
                map.put("isOverHead","0");
            topName="置顶";
        }
        bottomDialog = new BottomDialog(mAct);
        bottomDialog.setTopButton(topName, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mAct,"置顶",Toast.LENGTH_LONG).show();
                setRequestTopState();
            }
        }).setBottomButton("删除", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mAct,"删除",Toast.LENGTH_LONG).show();
                bottomDialog.cancel();
                showDeleteSubjectDialog();
            }
        }).setCanselButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mAct,"取消",Toast.LENGTH_LONG).show();
                bottomDialog.cancel();
            }
        });
        bottomDialog.show();
    }
    /**
     * 删除美食贴
     */
    private void showDeleteSubjectDialog(){
        final XhDialog dialog = new XhDialog(mAct);
        dialog.setTitle("真的要删除这个贴子么?").
                setSureButton("删除", new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        deleteSubject();
                        dialog.cancel();
                    }
                }).setCanselButton("取消", new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.show();
    }
    /**
     * 删除该美食贴
     */
    private void deleteSubject(){
        String url=StringManager.api_deleteSubject;
        url+="?userCode="+customers.get(0).get("code");
        url+="&subjectCode="+map.get("code");
        aplug.basic.ReqInternet.in().doGet(url, new InternetCallback(mAct) {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i>= aplug.basic.ReqInternet.REQ_OK_STRING){//成功
                    deleteSubjectCallBack.deleteSubjectPosition(position);
                }

            }
        });
    }
    /**
     * 处理置顶
     */
    private void setRequestTopState(){
        String url=StringManager.api_subjectOverHead;
        url+="?userCode="+customers.get(0).get("code");
        url+="&subjectCode="+map.get("code");
        final boolean isConfirm;
        String type="";
        if(map.containsKey("isOverHead")&&"1".equals(map.get("isOverHead"))){
            type="cancel";
            isConfirm = false;
        }
        else {
            type="confirm";
            isConfirm = true;
        }
        url+="&type="+type;
        aplug.basic.ReqInternet.in().doGet(url, new InternetCallback(mAct) {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i>= aplug.basic.ReqInternet.REQ_OK_STRING){//成功
                    setTopchange();
                    if(onTitleTopStateCallBack != null)onTitleTopStateCallBack.onTitleTopStateCallBack(true,isConfirm,position,o);
                }else{//失败
                    if(onTitleTopStateCallBack != null)onTitleTopStateCallBack.onTitleTopStateCallBack(false,isConfirm,position,o);
                }
                if(bottomDialog!=null)bottomDialog.cancel();
            }
        });
    }

    /**
     * 变更isOverHead状态
     */
    private void setTopchange(){
        if(map.containsKey("isOverHead")&&"1".equals(map.get("isOverHead"))){
            //先是置顶状态，，变为不置顶
            map.put("isOverHead","0");
        }else{
            map.put("isOverHead","1");
        }
        changeTitleView();
    }
    /**
     * 置顶回调
     */
    public interface OnTitleTopStateCallBack{
        public void onTitleTopStateCallBack(boolean isOk,boolean isConfirm,int position,Object data);
    }
    private OnTitleTopStateCallBack onTitleTopStateCallBack;
    public void setOnTitleTopStateCallBack(OnTitleTopStateCallBack onTitleTopStateCallBack,int position){
        this.position=position;
        this.onTitleTopStateCallBack=onTitleTopStateCallBack;
    }

    /**
     * 删除回调
     */
    public interface TitleDeleteSubjectCallBack{
        public void deleteSubjectPosition(int position);
    }
    private TitleDeleteSubjectCallBack deleteSubjectCallBack;
    public void setTitleDeleteSubjectCallBacks(TitleDeleteSubjectCallBack deleteSubjectCallBack,int position){
        this.position=position;
        this.deleteSubjectCallBack=deleteSubjectCallBack;
    }
}
